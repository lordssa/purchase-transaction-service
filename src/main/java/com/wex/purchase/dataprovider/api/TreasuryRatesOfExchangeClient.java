package com.wex.purchase.dataprovider.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Objects;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.wex.purchase.application.dataprovider.ExchangeRateDataProvider;
import com.wex.purchase.application.exception.CurrencyConversionUnavailableException;
import com.wex.purchase.application.exception.ExternalServiceUnavailableException;
import com.wex.purchase.dataprovider.api.response.TreasuryRatesResponse;

@Component
public class TreasuryRatesOfExchangeClient implements ExchangeRateDataProvider {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int MONTHS_WINDOW = 6;
    private static final String SERVICE_NAME = "Treasury rates of exchange";
    private static final String RATES_PATH = "/services/api/fiscal_service/v1/accounting/od/rates_of_exchange";
    private static final String SORT_DESC_RECORD_DATE = "-record_date";
    private static final int PAGE_SIZE_LATEST_ONLY = 1;

    private static final int MAX_ATTEMPTS = 4;
    private static final long INITIAL_BACKOFF_MS = 200;
    private static final long MAX_BACKOFF_MS = 2_000;

    private final RestClient restClient;

    public TreasuryRatesOfExchangeClient(
            RestClient.Builder builder,
            @Value("${purchase.treasury.base-url}") String baseUrl
    ) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public BigDecimal getLatestRateWithinSixMonths(String currency, String country, LocalDate purchaseDate) {
        Objects.requireNonNull(purchaseDate, "purchaseDate");
        requireNonBlank(currency, "currency");
        requireNonBlank(country, "country");

        final var normalizedCurrency = titleCase(currency);
        final var normalizedCountry = titleCase(country);

        final var filter = buildFilter(normalizedCountry, normalizedCurrency, purchaseDate);
        final var response = executeWithRetry(() -> fetchRates(filter));

        final var rateRaw = Optional.ofNullable(response)
                .map(TreasuryRatesResponse::data)
                .filter(treasuryRates -> !treasuryRates.isEmpty())
                .map(treasuryRates -> treasuryRates.getFirst().exchange_rate())
                .filter(rate -> rate != null && !rate.isBlank())
                .orElseThrow(() -> new CurrencyConversionUnavailableException(normalizedCurrency, purchaseDate));

        return parseRate(rateRaw, normalizedCurrency, purchaseDate);
    }

    private TreasuryRatesResponse fetchRates(String filter) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(RATES_PATH)
                        .queryParam("filter", filter)
                        .queryParam("sort", SORT_DESC_RECORD_DATE)
                        .queryParam("page[size]", PAGE_SIZE_LATEST_ONLY)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(TreasuryRatesResponse.class);
    }

    private static String buildFilter(String normalizedCountry, String normalizedCurrency, LocalDate purchaseDate) {
        LocalDate gte = purchaseDate.minusMonths(MONTHS_WINDOW);
        return "country_currency_desc:eq:" + normalizedCountry + "-" + normalizedCurrency
                + ",record_date:lte:" + purchaseDate.format(ISO_DATE)
                + ",record_date:gte:" + gte.format(ISO_DATE);
    }

    private static BigDecimal parseRate(String rateRaw, String normalizedCurrency, LocalDate purchaseDate) {
        try {
            return new BigDecimal(rateRaw.trim());
        } catch (NumberFormatException e) {
            throw new CurrencyConversionUnavailableException(normalizedCurrency, purchaseDate);
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be provided");
        }
    }

    private static String titleCase(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }

        String[] parts = trimmed.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder(trimmed.length());
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.isEmpty()) {
                continue;
            }
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) {
                sb.append(p.substring(1));
            }
        }
        return sb.toString();
    }

    private static <T> T executeWithRetry(Supplier<T> call) {
        long backoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return call.get();
            } catch (RestClientResponseException e) {
                if (!isRetryable(e) || attempt == MAX_ATTEMPTS) {
                    throw e;
                }
            } catch (ResourceAccessException e) {
                if (attempt == MAX_ATTEMPTS) {
                    throw new ExternalServiceUnavailableException(SERVICE_NAME, e);
                }
            } catch (RuntimeException e) {
                if (attempt == MAX_ATTEMPTS) {
                    throw new ExternalServiceUnavailableException(SERVICE_NAME, e);
                }
            }

            backoffMs = sleepAndBackoff(backoffMs);
        }

        throw new ExternalServiceUnavailableException(
                SERVICE_NAME,
                new IllegalStateException("Retry attempts exhausted")
        );
    }

    private static boolean isRetryable(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        return status == HttpStatus.TOO_MANY_REQUESTS.value() || (status >= 500 && status <= 599);
    }

    private static long sleepAndBackoff(long backoffMs) {
        try {
            Thread.sleep(backoffMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceUnavailableException(SERVICE_NAME, ie);
        }
        return Math.min(backoffMs * 2, MAX_BACKOFF_MS);
    }
}


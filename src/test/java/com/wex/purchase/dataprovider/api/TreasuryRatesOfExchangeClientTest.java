package com.wex.purchase.dataprovider.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;


import com.wex.purchase.application.exception.CurrencyConversionUnavailableException;

class TreasuryRatesOfExchangeClientTest {

    private static final String BASE = "http://localhost:9999";

    private MockRestServiceServer server;
    private TreasuryRatesOfExchangeClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new TreasuryRatesOfExchangeClient(builder, BASE);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.verify();
        }
    }

    @Test
    void getLatestRateWithinSixMonths_returnsFirstDataExchangeRate() {
        String json = """
                {
                  "data": [
                    {
                      "record_date": "2026-03-31",
                      "country": "Brazil",
                      "currency": "Real",
                      "exchange_rate": "5.477",
                      "effective_date": "2026-03-31"
                    }
                  ]
                }
                """;

        server.expect(
                requestTo(allOf(
                        containsString("/services/api/fiscal_service/v1/accounting/od/rates_of_exchange"),
                        containsString("country_currency_desc:eq:Brazil-Real"),
                        containsString("record_date:lte:2026-05-01"),
                        containsString("record_date:gte:2025-11-01"),
                        containsString("sort=-record_date"),
                        containsString("page%5Bsize%5D=1")
                ))
        ).andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        BigDecimal rate = client.getLatestRateWithinSixMonths(
                "real",
                "brazil",
                LocalDate.of(2026, 5, 1)
        );

        assertThat(rate).isEqualByComparingTo(new BigDecimal("5.477"));
    }

    @Test
    void getLatestRateWithinSixMonths_whenNoData_throwsConversionUnavailable() {
        server.expect(requestTo(containsString("/rates_of_exchange")))
                .andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.getLatestRateWithinSixMonths(
                "Real",
                "Brazil",
                LocalDate.of(2026, 5, 1)
        ))
                .isInstanceOf(CurrencyConversionUnavailableException.class);
    }

    @Test
    void getLatestRateWithinSixMonths_whenCurrencyBlank_throwsIllegalArgument() {
        assertThatThrownBy(() -> client.getLatestRateWithinSixMonths(
                "  ",
                "Brazil",
                LocalDate.of(2026, 5, 1)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currency");
    }
}

package com.wex.purchase.entrypoint.controller.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.wex.purchase.application.domain.ConvertedPurchaseTransaction;
import com.wex.purchase.application.domain.PurchaseTransaction;
import com.wex.purchase.entrypoint.controller.request.PurchaseTransactionRequest;

class PurchaseTransactionControllerMapperTest {

    @Test
    void toDomain_mapsFieldsAndScalesAmountToTwoDecimals() {
        PurchaseTransactionRequest request = new PurchaseTransactionRequest(
                "Paper",
                LocalDate.of(2026, 3, 1),
                new BigDecimal("99.999")
        );

        PurchaseTransaction domain = PurchaseTransactionControllerMapper.toDomain(request);

        assertThat(domain.transactionId()).isNull();
        assertThat(domain.description()).isEqualTo("Paper");
        assertThat(domain.transactionDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(domain.amountUsd()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void toResponse_fromPurchaseTransaction_setsExchangeFieldsNull() {
        UUID id = UUID.fromString("aaaaaaaa-bbbb-4ccc-dddd-eeeeeeeeeeee");
        PurchaseTransaction tx = new PurchaseTransaction(
                id,
                "Ink",
                LocalDate.of(2026, 4, 2),
                new BigDecimal("10.50")
        );

        var response = PurchaseTransactionControllerMapper.toResponse(tx);

        assertThat(response.transactionId()).isEqualTo(id);
        assertThat(response.description()).isEqualTo("Ink");
        assertThat(response.transactionDate()).isEqualTo(LocalDate.of(2026, 4, 2));
        assertThat(response.amountUsd()).isEqualByComparingTo(new BigDecimal("10.50"));
        assertThat(response.exchangeRateUsed()).isNull();
        assertThat(response.convertedAmount()).isNull();
    }

    @Test
    void toResponse_fromConvertedPurchaseTransaction_includesRateAndConvertedAmount() {
        UUID id = UUID.fromString("bbbbbbbb-cccc-4ddd-eeee-ffffffffffff");
        PurchaseTransaction tx = new PurchaseTransaction(
                id,
                "Chair",
                LocalDate.of(2026, 5, 7),
                new BigDecimal("100.00")
        );
        ConvertedPurchaseTransaction converted = new ConvertedPurchaseTransaction(tx, "BRL", new BigDecimal("5.50"));

        var response = PurchaseTransactionControllerMapper.toResponse(converted);

        assertThat(response.transactionId()).isEqualTo(id);
        assertThat(response.exchangeRateUsed()).isEqualByComparingTo(new BigDecimal("5.50"));
        assertThat(response.convertedAmount()).isEqualByComparingTo(new BigDecimal("550.00"));
    }
}

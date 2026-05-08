package com.wex.purchase.application.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ConvertedPurchaseTransactionTest {

    @Test
    void convertedAmount_multipliesUsdByRateAndRoundsHalfUpToTwoDecimals() {
        var tx = new PurchaseTransaction(
                UUID.randomUUID(),
                "Test",
                LocalDate.of(2026, 5, 1),
                new BigDecimal("12.233")
        );
        var converted = new ConvertedPurchaseTransaction(tx, "Real", new BigDecimal("5.254"));

        assertThat(converted.convertedAmount()).isEqualByComparingTo(new BigDecimal("64.27"));
    }
}

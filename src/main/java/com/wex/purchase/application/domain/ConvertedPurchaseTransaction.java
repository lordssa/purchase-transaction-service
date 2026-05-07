package com.wex.purchase.application.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;


 public record ConvertedPurchaseTransaction(
            PurchaseTransaction transaction,
            String targetCurrency,
            BigDecimal exchangeRateUsed
    ) {
        public BigDecimal convertedAmount() {
                return transaction.amountUsd()
                        .multiply(exchangeRateUsed())
                        .setScale(2, RoundingMode.HALF_UP);
        }
    }
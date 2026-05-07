package com.wex.purchase.application.exception;

import java.time.LocalDate;

public class CurrencyConversionUnavailableException extends RuntimeException {
    public CurrencyConversionUnavailableException(String currency, LocalDate purchaseDate) {
        super("Purchase cannot be converted to " + currency + " for transaction date " + purchaseDate);
    }
}


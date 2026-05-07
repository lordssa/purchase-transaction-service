package com.wex.purchase.application.exception;

import java.util.UUID;

public class PurchaseTransactionNotFoundException extends RuntimeException {
    public PurchaseTransactionNotFoundException(UUID transactionId) {
        super("Purchase transaction not found: " + transactionId);
    }
}


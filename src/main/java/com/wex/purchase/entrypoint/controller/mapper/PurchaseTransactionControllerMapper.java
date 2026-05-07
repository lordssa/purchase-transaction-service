package com.wex.purchase.entrypoint.controller.mapper;

import com.wex.purchase.application.domain.PurchaseTransaction;
import com.wex.purchase.entrypoint.controller.request.PurchaseTransactionRequest;
import com.wex.purchase.entrypoint.controller.response.PurchaseTransactionResponse;
import com.wex.purchase.application.domain.ConvertedPurchaseTransaction;

import java.math.RoundingMode;

public class PurchaseTransactionControllerMapper {
    public static PurchaseTransaction toDomain(PurchaseTransactionRequest purchaseTransactionRequest) {
        return new PurchaseTransaction(
                null,
                purchaseTransactionRequest.description(),
                purchaseTransactionRequest.transactionDate(),
                purchaseTransactionRequest.amountUsd().setScale(2, RoundingMode.HALF_UP)
        );
    }

    public static PurchaseTransactionResponse toResponse(
        PurchaseTransaction purchaseTransaction
    ) {
        return new PurchaseTransactionResponse(
            purchaseTransaction.transactionId(),
            purchaseTransaction.description(),
            purchaseTransaction.transactionDate(),
            purchaseTransaction.amountUsd(),
            null,
            null
        );
    }

    public static PurchaseTransactionResponse toResponse(
            ConvertedPurchaseTransaction convertedPurchaseTransaction
    ) {
        PurchaseTransaction purchaseTransaction = convertedPurchaseTransaction.transaction();
        return new PurchaseTransactionResponse(
            purchaseTransaction.transactionId(),
            purchaseTransaction.description(),
            purchaseTransaction.transactionDate(),
            purchaseTransaction.amountUsd(),
            convertedPurchaseTransaction.exchangeRateUsed(),
            convertedPurchaseTransaction.convertedAmount()
        );
    }
}

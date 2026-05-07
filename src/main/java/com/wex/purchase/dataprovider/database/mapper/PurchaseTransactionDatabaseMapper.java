package com.wex.purchase.dataprovider.database.mapper;

import com.wex.purchase.application.domain.PurchaseTransaction;
import com.wex.purchase.dataprovider.database.entity.PurchaseTransactionEntity;

public class PurchaseTransactionDatabaseMapper {

    public PurchaseTransactionEntity toEntity(PurchaseTransaction purchaseTransaction) {
        return new PurchaseTransactionEntity(
                purchaseTransaction.transactionId(),
                purchaseTransaction.description(),
                purchaseTransaction.transactionDate(),
                purchaseTransaction.amountUsd()
        );
    }

    public PurchaseTransaction toDomain(PurchaseTransactionEntity purchaseTransactionEntity) {
        return new PurchaseTransaction(
                purchaseTransactionEntity.getId(),
                purchaseTransactionEntity.getDescription(),
                purchaseTransactionEntity.getTransactionDate(),
                purchaseTransactionEntity.getAmountUsd()
        );
    }
    
}

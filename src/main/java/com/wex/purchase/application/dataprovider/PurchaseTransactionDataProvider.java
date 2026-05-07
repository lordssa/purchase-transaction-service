package com.wex.purchase.application.dataprovider;

import java.util.Optional;
import java.util.UUID;

import com.wex.purchase.application.domain.PurchaseTransaction;

public interface PurchaseTransactionDataProvider {
    UUID save(PurchaseTransaction purchaseTransaction);

    Optional<PurchaseTransaction> findById(UUID transactionId);
}

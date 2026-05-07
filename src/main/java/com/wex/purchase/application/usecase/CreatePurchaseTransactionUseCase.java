package com.wex.purchase.application.usecase;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wex.purchase.application.dataprovider.CorrelationReservationDataProvider;
import com.wex.purchase.application.domain.PurchaseTransaction;
import com.wex.purchase.application.dataprovider.PurchaseTransactionDataProvider;

@Service
public class CreatePurchaseTransactionUseCase {
    private final PurchaseTransactionDataProvider purchaseTransactionDataProvider;
    private final CorrelationReservationDataProvider correlationReservationDataProvider;

    public CreatePurchaseTransactionUseCase(
            PurchaseTransactionDataProvider purchaseTransactionDataProvider,
            CorrelationReservationDataProvider correlationReservationDataProvider
    ) {
        this.purchaseTransactionDataProvider = purchaseTransactionDataProvider;
        this.correlationReservationDataProvider = correlationReservationDataProvider;
    }

    @Transactional
    public PurchaseTransaction execute(UUID requestId, PurchaseTransaction purchaseTransaction) {
        correlationReservationDataProvider.reserve(requestId);

        final var transactionId = this.purchaseTransactionDataProvider.save(purchaseTransaction);
        return new PurchaseTransaction(
                transactionId,
                purchaseTransaction.description(),
                purchaseTransaction.transactionDate(),
                purchaseTransaction.amountUsd()
        );
    }

}

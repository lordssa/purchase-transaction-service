package com.wex.purchase.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.wex.purchase.application.dataprovider.ExchangeRateDataProvider;
import com.wex.purchase.application.dataprovider.PurchaseTransactionDataProvider;
import com.wex.purchase.application.domain.ConvertedPurchaseTransaction;
import com.wex.purchase.application.exception.PurchaseTransactionNotFoundException;

@Service
public class GetPurchaseTransactionConvertedUseCase {

    private final PurchaseTransactionDataProvider purchaseTransactionDataProvider;
    private final ExchangeRateDataProvider exchangeRateDataProvider;

    public GetPurchaseTransactionConvertedUseCase(
            PurchaseTransactionDataProvider purchaseTransactionDataProvider,
            ExchangeRateDataProvider exchangeRateDataProvider
    ) {
        this.purchaseTransactionDataProvider = purchaseTransactionDataProvider;
        this.exchangeRateDataProvider = exchangeRateDataProvider;
    }

    public ConvertedPurchaseTransaction execute(UUID transactionId, String currency, String country) {
        final var purchaseTransaction = purchaseTransactionDataProvider
                .findById(transactionId)
                .orElseThrow(() -> new PurchaseTransactionNotFoundException(transactionId));

        final var rate = exchangeRateDataProvider.getLatestRateWithinSixMonths(currency, country, purchaseTransaction.transactionDate());
        return new ConvertedPurchaseTransaction(purchaseTransaction, currency, rate);
    }

}


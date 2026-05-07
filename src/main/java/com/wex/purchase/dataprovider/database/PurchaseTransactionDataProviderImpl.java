package com.wex.purchase.dataprovider.database;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.wex.purchase.application.dataprovider.PurchaseTransactionDataProvider;
import com.wex.purchase.application.domain.PurchaseTransaction;
import com.wex.purchase.dataprovider.database.mapper.PurchaseTransactionDatabaseMapper;
import com.wex.purchase.dataprovider.database.repository.PurchaseTransactionRepository;

import java.util.Optional;

@Component
public class PurchaseTransactionDataProviderImpl implements PurchaseTransactionDataProvider {
    private final PurchaseTransactionRepository repository;
    private final PurchaseTransactionDatabaseMapper mapper;

    public PurchaseTransactionDataProviderImpl(PurchaseTransactionRepository repository) {
        this.repository = repository;
        this.mapper = new PurchaseTransactionDatabaseMapper();
    }

    @Override
    public UUID save(PurchaseTransaction purchaseTransaction) {
        return Optional.of(purchaseTransaction)
        .map(mapper::toEntity)
        .map(repository::save)
        .orElseThrow(() -> new IllegalArgumentException("Purchase transaction could not be saved"))
        .getId();
    }

    @Override
    public Optional<PurchaseTransaction> findById(UUID transactionId) {
        return repository.findById(transactionId).map(mapper::toDomain);
    }
}

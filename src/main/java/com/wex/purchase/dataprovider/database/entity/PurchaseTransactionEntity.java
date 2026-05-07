package com.wex.purchase.dataprovider.database.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "purchase_transaction")
public class PurchaseTransactionEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 50)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "amount_usd", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountUsd;

    protected PurchaseTransactionEntity() {}

    public PurchaseTransactionEntity(UUID id, String description, LocalDate transactionDate, BigDecimal amountUsd) {
        this.id = id;
        this.description = description;
        this.transactionDate = transactionDate;
        this.amountUsd = amountUsd;
    }

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public BigDecimal getAmountUsd() {
        return amountUsd;
    }
}

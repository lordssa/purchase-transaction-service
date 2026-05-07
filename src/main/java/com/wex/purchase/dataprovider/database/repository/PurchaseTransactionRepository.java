package com.wex.purchase.dataprovider.database.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wex.purchase.dataprovider.database.entity.PurchaseTransactionEntity;

@Repository
public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransactionEntity, UUID> {}

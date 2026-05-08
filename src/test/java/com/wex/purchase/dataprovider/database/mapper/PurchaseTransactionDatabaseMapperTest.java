package com.wex.purchase.dataprovider.database.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.wex.purchase.application.domain.PurchaseTransaction;
import com.wex.purchase.dataprovider.database.entity.PurchaseTransactionEntity;

class PurchaseTransactionDatabaseMapperTest {

    private final PurchaseTransactionDatabaseMapper mapper = new PurchaseTransactionDatabaseMapper();

    @Test
    void toEntity_and_toDomain_roundTrip() {
        var id = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
        var domain = new PurchaseTransaction(
                id,
                "Paper",
                LocalDate.of(2026, 4, 15),
                new BigDecimal("99.99")
        );

        PurchaseTransactionEntity entity = mapper.toEntity(domain);
        PurchaseTransaction back = mapper.toDomain(entity);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getDescription()).isEqualTo("Paper");
        assertThat(entity.getTransactionDate()).isEqualTo(LocalDate.of(2026, 4, 15));
        assertThat(entity.getAmountUsd()).isEqualByComparingTo("99.99");

        assertThat(back).isEqualTo(domain);
    }
}

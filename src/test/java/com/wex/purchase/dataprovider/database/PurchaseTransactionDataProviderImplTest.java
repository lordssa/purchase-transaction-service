package com.wex.purchase.dataprovider.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wex.purchase.application.domain.PurchaseTransaction;
import com.wex.purchase.dataprovider.database.entity.PurchaseTransactionEntity;
import com.wex.purchase.dataprovider.database.repository.PurchaseTransactionRepository;

@ExtendWith(MockitoExtension.class)
class PurchaseTransactionDataProviderImplTest {

    @Mock
    private PurchaseTransactionRepository repository;

    @InjectMocks
    private PurchaseTransactionDataProviderImpl dataProvider;

    @Test
    void save_mapsEntityAndReturnsIdFromRepository() {
        var domain = new PurchaseTransaction(
                null,
                "Desk",
                LocalDate.of(2026, 5, 1),
                new BigDecimal("10.00")
        );
        var savedId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        when(repository.save(any(PurchaseTransactionEntity.class)))
                .thenAnswer(inv -> {
                    PurchaseTransactionEntity e = inv.getArgument(0);
                    return new PurchaseTransactionEntity(
                            savedId,
                            e.getDescription(),
                            e.getTransactionDate(),
                            e.getAmountUsd()
                    );
                });

        UUID result = dataProvider.save(domain);

        assertThat(result).isEqualTo(savedId);
        verify(repository).save(any(PurchaseTransactionEntity.class));
    }

    @Test
    void findById_whenPresent_returnsMappedDomain() {
        var id = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        var entity = new PurchaseTransactionEntity(
                id,
                "Chair",
                LocalDate.of(2026, 6, 1),
                new BigDecimal("42.50")
        );
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        Optional<PurchaseTransaction> out = dataProvider.findById(id);

        assertThat(out).isPresent();
        assertThat(out.get().transactionId()).isEqualTo(id);
        assertThat(out.get().description()).isEqualTo("Chair");
        assertThat(out.get().transactionDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(out.get().amountUsd()).isEqualByComparingTo("42.50");
    }

    @Test
    void findById_whenAbsent_returnsEmpty() {
        var id = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThat(dataProvider.findById(id)).isEmpty();
    }

    @Test
    void save_withNullDomain_throwsBeforeRepository() {
        assertThatThrownBy(() -> dataProvider.save(null))
                .isInstanceOf(NullPointerException.class);
        verify(repository, never()).save(any());
    }
}

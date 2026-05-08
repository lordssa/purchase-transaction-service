package com.wex.purchase.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wex.purchase.application.dataprovider.CorrelationReservationDataProvider;
import com.wex.purchase.application.dataprovider.PurchaseTransactionDataProvider;
import com.wex.purchase.application.domain.PurchaseTransaction;
import com.wex.purchase.application.exception.DuplicateCorrelationException;

@ExtendWith(MockitoExtension.class)
class CreatePurchaseTransactionUseCaseTest {

    @Mock
    private PurchaseTransactionDataProvider purchaseTransactionDataProvider;

    @Mock
    private CorrelationReservationDataProvider correlationReservationDataProvider;

    @InjectMocks
    private CreatePurchaseTransactionUseCase useCase;

    private UUID requestId;
    private PurchaseTransaction input;

    @BeforeEach
    void setUp() {
        requestId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        input = new PurchaseTransaction(
                null,
                "Office supplies",
                LocalDate.of(2026, 5, 5),
                new BigDecimal("12.35")
        );
    }

    @Test
    void execute_reservesRequestId_savesAndReturnsTransactionWithGeneratedId() {
        UUID savedId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(purchaseTransactionDataProvider.save(input)).thenReturn(savedId);

        PurchaseTransaction result = useCase.execute(requestId, input);

        verify(correlationReservationDataProvider).reserve(requestId);
        verify(purchaseTransactionDataProvider).save(input);
        assertThat(result.transactionId()).isEqualTo(savedId);
        assertThat(result.description()).isEqualTo(input.description());
        assertThat(result.transactionDate()).isEqualTo(input.transactionDate());
        assertThat(result.amountUsd()).isEqualByComparingTo(input.amountUsd());
    }

    @Test
    void execute_whenReservationFails_doesNotSaveTransaction() {
        doThrow(new DuplicateCorrelationException("duplicate")).when(correlationReservationDataProvider)
                .reserve(requestId);

        assertThatThrownBy(() -> useCase.execute(requestId, input))
                .isInstanceOf(DuplicateCorrelationException.class);

        verify(purchaseTransactionDataProvider, never()).save(any());
    }
}

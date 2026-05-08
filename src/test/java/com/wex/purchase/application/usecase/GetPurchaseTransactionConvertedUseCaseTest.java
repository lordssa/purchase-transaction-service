package com.wex.purchase.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wex.purchase.application.dataprovider.ExchangeRateDataProvider;
import com.wex.purchase.application.dataprovider.PurchaseTransactionDataProvider;
import com.wex.purchase.application.domain.ConvertedPurchaseTransaction;
import com.wex.purchase.application.domain.PurchaseTransaction;
import com.wex.purchase.application.exception.PurchaseTransactionNotFoundException;

@ExtendWith(MockitoExtension.class)
class GetPurchaseTransactionConvertedUseCaseTest {

    @Mock
    private PurchaseTransactionDataProvider purchaseTransactionDataProvider;

    @Mock
    private ExchangeRateDataProvider exchangeRateDataProvider;

    @InjectMocks
    private GetPurchaseTransactionConvertedUseCase useCase;

    private UUID transactionId;
    private PurchaseTransaction stored;

    @BeforeEach
    void setUp() {
        transactionId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        stored = new PurchaseTransaction(
                transactionId,
                "Office supplies",
                LocalDate.of(2026, 5, 1),
                new BigDecimal("100.00")
        );
    }

    @Test
    void execute_whenTransactionMissing_throwsNotFound() {
        when(purchaseTransactionDataProvider.findById(transactionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(transactionId, "Real", "Brazil"))
                .isInstanceOf(PurchaseTransactionNotFoundException.class);

        verifyNoInteractions(exchangeRateDataProvider);
    }

    @Test
    void execute_whenTransactionFound_fetchesRateAndReturnsConvertedWrapper() {
        when(purchaseTransactionDataProvider.findById(transactionId)).thenReturn(Optional.of(stored));
        BigDecimal rate = new BigDecimal("5.254");
        when(exchangeRateDataProvider.getLatestRateWithinSixMonths("Real", "Brazil", stored.transactionDate()))
                .thenReturn(rate);

        ConvertedPurchaseTransaction result = useCase.execute(transactionId, "Real", "Brazil");

        verify(exchangeRateDataProvider).getLatestRateWithinSixMonths(
                eq("Real"),
                eq("Brazil"),
                eq(LocalDate.of(2026, 5, 1))
        );
        assertThat(result.transaction()).isEqualTo(stored);
        assertThat(result.targetCurrency()).isEqualTo("Real");
        assertThat(result.exchangeRateUsed()).isEqualByComparingTo(rate);
        assertThat(result.convertedAmount()).isEqualByComparingTo(new BigDecimal("525.40"));
    }
}

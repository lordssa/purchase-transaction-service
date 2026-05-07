package com.wex.purchase.entrypoint.controller.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PurchaseTransactionResponse(
        UUID transactionId,
        String description,
        LocalDate transactionDate,
        BigDecimal amountUsd,
        BigDecimal exchangeRateUsed,
        BigDecimal convertedAmount
) {}

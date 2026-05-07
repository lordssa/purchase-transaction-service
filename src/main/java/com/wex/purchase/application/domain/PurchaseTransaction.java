package com.wex.purchase.application.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PurchaseTransaction(
        UUID transactionId,
        String description,
        LocalDate transactionDate,
        BigDecimal amountUsd
) {}

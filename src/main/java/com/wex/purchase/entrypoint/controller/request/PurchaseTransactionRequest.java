package com.wex.purchase.entrypoint.controller.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PurchaseTransactionRequest(
        @NotBlank
        @Size(max = 50)
        String description,

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate transactionDate,

        @NotNull
        @DecimalMin(value = "0.00", inclusive = false)
        BigDecimal amountUsd
) {}

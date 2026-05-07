package com.wex.purchase.entrypoint.controller.error;

public record FieldValidationError(
        String field,
        String message
) {}


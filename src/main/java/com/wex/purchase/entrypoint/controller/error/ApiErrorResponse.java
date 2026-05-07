package com.wex.purchase.entrypoint.controller.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String message,
        String path,
        List<FieldValidationError> errors
) {}


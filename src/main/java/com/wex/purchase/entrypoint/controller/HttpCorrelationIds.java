package com.wex.purchase.entrypoint.controller;

import java.util.UUID;

import com.wex.purchase.application.exception.InvalidCorrelationIdFormatException;
import com.wex.purchase.application.exception.MissingCorrelationIdException;

import io.micrometer.common.util.StringUtils;

final class HttpCorrelationIds {

    static final String X_REQUEST_ID = "X-Request-Id";

    private HttpCorrelationIds() {}

    static UUID parseRequestId(String requestIdRaw) {
        if (StringUtils.isBlank(requestIdRaw)) {
            throw new MissingCorrelationIdException(
                    X_REQUEST_ID + " header is required and must be an UUID v4"
            );
        }

        try {
            return UUID.fromString(requestIdRaw.trim());
        } catch (IllegalArgumentException e) {
            throw new InvalidCorrelationIdFormatException();
        }
    }
}

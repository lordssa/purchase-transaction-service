package com.wex.purchase.application.exception;

public class DuplicateCorrelationException extends RuntimeException {
    public DuplicateCorrelationException(String message) {
        super(message);
    }
}

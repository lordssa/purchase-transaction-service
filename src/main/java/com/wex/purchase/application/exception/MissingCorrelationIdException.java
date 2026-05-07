package com.wex.purchase.application.exception;

public class MissingCorrelationIdException extends RuntimeException {
    public MissingCorrelationIdException(String message) {
        super(message);
    }
}

package com.wex.purchase.application.exception;

public class InvalidCorrelationIdFormatException extends RuntimeException {
    public InvalidCorrelationIdFormatException() {
        super("Correlation identifier must be a valid UUID");
    }
}

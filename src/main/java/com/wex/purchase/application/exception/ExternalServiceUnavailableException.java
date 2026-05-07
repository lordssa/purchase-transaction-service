package com.wex.purchase.application.exception;

public class ExternalServiceUnavailableException extends RuntimeException {
    public ExternalServiceUnavailableException(String serviceName, Throwable cause) {
        super(serviceName + " service is temporarily unavailable", cause);
    }
}


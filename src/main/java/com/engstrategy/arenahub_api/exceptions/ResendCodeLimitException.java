package com.engstrategy.arenahub_api.exceptions;

public class ResendCodeLimitException extends RuntimeException {
    public ResendCodeLimitException(String message) {
        super(message);
    }
}

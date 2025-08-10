package com.engstrategy.alugai_api.exceptions;

public class ResendCodeLimitException extends RuntimeException {
    public ResendCodeLimitException(String message) {
        super(message);
    }
}

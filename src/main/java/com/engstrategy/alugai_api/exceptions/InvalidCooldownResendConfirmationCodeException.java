package com.engstrategy.alugai_api.exceptions;

public class InvalidCooldownResendConfirmationCodeException extends RuntimeException {
    public InvalidCooldownResendConfirmationCodeException(String message) {
        super(message);
    }
}

package com.engstrategy.arenahub_api.exceptions;

public class InvalidCooldownResendConfirmationCodeException extends RuntimeException {
    public InvalidCooldownResendConfirmationCodeException(String message) {
        super(message);
    }
}

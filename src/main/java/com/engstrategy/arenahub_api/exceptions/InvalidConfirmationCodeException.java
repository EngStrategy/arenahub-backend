package com.engstrategy.arenahub_api.exceptions;

public class InvalidConfirmationCodeException extends RuntimeException {
    public InvalidConfirmationCodeException(String message) {
        super(message);
    }
}

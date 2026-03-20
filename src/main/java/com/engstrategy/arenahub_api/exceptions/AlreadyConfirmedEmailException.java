package com.engstrategy.arenahub_api.exceptions;

public class AlreadyConfirmedEmailException extends RuntimeException {
    public AlreadyConfirmedEmailException(String message) {
        super(message);
    }
}

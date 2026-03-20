package com.engstrategy.arenahub_api.exceptions;

public class EmailUnconfirmedException extends RuntimeException {
    public EmailUnconfirmedException(String message) {
        super(message);
    }
}

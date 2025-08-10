package com.engstrategy.alugai_api.exceptions;

public class EmailUnconfirmedException extends RuntimeException {
    public EmailUnconfirmedException(String message) {
        super(message);
    }
}

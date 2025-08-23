package com.engstrategy.alugai_api.exceptions;

public class AlreadyConfirmedEmailException extends RuntimeException {
    public AlreadyConfirmedEmailException(String message) {
        super(message);
    }
}

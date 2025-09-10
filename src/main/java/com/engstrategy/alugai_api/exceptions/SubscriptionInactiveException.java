package com.engstrategy.alugai_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SubscriptionInactiveException extends RuntimeException {
    public SubscriptionInactiveException(String message) {
        super(message);
    }
}
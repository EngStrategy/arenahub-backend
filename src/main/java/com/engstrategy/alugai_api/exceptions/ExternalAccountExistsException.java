package com.engstrategy.alugai_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // Retorna HTTP 409 Conflict
public class ExternalAccountExistsException extends RuntimeException {
    public ExternalAccountExistsException(String message) {
        super(message);
    }
}
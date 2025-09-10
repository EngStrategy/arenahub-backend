package com.engstrategy.alugai_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class LimiteDeQuadrasExcedidoException extends RuntimeException {
    public LimiteDeQuadrasExcedidoException(String message) {
        super(message);
    }
}
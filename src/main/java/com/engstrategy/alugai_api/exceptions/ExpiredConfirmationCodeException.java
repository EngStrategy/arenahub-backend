package com.engstrategy.alugai_api.exceptions;

public class ExpiredConfirmationCodeException extends RuntimeException {
  public ExpiredConfirmationCodeException(String message) {
    super(message);
  }
}

package com.engstrategy.arenahub_api.exceptions;

public class ExpiredConfirmationCodeException extends RuntimeException {
  public ExpiredConfirmationCodeException(String message) {
    super(message);
  }
}

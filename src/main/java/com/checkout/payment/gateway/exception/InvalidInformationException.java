package com.checkout.payment.gateway.exception;

public class InvalidInformationException extends RuntimeException{
  public InvalidInformationException(String message) {
    super(message);
  }
}

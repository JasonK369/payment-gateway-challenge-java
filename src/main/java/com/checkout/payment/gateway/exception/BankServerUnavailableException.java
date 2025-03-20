package com.checkout.payment.gateway.exception;

public class BankServerUnavailableException extends RuntimeException{
  public BankServerUnavailableException(String message) {
    super(message);
  }
}

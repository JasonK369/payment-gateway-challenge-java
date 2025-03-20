package com.checkout.payment.gateway.exception;

public class MissingPaymentInformationException extends RuntimeException{
  public MissingPaymentInformationException(String message) {
    super(message);
  }
}

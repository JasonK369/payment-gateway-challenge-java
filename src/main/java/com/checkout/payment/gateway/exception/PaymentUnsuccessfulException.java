package com.checkout.payment.gateway.exception;

public class PaymentUnsuccessfulException extends RuntimeException{
  public PaymentUnsuccessfulException(String message) {
    super(message);
  }

}

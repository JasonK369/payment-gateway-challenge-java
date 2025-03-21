package com.checkout.payment.gateway.exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException{
  public PaymentNotFoundException(UUID uuid) {
    super(String.format("Payment not found, id = %s", uuid));
  }

}

package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PaymentDetail {
  private UUID id;
  private PaymentStatus status;
  private String cardNumber;
  private int expiryMonth;
  private int expiryYear;
  private String currency;
  private int amount;
}

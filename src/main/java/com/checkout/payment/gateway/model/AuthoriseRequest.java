package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthoriseRequest {

  @JsonProperty("card_number")
  private String cardNumber;

  @JsonProperty("expiry_date")
  private String expiryDate;

  private String currency;

  private int amount;

  private String cvv;

  @Override
  public String toString() {
    return """
        {
          cardNumber: %s,
          expiryDate: %s,
          currency: %s,
          int: %d,
          cvv: %s
        }
        """.formatted(
        "*".repeat(cardNumber.length() - 4) + cardNumber.substring(cardNumber.length() - 4),
        expiryDate,
        currency,
        amount,
        cvv
    );
  }
}

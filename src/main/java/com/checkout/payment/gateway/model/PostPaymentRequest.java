package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class PostPaymentRequest implements Serializable {

  @NotEmpty(message = "card number cannot be null")
  @Size(min = 14, max = 19, message = "card number between 14-19 characters long")
  @JsonProperty("card_number")
  private String cardNumber;

  @NotNull(message = "expiry month cannot be null")
  @JsonProperty("expiry_month")
  private Integer expiryMonth;

  @NotNull(message = "expiry year cannot be null")
  @JsonProperty("expiry_year")
  private Integer expiryYear;

  @NotEmpty(message = "currency cannot be null")
  @Size(min = 3, max = 3, message = "currency be 3 characters")
  private String currency;

  @NotNull(message = "amount cannot be null")
  @Positive(message = "The user's Id must be greater than 0")
  private Integer amount;

  @NotEmpty(message = "cvv cannot be null")
  @Size(min = 3, max = 4, message = "cvv must be 3-4 characters long")
  private String cvv;

  public String getExpiryDate() {
    return String.format("%02d/%d", expiryMonth, expiryYear);
  }

  @Override
  public String toString() {
    return "PostPaymentRequest{" +
        "cardNumber=" + cardNumber +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        ", cvv=" + cvv +
        '}';
  }
}

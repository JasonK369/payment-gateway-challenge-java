package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@AllArgsConstructor
public class AuthoriseRequest {
  private String cardNumber;

  @JsonProperty("expiry_date")
  private String expiryDate;

  private String currency;

  private int amount;

  private String cvv;
}

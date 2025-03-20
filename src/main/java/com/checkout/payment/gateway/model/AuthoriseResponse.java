package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthoriseResponse {
  private Boolean authorized;

  @JsonProperty("authorization_code")
  private String authorizationCode;

}

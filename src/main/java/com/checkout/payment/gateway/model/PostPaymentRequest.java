package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Pattern.Flag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class PostPaymentRequest implements Serializable {
  public static final String INVALID_CARD_NUMBER_MESSAGE = "card number between 14-19 characters long";
  public static final String EXPIRY_MONTH_OUT_OF_RANGE_MESSAGE = "Value must be between 1-12";
  public static final String CARD_NUMBER_CANNOT_BE_NULL = "card number cannot be null";
  public static final String EXPIRY_MONTH_CANNOT_BE_NULL = "expiry month cannot be null";
  public static final String EXPIRY_YEAR_CANNOT_BE_NULL = "expiry year cannot be null";
  public static final String CURRENCY_CANNOT_BE_NULL = "currency cannot be null";
  public static final String AMOUNT_CANNOT_BE_NULL = "amount cannot be null";
  public static final String CVV_CANNOT_BE_NULL = "cvv cannot be null";
  public static final String CURRENCY_OUT_OF_RANGE_MESSAGE = "currency be 3 characters";
  public static final String AMOUNT_MUST_BE_POSITIVE_MESSAGE = "The amount must be greater than 0";

  private static final String CVV_REGEX = "^[0-9]{3,4}$";
  public static final String INVALID_CVV_MESSAGE = "cvv cannot contains non-numeric character(s) and should 3-4 characters long";

  @NotEmpty(message = CARD_NUMBER_CANNOT_BE_NULL)
  @Size(min = 14, max = 19, message = INVALID_CARD_NUMBER_MESSAGE)
  @JsonProperty("card_number")
  private String cardNumber;

  @NotNull(message = EXPIRY_MONTH_CANNOT_BE_NULL)
  @JsonProperty("expiry_month")
  @Min(value = 1, message = EXPIRY_MONTH_OUT_OF_RANGE_MESSAGE)
  @Max(value = 12, message = EXPIRY_MONTH_OUT_OF_RANGE_MESSAGE)
  private Integer expiryMonth;

  @NotNull(message = EXPIRY_YEAR_CANNOT_BE_NULL)
  @JsonProperty("expiry_year")
  private Integer expiryYear;

  @NotEmpty(message = CURRENCY_CANNOT_BE_NULL)
  @Size(min = 3, max = 3, message = CURRENCY_OUT_OF_RANGE_MESSAGE)
  private String currency;

  @NotNull(message = AMOUNT_CANNOT_BE_NULL)
  @Positive(message = AMOUNT_MUST_BE_POSITIVE_MESSAGE)
  private Integer amount;

  @NotEmpty(message = CVV_CANNOT_BE_NULL)
  @Pattern(regexp = CVV_REGEX,flags = { Flag.CASE_INSENSITIVE}, message = INVALID_CVV_MESSAGE)
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

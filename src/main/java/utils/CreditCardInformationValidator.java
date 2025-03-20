package utils;

import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;

public class CreditCardInformationValidator {

  private static final String CVV_VALIDATION_REGEX = "^[0-9]{3,4}$";

  public static boolean isValidCardNumber(String cardNumber) {
    return cardNumber.length() >= 14 && cardNumber.length() <= 19;
  }

  public static boolean isCardExpired(int expiryMonth, int expiryYear){
    LocalDate now = LocalDate.now().withDayOfMonth(1);
    LocalDate cardExpiry = LocalDate.of(expiryYear, Month.of(expiryMonth), 1);
    return cardExpiry.toEpochDay() <= now.toEpochDay();
  }

  public static boolean isValidCvv(String cvv){
    return Objects.nonNull(cvv) && cvv.matches(CVV_VALIDATION_REGEX);
  }

}

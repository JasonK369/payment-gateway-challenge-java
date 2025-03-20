package com.checkout.payment.gateway.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;


import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static utils.CreditCardInformationValidator.isCardExpired;
import static utils.CreditCardInformationValidator.isValidCardNumber;
import static utils.CreditCardInformationValidator.isValidCvv;

public class CreditCardInformationValidatorTest {

  private MockedStatic<Clock> clockMock;

  @BeforeEach
  void beforeEach(){
    Clock spyClock = spy(Clock.systemDefaultZone());
    clockMock = mockStatic(Clock.class);
    clockMock.when(Clock::systemDefaultZone).thenReturn(spyClock);
    when(spyClock.instant()).thenReturn(Instant.ofEpochSecond(1740787200));
  }

  @AfterEach
  void afterEach(){
    clockMock.close();
  }

  @ParameterizedTest
  @CsvSource({"36227206271667", "378282246310005", "4242424242424242", "42424242424242420",
      "620550000000000000", "6205500000000000004"})
  void validCardNumber(String cardNumber) {
    assertTrue(isValidCardNumber(cardNumber));
  }

  @ParameterizedTest
  @CsvSource({"1", "10", "13", "20", "100"})
  void invalidCardNumber(int cardNumberLength) {
    assertFalse(isValidCardNumber("0".repeat(cardNumberLength)));
  }

  @ParameterizedTest
  @CsvSource(value = {"12/2025", "1/2345", "12/2525"}, delimiterString = "/")
  void expiryDateNotPassed(int expiryMonth, int expiryYear) {
    assertTrue(isCardExpired(expiryMonth, expiryYear));
  }

  @ParameterizedTest
  @CsvSource(value = {"12/1212", "1/1984", "12/1999"}, delimiterString = "/")
  void expiryDatePassed(int expiryMonth, int expiryYear) {
    assertFalse(isCardExpired(expiryMonth, expiryYear));
  }

  @ParameterizedTest
  @CsvSource(value = {"0/1212", "13/1984", "71/2025"}, delimiterString = "/")
  void invalidExpiryMonth(int expiryMonth, int expiryYear) {
    assertThrows(DateTimeException.class, () -> isCardExpired(expiryMonth, expiryYear));
  }

  @ParameterizedTest
  @CsvSource(value = {"247", "123", "012", "9999"})
  void validCVV(String cvv){
    assertTrue(isValidCvv(cvv));
  }

  @ParameterizedTest
  @CsvSource(value = {"247123199", "1", "76", "6AB", "XX", "!!", "\"\""})
  void invalidCVV(String cvv){
    assertFalse(isValidCvv(cvv));
  }

  @Test
  void nullCVV(){
    assertFalse(isValidCvv(null));
  }

}

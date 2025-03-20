package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.exception.BankServerUnavailableException;
import com.checkout.payment.gateway.exception.MissingPaymentInformationException;
import com.checkout.payment.gateway.model.AuthoriseRequest;
import com.checkout.payment.gateway.model.AuthoriseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
public class PaymentHandlingServiceTest {

  @Mock
  private RestTemplate mockRestTemplate;

  private AuthoriseResponse authoriseResponse;

  @Mock
  private ResponseEntity mockResponseEntity;

  @InjectMocks
  private PaymentHandlingService paymentHandlingService;

  private static final String RANDOM_AUTH_CODE = "random_auth_code";

  @BeforeEach
  void beforeEach() {
    openMocks(PaymentHandlingService.class);
  }

  @Test
  void return400WhenMissingInformation(){
    when(mockRestTemplate.postForEntity(anyString(), any(), any())).thenThrow(
        new HttpClientErrorException(
            HttpStatusCode.valueOf(400)));


    assertThrows(MissingPaymentInformationException.class, () -> paymentHandlingService.authorisePayment(new AuthoriseRequest(
        "cardNumber",
        "",
        "GBP",
        100,
        "123"
    )));

    verify(mockRestTemplate, times(1)).postForEntity(anyString(), any(), any());
  }

  @Test
  void paymentAuthorise(){
    when(mockRestTemplate.postForEntity(anyString(), any(), any())).thenReturn(mockResponseEntity);
    when(mockResponseEntity.getBody()).thenReturn(new AuthoriseResponse(
        true,
        RANDOM_AUTH_CODE
    ));

    AuthoriseResponse response = paymentHandlingService.authorisePayment(new AuthoriseRequest(
        "cardNumber",
        "10/2025",
        "GBP",
        100,
        "123"
    ));

    assertTrue(response.getAuthorized());
    assertEquals(RANDOM_AUTH_CODE , response.getAuthorizationCode());
    verify(mockRestTemplate, times(1)).postForEntity(anyString(), any(), any());
  }

  @Test
  void paymentUnauthorise(){
    when(mockRestTemplate.postForEntity(anyString(), any(), any())).thenReturn(mockResponseEntity);
    when(mockResponseEntity.getBody()).thenReturn(new AuthoriseResponse(
        false,
        RANDOM_AUTH_CODE
    ));

    AuthoriseResponse response = paymentHandlingService.authorisePayment(new AuthoriseRequest(
        "cardNumber",
        "10/2025",
        "GBP",
        100,
        "123"
    ));

    assertFalse(response.getAuthorized());
    assertEquals(RANDOM_AUTH_CODE , response.getAuthorizationCode());
    verify(mockRestTemplate, times(1)).postForEntity(anyString(), any(), any());
  }

  @Test
  void bankUnavailable(){
    when(mockRestTemplate.postForEntity(anyString(), any(), any())).thenThrow(
        new HttpClientErrorException(
            HttpStatusCode.valueOf(503)));

    assertThrows(BankServerUnavailableException.class,  () -> paymentHandlingService.authorisePayment(new AuthoriseRequest(
        "cardNumber",
        "10/2025",
        "GBP",
        100,
        "123"
    )));

    verify(mockRestTemplate, times(1)).postForEntity(anyString(), any(), any());
  }

}

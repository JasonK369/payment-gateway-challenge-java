package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.InvalidInformationException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.AuthoriseResponse;
import com.checkout.payment.gateway.model.GetPaymentResponse;
import com.checkout.payment.gateway.model.PaymentDetail;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
public class PaymentGatewayServiceTest {
  @Mock
  private PaymentHandlingService mockPaymentHandlingService;

  @Mock
  private PaymentsRepository mockPaymentsRepository;

  @InjectMocks
  private PaymentGatewayService paymentGatewayService;

  private PostPaymentRequest postPaymentRequest;

  private MockedStatic<Clock> clockMock;

  private static final int MAR_1_2025_UNIX_TIMESTAMP = 1740787200;
  private static final String RANDOM_AUTH_CODE = "random_auth_code";
  private static final String CARD_NUMBER = "4242424242424242";
  private static final int EXPIRE_MONTH = 10;
  private static final int EXPIRE_YEAR = 2025;
  private static final String CURRENCY = "GBP";
  private static final int AMOUNT = 100;
  private static final String CVV = "012";
  private static final int NUMBER_OF_DIGIT_TO_SHOW = 4;

  @BeforeEach
  void beforeEach(){
    postPaymentRequest = new PostPaymentRequest(
        CARD_NUMBER,
        EXPIRE_MONTH,
        EXPIRE_YEAR,
        CURRENCY,
        AMOUNT,
        CVV);

    openMocks(PaymentGatewayService.class);
  }

  @Test
  void paymentSuccessfully(){
    mockClock();
    when(mockPaymentHandlingService.authorisePayment(any())).thenReturn(new AuthoriseResponse(
        true, RANDOM_AUTH_CODE
    ));

    PostPaymentResponse postPaymentResponse = paymentGatewayService.processPayment(postPaymentRequest);

    verifyPostPaymentResponseWhenAuthorised(postPaymentResponse);

    clockMock.close();
  }

  @Test
  void paymentDeclined(){
    mockClock();
    when(mockPaymentHandlingService.authorisePayment(any())).thenReturn(new AuthoriseResponse(
        false, RANDOM_AUTH_CODE
    ));

    PostPaymentResponse postPaymentResponse = paymentGatewayService.processPayment(postPaymentRequest);

    verifyPostPaymentResponseWhenDeclined(postPaymentResponse);

    clockMock.close();
  }

  @Test
  void nonExistCurrencyCode(){
    postPaymentRequest.setCurrency("AAA");

    assertThrows(InvalidInformationException.class, () -> paymentGatewayService.processPayment(postPaymentRequest));
  }

  @Test
  void cardExpired() {
    mockClock();

    postPaymentRequest.setExpiryMonth(3);
    postPaymentRequest.setExpiryYear(2025);

    assertThrows(InvalidInformationException.class, () -> paymentGatewayService.processPayment(postPaymentRequest));

    clockMock.close();
  }

  @Test
  void successfullyGetPayment() {
    UUID uuid = UUID.randomUUID();

    when(mockPaymentsRepository.get(any())).thenReturn(Optional.of(new PaymentDetail(
        uuid,
        PaymentStatus.AUTHORIZED,
        CARD_NUMBER.substring(CARD_NUMBER.length() - NUMBER_OF_DIGIT_TO_SHOW),
        EXPIRE_MONTH,
        EXPIRE_YEAR,
        CURRENCY,
        AMOUNT
    )));

    GetPaymentResponse dataFromStore = paymentGatewayService.getPaymentById(UUID.randomUUID());

    assertEquals(uuid, dataFromStore.getId());
    assertEquals(PaymentStatus.AUTHORIZED, dataFromStore.getStatus());
    assertEquals(CARD_NUMBER.substring(CARD_NUMBER.length() - NUMBER_OF_DIGIT_TO_SHOW), dataFromStore.getCardNumberLastFour());
    assertEquals(EXPIRE_MONTH, dataFromStore.getExpiryMonth());
    assertEquals(EXPIRE_YEAR, dataFromStore.getExpiryYear());
    assertEquals(CURRENCY, dataFromStore.getCurrency());
    assertEquals(AMOUNT, dataFromStore.getAmount());
  }

  @Test
  void throwExceptionWhenPaymentNotExist(){
    when(mockPaymentsRepository.get(any())).thenReturn(Optional.empty());

    assertThrows(PaymentNotFoundException.class, () -> paymentGatewayService.getPaymentById(UUID.randomUUID()));
  }

  private void verifyPostPaymentResponseWhenAuthorised(PostPaymentResponse postPaymentResponse){
    verifyPostPaymentResponseWhen(PaymentStatus.AUTHORIZED, postPaymentResponse);
  }

  private void verifyPostPaymentResponseWhenDeclined(PostPaymentResponse postPaymentResponse){
    verifyPostPaymentResponseWhen(PaymentStatus.DECLINED, postPaymentResponse);
  }

  private void verifyPostPaymentResponseWhen(PaymentStatus expectedPaymentStatus, PostPaymentResponse postPaymentResponse){
    verify(mockPaymentHandlingService, times(1)).authorisePayment(any());
    verify(mockPaymentsRepository, times(1)).add(any());

    assertNotNull(postPaymentResponse.getId());
    assertEquals(expectedPaymentStatus, postPaymentResponse.getStatus());
    assertEquals(CARD_NUMBER.substring(CARD_NUMBER.length() - NUMBER_OF_DIGIT_TO_SHOW), postPaymentResponse.getCardNumberLastFour());
    assertEquals(EXPIRE_MONTH, postPaymentResponse.getExpiryMonth());
    assertEquals(EXPIRE_YEAR, postPaymentResponse.getExpiryYear());
    assertEquals(CURRENCY, postPaymentResponse.getCurrency());
    assertEquals(AMOUNT, postPaymentResponse.getAmount());
  }

  private void mockClock() {
    Clock spyClock = spy(Clock.systemDefaultZone());
    when(spyClock.instant()).thenReturn(Instant.ofEpochSecond(MAR_1_2025_UNIX_TIMESTAMP));

    clockMock = mockStatic(Clock.class);
    clockMock.when(Clock::systemDefaultZone).thenReturn(spyClock);
    clockMock.when(Clock::systemUTC).thenReturn(spyClock);
  }

}

package com.checkout.payment.gateway.controller;


import static com.checkout.payment.gateway.model.PostPaymentRequest.AMOUNT_CANNOT_BE_NULL;
import static com.checkout.payment.gateway.model.PostPaymentRequest.AMOUNT_MUST_BE_POSITIVE_MESSAGE;
import static com.checkout.payment.gateway.model.PostPaymentRequest.CARD_NUMBER_CANNOT_BE_NULL;
import static com.checkout.payment.gateway.model.PostPaymentRequest.CURRENCY_CANNOT_BE_NULL;
import static com.checkout.payment.gateway.model.PostPaymentRequest.CURRENCY_OUT_OF_RANGE_MESSAGE;
import static com.checkout.payment.gateway.model.PostPaymentRequest.CVV_CANNOT_BE_NULL;
import static com.checkout.payment.gateway.model.PostPaymentRequest.INVALID_CVV_MESSAGE;
import static com.checkout.payment.gateway.model.PostPaymentRequest.EXPIRY_MONTH_CANNOT_BE_NULL;
import static com.checkout.payment.gateway.model.PostPaymentRequest.EXPIRY_MONTH_OUT_OF_RANGE_MESSAGE;
import static com.checkout.payment.gateway.model.PostPaymentRequest.EXPIRY_YEAR_CANNOT_BE_NULL;
import static com.checkout.payment.gateway.model.PostPaymentRequest.INVALID_CARD_NUMBER_MESSAGE;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PaymentDetail;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentGatewayControllerTest {
  private WireMockServer wireMockServer;

  public static final String PAYMENT_END_POINT = "/payment";
  @Autowired
  private MockMvc mvc;
  @Autowired
  PaymentsRepository paymentsRepository;

  @Value("${wiremock.port}")
  int wireMockPort;

  private static final String CARD_NUMBER = "4242424242424242";
  private static final int EXPIRE_MONTH = 10;
  private static final int EXPIRE_YEAR = 2025;
  private static final String CURRENCY = "GBP";
  private static final int AMOUNT = 100;
  private static final String CVV = "012";
  private PostPaymentRequest postPaymentRequest = new PostPaymentRequest(
      CARD_NUMBER,
      EXPIRE_MONTH,
      EXPIRE_YEAR,
      CURRENCY,
      AMOUNT,
      CVV
  );

  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void beforeEach() {
    wireMockServer = new WireMockServer(wireMockPort);

    if(!wireMockServer.isRunning()){
      wireMockServer.start();
    }

    WireMock.configureFor("localhost", wireMockPort);
  }

  @AfterEach
  void afterEach() throws InterruptedException {
    if(wireMockServer.isRunning()) {
      WireMock.reset();
      wireMockServer.stop();
    }

    postPaymentRequest = new PostPaymentRequest(
        CARD_NUMBER,
        EXPIRE_MONTH,
        EXPIRE_YEAR,
        CURRENCY,
        AMOUNT,
        CVV
    );

  }

  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {
    PaymentDetail paymentDetail = new PaymentDetail(
        UUID.randomUUID(),
        PaymentStatus.AUTHORIZED,
        CARD_NUMBER,
        EXPIRE_MONTH,
        EXPIRE_YEAR,
        CURRENCY,
        AMOUNT
    );

    paymentsRepository.add(paymentDetail);

    mvc.perform(MockMvcRequestBuilders.get(PAYMENT_END_POINT+ "/" + paymentDetail.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(paymentDetail.getStatus().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(paymentDetail.getCardNumber().substring(paymentDetail.getCardNumber().length() - 4)))
        .andExpect(jsonPath("$.expiryMonth").value(paymentDetail.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(paymentDetail.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(paymentDetail.getCurrency()))
        .andExpect(jsonPath("$.amount").value(paymentDetail.getAmount()));
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {
    UUID uuid = UUID.randomUUID();
    mvc.perform(MockMvcRequestBuilders.get(PAYMENT_END_POINT+ "/" + uuid))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(String.format("Payment not found, id = %s",uuid)));
  }

  @ParameterizedTest
  @CsvSource({"1", "abcefg", "a", "Hello!World!!~`+"})
  void whenPaymentIdIsNotUUID(String invalidId) throws Exception {
    mvc.perform(MockMvcRequestBuilders.get(PAYMENT_END_POINT + "/" + invalidId))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(
            String.format("Value [%s] is not accepted, please check your input", invalidId)));
  }

  @Test
  void whenPaymentAuthorised() throws Exception{
    mockBankAuthorised();

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.status").value(PaymentStatus.AUTHORIZED.getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(CARD_NUMBER.substring(CARD_NUMBER.length() - 4)))
        .andExpect(jsonPath("$.expiryMonth").value(EXPIRE_MONTH))
        .andExpect(jsonPath("$.expiryYear").value(EXPIRE_YEAR))
        .andExpect(jsonPath("$.currency").value(CURRENCY))
        .andExpect(jsonPath("$.amount").value(AMOUNT));

    Thread.sleep(2000);
  }

  @Test
  void whenPaymentAuthorisedAndQueryWithId() throws Exception {
    mockBankAuthorised();

    String postPaymentResponseString = mvc.perform(
        MockMvcRequestBuilders.post(PAYMENT_END_POINT)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(postPaymentRequest)
            )).andReturn().getResponse().getContentAsString();

    PostPaymentResponse postPaymentResponse = objectMapper.readValue(postPaymentResponseString,
        PostPaymentResponse.class);

    mvc.perform(MockMvcRequestBuilders.get(PAYMENT_END_POINT + "/" + postPaymentResponse.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(postPaymentResponse.getStatus().getName()))
        .andExpect(
            jsonPath("$.cardNumberLastFour").value(postPaymentResponse.getCardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(postPaymentResponse.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(postPaymentResponse.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(postPaymentResponse.getCurrency()))
        .andExpect(jsonPath("$.amount").value(postPaymentResponse.getAmount()));

    Thread.sleep(2000);
  }

  @Test
  void whenPaymentContainsInvalidAmount() throws Exception{

    String requsetString = """
        {
          "currency": "GBP",
          "amount": 12.3,
          "cvv": "306",
          "card_number": "9123912391239123",
          "expiry_month": 1,
          "expiry_year": 2034
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            requsetString)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Payload malformat found, please check documentation and change payload"));

    Thread.sleep(2000);
  }

  @Test
  void whenPaymentDeclined() throws Exception{
    mockBankUnauthorised();

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.status").value(PaymentStatus.DECLINED.getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(CARD_NUMBER.substring(CARD_NUMBER.length() - 4)))
        .andExpect(jsonPath("$.expiryMonth").value(EXPIRE_MONTH))
        .andExpect(jsonPath("$.expiryYear").value(EXPIRE_YEAR))
        .andExpect(jsonPath("$.currency").value(CURRENCY))
        .andExpect(jsonPath("$.amount").value(AMOUNT));

    Thread.sleep(2000);
  }

  @Test
  void cardNumberIsNull() throws Exception {
    postPaymentRequest.setCardNumber(null);

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(CARD_NUMBER_CANNOT_BE_NULL));
  }

  @ParameterizedTest
  @CsvSource({"1", "12345", "4242424242424", "42424242424242427890"})
  void invalidCardNumber(String cardNumber) throws Exception{
    postPaymentRequest.setCardNumber(cardNumber);

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(INVALID_CARD_NUMBER_MESSAGE));
  }

  @Test
  void expiryMonthIsNull() throws Exception {
    postPaymentRequest.setExpiryMonth(null);

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(EXPIRY_MONTH_CANNOT_BE_NULL));
  }

  @ParameterizedTest
  @CsvSource({"0", "13", "99", "1000"})
  void invalidExpiryMonth(int expiryMonth) throws Exception{
    postPaymentRequest.setExpiryMonth(expiryMonth);

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(EXPIRY_MONTH_OUT_OF_RANGE_MESSAGE));
  }

  @Test
  void expiryYearIsNull() throws Exception {
    postPaymentRequest.setExpiryYear(null);

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(EXPIRY_YEAR_CANNOT_BE_NULL));
  }

  @ParameterizedTest
  @CsvSource(value = {"12/1996", "3/2025", "01/2023"}, delimiterString = "/")
  void cardExpired(int expiryMonth, int expiryYear) throws Exception{
    postPaymentRequest.setExpiryMonth(expiryMonth);
    postPaymentRequest.setExpiryYear(expiryYear);

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Card expired"));
  }

  @Test
  void currencyIsNull() throws Exception{
    postPaymentRequest.setCurrency(null);

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(CURRENCY_CANNOT_BE_NULL));
  }

  @ParameterizedTest
  @CsvSource({"A", "BB", "CCCC"})
  void invalidCurrencyLength(String currency) throws Exception{
    postPaymentRequest.setCurrency(currency);

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(CURRENCY_OUT_OF_RANGE_MESSAGE));
  }

  @ParameterizedTest
  @CsvSource({"AAA", "BBB", "CCC"})
  void invalidCurrency(String currency) throws Exception{
    postPaymentRequest.setCurrency(currency);

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid currency"));
  }

  @Test
  void amountIsNull() throws Exception{
    postPaymentRequest.setAmount(null);

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(AMOUNT_CANNOT_BE_NULL));
  }

  @Test
  void amountIsNegative() throws Exception{
    postPaymentRequest.setAmount(AMOUNT * -1);

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(AMOUNT_MUST_BE_POSITIVE_MESSAGE));
  }

  @Test
  void cvvIsNull() throws Exception{
    postPaymentRequest.setCvv(null);

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(CVV_CANNOT_BE_NULL));
  }

  @ParameterizedTest
  @CsvSource({"123", "1111"})
  void validCvvLength(String cvv) throws Exception{
    mockBankAuthorised();

    postPaymentRequest.setCvv(cvv);

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isOk());

    Thread.sleep(2000);
  }

  @ParameterizedTest
  @CsvSource({"1", "22", "55555", "EEEEE", "CCC", "DDDD", "2BB", "3CCC", "1B3D"})
  void invalidCvv(String cvv) throws Exception{
    postPaymentRequest.setCvv(cvv);

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(INVALID_CVV_MESSAGE));
  }

  @Test
  void noPaymentProcessedWhenBankUnavailable() throws Exception{
    mockBankServiceUnavailable();

    mvc.perform(MockMvcRequestBuilders.post(PAYMENT_END_POINT).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
            postPaymentRequest)))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.message").value("Bank unavailable, no payment made"));
  }



  private void mockBankAuthorised(){
    willBankAuthorisePayment(true);
  }

  private void mockBankUnauthorised(){
    willBankAuthorisePayment(false);
  }

  private void willBankAuthorisePayment(boolean authorise){
    stubFor(post(urlEqualTo("/payments"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody("{\"authorized\":"+ authorise +",\"authorization_code\":\"0bb07405-6d44-4b50-a14f-7ae0beff13ad\"}")));
  }

  private void mockBankServiceUnavailable(){
    stubFor(post(urlEqualTo("/payments"))
        .willReturn(aResponse()
            .withStatus(503)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody("{}")));
  }


}

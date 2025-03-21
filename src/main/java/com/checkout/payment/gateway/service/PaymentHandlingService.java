package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.exception.BankServerUnavailableException;
import com.checkout.payment.gateway.exception.MissingPaymentInformationException;
import com.checkout.payment.gateway.exception.PaymentUnsuccessfulException;
import com.checkout.payment.gateway.model.AuthoriseRequest;
import com.checkout.payment.gateway.model.AuthoriseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PaymentHandlingService {

  @Autowired
  RestTemplate restTemplate;

  @Value("${bank.base-url}")
  String baseUrl;

  @Value("${bank.payment-endpoint}")
  String paymentEndpoint;

  public AuthoriseResponse authorisePayment(AuthoriseRequest authoriseRequest) {
    log.info("Incoming request = {}", authoriseRequest);

    try {
      return restTemplate.postForEntity(baseUrl + paymentEndpoint, authoriseRequest,
          AuthoriseResponse.class).getBody();
    } catch (HttpClientErrorException httpClientErrorException) {
      HttpStatusCode httpStatusCode = httpClientErrorException.getStatusCode();
      String errorResponse = httpClientErrorException.getResponseBodyAsString();

      if (httpStatusCode.value() == HttpStatus.BAD_REQUEST.value()) {
        throw new MissingPaymentInformationException(errorResponse);
      }

      throw new PaymentUnsuccessfulException(errorResponse);
    } catch (HttpServerErrorException httpServerErrorException) {
      log.error("Exception on http service: {}", httpServerErrorException);

      throw new BankServerUnavailableException("Bank unavailable, no payment made");
    } catch (Exception exception) {
      log.error("Exception when calling bank api: {}", exception);
      throw new PaymentUnsuccessfulException(exception.getMessage());
    }
  }

}

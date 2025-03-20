package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.exception.BankServerUnavailableException;
import com.checkout.payment.gateway.exception.MissingPaymentInformationException;
import com.checkout.payment.gateway.exception.PaymentUnsuccessfulException;
import com.checkout.payment.gateway.model.AuthoriseRequest;
import com.checkout.payment.gateway.model.AuthoriseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PaymentHandlingService {

  @Autowired
  RestTemplate restTemplate;

  public AuthoriseResponse authorisePayment(AuthoriseRequest authoriseRequest){
    log.info("Incoming request = {}", authoriseRequest);

    try {
      return restTemplate.postForEntity("localhost:8080/payments", authoriseRequest,
          AuthoriseResponse.class).getBody();
    }catch(HttpClientErrorException httpClientErrorException){
      HttpStatusCode httpStatusCode = httpClientErrorException.getStatusCode();
      String errorResponse = httpClientErrorException.getResponseBodyAsString();

      if (httpStatusCode.value() == HttpStatus.BAD_REQUEST.value()) {
        throw new MissingPaymentInformationException(errorResponse);
      } else if (httpStatusCode.value() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
        throw new BankServerUnavailableException(errorResponse);
      }

      throw new PaymentUnsuccessfulException(errorResponse);
    }
  }

}

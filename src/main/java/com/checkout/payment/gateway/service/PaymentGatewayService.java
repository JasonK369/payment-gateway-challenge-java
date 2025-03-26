package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.InvalidInformationException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.AuthoriseRequest;
import com.checkout.payment.gateway.model.AuthoriseResponse;
import com.checkout.payment.gateway.model.GetPaymentResponse;
import com.checkout.payment.gateway.model.PaymentDetail;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.Currency;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static utils.CreditCardInformationValidator.isCardExpired;

@Service
@Slf4j
public class PaymentGatewayService {

  @Autowired
  private PaymentHandlingService paymentHandlingService;

  @Autowired
  private PaymentsRepository paymentsRepository;

  public final int CREDIT_CARD_MASKING_LENGTH = 4;


  public GetPaymentResponse getPaymentById(UUID id) {
    log.info("Requesting access to to payment with ID {}", id);
    PaymentDetail paymentDetail = paymentsRepository.get(id).orElseThrow(() -> new PaymentNotFoundException(id));


    return mapGetPaymentResponseFrom(paymentDetail);
  }

  public PostPaymentResponse processPayment(PostPaymentRequest postPaymentRequest) {
    validationPaymentInformation(postPaymentRequest);

    AuthoriseResponse authoriseResponse = paymentHandlingService.authorisePayment(
        mapAuthoriseRequest(postPaymentRequest));

    PaymentDetail paymentDetail = mapPaymentDetail(postPaymentRequest, authoriseResponse);
    paymentsRepository.add(paymentDetail);

    return mapPostPaymentResponse(paymentDetail);
  }

  private PaymentDetail mapPaymentDetail(PostPaymentRequest postPaymentRequest,
      AuthoriseResponse authoriseResponse) {
    return new PaymentDetail(
        UUID.randomUUID(),
        authoriseResponse.getAuthorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED,
        postPaymentRequest.getCardNumber(),
        postPaymentRequest.getExpiryMonth(),
        postPaymentRequest.getExpiryYear(),
        postPaymentRequest.getCurrency(),
        postPaymentRequest.getAmount()
    );
  }

  private AuthoriseRequest mapAuthoriseRequest(PostPaymentRequest postPaymentRequest) {

    return new AuthoriseRequest(
        postPaymentRequest.getCardNumber(),
        postPaymentRequest.getExpiryDate(),
        postPaymentRequest.getCurrency(),
        postPaymentRequest.getAmount(),
        postPaymentRequest.getCvv()
    );
  }

  private PostPaymentResponse mapPostPaymentResponse(PaymentDetail paymentDetail) {
    return new PostPaymentResponse(
        UUID.randomUUID(),
        paymentDetail.getStatus(),
        trimCreditCardNumber(paymentDetail.getCardNumber(), CREDIT_CARD_MASKING_LENGTH),
        paymentDetail.getExpiryMonth(),
        paymentDetail.getExpiryYear(),
        paymentDetail.getCurrency(),
        paymentDetail.getAmount()
    );
  }

  private GetPaymentResponse mapGetPaymentResponseFrom(PaymentDetail paymentDetail){
    return new GetPaymentResponse(
        paymentDetail.getId(),
        paymentDetail.getStatus(),
        trimCreditCardNumber(paymentDetail.getCardNumber(), CREDIT_CARD_MASKING_LENGTH),
        paymentDetail.getExpiryMonth(),
        paymentDetail.getExpiryYear(),
        paymentDetail.getCurrency(),
        paymentDetail.getAmount()
    );
  }

  private void validationPaymentInformation(PostPaymentRequest postPaymentRequest) {
    checkCardExpired(postPaymentRequest.getExpiryMonth(), postPaymentRequest.getExpiryYear());

    checkCurrency(postPaymentRequest.getCurrency());
  }

  private void checkCardExpired(int expiryMonth, int expiryYear) {
    if (isCardExpired(expiryMonth, expiryYear)) {
      log.error("Card expired, expiry month = {}, expiry year = {}", expiryMonth, expiryYear);
      throw new InvalidInformationException("Card expired");
    }
  }

  private void checkCurrency(String currency) {
    try {
      Currency.getInstance(currency);
    } catch (IllegalArgumentException illegalArgumentException) {
      log.error("currency [{}] not exist", currency);
      throw new InvalidInformationException("Invalid currency");
    }
  }

  private String trimCreditCardNumber(String creditCardNumber, int digitsToDisplay) {
    return creditCardNumber.substring(creditCardNumber.length() - digitsToDisplay);
  }
}

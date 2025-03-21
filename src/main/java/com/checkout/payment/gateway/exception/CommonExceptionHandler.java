package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler(BankServerUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleBankServerUnavailable(
      BankServerUnavailableException bankServerUnavailableException) {
    return new ResponseEntity<>(
        new ErrorResponse(bankServerUnavailableException.getMessage(), PaymentStatus.REJECTED),
        HttpStatus.BAD_GATEWAY);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlePaymentNotFoundException(PaymentNotFoundException paymentNotFoundException){
    return new ResponseEntity<>(new ErrorResponse(paymentNotFoundException.getMessage(), null),
        HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(PaymentUnsuccessfulException.class)
  public ResponseEntity<ErrorResponse> handlePaymentUnsuccessfulException(PaymentUnsuccessfulException paymentUnsuccessfulException){
    return new ResponseEntity<>(new ErrorResponse(paymentUnsuccessfulException.getMessage(), PaymentStatus.REJECTED),
        HttpStatus.BAD_REQUEST);
  }
}

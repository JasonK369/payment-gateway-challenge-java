package com.checkout.payment.gateway.repository;

import com.checkout.payment.gateway.model.PaymentDetail;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentsRepository {

  private final HashMap<UUID, PaymentDetail> payments = new HashMap<>();

  public void add(PaymentDetail paymentDetail) {
    payments.put(paymentDetail.getId(), paymentDetail);
  }

  public Optional<PaymentDetail> get(UUID id) {
    return Optional.ofNullable(payments.get(id));
  }

}

package com.hiberus.candidateapi.application.port.out;

import com.hiberus.candidateapi.domain.model.PaymentOrder;
import java.util.Optional;

public interface PaymentOrderRepository {

	Optional<PaymentOrder> findById(String paymentOrderId);

	PaymentOrder save(PaymentOrder paymentOrder);
}

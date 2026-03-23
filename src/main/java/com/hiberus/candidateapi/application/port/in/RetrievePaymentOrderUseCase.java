package com.hiberus.candidateapi.application.port.in;

import com.hiberus.candidateapi.domain.model.PaymentOrder;
import java.util.Optional;

public interface RetrievePaymentOrderUseCase {

	Optional<PaymentOrder> retrieve(String paymentOrderId);
}

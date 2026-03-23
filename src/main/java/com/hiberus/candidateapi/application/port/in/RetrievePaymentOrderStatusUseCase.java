package com.hiberus.candidateapi.application.port.in;

import com.hiberus.candidateapi.application.port.in.result.PaymentOrderStatusResult;
import java.util.Optional;

public interface RetrievePaymentOrderStatusUseCase {

	Optional<PaymentOrderStatusResult> retrieveStatus(String paymentOrderId);
}

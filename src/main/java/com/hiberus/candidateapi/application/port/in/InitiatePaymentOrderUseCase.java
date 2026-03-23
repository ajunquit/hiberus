package com.hiberus.candidateapi.application.port.in;

import com.hiberus.candidateapi.application.port.in.command.InitiatePaymentOrderCommand;
import com.hiberus.candidateapi.domain.model.PaymentOrder;

public interface InitiatePaymentOrderUseCase {

	PaymentOrder initiate(InitiatePaymentOrderCommand command);
}

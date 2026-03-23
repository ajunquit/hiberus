package com.hiberus.candidateapi.application.service;

import com.hiberus.candidateapi.application.port.in.InitiatePaymentOrderUseCase;
import com.hiberus.candidateapi.application.port.in.RetrievePaymentOrderStatusUseCase;
import com.hiberus.candidateapi.application.port.in.RetrievePaymentOrderUseCase;
import com.hiberus.candidateapi.application.port.in.command.InitiatePaymentOrderCommand;
import com.hiberus.candidateapi.application.port.in.result.PaymentOrderStatusResult;
import com.hiberus.candidateapi.application.port.out.PaymentOrderIdGenerator;
import com.hiberus.candidateapi.application.port.out.PaymentOrderRepository;
import com.hiberus.candidateapi.domain.model.AccountNumber;
import com.hiberus.candidateapi.domain.model.MonetaryAmount;
import com.hiberus.candidateapi.domain.model.PaymentOrder;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PaymentOrderService
	implements
		InitiatePaymentOrderUseCase,
		RetrievePaymentOrderUseCase,
		RetrievePaymentOrderStatusUseCase {

	private final PaymentOrderRepository paymentOrderRepository;
	private final PaymentOrderIdGenerator paymentOrderIdGenerator;
	private final Clock clock;

	public PaymentOrderService(
		PaymentOrderRepository paymentOrderRepository,
		PaymentOrderIdGenerator paymentOrderIdGenerator,
		Clock clock
	) {
		this.paymentOrderRepository = paymentOrderRepository;
		this.paymentOrderIdGenerator = paymentOrderIdGenerator;
		this.clock = clock;
	}

	@Override
	public PaymentOrder initiate(InitiatePaymentOrderCommand command) {
		OffsetDateTime now = OffsetDateTime.now(clock);
		PaymentOrder paymentOrder = PaymentOrder.initiate(
			paymentOrderIdGenerator.nextId(),
			command.externalReference(),
			new AccountNumber(command.debtorIban()),
			new AccountNumber(command.creditorIban()),
			new MonetaryAmount(command.amount(), command.currency()),
			command.remittanceInformation(),
			command.requestedExecutionDate(),
			now
		);
		return paymentOrderRepository.save(paymentOrder);
	}

	@Override
	public Optional<PaymentOrder> retrieve(String paymentOrderId) {
		return paymentOrderRepository.findById(paymentOrderId);
	}

	@Override
	public Optional<PaymentOrderStatusResult> retrieveStatus(String paymentOrderId) {
		return paymentOrderRepository
			.findById(paymentOrderId)
			.map(paymentOrder ->
				new PaymentOrderStatusResult(
					paymentOrder.getPaymentOrderId(),
					paymentOrder.getStatus(),
					paymentOrder.getLastUpdate()
				)
			);
	}
}

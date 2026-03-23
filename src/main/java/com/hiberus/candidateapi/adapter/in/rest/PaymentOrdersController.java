package com.hiberus.candidateapi.adapter.in.rest;

import com.hiberus.candidateapi.adapter.in.rest.mapper.PaymentOrderApiMapper;
import com.hiberus.candidateapi.application.port.in.InitiatePaymentOrderUseCase;
import com.hiberus.candidateapi.application.port.in.RetrievePaymentOrderStatusUseCase;
import com.hiberus.candidateapi.application.port.in.RetrievePaymentOrderUseCase;
import com.hiberus.candidateapi.generated.api.PaymentOrdersApi;
import com.hiberus.candidateapi.generated.model.PaymentOrder;
import com.hiberus.candidateapi.generated.model.PaymentOrderInitiationRequest;
import com.hiberus.candidateapi.generated.model.PaymentOrderStatusView;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentOrdersController implements PaymentOrdersApi {

	private final InitiatePaymentOrderUseCase initiatePaymentOrderUseCase;
	private final RetrievePaymentOrderUseCase retrievePaymentOrderUseCase;
	private final RetrievePaymentOrderStatusUseCase retrievePaymentOrderStatusUseCase;
	private final PaymentOrderApiMapper paymentOrderApiMapper;

	public PaymentOrdersController(
		InitiatePaymentOrderUseCase initiatePaymentOrderUseCase,
		RetrievePaymentOrderUseCase retrievePaymentOrderUseCase,
		RetrievePaymentOrderStatusUseCase retrievePaymentOrderStatusUseCase,
		PaymentOrderApiMapper paymentOrderApiMapper
	) {
		this.initiatePaymentOrderUseCase = initiatePaymentOrderUseCase;
		this.retrievePaymentOrderUseCase = retrievePaymentOrderUseCase;
		this.retrievePaymentOrderStatusUseCase = retrievePaymentOrderStatusUseCase;
		this.paymentOrderApiMapper = paymentOrderApiMapper;
	}

	@Override
	public ResponseEntity<PaymentOrder> initiatePaymentOrder(
		PaymentOrderInitiationRequest paymentOrderInitiationRequest
	) {
		com.hiberus.candidateapi.domain.model.PaymentOrder createdPaymentOrder = initiatePaymentOrderUseCase.initiate(
			paymentOrderApiMapper.toCommand(paymentOrderInitiationRequest)
		);
		URI location = URI.create(
			"/payment-initiation/payment-orders/" + createdPaymentOrder.getPaymentOrderId()
		);
		return ResponseEntity
			.created(location)
			.body(paymentOrderApiMapper.toApiModel(createdPaymentOrder));
	}

	@Override
	public ResponseEntity<PaymentOrder> retrievePaymentOrder(String paymentOrderId) {
		return retrievePaymentOrderUseCase
			.retrieve(paymentOrderId)
			.map(paymentOrderApiMapper::toApiModel)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<PaymentOrderStatusView> retrievePaymentOrderStatus(String paymentOrderId) {
		return retrievePaymentOrderStatusUseCase
			.retrieveStatus(paymentOrderId)
			.map(paymentOrderApiMapper::toStatusView)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}
}

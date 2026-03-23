package com.hiberus.candidateapi.adapter.out.persistence;

import com.hiberus.candidateapi.application.port.out.PaymentOrderRepository;
import com.hiberus.candidateapi.domain.model.PaymentOrder;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPaymentOrderRepository implements PaymentOrderRepository {

	private final Map<String, PaymentOrder> storage = new ConcurrentHashMap<>();

	@Override
	public Optional<PaymentOrder> findById(String paymentOrderId) {
		return Optional.ofNullable(storage.get(paymentOrderId));
	}

	@Override
	public PaymentOrder save(PaymentOrder paymentOrder) {
		storage.put(paymentOrder.getPaymentOrderId(), paymentOrder);
		return paymentOrder;
	}
}

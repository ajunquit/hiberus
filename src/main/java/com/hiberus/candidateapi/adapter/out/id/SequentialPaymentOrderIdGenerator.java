package com.hiberus.candidateapi.adapter.out.id;

import com.hiberus.candidateapi.application.port.out.PaymentOrderIdGenerator;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class SequentialPaymentOrderIdGenerator implements PaymentOrderIdGenerator {

	private final AtomicLong sequence = new AtomicLong(1);

	@Override
	public String nextId() {
		return "PO-%04d".formatted(sequence.getAndIncrement());
	}
}

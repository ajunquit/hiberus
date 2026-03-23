package com.hiberus.candidateapi.application.port.in.result;

import com.hiberus.candidateapi.domain.model.PaymentOrderStatus;
import java.time.OffsetDateTime;
import java.util.Objects;

public record PaymentOrderStatusResult(
	String paymentOrderId,
	PaymentOrderStatus status,
	OffsetDateTime lastUpdate
) {

	public PaymentOrderStatusResult {
		Objects.requireNonNull(paymentOrderId, "paymentOrderId is required");
		Objects.requireNonNull(status, "status is required");
		Objects.requireNonNull(lastUpdate, "lastUpdate is required");
	}
}

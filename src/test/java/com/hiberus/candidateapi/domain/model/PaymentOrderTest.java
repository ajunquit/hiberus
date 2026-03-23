package com.hiberus.candidateapi.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hiberus.candidateapi.domain.exception.DomainValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class PaymentOrderTest {

	@Test
	void shouldInitiatePaymentOrderWithAcceptedStatusAndMatchingTimestamps() {
		OffsetDateTime now = OffsetDateTime.parse("2025-10-30T16:20:00Z");

		PaymentOrder paymentOrder = PaymentOrder.initiate(
			"PO-0001",
			"EXT-1",
			new AccountNumber("EC12DEBTOR"),
			new AccountNumber("EC98CREDITOR"),
			new MonetaryAmount(new BigDecimal("150.75"), "USD"),
			"Factura 001-123",
			LocalDate.parse("2025-10-31"),
			now
		);

		assertThat(paymentOrder.getPaymentOrderId()).isEqualTo("PO-0001");
		assertThat(paymentOrder.getStatus()).isEqualTo(PaymentOrderStatus.ACCEPTED);
		assertThat(paymentOrder.getCreatedAt()).isEqualTo(now);
		assertThat(paymentOrder.getLastUpdate()).isEqualTo(now);
	}

	@Test
	void shouldUpdateStatusWhenTransitionUsesNonDecreasingTimestamp() {
		PaymentOrder paymentOrder = PaymentOrder.initiate(
			"PO-0001",
			"EXT-1",
			new AccountNumber("EC12DEBTOR"),
			new AccountNumber("EC98CREDITOR"),
			new MonetaryAmount(new BigDecimal("150.75"), "USD"),
			"Factura 001-123",
			LocalDate.parse("2025-10-31"),
			OffsetDateTime.parse("2025-10-30T16:20:00Z")
		);

		PaymentOrder settledPaymentOrder = paymentOrder.withStatus(
			PaymentOrderStatus.SETTLED,
			OffsetDateTime.parse("2025-10-30T16:25:30Z")
		);

		assertThat(settledPaymentOrder.getStatus()).isEqualTo(PaymentOrderStatus.SETTLED);
		assertThat(settledPaymentOrder.getCreatedAt()).isEqualTo(paymentOrder.getCreatedAt());
		assertThat(settledPaymentOrder.getLastUpdate()).isEqualTo(OffsetDateTime.parse("2025-10-30T16:25:30Z"));
	}

	@Test
	void shouldRejectStatusUpdateWhenTimestampGoesBackwards() {
		PaymentOrder paymentOrder = PaymentOrder.initiate(
			"PO-0001",
			"EXT-1",
			new AccountNumber("EC12DEBTOR"),
			new AccountNumber("EC98CREDITOR"),
			new MonetaryAmount(new BigDecimal("150.75"), "USD"),
			"Factura 001-123",
			LocalDate.parse("2025-10-31"),
			OffsetDateTime.parse("2025-10-30T16:20:00Z")
		);

		assertThatThrownBy(() ->
			paymentOrder.withStatus(
				PaymentOrderStatus.SETTLED,
				OffsetDateTime.parse("2025-10-30T16:19:59Z")
			)
		)
			.isInstanceOf(DomainValidationException.class)
			.hasMessage("updatedAt must be greater than or equal to lastUpdate");
	}

	@Test
	void shouldRejectPaymentOrderWhenExternalReferenceIsBlank() {
		assertThatThrownBy(() ->
			PaymentOrder.initiate(
				"PO-0001",
				" ",
				new AccountNumber("EC12DEBTOR"),
				new AccountNumber("EC98CREDITOR"),
				new MonetaryAmount(new BigDecimal("150.75"), "USD"),
				"Factura 001-123",
				LocalDate.parse("2025-10-31"),
				OffsetDateTime.parse("2025-10-30T16:20:00Z")
			)
		)
			.isInstanceOf(DomainValidationException.class)
			.hasMessage("externalReference must contain between 1 and 64 characters");
	}
}

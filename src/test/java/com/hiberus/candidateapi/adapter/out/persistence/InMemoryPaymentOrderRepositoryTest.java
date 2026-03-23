package com.hiberus.candidateapi.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.hiberus.candidateapi.domain.model.AccountNumber;
import com.hiberus.candidateapi.domain.model.MonetaryAmount;
import com.hiberus.candidateapi.domain.model.PaymentOrder;
import com.hiberus.candidateapi.domain.model.PaymentOrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class InMemoryPaymentOrderRepositoryTest {

	@Test
	void shouldStoreAndRetrievePaymentOrderById() {
		InMemoryPaymentOrderRepository repository = new InMemoryPaymentOrderRepository();
		PaymentOrder paymentOrder = PaymentOrder.rehydrate(
			"PO-0001",
			"EXT-1",
			new AccountNumber("EC12DEBTOR"),
			new AccountNumber("EC98CREDITOR"),
			new MonetaryAmount(new BigDecimal("150.75"), "USD"),
			"Factura 001-123",
			LocalDate.parse("2025-10-31"),
			PaymentOrderStatus.ACCEPTED,
			OffsetDateTime.parse("2025-10-30T16:20:00Z"),
			OffsetDateTime.parse("2025-10-30T16:20:00Z")
		);

		repository.save(paymentOrder);

		assertThat(repository.findById("PO-0001")).contains(paymentOrder);
		assertThat(repository.findById("PO-9999")).isEmpty();
	}
}

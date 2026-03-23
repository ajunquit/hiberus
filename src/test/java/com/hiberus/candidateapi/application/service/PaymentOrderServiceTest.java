package com.hiberus.candidateapi.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hiberus.candidateapi.application.port.in.command.InitiatePaymentOrderCommand;
import com.hiberus.candidateapi.application.port.in.result.PaymentOrderStatusResult;
import com.hiberus.candidateapi.application.port.out.PaymentOrderIdGenerator;
import com.hiberus.candidateapi.application.port.out.PaymentOrderRepository;
import com.hiberus.candidateapi.domain.model.AccountNumber;
import com.hiberus.candidateapi.domain.model.MonetaryAmount;
import com.hiberus.candidateapi.domain.model.PaymentOrder;
import com.hiberus.candidateapi.domain.model.PaymentOrderStatus;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentOrderServiceTest {

	@Mock
	private PaymentOrderRepository paymentOrderRepository;

	@Mock
	private PaymentOrderIdGenerator paymentOrderIdGenerator;

	@Captor
	private ArgumentCaptor<PaymentOrder> paymentOrderCaptor;

	@Test
	void shouldInitiateAndPersistAcceptedPaymentOrder() {
		Clock fixedClock = Clock.fixed(Instant.parse("2025-10-30T16:20:00Z"), ZoneOffset.UTC);
		PaymentOrderService service = new PaymentOrderService(
			paymentOrderRepository,
			paymentOrderIdGenerator,
			fixedClock
		);
		InitiatePaymentOrderCommand command = new InitiatePaymentOrderCommand(
			"EXT-1",
			"EC12DEBTOR",
			"EC98CREDITOR",
			new BigDecimal("150.75"),
			"USD",
			"Factura 001-123",
			LocalDate.parse("2025-10-31")
		);
		when(paymentOrderIdGenerator.nextId()).thenReturn("PO-0001");
		when(paymentOrderRepository.save(any(PaymentOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

		PaymentOrder paymentOrder = service.initiate(command);

		verify(paymentOrderRepository).save(paymentOrderCaptor.capture());
		assertThat(paymentOrder.getPaymentOrderId()).isEqualTo("PO-0001");
		assertThat(paymentOrder.getStatus()).isEqualTo(PaymentOrderStatus.ACCEPTED);
		assertThat(paymentOrder.getCreatedAt()).isEqualTo(OffsetDateTime.parse("2025-10-30T16:20:00Z"));
		assertThat(paymentOrderCaptor.getValue().getDebtorAccount().iban()).isEqualTo("EC12DEBTOR");
	}

	@Test
	void shouldRetrieveCurrentStatusWhenPaymentOrderExists() {
		PaymentOrderService service = new PaymentOrderService(
			paymentOrderRepository,
			paymentOrderIdGenerator,
			Clock.systemUTC()
		);
		PaymentOrder paymentOrder = PaymentOrder.rehydrate(
			"PO-0001",
			"EXT-1",
			new AccountNumber("EC12DEBTOR"),
			new AccountNumber("EC98CREDITOR"),
			new MonetaryAmount(new BigDecimal("150.75"), "USD"),
			"Factura 001-123",
			LocalDate.parse("2025-10-31"),
			PaymentOrderStatus.SETTLED,
			OffsetDateTime.parse("2025-10-30T16:20:00Z"),
			OffsetDateTime.parse("2025-10-30T16:25:30Z")
		);
		when(paymentOrderRepository.findById("PO-0001")).thenReturn(Optional.of(paymentOrder));

		Optional<PaymentOrderStatusResult> result = service.retrieveStatus("PO-0001");

		assertThat(result).isPresent();
		assertThat(result.orElseThrow().status()).isEqualTo(PaymentOrderStatus.SETTLED);
		assertThat(result.orElseThrow().lastUpdate()).isEqualTo(OffsetDateTime.parse("2025-10-30T16:25:30Z"));
	}

	@Test
	void shouldReturnEmptyOptionalWhenPaymentOrderDoesNotExist() {
		PaymentOrderService service = new PaymentOrderService(
			paymentOrderRepository,
			paymentOrderIdGenerator,
			Clock.systemUTC()
		);
		when(paymentOrderRepository.findById("PO-9999")).thenReturn(Optional.empty());

		assertThat(service.retrieve("PO-9999")).isEmpty();
		assertThat(service.retrieveStatus("PO-9999")).isEmpty();
	}
}

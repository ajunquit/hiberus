package com.hiberus.candidateapi.adapter.in.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hiberus.candidateapi.application.port.in.result.PaymentOrderStatusResult;
import com.hiberus.candidateapi.domain.model.AccountNumber;
import com.hiberus.candidateapi.domain.model.MonetaryAmount;
import com.hiberus.candidateapi.domain.model.PaymentOrder;
import com.hiberus.candidateapi.domain.model.PaymentOrderStatus;
import com.hiberus.candidateapi.generated.model.AccountReference;
import com.hiberus.candidateapi.generated.model.InstructedAmount;
import com.hiberus.candidateapi.generated.model.PaymentOrderInitiationRequest;
import com.hiberus.candidateapi.generated.model.PaymentOrderStatusCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class PaymentOrderApiMapperTest {

	private final PaymentOrderApiMapper mapper = new PaymentOrderApiMapper();

	@Test
	void shouldMapApiRequestToCommand() {
		PaymentOrderInitiationRequest request = new PaymentOrderInitiationRequest()
			.externalReference("EXT-1")
			.debtorAccount(new AccountReference().iban("EC12DEBTOR"))
			.creditorAccount(new AccountReference().iban("EC98CREDITOR"))
			.instructedAmount(new InstructedAmount().amount(new BigDecimal("150.75")).currency("USD"))
			.remittanceInformation("Factura 001-123")
			.requestedExecutionDate(LocalDate.parse("2025-10-31"));

		var command = mapper.toCommand(request);

		assertThat(command.externalReference()).isEqualTo("EXT-1");
		assertThat(command.debtorIban()).isEqualTo("EC12DEBTOR");
		assertThat(command.creditorIban()).isEqualTo("EC98CREDITOR");
		assertThat(command.amount()).isEqualByComparingTo("150.75");
	}

	@Test
	void shouldMapDomainPaymentOrderToApiModel() {
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

		var apiModel = mapper.toApiModel(paymentOrder);

		assertThat(apiModel.getPaymentOrderId()).isEqualTo("PO-0001");
		assertThat(apiModel.getStatus()).isEqualTo(PaymentOrderStatusCode.SETTLED);
		assertThat(apiModel.getDebtorAccount().getIban()).isEqualTo("EC12DEBTOR");
		assertThat(apiModel.getInstructedAmount().getAmount()).isEqualByComparingTo("150.75");
	}

	@Test
	void shouldMapStatusResultToApiStatusView() {
		PaymentOrderStatusResult statusResult = new PaymentOrderStatusResult(
			"PO-0001",
			PaymentOrderStatus.ACCEPTED,
			OffsetDateTime.parse("2025-10-30T16:20:00Z")
		);

		var statusView = mapper.toStatusView(statusResult);

		assertThat(statusView.getPaymentOrderId()).isEqualTo("PO-0001");
		assertThat(statusView.getStatus()).isEqualTo(PaymentOrderStatusCode.ACCEPTED);
		assertThat(statusView.getLastUpdate()).isEqualTo(OffsetDateTime.parse("2025-10-30T16:20:00Z"));
	}
}

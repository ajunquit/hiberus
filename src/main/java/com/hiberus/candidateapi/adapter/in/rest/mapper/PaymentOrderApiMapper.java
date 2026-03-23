package com.hiberus.candidateapi.adapter.in.rest.mapper;

import com.hiberus.candidateapi.application.port.in.command.InitiatePaymentOrderCommand;
import com.hiberus.candidateapi.application.port.in.result.PaymentOrderStatusResult;
import com.hiberus.candidateapi.domain.model.PaymentOrderStatus;
import com.hiberus.candidateapi.generated.model.AccountReference;
import com.hiberus.candidateapi.generated.model.InstructedAmount;
import com.hiberus.candidateapi.generated.model.PaymentOrder;
import com.hiberus.candidateapi.generated.model.PaymentOrderInitiationRequest;
import com.hiberus.candidateapi.generated.model.PaymentOrderStatusCode;
import com.hiberus.candidateapi.generated.model.PaymentOrderStatusView;
import org.springframework.stereotype.Component;

@Component
public class PaymentOrderApiMapper {

	public InitiatePaymentOrderCommand toCommand(PaymentOrderInitiationRequest source) {
		return new InitiatePaymentOrderCommand(
			source.getExternalReference(),
			source.getDebtorAccount().getIban(),
			source.getCreditorAccount().getIban(),
			source.getInstructedAmount().getAmount(),
			source.getInstructedAmount().getCurrency(),
			source.getRemittanceInformation(),
			source.getRequestedExecutionDate()
		);
	}

	public PaymentOrder toApiModel(com.hiberus.candidateapi.domain.model.PaymentOrder source) {
		return new PaymentOrder()
			.paymentOrderId(source.getPaymentOrderId())
			.externalReference(source.getExternalReference())
			.debtorAccount(new AccountReference().iban(source.getDebtorAccount().iban()))
			.creditorAccount(new AccountReference().iban(source.getCreditorAccount().iban()))
			.instructedAmount(
				new InstructedAmount()
					.amount(source.getInstructedAmount().amount())
					.currency(source.getInstructedAmount().currency())
			)
			.remittanceInformation(source.getRemittanceInformation())
			.requestedExecutionDate(source.getRequestedExecutionDate())
			.status(toStatusCode(source.getStatus()))
			.createdAt(source.getCreatedAt())
			.lastUpdate(source.getLastUpdate());
	}

	public PaymentOrderStatusView toStatusView(PaymentOrderStatusResult source) {
		return new PaymentOrderStatusView()
			.paymentOrderId(source.paymentOrderId())
			.status(toStatusCode(source.status()))
			.lastUpdate(source.lastUpdate());
	}

	private PaymentOrderStatusCode toStatusCode(PaymentOrderStatus source) {
		return switch (source) {
			case ACCEPTED -> PaymentOrderStatusCode.ACCEPTED;
			case SETTLED -> PaymentOrderStatusCode.SETTLED;
			case REJECTED -> PaymentOrderStatusCode.REJECTED;
		};
	}
}

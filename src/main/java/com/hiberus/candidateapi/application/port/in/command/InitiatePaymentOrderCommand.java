package com.hiberus.candidateapi.application.port.in.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record InitiatePaymentOrderCommand(
	String externalReference,
	String debtorIban,
	String creditorIban,
	BigDecimal amount,
	String currency,
	String remittanceInformation,
	LocalDate requestedExecutionDate
) {

	public InitiatePaymentOrderCommand {
		Objects.requireNonNull(externalReference, "externalReference is required");
		Objects.requireNonNull(debtorIban, "debtorIban is required");
		Objects.requireNonNull(creditorIban, "creditorIban is required");
		Objects.requireNonNull(amount, "amount is required");
		Objects.requireNonNull(currency, "currency is required");
		Objects.requireNonNull(requestedExecutionDate, "requestedExecutionDate is required");
	}
}

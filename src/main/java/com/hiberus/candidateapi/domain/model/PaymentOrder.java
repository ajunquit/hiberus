package com.hiberus.candidateapi.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

public final class PaymentOrder {

	private static final Pattern PAYMENT_ORDER_ID_PATTERN = Pattern.compile("^PO-[A-Z0-9-]{4,36}$");

	private final String paymentOrderId;
	private final String externalReference;
	private final AccountNumber debtorAccount;
	private final AccountNumber creditorAccount;
	private final MonetaryAmount instructedAmount;
	private final String remittanceInformation;
	private final LocalDate requestedExecutionDate;
	private final PaymentOrderStatus status;
	private final OffsetDateTime createdAt;
	private final OffsetDateTime lastUpdate;

	private PaymentOrder(
		String paymentOrderId,
		String externalReference,
		AccountNumber debtorAccount,
		AccountNumber creditorAccount,
		MonetaryAmount instructedAmount,
		String remittanceInformation,
		LocalDate requestedExecutionDate,
		PaymentOrderStatus status,
		OffsetDateTime createdAt,
		OffsetDateTime lastUpdate
	) {
		this.paymentOrderId = validatePaymentOrderId(paymentOrderId);
		this.externalReference = validateExternalReference(externalReference);
		this.debtorAccount = Objects.requireNonNull(debtorAccount, "debtorAccount is required");
		this.creditorAccount = Objects.requireNonNull(creditorAccount, "creditorAccount is required");
		this.instructedAmount = Objects.requireNonNull(instructedAmount, "instructedAmount is required");
		this.remittanceInformation = validateRemittanceInformation(remittanceInformation);
		this.requestedExecutionDate = Objects.requireNonNull(
			requestedExecutionDate,
			"requestedExecutionDate is required"
		);
		this.status = Objects.requireNonNull(status, "status is required");
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
		this.lastUpdate = Objects.requireNonNull(lastUpdate, "lastUpdate is required");
		if (lastUpdate.isBefore(createdAt)) {
			throw new IllegalArgumentException("lastUpdate must be greater than or equal to createdAt");
		}
	}

	public static PaymentOrder initiate(
		String paymentOrderId,
		String externalReference,
		AccountNumber debtorAccount,
		AccountNumber creditorAccount,
		MonetaryAmount instructedAmount,
		String remittanceInformation,
		LocalDate requestedExecutionDate,
		OffsetDateTime now
	) {
		Objects.requireNonNull(now, "now is required");
		return new PaymentOrder(
			paymentOrderId,
			externalReference,
			debtorAccount,
			creditorAccount,
			instructedAmount,
			remittanceInformation,
			requestedExecutionDate,
			PaymentOrderStatus.ACCEPTED,
			now,
			now
		);
	}

	public static PaymentOrder rehydrate(
		String paymentOrderId,
		String externalReference,
		AccountNumber debtorAccount,
		AccountNumber creditorAccount,
		MonetaryAmount instructedAmount,
		String remittanceInformation,
		LocalDate requestedExecutionDate,
		PaymentOrderStatus status,
		OffsetDateTime createdAt,
		OffsetDateTime lastUpdate
	) {
		return new PaymentOrder(
			paymentOrderId,
			externalReference,
			debtorAccount,
			creditorAccount,
			instructedAmount,
			remittanceInformation,
			requestedExecutionDate,
			status,
			createdAt,
			lastUpdate
		);
	}

	public PaymentOrder withStatus(PaymentOrderStatus newStatus, OffsetDateTime updatedAt) {
		Objects.requireNonNull(newStatus, "newStatus is required");
		Objects.requireNonNull(updatedAt, "updatedAt is required");
		if (updatedAt.isBefore(lastUpdate)) {
			throw new IllegalArgumentException("updatedAt must be greater than or equal to lastUpdate");
		}
		return new PaymentOrder(
			paymentOrderId,
			externalReference,
			debtorAccount,
			creditorAccount,
			instructedAmount,
			remittanceInformation,
			requestedExecutionDate,
			newStatus,
			createdAt,
			updatedAt
		);
	}

	public String getPaymentOrderId() {
		return paymentOrderId;
	}

	public String getExternalReference() {
		return externalReference;
	}

	public AccountNumber getDebtorAccount() {
		return debtorAccount;
	}

	public AccountNumber getCreditorAccount() {
		return creditorAccount;
	}

	public MonetaryAmount getInstructedAmount() {
		return instructedAmount;
	}

	public String getRemittanceInformation() {
		return remittanceInformation;
	}

	public LocalDate getRequestedExecutionDate() {
		return requestedExecutionDate;
	}

	public PaymentOrderStatus getStatus() {
		return status;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getLastUpdate() {
		return lastUpdate;
	}

	private static String validatePaymentOrderId(String value) {
		Objects.requireNonNull(value, "paymentOrderId is required");
		if (!PAYMENT_ORDER_ID_PATTERN.matcher(value).matches()) {
			throw new IllegalArgumentException("paymentOrderId must match the expected format");
		}
		return value;
	}

	private static String validateExternalReference(String value) {
		Objects.requireNonNull(value, "externalReference is required");
		if (value.isBlank() || value.length() > 64) {
			throw new IllegalArgumentException("externalReference must contain between 1 and 64 characters");
		}
		return value;
	}

	private static String validateRemittanceInformation(String value) {
		if (value == null) {
			return null;
		}
		if (value.isBlank() || value.length() > 140) {
			throw new IllegalArgumentException("remittanceInformation must contain between 1 and 140 characters");
		}
		return value;
	}
}

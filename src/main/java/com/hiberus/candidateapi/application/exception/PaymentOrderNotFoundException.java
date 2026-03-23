package com.hiberus.candidateapi.application.exception;

public class PaymentOrderNotFoundException extends RuntimeException {

	public PaymentOrderNotFoundException(String paymentOrderId) {
		super("No payment order exists for identifier '%s'".formatted(paymentOrderId));
	}
}

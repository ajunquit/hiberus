package com.hiberus.candidateapi.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

public record AccountNumber(String iban) {

	private static final Pattern IBAN_PATTERN = Pattern.compile("^[A-Z]{2}[0-9A-Z]{6,32}$");

	public AccountNumber {
		Objects.requireNonNull(iban, "iban is required");
		if (!IBAN_PATTERN.matcher(iban).matches()) {
			throw new IllegalArgumentException("iban must match the expected format");
		}
	}
}

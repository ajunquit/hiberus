package com.hiberus.candidateapi.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hiberus.candidateapi.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

class AccountNumberTest {

	@Test
	void shouldCreateAccountNumberWhenIbanMatchesExpectedFormat() {
		AccountNumber accountNumber = new AccountNumber("EC12DEBTOR");

		assertThat(accountNumber.iban()).isEqualTo("EC12DEBTOR");
	}

	@Test
	void shouldRejectAccountNumberWhenIbanDoesNotMatchExpectedFormat() {
		assertThatThrownBy(() -> new AccountNumber("invalid-iban"))
			.isInstanceOf(DomainValidationException.class)
			.hasMessage("iban must match the expected format");
	}
}

package com.hiberus.candidateapi.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hiberus.candidateapi.domain.exception.DomainValidationException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MonetaryAmountTest {

	@Test
	void shouldNormalizeAmountScaleWhenValueIsValid() {
		MonetaryAmount monetaryAmount = new MonetaryAmount(new BigDecimal("150.75"), "USD");

		assertThat(monetaryAmount.amount()).isEqualByComparingTo("150.75");
		assertThat(monetaryAmount.currency()).isEqualTo("USD");
	}

	@Test
	void shouldRejectAmountWhenItIsNotPositive() {
		assertThatThrownBy(() -> new MonetaryAmount(new BigDecimal("0.00"), "USD"))
			.isInstanceOf(DomainValidationException.class)
			.hasMessage("amount must be greater than zero");
	}

	@Test
	void shouldRejectAmountWhenItUsesMoreThanTwoDecimalPlaces() {
		assertThatThrownBy(() -> new MonetaryAmount(new BigDecimal("10.123"), "USD"))
			.isInstanceOf(DomainValidationException.class)
			.hasMessage("amount must use at most 2 decimal places");
	}

	@Test
	void shouldRejectCurrencyWhenItDoesNotUseThreeUppercaseLetters() {
		assertThatThrownBy(() -> new MonetaryAmount(new BigDecimal("10.00"), "usd"))
			.isInstanceOf(DomainValidationException.class)
			.hasMessage("currency must be a 3-letter uppercase code");
	}
}

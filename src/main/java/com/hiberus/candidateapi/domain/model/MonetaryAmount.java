package com.hiberus.candidateapi.domain.model;

import com.hiberus.candidateapi.domain.exception.DomainValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.regex.Pattern;

public record MonetaryAmount(BigDecimal amount, String currency) {

    private static final Pattern CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");

    public MonetaryAmount {
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(currency, "currency is required");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException("amount must be greater than zero");
        }
        if (amount.scale() > 2) {
            throw new DomainValidationException("amount must use at most 2 decimal places");
        }
        if (!CURRENCY_PATTERN.matcher(currency).matches()) {
            throw new DomainValidationException("currency must be a 3-letter uppercase code");
        }

        amount = amount.setScale(2, RoundingMode.UNNECESSARY);
    }
}

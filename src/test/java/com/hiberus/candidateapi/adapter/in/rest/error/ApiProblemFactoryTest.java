package com.hiberus.candidateapi.adapter.in.rest.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.hiberus.candidateapi.generated.model.InvalidParam;
import com.hiberus.candidateapi.generated.model.Problem;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApiProblemFactoryTest {

	private final ApiProblemFactory apiProblemFactory = new ApiProblemFactory();

	@Test
	void shouldBuildValidationProblemWithInvalidParams() {
		Problem problem = apiProblemFactory.validationProblem(
			"/payment-initiation/payment-orders",
			"One or more fields contain invalid values.",
			List.of(new InvalidParam().name("debtorAccount.iban").reason("must match the expected format"))
		);

		assertThat(problem.getStatus()).isEqualTo(400);
		assertThat(problem.getTitle()).isEqualTo("Request validation failed");
		assertThat(problem.getType().toString()).endsWith("/validation-error");
		assertThat(problem.getInvalidParams()).hasSize(1);
	}

	@Test
	void shouldBuildNotFoundProblem() {
		Problem problem = apiProblemFactory.notFoundProblem(
			"/payment-initiation/payment-orders/PO-9999",
			"No payment order exists for identifier 'PO-9999'"
		);

		assertThat(problem.getStatus()).isEqualTo(404);
		assertThat(problem.getTitle()).isEqualTo("PaymentOrder not found");
		assertThat(problem.getType().toString()).endsWith("/payment-order-not-found");
	}
}

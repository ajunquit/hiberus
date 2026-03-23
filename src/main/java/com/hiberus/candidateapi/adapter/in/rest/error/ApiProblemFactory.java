package com.hiberus.candidateapi.adapter.in.rest.error;

import com.hiberus.candidateapi.generated.model.InvalidParam;
import com.hiberus.candidateapi.generated.model.Problem;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ApiProblemFactory {

	private static final String PROBLEM_BASE_URI = "https://api.hiberus.local/problems/";

	public Problem validationProblem(String instance, String detail, List<InvalidParam> invalidParams) {
		Problem problem = baseProblem(
			HttpStatus.BAD_REQUEST,
			"validation-error",
			"Request validation failed",
			detail,
			instance
		);
		if (invalidParams != null && !invalidParams.isEmpty()) {
			problem.invalidParams(invalidParams);
		}
		return problem;
	}

	public Problem notFoundProblem(String instance, String detail) {
		return baseProblem(
			HttpStatus.NOT_FOUND,
			"payment-order-not-found",
			"PaymentOrder not found",
			detail,
			instance
		);
	}

	public Problem internalServerErrorProblem(String instance) {
		return baseProblem(
			HttpStatus.INTERNAL_SERVER_ERROR,
			"internal-server-error",
			"Internal server error",
			"The service could not complete the request due to an unexpected condition.",
			instance
		);
	}

	private Problem baseProblem(
		HttpStatus status,
		String type,
		String title,
		String detail,
		String instance
	) {
		return new Problem()
			.type(URI.create(PROBLEM_BASE_URI + type))
			.title(title)
			.status(status.value())
			.detail(detail)
			.instance(instance);
	}
}

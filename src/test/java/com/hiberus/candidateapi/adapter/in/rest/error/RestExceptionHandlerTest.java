package com.hiberus.candidateapi.adapter.in.rest.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.hiberus.candidateapi.application.exception.PaymentOrderNotFoundException;
import com.hiberus.candidateapi.domain.exception.DomainValidationException;
import com.hiberus.candidateapi.generated.model.Problem;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

class RestExceptionHandlerTest {

	private final RestExceptionHandler restExceptionHandler = new RestExceptionHandler(new ApiProblemFactory());
	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void shouldMapMethodArgumentValidationToProblemDetails() throws Exception {
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "paymentOrder");
		bindingResult.addError(new FieldError("paymentOrder", "debtorAccount.iban", "must match the expected format"));
		Method method = getClass().getDeclaredMethod("sampleMethod", String.class);
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
			new MethodParameter(method, 0),
			bindingResult
		);

		var response = restExceptionHandler.handleMethodArgumentNotValid(
			exception,
			new HttpHeaders(),
			HttpStatus.BAD_REQUEST,
			new ServletWebRequest(requestFor("/payment-initiation/payment-orders"))
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		Problem problem = (Problem) response.getBody();
		assertThat(problem.getInvalidParams()).hasSize(1);
		assertThat(problem.getInvalidParams().get(0).getName()).isEqualTo("debtorAccount.iban");
	}

	@Test
	void shouldMapDomainValidationToProblemDetails() {
		var response = restExceptionHandler.handleDomainValidation(
			new DomainValidationException("amount must be greater than zero"),
			requestFor("/payment-initiation/payment-orders")
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().getDetail()).isEqualTo("amount must be greater than zero");
	}

	@Test
	void shouldMapConstraintViolationToProblemDetails() {
		var violations = validator.validate(new InvalidRequest(""));

		var response = restExceptionHandler.handleConstraintViolation(
			new jakarta.validation.ConstraintViolationException(violations),
			requestFor("/payment-initiation/payment-orders/invalid/status")
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().getInvalidParams()).isNotEmpty();
	}

	@Test
	void shouldMapPaymentOrderNotFoundToProblemDetails() {
		var response = restExceptionHandler.handlePaymentOrderNotFound(
			new PaymentOrderNotFoundException("PO-9999"),
			requestFor("/payment-initiation/payment-orders/PO-9999")
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody().getTitle()).isEqualTo("PaymentOrder not found");
	}

	@Test
	void shouldMapUnreadablePayloadToProblemDetails() {
		var response = restExceptionHandler.handleHttpMessageNotReadable(
			new HttpMessageNotReadableException("bad payload"),
			new HttpHeaders(),
			HttpStatus.BAD_REQUEST,
			new ServletWebRequest(requestFor("/payment-initiation/payment-orders"))
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(((Problem) response.getBody()).getDetail())
			.isEqualTo("Request body is malformed or uses an unsupported format.");
	}

	@Test
	void shouldMapUnexpectedExceptionToInternalServerErrorProblem() {
		var response = restExceptionHandler.handleUnexpectedException(
			new IllegalStateException("boom"),
			requestFor("/payment-initiation/payment-orders/PO-0001")
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody().getTitle()).isEqualTo("Internal server error");
	}

	@SuppressWarnings("unused")
	private void sampleMethod(String ignored) {
	}

	private MockHttpServletRequest requestFor(String requestUri) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI(requestUri);
		return request;
	}

	private record InvalidRequest(@NotBlank String value) {}
}

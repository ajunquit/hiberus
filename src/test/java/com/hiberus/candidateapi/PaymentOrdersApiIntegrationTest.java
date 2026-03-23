package com.hiberus.candidateapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.hiberus.candidateapi.generated.model.AccountReference;
import com.hiberus.candidateapi.generated.model.InstructedAmount;
import com.hiberus.candidateapi.generated.model.PaymentOrder;
import com.hiberus.candidateapi.generated.model.PaymentOrderInitiationRequest;
import com.hiberus.candidateapi.generated.model.PaymentOrderStatusCode;
import com.hiberus.candidateapi.generated.model.PaymentOrderStatusView;
import com.hiberus.candidateapi.generated.model.Problem;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentOrdersApiIntegrationTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void shouldInitiateRetrieveAndRetrieveStatusForPaymentOrder() {
		PaymentOrderInitiationRequest request = new PaymentOrderInitiationRequest()
			.externalReference("EXT-1")
			.debtorAccount(new AccountReference().iban("EC12DEBTOR"))
			.creditorAccount(new AccountReference().iban("EC98CREDITOR"))
			.instructedAmount(new InstructedAmount().amount(new BigDecimal("150.75")).currency("USD"))
			.remittanceInformation("Factura 001-123")
			.requestedExecutionDate(java.time.LocalDate.parse("2025-10-31"));

		ResponseEntity<PaymentOrder> createResponse = restTemplate.postForEntity(
			"/payment-initiation/payment-orders",
			request,
			PaymentOrder.class
		);

		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(createResponse.getHeaders().getLocation()).isNotNull();
		assertThat(createResponse.getBody()).isNotNull();
		assertThat(createResponse.getBody().getStatus()).isEqualTo(PaymentOrderStatusCode.ACCEPTED);

		String paymentOrderId = createResponse.getBody().getPaymentOrderId();

		ResponseEntity<PaymentOrder> retrieveResponse = restTemplate.getForEntity(
			"/payment-initiation/payment-orders/" + paymentOrderId,
			PaymentOrder.class
		);
		ResponseEntity<PaymentOrderStatusView> statusResponse = restTemplate.getForEntity(
			"/payment-initiation/payment-orders/" + paymentOrderId + "/status",
			PaymentOrderStatusView.class
		);

		assertThat(retrieveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(retrieveResponse.getBody()).isNotNull();
		assertThat(retrieveResponse.getBody().getPaymentOrderId()).isEqualTo(paymentOrderId);
		assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(statusResponse.getBody()).isNotNull();
		assertThat(statusResponse.getBody().getPaymentOrderId()).isEqualTo(paymentOrderId);
	}

	@Test
	void shouldReturnProblemDetailsWhenPaymentOrderDoesNotExist() {
		ResponseEntity<Problem> response = restTemplate.getForEntity(
			"/payment-initiation/payment-orders/PO-9999",
			Problem.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTitle()).isEqualTo("PaymentOrder not found");
	}

	@Test
	void shouldReturnProblemDetailsWhenRequestBodyIsInvalid() {
		Map<String, Object> request = Map.of(
			"debtorAccount", Map.of("iban", "invalid"),
			"creditorAccount", Map.of("iban", "EC98CREDITOR"),
			"instructedAmount", Map.of("amount", 150.75, "currency", "USD")
		);

		ResponseEntity<Problem> response = restTemplate.postForEntity(
			"/payment-initiation/payment-orders",
			request,
			Problem.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTitle()).isEqualTo("Request validation failed");
		assertThat(response.getBody().getInvalidParams()).isNotEmpty();
	}

	@Test
	void shouldReturnProblemDetailsWhenPathVariableDoesNotMatchContract() {
		ResponseEntity<Problem> response = restTemplate.getForEntity(
			"/payment-initiation/payment-orders/invalid/status",
			Problem.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getInvalidParams()).isNotEmpty();
	}

	@Test
	void shouldExposePaymentInitiationMetadataOnInfoEndpoint() {
		ResponseEntity<Map> response = restTemplate.exchange(
			"/actuator/info",
			HttpMethod.GET,
			HttpEntity.EMPTY,
			Map.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).containsKey("paymentInitiation");
		assertThat(((Map<?, ?>) response.getBody().get("paymentInitiation")).get("behaviorQualifier"))
			.isEqualTo("PaymentOrder");
	}
}

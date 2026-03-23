package com.hiberus.candidateapi;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.hiberus.candidateapi.generated.model.AccountReference;
import com.hiberus.candidateapi.generated.model.InstructedAmount;
import com.hiberus.candidateapi.generated.model.PaymentOrder;
import com.hiberus.candidateapi.generated.model.PaymentOrderInitiationRequest;
import com.hiberus.candidateapi.generated.model.PaymentOrderStatusCode;
import com.hiberus.candidateapi.generated.model.PaymentOrderStatusView;
import com.hiberus.candidateapi.generated.model.Problem;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentOrdersApiIntegrationTest {

	@LocalServerPort
	private int port;

	@BeforeEach
	void setUpRestAssured() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
	}

	@Test
	void shouldInitiateRetrieveAndRetrieveStatusForPaymentOrder() {
		PaymentOrderInitiationRequest request = new PaymentOrderInitiationRequest()
			.externalReference("EXT-1")
			.debtorAccount(new AccountReference().iban("EC12DEBTOR"))
			.creditorAccount(new AccountReference().iban("EC98CREDITOR"))
			.instructedAmount(new InstructedAmount().amount(new BigDecimal("150.75")).currency("USD"))
			.remittanceInformation("Factura 001-123")
			.requestedExecutionDate(LocalDate.parse("2025-10-31"));

		Response createResponse = given()
			.contentType(ContentType.JSON)
			.body(request)
			.when()
			.post("/payment-initiation/payment-orders")
			.then()
			.statusCode(HttpStatus.CREATED.value())
			.extract()
			.response();

		PaymentOrder createdPaymentOrder = createResponse.as(PaymentOrder.class);
		assertThat(createResponse.getHeader("Location")).isNotBlank();
		assertThat(createdPaymentOrder).isNotNull();
		assertThat(createdPaymentOrder.getStatus()).isEqualTo(PaymentOrderStatusCode.ACCEPTED);

		String paymentOrderId = createdPaymentOrder.getPaymentOrderId();

		PaymentOrder retrieveResponse = given()
			.accept(ContentType.JSON)
			.when()
			.get("/payment-initiation/payment-orders/{paymentOrderId}", paymentOrderId)
			.then()
			.statusCode(HttpStatus.OK.value())
			.extract()
			.as(PaymentOrder.class);

		PaymentOrderStatusView statusResponse = given()
			.accept(ContentType.JSON)
			.when()
			.get("/payment-initiation/payment-orders/{paymentOrderId}/status", paymentOrderId)
			.then()
			.statusCode(HttpStatus.OK.value())
			.extract()
			.as(PaymentOrderStatusView.class);

		assertThat(retrieveResponse).isNotNull();
		assertThat(retrieveResponse.getPaymentOrderId()).isEqualTo(paymentOrderId);
		assertThat(statusResponse).isNotNull();
		assertThat(statusResponse.getPaymentOrderId()).isEqualTo(paymentOrderId);
	}

	@Test
	void shouldReturnProblemDetailsWhenPaymentOrderDoesNotExist() {
		Response response = given()
			.accept("application/problem+json")
			.when()
			.get("/payment-initiation/payment-orders/{paymentOrderId}", "PO-9999")
			.then()
			.statusCode(HttpStatus.NOT_FOUND.value())
			.extract()
			.response();

		Problem problem = response.as(Problem.class);
		assertThat(response.getContentType()).startsWith("application/problem+json");
		assertThat(problem).isNotNull();
		assertThat(problem.getTitle()).isEqualTo("PaymentOrder not found");
	}

	@Test
	void shouldReturnProblemDetailsWhenRequestBodyIsInvalid() {
		Map<String, Object> request = Map.of(
			"debtorAccount", Map.of("iban", "invalid"),
			"creditorAccount", Map.of("iban", "EC98CREDITOR"),
			"instructedAmount", Map.of("amount", 150.75, "currency", "USD")
		);

		Response response = given()
			.contentType(ContentType.JSON)
			.accept("application/problem+json")
			.body(request)
			.when()
			.post("/payment-initiation/payment-orders")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.extract()
			.response();

		Problem problem = response.as(Problem.class);
		assertThat(response.getContentType()).startsWith("application/problem+json");
		assertThat(problem).isNotNull();
		assertThat(problem.getTitle()).isEqualTo("Request validation failed");
		assertThat(problem.getInvalidParams()).isNotEmpty();
	}

	@Test
	void shouldReturnProblemDetailsWhenPathVariableDoesNotMatchContract() {
		Problem problem = given()
			.accept("application/problem+json")
			.when()
			.get("/payment-initiation/payment-orders/{paymentOrderId}/status", "invalid")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.extract()
			.as(Problem.class);

		assertThat(problem).isNotNull();
		assertThat(problem.getInvalidParams()).isNotEmpty();
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldExposePaymentInitiationMetadataOnInfoEndpoint() {
		Map<String, Object> response = given()
			.accept(ContentType.JSON)
			.when()
			.get("/actuator/info")
			.then()
			.statusCode(HttpStatus.OK.value())
			.extract()
			.as(Map.class);

		assertThat(response).containsKey("paymentInitiation");
		assertThat(((Map<String, Object>) response.get("paymentInitiation")).get("behaviorQualifier"))
			.isEqualTo("PaymentOrder");
	}
}

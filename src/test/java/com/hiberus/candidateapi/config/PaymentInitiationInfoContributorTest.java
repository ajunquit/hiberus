package com.hiberus.candidateapi.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;

class PaymentInitiationInfoContributorTest {

	@Test
	void shouldContributePaymentInitiationMetadata() {
		PaymentInitiationInfoContributor contributor = new PaymentInitiationInfoContributor(
			"candidateapi",
			"8075"
		);
		Info.Builder builder = new Info.Builder();

		contributor.contribute(builder);

		Info info = builder.build();
		assertThat(info.get("paymentInitiation")).isInstanceOf(Map.class);
		assertThat(((Map<?, ?>) info.get("paymentInitiation")).get("behaviorQualifier")).isEqualTo("PaymentOrder");
		assertThat(((Map<?, ?>) info.get("paymentInitiation")).get("serverPort")).isEqualTo("8075");
	}
}

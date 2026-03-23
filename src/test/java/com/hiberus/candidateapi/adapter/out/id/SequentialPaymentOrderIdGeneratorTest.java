package com.hiberus.candidateapi.adapter.out.id;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SequentialPaymentOrderIdGeneratorTest {

	@Test
	void shouldGenerateSequentialPaymentOrderIdentifiers() {
		SequentialPaymentOrderIdGenerator generator = new SequentialPaymentOrderIdGenerator();

		assertThat(generator.nextId()).isEqualTo("PO-0001");
		assertThat(generator.nextId()).isEqualTo("PO-0002");
	}
}

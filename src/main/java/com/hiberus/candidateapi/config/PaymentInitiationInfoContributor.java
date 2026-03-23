package com.hiberus.candidateapi.config;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class PaymentInitiationInfoContributor implements InfoContributor {

    private final String applicationName;
    private final String serverPort;

    public PaymentInitiationInfoContributor(
        @Value("${spring.application.name}") String applicationName,
        @Value("${server.port}") String serverPort
    ) {
        this.applicationName = applicationName;
        this.serverPort = serverPort;
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail(
            "paymentInitiation",
            Map.of(
                "application", applicationName,
                "serviceDomain", "Payment Initiation",
                "behaviorQualifier", "PaymentOrder",
                "persistence", "in-memory",
                "serverPort", serverPort
            )
        );
    }
}

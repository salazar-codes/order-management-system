package com.portfolio.orderservice.client;

import com.portfolio.orderservice.exception.PaymentChargeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentClient {

    private final RestClient restClient;

    public PaymentClient(@Qualifier("paymentRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void charge(UUID orderId, BigDecimal amount) {
        try {
            restClient.post()
                    .uri("/api/payments")
                    .body(new ChargeRequest(orderId, amount))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException ex) {
            // payment-service responde 402 cuando su regla de simulación
            // rechaza el monto (ver PaymentFailedException allá).
            throw new PaymentChargeException(ex.getResponseBodyAsString());
        }
    }
}
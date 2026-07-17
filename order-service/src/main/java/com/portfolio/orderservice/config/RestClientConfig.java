package com.portfolio.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Reintroducido en el Paso 6, después de haberlo retirado en el Paso 3: en
 * aquel momento TODA la comunicación entre servicios pasó a ser asíncrona
 * por Kafka. Este bean es exclusivamente para el pre-chequeo síncrono de
 * disponibilidad — la única llamada HTTP directa entre servicios que queda
 * en el proyecto, y por eso es la única candidata a un circuit breaker.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient inventoryRestClient(@Value("${services.inventory.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
package com.portfolio.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * RestClient (Spring Framework 6.1+) es el reemplazo moderno de RestTemplate:
 * misma naturaleza bloqueante/síncrona (encaja con Spring MVC clásico, no
 * necesitamos WebFlux aquí), pero con una API fluida más legible. No usamos
 * WebClient reactivo en estos 3 servicios a propósito — eso lo reservamos
 * para order-query-service en el Paso 5, como pieza contenida de aprendizaje.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient inventoryRestClient(@Value("${services.inventory.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public RestClient paymentRestClient(@Value("${services.payment.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
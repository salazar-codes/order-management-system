package com.portfolio.orderservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * La única llamada HTTP síncrona directa entre servicios que queda en el
 * proyecto (todo lo demás del SAGA es Kafka desde el Paso 3). Por ser
 * síncrona y potencialmente lenta/fallida, es la candidata natural para un
 * circuit breaker.
 *
 * @Retry se aplica ANTES que @CircuitBreaker en la cadena de aspectos de
 * Resilience4j (el orden de las anotaciones en el método no importa, el
 * framework siempre envuelve Retry por fuera de CircuitBreaker) — así,
 * varios reintentos fallidos SEGUIDOS son los que cuentan como una sola
 * "llamada fallida" para las estadísticas del circuit breaker, evitando que
 * los reintentos internos inflen artificialmente la tasa de fallos.
 */
@Component
public class InventoryAvailabilityClient {

    private final RestClient restClient;

    public InventoryAvailabilityClient(@Qualifier("inventoryRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @CircuitBreaker(name = "inventoryAvailability", fallbackMethod = "fallbackAssumeUnavailable")
    @Retry(name = "inventoryAvailability")
    public boolean checkAvailability(String sku, int quantity) {
        AvailabilityResponse response = restClient.get()
                .uri("/api/products/availability?sku={sku}&quantity={quantity}", sku, quantity)
                .retrieve()
                .body(AvailabilityResponse.class);
        return response != null && response.available();
    }

    /**
     * Se invoca cuando el circuito está ABIERTO (inventory-service viene
     * fallando mucho) o cuando se agotaron los reintentos. La firma debe
     * calzar EXACTAMENTE con el método original + un Throwable al final —
     * es un requisito de Resilience4j, no una convención opcional.
     *
     * Decisión deliberada: devolvemos "false" (no disponible) en vez de
     * asumir que sí hay stock. Preferimos rechazar pedidos de más (falso
     * negativo, el cliente reintenta) a arrancar SAGAs que probablemente
     * tampoco van a poder completarse porque inventory-service está caído.
     */
    private boolean fallbackAssumeUnavailable(String sku, int quantity, Throwable t) {
        return false;
    }

    public record AvailabilityResponse(boolean available) {
    }
}
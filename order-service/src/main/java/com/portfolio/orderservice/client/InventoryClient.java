package com.portfolio.orderservice.client;

import com.portfolio.orderservice.exception.StockReservationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(@Qualifier("inventoryRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void reserveStock(UUID orderId, List<ReserveItemRequest> items) {
        try {
            restClient.post()
                    .uri("/api/inventory/reserve")
                    .body(new ReserveStockRequest(orderId, items))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict ex) {
            // inventory-service responde 409 exactamente cuando no hay stock
            // suficiente (ver InsufficientStockException allá). Lo traducimos
            // a nuestra propia excepción de dominio: el orquestador no
            // debería tener que conocer detalles de HTTP.
            throw new StockReservationException(ex.getResponseBodyAsString());
        }
    }

    public void releaseStock(UUID orderId) {
        // Compensación: se llama solo cuando el cobro falló DESPUÉS de haber
        // reservado con éxito. No necesita enviar los items de nuevo — el
        // orderId le basta a inventory-service para saber qué deshacer,
        // porque StockReservation ya guardó esa información allá.
        restClient.post()
                .uri("/api/inventory/release/{orderId}", orderId)
                .retrieve()
                .toBodilessEntity();
    }
}
package com.portfolio.orderservice.saga;

import com.portfolio.orderservice.client.InventoryAvailabilityClient;
import com.portfolio.orderservice.dto.CreateOrderRequest;
import org.springframework.stereotype.Component;

/**
 * Pre-chequeo de MEJOR ESFUERZO, no autoritativo. Existe puramente como
 * optimización: evitar arrancar todo el SAGA (crear el pedido, escribir al
 * outbox, esperar la respuesta async) cuando ya sabemos de antemano que muy
 * probablemente va a fallar. La validación que de verdad compromete stock
 * sigue siendo reserveStock() dentro del SAGA (Paso 2) — por eso esto NO
 * reemplaza esa validación, solo se adelanta a ella cuando puede.
 *
 * Nota sobre condición de carrera (TOCTOU - time-of-check to time-of-use):
 * es posible (aunque raro) que el stock alcance en este chequeo y ya no
 * alcance milisegundos después, cuando el SAGA real intente reservar. Eso
 * está bien: el SAGA sigue siendo la fuente de verdad y cancelará el pedido
 * correctamente si eso pasa (tal como vimos en el Paso 2).
 */
@Component
public class StockAvailabilityValidator {

    private final InventoryAvailabilityClient inventoryAvailabilityClient;

    public StockAvailabilityValidator(InventoryAvailabilityClient inventoryAvailabilityClient) {
        this.inventoryAvailabilityClient = inventoryAvailabilityClient;
    }

    public boolean allItemsLikelyAvailable(CreateOrderRequest request) {
        return request.items().stream()
                .allMatch(item -> inventoryAvailabilityClient.checkAvailability(item.productSku(), item.quantity()));
    }
}
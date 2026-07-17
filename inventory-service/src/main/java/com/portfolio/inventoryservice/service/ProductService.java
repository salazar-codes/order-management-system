package com.portfolio.inventoryservice.service;

import com.portfolio.inventoryservice.dto.CreateProductRequest;
import com.portfolio.inventoryservice.dto.ProductResponse;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse getProduct(UUID id);

    List<ProductResponse> listProducts();

    /**
     * Chequeo RÁPIDO y no autoritativo: no reserva nada, solo mira el
     * contador actual. Usado por el pre-chequeo síncrono de order-service
     * (Paso 6) antes de arrancar el SAGA — la validación real sigue siendo
     * reserveStock() en StockReservationService.
     */
    boolean checkAvailability(String sku, int quantity);
}

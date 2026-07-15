package com.portfolio.inventoryservice.service;

import com.portfolio.inventoryservice.dto.ReserveStockRequest;

import java.util.UUID;

public interface StockReservationService {

    void reserveStock(ReserveStockRequest request);

    void releaseStock(UUID orderId);
}
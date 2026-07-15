package com.portfolio.inventoryservice.repository;

import com.portfolio.inventoryservice.domain.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {
    List<StockReservation> findByOrderIdAndActiveTrue(UUID orderId);
}
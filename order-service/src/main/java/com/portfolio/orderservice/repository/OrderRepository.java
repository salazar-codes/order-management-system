package com.portfolio.orderservice.repository;

import com.portfolio.orderservice.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    // Spring Data genera la implementación en runtime a partir de esta interfaz.
    // No necesitamos escribir SQL ni implementación mientras el CRUD sea simple.
}

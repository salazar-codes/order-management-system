package com.portfolio.orderservice.service;

import com.portfolio.orderservice.dto.CreateOrderRequest;
import com.portfolio.orderservice.dto.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrder(UUID id);

    List<OrderResponse> listOrders();
}

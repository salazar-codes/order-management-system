package com.portfolio.orderservice.service;

import com.portfolio.orderservice.domain.Order;
import com.portfolio.orderservice.dto.OrderResponse;
import com.portfolio.orderservice.exception.OrderNotFoundException;
import com.portfolio.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return OrderResponse.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .toList();
    }
}
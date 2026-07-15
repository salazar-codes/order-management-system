package com.portfolio.orderservice.service;

import com.portfolio.orderservice.domain.Order;
import com.portfolio.orderservice.domain.OrderItem;
import com.portfolio.orderservice.dto.CreateOrderRequest;
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

    // Constructor injection: nada de @Autowired en campos. Esto hace que la
    // clase sea trivial de instanciar en un test unitario con `new
    // OrderServiceImpl(mockRepository)`, sin levantar el contexto de Spring.
    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        List<OrderItem> items = request.items().stream()
                .map(i -> new OrderItem(i.productSku(), i.quantity(), i.unitPrice()))
                .toList();

        Order order = new Order(request.customerId(), items);
        Order saved = orderRepository.save(order);
        return OrderResponse.from(saved);
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

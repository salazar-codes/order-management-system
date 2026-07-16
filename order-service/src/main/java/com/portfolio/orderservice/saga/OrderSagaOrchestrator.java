package com.portfolio.orderservice.saga;

import com.portfolio.orderservice.domain.Order;
import com.portfolio.orderservice.domain.OrderItem;
import com.portfolio.orderservice.dto.CreateOrderRequest;
import com.portfolio.orderservice.dto.OrderResponse;
import com.portfolio.orderservice.exception.OrderNotFoundException;
import com.portfolio.orderservice.messaging.OrderEventPublisher;
import com.portfolio.orderservice.messaging.OrderStatusChanged;
import com.portfolio.orderservice.messaging.PaymentChargeReplied;
import com.portfolio.orderservice.messaging.PaymentChargeRequested;
import com.portfolio.orderservice.messaging.StockReleaseRequested;
import com.portfolio.orderservice.messaging.StockReservationReplied;
import com.portfolio.orderservice.messaging.StockReservationRequested;
import com.portfolio.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * El orquestador del SAGA, ahora asíncrono. Ya NO hay un solo método que
 * corre los 3 pasos de punta a punta — el flujo se divide en 3 métodos que
 * se disparan en momentos distintos: uno al crear el pedido, y dos más cada
 * vez que llega un evento de respuesta por Kafka.
 *
 * Nota deliberada sobre @Transactional: envuelve el guardado en la base Y
 * el encolado al outbox en el mismo método, y eso SÍ los hace atómicos entre
 * sí desde el Paso 4 (ambos INSERT/UPDATE viven en la misma transacción
 * Postgres). El envío real a Kafka lo hace OutboxPoller por separado.
 *
 * NUEVO en este paso: cada vez que el estado del pedido cambia, además de
 * los eventos de coordinación del SAGA, publicamos un OrderStatusChanged —
 * el evento "para el mundo exterior" que order-query-service va a consumir.
 */
@Service
public class OrderSagaOrchestrator {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public OrderSagaOrchestrator(OrderRepository orderRepository, OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse createOrderAndStartSaga(CreateOrderRequest request) {
        List<OrderItem> items = request.items().stream()
                .map(i -> new OrderItem(i.productSku(), i.quantity(), i.unitPrice()))
                .toList();

        Order order = new Order(request.customerId(), items);
        order = orderRepository.save(order);
        publishStatusChanged(order); // CREATED

        List<StockReservationRequested.ReservationItem> reservationItems = order.getItems().stream()
                .map(item -> new StockReservationRequested.ReservationItem(item.getProductSku(), item.getQuantity()))
                .toList();
        eventPublisher.publishStockReservationRequested(
                new StockReservationRequested(order.getId(), reservationItems));

        // El pedido se devuelve en CREATED — el cliente NO espera a que el
        // SAGA termine. Tiene que volver a consultar (o, desde el Paso 5,
        // preguntarle a order-query-service) para ver cómo avanza.
        return OrderResponse.from(order);
    }

    /** Reacciona a la respuesta de inventory-service (topic stock.reservation.replied). */
    @Transactional
    public void handleStockReservationReplied(StockReservationReplied event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderNotFoundException(event.orderId()));

        if (!event.approved()) {
            // No hay nada que compensar: inventory-service valida ANTES de
            // mutar, así que un rechazo aquí significa que no reservó nada.
            order.markCancelled();
            orderRepository.save(order);
            publishStatusChanged(order); // CANCELLED
            return;
        }

        order.markStockReserved();
        orderRepository.save(order);
        publishStatusChanged(order); // STOCK_RESERVED

        eventPublisher.publishPaymentChargeRequested(
                new PaymentChargeRequested(order.getId(), order.getTotalAmount()));
    }

    /** Reacciona a la respuesta de payment-service (topic payment.charge.replied). */
    @Transactional
    public void handlePaymentChargeReplied(PaymentChargeReplied event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderNotFoundException(event.orderId()));

        if (!event.approved()) {
            // Aquí SÍ hay que compensar: el paso anterior (reservar) tuvo
            // éxito y quedó comprometido en inventory-service.
            eventPublisher.publishStockReleaseRequested(new StockReleaseRequested(order.getId()));
            order.markCancelled();
            orderRepository.save(order);
            publishStatusChanged(order); // CANCELLED
            return;
        }

        order.markConfirmed();
        orderRepository.save(order);
        publishStatusChanged(order); // CONFIRMED
    }

    /**
     * Un solo punto centralizado para armar y encolar el evento "externo".
     * Así cualquier transición futura que agreguemos al SAGA (por ejemplo,
     * en el Paso 6 con el circuit breaker) solo necesita llamar a este
     * método, sin duplicar la construcción del evento en cada lugar.
     */
    private void publishStatusChanged(Order order) {
        eventPublisher.publishOrderStatusChanged(new OrderStatusChanged(
                order.getId(),
                order.getCustomerId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                Instant.now()
        ));
    }
}
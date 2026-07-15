package com.portfolio.orderservice.saga;

import com.portfolio.orderservice.domain.Order;
import com.portfolio.orderservice.domain.OrderItem;
import com.portfolio.orderservice.dto.CreateOrderRequest;
import com.portfolio.orderservice.dto.OrderResponse;
import com.portfolio.orderservice.exception.OrderNotFoundException;
import com.portfolio.orderservice.messaging.OrderEventPublisher;
import com.portfolio.orderservice.messaging.PaymentChargeReplied;
import com.portfolio.orderservice.messaging.PaymentChargeRequested;
import com.portfolio.orderservice.messaging.StockReleaseRequested;
import com.portfolio.orderservice.messaging.StockReservationReplied;
import com.portfolio.orderservice.messaging.StockReservationRequested;
import com.portfolio.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * El orquestador del SAGA, ahora asíncrono. Ya NO hay un solo método que
 * corre los 3 pasos de punta a punta — el flujo se divide en 3 métodos que
 * se disparan en momentos distintos: uno al crear el pedido, y dos más cada
 * vez que llega un evento de respuesta por Kafka.
 *
 * Nota deliberada sobre @Transactional: envuelve el guardado en la base Y
 * el publish a Kafka en el mismo método, pero eso NO los hace atómicos entre
 * sí — @Transactional aquí solo gestiona la transacción JPA. Si el save()
 * tiene éxito pero el publish() falla justo después, el pedido queda
 * "colgado" sin que nadie se entere. Ese es el "dual write problem" que el
 * Outbox pattern del Paso 4 va a resolver — lo dejamos sin resolver aquí a
 * propósito.
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

        List<StockReservationRequested.ReservationItem> reservationItems = order.getItems().stream()
                .map(item -> new StockReservationRequested.ReservationItem(item.getProductSku(), item.getQuantity()))
                .toList();
        eventPublisher.publishStockReservationRequested(
                new StockReservationRequested(order.getId(), reservationItems));

        // El pedido se devuelve en CREATED — el cliente NO espera a que el
        // SAGA termine. Tiene que volver a consultar GET /api/orders/{id}
        // para ver cómo avanza. Esta es la diferencia central con el Paso 2.
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
            return;
        }

        order.markStockReserved();
        orderRepository.save(order);

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
            return;
        }

        order.markConfirmed();
        orderRepository.save(order);
    }
}
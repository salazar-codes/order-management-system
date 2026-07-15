package com.portfolio.orderservice.saga;

import com.portfolio.orderservice.client.InventoryClient;
import com.portfolio.orderservice.client.PaymentClient;
import com.portfolio.orderservice.client.ReserveItemRequest;
import com.portfolio.orderservice.domain.Order;
import com.portfolio.orderservice.domain.OrderItem;
import com.portfolio.orderservice.dto.CreateOrderRequest;
import com.portfolio.orderservice.dto.OrderResponse;
import com.portfolio.orderservice.exception.PaymentChargeException;
import com.portfolio.orderservice.exception.StockReservationException;
import com.portfolio.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * El orquestador del SAGA. A propósito NO tiene @Transactional a nivel de
 * clase o método: cada order.save() de abajo es su propia transacción local
 * corta (Spring Data la abre y cierra sola dentro de save()). Envolver todo
 * el flujo en una sola transacción mantendría una conexión a base de datos
 * abierta mientras esperamos respuestas HTTP de otros servicios — exactamente
 * el antipatrón que el SAGA existe para evitar.
 *
 * Flujo:
 *   1. Guardar Order en CREATED               (transacción local #1)
 *   2. Reservar stock en inventory-service     (llamada HTTP remota)
 *      - falla -> marcar CANCELLED y terminar  (transacción local #2a)
 *      - éxito -> marcar STOCK_RESERVED        (transacción local #2b)
 *   3. Cobrar en payment-service               (llamada HTTP remota)
 *      - falla -> COMPENSAR: liberar stock     (llamada HTTP remota)
 *              -> marcar CANCELLED             (transacción local #3a)
 *      - éxito -> marcar CONFIRMED             (transacción local #3b)
 */
@Service
public class OrderSagaOrchestrator {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;

    public OrderSagaOrchestrator(OrderRepository orderRepository,
                                 InventoryClient inventoryClient,
                                 PaymentClient paymentClient) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
    }

    public OrderResponse createOrderAndRunSaga(CreateOrderRequest request) {
        List<OrderItem> items = request.items().stream()
                .map(i -> new OrderItem(i.productSku(), i.quantity(), i.unitPrice()))
                .toList();

        Order order = new Order(request.customerId(), items);
        order = orderRepository.save(order); // transacción local #1

        return runSaga(order);
    }

    private OrderResponse runSaga(Order order) {
        List<ReserveItemRequest> reserveItems = order.getItems().stream()
                .map(item -> new ReserveItemRequest(item.getProductSku(), item.getQuantity()))
                .toList();

        try {
            inventoryClient.reserveStock(order.getId(), reserveItems);
        } catch (StockReservationException ex) {
            // No hay nada que compensar: inventory-service validó ANTES de
            // mutar (ver InsufficientStockException), así que si esto falló
            // no reservó absolutamente nada.
            order.markCancelled();
            orderRepository.save(order);
            return OrderResponse.from(order);
        }

        order.markStockReserved();
        orderRepository.save(order);

        try {
            paymentClient.charge(order.getId(), order.getTotalAmount());
        } catch (PaymentChargeException ex) {
            // Aquí SÍ hay que compensar: el paso anterior (reservar) tuvo
            // éxito y quedó comprometido en inventory-service. Deshacerlo es
            // una acción de negocio explícita, no un rollback automático.
            inventoryClient.releaseStock(order.getId());
            order.markCancelled();
            orderRepository.save(order);
            return OrderResponse.from(order);
        }

        order.markConfirmed();
        orderRepository.save(order);
        return OrderResponse.from(order);
    }
}
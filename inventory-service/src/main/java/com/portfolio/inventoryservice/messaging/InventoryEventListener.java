package com.portfolio.inventoryservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.inventoryservice.dto.ReserveItemRequest;
import com.portfolio.inventoryservice.dto.ReserveStockRequest;
import com.portfolio.inventoryservice.exception.InsufficientStockException;
import com.portfolio.inventoryservice.exception.ProductNotFoundException;
import com.portfolio.inventoryservice.service.StockReservationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryEventListener {

    private final ObjectMapper objectMapper;
    private final StockReservationService reservationService;
    private final InventoryEventPublisher eventPublisher;

    public InventoryEventListener(ObjectMapper objectMapper,
                                  StockReservationService reservationService,
                                  InventoryEventPublisher eventPublisher) {
        this.objectMapper = objectMapper;
        this.reservationService = reservationService;
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = KafkaTopics.STOCK_RESERVATION_REQUESTED)
    public void onStockReservationRequested(ConsumerRecord<String, String> record) throws JsonProcessingException {
        StockReservationRequested event = objectMapper.readValue(record.value(), StockReservationRequested.class);

        List<ReserveItemRequest> items = event.items().stream()
                .map(item -> new ReserveItemRequest(item.sku(), item.quantity()))
                .toList();

        try {
            reservationService.reserveStock(new ReserveStockRequest(event.orderId(), items));
            eventPublisher.publishStockReservationReplied(
                    new StockReservationReplied(event.orderId(), true, null));
        } catch (InsufficientStockException | ProductNotFoundException ex) {
            // No relanzamos: un fallo de NEGOCIO (no hay stock) no es un
            // error de infraestructura. Lo traducimos a un evento de
            // respuesta normal, que order-service va a interpretar y actuar
            // en consecuencia (cancelar el pedido).
            eventPublisher.publishStockReservationReplied(
                    new StockReservationReplied(event.orderId(), false, ex.getMessage()));
        }
    }

    @KafkaListener(topics = KafkaTopics.STOCK_RELEASE_REQUESTED)
    public void onStockReleaseRequested(ConsumerRecord<String, String> record) throws JsonProcessingException {
        StockReleaseRequested event = objectMapper.readValue(record.value(), StockReleaseRequested.class);
        // releaseStock ya es idempotente (ver StockReservationServiceImpl):
        // si Kafka reintenta la entrega de este mensaje, no pasa nada raro.
        reservationService.releaseStock(event.orderId());
    }
}
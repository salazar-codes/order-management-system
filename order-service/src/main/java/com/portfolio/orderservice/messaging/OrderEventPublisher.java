package com.portfolio.orderservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.orderservice.outbox.OutboxEvent;
import com.portfolio.orderservice.outbox.OutboxEventRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * ANTES (Paso 3): estos métodos llamaban a kafkaTemplate.send(...) directo.
 * AHORA (Paso 4): guardan una fila en outbox_events, dentro de la MISMA
 * transacción JPA que ya está corriendo (la de OrderSagaOrchestrator). Esa
 * es la esencia del patrón: el Order y su evento saliente viven o mueren
 * juntos en un solo commit de Postgres. El envío real a Kafka lo hace
 * OutboxPoller, en otro momento, leyendo esta tabla.
 *
 * Fíjate que la API pública de esta clase NO cambió — OrderSagaOrchestrator
 * no tuvo que tocarse para nada. Ese aislamiento es exactamente lo que buscas
 * cuando separas "qué evento hay que publicar" (el orquestador) de "cómo se
 * publica de forma confiable" (esta clase).
 */
@Component
public class OrderEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OrderEventPublisher(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void publishStockReservationRequested(StockReservationRequested event) {
        enqueue(KafkaTopics.STOCK_RESERVATION_REQUESTED, event.orderId(), event);
    }

    public void publishPaymentChargeRequested(PaymentChargeRequested event) {
        enqueue(KafkaTopics.PAYMENT_CHARGE_REQUESTED, event.orderId(), event);
    }

    public void publishStockReleaseRequested(StockReleaseRequested event) {
        enqueue(KafkaTopics.STOCK_RELEASE_REQUESTED, event.orderId(), event);
    }

    private void enqueue(String topic, UUID orderId, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            outboxEventRepository.save(new OutboxEvent(topic, orderId.toString(), json));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el evento para el topic " + topic, e);
        }
    }
}
package com.portfolio.orderservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Serializamos a JSON "a mano" con ObjectMapper y enviamos como String, en
 * vez de dejar que Spring Kafka serialice objetos Java directamente. Así
 * evitamos que el deserializador del lado consumidor dependa del nombre de
 * clase Java del productor (que ni siquiera existe en el classpath del otro
 * servicio) — el contrato es el JSON en sí, no un tipo compartido.
 */
@Component
public class OrderEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishStockReservationRequested(StockReservationRequested event) {
        send(KafkaTopics.STOCK_RESERVATION_REQUESTED, event.orderId(), event);
    }

    public void publishPaymentChargeRequested(PaymentChargeRequested event) {
        send(KafkaTopics.PAYMENT_CHARGE_REQUESTED, event.orderId(), event);
    }

    public void publishStockReleaseRequested(StockReleaseRequested event) {
        send(KafkaTopics.STOCK_RELEASE_REQUESTED, event.orderId(), event);
    }

    private void send(String topic, java.util.UUID orderId, Object payload) {
        try {
            // El orderId como KEY del mensaje: garantiza que todos los
            // mensajes de un mismo pedido caigan en la misma partición y se
            // procesen en orden entre sí (Kafka solo ordena dentro de una
            // partición, nunca entre particiones distintas).
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, orderId.toString(), json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el evento para el topic " + topic, e);
        }
    }
}
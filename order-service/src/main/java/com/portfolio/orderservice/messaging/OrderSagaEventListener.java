package com.portfolio.orderservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.orderservice.saga.OrderSagaOrchestrator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Punto de entrada de los eventos que le llegan a order-service. Nótese que
 * este listener no decide nada por sí mismo — parsea el JSON y se lo pasa al
 * orquestador, que es quien conoce las reglas del SAGA.
 */
@Component
public class OrderSagaEventListener {

    private final ObjectMapper objectMapper;
    private final OrderSagaOrchestrator orchestrator;

    public OrderSagaEventListener(ObjectMapper objectMapper, OrderSagaOrchestrator orchestrator) {
        this.objectMapper = objectMapper;
        this.orchestrator = orchestrator;
    }

    @KafkaListener(topics = KafkaTopics.STOCK_RESERVATION_REPLIED)
    public void onStockReservationReplied(ConsumerRecord<String, String> record) throws JsonProcessingException {
        StockReservationReplied event = objectMapper.readValue(record.value(), StockReservationReplied.class);
        orchestrator.handleStockReservationReplied(event);
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_CHARGE_REPLIED)
    public void onPaymentChargeReplied(ConsumerRecord<String, String> record) throws JsonProcessingException {
        PaymentChargeReplied event = objectMapper.readValue(record.value(), PaymentChargeReplied.class);
        orchestrator.handlePaymentChargeReplied(event);
    }
}
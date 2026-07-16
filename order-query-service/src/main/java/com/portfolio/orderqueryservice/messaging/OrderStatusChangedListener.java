package com.portfolio.orderqueryservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.orderqueryservice.domain.OrderView;
import com.portfolio.orderqueryservice.write.OrderViewWriter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor CLÁSICO de Spring Kafka (bloqueante), no Reactor Kafka. Este
 * método corre en un hilo dedicado del listener container de Spring Kafka —
 * un hilo que NUNCA comparte con el servidor Netty que atiende las requests
 * HTTP reactivas. Por eso bloquear aquí con .block() es seguro: no le quita
 * capacidad al servidor WebFlux, que sigue siendo 100% no bloqueante para
 * las lecturas HTTP. Esta es la migración recomendada oficialmente por
 * Spring tras la discontinuación de Reactor Kafka (mayo 2025).
 */
@Component
public class OrderStatusChangedListener {

    private final ObjectMapper objectMapper;
    private final OrderViewWriter orderViewWriter;

    public OrderStatusChangedListener(ObjectMapper objectMapper, OrderViewWriter orderViewWriter) {
        this.objectMapper = objectMapper;
        this.orderViewWriter = orderViewWriter;
    }

    @KafkaListener(topics = "order.status.changed")
    public void onOrderStatusChanged(ConsumerRecord<String, String> record) throws JsonProcessingException {
        OrderStatusChanged event = objectMapper.readValue(record.value(), OrderStatusChanged.class);

        OrderView view = new OrderView(
                event.orderId(),
                event.customerId(),
                event.status(),
                event.totalAmount(),
                event.updatedAt()
        );

        orderViewWriter.upsert(view).block();
    }
}
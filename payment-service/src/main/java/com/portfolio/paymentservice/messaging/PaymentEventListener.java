package com.portfolio.paymentservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.paymentservice.dto.CreatePaymentRequest;
import com.portfolio.paymentservice.exception.PaymentFailedException;
import com.portfolio.paymentservice.service.PaymentService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;
    private final PaymentEventPublisher eventPublisher;

    public PaymentEventListener(ObjectMapper objectMapper,
                                PaymentService paymentService,
                                PaymentEventPublisher eventPublisher) {
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_CHARGE_REQUESTED)
    public void onPaymentChargeRequested(ConsumerRecord<String, String> record) throws JsonProcessingException {
        PaymentChargeRequested event = objectMapper.readValue(record.value(), PaymentChargeRequested.class);

        try {
            paymentService.createPayment(new CreatePaymentRequest(event.orderId(), event.amount()));
            eventPublisher.publishPaymentChargeReplied(
                    new PaymentChargeReplied(event.orderId(), true, null));
        } catch (PaymentFailedException ex) {
            // Igual que en inventory-service: un rechazo de negocio se
            // traduce a un evento de respuesta, nunca se relanza como error
            // de infraestructura.
            eventPublisher.publishPaymentChargeReplied(
                    new PaymentChargeReplied(event.orderId(), false, ex.getMessage()));
        }
    }
}
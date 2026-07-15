package com.portfolio.paymentservice.messaging;

public final class KafkaTopics {

    public static final String PAYMENT_CHARGE_REQUESTED = "payment.charge.requested";
    public static final String PAYMENT_CHARGE_REPLIED = "payment.charge.replied";

    private KafkaTopics() {
    }
}
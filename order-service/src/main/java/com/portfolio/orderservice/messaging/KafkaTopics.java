package com.portfolio.orderservice.messaging;

/**
 * Nombres de topics centralizados en un solo lugar, para no tener strings
 * mágicos repetidos entre el publisher y los listeners.
 */
public final class KafkaTopics {

    public static final String STOCK_RESERVATION_REQUESTED = "stock.reservation.requested";
    public static final String STOCK_RESERVATION_REPLIED = "stock.reservation.replied";
    public static final String PAYMENT_CHARGE_REQUESTED = "payment.charge.requested";
    public static final String PAYMENT_CHARGE_REPLIED = "payment.charge.replied";
    public static final String STOCK_RELEASE_REQUESTED = "stock.release.requested";

    private KafkaTopics() {
    }
}
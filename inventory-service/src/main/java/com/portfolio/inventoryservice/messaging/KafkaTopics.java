package com.portfolio.inventoryservice.messaging;

public final class KafkaTopics {

    public static final String STOCK_RESERVATION_REQUESTED = "stock.reservation.requested";
    public static final String STOCK_RESERVATION_REPLIED = "stock.reservation.replied";
    public static final String STOCK_RELEASE_REQUESTED = "stock.release.requested";

    private KafkaTopics() {
    }
}
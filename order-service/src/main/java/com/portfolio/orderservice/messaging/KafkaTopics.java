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

    /**
     * Topic distinto a los 4 anteriores: mientras esos son coordinación
     * INTERNA del SAGA (le hablan a inventory-service y payment-service),
     * este es para cualquiera que solo quiera saber "¿cómo va este
     * pedido?" — como order-query-service en el Paso 5. Contiene solo lo
     * que a un lector le importa, no los detalles de cómo se llegó ahí.
     */
    public static final String ORDER_STATUS_CHANGED = "order.status.changed";

    private KafkaTopics() {
    }
}
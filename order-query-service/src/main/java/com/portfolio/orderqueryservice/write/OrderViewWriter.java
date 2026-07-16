package com.portfolio.orderqueryservice.write;

import com.portfolio.orderqueryservice.domain.OrderView;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Por qué esto no es simplemente "orderViewRepository.save(view)":
 *
 * Spring Data R2DBC decide si hacer INSERT o UPDATE mirando si la entidad
 * "es nueva". Para IDs autogenerados eso es fácil (id nulo = nueva). Pero
 * nuestro orderId NO se autogenera aquí — nos llega ya asignado desde el
 * evento OrderStatusChanged de order-service. Sin más contexto, R2DBC
 * asumiría "ya existe" y generaría un UPDATE... que no inserta nada la
 * primera vez que este pedido aparece (0 filas afectadas, silenciosamente).
 *
 * En vez de pelear con esa ambigüedad (implementando Persistable<UUID>,
 * etc.), usamos SQL explícito con "INSERT ... ON CONFLICT DO UPDATE" — un
 * upsert real de Postgres. Es más directo de leer y no depende de que
 * Spring adivine correctamente si el registro es nuevo o no.
 */
@Component
public class OrderViewWriter {

    private static final String UPSERT_SQL = """
            INSERT INTO order_view (order_id, customer_id, status, total_amount, updated_at)
            VALUES (:orderId, :customerId, :status, :totalAmount, :updatedAt)
            ON CONFLICT (order_id) DO UPDATE
            SET customer_id = EXCLUDED.customer_id,
                status = EXCLUDED.status,
                total_amount = EXCLUDED.total_amount,
                updated_at = EXCLUDED.updated_at
            """;

    private final DatabaseClient databaseClient;

    public OrderViewWriter(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<Void> upsert(OrderView view) {
        return databaseClient.sql(UPSERT_SQL)
                .bind("orderId", view.orderId())
                .bind("customerId", view.customerId())
                .bind("status", view.status())
                .bind("totalAmount", view.totalAmount())
                .bind("updatedAt", view.updatedAt())
                .then();
    }
}
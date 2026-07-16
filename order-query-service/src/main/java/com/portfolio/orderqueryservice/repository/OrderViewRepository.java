package com.portfolio.orderqueryservice.repository;

import com.portfolio.orderqueryservice.domain.OrderView;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * ReactiveCrudRepository, no JpaRepository: cada método devuelve Mono/Flux
 * en vez del valor directo — findById ya no es "bloquéate hasta tener el
 * resultado", es "aquí tienes la PROMESA de un resultado". Spring WebFlux
 * sabe cómo "suscribirse" a eso automáticamente cuando lo devuelves desde un
 * controller.
 *
 * Solo se usa para LEER. La escritura/upsert vive en OrderViewWriter,
 * porque el patrón save()-decide-INSERT-o-UPDATE de Spring Data R2DBC no
 * maneja bien IDs asignados manualmente (nuestro caso: el orderId ya viene
 * dado por el evento, no se autogenera) — ver esa clase para el detalle.
 */
public interface OrderViewRepository extends ReactiveCrudRepository<OrderView, UUID> {
    Flux<OrderView> findByCustomerId(String customerId);
}
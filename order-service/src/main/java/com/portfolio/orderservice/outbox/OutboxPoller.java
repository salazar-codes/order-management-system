package com.portfolio.orderservice.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * El único componente de order-service que le habla a Kafka para PUBLICAR
 * (el consumo de respuestas sigue en OrderSagaEventListener). Cada ciclo:
 * busca eventos PENDING, intenta publicarlos, y solo marca PUBLISHED el que
 * Kafka confirmó de verdad.
 *
 * Nota sobre el .get(): kafkaTemplate.send(...) es asíncrono por defecto
 * (devuelve un Future). Usamos .get() para bloquear hasta tener la
 * confirmación del broker ANTES de decidir si marcar el evento como
 * publicado — si no esperáramos, podríamos marcar PUBLISHED un evento que en
 * realidad Kafka todavía no confirmó.
 *
 * Nota sobre concurrencia: con una sola instancia de order-service corriendo
 * (nuestro caso hoy) esto es perfectamente seguro. Si algún día corrieras
 * VARIAS instancias en paralelo, dos pollers podrían leer la misma fila
 * PENDING al mismo tiempo y publicarla duplicada — ahí se necesitaría
 * bloqueo a nivel de fila (SELECT ... FOR UPDATE SKIP LOCKED). Lo dejamos
 * fuera de alcance por ahora.
 */
@Component
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPoller(OutboxEventRepository outboxEventRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getMessageKey(), event.getPayload())
                        .get(5, TimeUnit.SECONDS);
                event.markPublished();
                outboxEventRepository.save(event);
            } catch (Exception e) {
                // NO relanzamos: dejamos el evento en PENDING. El próximo
                // ciclo (en 1 segundo) lo vuelve a intentar solo. Esto es lo
                // que hace que el patrón sea resiliente a que Kafka esté
                // caído momentáneamente.
                log.warn("No se pudo publicar el evento outbox {} al topic {}, se reintentará: {}",
                        event.getId(), event.getTopic(), e.getMessage());
            }
        }
    }
}
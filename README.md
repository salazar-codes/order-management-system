# Order Management System — Portafolio Java/Spring

Sistema de gestión de pedidos con microservicios, construido incrementalmente
como pieza de portafolio. Este README se irá actualizando en cada fase.

## Estado actual: Paso 1 de 13

✅ `order-service`, `inventory-service` y `payment-service` como CRUDs
independientes, cada uno con su propia base de datos PostgreSQL.
Sin distribución todavía: sin Kafka, sin SAGA, sin Gateway.

Próximos pasos: SAGA por orquestación (Paso 2) → Kafka (Paso 3) → Outbox
(Paso 4) → CQRS con `order-query-service` en WebFlux (Paso 5) → Resilience4j
(Paso 6) → reglas de validación funcionales (Paso 7) → tests con
Testcontainers (Paso 8) → OpenAPI (Paso 9) → Postman/Newman (Paso 10) →
API Gateway (Paso 11) → docker-compose completo (Paso 12) → frontend (Paso 13).

## Cómo correr esto hoy

1. Levanta las 3 bases de datos:
   ```
   docker-compose -f docker-compose.dev.yml up -d
   ```
2. En 3 terminales distintas, levanta cada servicio (necesitas Java 21 y Maven):
   ```
   cd order-service     && ./mvnw spring-boot:run   # puerto 8081
   cd inventory-service  && ./mvnw spring-boot:run   # puerto 8082
   cd payment-service    && ./mvnw spring-boot:run   # puerto 8083
   ```
3. Prueba cada uno, por ejemplo:
   ```
   curl -X POST http://localhost:8081/api/orders \
     -H "Content-Type: application/json" \
     -d '{"customerId":"cust-1","items":[{"productSku":"SKU-1","quantity":2,"unitPrice":19.90}]}'

   curl -X POST http://localhost:8082/api/products \
     -H "Content-Type: application/json" \
     -d '{"sku":"SKU-1","name":"Teclado mecánico","initialStock":50}'

   curl -X POST http://localhost:8083/api/payments \
     -H "Content-Type: application/json" \
     -d '{"orderId":"<uuid-de-un-pedido>","amount":39.80}'
   ```

Nota: el wrapper `mvnw` no viene incluido en este esqueleto — genera cada
wrapper localmente con `mvn -N wrapper:wrapper` dentro de cada carpeta de
servicio, o usa tu `mvn` global.

## Por qué cada servicio tiene su propia base de datos

Es el principio de "database per service": ningún servicio puede leer ni
escribir directamente en la tabla de otro. Es lo que hace *necesarios* los
patrones que vienen después (SAGA, eventos, CQRS) — no son complejidad
gratuita, son la consecuencia de no compartir base de datos.

## Resumen de lo aprendido en el Paso 1

- **Entidades JPA vs. DTOs record**: las entidades necesitan mutabilidad y
  constructor vacío para que Hibernate las gestione; los DTOs, al cruzar la
  frontera HTTP, sí pueden (y deben) ser inmutables → records.
- **Controller sin lógica de negocio**: el controller solo válida el shape
  del request (`@Valid`) y traduce a/desde DTOs. Toda decisión vive en el
  `service`.
- **Constructor injection**: hace que cada `*ServiceImpl` sea instanciable en
  un test unitario sin levantar Spring — clave cuando lleguemos al
  Paso 8 con JUnit + Mockito.
- **GlobalExceptionHandler por servicio**: evita repetir manejo de errores
  en cada controller y centraliza el formato de respuesta de error.

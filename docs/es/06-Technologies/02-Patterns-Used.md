---
tags: [technologies, es]
---

🌐 **Idioma:** Español · [English](../../en/06-Technologies/02-Patterns-Used.md)
🏠 [[../00-Home|Inicio]] › Tecnologías › Patrones Utilizados

# 🧩 Patrones de Diseño Utilizados

| Patrón | Dónde se aplica | Detalle |
|---|---|---|
| **Event-Driven Architecture** | `order-service` → `payment-service` → `notification-service` | Comunicación asíncrona vía Kafka en lugar de llamadas REST encadenadas. Ver [[../05-Kafka-Events/01-Topics\|Tópicos de Kafka]] |
| **Service Layer Pattern** | Todos los servicios | Interfaces `XxxService` + implementación `impl.XxxServiceImpl`, separando la lógica de negocio de los controllers |
| **Repository Pattern** | `UserRepository`, `CustomerRepository`, `CarSaleRepository`, `PaymentRepository` | Interfaces `JpaRepository<T, Long>` de Spring Data |
| **DTO Pattern** | `LoginRequest`/`RegisterRequest` (api-gateway), `CreateCarSaleRequest`/`CarSaleResponse` (order-service) | Separa el modelo de persistencia del contrato expuesto por la API |
| **Mapper Pattern (MapStruct)** | `CarSaleMapper` en `order-service` | Genera código de mapeo entidad → evento/DTO en tiempo de compilación |
| **Builder Pattern (Lombok `@Builder`)** | `CarSaleCreatedEvent`, `PaymentProcessedEvent`, `Payment` | Construcción legible de objetos inmutables/complejos |
| **Global Exception Handler** | `@RestControllerAdvice` en cada servicio | Centraliza el manejo de `BusinessException`/`PaymentException` y excepciones genéricas, devolviendo respuestas consistentes |
| **Circuit Breaker + Retry (Resilience4j)** | `PaymentServiceImpl.processPayment` (`payment-service`) | Protege la llamada a Stripe: 3 reintentos, *circuit breaker* con umbral de fallo del 50% |
| **Shared Kernel** | Módulo `shared-lib` | Constantes de tópicos/estados, DTOs de eventos y `JwtUtil` compartidos entre servicios, evitando duplicación de contratos |
| **Soft State Transition** | `CarSale.status` (`PENDING → PAID/CANCELLED/COMPLETED`) | El `DELETE /sales/{id}` no borra la fila, solo cambia el estado a `CANCELLED`, y solo si no está ya `PAID` |
| **Stateless Authentication (JWT)** | `api-gateway` | `SessionCreationPolicy.STATELESS` + token JWT autocontenido (sin sesión de servidor) |
| **Homologated Response Envelope** | `GenericResponse` (`shared-lib`), extendida por `LoginResponse`, `CarSaleResponse`, `PaymentResponse`, etc. | Todas las respuestas REST (éxito y error, incluyendo los 401 de Spring Security) comparten `response_code`/`description`/`timestamp`, vía herencia en vez de un wrapper `data`. Ver [[../04-API-Reference/00-Response-Format\|Formato de Respuesta]] |

> [!info] Patrones que el proyecto NO implementa
> Vale la pena documentar también las ausencias, para no asumir que existen: no hay **Saga Pattern** (a pesar del flujo multi-servicio, no existe compensación/rollback si un paso falla), no hay **Dead Letter Queue** para mensajes fallidos, no hay **API Gateway real** (routing/agregación), y no hay **Outbox Pattern** (el `INSERT` en base de datos y el `publish` a Kafka en `order-service` no son transaccionalmente atómicos). Ver [[../01-Architecture/04-Design-Decisions|Decisiones de Diseño]] para el detalle completo de limitaciones.

---

**Ver también:** [[01-Stack|Stack Tecnológico]] · [[../01-Architecture/01-System-Design|Diseño del Sistema]]

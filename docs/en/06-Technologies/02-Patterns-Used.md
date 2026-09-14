---
tags: [technologies, en]
---

🌐 **Language:** English · [Español](../../es/06-Technologies/02-Patterns-Used.md)
🏠 [[../00-Home|Home]] › Technologies › Patterns Used

# 🧩 Design Patterns Used

| Pattern | Where it's applied | Detail |
|---|---|---|
| **Event-Driven Architecture** | `order-service` → `payment-service` → `notification-service` | Asynchronous communication via Kafka instead of chained REST calls. See [[../05-Kafka-Events/01-Topics\|Kafka Topics]] |
| **Service Layer Pattern** | Every service | `XxxService` interfaces + `impl.XxxServiceImpl` implementation, separating business logic from controllers |
| **Repository Pattern** | `UserRepository`, `CustomerRepository`, `CarSaleRepository`, `PaymentRepository` | Spring Data `JpaRepository<T, Long>` interfaces |
| **DTO Pattern** | `LoginRequest`/`RegisterRequest` (api-gateway), `CreateCarSaleRequest`/`CarSaleResponse` (order-service) | Separates the persistence model from the API's exposed contract |
| **Mapper Pattern (MapStruct)** | `CarSaleMapper` in `order-service` | Generates entity → event/DTO mapping code at compile time |
| **Builder Pattern (Lombok `@Builder`)** | `CarSaleCreatedEvent`, `PaymentProcessedEvent`, `Payment` | Readable construction of immutable/complex objects |
| **Global Exception Handler** | `@RestControllerAdvice` in each service | Centralizes handling of `BusinessException`/`PaymentException` and generic exceptions, returning consistent responses |
| **Circuit Breaker + Retry (Resilience4j)** | `PaymentServiceImpl.processPayment` (`payment-service`) | Protects the Stripe call: 3 retries, circuit breaker with a 50% failure-rate threshold |
| **Shared Kernel** | `shared-lib` module | Topic/status constants, event DTOs, and `JwtUtil` shared across services, avoiding contract duplication |
| **Soft State Transition** | `CarSale.status` (`PENDING → PAID/CANCELLED/COMPLETED`) | `DELETE /sales/{id}` doesn't delete the row — it only flips the status to `CANCELLED`, and only if not already `PAID` |
| **Stateless Authentication (JWT)** | `api-gateway` | `SessionCreationPolicy.STATELESS` + self-contained JWT (no server-side session) |
| **Homologated Response Envelope** | `GenericResponse` (`shared-lib`), extended by `LoginResponse`, `CarSaleResponse`, `PaymentResponse`, etc. | Every REST response (success and error, including Spring Security's 401s) shares `response_code`/`description`/`timestamp`, via inheritance rather than a `data` wrapper. See [[../04-API-Reference/00-Response-Format\|Response Format]] |

> [!info] Patterns the project does NOT implement
> It's worth documenting the absences too, so they aren't assumed to exist: there is no **Saga Pattern** (despite the multi-service flow, there's no compensation/rollback if a step fails), no **Dead Letter Queue** for failed messages, no **real API Gateway** (routing/aggregation), and no **Outbox Pattern** (the database `INSERT` and the Kafka `publish` in `order-service` are not transactionally atomic). See [[../01-Architecture/04-Design-Decisions|Design Decisions]] for the full list of limitations.

---

**See also:** [[01-Stack|Technology Stack]] · [[../01-Architecture/01-System-Design|System Design]]

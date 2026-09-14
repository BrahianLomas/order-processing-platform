---
tags: [technologies, en]
---

🌐 **Language:** English · [Español](../../es/06-Technologies/01-Stack.md)
🏠 [[../00-Home|Home]] › Technologies › Stack

# 🛠️ Technology Stack

## Language and build

| | |
|---|---|
| **Java** | 21 (`sourceCompatibility`/`targetCompatibility` across all modules) |
| **Build tool** | Gradle 9.5.1 (wrapper included), multi-module |
| **Modules** | `shared-lib`, `api-gateway`, `order-service`, `payment-service`, `notification-service` |
| **Group / Version** | `com.brahian` / `1.0.0` |

## Framework and libraries

| Library | Version (root) | Notes |
|---|---|---|
| **Spring Boot** | `4.0.8-SNAPSHOT` | ⚠️ `order-service` and `payment-service` declare `4.1.0` in their own `build.gradle` — see [[../01-Architecture/04-Design-Decisions\|Design Decisions]] |
| **Spring Data JPA** | (via Spring Boot BOM) | Persistence in all 4 Spring Boot services |
| **Spring Kafka** | `3.1.1` | `order-service`, `payment-service`, `notification-service` |
| **Spring Security** | (via Spring Boot BOM) | `api-gateway` (signs JWT), `order-service` and `payment-service` (validate JWT) |
| **springdoc-openapi** | `3.1.0` | Swagger UI on `api-gateway`, `order-service`, `payment-service` (`/api/swagger-ui/index.html`), public, with JWT support via "Authorize" |
| **spring-boot-starter-web** | (via BOM) | Included by `notification-service` but unused (no controllers) |
| **auth0 java-jwt** | `4.4.0` (root) / `4.6.0` (`shared-lib`) | HS256 JWT generation/validation |
| **stripe-java** | `23.10.0` | Payment integration in `payment-service` |
| **resilience4j-spring-boot3 / retry / circuitbreaker** | `2.1.0` | Retry + Circuit Breaker in `payment-service` |
| **Lombok** | `1.18.36` | All modules (`@Data`, `@Builder`, `@Slf4j`, etc.) |
| **MapStruct** | `1.5.5.Final` | Entity ↔ DTO mapping in `order-service` |
| **Jackson (databind / jsr310)** | `2.17.0` (root) / `2.22.1` (`shared-lib`) | JSON serialization for Kafka events |

## Infrastructure (Docker)

| Component | Image | Host port |
|---|---|---|
| MySQL | `mysql:8.0` | `3307 → 3306` |
| Kafka | `confluentinc/cp-kafka:7.5.0` | `9092` |
| Zookeeper | `confluentinc/cp-zookeeper:7.5.0` | `2181` |

Defined only in the root `docker-compose.yml` — the Java microservices have **no** Dockerfile and are not orchestrated via compose.

## Database

- **MySQL 8.0**, single schema `order_platform`
- `spring.jpa.hibernate.ddl-auto=update` on every service (no Flyway/Liquibase migrations)
- `api-gateway` additionally ships a manual reference `sql/schema.sql` (not executed automatically)

---

**See also:** [[02-Patterns-Used|Patterns Used]] · [[../01-Architecture/01-System-Design|System Design]]

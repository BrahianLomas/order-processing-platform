---
tags: [technologies, es]
---

🌐 **Idioma:** Español · [English](../../en/06-Technologies/01-Stack.md)
🏠 [[../00-Home|Inicio]] › Tecnologías › Stack

# 🛠️ Stack Tecnológico

## Lenguaje y build

| | |
|---|---|
| **Java** | 21 (`sourceCompatibility`/`targetCompatibility` en todos los módulos) |
| **Build tool** | Gradle 9.5.1 (wrapper incluido), multi-módulo |
| **Módulos** | `shared-lib`, `api-gateway`, `order-service`, `payment-service`, `notification-service` |
| **Group / Version** | `com.brahian` / `1.0.0` |

## Framework y librerías

| Librería | Versión (root) | Notas |
|---|---|---|
| **Spring Boot** | `4.0.8-SNAPSHOT` | ⚠️ `order-service` y `payment-service` declaran `4.1.0` en su propio `build.gradle` — ver [[../01-Architecture/04-Design-Decisions\|Decisiones de Diseño]] |
| **Spring Data JPA** | (vía BOM Spring Boot) | Persistencia en los 4 servicios Spring Boot |
| **Spring Kafka** | `3.1.1` | `order-service`, `payment-service`, `notification-service` |
| **Spring Security** | (vía BOM Spring Boot) | `api-gateway` (firma JWT), `order-service` y `payment-service` (validan JWT) |
| **springdoc-openapi** | `3.1.0` | Swagger UI en `api-gateway`, `order-service`, `payment-service` (`/api/swagger-ui/index.html`), público, con soporte de JWT vía "Authorize" |
| **spring-boot-starter-web** | (vía BOM) | `notification-service` la incluye pero no la usa (sin controllers) |
| **auth0 java-jwt** | `4.4.0` (root) / `4.6.0` (`shared-lib`) | Generación/validación de JWT HS256 |
| **stripe-java** | `23.10.0` | Integración de pagos en `payment-service` |
| **resilience4j-spring-boot3 / retry / circuitbreaker** | `2.1.0` | Retry + Circuit Breaker en `payment-service` |
| **Lombok** | `1.18.36` | Todos los módulos (`@Data`, `@Builder`, `@Slf4j`, etc.) |
| **MapStruct** | `1.5.5.Final` | Mapeo entidad ↔ DTO en `order-service` |
| **Jackson (databind / jsr310)** | `2.17.0` (root) / `2.22.1` (`shared-lib`) | Serialización JSON de eventos Kafka |

## Infraestructura (Docker)

| Componente | Imagen | Puerto host |
|---|---|---|
| MySQL | `mysql:8.0` | `3307 → 3306` |
| Kafka | `confluentinc/cp-kafka:7.5.0` | `9092` |
| Zookeeper | `confluentinc/cp-zookeeper:7.5.0` | `2181` |

Definidos únicamente en el `docker-compose.yml` raíz — los microservicios Java **no** tienen Dockerfile ni se orquestan vía compose.

## Base de datos

- **MySQL 8.0**, esquema único `order_platform`
- `spring.jpa.hibernate.ddl-auto=update` en todos los servicios (sin migraciones Flyway/Liquibase)
- `api-gateway` además incluye un `sql/schema.sql` manual de referencia (no ejecutado automáticamente)

---

**Ver también:** [[02-Patterns-Used|Patrones Utilizados]] · [[../01-Architecture/01-System-Design|Diseño del Sistema]]

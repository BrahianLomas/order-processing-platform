---
tags: [setup, en]
---

🌐 **Language:** English · [Español](../../es/02-Setup/03-Running-Services.md)
🏠 [[../00-Home|Home]] › Setup › Running the Services

# ▶️ Running the Services

Each microservice is an independent Spring Boot app. There's no `docker-compose` orchestrating them — they run manually via Gradle or as a JAR.

## Commands per service

| Service | Gradle command | Generated JAR | Port |
|---|---|---|---|
| api-gateway | `./gradlew :api-gateway:bootRun` | `api-gateway/build/libs/*.jar` | `8080` |
| order-service | `./gradlew :order-service:bootRun` | `order-service/build/libs/*.jar` | `8081` |
| payment-service | `./gradlew :payment-service:bootRun` | `payment-service/build/libs/*.jar` | `8082` |
| notification-service | `./gradlew :notification-service:bootRun` | `notification-service/build/libs/*.jar` | `8083` |

Alternative via JAR (after `./gradlew build`):

```bash
java -jar api-gateway/build/libs/api-gateway-1.0.0.jar
```

## Recommended startup order

1. **Infrastructure** (Docker: MySQL, Kafka, Zookeeper) — see [[02-Local-Setup|Local Setup Guide]]
2. **`payment-service`** and **`notification-service`** — so their consumer groups (`payment-service`, `notification-service`) are ready before events get published
3. **`order-service`** — the `car-sale-created` producer
4. **`api-gateway`** — can start at any time, it's independent of Kafka

> [!note] The order isn't strictly required
> Kafka retains messages on the topic; if a consumer starts later, it will process pending messages per its `auto-offset-reset=earliest` setting. Still, starting the consumers first avoids confusion when testing manually.

## Manual verification

Since **no service has a confirmed Spring Boot Actuator dependency in the code**, the simplest way to check a service is up is:

```bash
# Check the Spring Boot startup log (banner + "Started Main in X seconds")
# or hit an endpoint directly:
curl http://localhost:8081/api/sales/1
```

To verify Kafka consumption, check the logs of `payment-service` and `notification-service` after creating a sale — you should see `CarSaleEventConsumer` and `PaymentProcessedEventConsumer` traces respectively.

---

**See also:** [[../03-Services/01-API-Gateway|Per-service documentation]]

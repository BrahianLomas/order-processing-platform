---
tags: [setup, es]
---

🌐 **Idioma:** Español · [English](../../en/02-Setup/03-Running-Services.md)
🏠 [[../00-Home|Inicio]] › Instalación › Cómo levantar cada servicio

# ▶️ Cómo levantar cada servicio

Cada microservicio es una app Spring Boot independiente. No existe un `docker-compose` que los orqueste — se ejecutan manualmente con Gradle o como JAR.

## Comandos por servicio

| Servicio | Comando Gradle | JAR generado | Puerto |
|---|---|---|---|
| api-gateway | `./gradlew :api-gateway:bootRun` | `api-gateway/build/libs/*.jar` | `8080` |
| order-service | `./gradlew :order-service:bootRun` | `order-service/build/libs/*.jar` | `8081` |
| payment-service | `./gradlew :payment-service:bootRun` | `payment-service/build/libs/*.jar` | `8082` |
| notification-service | `./gradlew :notification-service:bootRun` | `notification-service/build/libs/*.jar` | `8083` |

Alternativa vía JAR (tras `./gradlew build`):

```bash
java -jar api-gateway/build/libs/api-gateway-1.0.0.jar
```

## Orden recomendado de arranque

1. **Infraestructura** (Docker: MySQL, Kafka, Zookeeper) — ver [[02-Local-Setup|Guía de Instalación Local]]
2. **`payment-service`** y **`notification-service`** — para que sus *consumer groups* (`payment-service`, `notification-service`) estén listos antes de que se publiquen eventos
3. **`order-service`** — productor de `car-sale-created`
4. **`api-gateway`** — puede iniciarse en cualquier momento, es independiente de Kafka

> [!note] El orden no es estrictamente obligatorio
> Kafka retiene los mensajes en el tópico; si un consumidor arranca después, procesará los mensajes pendientes según su `auto-offset-reset=earliest`. Sin embargo, iniciar los consumidores primero evita confusión al probar manualmente.

## Verificación manual

Como **ningún servicio expone Spring Boot Actuator confirmado en el código**, la forma más simple de verificar que un servicio está arriba es:

```bash
# Revisar el log de arranque de Spring Boot (banner + "Started Main in X seconds")
# o probar directamente un endpoint:
curl http://localhost:8081/api/sales/1
```

Para verificar el consumo de Kafka, revisa los logs de `payment-service` y `notification-service` tras crear una venta — deberían mostrar las trazas de `CarSaleEventConsumer` y `PaymentProcessedEventConsumer` respectivamente.

---

**Ver también:** [[../03-Services/01-API-Gateway|Documentación de cada servicio]]

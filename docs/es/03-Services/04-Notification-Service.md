---
tags: [service, es]
---

🌐 **Idioma:** Español · [English](../../en/03-Services/04-Notification-Service.md)
🏠 [[../00-Home|Inicio]] › Servicios › Notification Service

# ✉️ Notification Service

| | |
|---|---|
| **Puerto** | `8083` |
| **Context path** | `/api` |
| **Base package** | `org.example.notification` |
| **Entry point** | `Main` (`@EnableKafka`) |
| **Base de datos** | ninguna entidad definida (datasource configurado pero sin uso) |

> [!info] Consumidor Kafka puro
> Este servicio **no expone ningún endpoint REST** ni define entidades JPA o repositorios, a pesar de tener las dependencias `spring-boot-starter-web` y `spring-boot-starter-data-jpa` en su `build.gradle`. Su única función es escuchar Kafka.

## Estructura de paquetes

```
org.example.notification
├── config.KafkaConsumerConfig
├── kafka.PaymentProcessedEventConsumer
└── service
    ├── NotificationService (interfaz)
    └── impl.NotificationServiceImpl
```

## Kafka — Consumidor

`PaymentProcessedEventConsumer.consumePaymentProcessed(PaymentProcessedEvent event)`

```java
@KafkaListener(topics = AppConstants.TOPIC_PAYMENT_PROCESSED, groupId = "notification-service")
```

- Tópico: **`payment-processed`**
- `group-id`: `notification-service`
- Cualquier excepción se captura y solo se registra en el log (no hay reintento ni DLQ)

## ¿Cómo se "envían" las notificaciones?

`NotificationServiceImpl.sendPaymentConfirmation(PaymentProcessedEvent event)` es **100% simulado**: no existe integración con ningún proveedor real (sin SMTP, sin Twilio, sin AWS SES, sin `JavaMailSender`). El método solo construye texto y lo escribe en el log:

```text
✉️ EMAIL ENVIADO:
Hola,

Tu pago ha sido procesado exitosamente.
Venta ID: {saleId}
Monto: ${amount} {currency}
Transacción: {transactionId}
Estado: {status}

Gracias por tu compra!

📱 SMS ENVIADO: Pago confirmado para venta {saleId}. Transacción: {transactionId}
```

> [!warning] No confundir con un servicio de notificaciones real
> Si este proyecto avanza a producción, este componente necesitará integrarse con un proveedor real de email/SMS (p. ej. SendGrid, Twilio, SES).

## Configuración (`application.properties`)

```properties
server.port=8083
server.servlet.context-path=/api
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=notification-service
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.properties.spring.json.trusted.packages=*
```

El propio archivo de propiedades incluye el comentario `# Database (no necesita, pero para consistency)`, confirmando que el datasource está configurado únicamente por convención, sin uso real.

---

**Ver también:** [[../05-Kafka-Events/03-PaymentProcessedEvent|Evento PaymentProcessedEvent]] · [[../01-Architecture/03-Data-Flow|Flujo de Datos]]

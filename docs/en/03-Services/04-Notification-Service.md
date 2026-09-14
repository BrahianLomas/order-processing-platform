---
tags: [service, en]
---

🌐 **Language:** English · [Español](../../es/03-Services/04-Notification-Service.md)
🏠 [[../00-Home|Home]] › Services › Notification Service

# ✉️ Notification Service

| | |
|---|---|
| **Port** | `8083` |
| **Context path** | `/api` |
| **Base package** | `org.example.notification` |
| **Entry point** | `Main` (`@EnableKafka`) |
| **Database** | no entities defined (datasource configured but unused) |

> [!info] Pure Kafka consumer
> This service **exposes no REST endpoint** and defines no JPA entities or repositories, despite having `spring-boot-starter-web` and `spring-boot-starter-data-jpa` as dependencies in its `build.gradle`. Its only job is listening to Kafka.

## Package structure

```
org.example.notification
├── config.KafkaConsumerConfig
├── kafka.PaymentProcessedEventConsumer
└── service
    ├── NotificationService (interface)
    └── impl.NotificationServiceImpl
```

## Kafka — Consumer

`PaymentProcessedEventConsumer.consumePaymentProcessed(PaymentProcessedEvent event)`

```java
@KafkaListener(topics = AppConstants.TOPIC_PAYMENT_PROCESSED, groupId = "notification-service")
```

- Topic: **`payment-processed`**
- `group-id`: `notification-service`
- Any exception is caught and only logged (no retry, no DLQ)

## How are notifications actually "sent"?

`NotificationServiceImpl.sendPaymentConfirmation(PaymentProcessedEvent event)` is **100% simulated**: there's no integration with any real provider (no SMTP, no Twilio, no AWS SES, no `JavaMailSender`). The method simply builds text and writes it to the log:

```text
✉️ EMAIL SENT:
Hello,

Your payment has been processed successfully.
Sale ID: {saleId}
Amount: ${amount} {currency}
Transaction: {transactionId}
Status: {status}

Thank you for your purchase!

📱 SMS SENT: Payment confirmed for sale {saleId}. Transaction: {transactionId}
```

*(The actual log message text in the code is in Spanish — shown translated here for the English docs.)*

> [!warning] Don't mistake this for a real notification service
> If this project moves toward production, this component will need to integrate with a real email/SMS provider (e.g. SendGrid, Twilio, SES).

## Configuration (`application.properties`)

```properties
server.port=8083
server.servlet.context-path=/api
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=notification-service
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.properties.spring.json.trusted.packages=*
```

The properties file itself includes the comment `# Database (no necesita, pero para consistency)` ("not needed, but for consistency"), confirming the datasource is configured only by convention, with no real use.

---

**See also:** [[../05-Kafka-Events/03-PaymentProcessedEvent|PaymentProcessedEvent]] · [[../01-Architecture/03-Data-Flow|Data Flow]]

---
tags: [architecture, en]
---

🌐 **Language:** English · [Español](../../es/01-Architecture/03-Data-Flow.md)
🏠 [[../00-Home|Home]] › Architecture › Data Flow

# 🔄 Complete Data Flow

End-to-end walkthrough of a car sale, from user registration to the final notification.

```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant GW as API Gateway (:8080)
    participant OS as Order Service (:8081)
    participant K1 as Kafka: car-sale-created
    participant PS as Payment Service (:8082)
    participant Stripe as Stripe API
    participant K2 as Kafka: payment-processed
    participant NS as Notification Service (:8083)
    participant DB as MySQL

    U->>GW: POST /api/auth/register
    GW->>DB: INSERT INTO users
    GW-->>U: 201 {id, email, firstName, lastName}

    U->>GW: POST /api/auth/login
    GW->>DB: SELECT users WHERE email=?
    GW-->>U: 200 {token, type: Bearer, expiresIn}

    U->>OS: POST /api/sales (Authorization: Bearer <token>)
    Note over OS: JwtAuthenticationFilter validates the token<br/>(same app.jwt.secret as the gateway)
    OS->>DB: SELECT customers WHERE email=?
    OS->>DB: INSERT INTO car_sales (status=PENDING)
    OS->>K1: publish CarSaleCreatedEvent
    OS-->>U: 201 CarSaleResponse

    K1->>PS: consume CarSaleCreatedEvent (group: payment-service)
    Note over PS: Kafka consumption doesn't go<br/>through the HTTP security filter
    PS->>Stripe: PaymentIntent.create(amount*100, currency)
    Stripe-->>PS: PaymentIntent
    Note over PS: Test mode:<br/>status is hardcoded to PAID<br/>(simulated)
    PS->>DB: INSERT INTO payments (status=PAID)
    PS->>K2: publish PaymentProcessedEvent

    K2->>NS: consume PaymentProcessedEvent (group: notification-service)
    Note over NS: Simulated "sending":<br/>only logged
    NS-->>NS: log.info("✉️ EMAIL SENT...")
    NS-->>NS: log.info("📱 SMS SENT...")

    U->>PS: GET /api/payments/sales/{saleId} (Authorization: Bearer <token>)
    Note over PS: JwtAuthenticationFilter validates the token<br/>before it reaches the controller
    PS->>DB: SELECT payments WHERE sale_id=?
    PS-->>U: 200 {status: PAID, transactionId, ...}
```

## Key takeaways

1. **The JWT is validated in three services.** `api-gateway` signs it; `order-service` and `payment-service` validate it with the same `app.jwt.secret`. `notification-service` doesn't validate it, but it also exposes no REST endpoints. Kafka consumption (between `order-service` and `payment-service`, and between `payment-service` and `notification-service`) never passes through any HTTP security filter — that only protects incoming REST requests.
2. **Pricing happens in `order-service`:** `tax = (salePrice - discount) * 0.08` and `totalAmount = salePrice - discount + tax`. Curiously, the `sale_price` field on the `CarSaleCreatedEvent` is populated with `totalAmount`, not the raw sale price — see [[../05-Kafka-Events/02-CarSaleCreatedEvent|CarSaleCreatedEvent]].
3. **The payment is simulated.** `payment-service` always marks the payment as `PAID` after creating the `PaymentIntent`, regardless of the actual status Stripe returns (there's an explicit code comment: *"EN TEST MODE, SIMULAR PAGADO"*).
4. **No Dead Letter Topic.** If the Kafka listener throws an exception, it is only logged — there is no topic-level retry or error queue.
5. **The notification is 100% simulated.** There is no integration with any email/SMS provider (no SMTP, no Twilio, no SES).

---

**See also:** [[01-System-Design|System Design]] · [[../05-Kafka-Events/01-Topics|Kafka Topics]]

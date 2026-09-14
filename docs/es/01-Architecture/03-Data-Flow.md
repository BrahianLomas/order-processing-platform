---
tags: [architecture, es]
---

🌐 **Idioma:** Español · [English](../../en/01-Architecture/03-Data-Flow.md)
🏠 [[../00-Home|Inicio]] › Arquitectura › Flujo de Datos

# 🔄 Flujo de Datos Completo

Recorrido end-to-end de una venta de vehículo, desde el registro del usuario hasta la notificación final.

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
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
    Note over OS: JwtAuthenticationFilter valida el token<br/>(mismo app.jwt.secret que el gateway)
    OS->>DB: SELECT customers WHERE email=?
    OS->>DB: INSERT INTO car_sales (status=PENDING)
    OS->>K1: publish CarSaleCreatedEvent
    OS-->>U: 201 CarSaleResponse

    K1->>PS: consume CarSaleCreatedEvent (group: payment-service)
    Note over PS: El consumo de Kafka no pasa<br/>por el filtro de seguridad HTTP
    PS->>Stripe: PaymentIntent.create(amount*100, currency)
    Stripe-->>PS: PaymentIntent
    Note over PS: Modo test:<br/>el status se fija en PAID<br/>de forma simulada
    PS->>DB: INSERT INTO payments (status=PAID)
    PS->>K2: publish PaymentProcessedEvent

    K2->>NS: consume PaymentProcessedEvent (group: notification-service)
    Note over NS: "Envío" simulado:<br/>solo se escribe en el log
    NS-->>NS: log.info("✉️ EMAIL ENVIADO...")
    NS-->>NS: log.info("📱 SMS ENVIADO...")

    U->>PS: GET /api/payments/sales/{saleId} (Authorization: Bearer <token>)
    Note over PS: JwtAuthenticationFilter valida el token<br/>antes de llegar al controller
    PS->>DB: SELECT payments WHERE sale_id=?
    PS-->>U: 200 {status: PAID, transactionId, ...}
```

## Puntos clave del flujo

1. **El JWT se valida en tres servicios.** `api-gateway` lo firma; `order-service` y `payment-service` lo validan con el mismo `app.jwt.secret`. `notification-service` no lo valida, pero tampoco expone endpoints REST. El consumo de Kafka (entre `order-service` y `payment-service`, y entre `payment-service` y `notification-service`) no pasa por ningún filtro de seguridad HTTP — solo protege las peticiones REST entrantes.
2. **El cálculo de precio ocurre en `order-service`:** `tax = (salePrice - discount) * 0.08` y `totalAmount = salePrice - discount + tax`. Curiosamente, el campo `sale_price` del evento `CarSaleCreatedEvent` se llena con `totalAmount`, no con el precio bruto — ver [[../05-Kafka-Events/02-CarSaleCreatedEvent|CarSaleCreatedEvent]].
3. **El pago está simulado.** `payment-service` siempre marca el pago como `PAID` tras crear el `PaymentIntent`, independientemente del estado real que devuelva Stripe (comentario explícito en el código: *"EN TEST MODE, SIMULAR PAGADO"*).
4. **No hay Dead Letter Topic.** Si el consumidor de Kafka lanza una excepción, esta solo se registra en el log; no existe reintento a nivel de tópico ni cola de errores.
5. **La notificación es 100% simulada.** No hay integración con ningún proveedor de email/SMS (sin SMTP, sin Twilio, sin SES).

---

**Ver también:** [[01-System-Design|Diseño del Sistema]] · [[../05-Kafka-Events/01-Topics|Tópicos de Kafka]]

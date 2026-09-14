---
tags: [kafka, es]
---

🌐 **Idioma:** Español · [English](../../en/05-Kafka-Events/03-PaymentProcessedEvent.md)
🏠 [[../00-Home|Inicio]] › Kafka Events › PaymentProcessedEvent

# 📤 PaymentProcessedEvent

| | |
|---|---|
| **Tópico** | `payment-processed` |
| **Productor** | `payment-service` → `PaymentEventProducer.publishPaymentProcessed()` |
| **Consumidor** | `notification-service` → `PaymentProcessedEventConsumer.consumePaymentProcessed()` (`group-id: notification-service`) |
| **Clase** | `org.example.shared.dto.PaymentProcessedEvent` (`shared-lib`) |
| **Disparado por** | Fin del procesamiento de pago en `payment-service` (éxito o fallo), tras consumir [[02-CarSaleCreatedEvent\|CarSaleCreatedEvent]] |

## Esquema (campos `@JsonProperty`)

| Campo Java | JSON | Tipo | Descripción |
|---|---|---|---|
| `saleId` | `sale_id` | `Long` | ID de la venta |
| `transactionId` | `transaction_id` | `String` | ID del `PaymentIntent` de Stripe |
| `amount` | `amount` | `BigDecimal` | Monto cobrado |
| `currency` | `currency` | `String` | Moneda |
| `status` | `status` | `String` | `PAID` o `FAILED` |
| `processedAt` | `processed_at` | `LocalDateTime` | Momento del procesamiento |
| `errorMessage` | `error_message` | `String` | Solo presente si `status = FAILED` |

## Ejemplo — pago exitoso (simulado)

```json
{
  "sale_id": 10,
  "transaction_id": "pi_3Nx...",
  "amount": 26460.00,
  "currency": "usd",
  "status": "PAID",
  "processed_at": "2026-09-03T10:15:03",
  "error_message": null
}
```

## Ejemplo — pago fallido (tras agotar reintentos)

```json
{
  "sale_id": 11,
  "transaction_id": null,
  "amount": 15000.00,
  "currency": "usd",
  "status": "FAILED",
  "processed_at": "2026-09-03T10:20:11",
  "error_message": "<mensaje de la excepción de Stripe>"
}
```

> [!warning] `PAID` no confirma un cobro real
> Como se documenta en [[../03-Services/03-Payment-Service|Payment Service]], `payment-service` marca `status=PAID` de forma simulada tras crear el `PaymentIntent`, sin verificar su estado real en Stripe. `FAILED` solo se emite cuando la creación del `PaymentIntent` lanza una excepción y se agotan los reintentos de Resilience4j (3 intentos).

## Efecto en `notification-service`

Al consumir este evento, `notification-service` **solo registra logs simulando** el envío de un email y un SMS — no hay envío real. Ver [[../03-Services/04-Notification-Service|Notification Service]].

---

**Ver también:** [[01-Topics|Tópicos de Kafka]] · [[02-CarSaleCreatedEvent|CarSaleCreatedEvent]] · [[../04-API-Reference/03-Payment-Endpoints|Payment Endpoints]]

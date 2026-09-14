---
tags: [kafka, en]
---

🌐 **Language:** English · [Español](../../es/05-Kafka-Events/03-PaymentProcessedEvent.md)
🏠 [[../00-Home|Home]] › Kafka Events › PaymentProcessedEvent

# 📤 PaymentProcessedEvent

| | |
|---|---|
| **Topic** | `payment-processed` |
| **Producer** | `payment-service` → `PaymentEventProducer.publishPaymentProcessed()` |
| **Consumer** | `notification-service` → `PaymentProcessedEventConsumer.consumePaymentProcessed()` (`group-id: notification-service`) |
| **Class** | `org.example.shared.dto.PaymentProcessedEvent` (`shared-lib`) |
| **Triggered by** | End of payment processing in `payment-service` (success or failure), after consuming [[02-CarSaleCreatedEvent\|CarSaleCreatedEvent]] |

## Schema (`@JsonProperty` fields)

| Java field | JSON | Type | Description |
|---|---|---|---|
| `saleId` | `sale_id` | `Long` | Sale ID |
| `transactionId` | `transaction_id` | `String` | Stripe `PaymentIntent` ID |
| `amount` | `amount` | `BigDecimal` | Charged amount |
| `currency` | `currency` | `String` | Currency |
| `status` | `status` | `String` | `PAID` or `FAILED` |
| `processedAt` | `processed_at` | `LocalDateTime` | Processing timestamp |
| `errorMessage` | `error_message` | `String` | Only present when `status = FAILED` |

## Example — successful (simulated) payment

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

## Example — failed payment (after retries are exhausted)

```json
{
  "sale_id": 11,
  "transaction_id": null,
  "amount": 15000.00,
  "currency": "usd",
  "status": "FAILED",
  "processed_at": "2026-09-03T10:20:11",
  "error_message": "<Stripe exception message>"
}
```

> [!warning] `PAID` does not confirm a real charge
> As documented in [[../03-Services/03-Payment-Service|Payment Service]], `payment-service` sets `status=PAID` in a simulated way right after creating the `PaymentIntent`, without checking its actual status in Stripe. `FAILED` is only emitted when creating the `PaymentIntent` throws and Resilience4j's retries are exhausted (3 attempts).

## Effect on `notification-service`

When consuming this event, `notification-service` **only logs a simulated** email and SMS send — nothing is actually sent. See [[../03-Services/04-Notification-Service|Notification Service]].

---

**See also:** [[01-Topics|Kafka Topics]] · [[02-CarSaleCreatedEvent|CarSaleCreatedEvent]] · [[../04-API-Reference/03-Payment-Endpoints|Payment Endpoints]]

---
tags: [kafka, en]
---

🌐 **Language:** English · [Español](../../es/05-Kafka-Events/02-CarSaleCreatedEvent.md)
🏠 [[../00-Home|Home]] › Kafka Events › CarSaleCreatedEvent

# 📤 CarSaleCreatedEvent

| | |
|---|---|
| **Topic** | `car-sale-created` |
| **Producer** | `order-service` → `CarSaleEventProducer.publishCarSaleCreated()` |
| **Consumer** | `payment-service` → `CarSaleEventConsumer.consumeCarSaleCreated()` (`group-id: payment-service`) |
| **Class** | `org.example.shared.dto.CarSaleCreatedEvent` (`shared-lib`) |
| **Triggered by** | `POST /api/sales` (see [[../04-API-Reference/02-Sales-Endpoints|Sales Endpoints]]) |

## Schema (`@JsonProperty` fields)

| Java field | JSON | Type | Description |
|---|---|---|---|
| `saleId` | `sale_id` | `Long` | ID of the created sale |
| `customerId` | `customer_id` | `Long` | Customer ID |
| `vehicleVin` | `vehicle_vin` | `String` | Vehicle VIN |
| `vehicleMake` | `vehicle_make` | `String` | Make |
| `vehicleModel` | `vehicle_model` | `String` | Model |
| `vehicleYear` | `vehicle_year` | `Integer` | Year |
| `salePrice` | `sale_price` | `BigDecimal` | ⚠️ See note below |
| `createdAt` | `created_at` | `LocalDateTime` | Event creation timestamp (not the sale's) |
| `status` | `status` | `String` | Sale status at publish time |

## Example payload

```json
{
  "sale_id": 10,
  "customer_id": 3,
  "vehicle_vin": "1HGCM82633A004352",
  "vehicle_make": "Toyota",
  "vehicle_model": "Corolla",
  "vehicle_year": 2023,
  "sale_price": 26460.00,
  "created_at": "2026-09-03T10:15:00",
  "status": "PENDING"
}
```

> [!warning] `sale_price` is actually the total amount
> `CarSaleMapper.toEvent(CarSale)` maps `salePrice ← CarSale.totalAmount`, meaning **the event's `sale_price` field carries the already-computed amount** (discount and tax included), not the raw price entered in the request. `payment-service` uses this value directly to create the Stripe `PaymentIntent`.
>
> `createdAt` is also recomputed with `LocalDateTime.now()` at mapping time — it's not copied from the `CarSale` entity's `createdAt`.

---

**See also:** [[01-Topics|Kafka Topics]] · [[03-PaymentProcessedEvent|PaymentProcessedEvent]] · [[../03-Services/02-Order-Service|Order Service]]

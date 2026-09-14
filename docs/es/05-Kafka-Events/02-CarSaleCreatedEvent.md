---
tags: [kafka, es]
---

🌐 **Idioma:** Español · [English](../../en/05-Kafka-Events/02-CarSaleCreatedEvent.md)
🏠 [[../00-Home|Inicio]] › Kafka Events › CarSaleCreatedEvent

# 📤 CarSaleCreatedEvent

| | |
|---|---|
| **Tópico** | `car-sale-created` |
| **Productor** | `order-service` → `CarSaleEventProducer.publishCarSaleCreated()` |
| **Consumidor** | `payment-service` → `CarSaleEventConsumer.consumeCarSaleCreated()` (`group-id: payment-service`) |
| **Clase** | `org.example.shared.dto.CarSaleCreatedEvent` (`shared-lib`) |
| **Disparado por** | `POST /api/sales` (ver [[../04-API-Reference/02-Sales-Endpoints|Sales Endpoints]]) |

## Esquema (campos `@JsonProperty`)

| Campo Java | JSON | Tipo | Descripción |
|---|---|---|---|
| `saleId` | `sale_id` | `Long` | ID de la venta creada |
| `customerId` | `customer_id` | `Long` | ID del cliente |
| `vehicleVin` | `vehicle_vin` | `String` | VIN del vehículo |
| `vehicleMake` | `vehicle_make` | `String` | Marca |
| `vehicleModel` | `vehicle_model` | `String` | Modelo |
| `vehicleYear` | `vehicle_year` | `Integer` | Año |
| `salePrice` | `sale_price` | `BigDecimal` | ⚠️ Ver nota abajo |
| `createdAt` | `created_at` | `LocalDateTime` | Fecha de creación del evento (no de la venta) |
| `status` | `status` | `String` | Estado de la venta al momento de publicar |

## Ejemplo de payload

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

> [!warning] `sale_price` en realidad es el monto total
> `CarSaleMapper.toEvent(CarSale)` mapea `salePrice ← CarSale.totalAmount`, es decir, **el campo `sale_price` del evento contiene el monto ya calculado con descuento e impuesto incluidos**, no el precio bruto ingresado en el request. `payment-service` usa este valor directamente para crear el `PaymentIntent` en Stripe.
>
> `createdAt` también se recalcula con `LocalDateTime.now()` en el momento del mapeo, no se copia del `createdAt` de la entidad `CarSale`.

---

**Ver también:** [[01-Topics|Tópicos de Kafka]] · [[03-PaymentProcessedEvent|PaymentProcessedEvent]] · [[../03-Services/02-Order-Service|Order Service]]

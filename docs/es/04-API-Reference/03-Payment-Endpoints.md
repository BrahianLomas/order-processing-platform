---
tags: [api, es]
---

🌐 **Idioma:** Español · [English](../../en/04-API-Reference/03-Payment-Endpoints.md)
🏠 [[../00-Home|Inicio]] › API Reference › Payment Endpoints

# 💳 Payment Endpoints

Base URL: `http://localhost:8082/api`

> [!note] Único endpoint, de solo lectura
> `payment-service` no expone ninguna forma de crear pagos vía REST. Los pagos se generan exclusivamente al consumir `CarSaleCreatedEvent` desde Kafka — ver [[../03-Services/03-Payment-Service|Payment Service]].

> [!tip] Requiere autenticación
> Requiere un header `Authorization: Bearer <token>` válido (emitido por `api-gateway`). Sin él → `401 Unauthorized`.

> [!info] Formato de respuesta
> Todas las respuestas siguen el envelope homologado `GenericResponse` — ver [[00-Response-Format|Formato de Respuesta]].

## GET `/payments/sales/{saleId}`

Obtiene el pago asociado a una venta.

**Response — `200 OK`**
```json
{
  "id": 7,
  "saleId": 10,
  "amount": 26460.00,
  "currency": "usd",
  "status": "PAID",
  "transactionId": "pi_3Nx...",
  "createdAt": "2026-09-03T10:15:03",
  "response_code": "00",
  "description": "Successful",
  "timestamp": "2026-09-12T09:15:31.594"
}
```

**Response — `404 Not Found`**
```json
{
  "response_code": "E006",
  "description": "Pago no encontrado",
  "timestamp": "2026-09-12T09:15:31.594"
}
```
> Lanzado internamente como `PaymentException("PAYMENT_NOT_FOUND", ...)`.

## Notas sobre el estado del pago

- `status = "PAID"` **no significa que Stripe confirmó el cobro** — es un valor fijado por el servicio de forma simulada tras crear el `PaymentIntent` (modo test). Ver la advertencia en [[../03-Services/03-Payment-Service|Payment Service]].
- `transactionId` corresponde al `id` del `PaymentIntent` de Stripe.
- `amount` se guarda en la unidad monetaria normal (ej. dólares), aunque Stripe internamente trabaja en centavos.

---

**Ver también:** [[00-Response-Format|Formato de Respuesta]] · [[../05-Kafka-Events/03-PaymentProcessedEvent|Evento PaymentProcessedEvent]] · [[02-Sales-Endpoints|Sales Endpoints]]

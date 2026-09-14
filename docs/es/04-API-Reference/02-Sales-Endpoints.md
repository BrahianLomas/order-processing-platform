---
tags: [api, es]
---

🌐 **Idioma:** Español · [English](../../en/04-API-Reference/02-Sales-Endpoints.md)
🏠 [[../00-Home|Inicio]] › API Reference › Sales Endpoints

# 📦 Sales Endpoints

Base URL: `http://localhost:8081/api`

> [!tip] Requiere autenticación
> Todos estos endpoints exigen un header `Authorization: Bearer <token>` válido (emitido por `api-gateway`). Sin él, o con un token inválido/expirado, la respuesta es `401 Unauthorized`. Ver [[01-Authentication|Autenticación]].

> [!info] Formato de respuesta
> Todas las respuestas siguen el envelope homologado `GenericResponse` — ver [[00-Response-Format|Formato de Respuesta]].

## POST `/sales`

Crea una nueva venta. El cliente (`Customer`) debe existir previamente (búsqueda por email).

**Request**
```json
{
  "customerEmail": "cliente@test.com",
  "vehicleVin": "1HGCM82633A004352",
  "vehicleMake": "Toyota",
  "vehicleModel": "Corolla",
  "vehicleYear": 2023,
  "vehicleColor": "Blanco",
  "vehicleMileage": 0,
  "salePrice": 25000,
  "discount": 500
}
```

**Cálculo aplicado por el servicio**
```text
tax         = (salePrice - discount) * 0.08 = (25000 - 500) * 0.08 = 1960.00
totalAmount = salePrice - discount + tax    = 25000 - 500 + 1960  = 26460.00
```

**Response — `201 Created`**
```json
{
  "id": 10,
  "customer_id": 3,
  "customer_name": "Juan Pérez",
  "vehicle_vin": "1HGCM82633A004352",
  "vehicle_make": "Toyota",
  "vehicle_model": "Corolla",
  "vehicle_year": 2023,
  "vehicle_color": "Blanco",
  "sale_price": 25000,
  "discount": 500,
  "tax": 1960.00,
  "total_amount": 26460.00,
  "status": "PENDING",
  "created_at": "2026-09-03T10:15:00",
  "response_code": "00",
  "description": "Successful",
  "timestamp": "2026-09-12T08:54:08.630"
}
```

**Efecto secundario:** publica `CarSaleCreatedEvent` al tópico `car-sale-created` → dispara [[../03-Services/03-Payment-Service|Payment Service]]. Ver [[../05-Kafka-Events/02-CarSaleCreatedEvent|CarSaleCreatedEvent]].

**Response — `400 Bad Request`** (cliente no encontrado u otro error de negocio)
```json
{
  "response_code": "E002",
  "description": "Cliente no encontrado",
  "timestamp": "2026-09-12T08:54:08.630"
}
```

## GET `/sales/{saleId}`

**Response — `200 OK`**: mismo esquema `CarSaleResponse` de arriba.

**Response — `404 Not Found`**
```json
{
  "response_code": "E006",
  "description": "Venta no encontrada",
  "timestamp": "2026-09-12T08:54:08.791"
}
```

## GET `/sales/customer/{customerId}`

> [!warning] No es un array plano
> A diferencia de una lista JSON tradicional, la respuesta envuelve el array en un objeto `CarSaleListResponse` para poder llevar `response_code`/`description`/`timestamp` a nivel superior. Ver [[00-Response-Format|Formato de Respuesta]].

**Response — `200 OK`**
```json
{
  "sales": [
    { "id": 10, "customer_id": 3, "status": "PAID", "...": "..." },
    { "id": 11, "customer_id": 3, "status": "PENDING", "...": "..." }
  ],
  "response_code": "00",
  "description": "Successful",
  "timestamp": "2026-09-12T09:15:31.488"
}
```

**Response — `400 Bad Request`** en caso de error (mismo formato que arriba, `response_code: E002`).

## DELETE `/sales/{saleId}`

Cancelación **suave**: cambia `status` a `"CANCELLED"`. No es un `DELETE` físico de la fila.

**Response — `200 OK`**
```json
{
  "id": 10,
  "status": "CANCELLED",
  "response_code": "00",
  "description": "Successful",
  "timestamp": "2026-09-12T09:20:00.000",
  "...": "..."
}
```

**Response — `400 Bad Request`** si la venta ya tiene `status = PAID`:
```json
{
  "response_code": "E002",
  "description": "No se puede cancelar venta pagada",
  "timestamp": "2026-09-12T09:20:00.000"
}
```
> Lanza internamente `BusinessException("CANNOT_CANCEL", ...)`.

---

**Ver también:** [[00-Response-Format|Formato de Respuesta]] · [[03-Payment-Endpoints|Payment Endpoints]] · [[../03-Services/02-Order-Service|Order Service]]

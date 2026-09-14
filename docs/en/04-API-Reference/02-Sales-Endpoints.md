---
tags: [api, en]
---

🌐 **Language:** English · [Español](../../es/04-API-Reference/02-Sales-Endpoints.md)
🏠 [[../00-Home|Home]] › API Reference › Sales Endpoints

# 📦 Sales Endpoints

Base URL: `http://localhost:8081/api`

> [!tip] Requires authentication
> Every endpoint here requires a valid `Authorization: Bearer <token>` header (issued by `api-gateway`). Without it, or with an invalid/expired token, the response is `401 Unauthorized`. See [[01-Authentication|Authentication]].

> [!info] Response format
> Every response follows the homologated `GenericResponse` envelope — see [[00-Response-Format|Response Format]].

## POST `/sales`

Creates a new sale. The `Customer` must already exist (looked up by email).

**Request**
```json
{
  "customerEmail": "customer@test.com",
  "vehicleVin": "1HGCM82633A004352",
  "vehicleMake": "Toyota",
  "vehicleModel": "Corolla",
  "vehicleYear": 2023,
  "vehicleColor": "White",
  "vehicleMileage": 0,
  "salePrice": 25000,
  "discount": 500
}
```

**Calculation applied by the service**
```text
tax         = (salePrice - discount) * 0.08 = (25000 - 500) * 0.08 = 1960.00
totalAmount = salePrice - discount + tax    = 25000 - 500 + 1960  = 26460.00
```

**Response — `201 Created`**
```json
{
  "id": 10,
  "customer_id": 3,
  "customer_name": "John Doe",
  "vehicle_vin": "1HGCM82633A004352",
  "vehicle_make": "Toyota",
  "vehicle_model": "Corolla",
  "vehicle_year": 2023,
  "vehicle_color": "White",
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

**Side effect:** publishes `CarSaleCreatedEvent` to the `car-sale-created` topic → triggers [[../03-Services/03-Payment-Service|Payment Service]]. See [[../05-Kafka-Events/02-CarSaleCreatedEvent|CarSaleCreatedEvent]].

**Response — `400 Bad Request`** (customer not found or another business error)
```json
{
  "response_code": "E002",
  "description": "Cliente no encontrado",
  "timestamp": "2026-09-12T08:54:08.630"
}
```

## GET `/sales/{saleId}`

**Response — `200 OK`**: same `CarSaleResponse` schema as above.

**Response — `404 Not Found`**
```json
{
  "response_code": "E006",
  "description": "Venta no encontrada",
  "timestamp": "2026-09-12T08:54:08.791"
}
```

## GET `/sales/customer/{customerId}`

> [!warning] Not a plain array
> Unlike a plain JSON array, the response wraps the array in a `CarSaleListResponse` object so it can also carry `response_code`/`description`/`timestamp` at the top level. See [[00-Response-Format|Response Format]].

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

**Response — `400 Bad Request`** on error (same shape as above, `response_code: E002`).

## DELETE `/sales/{saleId}`

**Soft** cancellation: sets `status` to `"CANCELLED"`. Not a physical row deletion.

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

**Response — `400 Bad Request`** if the sale is already `status = PAID`:
```json
{
  "response_code": "E002",
  "description": "No se puede cancelar venta pagada",
  "timestamp": "2026-09-12T09:20:00.000"
}
```
> Internally throws `BusinessException("CANNOT_CANCEL", ...)`.

---

**See also:** [[00-Response-Format|Response Format]] · [[03-Payment-Endpoints|Payment Endpoints]] · [[../03-Services/02-Order-Service|Order Service]]

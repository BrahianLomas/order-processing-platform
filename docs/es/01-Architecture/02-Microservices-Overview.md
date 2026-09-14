---
tags: [architecture, es]
---

🌐 **Idioma:** Español · [English](../../en/01-Architecture/02-Microservices-Overview.md)
🏠 [[../00-Home|Inicio]] › Arquitectura › Resumen de Microservicios

# 🧩 Resumen de Microservicios

## Tabla comparativa

| Servicio | Puerto | Base package | Base de datos | Seguridad | Kafka |
|---|---|---|---|---|---|
| [[../03-Services/01-API-Gateway\|api-gateway]] | `8080` | `org.example.gateway` | tabla `users` | ✅ Spring Security + JWT (firma) | — |
| [[../03-Services/02-Order-Service\|order-service]] | `8081` | `org.example.order_service` | tablas `customers`, `car_sales` | ✅ Spring Security + JWT (valida) | Produce `car-sale-created` |
| [[../03-Services/03-Payment-Service\|payment-service]] | `8082` | `org.example.payment` | tabla `payments` | ✅ Spring Security + JWT (valida) | Consume `car-sale-created`, produce `payment-processed` |
| [[../03-Services/04-Notification-Service\|notification-service]] | `8083` | `org.example.notification` | ninguna (datasource sin uso) | ❌ ninguna (sin endpoints que proteger) | Consume `payment-processed` |

Todos los servicios comparten:
- Prefijo de contexto `server.servlet.context-path=/api`
- Conexión a la misma base MySQL (`localhost:3307/order_platform`)
- El módulo [[../06-Technologies/01-Stack|shared-lib]] (DTOs de eventos, constantes, excepciones, `JwtUtil`)

## api-gateway

| | |
|---|---|
| Endpoints | 3 (`/auth/register`, `/auth/login`, `/auth/refresh`) |
| Entidades | `User` |
| Rol en el flujo | Punto de entrada de autenticación (no enruta tráfico) |
| Documentación | [[../03-Services/01-API-Gateway\|API Gateway]] |

## order-service

| | |
|---|---|
| Endpoints | 4 (`POST /sales`, `GET /sales/{id}`, `GET /sales/customer/{id}`, `DELETE /sales/{id}`) |
| Entidades | `Customer`, `CarSale` |
| Rol en el flujo | Origina la venta y dispara el flujo de pago vía Kafka |
| Documentación | [[../03-Services/02-Order-Service\|Order Service]] |

## payment-service

| | |
|---|---|
| Endpoints | 1 (`GET /payments/sales/{saleId}`) |
| Entidades | `Payment` |
| Rol en el flujo | Cobra vía Stripe (simulado) y notifica el resultado por Kafka |
| Documentación | [[../03-Services/03-Payment-Service\|Payment Service]] |

## notification-service

| | |
|---|---|
| Endpoints | 0 — es un consumidor Kafka puro |
| Entidades | ninguna |
| Rol en el flujo | Simula el envío de confirmaciones (solo logs) |
| Documentación | [[../03-Services/04-Notification-Service\|Notification Service]] |

---

**Ver también:** [[01-System-Design|Diseño del Sistema]] · [[03-Data-Flow|Flujo de Datos]]

---
tags: [architecture, en]
---

🌐 **Language:** English · [Español](../../es/01-Architecture/02-Microservices-Overview.md)
🏠 [[../00-Home|Home]] › Architecture › Microservices Overview

# 🧩 Microservices Overview

## Comparison table

| Service | Port | Base package | Database | Security | Kafka |
|---|---|---|---|---|---|
| [[../03-Services/01-API-Gateway\|api-gateway]] | `8080` | `org.example.gateway` | `users` table | ✅ Spring Security + JWT (signs) | — |
| [[../03-Services/02-Order-Service\|order-service]] | `8081` | `org.example.order_service` | `customers`, `car_sales` tables | ✅ Spring Security + JWT (validates) | Produces `car-sale-created` |
| [[../03-Services/03-Payment-Service\|payment-service]] | `8082` | `org.example.payment` | `payments` table | ✅ Spring Security + JWT (validates) | Consumes `car-sale-created`, produces `payment-processed` |
| [[../03-Services/04-Notification-Service\|notification-service]] | `8083` | `org.example.notification` | none (unused datasource) | ❌ none (no endpoints to protect) | Consumes `payment-processed` |

All services share:
- The `server.servlet.context-path=/api` prefix
- A connection to the same MySQL database (`localhost:3307/order_platform`)
- The [[../06-Technologies/01-Stack|shared-lib]] module (event DTOs, constants, exceptions, `JwtUtil`)

## api-gateway

| | |
|---|---|
| Endpoints | 3 (`/auth/register`, `/auth/login`, `/auth/refresh`) |
| Entities | `User` |
| Role in the flow | Authentication entry point (does not route traffic) |
| Docs | [[../03-Services/01-API-Gateway\|API Gateway]] |

## order-service

| | |
|---|---|
| Endpoints | 4 (`POST /sales`, `GET /sales/{id}`, `GET /sales/customer/{id}`, `DELETE /sales/{id}`) |
| Entities | `Customer`, `CarSale` |
| Role in the flow | Originates the sale and kicks off the payment flow via Kafka |
| Docs | [[../03-Services/02-Order-Service\|Order Service]] |

## payment-service

| | |
|---|---|
| Endpoints | 1 (`GET /payments/sales/{saleId}`) |
| Entities | `Payment` |
| Role in the flow | Charges via Stripe (simulated) and reports the outcome through Kafka |
| Docs | [[../03-Services/03-Payment-Service\|Payment Service]] |

## notification-service

| | |
|---|---|
| Endpoints | 0 — a pure Kafka consumer |
| Entities | none |
| Role in the flow | Simulates sending confirmations (log only) |
| Docs | [[../03-Services/04-Notification-Service\|Notification Service]] |

---

**See also:** [[01-System-Design|System Design]] · [[03-Data-Flow|Data Flow]]

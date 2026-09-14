---
tags: [api, en]
---

🌐 **Language:** English · [Español](../../es/04-API-Reference/01-Authentication.md)
🏠 [[../00-Home|Home]] › API Reference › Authentication

# 🔑 Authentication

Base URL: `http://localhost:8080/api`

> [!info] Response format
> Every response follows the homologated `GenericResponse` envelope — see [[00-Response-Format|Response Format]].

## POST `/auth/register`

Registers a new user. Password is stored with **BCrypt**.

**Request**
```json
{
  "email": "demo@test.com",
  "password": "secret123",
  "firstName": "Demo",
  "lastName": "User"
}
```

**Response — `201 Created`**
```json
{
  "id": 1,
  "email": "demo@test.com",
  "firstName": "Demo",
  "lastName": "User",
  "response_code": "00",
  "description": "Successful",
  "timestamp": "2026-09-12T09:15:30.381"
}
```

**Response — `400 Bad Request`** (e.g. duplicate email)
```json
{
  "response_code": "E002",
  "description": "Email ya registrado",
  "timestamp": "2026-09-12T09:15:30.381"
}
```

## POST `/auth/login`

**Request**
```json
{
  "email": "demo@test.com",
  "password": "secret123"
}
```

**Response — `200 OK`**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "email": "demo@test.com",
    "firstName": "Demo",
    "lastName": "User"
  },
  "response_code": "00",
  "description": "Successful",
  "timestamp": "2026-09-12T09:15:30.576"
}
```

**Response — `401 Unauthorized`**
```json
{
  "response_code": "E004",
  "description": "Email o contraseña inválido",
  "timestamp": "2026-09-12T09:15:30.576"
}
```

## POST `/auth/refresh`

Issues a new token from a currently valid one. No body required; it reads the `Authorization` header.

**Headers**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response — `200 OK`**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "expiresIn": 86400000,
  "response_code": "00",
  "description": "Successful",
  "timestamp": "2026-09-12T09:15:30.576"
}
```

**Response — `401 Unauthorized`**
```json
{
  "response_code": "E004",
  "description": "Token inválido o expirado",
  "timestamp": "2026-09-12T09:15:30.576"
}
```

## JWT anatomy

| Property | Value |
|---|---|
| Algorithm | `HS256` (`Algorithm.HMAC256`, `auth0/java-jwt` library) |
| `subject` claim (`sub`) | `userId` |
| `email` claim | the user's email |
| `iat` / `exp` | issued now / now + `app.jwt.expiration` (86,400,000 ms = 24h) |
| Roles/scopes | **none** — the token carries no role claim |

To call protected endpoints, send:

```
Authorization: Bearer <token>
```

> [!tip] The JWT is now validated in three services
> `api-gateway` (signs it), `order-service` and `payment-service` (validate it) all share the same `app.jwt.secret`. Any request to `/sales/**` or `/payments/**` without a valid token gets **`401 Unauthorized`** with a `GenericResponse` body (`response_code: E004`) — see [[00-Response-Format|Response Format]]. `notification-service` still doesn't validate it, but it exposes no REST endpoint. See [[../01-Architecture/04-Design-Decisions|Design Decisions]].

---

**See also:** [[00-Response-Format|Response Format]] · [[02-Sales-Endpoints|Sales Endpoints]] · [[../03-Services/01-API-Gateway|API Gateway]]

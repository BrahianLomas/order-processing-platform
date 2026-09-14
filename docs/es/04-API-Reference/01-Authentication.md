---
tags: [api, es]
---

🌐 **Idioma:** Español · [English](../../en/04-API-Reference/01-Authentication.md)
🏠 [[../00-Home|Inicio]] › API Reference › Autenticación

# 🔑 Autenticación

Base URL: `http://localhost:8080/api`

> [!info] Formato de respuesta
> Todas las respuestas siguen el envelope homologado `GenericResponse` — ver [[00-Response-Format|Formato de Respuesta]].

## POST `/auth/register`

Registra un nuevo usuario. Contraseña almacenada con **BCrypt**.

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

**Response — `400 Bad Request`** (p. ej. email duplicado)
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

Genera un nuevo token a partir de uno vigente. No requiere body; lee el header `Authorization`.

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

## Anatomía del JWT

| Propiedad | Valor |
|---|---|
| Algoritmo | `HS256` (`Algorithm.HMAC256`, librería `auth0/java-jwt`) |
| `subject` (claim `sub`) | `userId` |
| Claim `email` | email del usuario |
| `iat` / `exp` | emitido ahora / ahora + `app.jwt.expiration` (86 400 000 ms = 24h) |
| Roles/scopes | **ninguno** — el token no lleva ninguna claim de rol |

Para llamar endpoints protegidos, envía:

```
Authorization: Bearer <token>
```

> [!tip] El JWT ahora se valida en tres servicios
> `api-gateway` (lo firma), `order-service` y `payment-service` (lo validan) comparten el mismo `app.jwt.secret`. Cualquier petición a `/sales/**` o `/payments/**` sin un token válido recibe **`401 Unauthorized`** con un body `GenericResponse` (`response_code: E004`) — ver [[00-Response-Format|Formato de Respuesta]]. `notification-service` sigue sin validarlo, pero no expone ningún endpoint REST. Ver [[../01-Architecture/04-Design-Decisions|Decisiones de Diseño]].

---

**Ver también:** [[00-Response-Format|Formato de Respuesta]] · [[02-Sales-Endpoints|Sales Endpoints]] · [[../03-Services/01-API-Gateway|API Gateway]]

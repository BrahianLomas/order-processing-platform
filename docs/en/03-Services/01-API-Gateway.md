---
tags: [service, en]
---

🌐 **Language:** English · [Español](../../es/03-Services/01-API-Gateway.md)
🏠 [[../00-Home|Home]] › Services › API Gateway

# 🔐 API Gateway

| | |
|---|---|
| **Port** | `8080` |
| **Context path** | `/api` |
| **Base package** | `org.example.gateway` |
| **Entry point** | `ApiGatewayApplication` |
| **Database** | `users` table |

> [!warning] Not a routing gateway
> Despite its name, this service does **not** route requests to `order-service`, `payment-service`, or `notification-service`. It is exclusively an **authentication** microservice. See [[../01-Architecture/04-Design-Decisions|Design Decisions]].

## Package structure

```
org.example.gateway
├── config
│   ├── GlobalExceptionHandler   (@RestControllerAdvice)
│   ├── JwtConfig                 (JwtUtil bean)
│   ├── RestAuthenticationEntryPoint (homologated 401 without a token)
│   └── SecurityConfig            (filter chain)
├── controller
│   └── AuthController            (/auth)
├── dto
│   ├── LoginRequest
│   ├── RegisterRequest
│   ├── RegisterResponse          (extends GenericResponse)
│   ├── LoginResponse             (extends GenericResponse)
│   ├── RefreshResponse           (extends GenericResponse)
│   └── UserSummary
├── entity
│   └── User
├── filter
│   └── JwtAuthenticationFilter   (OncePerRequestFilter)
├── repository
│   └── UserRepository
└── service
    ├── AuthService (interface)
    └── impl.AuthServiceImpl
```

## `User` entity (`users` table)

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | PK, `IDENTITY` |
| `email` | `String` | unique, not null |
| `password` | `String` | BCrypt hash, not null |
| `firstName` | `String` | not null |
| `lastName` | `String` | not null |
| `role` | `String` | not null; always `"USER"` in current code (never `"ADMIN"`) |
| `active` | `Boolean` | default `true` |
| `createdAt` / `updatedAt` | `LocalDateTime` | via `@PrePersist`/`@PreUpdate` |

## Security — `SecurityConfig`

```java
http.csrf(disable)
    .sessionManagement(STATELESS)
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/auth/**").permitAll()
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/actuator/**").permitAll()
        .anyRequest().authenticated())
    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

- `PasswordEncoder` = `BCryptPasswordEncoder`
- `JwtAuthenticationFilter` reads the `Authorization: Bearer <token>` header, validates it via `JwtUtil.isTokenValid`, and builds `UsernamePasswordAuthenticationToken(userId, null, [])` — **the authorities list is always empty**, so there's no role differentiation at the Spring Security level.
- `RestAuthenticationEntryPoint` homologates the `401 Unauthorized` for a missing/invalid token with `GenericResponse` (`response_code: E004`) — see [[../04-API-Reference/00-Response-Format|Response Format]].

## Endpoints

Full request/response detail in [[../04-API-Reference/01-Authentication|API Reference: Authentication]].

| Method | Path | Auth | Success response |
|---|---|---|---|
| `POST` | `/api/auth/register` | public | `RegisterResponse` |
| `POST` | `/api/auth/login` | public | `LoginResponse` |
| `POST` | `/api/auth/refresh` | public (requires a valid token in the header) | `RefreshResponse` |

All three extend `GenericResponse` — see [[../04-API-Reference/00-Response-Format|Response Format]].

## Configuration (`application.properties`)

```properties
server.port=8080
server.servlet.context-path=/api
spring.datasource.url=jdbc:mysql://localhost:3307/order_platform
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=86400000
```

> [!tip] JWT secret and MySQL password are no longer hardcoded
> Both are read from environment variables, resolved from a root-level `.env` (gitignored) that `bootRun` injects automatically. See [[../01-Architecture/04-Design-Decisions|Design Decisions]] and [[../02-Setup/02-Local-Setup|Local Setup Guide]].

---

**See also:** [[../04-API-Reference/00-Response-Format|Response Format]] · [[02-Order-Service|Order Service]] · [[../06-Technologies/01-Stack|Technology Stack]]

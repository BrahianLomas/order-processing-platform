---
tags: [service, es]
---

🌐 **Idioma:** Español · [English](../../en/03-Services/01-API-Gateway.md)
🏠 [[../00-Home|Inicio]] › Servicios › API Gateway

# 🔐 API Gateway

| | |
|---|---|
| **Puerto** | `8080` |
| **Context path** | `/api` |
| **Base package** | `org.example.gateway` |
| **Entry point** | `ApiGatewayApplication` |
| **Base de datos** | tabla `users` |

> [!warning] No es un gateway de enrutamiento
> A pesar del nombre, este servicio **no** enruta peticiones a `order-service`, `payment-service` ni `notification-service`. Es exclusivamente un microservicio de **autenticación**. Ver [[../01-Architecture/04-Design-Decisions|Decisiones de Diseño]].

## Estructura de paquetes

```
org.example.gateway
├── config
│   ├── GlobalExceptionHandler   (@RestControllerAdvice)
│   ├── JwtConfig                 (bean JwtUtil)
│   ├── RestAuthenticationEntryPoint (401 homologado sin token)
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
    ├── AuthService (interfaz)
    └── impl.AuthServiceImpl
```

## Entidad `User` (tabla `users`)

| Campo | Tipo | Notas |
|---|---|---|
| `id` | `Long` | PK, `IDENTITY` |
| `email` | `String` | único, not null |
| `password` | `String` | hash BCrypt, not null |
| `firstName` | `String` | not null |
| `lastName` | `String` | not null |
| `role` | `String` | not null; siempre `"USER"` en el código actual (nunca `"ADMIN"`) |
| `active` | `Boolean` | default `true` |
| `createdAt` / `updatedAt` | `LocalDateTime` | vía `@PrePersist`/`@PreUpdate` |

## Seguridad — `SecurityConfig`

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
- `JwtAuthenticationFilter` lee el header `Authorization: Bearer <token>`, valida con `JwtUtil.isTokenValid`, y construye `UsernamePasswordAuthenticationToken(userId, null, [])` — **la lista de authorities siempre está vacía**, por lo que no hay diferenciación de roles a nivel de Spring Security.
- `RestAuthenticationEntryPoint` homologa el `401 Unauthorized` de un token ausente/inválido con `GenericResponse` (`response_code: E004`) — ver [[../04-API-Reference/00-Response-Format|Formato de Respuesta]].

## Endpoints

Ver el detalle completo de request/response en [[../04-API-Reference/01-Authentication|API Reference: Authentication]].

| Método | Ruta | Auth | Respuesta de éxito |
|---|---|---|---|
| `POST` | `/api/auth/register` | pública | `RegisterResponse` |
| `POST` | `/api/auth/login` | pública | `LoginResponse` |
| `POST` | `/api/auth/refresh` | pública (requiere token vigente en el header) | `RefreshResponse` |

Las tres extienden `GenericResponse` — ver [[../04-API-Reference/00-Response-Format|Formato de Respuesta]].

## Configuración (`application.properties`)

```properties
server.port=8080
server.servlet.context-path=/api
spring.datasource.url=jdbc:mysql://localhost:3307/order_platform
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=86400000
```

> [!tip] Secreto JWT y password de MySQL ya no están hardcodeados
> Ambos se leen de variables de entorno, resueltas desde un `.env` en la raíz del repo (gitignored) que `bootRun` inyecta automáticamente. Ver [[../01-Architecture/04-Design-Decisions|Decisiones de Diseño]] y [[../02-Setup/02-Local-Setup|Guía de Instalación Local]].

---

**Ver también:** [[../04-API-Reference/00-Response-Format|Formato de Respuesta]] · [[02-Order-Service|Order Service]] · [[../06-Technologies/01-Stack|Stack Tecnológico]]

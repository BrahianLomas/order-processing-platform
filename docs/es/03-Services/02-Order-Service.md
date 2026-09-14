---
tags: [service, es]
---

🌐 **Idioma:** Español · [English](../../en/03-Services/02-Order-Service.md)
🏠 [[../00-Home|Inicio]] › Servicios › Order Service

# 📦 Order Service

| | |
|---|---|
| **Puerto** | `8081` |
| **Context path** | `/api` |
| **Base package** | `org.example.order_service` |
| **Entry point** | `Main` |
| **Base de datos** | tablas `customers`, `car_sales` |

## Estructura de paquetes

```
org.example.order_service
├── config
│   ├── GlobalExceptionHandler
│   ├── JwtConfig                       (bean JwtUtil)
│   ├── OpenApiConfig                   (metadata Swagger + esquema bearerAuth)
│   ├── RestAuthenticationEntryPoint    (401 homologado sin token)
│   └── SecurityConfig                  (filter chain)
├── controller.CarSaleController        (/sales)
├── dto
│   ├── CreateCarSaleRequest
│   ├── CarSaleResponse                 (extends GenericResponse)
│   ├── CarSaleListResponse             (extends GenericResponse)
│   └── mapper.CarSaleMapper            (MapStruct)
├── entity
│   ├── Customer                        (tabla customers)
│   └── CarSale                         (tabla car_sales)
├── filter
│   └── JwtAuthenticationFilter         (OncePerRequestFilter)
├── kafka
│   ├── CarSaleEventProducer
│   └── KafkaConfig
├── repository
│   ├── CustomerRepository
│   └── CarSaleRepository
└── service
    ├── CarSaleService (interfaz)
    └── impl.CarSaleServiceImpl
```

## Entidades

### `Customer` (tabla `customers`)

| Campo | Tipo | Notas |
|---|---|---|
| `id` | `Long` | PK, `IDENTITY` |
| `firstName`, `lastName` | `String` | not null |
| `email` | `String` | único, not null |
| `phone` | `String` | único, not null |
| `address`, `city`, `state`, `zipCode` | `String` | not null |
| `createdAt` | `LocalDateTime` | `@PrePersist` |

### `CarSale` (tabla `car_sales`)

| Campo | Tipo | Notas |
|---|---|---|
| `id` | `Long` | PK, `IDENTITY` |
| `customer` | `Customer` | `@ManyToOne(LAZY)`, `customer_id` not null |
| `vehicleVin`, `vehicleMake`, `vehicleModel`, `vehicleColor` | `String` | |
| `vehicleYear` | `Integer` | |
| `vehicleMileage` | `Long` | |
| `salePrice`, `discount`, `tax`, `totalAmount` | `BigDecimal` | not null |
| `status` | `String` | `PENDING` (default) → `PAID` / `COMPLETED` / `CANCELLED` |
| `createdAt` / `updatedAt` | `LocalDateTime` | `@PrePersist`/`@PreUpdate` |

## Lógica de negocio — cálculo de precio

```text
tax          = (salePrice - discount) * 0.08
totalAmount  = salePrice - discount + tax
```

## Endpoints

Ver ejemplos completos en [[../04-API-Reference/02-Sales-Endpoints|API Reference: Sales]].

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/sales` | Crea una venta, calcula precio, publica `CarSaleCreatedEvent` |
| `GET` | `/api/sales/{saleId}` | Obtiene una venta por ID |
| `GET` | `/api/sales/customer/{customerId}` | Lista las ventas de un cliente |
| `DELETE` | `/api/sales/{saleId}` | Cancelación "suave" (`status=CANCELLED`); falla si ya está `PAID` |

## 🔐 Seguridad

`order-service` valida el JWT emitido por `api-gateway` (mismo `app.jwt.secret`, HS256). Todos los endpoints de `/sales/**` requieren un token válido:

```java
.authorizeHttpRequests(authorize -> authorize
    .anyRequest().authenticated()
)
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

- Sin header `Authorization: Bearer <token>`, o con un token inválido/expirado → `401 Unauthorized` con body `GenericResponse` (`response_code: E004`), generado por `RestAuthenticationEntryPoint` — nunca llega al controller. Ver [[../04-API-Reference/00-Response-Format|Formato de Respuesta]].
- Con un token válido → la petición sigue normalmente hacia `CarSaleController`
- Verificado en caliente: `POST /auth/login` en `api-gateway` → `GET /sales/{id}` en `order-service` con el token obtenido

> [!note] Mismo patrón que `api-gateway`
> `JwtConfig`, `SecurityConfig`, `JwtAuthenticationFilter` y `RestAuthenticationEntryPoint` son una copia deliberada de los de `api-gateway` (ver [[01-API-Gateway|API Gateway]]), reutilizando `JwtUtil` de `shared-lib`. No se extrajo a `shared-lib` para no acoplar ese módulo a Spring Security — ver [[../01-Architecture/04-Design-Decisions|Decisiones de Diseño]].

## 📖 Swagger UI

Público, sin autenticación: **`http://localhost:8081/api/swagger-ui/index.html`**. A diferencia de `api-gateway`, aquí sí hay botón **Authorize** (esquema `bearerAuth` definido en `OpenApiConfig`) — pega el JWT obtenido en `/auth/login` para probar `POST/GET/DELETE /sales/*` directamente desde el navegador.

## Kafka — Productor

`CarSaleEventProducer.publishCarSaleCreated(CarSaleCreatedEvent event)` publica al tópico **`car-sale-created`** usando `KafkaTemplate<String, CarSaleCreatedEvent>` (key: `StringSerializer`, value: `JsonSerializer`).

> [!note] Detalle del mapeo (`CarSaleMapper`)
> El campo `sale_price` del evento se llena con `CarSale.totalAmount`, **no** con el precio bruto (`salePrice`). Es decir, el evento transporta el monto final a cobrar. Ver [[../05-Kafka-Events/02-CarSaleCreatedEvent|CarSaleCreatedEvent]].

## Configuración (`application.properties`)

```properties
server.port=8081
server.servlet.context-path=/api
spring.datasource.url=jdbc:mysql://localhost:3307/order_platform
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=86400000
```

---

**Ver también:** [[03-Payment-Service|Payment Service]] · [[../05-Kafka-Events/02-CarSaleCreatedEvent|Evento CarSaleCreatedEvent]]

---
tags: [architecture, es]
---

🌐 **Idioma:** Español · [English](../../en/01-Architecture/04-Design-Decisions.md)
🏠 [[../00-Home|Inicio]] › Arquitectura › Decisiones de Diseño

# 🧭 Decisiones de Diseño

## Decisiones intencionales

| Decisión | Justificación |
|---|---|
| **Event-driven con Kafka** entre order → payment → notification | Desacopla servicios; el productor no espera al consumidor |
| **Un `shared-lib` común** (constantes, DTOs de eventos, `JwtUtil`, excepciones) | Evita duplicar contratos de eventos y utilidades de JWT entre módulos |
| **MapStruct** para mapear entidades ↔ DTOs/eventos | Reduce boilerplate de conversión manual |
| **Resilience4j** (`Retry` + `CircuitBreaker`) en la llamada a Stripe | Stripe es una dependencia externa; se protege el flujo ante fallos transitorios |
| **`GlobalExceptionHandler`** (`@RestControllerAdvice`) por servicio | Respuestas de error consistentes dentro de cada servicio |
| **Cancelación "suave"** de ventas (`DELETE` → `status=CANCELLED`) | Conserva el historial; no permite cancelar si ya está `PAID` |
| **Secretos vía `.env`** (gitignored) + `bootRun` los inyecta como variables de entorno | Evita commitear el secreto JWT, la clave de Stripe y el password de MySQL; ver [[../02-Setup/02-Local-Setup|Guía de Instalación Local]] |
| **JWT compartido** entre `api-gateway`, `order-service` y `payment-service` (mismo `app.jwt.secret`) | `api-gateway` firma, los otros dos validan — misma clave simétrica HS256 |
| **`GenericResponse` por herencia** (no un wrapper con `data`) para homologar respuestas | Cada respuesta concreta (`LoginResponse`, `CarSaleResponse`, `PaymentResponse`...) extiende `GenericResponse` y agrega `response_code`/`description`/`timestamp`, manteniendo sus campos de negocio "planos"; ver [[../04-API-Reference/00-Response-Format\|Formato de Respuesta]] |
| **Swagger UI público** (sin JWT para abrirlo) en los 3 servicios REST | Prioriza demo/portafolio: cualquiera puede explorar la API sin fricción; el botón "Authorize" (JWT) sigue protegiendo las llamadas reales a `order-service`/`payment-service` |

Ver también el detalle de patrones aplicados en [[../06-Technologies/02-Patterns-Used|Patrones Utilizados]].

## ✅ Resueltas

> [!tip] Estas eran las dos limitaciones marcadas como más críticas; ya están corregidas y verificadas en caliente (registro → login → llamada autenticada a `order-service`/`payment-service`).

- **🔐 Seguridad en servicios internos (resuelto).** `order-service` y `payment-service` ahora tienen Spring Security + un `JwtAuthenticationFilter` propio (mismo patrón que `api-gateway`), y exigen un JWT válido en **todos** sus endpoints (`anyRequest().authenticated()`). Una petición sin token, o con un token inválido/expirado, recibe **`401 Unauthorized`** con un body `GenericResponse` homologado (gracias a un `RestAuthenticationEntryPoint` propio en cada servicio) — antes de este cambio era un `403 Forbidden` sin body. Ver [[../03-Services/02-Order-Service|Order Service]] y [[../03-Services/03-Payment-Service|Payment Service]].
  - `notification-service` se dejó **sin cambios a propósito**: no expone ningún endpoint REST (es un consumidor Kafka puro), así que no hay nada que proteger con Spring Security allí.
- **🔑 Secretos en texto plano (resuelto).** `app.jwt.secret`, `stripe.api.key` y el password de MySQL (`spring.datasource.password`) ya no están hardcodeados: los `application.properties` referencian `${JWT_SECRET}` / `${STRIPE_API_KEY}` / `${DB_PASSWORD}`, y esos valores viven en un `.env` en la raíz del repo (agregado a `.gitignore`). Cada `build.gradle` (los 4 servicios) tiene un bloque `bootRun { doFirst { ... } }` que lee ese `.env` y lo inyecta como variables de entorno del proceso, así que `./gradlew :servicio:bootRun` sigue funcionando sin pasos manuales. `docker-compose.yml` también referencia `${DB_PASSWORD}` para `MYSQL_ROOT_PASSWORD`/`MYSQL_PASSWORD` (Docker Compose lee el mismo `.env` automáticamente). Se agregó `.env.example` (sin secretos reales) como plantilla.
- **📐 Formato de respuesta homologado (resuelto/agregado).** `GenericResponse` (`shared-lib`) reemplaza a la antigua `ApiErrorResponse` (que solo cubría errores). Ahora **toda** respuesta — éxito o error, incluyendo los 401 generados por Spring Security — sigue el mismo esquema `response_code`/`description`/`timestamp`. Ver [[../04-API-Reference/00-Response-Format|Formato de Respuesta]].
  - Esto **no** resuelve la inconsistencia de *casing* entre servicios (`order-service` sigue en `snake_case`, `payment-service` en `camelCase` para sus campos de negocio) — ver [[../07-Testing/01-Postman-Collection|Colección de Postman]]. Solo homologa el "sobre" (`response_code`/`description`/`timestamp`), no los campos internos de cada payload.
  - **Decisión confirmada explícitamente:** los items dentro de una lista (ej. `sales` en `GET /sales/customer/{id}`) NO llevan su propio `response_code`/`description`/`timestamp` — solo el wrapper (`CarSaleListResponse`) lo tiene. Se evaluó forzar `"00"` en cada item, pero se descartó por ser el mismo valor repetido sin aportar información (un item nunca falla de forma independiente en este código: si algo sale mal, se rompe toda la consulta). Ver la explicación completa en [[../04-API-Reference/00-Response-Format|Formato de Respuesta]].
- **✅ Validación de input (agregado).** `RegisterRequest`, `LoginRequest` y `CreateCarSaleRequest` ahora usan Bean Validation (`@NotBlank`, `@Email`, `@Size`, `@Positive`, etc.) vía `@Valid`; los path variables (`saleId`, `customerId`) se validan con `@Positive` vía `@Validated`. Las fallas se homologan como `response_code: E005` combinando todos los campos con error en un solo mensaje. Verificado en caliente: email inválido, password corto, VIN de largo incorrecto, año/kilometraje/precio negativos, e IDs negativos o cero — todos rechazados antes de llegar al controller. Ver [[../04-API-Reference/00-Response-Format|Formato de Respuesta]].
  - Nota técnica: en `api-gateway`, `GlobalExceptionHandler` extiende `ResponseEntityExceptionHandler` (para el manejo de excepciones de Spring MVC en general), que ya intercepta `MethodArgumentNotValidException` internamente — agregar un `@ExceptionHandler` adicional para ese mismo tipo causaba un error de arranque ("Ambiguous @ExceptionHandler"). La solución correcta es sobrescribir el método protegido `handleMethodArgumentNotValid(...)` en vez de declarar un handler nuevo.

## 🚧 Limitaciones conocidas (deuda técnica)

> [!warning] Estas observaciones provienen de una revisión directa del código fuente. Son útiles para entender qué NO hacer si este proyecto avanza hacia producción.

- **🏷️ El "API Gateway" no es un gateway.** No enruta ni proxea tráfico; es solo un servicio de autenticación. Un cliente debe conocer y llamar directamente la URL de cada microservicio.
- **💳 El pago siempre es "exitoso".** `PaymentServiceImpl` fija el estado en `PAID` tras crear el `PaymentIntent` en Stripe, sin verificar el estado real devuelto (`requires_action`, `processing`, etc.). El propio código lo documenta como simulación de modo test.
- **✉️ Notificaciones simuladas.** No existe integración real con ningún proveedor de email o SMS; `notification-service` solo escribe logs con formato de "email"/"SMS".
- **📉 Sin Dead Letter Topic ni reintentos de Kafka.** Las excepciones en los `@KafkaListener` de `payment-service` y `notification-service` solo se registran en el log; el mensaje no se reencola ni se mueve a un tópico de errores.
- **🧬 Desalineación de versiones entre módulos.** El root `build.gradle` fija Spring Boot `4.0.8-SNAPSHOT`, pero `order-service` y `payment-service` declaran `4.1.0` en sus propios `build.gradle`. De forma similar, `shared-lib` declara `auth0:java-jwt:4.6.0` y `jackson-databind:2.22.1`, mientras el root gestiona `4.4.0` y `2.17.0` respectivamente. Recomendable centralizar versiones en un único BOM/catálogo.
- **🗃️ Base de datos compartida entre servicios.** Los cuatro servicios se conectan al mismo esquema `order_platform` en lugar de tener bases de datos independientes — una práctica que contradice el principio de "database per service" típico de microservicios.
- **⚙️ Propiedad sin uso.** `stripe.api.version=2023-10-16` está definida en `payment-service` pero ningún código la lee; la versión efectiva de la API es la que trae por defecto el SDK `stripe-java:23.10.0`.
- **👤 Usuario de MySQL sin uso.** `docker-compose.yml` crea un usuario `user`/`password`, pero todos los servicios se conectan como `root`.
- **🎭 Sin control de roles (RBAC).** El campo `role` de `User` siempre se guarda como `"USER"` (nunca `"ADMIN"`), y los tres `JwtAuthenticationFilter` (gateway, order, payment) construyen la autenticación con una lista de *authorities* vacía — no hay ningún `@PreAuthorize`/`hasRole` en todo el código. La seguridad actual solo distingue "autenticado" vs "no autenticado", no roles.
- **🔁 Matiz en el flujo de reintentos de pago.** El chequeo de "¿ya existe un Payment?" en `PaymentServiceImpl.processPayment` no distingue el estado del registro existente — también coincide con el `PENDING` que `handlePaymentFailure` crea tras la primera falla. Con la lógica actual, un evento reprocesado no vuelve a intentar Stripe en ese escenario, por lo que el camino hacia `FAILED` (tras 3 fallas) requeriría ajustar ese chequeo para distinguir un pago ya resuelto de uno todavía pendiente. Confirmado con `PaymentServiceImplTest`. Ver [[../03-Services/03-Payment-Service|Payment Service]].

---

**Ver también:** [[01-System-Design|Diseño del Sistema]] · [[../06-Technologies/02-Patterns-Used|Patrones Utilizados]]

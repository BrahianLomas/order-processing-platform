---
tags: [setup, es]
---

🌐 **Idioma:** Español · [English](../../en/02-Setup/02-Local-Setup.md)
🏠 [[../00-Home|Inicio]] › Instalación › Guía de Instalación Local

# 🖥️ Guía de Instalación Local

## 1. Clonar el repositorio

```bash
git clone <url-del-repo>
cd order-processing-platform
```

## 2. Levantar la infraestructura (MySQL + Kafka + Zookeeper)

El `docker-compose.yml` en la raíz define los tres servicios de infraestructura (no incluye los microservicios Java):

```bash
docker-compose up -d
```

Esto levanta:

| Contenedor | Imagen | Puerto host |
|---|---|---|
| `order-platform-mysql` | `mysql:8.0` | `3307 → 3306` |
| `order-platform-kafka` | `confluentinc/cp-kafka:7.5.0` | `9092` |
| `order-platform-zookeeper` | `confluentinc/cp-zookeeper:7.5.0` | `2181` |

> [!info] Base de datos
> El compose crea la base `order_platform` con `root/password`. Todos los servicios se conectan como `root` — el usuario secundario `user/password` definido en el compose no se usa en ningún `application.properties`.

Verifica que MySQL esté saludable antes de continuar:

```bash
docker ps
docker logs order-platform-mysql
```

## 3. Configurar variables de entorno (`.env`)

`app.jwt.secret` (compartido por `api-gateway`, `order-service` y `payment-service`), `stripe.api.key` (`payment-service`) y `spring.datasource.password` (los 4 servicios) ya no están hardcodeados — se leen de variables de entorno. Copia la plantilla y complétala:

```bash
cp .env.example .env
```

El repositorio ya incluye un `.env` con los valores de desarrollo que se usaban antes (clave de test de Stripe incluida), así que si solo quieres correr el proyecto localmente, no necesitas cambiar nada. `.env` está en `.gitignore` — nunca se sube al repo.

> [!info] ¿Cómo llega el `.env` a la app?
> `./gradlew bootRun` no lee `.env` por sí solo. Cada `build.gradle` (`api-gateway`, `order-service`, `payment-service`) tiene un bloque `bootRun { doFirst { ... } } ` que lee el `.env` de la raíz del proyecto y lo inyecta como variables de entorno del proceso antes de arrancar. Si usas IntelliJ con una *Run Configuration* en vez de `bootRun`, tendrás que cargar esas variables ahí manualmente (Environment variables en la configuración de ejecución).

## 4. Compilar el proyecto

```bash
./gradlew build
```

Esto compila los 5 módulos: `shared-lib`, `api-gateway`, `order-service`, `payment-service`, `notification-service`.

## 5. Levantar los microservicios

Cada servicio se ejecuta de forma independiente (no hay orquestación Docker para ellos). Ver el detalle en [[03-Running-Services|Cómo levantar cada servicio]].

```bash
./gradlew :api-gateway:bootRun
./gradlew :order-service:bootRun
./gradlew :payment-service:bootRun
./gradlew :notification-service:bootRun
```

> [!tip] Kafka crea los tópicos automáticamente
> `KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"` está activado en el compose, así que no necesitas crear manualmente los tópicos `car-sale-created` y `payment-processed`.

## 6. Probar el flujo completo

```bash
# 1. Registrar un usuario
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@test.com","password":"secret123","firstName":"Demo","lastName":"User"}'

# 2. Iniciar sesión y guardar el token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@test.com","password":"secret123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 3. Crear una venta (order-service necesita un customer existente por email)
curl -X POST http://localhost:8081/api/sales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"customerEmail":"cliente@test.com","vehicleVin":"1HGCM82633A004352","vehicleMake":"Toyota","vehicleModel":"Corolla","vehicleYear":2023,"vehicleColor":"Blanco","vehicleMileage":0,"salePrice":25000,"discount":0}'

# 4. Consultar el pago generado
curl http://localhost:8082/api/payments/sales/{saleId} \
  -H "Authorization: Bearer $TOKEN"
```

> [!tip] `order-service` y `payment-service` ahora exigen el token
> Desde que se agregó Spring Security a ambos, los pasos 3 y 4 fallan con `403 Forbidden` si omites el header `Authorization`. Ver [[../01-Architecture/04-Design-Decisions|Decisiones de Diseño]].

> [!warning] No hay un customer sembrado por defecto
> `order-service` busca el cliente por email en la tabla `customers`; si no existe, la creación de la venta fallará. Deberás insertarlo manualmente en MySQL o crear un endpoint/seed adicional (no existe uno en el código actual).

> [!tip] Colección de Postman
> En vez de usar `curl`, puedes importar la [[../07-Testing/01-Postman-Collection|colección de Postman]] incluida en el repositorio — ya trae estos mismos requests encadenados (login → crear venta → consultar pago).

---

**Siguiente paso:** [[03-Running-Services|Cómo levantar cada servicio]]

---
tags: [setup, en]
---

🌐 **Language:** English · [Español](../../es/02-Setup/02-Local-Setup.md)
🏠 [[../00-Home|Home]] › Setup › Local Setup Guide

# 🖥️ Local Setup Guide

## 1. Clone the repository

```bash
git clone <repo-url>
cd order-processing-platform
```

## 2. Bring up the infrastructure (MySQL + Kafka + Zookeeper)

The root `docker-compose.yml` defines the three infrastructure services (it does not include the Java microservices):

```bash
docker-compose up -d
```

This starts:

| Container | Image | Host port |
|---|---|---|
| `order-platform-mysql` | `mysql:8.0` | `3307 → 3306` |
| `order-platform-kafka` | `confluentinc/cp-kafka:7.5.0` | `9092` |
| `order-platform-zookeeper` | `confluentinc/cp-zookeeper:7.5.0` | `2181` |

> [!info] Database
> The compose file creates the `order_platform` database with `root/password`. Every service connects as `root` — the secondary `user/password` account defined in the compose file is not used by any `application.properties`.

Verify MySQL is healthy before continuing:

```bash
docker ps
docker logs order-platform-mysql
```

## 3. Configure environment variables (`.env`)

`app.jwt.secret` (shared by `api-gateway`, `order-service` and `payment-service`), `stripe.api.key` (`payment-service`), and `spring.datasource.password` (all 4 services) are no longer hardcoded — they're read from environment variables. Copy the template and fill it in:

```bash
cp .env.example .env
```

The repository already ships a `.env` with the same development values that were previously hardcoded (Stripe test key included), so if you just want to run the project locally, there's nothing to change. `.env` is in `.gitignore` — it's never pushed to the repo.

> [!info] How does `.env` reach the app?
> `./gradlew bootRun` doesn't read `.env` on its own. Each `build.gradle` (`api-gateway`, `order-service`, `payment-service`) has a `bootRun { doFirst { ... } }` block that reads the project's root `.env` and injects it as process environment variables before startup. If you use an IntelliJ Run Configuration instead of `bootRun`, you'll need to load those variables there manually (Environment variables field in the run configuration).

## 4. Build the project

```bash
./gradlew build
```

This compiles all 5 modules: `shared-lib`, `api-gateway`, `order-service`, `payment-service`, `notification-service`.

## 5. Start the microservices

Each service runs independently (there's no Docker orchestration for them). See details in [[03-Running-Services|Running the Services]].

```bash
./gradlew :api-gateway:bootRun
./gradlew :order-service:bootRun
./gradlew :payment-service:bootRun
./gradlew :notification-service:bootRun
```

> [!tip] Kafka auto-creates topics
> `KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"` is set in the compose file, so you don't need to manually create the `car-sale-created` and `payment-processed` topics.

## 6. Test the full flow

```bash
# 1. Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@test.com","password":"secret123","firstName":"Demo","lastName":"User"}'

# 2. Log in and save the token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@test.com","password":"secret123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 3. Create a sale (order-service needs an existing customer by email)
curl -X POST http://localhost:8081/api/sales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"customerEmail":"customer@test.com","vehicleVin":"1HGCM82633A004352","vehicleMake":"Toyota","vehicleModel":"Corolla","vehicleYear":2023,"vehicleColor":"White","vehicleMileage":0,"salePrice":25000,"discount":0}'

# 4. Check the resulting payment
curl http://localhost:8082/api/payments/sales/{saleId} \
  -H "Authorization: Bearer $TOKEN"
```

> [!tip] `order-service` and `payment-service` now require the token
> Since Spring Security was added to both, steps 3 and 4 fail with `403 Forbidden` if you omit the `Authorization` header. See [[../01-Architecture/04-Design-Decisions|Design Decisions]].

> [!warning] No customer is seeded by default
> `order-service` looks up the customer by email in the `customers` table; if it doesn't exist, sale creation will fail. You'll need to insert one manually in MySQL, or add a seed/endpoint (none exists in the current code).

> [!tip] Postman collection
> Instead of using `curl`, you can import the [[../07-Testing/01-Postman-Collection|Postman collection]] included in the repository — it already chains these same requests (login → create sale → check payment).

---

**Next step:** [[03-Running-Services|Running the Services]]

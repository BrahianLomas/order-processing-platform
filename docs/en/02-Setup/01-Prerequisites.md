---
tags: [setup, en]
---

🌐 **Language:** English · [Español](../../es/02-Setup/01-Prerequisites.md)
🏠 [[../00-Home|Home]] › Setup › Prerequisites

# ⚙️ Prerequisites

## Required software

| Tool | Version | Notes |
|---|---|---|
| **JDK** | 21 | Required by every module (`sourceCompatibility = 21` in the root `build.gradle`) |
| **Docker + Docker Compose** | recent | To bring up MySQL, Kafka and Zookeeper via `docker-compose.yml` |
| **Gradle** | — | No need to install it: the project ships the wrapper (`./gradlew`, Gradle 9.5.1) |
| **Git** | — | To clone the repository |

> [!tip] Recommended IDE
> IntelliJ IDEA (the repo ships `.idea/modules` config for all 5 modules) or any IDE with support for multi-module Gradle projects and Lombok.

## Ports that must be free

| Port | Used by |
|---|---|
| `8080` | api-gateway |
| `8081` | order-service |
| `8082` | payment-service |
| `8083` | notification-service |
| `3307` | MySQL (mapped from the container, not the default `3306`) |
| `9092` | Kafka (external broker) |
| `2181` | Zookeeper |

## External accounts / keys

- **Stripe:** the repository ships a development `.env` with a test key (`sk_test_...`), so **you don't need to create a Stripe account** to run the project locally. If you want to use your own test account, change it in your `.env` (see [[02-Local-Setup|Local Setup Guide]]).
- **JWT secret:** just like the Stripe key, it's resolved from `.env` (`JWT_SECRET`), shared by `api-gateway`, `order-service` and `payment-service`.
- **MySQL password:** also comes from `.env` (`DB_PASSWORD`), used by all 4 services and by `docker-compose.yml` (`MYSQL_ROOT_PASSWORD`).

---

**Next step:** [[02-Local-Setup|Local Setup Guide]]

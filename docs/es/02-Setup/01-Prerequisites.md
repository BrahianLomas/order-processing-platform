---
tags: [setup, es]
---

🌐 **Idioma:** Español · [English](../../en/02-Setup/01-Prerequisites.md)
🏠 [[../00-Home|Inicio]] › Instalación › Prerrequisitos

# ⚙️ Prerrequisitos

## Software requerido

| Herramienta | Versión | Notas |
|---|---|---|
| **JDK** | 21 | Requerido por todos los módulos (`sourceCompatibility = 21` en el `build.gradle` raíz) |
| **Docker + Docker Compose** | reciente | Para levantar MySQL, Kafka y Zookeeper vía `docker-compose.yml` |
| **Gradle** | — | No es necesario instalarlo: el proyecto trae el *wrapper* (`./gradlew`, Gradle 9.5.1) |
| **Git** | — | Para clonar el repositorio |

> [!tip] IDE recomendado
> IntelliJ IDEA (el repo incluye configuración `.idea/modules` para los 5 módulos) o cualquier IDE con soporte para proyectos Gradle multi-módulo y Lombok.

## Puertos que deben estar libres

| Puerto | Uso |
|---|---|
| `8080` | api-gateway |
| `8081` | order-service |
| `8082` | payment-service |
| `8083` | notification-service |
| `3307` | MySQL (mapeado desde el contenedor, no el `3306` por defecto) |
| `9092` | Kafka (broker externo) |
| `2181` | Zookeeper |

## Cuentas / claves externas

- **Stripe:** el repositorio incluye un `.env` de desarrollo con una clave de prueba (`sk_test_...`), por lo que **no necesitas crear una cuenta de Stripe** para correr el proyecto localmente. Si quieres usar tu propia cuenta de test, cámbiala en tu `.env` (ver [[02-Local-Setup|Guía de Instalación Local]]).
- **JWT secret:** igual que la clave de Stripe, se resuelve desde `.env` (`JWT_SECRET`), compartido por `api-gateway`, `order-service` y `payment-service`.
- **Password de MySQL:** también viene del `.env` (`DB_PASSWORD`), usado por los 4 servicios y por `docker-compose.yml` (`MYSQL_ROOT_PASSWORD`).

---

**Siguiente paso:** [[02-Local-Setup|Guía de Instalación Local]]

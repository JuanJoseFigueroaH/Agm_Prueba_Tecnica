# Clientes API - CRUD Backend

API REST reactiva para gestión de clientes construida con **Java 21**, **Spring Boot 3.x**, **WebFlux**, **PostgreSQL** y **Redis**.

## 📋 Requisitos Previos

- **Java 21** o superior
- **Maven 3.8+**
- **Docker** y **Docker Compose**

## 🚀 Cómo Ejecutar el Proyecto

### Opción 1: Con Docker Compose (Recomendado)

Esta opción levanta automáticamente PostgreSQL, Redis y la API en contenedores.

```bash
docker compose up --build
```

Esto iniciará:
- PostgreSQL en puerto 5432
- Redis en puerto 6379
- API en puerto 8080

**Acceder a la aplicación:**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

**Detener los servicios:**
```bash
docker compose down
```

**Ver logs:**
```bash
docker compose logs -f api
```

### Opción 2: Sin Docker (Ejecución Local)

#### Paso 1: Levantar PostgreSQL y Redis con Docker

```bash
docker compose up postgres redis -d
```

#### Paso 2: Configurar variables de entorno

Crear archivo `.env` basado en `.env.example`:

```properties
DB_NAME=clientesdb
DB_USER=postgres
DB_PASS=postgres
DB_HOST=localhost
DB_PORT=5432

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASS=

SERVER_PORT=8080
```

#### Paso 3: Ejecutar la aplicación

```bash
mvn clean install
mvn spring-boot:run
```

**Acceder a la aplicación:**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

**Detener la aplicación:**
Presionar `Ctrl + C` en la terminal

**Detener PostgreSQL y Redis:**
```bash
docker compose down
```

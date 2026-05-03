# API de Gestión de Franquicias — Nequi

> RFC-001 rev 2 · Spring WebFlux + R2DBC + PostgreSQL

API reactiva para gestionar franquicias, sucursales y productos. Construida con arquitectura hexagonal, Spring Boot 3 WebFlux y PostgreSQL 16.

## Tabla de Contenidos

1. [Requisitos previos](#1-requisitos-previos)
2. [Quickstart local (docker-compose)](#2-quickstart-local-docker-compose)
3. [Despliegue en AWS (Terraform + RDS)](#3-despliegue-en-aws-terraform--rds)
4. [Variables de entorno](#4-variables-de-entorno)
5. [Endpoints disponibles](#5-endpoints-disponibles)
6. [Ejecutar tests](#6-ejecutar-tests)
7. [Arquitectura](#7-arquitectura)

---

## 1. Requisitos previos

| Herramienta | Versión mínima | Uso |
|---|---|---|
| Java | 17 | Compilar y ejecutar la aplicación |
| Maven | 3.8 | Gestión de dependencias y build |
| Docker | 24+ | Contenedores de app y base de datos |
| Docker Compose | 2.20+ | Orquestación local |
| Terraform | 1.5+ | Despliegue de infraestructura en AWS |
| Cuenta AWS | — | RDS, VPC, Security Groups (solo AWS) |

---

## 2. Quickstart local (docker-compose)

### Paso 1 — Clonar el repositorio

```bash
git clone https://github.com/JuanMoraleswizz/reto-nequi.git
cd reto-nequi
```

### Paso 2 — (Opcional) Personalizar credenciales

```bash
cp .env.example .env
# Editar .env con las credenciales deseadas (por defecto: postgres/postgres)
```

### Paso 3 — Levantar el stack

```bash
docker compose up --build
```

Esto compila la imagen de la aplicación, levanta PostgreSQL 16, aplica las migraciones Flyway automáticamente y arranca la API en el puerto `8080`.

### Paso 4 — Verificar que el servicio está en línea

```bash
curl http://localhost:8080/actuator/health
# → {"status":"UP", ...}
```

### Paso 5 — Explorar la API

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

### Comandos útiles

```bash
# Levantar en background
docker compose up -d

# Ver logs de la app
docker compose logs -f app

# Detener y eliminar volúmenes
docker compose down -v
```

---

## 3. Despliegue en AWS (Terraform + RDS)

### Paso 1 — Inicializar Terraform

```bash
cd infra/terraform
terraform init
```

### Paso 2 — Revisar el plan

```bash
terraform plan \
  -var="vpc_id=vpc-xxxxxxxx" \
  -var='subnet_ids=["subnet-aaaa","subnet-bbbb"]' \
  -var="db_username=nequi_app" \
  -var="db_password=YOUR_SECURE_PASSWORD"
```

### Paso 3 — Aplicar la infraestructura

```bash
terraform apply \
  -var="vpc_id=vpc-xxxxxxxx" \
  -var='subnet_ids=["subnet-aaaa","subnet-bbbb"]' \
  -var="db_username=nequi_app" \
  -var="db_password=YOUR_SECURE_PASSWORD"
```

### Paso 4 — Obtener las URLs de conexión

```bash
terraform output rds_r2dbc_url    # → valor para R2DBC_URL
terraform output rds_flyway_url   # → valor para FLYWAY_URL
```

### Paso 5 — Ejecutar la imagen contra RDS

```bash
docker run -p 8080:8080 \
  -e R2DBC_URL="$(terraform output -raw rds_r2dbc_url)" \
  -e FLYWAY_URL="$(terraform output -raw rds_flyway_url)" \
  -e POSTGRES_USER=nequi_app \
  -e POSTGRES_PASSWORD=YOUR_SECURE_PASSWORD \
  nequi/franchises:latest
```

### Recursos de Terraform creados

| Recurso | Tipo | Descripción |
|---|---|---|
| `franchises-rds` | `aws_db_instance` | PostgreSQL 16, `db.t3.micro`, 20GB gp2, cifrado en reposo |
| `franchises-rds-sg` | `aws_security_group` | Permite TCP:5432 desde los CIDRs configurados |
| `franchises-rds-subnet-group` | `aws_db_subnet_group` | Grupo de subnets para alta disponibilidad |

---

## 4. Variables de entorno

### Requeridas (sin valor por defecto en producción)

| Variable | Descripción | Ejemplo |
|---|---|---|
| `R2DBC_URL` | URL de conexión R2DBC para runtime | `r2dbc:postgresql://host:5432/franchises_db` |
| `FLYWAY_URL` | URL JDBC para migraciones Flyway al arrancar | `jdbc:postgresql://host:5432/franchises_db` |
| `POSTGRES_USER` | Usuario de la base de datos | `nequi_app` |
| `POSTGRES_PASSWORD` | Contraseña de la base de datos | `***` |

### Opcionales (con valores por defecto)

| Variable | Por defecto | Descripción |
|---|---|---|
| `SERVER_PORT` | `8080` | Puerto HTTP del servidor Netty |
| `SPRING_R2DBC_POOL_INITIAL_SIZE` | `5` | Conexiones mínimas en el pool R2DBC |
| `SPRING_R2DBC_POOL_MAX_SIZE` | `20` | Conexiones máximas en el pool R2DBC |

---

## 5. Endpoints disponibles

| Método | URL | Descripción |
|---|---|---|
| `POST` | `/api/v1/franchises` | Crear franquicia |
| `PATCH` | `/api/v1/franchises/{franchiseId}/name` | Renombrar franquicia |
| `GET` | `/api/v1/franchises/{franchiseId}/top-stock` | Producto con más stock por sucursal |
| `POST` | `/api/v1/franchises/{franchiseId}/branches` | Agregar sucursal a una franquicia |
| `PATCH` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/name` | Renombrar sucursal |
| `POST` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products` | Agregar producto a una sucursal |
| `DELETE` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}` | Eliminar producto |
| `PATCH` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` | Modificar stock de un producto |
| `PATCH` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name` | Renombrar producto |

La documentación interactiva completa está disponible en `/swagger-ui.html`.

### Ejemplos curl

> Los IDs son UUIDs generados por la base de datos. El flujo típico es: crear franquicia → agregar sucursal → agregar producto → operar sobre el producto.

---

#### POST `/api/v1/franchises` — Crear franquicia

```bash
curl -s -X POST http://localhost:8080/api/v1/franchises \
  -H "Content-Type: application/json" \
  -d '{"name": "Franquicia Colombia"}' | jq
```

```json
{
  "id": "a1b2c3d4-0000-0000-0000-000000000001",
  "name": "Franquicia Colombia"
}
```

---

#### PATCH `/api/v1/franchises/{franchiseId}/name` — Renombrar franquicia

```bash
FRANCHISE_ID="a1b2c3d4-0000-0000-0000-000000000001"

curl -s -X PATCH http://localhost:8080/api/v1/franchises/$FRANCHISE_ID/name \
  -H "Content-Type: application/json" \
  -d '{"name": "Franquicia Colombia Actualizada"}' | jq
```

```json
{
  "id": "a1b2c3d4-0000-0000-0000-000000000001",
  "name": "Franquicia Colombia Actualizada"
}
```

---

#### GET `/api/v1/franchises/{franchiseId}/top-stock` — Producto con más stock por sucursal

```bash
FRANCHISE_ID="a1b2c3d4-0000-0000-0000-000000000001"

curl -s http://localhost:8080/api/v1/franchises/$FRANCHISE_ID/top-stock | jq
```

```json
[
  {
    "branchId": "b1b2c3d4-0000-0000-0000-000000000002",
    "branchName": "Sucursal Bogotá",
    "productId": "c1b2c3d4-0000-0000-0000-000000000003",
    "productName": "Producto A",
    "stock": 150
  }
]
```

---

#### POST `/api/v1/franchises/{franchiseId}/branches` — Agregar sucursal

```bash
FRANCHISE_ID="a1b2c3d4-0000-0000-0000-000000000001"

curl -s -X POST http://localhost:8080/api/v1/franchises/$FRANCHISE_ID/branches \
  -H "Content-Type: application/json" \
  -d '{"name": "Sucursal Bogotá"}' | jq
```

```json
{
  "id": "b1b2c3d4-0000-0000-0000-000000000002",
  "name": "Sucursal Bogotá",
  "franchiseId": "a1b2c3d4-0000-0000-0000-000000000001"
}
```

---

#### PATCH `/api/v1/franchises/{franchiseId}/branches/{branchId}/name` — Renombrar sucursal

```bash
FRANCHISE_ID="a1b2c3d4-0000-0000-0000-000000000001"
BRANCH_ID="b1b2c3d4-0000-0000-0000-000000000002"

curl -s -X PATCH http://localhost:8080/api/v1/franchises/$FRANCHISE_ID/branches/$BRANCH_ID/name \
  -H "Content-Type: application/json" \
  -d '{"name": "Sucursal Bogotá Norte"}' | jq
```

```json
{
  "id": "b1b2c3d4-0000-0000-0000-000000000002",
  "name": "Sucursal Bogotá Norte",
  "franchiseId": "a1b2c3d4-0000-0000-0000-000000000001"
}
```

---

#### POST `/api/v1/franchises/{franchiseId}/branches/{branchId}/products` — Agregar producto

```bash
FRANCHISE_ID="a1b2c3d4-0000-0000-0000-000000000001"
BRANCH_ID="b1b2c3d4-0000-0000-0000-000000000002"

curl -s -X POST http://localhost:8080/api/v1/franchises/$FRANCHISE_ID/branches/$BRANCH_ID/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Producto A", "stock": 150}' | jq
```

```json
{
  "id": "c1b2c3d4-0000-0000-0000-000000000003",
  "name": "Producto A",
  "stock": 150,
  "branchId": "b1b2c3d4-0000-0000-0000-000000000002"
}
```

---

#### PATCH `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` — Modificar stock

```bash
FRANCHISE_ID="a1b2c3d4-0000-0000-0000-000000000001"
BRANCH_ID="b1b2c3d4-0000-0000-0000-000000000002"
PRODUCT_ID="c1b2c3d4-0000-0000-0000-000000000003"

curl -s -X PATCH \
  http://localhost:8080/api/v1/franchises/$FRANCHISE_ID/branches/$BRANCH_ID/products/$PRODUCT_ID/stock \
  -H "Content-Type: application/json" \
  -d '{"stock": 200}' | jq
```

```json
{
  "id": "c1b2c3d4-0000-0000-0000-000000000003",
  "name": "Producto A",
  "stock": 200,
  "branchId": "b1b2c3d4-0000-0000-0000-000000000002"
}
```

---

#### PATCH `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name` — Renombrar producto

```bash
FRANCHISE_ID="a1b2c3d4-0000-0000-0000-000000000001"
BRANCH_ID="b1b2c3d4-0000-0000-0000-000000000002"
PRODUCT_ID="c1b2c3d4-0000-0000-0000-000000000003"

curl -s -X PATCH \
  http://localhost:8080/api/v1/franchises/$FRANCHISE_ID/branches/$BRANCH_ID/products/$PRODUCT_ID/name \
  -H "Content-Type: application/json" \
  -d '{"name": "Producto A Premium"}' | jq
```

```json
{
  "id": "c1b2c3d4-0000-0000-0000-000000000003",
  "name": "Producto A Premium",
  "stock": 200,
  "branchId": "b1b2c3d4-0000-0000-0000-000000000002"
}
```

---

#### DELETE `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}` — Eliminar producto

```bash
FRANCHISE_ID="a1b2c3d4-0000-0000-0000-000000000001"
BRANCH_ID="b1b2c3d4-0000-0000-0000-000000000002"
PRODUCT_ID="c1b2c3d4-0000-0000-0000-000000000003"

curl -s -X DELETE \
  http://localhost:8080/api/v1/franchises/$FRANCHISE_ID/branches/$BRANCH_ID/products/$PRODUCT_ID \
  -w "\nHTTP %{http_code}\n"
```

```
HTTP 204
```

---

## 6. Ejecutar tests

Los tests de integración usan **Testcontainers** y requieren Docker en ejecución.

```bash
# Todos los tests
mvn test

# Solo tests de franquicia
mvn test -Dtest="Franchise*"

# Solo tests de sucursal
mvn test -Dtest="Branch*"

# Solo tests de producto
mvn test -Dtest="Product*"

# Build completo (compila + tests + empaqueta)
mvn clean package
```

**Resultado esperado:**

```
Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 7. Arquitectura

### Diagrama de capas

```
┌─────────────────────────────────────────────────────────┐
│                    HTTP (Netty :8080)                    │
├─────────────────────────────────────────────────────────┤
│  Router Layer      RouterFunctions (sin @Controller)    │
├─────────────────────────────────────────────────────────┤
│  Handler Layer     Extrae, valida, mapea ServerResponse │
├─────────────────────────────────────────────────────────┤
│  Service Layer     Lógica de negocio · Ports & Adapters │
├─────────────────────────────────────────────────────────┤
│  Repository Layer  R2dbcRepository + DatabaseClient     │
├─────────────────────────────────────────────────────────┤
│  PostgreSQL 16     R2DBC (runtime) · JDBC/Flyway (init) │
└─────────────────────────────────────────────────────────┘
```

### Stack tecnológico

| Capa | Tecnología |
|---|---|
| Framework | Spring Boot 3.2.5 |
| Runtime web | Spring WebFlux (Netty) |
| Persistencia | Spring Data R2DBC |
| Migraciones | Flyway 10 |
| Base de datos | PostgreSQL 16 |
| Lenguaje | Java 17 |
| Tests | JUnit 5 + Testcontainers + WebTestClient |
| Documentación | SpringDoc OpenAPI 3 (Swagger UI) |
| Infraestructura | Terraform 1.5+ → AWS RDS |

### Estructura del proyecto

```
src/
├── main/java/com/nequi/franchises/
│   ├── domain/model/               # Entidades: Franchise, Branch, Product
│   ├── application/
│   │   ├── port/in/                # Interfaces de casos de uso (puertos de entrada)
│   │   ├── port/out/               # Interfaces de repositorio (puertos de salida)
│   │   ├── service/                # Implementación de la lógica de negocio
│   │   └── exception/              # Excepciones de dominio
│   └── infrastructure/
│       ├── adapter/in/web/         # Routers, Handlers, DTOs, GlobalErrorHandler
│       ├── adapter/out/persistence/# Repositorios R2DBC
│       └── config/                 # OpenAPI config
└── main/resources/
    ├── application.yml
    └── db/migration/               # V1, V2, V3 — migraciones Flyway
infra/
└── terraform/                      # main.tf, variables.tf, outputs.tf → AWS RDS
```

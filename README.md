# API de Gestión de Franquicias — Nequi

> RFC-001 rev 2 · Spring WebFlux + R2DBC + PostgreSQL

API reactiva para gestionar franquicias, sucursales y productos. Construida con arquitectura hexagonal, Spring Boot 3 WebFlux y PostgreSQL 16.

## Tabla de Contenidos

1. [Requisitos previos](#1-requisitos-previos)
2. [Quickstart local (docker-compose)](#2-quickstart-local-docker-compose)
3. [Despliegue en GCP (Terraform + Cloud SQL)](#3-despliegue-en-gcp-terraform--cloud-sql)
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
| Terraform | 1.5+ | Despliegue de infraestructura en GCP |
| Cuenta GCP | — | Cloud SQL, VPC, Compute (solo GCP) |

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

## 3. Despliegue en GCP (Terraform + Cloud SQL)

### Prerrequisitos GCP

- Tener instalado y autenticado el [Google Cloud CLI](https://cloud.google.com/sdk/docs/install):
  ```bash
  gcloud auth application-default login
  ```
- Habilitar las APIs necesarias en el proyecto:
  ```bash
  gcloud services enable sqladmin.googleapis.com \
    servicenetworking.googleapis.com \
    compute.googleapis.com \
    --project=MY_PROJECT_ID
  ```

### Paso 1 — Inicializar Terraform

```bash
cd infra/terraform
terraform init
```

### Paso 2 — Revisar el plan

```bash
terraform plan \
  -var="gcp_project=MY_PROJECT_ID" \
  -var="vpc_network=projects/MY_PROJECT_ID/global/networks/default" \
  -var="db_username=nequi_app" \
  -var="db_password=YOUR_SECURE_PASSWORD"
```

### Paso 3 — Aplicar la infraestructura

```bash
terraform apply \
  -var="gcp_project=MY_PROJECT_ID" \
  -var="vpc_network=projects/MY_PROJECT_ID/global/networks/default" \
  -var="db_username=nequi_app" \
  -var="db_password=YOUR_SECURE_PASSWORD"
```

> El aprovisionamiento de Cloud SQL tarda aproximadamente 5-10 minutos.

### Paso 4 — Obtener las URLs de conexión

```bash
terraform output cloudsql_r2dbc_url    # → valor para R2DBC_URL
terraform output cloudsql_flyway_url   # → valor para FLYWAY_URL
```

### Paso 5 — Ejecutar la imagen contra Cloud SQL

```bash
docker run -p 8080:8080 \
  -e R2DBC_URL="$(terraform output -raw cloudsql_r2dbc_url)" \
  -e FLYWAY_URL="$(terraform output -raw cloudsql_flyway_url)" \
  -e POSTGRES_USER=nequi_app \
  -e POSTGRES_PASSWORD=YOUR_SECURE_PASSWORD \
  nequi/franchises:latest
```

### Recursos de Terraform creados

| Recurso | Tipo GCP | Descripción |
|---|---|---|
| `franchises-cloudsql` | `google_sql_database_instance` | PostgreSQL 16, `db-f1-micro`, 20GB SSD, IP privada |
| `franchises_db` | `google_sql_database` | Base de datos dentro de la instancia |
| `nequi_app` | `google_sql_user` | Usuario de aplicación |
| `franchises-allow-postgres` | `google_compute_firewall` | Permite TCP:5432 desde los CIDRs configurados |
| `franchises-cloudsql-private-ip` | `google_compute_global_address` | Rango IP reservado para peering privado con Cloud SQL |
| VPC peering | `google_service_networking_connection` | Conexión privada entre el VPC y `servicenetworking.googleapis.com` |

---

## 4. Variables de entorno

### Requeridas (sin valor por defecto en producción)

| Variable | Descripción | Ejemplo |
|---|---|---|
| `R2DBC_URL` | URL de conexión R2DBC para runtime | `r2dbc:postgresql://<cloudsql-private-ip>:5432/franchises_db` |
| `FLYWAY_URL` | URL JDBC para migraciones Flyway al arrancar | `jdbc:postgresql://<cloudsql-private-ip>:5432/franchises_db` |
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

---

## 6. Ejecutar tests

El proyecto tiene tres niveles de tests. Los tests de integración y Karate requieren **Docker en ejecución** (Testcontainers levanta PostgreSQL automáticamente).

### Tests unitarios — sin Docker, sin base de datos

Prueban la lógica de negocio de los servicios con mocks (Mockito + StepVerifier). Son los más rápidos.

```bash
./mvnw test -Dtest="FranchiseServiceTest,BranchServiceTest,ProductServiceTest"
```

| Suite | Scenarios | Descripción |
|---|---|---|
| `FranchiseServiceTest` | 11 | Crear, listar, buscar, actualizar nombre, top-stock |
| `BranchServiceTest` | 9 | Crear, listar, buscar, actualizar nombre |
| `ProductServiceTest` | 16 | Agregar, eliminar, actualizar stock/nombre, validaciones |

### Tests de integración — WebTestClient + Testcontainers

Levantan el contexto Spring completo contra PostgreSQL real vía Testcontainers.

```bash
./mvnw test -Dtest="FranchiseRouterTest,BranchRouterTest,ProductRouterTest,\
FranchiseNameUpdateRouterTest,BranchNameUpdateRouterTest,\
ProductNameUpdateRouterTest,TopStockRouterTest,GlobalErrorHandlerTest"
```

### Tests de Karate (E2E con Gherkin)

Tests end-to-end escritos en lenguaje natural (BDD) que ejercen la API HTTP completa.

```bash
./mvnw test -Dtest="KarateRunner"
```

| Feature | Scenarios | Descripción |
|---|---|---|
| `franchise.feature` | 10 | CRUD de franquicias + validaciones de negocio |
| `branch.feature` | 7 | CRUD de sucursales + validaciones |
| `product.feature` | 10 | CRUD de productos, stock, top-stock con datos reales |

### Todos los tests a la vez

```bash
./mvnw test
```

### Reporte HTML de Karate (Masterthought Cucumber Reporting)

Genera un reporte visual con el estado de cada escenario, steps detallados y estadísticas globales.

```bash
./mvnw verify -Dtest="KarateRunner"
```

Al finalizar, abrir en el browser:

```
target/cucumber-reports/cucumber-html-reports/overview-features.html
```

| Vista del reporte | Contenido |
|---|---|
| `overview-features.html` | Resumen de las 3 features con barras passed/failed |
| `overview-steps.html` | Estadísticas de todos los steps |
| `overview-failures.html` | Lista de escenarios fallidos con mensaje de error |
| `report-feature_*.html` | Detalle completo por feature con request/response HTTP |

Karate también genera su propio reporte en `target/karate-reports/karate-summary.html`.

### Resultado esperado (todos los tests)

```
Tests run: 63, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Suite | Tests |
|---|---|
| Tests unitarios (Mockito) | 36 |
| Tests de integración (WebTestClient) | 0* |
| Tests de Karate (E2E) | 27 |

> \* Los tests de integración existentes no se cuentan aquí ya que comparten el mismo contexto. Al ejecutar `./mvnw test` el total puede variar según el orden de ejecución.

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
| Tests | JUnit 5 + Mockito + Testcontainers + WebTestClient + Karate |
| Reportes | Masterthought Cucumber Reporting 5.8 |
| Documentación | SpringDoc OpenAPI 3 (Swagger UI) |
| Infraestructura | Terraform 1.5+ → GCP Cloud SQL |

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
├── main/resources/
│   ├── application.yml
│   └── db/migration/               # V1, V2, V3 — migraciones Flyway
├── test/java/com/nequi/franchises/
│   ├── application/service/        # Tests unitarios (Mockito + StepVerifier)
│   │   ├── FranchiseServiceTest
│   │   ├── BranchServiceTest
│   │   └── ProductServiceTest
│   ├── infrastructure/adapter/in/web/ # Tests de integración (WebTestClient)
│   ├── karate/
│   │   └── KarateRunner            # Runner JUnit 5 para features Karate
│   └── shared/
│       └── PostgresTestContainer   # Singleton Testcontainers (PostgreSQL 16)
└── test/resources/
    ├── karate-config.js            # Configuración global de Karate (baseUrl)
    └── karate/
        ├── franchise/
        │   ├── franchise.feature   # 10 escenarios E2E de franquicias
        │   └── helpers/
        ├── branch/
        │   ├── branch.feature      # 7 escenarios E2E de sucursales
        │   └── helpers/
        └── product/
            ├── product.feature     # 10 escenarios E2E de productos
            └── helpers/
infra/
└── terraform/                      # main.tf, variables.tf, outputs.tf → GCP Cloud SQL
```

# RFC-001 · rev 2
# API de Gestión de Franquicias — Nequi

| Campo     | Detalle        |
|-----------|----------------|
| **Estado**  | Propuesta      |
| **Autor**   | Juan David Morales  |



---

## Tabla de Contenidos

1. [Objetivo](#01--objetivo)
2. [Contexto](#02--contexto)
3. [Solución Propuesta](#03--solución-propuesta)
4. [Diseño de Arquitectura](#04--diseño-de-arquitectura)
5. [Diseño de Base de Datos](#05--diseño-de-base-de-datos)
6. [Documentación de API](#06--documentación-de-api)
7. [Consideraciones](#07--consideraciones)
8. [Plan de Entrega](#08--plan-de-entrega)

---

## 01 · Objetivo

Diseñar e implementar una **API REST reactiva** para gestionar el catálogo de franquicias de Nequi, sus sucursales y los productos ofertados por sucursal, con persistencia relacional en **PostgreSQL** y un stack completamente no bloqueante usando **Spring WebFlux + R2DBC**.

La solución cubre todos los criterios de aceptación obligatorios y maximiza los puntos extra mediante programación reactiva, persistencia en AWS RDS, empaquetado con Docker e infraestructura como código con Terraform.

### Criterios cubiertos

| Criterios obligatorios       | Puntos extra              |
|------------------------------|---------------------------|
| **7 de 7** cubiertos         | **6 de 6** cubiertos      |

**Obligatorios:**
- Endpoint para agregar nueva franquicia
- Endpoint para agregar sucursal a franquicia
- Endpoint para agregar producto a sucursal
- Endpoint para eliminar producto de sucursal
- Endpoint para modificar stock de producto
- Endpoint de producto con más stock por sucursal (por franquicia)
- Programación reactiva con Spring WebFlux + R2DBC

**Puntos extra:**
- Actualización de nombre de franquicia, sucursal y producto
- Docker + docker-compose para despliegue local
- Terraform para provisionar AWS RDS PostgreSQL
- Despliegue en nube (RDS en AWS)

---

## 02 · Contexto

Nequi necesita administrar su red de franquicias comerciales. Se eligió **PostgreSQL** sobre MongoDB por las siguientes razones fundamentales:

| Razón | Detalle |
|---|---|
| **Integridad referencial** | Foreign keys garantizan que no existan productos huérfanos si se elimina una sucursal. |
| **Consultas relacionales** | La consulta de max stock por sucursal es SQL idiomático con `DISTINCT ON`. Más legible y optimizable. |
| **Transacciones ACID** | Operaciones multi-tabla son transaccionales nativamente, sin lógica de compensación extra. |

### Modelo de dominio

El dominio tiene tres entidades con relaciones **1:N en cadena**:

| Entidad | Campos | Relación | Entidad hija |
|---|---|---|---|
| **Franquicia** | nombre + sucursales[] | 1:N | **Sucursal** |
| **Sucursal** | nombre + productos[] | 1:N | **Producto** |
| **Producto** | nombre + stock | — | — |

---

## 03 · Solución Propuesta

Stack completamente reactivo usando **R2DBC** (Reactive Relational Database Connectivity), el driver no bloqueante oficial para PostgreSQL en el ecosistema Spring. Esto mantiene todos los puntos extra de programación reactiva sin sacrificar SQL.

### Stack tecnológico

| Componente      | Tecnología                        | Justificación |
|-----------------|-----------------------------------|---------------|
| **Runtime**     | Java 17+                          | LTS, soporte virtual threads, records |
| **Framework**   | Spring Boot 3.x + WebFlux         | API reactiva no bloqueante, Netty embedded |
| **DB Driver**   | R2DBC PostgreSQL                  | Driver reactivo oficial, Mono/Flux nativo |
| **Base de datos** | PostgreSQL 16                   | Relacional, ACID, DISTINCT ON, índices avanzados |
| **Nube**        | AWS RDS PostgreSQL                | db.t3.micro, free tier, Multi-AZ opcional |
| **Migraciones** | Flyway                            | Esquema versionado, ejecuta en startup automáticamente |
| **Contenedores** | Docker + Compose                 | Imagen multi-stage, postgres:16-alpine local |
| **IaC**         | Terraform                         | Provisiona RDS, security group, subnet group |

### Arquitectura de despliegue

> Para desarrollo local, `docker-compose` levanta la app + `postgres:16-alpine`. Flyway ejecuta las migraciones automáticamente al iniciar la aplicación.
> En producción se apunta al endpoint de RDS vía variable de entorno `R2DBC_URL`.

---

## 04 · Diseño de Arquitectura

La aplicación sigue una **arquitectura hexagonal (ports & adapters)** con capas bien definidas. Al usar Spring WebFlux el servidor embebido es **Netty** (no Tomcat), permitiendo manejar concurrencia sin hilos bloqueados de extremo a extremo.

### Capas

| Capa | Responsabilidad |
|---|---|
| **Router Layer** | `RouterFunctions` definen las rutas HTTP. Sin `@Controller`. Mapean `ServerRequest` a `HandlerFunctions`. |
| **Handler Layer** | Extrae y valida el cuerpo del request. Llama al servicio y devuelve `ServerResponse`. |
| **Service Layer** | Lógica de negocio pura. Orquesta repositorios. `@Transactional` reactivo con R2DBC. |
| **Repository Layer** | `R2dbcRepository` para CRUD estándar. `DatabaseClient` para la query `DISTINCT ON` de max-stock. |
| **Domain Model** | POJOs con `@Table`/`@Column` de Spring Data R2DBC. Sin anotaciones JPA/Hibernate. |
| **Migrations (Flyway)** | Scripts SQL versionados en `resources/db/migration`. `V1__create_franchises`, `V2__branches`, `V3__products`. |

> **IMPORTANTE:** Spring Data R2DBC no soporta lazy loading ni relaciones automáticas como JPA. Las relaciones entre tablas se manejan manualmente en el Service Layer, cargando entidades relacionadas con `flatMap()` reactivo.

---

## 05 · Diseño de Base de Datos

Modelo relacional normalizado en **tercera forma normal (3FN)**. Tres tablas con relaciones 1:N en cascada. Los IDs son UUID generados por la aplicación para evitar dependencia del autoincrement de la base de datos.

### Tabla: `franchises`

| Columna      | Tipo          | Restricciones        | Descripción              |
|--------------|---------------|----------------------|--------------------------|
| `id`         | UUID          | PK, NOT NULL         | Identificador único      |
| `name`       | VARCHAR(255)  | NOT NULL, UNIQUE     | Nombre de la franquicia  |
| `created_at` | TIMESTAMPTZ   | DEFAULT now()        | Fecha de creación        |

### Tabla: `branches`

| Columna        | Tipo         | Restricciones                          | Descripción              |
|----------------|--------------|----------------------------------------|--------------------------|
| `id`           | UUID         | PK, NOT NULL                           | Identificador único      |
| `franchise_id` | UUID         | FK -> franchises(id) ON DELETE CASCADE | Franquicia propietaria   |
| `name`         | VARCHAR(255) | NOT NULL                               | Nombre de la sucursal    |
| `created_at`   | TIMESTAMPTZ  | DEFAULT now()                          | Fecha de creación        |

### Tabla: `products`

| Columna      | Tipo         | Restricciones                        | Descripción              |
|--------------|--------------|--------------------------------------|--------------------------|
| `id`         | UUID         | PK, NOT NULL                         | Identificador único      |
| `branch_id`  | UUID         | FK -> branches(id) ON DELETE CASCADE | Sucursal propietaria     |
| `name`       | VARCHAR(255) | NOT NULL                             | Nombre del producto      |
| `stock`      | INTEGER      | NOT NULL, CHECK (stock >= 0)         | Cantidad en inventario   |
| `created_at` | TIMESTAMPTZ  | DEFAULT now()                        | Fecha de creación        |

### Query: producto con más stock por sucursal

Se usa `DISTINCT ON` de PostgreSQL, que es más elegante y performante que `GROUP BY + subquery` para este caso:

```sql
-- Devuelve 1 producto por sucursal con el mayor stock, para una franquicia dada
SELECT DISTINCT ON (b.id)
  b.id          AS branch_id,
  b.name        AS branch_name,
  p.id          AS product_id,
  p.name        AS product_name,
  p.stock
FROM branches b
JOIN products p ON p.branch_id = b.id
WHERE b.franchise_id = :franchiseId
ORDER BY b.id, p.stock DESC;
```

### Índices recomendados

```sql
-- Acelera búsquedas de sucursales por franquicia
CREATE INDEX idx_branches_franchise_id ON branches(franchise_id);

-- Acelera búsquedas de productos por sucursal y la query de max stock
CREATE INDEX idx_products_branch_id ON products(branch_id);
CREATE INDEX idx_products_branch_stock ON products(branch_id, stock DESC);
```

---

## 06 · Documentación de API

**Base URL:** `/api/v1`  
Todos los endpoints consumen y producen `application/json`.

### Franquicias

| Método  | Endpoint                                  | Descripción                             |
|---------|-------------------------------------------|-----------------------------------------|
| `POST`  | `/franchises`                             | Crear nueva franquicia                  |
| `PATCH` | `/franchises/{franchiseId}/name`          | Actualizar nombre de franquicia         |
| `GET`   | `/franchises/{franchiseId}/top-stock`     | Producto con más stock por sucursal     |

### Sucursales

| Método  | Endpoint                                                          | Descripción                      |
|---------|-------------------------------------------------------------------|----------------------------------|
| `POST`  | `/franchises/{franchiseId}/branches`                              | Agregar sucursal a franquicia    |
| `PATCH` | `/franchises/{franchiseId}/branches/{branchId}/name`              | Actualizar nombre de sucursal    |

### Productos

| Método   | Endpoint                                                                  | Descripción               |
|----------|---------------------------------------------------------------------------|---------------------------|
| `POST`   | `/franchises/{fId}/branches/{bId}/products`                               | Agregar producto a sucursal |
| `DELETE` | `/franchises/{fId}/branches/{bId}/products/{pId}`                         | Eliminar producto         |
| `PATCH`  | `/franchises/{fId}/branches/{bId}/products/{pId}/stock`                   | Modificar stock           |
| `PATCH`  | `/franchises/{fId}/branches/{bId}/products/{pId}/name`                    | Actualizar nombre         |

### Ejemplos de Request / Response

#### `POST /franchises` — Crear franquicia

```json
// Request Body
{ "name": "Franquicia Norte" }

// Response 201 Created
{ "id": "550e8400-e29b-41d4-a716-446655440000", "name": "Franquicia Norte" }

// Errores
// 409 Conflict    — nombre ya existe
// 400 Bad Request — nombre nulo o vacío
```

#### `GET /franchises/{franchiseId}/top-stock` — Max stock por sucursal

```json
// Response 200 OK
[
  {
    "branchId": "abc-123",
    "branchName": "Sucursal Centro",
    "productId": "xyz-456",
    "productName": "Producto A",
    "stock": 150
  },
  {
    "branchId": "def-789",
    "branchName": "Sucursal Norte",
    "productId": "uvw-012",
    "productName": "Producto B",
    "stock": 320
  }
]

// Nota: sucursales sin productos no aparecen en el resultado
```

---

## 07 · Consideraciones

| Tema | Detalle |
|---|---|
| **R2DBC vs JPA** | R2DBC no tiene lazy loading. Las relaciones se cargan explícitamente en el servicio con `flatMap()`. Mayor verbosidad pero control total del pipeline reactivo. |
| **Transacciones reactivas** | `@Transactional` funciona con R2DBC usando `TransactionalOperator`. Operaciones multi-tabla son atómicas. |
| **Migraciones Flyway** | Scripts SQL versionados: `V1__create_franchises.sql`, `V2__create_branches.sql`, `V3__create_products.sql`. Ejecutan en startup. |
| **Manejo de errores** | `GlobalErrorHandler` retorna `ProblemDetail` (RFC 9457). 404 para not found, 400 para validaciones, 409 para nombres duplicados. |
| **Docker local** | `docker-compose` levanta la app + `postgres:16-alpine`. Variables: `R2DBC_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`. Health check incluido. |
| **Terraform (AWS RDS)** | Provisiona RDS PostgreSQL 16 (`db.t3.micro`), security group, subnet group y output del endpoint. Compatible con free tier de AWS. |

> La instancia `db.t3.micro` de RDS cubre el free tier de AWS (750 horas/mes durante 12 meses). Para producción real se recomienda `db.t3.small` con Multi-AZ y backups automáticos habilitados.

---

## 08 · Plan de Entrega

| Fase   | Descripción               | Entregables clave |
|--------|---------------------------|-------------------|
| **Fase 1** | Scaffolding y esquema | Spring Boot 3 + WebFlux + R2DBC. Scripts Flyway (3 tablas + índices). Terraform para RDS. `docker-compose` con postgres local. |
| **Fase 2** | Endpoints CRUD | 7 endpoints obligatorios con `RouterFunctions` + Handlers + Services + R2DBC Repositories. Tests con `WebTestClient` + Testcontainers. |
| **Fase 3** | Puntos extra y query avanzada | Endpoints de actualización de nombre. Query `DISTINCT ON` para max-stock con `DatabaseClient`. `GlobalErrorHandler` con `ProblemDetail`. |
| **Fase 4** | Empaquetado y README | Dockerfile multi-stage (Maven builder + JRE 17 slim). README con instrucciones de despliegue local y en AWS. Push a GitHub público con git flow. |

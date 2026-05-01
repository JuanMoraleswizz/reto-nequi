# SPEC-001 · Scaffolding y Esquema Base

| Campo       | Detalle                          |
|-------------|----------------------------------|
| **Fase**    | 1 — Scaffolding y Esquema        |
| **Rama**    | `feature/spec-001/scaffolding`   |
| **RFC ref** | RFC-001 rev 2 · Sección 03, 04, 05 |
| **Estado**  | Pendiente de implementación      |

---

## Tabla de Contenidos

1. [Objetivo](#1-objetivo)
2. [Árbol de directorios](#2-árbol-de-directorios)
3. [pom.xml — Dependencias Maven](#3-pomxml--dependencias-maven)
4. [application.yml — Configuración](#4-applicationyml--configuración)
5. [Migraciones Flyway](#5-migraciones-flyway)
6. [docker-compose.yml](#6-docker-composeyml)
7. [Terraform — AWS RDS](#7-terraform--aws-rds)
8. [Criterios de aceptación](#8-criterios-de-aceptación)

---

## 1. Objetivo

Establecer el **esqueleto completo del proyecto** antes de escribir cualquier endpoint. Al terminar esta fase se debe poder:

- Compilar el proyecto con `mvn clean package -DskipTests`
- Levantar `docker-compose up` y que la app arranque con Flyway aplicando las 3 migraciones sin errores
- Conectar la app a PostgreSQL local a través de R2DBC
- Tener el módulo Terraform listo para provisionar AWS RDS con `terraform apply`

---

## 2. Árbol de directorios

```
proyecto-nequi/
├── docs/
│   └── specs/
│       ├── SPEC_phase1_scaffolding.md      ← este archivo
│       ├── SPEC_phase2_crud_endpoints.md
│       ├── SPEC_phase3_extra_features.md
│       └── SPEC_phase4_packaging.md
├── infra/
│   └── terraform/
│       ├── main.tf
│       ├── variables.tf
│       └── outputs.tf
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/nequi/franchises/
│   │   │       └── FranchisesApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   │           ├── V1__create_franchises.sql
│   │           ├── V2__create_branches.sql
│   │           └── V3__create_products.sql
│   └── test/
│       └── java/
│           └── com/nequi/franchises/
│               └── FranchisesApplicationTests.java
├── docker-compose.yml
└── pom.xml
```

---

## 3. pom.xml — Dependencias Maven

**Coordenadas del proyecto:**

```xml
<groupId>com.nequi</groupId>
<artifactId>franchises</artifactId>
<version>0.0.1-SNAPSHOT</version>
<packaging>jar</packaging>
<name>franchises</name>
<description>API de Gestión de Franquicias — Nequi</description>
```

**Parent:**

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.2.x</version>
</parent>
```

**Propiedades:**

```xml
<properties>
  <java.version>17</java.version>
  <testcontainers.version>1.19.x</testcontainers.version>
</properties>
```

**Dependencias requeridas:**

| Dependencia | GroupId | ArtifactId | Scope |
|---|---|---|---|
| Spring WebFlux | `org.springframework.boot` | `spring-boot-starter-webflux` | compile |
| Spring Data R2DBC | `org.springframework.boot` | `spring-boot-starter-data-r2dbc` | compile |
| R2DBC PostgreSQL driver | `org.postgresql` | `r2dbc-postgresql` | runtime |
| PostgreSQL JDBC (Flyway) | `org.postgresql` | `postgresql` | runtime |
| Flyway Core | `org.flywaydb` | `flyway-core` | compile |
| Flyway PostgreSQL | `org.flywaydb` | `flyway-database-postgresql` | compile |
| Validation | `org.springframework.boot` | `spring-boot-starter-validation` | compile |
| Lombok | `org.projectlombok` | `lombok` | provided |
| Spring Boot Test | `org.springframework.boot` | `spring-boot-starter-test` | test |
| Reactor Test | `io.projectreactor` | `reactor-test` | test |
| Testcontainers BOM | `org.testcontainers` | `testcontainers-bom` | test (import) |
| Testcontainers JUnit 5 | `org.testcontainers` | `junit-jupiter` | test |
| Testcontainers PostgreSQL | `org.testcontainers` | `postgresql` | test |

**Plugin:**

```xml
<plugin>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-maven-plugin</artifactId>
  <configuration>
    <excludes>
      <exclude>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
      </exclude>
    </excludes>
  </configuration>
</plugin>
```

---

## 4. application.yml — Configuración

**Archivo:** `src/main/resources/application.yml`

```yaml
server:
  port: 8080

spring:
  application:
    name: franchises

  r2dbc:
    url: ${R2DBC_URL:r2dbc:postgresql://localhost:5432/franchises_db}
    username: ${POSTGRES_USER:postgres}
    password: ${POSTGRES_PASSWORD:postgres}
    pool:
      initial-size: 5
      max-size: 20
      max-idle-time: 30m

  flyway:
    url: ${FLYWAY_URL:jdbc:postgresql://localhost:5432/franchises_db}
    user: ${POSTGRES_USER:postgres}
    password: ${POSTGRES_PASSWORD:postgres}
    locations: classpath:db/migration
    baseline-on-migrate: true

logging:
  level:
    com.nequi.franchises: DEBUG
    org.springframework.r2dbc: DEBUG
```

**Variables de entorno requeridas en producción:**

| Variable | Descripción | Ejemplo |
|---|---|---|
| `R2DBC_URL` | URL de conexión R2DBC | `r2dbc:postgresql://host:5432/franchises_db` |
| `FLYWAY_URL` | URL de conexión JDBC para Flyway | `jdbc:postgresql://host:5432/franchises_db` |
| `POSTGRES_USER` | Usuario de la base de datos | `nequi_app` |
| `POSTGRES_PASSWORD` | Contraseña de la base de datos | `*****` |

> **Nota:** Flyway requiere un driver JDBC (bloqueante) para ejecutar migraciones al inicio. R2DBC se usa exclusivamente en runtime para las queries de la aplicación. Ambas URLs apuntan al mismo servidor PostgreSQL.

---

## 5. Migraciones Flyway

### V1__create_franchises.sql

**Archivo:** `src/main/resources/db/migration/V1__create_franchises.sql`

```sql
CREATE TABLE IF NOT EXISTS franchises (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
```

### V2__create_branches.sql

**Archivo:** `src/main/resources/db/migration/V2__create_branches.sql`

```sql
CREATE TABLE IF NOT EXISTS branches (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    franchise_id UUID         NOT NULL REFERENCES franchises(id) ON DELETE CASCADE,
    name         VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_branches_franchise_id ON branches(franchise_id);
```

### V3__create_products.sql

**Archivo:** `src/main/resources/db/migration/V3__create_products.sql`

```sql
CREATE TABLE IF NOT EXISTS products (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id  UUID         NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    name       VARCHAR(255) NOT NULL,
    stock      INTEGER      NOT NULL CHECK (stock >= 0),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_products_branch_id        ON products(branch_id);
CREATE INDEX IF NOT EXISTS idx_products_branch_stock     ON products(branch_id, stock DESC);
```

---

## 6. docker-compose.yml

**Archivo:** `docker-compose.yml` (raíz del proyecto)

```yaml
version: '3.9'

services:

  postgres:
    image: postgres:16-alpine
    container_name: franchises_postgres
    environment:
      POSTGRES_DB:       franchises_db
      POSTGRES_USER:     postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d franchises_db"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build: .
    container_name: franchises_app
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      R2DBC_URL:         r2dbc:postgresql://postgres:5432/franchises_db
      FLYWAY_URL:        jdbc:postgresql://postgres:5432/franchises_db
      POSTGRES_USER:     postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "8080:8080"
    healthcheck:
      test: ["CMD-SHELL", "wget -qO- http://localhost:8080/actuator/health || exit 1"]
      interval: 15s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
```

> **Nota:** El servicio `app` depende de `postgres` con `condition: service_healthy`, garantizando que Flyway no intente conectarse antes de que PostgreSQL esté listo.

---

## 7. Terraform — AWS RDS

### Estructura de archivos

```
infra/terraform/
├── main.tf
├── variables.tf
└── outputs.tf
```

### variables.tf

```hcl
variable "aws_region" {
  description = "AWS region donde se despliega RDS"
  type        = string
  default     = "us-east-1"
}

variable "db_name" {
  description = "Nombre de la base de datos"
  type        = string
  default     = "franchises_db"
}

variable "db_username" {
  description = "Usuario maestro de RDS"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Contraseña maestra de RDS"
  type        = string
  sensitive   = true
}

variable "vpc_id" {
  description = "ID del VPC donde se creará el RDS"
  type        = string
}

variable "subnet_ids" {
  description = "Lista de subnet IDs para el subnet group"
  type        = list(string)
}

variable "allowed_cidr_blocks" {
  description = "CIDRs con acceso al puerto 5432"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}
```

### main.tf

```hcl
terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# ── Security Group ──────────────────────────────────────────────────────────

resource "aws_security_group" "rds_sg" {
  name        = "franchises-rds-sg"
  description = "Allow PostgreSQL access to Franchises RDS"
  vpc_id      = var.vpc_id

  ingress {
    description = "PostgreSQL"
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_blocks
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name    = "franchises-rds-sg"
    Project = "nequi-franchises"
  }
}

# ── Subnet Group ────────────────────────────────────────────────────────────

resource "aws_db_subnet_group" "rds_subnet_group" {
  name       = "franchises-rds-subnet-group"
  subnet_ids = var.subnet_ids

  tags = {
    Name    = "franchises-rds-subnet-group"
    Project = "nequi-franchises"
  }
}

# ── RDS Instance ─────────────────────────────────────────────────────────────

resource "aws_db_instance" "franchises_rds" {
  identifier             = "franchises-rds"
  engine                 = "postgres"
  engine_version         = "16"
  instance_class         = "db.t3.micro"
  allocated_storage      = 20
  max_allocated_storage  = 100
  storage_type           = "gp2"
  storage_encrypted      = true

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.rds_subnet_group.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]

  publicly_accessible    = false
  multi_az               = false
  deletion_protection    = false
  skip_final_snapshot    = true

  backup_retention_period = 7
  backup_window           = "03:00-04:00"
  maintenance_window      = "Mon:04:00-Mon:05:00"

  tags = {
    Name        = "franchises-rds"
    Project     = "nequi-franchises"
    Environment = "production"
  }
}
```

### outputs.tf

```hcl
output "rds_endpoint" {
  description = "Endpoint de conexión al RDS (host:port)"
  value       = aws_db_instance.franchises_rds.endpoint
  sensitive   = false
}

output "rds_r2dbc_url" {
  description = "URL R2DBC lista para usar en la variable de entorno R2DBC_URL"
  value       = "r2dbc:postgresql://${aws_db_instance.franchises_rds.endpoint}/${var.db_name}"
  sensitive   = false
}

output "rds_flyway_url" {
  description = "URL JDBC lista para usar en la variable de entorno FLYWAY_URL"
  value       = "jdbc:postgresql://${aws_db_instance.franchises_rds.endpoint}/${var.db_name}"
  sensitive   = false
}
```

**Comandos de uso:**

```bash
cd infra/terraform
terraform init
terraform plan -var="db_username=nequi_app" -var="db_password=SECRET" \
               -var="vpc_id=vpc-xxxxxxxx" -var="subnet_ids=[\"subnet-aaa\",\"subnet-bbb\"]"
terraform apply
```

---

## 8. Criterios de aceptación

| # | Criterio | Verificación |
|---|---|---|
| AC-01 | El proyecto compila sin errores | `mvn clean package -DskipTests` retorna `BUILD SUCCESS` |
| AC-02 | La app levanta y Flyway aplica las 3 migraciones | `docker-compose up` → logs muestran `Successfully applied 3 migrations` |
| AC-03 | Las 3 tablas existen con sus columnas y constraints | `\d franchises`, `\d branches`, `\d products` en psql |
| AC-04 | Los 3 índices fueron creados | `\di` en psql muestra `idx_branches_franchise_id`, `idx_products_branch_id`, `idx_products_branch_stock` |
| AC-05 | La app responde en el puerto 8080 | `curl http://localhost:8080/actuator/health` retorna `{"status":"UP"}` |
| AC-06 | docker-compose health check pasa | `docker ps` muestra ambos servicios en estado `healthy` |
| AC-07 | Terraform valida sin errores | `terraform validate` retorna `Success! The configuration is valid.` |
| AC-08 | R2DBC se conecta reactivamente | El log de startup no muestra errores de conexión R2DBC |

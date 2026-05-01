# SPEC-004 · Empaquetado y README

| Campo       | Detalle                              |
|-------------|--------------------------------------|
| **Fase**    | 4 — Empaquetado y README             |
| **Rama**    | `feature/spec-004/packaging`         |
| **RFC ref** | RFC-001 rev 2 · Sección 03, 07, 08   |
| **Estado**  | Pendiente de implementación          |
| **Depende** | SPEC-003 (API funcionalmente completa) |

---

## Tabla de Contenidos

1. [Objetivo](#1-objetivo)
2. [Dockerfile multi-stage](#2-dockerfile-multi-stage)
3. [Variables de entorno de producción](#3-variables-de-entorno-de-producción)
4. [README.md — estructura y secciones](#4-readmemd--estructura-y-secciones)
5. [Git flow y convención de ramas](#5-git-flow-y-convención-de-ramas)
6. [Checklist de entrega final](#6-checklist-de-entrega-final)
7. [Criterios de aceptación](#7-criterios-de-aceptación)

---

## 1. Objetivo

Preparar el proyecto para **entrega y despliegue en producción** mediante:

- Un `Dockerfile` multi-stage optimizado que produce una imagen lista para correr en cualquier entorno
- Un `README.md` completo con instrucciones de quickstart local y despliegue en AWS
- Documentación de todas las variables de entorno requeridas
- Un checklist de git flow para garantizar la calidad del historial de commits antes del push final

Al terminar esta fase el proyecto está listo para ser subido a GitHub público y desplegado en AWS RDS.

---

## 2. Dockerfile multi-stage

**Archivo:** `Dockerfile` (raíz del proyecto)

```dockerfile
# ─── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Copiar solo el pom.xml primero para aprovechar la caché de capas de Docker
# (las dependencias no se re-descargan si solo cambia el código fuente)
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress 2>/dev/null || true

# Instalar Maven (Alpine no lo incluye en eclipse-temurin)
RUN apk add --no-cache maven

COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# Copiar el código fuente y construir el jar
COPY src ./src
RUN mvn clean package -DskipTests -B --no-transfer-progress

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

# Usuario no-root para seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Copiar solo el jar desde el stage de build
COPY --from=builder /build/target/franchises-*.jar app.jar

# Puerto expuesto (documentativo, no publica el puerto automáticamente)
EXPOSE 8080

# Health check para Docker y orquestadores
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Opciones JVM para contenedores:
# -XX:+UseContainerSupport      → detecta límites de CPU/memoria del contenedor
# -XX:MaxRAMPercentage=75.0     → usa máximo el 75% de la RAM asignada al contenedor
# -Djava.security.egd=...       → acelera el inicio en entornos sin /dev/random robusto
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
```

### Optimizaciones del Dockerfile

| Optimización | Detalle |
|---|---|
| **Multi-stage** | El stage `builder` no se incluye en la imagen final. La imagen de producción solo tiene el JRE (~200MB vs ~400MB con JDK) |
| **Caché de capas** | `pom.xml` se copia antes que `src/` para que las dependencias se cacheen y no se re-descarguen en cada build si solo cambia el código |
| **Usuario no-root** | Se crea `appuser` sin privilegios. Buena práctica de seguridad para contenedores en producción |
| **JVM container-aware** | `UseContainerSupport` y `MaxRAMPercentage` evitan que la JVM ignore los límites de memoria del contenedor (problema común en Kubernetes/ECS) |
| **JRE Alpine** | `eclipse-temurin:17-jre-alpine` produce imágenes ~60% más pequeñas que el JDK completo |

### Comandos de uso

```bash
# Build de la imagen
docker build -t nequi/franchises:latest .

# Correr la imagen contra un postgres externo
docker run -p 8080:8080 \
  -e R2DBC_URL="r2dbc:postgresql://host.docker.internal:5432/franchises_db" \
  -e FLYWAY_URL="jdbc:postgresql://host.docker.internal:5432/franchises_db" \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  nequi/franchises:latest

# Con docker-compose (incluye postgres local)
docker-compose up --build
```

---

## 3. Variables de entorno de producción

### Variables requeridas (sin valor por defecto en producción)

| Variable | Descripción | Ejemplo AWS RDS |
|---|---|---|
| `R2DBC_URL` | URL de conexión R2DBC para runtime | `r2dbc:postgresql://franchises-rds.xxxx.us-east-1.rds.amazonaws.com:5432/franchises_db` |
| `FLYWAY_URL` | URL JDBC para migraciones Flyway al startup | `jdbc:postgresql://franchises-rds.xxxx.us-east-1.rds.amazonaws.com:5432/franchises_db` |
| `POSTGRES_USER` | Usuario de la base de datos | `nequi_app` |
| `POSTGRES_PASSWORD` | Contraseña (usar AWS Secrets Manager en prod) | `***` |

### Variables opcionales (con valores por defecto)

| Variable | Por defecto | Descripción |
|---|---|---|
| `SERVER_PORT` | `8080` | Puerto HTTP del servidor Netty |
| `SPRING_R2DBC_POOL_INITIAL_SIZE` | `5` | Pool mínimo de conexiones R2DBC |
| `SPRING_R2DBC_POOL_MAX_SIZE` | `20` | Pool máximo de conexiones R2DBC |

### Obtener URLs desde los outputs de Terraform

```bash
cd infra/terraform
terraform output rds_r2dbc_url   # → R2DBC_URL
terraform output rds_flyway_url  # → FLYWAY_URL
```

---

## 4. README.md — estructura y secciones

**Archivo:** `README.md` (raíz del proyecto)

El README debe seguir esta estructura exacta:

```markdown
# API de Gestión de Franquicias — Nequi

> RFC-001 rev 2 · Spring WebFlux + R2DBC + PostgreSQL

## Tabla de Contenidos
1. Requisitos previos
2. Quickstart local (docker-compose)
3. Despliegue en AWS (Terraform + RDS)
4. Variables de entorno
5. Endpoints disponibles
6. Ejecutar tests
7. Arquitectura

## 1. Requisitos previos
- Java 17+
- Maven 3.8+
- Docker + Docker Compose
- Terraform 1.5+ (solo para despliegue en AWS)
- Cuenta AWS con permisos para RDS, VPC, Security Groups (solo para AWS)

## 2. Quickstart local (docker-compose)
# Instrucciones paso a paso:
# 1. Clonar el repositorio
# 2. docker-compose up --build
# 3. Verificar health: curl http://localhost:8080/actuator/health
# 4. Importar colección Postman / usar ejemplos de curl del README

## 3. Despliegue en AWS (Terraform + RDS)
# Instrucciones:
# 1. cd infra/terraform && terraform init
# 2. terraform plan (con variables vpc_id, subnet_ids, db_username, db_password)
# 3. terraform apply
# 4. Copiar outputs rds_r2dbc_url y rds_flyway_url
# 5. Configurar variables de entorno en el servidor / ECS / EC2
# 6. docker run con las variables de RDS

## 4. Variables de entorno
# Tabla completa de variables (ver Sección 3 de esta spec)

## 5. Endpoints disponibles
# Tabla completa de los 11 endpoints con método, URL y descripción

## 6. Ejecutar tests
# mvn test                    → todos los tests (requiere Docker para Testcontainers)
# mvn test -pl . -Dtest=Franchise*  → solo tests de franquicia

## 7. Arquitectura
# Diagrama ASCII de las capas hexagonales
# Descripción del stack tecnológico
```

### Tabla de endpoints para el README

| Método | URL | Descripción |
|---|---|---|
| `POST` | `/api/v1/franchises` | Crear franquicia |
| `PATCH` | `/api/v1/franchises/{id}/name` | Renombrar franquicia |
| `GET` | `/api/v1/franchises/{id}/top-stock` | Producto con más stock por sucursal |
| `POST` | `/api/v1/franchises/{fId}/branches` | Agregar sucursal |
| `PATCH` | `/api/v1/franchises/{fId}/branches/{bId}/name` | Renombrar sucursal |
| `POST` | `/api/v1/franchises/{fId}/branches/{bId}/products` | Agregar producto |
| `DELETE` | `/api/v1/franchises/{fId}/branches/{bId}/products/{pId}` | Eliminar producto |
| `PATCH` | `/api/v1/franchises/{fId}/branches/{bId}/products/{pId}/stock` | Modificar stock |
| `PATCH` | `/api/v1/franchises/{fId}/branches/{bId}/products/{pId}/name` | Renombrar producto |

### Diagrama de arquitectura para el README

```
┌─────────────────────────────────────────────────────────┐
│                    HTTP (Netty :8080)                    │
├─────────────────────────────────────────────────────────┤
│  Router Layer      RouterFunctions (sin @Controller)    │
├─────────────────────────────────────────────────────────┤
│  Handler Layer     Extrae, valida, mapea ServerResponse │
├─────────────────────────────────────────────────────────┤
│  Service Layer     Lógica de negocio · @Transactional   │
├─────────────────────────────────────────────────────────┤
│  Repository Layer  R2dbcRepository + DatabaseClient     │
├─────────────────────────────────────────────────────────┤
│  PostgreSQL 16     R2DBC (runtime) · JDBC/Flyway (init) │
└─────────────────────────────────────────────────────────┘
```

---

## 5. Git flow y convención de ramas

### Convención de nombres de ramas

| Tipo | Patrón | Ejemplo |
|---|---|---|
| Feature / Spec | `feature/spec-00N/descripcion` | `feature/spec-001/scaffolding` |
| Bugfix | `fix/descripcion-corta` | `fix/top-stock-null-pointer` |
| Hotfix en producción | `hotfix/descripcion-corta` | `hotfix/stock-constraint-check` |
| Release | `release/vX.Y.Z` | `release/v1.0.0` |

### Convención de mensajes de commit (Conventional Commits)

```
<tipo>(<scope>): <descripción en imperativo>

tipos válidos:
  feat      → nueva funcionalidad
  fix       → corrección de bug
  spec      → documentos de especificación
  docs      → cambios en README u otra documentación
  test      → añadir o corregir tests
  refactor  → refactoring sin cambio de comportamiento
  chore     → tareas de build, CI, dependencias

Ejemplos:
  feat(franchise): add create franchise endpoint
  fix(product): handle negative stock on update
  test(branch): add Testcontainers integration test
  docs(readme): add AWS deployment instructions
```

### Flujo de merge a `main`

```bash
# 1. Asegurarse de que todos los tests pasan en la rama feature
mvn test

# 2. Hacer squash merge a main (mantiene historial limpio)
git checkout main
git merge --squash feature/spec-002/crud-endpoints
git commit -m "feat: implement 7 mandatory CRUD endpoints with WebFlux + R2DBC"

# 3. Etiquetar el release con semver
git tag -a v1.0.0 -m "Release v1.0.0 - API Gestión de Franquicias Nequi"
git push origin main --tags
```

### Tags semver para releases

| Tag | Cuándo usarlo |
|---|---|
| `v1.0.0` | Primera versión completa con todos los criterios cubiertos |
| `v1.1.0` | Nuevas funcionalidades backward-compatible |
| `v1.0.1` | Bugfix en producción |

---

## 6. Checklist de entrega final

Antes del push final a GitHub público verificar que:

### Código y tests

- [ ] `mvn clean package` pasa sin errores
- [ ] `mvn test` pasa con 0 tests fallidos y 0 tests saltados
- [ ] No hay llamadas `.block()` fuera del startup de Flyway
- [ ] No hay credenciales hardcodeadas (contraseñas, tokens, ARNs de AWS)
- [ ] El archivo `.gitignore` excluye: `target/`, `.env`, `*.tfstate`, `*.tfstate.backup`, `.terraform/`

### Docker

- [ ] `docker build -t nequi/franchises:latest .` completa sin errores
- [ ] `docker-compose up` levanta ambos servicios en estado `healthy`
- [ ] `curl http://localhost:8080/actuator/health` retorna `{"status":"UP"}`
- [ ] La imagen final pesa menos de 300MB (`docker images nequi/franchises`)

### Terraform

- [ ] `terraform validate` retorna `Success! The configuration is valid.`
- [ ] `terraform plan` no muestra errores de sintaxis
- [ ] Los outputs `rds_r2dbc_url` y `rds_flyway_url` están definidos

### Documentación

- [ ] `README.md` incluye instrucciones completas de quickstart local
- [ ] `README.md` incluye instrucciones de despliegue en AWS
- [ ] `README.md` incluye tabla de todos los endpoints
- [ ] `README.md` incluye tabla de variables de entorno

### Git

- [ ] El historial de `main` es limpio (sin commits de work-in-progress)
- [ ] El tag `v1.0.0` está creado y apunta al commit de release
- [ ] No hay archivos sensibles en el historial (`git log --all --full-history -- "*.env"`)

---

## 7. Criterios de aceptación

| # | Criterio | Verificación |
|---|---|---|
| AC-01 | `docker build` produce imagen sin errores | `docker build` retorna exit code 0 |
| AC-02 | Imagen final es menor a 300MB | `docker images nequi/franchises` muestra < 300MB |
| AC-03 | La app corre como usuario no-root | `docker exec <container> whoami` retorna `appuser` |
| AC-04 | Health check del contenedor pasa | `docker inspect` muestra `Status: healthy` |
| AC-05 | `docker-compose up` levanta todo el stack | Ambos servicios en `healthy` |
| AC-06 | `README.md` tiene sección de quickstart local | Existe y es ejecutable paso a paso |
| AC-07 | `README.md` tiene sección de deploy AWS | Existe con comandos Terraform |
| AC-08 | `README.md` tiene tabla de 9 endpoints | Tabla completa con método, URL, descripción |
| AC-09 | `terraform validate` pasa | `Success! The configuration is valid.` |
| AC-10 | `.gitignore` excluye artefactos sensibles | `git status` no muestra `target/`, `.terraform/`, `*.tfstate` |
| AC-11 | No hay secrets en el repositorio | `git log -p` no contiene contraseñas ni tokens |
| AC-12 | Tag `v1.0.0` existe en `main` | `git tag` lista `v1.0.0` |

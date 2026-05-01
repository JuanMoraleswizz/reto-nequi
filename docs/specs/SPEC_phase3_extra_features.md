# SPEC-003 · Puntos Extra y Query Avanzada

| Campo       | Detalle                                  |
|-------------|------------------------------------------|
| **Fase**    | 3 — Puntos Extra y Query Avanzada        |
| **Rama**    | `feature/spec-003/extra-features`        |
| **RFC ref** | RFC-001 rev 2 · Sección 06, 07           |
| **Estado**  | Pendiente de implementación              |
| **Depende** | SPEC-002 (endpoints CRUD completos)      |

---

## Tabla de Contenidos

1. [Objetivo](#1-objetivo)
2. [Endpoints PATCH de nombre](#2-endpoints-patch-de-nombre)
3. [Query DISTINCT ON — top-stock](#3-query-distinct-on--top-stock)
4. [GlobalErrorHandler — ProblemDetail](#4-globalerrorhandler--problemdetail)
5. [Tests de integración](#5-tests-de-integración)
6. [Criterios de aceptación](#6-criterios-de-aceptación)

---

## 1. Objetivo

Completar los **6 puntos extra** del RFC añadiendo:

- 4 endpoints `PATCH` para actualización de nombre (franquicia, sucursal, producto)
- La query avanzada `DISTINCT ON` para el endpoint `GET /top-stock` (que en SPEC-002 quedó como placeholder `501`)
- `GlobalErrorHandler` que convierte excepciones de dominio a respuestas `ProblemDetail` (RFC 9457)

Al terminar esta fase la API está funcionalmente completa con los 11 endpoints documentados en el RFC.

---

## 2. Endpoints PATCH de nombre

### 2.1 Nuevos métodos en los puertos de entrada

```java
// application/port/in/FranchiseUseCase.java — agregar:
Mono<Franchise> updateFranchiseName(UUID franchiseId, String newName);

// application/port/in/BranchUseCase.java — agregar:
Mono<Branch> updateBranchName(UUID franchiseId, UUID branchId, String newName);

// application/port/in/ProductUseCase.java — agregar:
Mono<Product> updateProductName(UUID franchiseId, UUID branchId, UUID productId, String newName);
```

### 2.2 Implementaciones en los servicios

#### `FranchiseService.java` — `updateFranchiseName`

```java
@Override
public Mono<Franchise> updateFranchiseName(UUID franchiseId, String newName) {
    return franchiseRepository.findById(franchiseId)
        .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
        .flatMap(franchise -> franchiseRepository.existsByName(newName)
            .flatMap(exists -> {
                if (exists) return Mono.error(new FranchiseNameAlreadyExistsException(newName));
                franchise.setName(newName);
                return franchiseRepository.save(franchise);
            })
        );
}
```

#### `BranchService.java` — `updateBranchName`

```java
@Override
public Mono<Branch> updateBranchName(UUID franchiseId, UUID branchId, String newName) {
    return franchiseRepository.findById(franchiseId)
        .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
        .flatMap(franchise -> branchRepository.findById(branchId))
        .switchIfEmpty(Mono.error(new BranchNotFoundException(branchId)))
        .flatMap(branch -> {
            branch.setName(newName);
            return branchRepository.save(branch);
        });
}
```

#### `ProductService.java` — `updateProductName`

```java
@Override
public Mono<Product> updateProductName(UUID franchiseId, UUID branchId, UUID productId, String newName) {
    return branchRepository.findById(branchId)
        .switchIfEmpty(Mono.error(new BranchNotFoundException(branchId)))
        .flatMap(branch -> productRepository.findById(productId))
        .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
        .flatMap(product -> {
            product.setName(newName);
            return productRepository.save(product);
        });
}
```

### 2.3 DTO compartido

```java
// infrastructure/adapter/in/web/dto/UpdateNameRequest.java
public record UpdateNameRequest(@NotBlank String name) {}
```

### 2.4 Nuevos métodos en los Handlers

#### `FranchiseHandler.java` — agregar

```java
// PATCH /api/v1/franchises/{franchiseId}/name
public Mono<ServerResponse> updateFranchiseName(ServerRequest request) {
    UUID franchiseId = UUID.fromString(request.pathVariable("franchiseId"));
    return request.bodyToMono(UpdateNameRequest.class)
        .flatMap(body -> franchiseUseCase.updateFranchiseName(franchiseId, body.name()))
        .flatMap(franchise -> ServerResponse.ok().bodyValue(franchise));
}
```

#### `BranchHandler.java` — agregar

```java
// PATCH /api/v1/franchises/{franchiseId}/branches/{branchId}/name
public Mono<ServerResponse> updateBranchName(ServerRequest request) {
    UUID franchiseId = UUID.fromString(request.pathVariable("franchiseId"));
    UUID branchId    = UUID.fromString(request.pathVariable("branchId"));
    return request.bodyToMono(UpdateNameRequest.class)
        .flatMap(body -> branchUseCase.updateBranchName(franchiseId, branchId, body.name()))
        .flatMap(branch -> ServerResponse.ok().bodyValue(branch));
}
```

#### `ProductHandler.java` — agregar

```java
// PATCH /api/v1/franchises/{fId}/branches/{bId}/products/{pId}/name
public Mono<ServerResponse> updateProductName(ServerRequest request) {
    UUID franchiseId = UUID.fromString(request.pathVariable("franchiseId"));
    UUID branchId    = UUID.fromString(request.pathVariable("branchId"));
    UUID productId   = UUID.fromString(request.pathVariable("productId"));
    return request.bodyToMono(UpdateNameRequest.class)
        .flatMap(body -> productUseCase.updateProductName(franchiseId, branchId, productId, body.name()))
        .flatMap(product -> ServerResponse.ok().bodyValue(product));
}
```

### 2.5 Nuevas rutas en los Routers

#### `FranchiseRouter.java` — agregar ruta

```java
.PATCH("/api/v1/franchises/{franchiseId}/name", handler::updateFranchiseName)
```

#### `BranchRouter.java` — agregar ruta

```java
.PATCH("/api/v1/franchises/{franchiseId}/branches/{branchId}/name", handler::updateBranchName)
```

#### `ProductRouter.java` — agregar ruta

```java
.PATCH("/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name",
       handler::updateProductName)
```

---

## 3. Query DISTINCT ON — top-stock

### 3.1 DTO de respuesta

```java
// infrastructure/adapter/in/web/dto/TopStockResponse.java
public record TopStockResponse(
    UUID   branchId,
    String branchName,
    UUID   productId,
    String productName,
    int    stock
) {}
```

### 3.2 Puerto de salida — nuevo método

```java
// application/port/out/ProductRepository.java — agregar:
Flux<TopStockResponse> findTopStockPerBranchByFranchiseId(UUID franchiseId);
```

### 3.3 Implementación con `DatabaseClient`

```java
// infrastructure/adapter/out/persistence/ProductCustomRepository.java
@Repository
@RequiredArgsConstructor
public class ProductCustomRepository {

    private final DatabaseClient databaseClient;

    private static final String TOP_STOCK_QUERY = """
        SELECT DISTINCT ON (b.id)
            b.id          AS branch_id,
            b.name        AS branch_name,
            p.id          AS product_id,
            p.name        AS product_name,
            p.stock
        FROM branches b
        JOIN products p ON p.branch_id = b.id
        WHERE b.franchise_id = :franchiseId
        ORDER BY b.id, p.stock DESC
        """;

    public Flux<TopStockResponse> findTopStockPerBranch(UUID franchiseId) {
        return databaseClient.sql(TOP_STOCK_QUERY)
            .bind("franchiseId", franchiseId)
            .map((row, metadata) -> new TopStockResponse(
                row.get("branch_id",   UUID.class),
                row.get("branch_name", String.class),
                row.get("product_id",  UUID.class),
                row.get("product_name",String.class),
                row.get("stock",       Integer.class)
            ))
            .all();
    }
}
```

### 3.4 Nuevo puerto de entrada — `FranchiseUseCase`

```java
// application/port/in/FranchiseUseCase.java — agregar:
Flux<TopStockResponse> getTopStockPerBranch(UUID franchiseId);
```

### 3.5 Implementación en `FranchiseService`

```java
@Override
public Flux<TopStockResponse> getTopStockPerBranch(UUID franchiseId) {
    return franchiseRepository.findById(franchiseId)
        .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
        .flatMapMany(franchise -> productCustomRepository.findTopStockPerBranch(franchiseId));
}
```

### 3.6 Handler — reemplazar placeholder `501`

```java
// FranchiseHandler.java — reemplazar la implementación placeholder:
// GET /api/v1/franchises/{franchiseId}/top-stock
public Mono<ServerResponse> getTopStock(ServerRequest request) {
    UUID franchiseId = UUID.fromString(request.pathVariable("franchiseId"));
    return ServerResponse.ok()
        .body(franchiseUseCase.getTopStockPerBranch(franchiseId), TopStockResponse.class);
}
```

---

## 4. GlobalErrorHandler — ProblemDetail

### 4.1 Estructura del handler

```
infrastructure/adapter/in/web/error/
└── GlobalErrorHandler.java
```

### 4.2 Implementación

```java
package com.nequi.franchises.infrastructure.adapter.in.web.error;

import com.nequi.franchises.application.exception.*;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Mono;

@Component
@Order(-2)  // Antes del DefaultErrorWebExceptionHandler de Spring Boot
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus  status  = resolveStatus(ex);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        detail.setTitle(resolveTitle(ex));

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(detail);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private HttpStatus resolveStatus(Throwable ex) {
        return switch (ex) {
            case FranchiseNotFoundException ignored      -> HttpStatus.NOT_FOUND;
            case BranchNotFoundException ignored         -> HttpStatus.NOT_FOUND;
            case ProductNotFoundException ignored        -> HttpStatus.NOT_FOUND;
            case FranchiseNameAlreadyExistsException ignored -> HttpStatus.CONFLICT;
            case InvalidStockException ignored           -> HttpStatus.BAD_REQUEST;
            default                                      -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private String resolveTitle(Throwable ex) {
        return switch (ex) {
            case FranchiseNotFoundException ignored      -> "Franchise Not Found";
            case BranchNotFoundException ignored         -> "Branch Not Found";
            case ProductNotFoundException ignored        -> "Product Not Found";
            case FranchiseNameAlreadyExistsException ignored -> "Franchise Name Conflict";
            case InvalidStockException ignored           -> "Invalid Stock Value";
            default                                      -> "Internal Server Error";
        };
    }
}
```

### 4.3 Formato de respuesta de error (ProblemDetail — RFC 9457)

```json
// 404 Not Found
{
  "type":     "about:blank",
  "title":    "Franchise Not Found",
  "status":   404,
  "detail":   "Franchise with id 550e8400-... not found",
  "instance": "/api/v1/franchises/550e8400-.../branches"
}

// 409 Conflict
{
  "type":     "about:blank",
  "title":    "Franchise Name Conflict",
  "status":   409,
  "detail":   "A franchise with name 'Franquicia Norte' already exists",
  "instance": "/api/v1/franchises"
}

// 400 Bad Request
{
  "type":     "about:blank",
  "title":    "Invalid Stock Value",
  "status":   400,
  "detail":   "Stock cannot be negative. Received: -5",
  "instance": "/api/v1/franchises/.../products/.../stock"
}
```

### 4.4 Actualizar mensajes en las excepciones de dominio

```java
// FranchiseNotFoundException.java
public FranchiseNotFoundException(UUID id) {
    super("Franchise with id " + id + " not found");
}

// FranchiseNameAlreadyExistsException.java
public FranchiseNameAlreadyExistsException(String name) {
    super("A franchise with name '" + name + "' already exists");
}

// BranchNotFoundException.java
public BranchNotFoundException(UUID id) {
    super("Branch with id " + id + " not found");
}

// ProductNotFoundException.java
public ProductNotFoundException(UUID id) {
    super("Product with id " + id + " not found");
}

// InvalidStockException.java
public InvalidStockException(int value) {
    super("Stock cannot be negative. Received: " + value);
}
```

---

## 5. Tests de integración

### Estructura

```
src/test/java/com/nequi/franchises/
└── infrastructure/
    └── adapter/
        └── in/web/
            ├── FranchiseNameUpdateRouterTest.java
            ├── BranchNameUpdateRouterTest.java
            ├── ProductNameUpdateRouterTest.java
            ├── TopStockRouterTest.java
            └── GlobalErrorHandlerTest.java
```

### `FranchiseNameUpdateRouterTest.java`

```java
// TC-FN01: Actualizar nombre de franquicia existente → 200
@Test void shouldUpdateFranchiseName_returns200()

// TC-FN02: Franquicia no encontrada → 404 con ProblemDetail
@Test void shouldReturn404ProblemDetail_whenFranchiseNotFound()

// TC-FN03: Nombre duplicado → 409 con ProblemDetail
@Test void shouldReturn409ProblemDetail_whenNameAlreadyExists()

// TC-FN04: Nombre vacío → 400 con ProblemDetail
@Test void shouldReturn400ProblemDetail_whenNameIsBlank()
```

### `TopStockRouterTest.java`

```java
// TC-TS01: Franquicia con sucursales y productos → lista correcta ordenada por stock DESC
@Test void shouldReturnTopStockPerBranch_returns200()

// TC-TS02: Sucursal sin productos → no aparece en el resultado
@Test void shouldExcludeBranchesWithNoProducts()

// TC-TS03: Franquicia no encontrada → 404 con ProblemDetail
@Test void shouldReturn404ProblemDetail_whenFranchiseNotFound()

// TC-TS04: Cada sucursal aparece una sola vez (DISTINCT ON funciona correctamente)
@Test void shouldReturnOnlyOneProductPerBranch()
```

### `GlobalErrorHandlerTest.java`

```java
// TC-EH01: Respuesta de error tiene Content-Type application/problem+json
@Test void shouldReturnProblemJsonContentType_on404()

// TC-EH02: Body tiene campos type, title, status, detail
@Test void shouldReturnProblemDetailFields_on404()

// TC-EH03: 409 retorna status 409 y título correcto
@Test void shouldReturn409_withCorrectTitle()

// TC-EH04: 400 retorna status 400 y detalle con el valor inválido
@Test void shouldReturn400_withInvalidValueInDetail()
```

---

## 6. Criterios de aceptación

| # | Criterio | Verificación |
|---|---|---|
| AC-01 | `PATCH /franchises/{id}/name` actualiza y retorna 200 | Test TC-FN01 pasa |
| AC-02 | `PATCH /branches/{id}/name` actualiza y retorna 200 | Test análogo en BranchNameUpdateRouterTest |
| AC-03 | `PATCH /products/{id}/name` actualiza y retorna 200 | Test análogo en ProductNameUpdateRouterTest |
| AC-04 | `GET /top-stock` retorna un producto por sucursal | Test TC-TS04 pasa |
| AC-05 | `GET /top-stock` usa `DISTINCT ON` via `DatabaseClient` | Inspección: no hay `findAll()` + filtrado en memoria |
| AC-06 | Sucursales sin productos no aparecen en top-stock | Test TC-TS02 pasa |
| AC-07 | Errores retornan `application/problem+json` | Test TC-EH01 pasa |
| AC-08 | `ProblemDetail` incluye `type`, `title`, `status`, `detail` | Test TC-EH02 pasa |
| AC-09 | 404 para entidades no encontradas | Tests TC-FN02, TC-TS03 pasan |
| AC-10 | 409 para nombres duplicados | Test TC-FN03 pasa |
| AC-11 | 400 para datos de entrada inválidos | Tests TC-FN04, TC-EH04 pasan |
| AC-12 | Todos los tests pasan con `mvn test` | `BUILD SUCCESS` |

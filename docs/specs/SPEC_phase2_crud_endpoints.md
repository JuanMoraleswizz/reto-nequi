# SPEC-002 · CRUD Endpoints Obligatorios

| Campo       | Detalle                              |
|-------------|--------------------------------------|
| **Fase**    | 2 — Endpoints CRUD                   |
| **Rama**    | `feature/spec-002/crud-endpoints`    |
| **RFC ref** | RFC-001 rev 2 · Sección 04, 06       |
| **Estado**  | Pendiente de implementación          |
| **Depende** | SPEC-001 (scaffolding completo)      |

---

## Tabla de Contenidos

1. [Objetivo](#1-objetivo)
2. [Árbol de paquetes hexagonal](#2-árbol-de-paquetes-hexagonal)
3. [Domain Model](#3-domain-model)
4. [Repository Layer](#4-repository-layer)
5. [Service Layer](#5-service-layer)
6. [Handler Layer](#6-handler-layer)
7. [Router Layer](#7-router-layer)
8. [Contratos de API — 7 endpoints obligatorios](#8-contratos-de-api--7-endpoints-obligatorios)
9. [Tests](#9-tests)
10. [Criterios de aceptación](#10-criterios-de-aceptación)

---

## 1. Objetivo

Implementar los **7 endpoints obligatorios** del RFC usando arquitectura hexagonal con Spring WebFlux funcional (`RouterFunctions` + `HandlerFunction`), Spring Data R2DBC para persistencia reactiva y Testcontainers + WebTestClient para tests de integración.

Al terminar esta fase todos los criterios obligatorios del RFC deben estar cubiertos y testeados.

---

## 2. Árbol de paquetes hexagonal

```
src/main/java/com/nequi/franchises/
├── FranchisesApplication.java
│
├── domain/
│   └── model/
│       ├── Franchise.java
│       ├── Branch.java
│       └── Product.java
│
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── FranchiseUseCase.java
│   │   │   ├── BranchUseCase.java
│   │   │   └── ProductUseCase.java
│   │   └── out/
│   │       ├── FranchiseRepository.java
│   │       ├── BranchRepository.java
│   │       └── ProductRepository.java
│   └── service/
│       ├── FranchiseService.java
│       ├── BranchService.java
│       └── ProductService.java
│
└── infrastructure/
    ├── adapter/
    │   ├── in/web/
    │   │   ├── FranchiseRouter.java
    │   │   ├── FranchiseHandler.java
    │   │   ├── BranchRouter.java
    │   │   ├── BranchHandler.java
    │   │   ├── ProductRouter.java
    │   │   └── ProductHandler.java
    │   └── out/persistence/
    │       ├── FranchiseR2dbcRepository.java
    │       ├── BranchR2dbcRepository.java
    │       └── ProductR2dbcRepository.java
    └── config/
        └── RouterConfig.java

src/main/java/com/nequi/franchises/
└── infrastructure/
    └── adapter/
        └── in/web/dto/
            ├── CreateFranchiseRequest.java
            ├── CreateBranchRequest.java
            ├── CreateProductRequest.java
            └── UpdateStockRequest.java
```

---

## 3. Domain Model

### `Franchise.java`

```java
package com.nequi.franchises.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("franchises")
public class Franchise {

    @Id
    private UUID id;

    private String name;

    private OffsetDateTime createdAt;
}
```

### `Branch.java`

```java
package com.nequi.franchises.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("branches")
public class Branch {

    @Id
    private UUID id;

    @Column("franchise_id")
    private UUID franchiseId;

    private String name;

    private OffsetDateTime createdAt;
}
```

### `Product.java`

```java
package com.nequi.franchises.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("products")
public class Product {

    @Id
    private UUID id;

    @Column("branch_id")
    private UUID branchId;

    private String name;

    private int stock;

    private OffsetDateTime createdAt;
}
```

---

## 4. Repository Layer

### Interfaces de puerto de salida

```java
// application/port/out/FranchiseRepository.java
public interface FranchiseRepository {
    Mono<Franchise> save(Franchise franchise);
    Mono<Franchise> findById(UUID id);
    Mono<Boolean>   existsByName(String name);
}

// application/port/out/BranchRepository.java
public interface BranchRepository {
    Mono<Branch>  save(Branch branch);
    Mono<Branch>  findById(UUID id);
    Mono<Boolean> existsByFranchiseId(UUID franchiseId);
}

// application/port/out/ProductRepository.java
public interface ProductRepository {
    Mono<Product> save(Product product);
    Mono<Void>    deleteById(UUID id);
    Mono<Product> findById(UUID id);
}
```

### Adaptadores R2DBC

```java
// infrastructure/adapter/out/persistence/FranchiseR2dbcRepository.java
@Repository
public interface FranchiseR2dbcRepository
        extends R2dbcRepository<Franchise, UUID>, FranchiseRepository {
    Mono<Boolean> existsByName(String name);
}

// infrastructure/adapter/out/persistence/BranchR2dbcRepository.java
@Repository
public interface BranchR2dbcRepository
        extends R2dbcRepository<Branch, UUID>, BranchRepository {
    Flux<Branch> findAllByFranchiseId(UUID franchiseId);
}

// infrastructure/adapter/out/persistence/ProductR2dbcRepository.java
@Repository
public interface ProductR2dbcRepository
        extends R2dbcRepository<Product, UUID>, ProductRepository {
    Flux<Product> findAllByBranchId(UUID branchId);
}
```

---

## 5. Service Layer

### Interfaces de puerto de entrada

```java
// application/port/in/FranchiseUseCase.java
public interface FranchiseUseCase {
    Mono<Franchise> createFranchise(String name);
}

// application/port/in/BranchUseCase.java
public interface BranchUseCase {
    Mono<Branch> addBranch(UUID franchiseId, String name);
}

// application/port/in/ProductUseCase.java
public interface ProductUseCase {
    Mono<Product> addProduct(UUID franchiseId, UUID branchId, String name, int stock);
    Mono<Void>    removeProduct(UUID franchiseId, UUID branchId, UUID productId);
    Mono<Product> updateStock(UUID franchiseId, UUID branchId, UUID productId, int newStock);
}
```

### Implementaciones de servicio

#### `FranchiseService.java`

```java
@Service
@RequiredArgsConstructor
public class FranchiseService implements FranchiseUseCase {

    private final FranchiseR2dbcRepository franchiseRepository;

    @Override
    public Mono<Franchise> createFranchise(String name) {
        return franchiseRepository.existsByName(name)
            .flatMap(exists -> {
                if (exists) return Mono.error(new FranchiseNameAlreadyExistsException(name));
                return franchiseRepository.save(
                    Franchise.builder()
                        .id(UUID.randomUUID())
                        .name(name)
                        .createdAt(OffsetDateTime.now())
                        .build()
                );
            });
    }
}
```

#### `BranchService.java`

```java
@Service
@RequiredArgsConstructor
public class BranchService implements BranchUseCase {

    private final BranchR2dbcRepository branchRepository;
    private final FranchiseR2dbcRepository franchiseRepository;

    @Override
    public Mono<Branch> addBranch(UUID franchiseId, String name) {
        return franchiseRepository.findById(franchiseId)
            .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
            .flatMap(franchise -> branchRepository.save(
                Branch.builder()
                    .id(UUID.randomUUID())
                    .franchiseId(franchiseId)
                    .name(name)
                    .createdAt(OffsetDateTime.now())
                    .build()
            ));
    }
}
```

#### `ProductService.java`

```java
@Service
@RequiredArgsConstructor
public class ProductService implements ProductUseCase {

    private final ProductR2dbcRepository productRepository;
    private final BranchR2dbcRepository  branchRepository;

    @Override
    public Mono<Product> addProduct(UUID franchiseId, UUID branchId, String name, int stock) {
        return branchRepository.findById(branchId)
            .switchIfEmpty(Mono.error(new BranchNotFoundException(branchId)))
            .flatMap(branch -> productRepository.save(
                Product.builder()
                    .id(UUID.randomUUID())
                    .branchId(branchId)
                    .name(name)
                    .stock(stock)
                    .createdAt(OffsetDateTime.now())
                    .build()
            ));
    }

    @Override
    public Mono<Void> removeProduct(UUID franchiseId, UUID branchId, UUID productId) {
        return productRepository.findById(productId)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
            .flatMap(p -> productRepository.deleteById(productId));
    }

    @Override
    public Mono<Product> updateStock(UUID franchiseId, UUID branchId, UUID productId, int newStock) {
        if (newStock < 0) return Mono.error(new InvalidStockException(newStock));
        return productRepository.findById(productId)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
            .flatMap(product -> {
                product.setStock(newStock);
                return productRepository.save(product);
            });
    }
}
```

### Excepciones de dominio

```
application/exception/
├── FranchiseNotFoundException.java        → extends RuntimeException
├── FranchiseNameAlreadyExistsException.java → extends RuntimeException
├── BranchNotFoundException.java           → extends RuntimeException
├── ProductNotFoundException.java          → extends RuntimeException
└── InvalidStockException.java             → extends RuntimeException
```

---

## 6. Handler Layer

### `FranchiseHandler.java`

```java
@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private final FranchiseUseCase franchiseUseCase;

    // POST /api/v1/franchises
    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        return request.bodyToMono(CreateFranchiseRequest.class)
            .flatMap(body -> franchiseUseCase.createFranchise(body.name()))
            .flatMap(franchise -> ServerResponse.status(HttpStatus.CREATED).bodyValue(franchise));
    }
}
```

### `BranchHandler.java`

```java
@Component
@RequiredArgsConstructor
public class BranchHandler {

    private final BranchUseCase branchUseCase;

    // POST /api/v1/franchises/{franchiseId}/branches
    public Mono<ServerResponse> addBranch(ServerRequest request) {
        UUID franchiseId = UUID.fromString(request.pathVariable("franchiseId"));
        return request.bodyToMono(CreateBranchRequest.class)
            .flatMap(body -> branchUseCase.addBranch(franchiseId, body.name()))
            .flatMap(branch -> ServerResponse.status(HttpStatus.CREATED).bodyValue(branch));
    }
}
```

### `ProductHandler.java`

```java
@Component
@RequiredArgsConstructor
public class ProductHandler {

    private final ProductUseCase productUseCase;

    // POST /api/v1/franchises/{fId}/branches/{bId}/products
    public Mono<ServerResponse> addProduct(ServerRequest request) {
        UUID franchiseId = UUID.fromString(request.pathVariable("franchiseId"));
        UUID branchId    = UUID.fromString(request.pathVariable("branchId"));
        return request.bodyToMono(CreateProductRequest.class)
            .flatMap(body -> productUseCase.addProduct(franchiseId, branchId, body.name(), body.stock()))
            .flatMap(product -> ServerResponse.status(HttpStatus.CREATED).bodyValue(product));
    }

    // DELETE /api/v1/franchises/{fId}/branches/{bId}/products/{pId}
    public Mono<ServerResponse> removeProduct(ServerRequest request) {
        UUID franchiseId = UUID.fromString(request.pathVariable("franchiseId"));
        UUID branchId    = UUID.fromString(request.pathVariable("branchId"));
        UUID productId   = UUID.fromString(request.pathVariable("productId"));
        return productUseCase.removeProduct(franchiseId, branchId, productId)
            .then(ServerResponse.noContent().build());
    }

    // PATCH /api/v1/franchises/{fId}/branches/{bId}/products/{pId}/stock
    public Mono<ServerResponse> updateStock(ServerRequest request) {
        UUID franchiseId = UUID.fromString(request.pathVariable("franchiseId"));
        UUID branchId    = UUID.fromString(request.pathVariable("branchId"));
        UUID productId   = UUID.fromString(request.pathVariable("productId"));
        return request.bodyToMono(UpdateStockRequest.class)
            .flatMap(body -> productUseCase.updateStock(franchiseId, branchId, productId, body.stock()))
            .flatMap(product -> ServerResponse.ok().bodyValue(product));
    }
}
```

### DTOs

```java
// infrastructure/adapter/in/web/dto/
public record CreateFranchiseRequest(@NotBlank String name) {}
public record CreateBranchRequest(@NotBlank String name) {}
public record CreateProductRequest(@NotBlank String name, @Min(0) int stock) {}
public record UpdateStockRequest(@Min(0) int stock) {}
```

---

## 7. Router Layer

### `FranchiseRouter.java`

```java
@Configuration
public class FranchiseRouter {

    @Bean
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler handler) {
        return RouterFunctions.route()
            .POST("/api/v1/franchises", handler::createFranchise)
            .build();
    }
}
```

### `BranchRouter.java`

```java
@Configuration
public class BranchRouter {

    @Bean
    public RouterFunction<ServerResponse> branchRoutes(BranchHandler handler) {
        return RouterFunctions.route()
            .POST("/api/v1/franchises/{franchiseId}/branches", handler::addBranch)
            .build();
    }
}
```

### `ProductRouter.java`

```java
@Configuration
public class ProductRouter {

    @Bean
    public RouterFunction<ServerResponse> productRoutes(ProductHandler handler) {
        return RouterFunctions.route()
            .POST  ("/api/v1/franchises/{franchiseId}/branches/{branchId}/products",
                    handler::addProduct)
            .DELETE("/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}",
                    handler::removeProduct)
            .PATCH ("/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock",
                    handler::updateStock)
            .build();
    }
}
```

---

## 8. Contratos de API — 7 endpoints obligatorios

**Base URL:** `/api/v1`

### EP-01 · `POST /franchises` — Crear franquicia

| Campo | Detalle |
|---|---|
| **Método** | `POST` |
| **URL** | `/api/v1/franchises` |
| **Request Body** | `{ "name": "Franquicia Norte" }` |
| **Response 201** | `{ "id": "uuid", "name": "Franquicia Norte", "createdAt": "..." }` |
| **Response 400** | nombre nulo o vacío |
| **Response 409** | nombre ya registrado |

### EP-02 · `POST /franchises/{franchiseId}/branches` — Agregar sucursal

| Campo | Detalle |
|---|---|
| **Método** | `POST` |
| **URL** | `/api/v1/franchises/{franchiseId}/branches` |
| **Request Body** | `{ "name": "Sucursal Centro" }` |
| **Response 201** | `{ "id": "uuid", "franchiseId": "uuid", "name": "Sucursal Centro", "createdAt": "..." }` |
| **Response 400** | nombre nulo o vacío |
| **Response 404** | franquicia no encontrada |

### EP-03 · `POST /franchises/{fId}/branches/{bId}/products` — Agregar producto

| Campo | Detalle |
|---|---|
| **Método** | `POST` |
| **URL** | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products` |
| **Request Body** | `{ "name": "Producto A", "stock": 100 }` |
| **Response 201** | `{ "id": "uuid", "branchId": "uuid", "name": "Producto A", "stock": 100, "createdAt": "..." }` |
| **Response 400** | nombre nulo/vacío o stock negativo |
| **Response 404** | sucursal no encontrada |

### EP-04 · `DELETE /franchises/{fId}/branches/{bId}/products/{pId}` — Eliminar producto

| Campo | Detalle |
|---|---|
| **Método** | `DELETE` |
| **URL** | `/api/v1/franchises/{fId}/branches/{bId}/products/{pId}` |
| **Response 204** | sin body |
| **Response 404** | producto no encontrado |

### EP-05 · `PATCH /franchises/{fId}/branches/{bId}/products/{pId}/stock` — Modificar stock

| Campo | Detalle |
|---|---|
| **Método** | `PATCH` |
| **URL** | `/api/v1/franchises/{fId}/branches/{bId}/products/{pId}/stock` |
| **Request Body** | `{ "stock": 250 }` |
| **Response 200** | producto actualizado completo |
| **Response 400** | stock negativo |
| **Response 404** | producto no encontrado |

### EP-06 · `GET /franchises/{franchiseId}/top-stock` — Producto con más stock por sucursal

> Este endpoint se implementa en SPEC-003 (query avanzada con `DatabaseClient`). El **Router y Handler deben declararse en esta fase** como placeholder que retorna `501 Not Implemented` hasta que SPEC-003 lo complete.

| Campo | Detalle |
|---|---|
| **Método** | `GET` |
| **URL** | `/api/v1/franchises/{franchiseId}/top-stock` |
| **Response 200** | array de `{ branchId, branchName, productId, productName, stock }` |
| **Response 404** | franquicia no encontrada |

### EP-07 · Programación reactiva (criterio transversal)

No es un endpoint adicional. El criterio se satisface al usar `Mono`/`Flux` en **todos** los métodos de servicio, repositorio y handler. No debe existir ninguna llamada bloqueante (`.block()`, `Thread.sleep()`, JDBC directo) fuera de la inicialización de Flyway.

---

## 9. Tests

### Estructura de tests

```
src/test/java/com/nequi/franchises/
├── infrastructure/
│   └── adapter/
│       └── in/web/
│           ├── FranchiseRouterTest.java
│           ├── BranchRouterTest.java
│           └── ProductRouterTest.java
└── shared/
    └── PostgresTestContainer.java
```

### `PostgresTestContainer.java` — Configuración compartida

```java
package com.nequi.franchises.shared;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public abstract class PostgresTestContainer {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("franchises_test")
            .withUsername("test")
            .withPassword("test");

    static {
        POSTGRES.start();
        System.setProperty("R2DBC_URL",
            "r2dbc:postgresql://" + POSTGRES.getHost() + ":" +
            POSTGRES.getFirstMappedPort() + "/franchises_test");
        System.setProperty("FLYWAY_URL", POSTGRES.getJdbcUrl());
        System.setProperty("POSTGRES_USER", POSTGRES.getUsername());
        System.setProperty("POSTGRES_PASSWORD", POSTGRES.getPassword());
    }
}
```

### `FranchiseRouterTest.java` — Casos de prueba

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class FranchiseRouterTest extends PostgresTestContainer {

    @Autowired WebTestClient webTestClient;

    // TC-F01: Crear franquicia exitosamente → 201
    @Test void shouldCreateFranchise_returns201()

    // TC-F02: Nombre duplicado → 409
    @Test void shouldReturn409_whenNameAlreadyExists()

    // TC-F03: Nombre vacío → 400
    @Test void shouldReturn400_whenNameIsBlank()
}
```

### `BranchRouterTest.java` — Casos de prueba

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class BranchRouterTest extends PostgresTestContainer {

    @Autowired WebTestClient webTestClient;

    // TC-B01: Agregar sucursal a franquicia existente → 201
    @Test void shouldAddBranch_returns201()

    // TC-B02: Franquicia no existe → 404
    @Test void shouldReturn404_whenFranchiseNotFound()

    // TC-B03: Nombre vacío → 400
    @Test void shouldReturn400_whenNameIsBlank()
}
```

### `ProductRouterTest.java` — Casos de prueba

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ProductRouterTest extends PostgresTestContainer {

    @Autowired WebTestClient webTestClient;

    // TC-P01: Agregar producto a sucursal existente → 201
    @Test void shouldAddProduct_returns201()

    // TC-P02: Sucursal no existe → 404
    @Test void shouldReturn404_whenBranchNotFound()

    // TC-P03: Eliminar producto existente → 204
    @Test void shouldDeleteProduct_returns204()

    // TC-P04: Eliminar producto no existente → 404
    @Test void shouldReturn404_whenProductNotFoundOnDelete()

    // TC-P05: Actualizar stock → 200
    @Test void shouldUpdateStock_returns200()

    // TC-P06: Stock negativo → 400
    @Test void shouldReturn400_whenStockIsNegative()
}
```

---

## 10. Criterios de aceptación

| # | Criterio | Verificación |
|---|---|---|
| AC-01 | `POST /franchises` crea y retorna 201 | Test TC-F01 pasa |
| AC-02 | `POST /franchises` retorna 409 si el nombre existe | Test TC-F02 pasa |
| AC-03 | `POST /branches` crea y retorna 201 | Test TC-B01 pasa |
| AC-04 | `POST /branches` retorna 404 si la franquicia no existe | Test TC-B02 pasa |
| AC-05 | `POST /products` crea y retorna 201 | Test TC-P01 pasa |
| AC-06 | `DELETE /products/{id}` elimina y retorna 204 | Test TC-P03 pasa |
| AC-07 | `PATCH /products/{id}/stock` actualiza y retorna 200 | Test TC-P05 pasa |
| AC-08 | Ningún método usa `.block()` fuera del startup | Inspección de código / ArchUnit |
| AC-09 | Todos los tests pasan con `mvn test` | `BUILD SUCCESS` sin tests saltados |
| AC-10 | Cobertura mínima del 80% en `application/service` | JaCoCo report |

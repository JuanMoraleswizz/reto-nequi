package com.nequi.franchises.infrastructure.adapter.in.web;

import com.nequi.franchises.shared.PostgresTestContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TopStockRouterTest {

    @Autowired
    WebTestClient webTestClient;

    @BeforeAll
    static void startContainer() {
        PostgreSQLContainer<?> container = PostgresTestContainer.getInstance();
        System.setProperty("R2DBC_URL", "r2dbc:postgresql://" + container.getHost() + ":" + container.getMappedPort(5432) + "/" + container.getDatabaseName());
        System.setProperty("FLYWAY_URL", container.getJdbcUrl());
        System.setProperty("POSTGRES_USER", container.getUsername());
        System.setProperty("POSTGRES_PASSWORD", container.getPassword());
    }

    private String createFranchise() {
        return webTestClient.post().uri("/api/v1/franchises")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "TS Franchise " + UUID.randomUUID()))
            .exchange().expectStatus().isCreated()
            .returnResult(Map.class).getResponseBody().blockFirst().get("id").toString();
    }

    private String createBranch(String franchiseId) {
        return webTestClient.post().uri("/api/v1/franchises/{fId}/branches", franchiseId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "Branch " + UUID.randomUUID()))
            .exchange().expectStatus().isCreated()
            .returnResult(Map.class).getResponseBody().blockFirst().get("id").toString();
    }

    private void addProduct(String franchiseId, String branchId, String name, int stock) {
        webTestClient.post().uri("/api/v1/franchises/{fId}/branches/{bId}/products", franchiseId, branchId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", name, "stock", stock))
            .exchange().expectStatus().isCreated();
    }

    // TC-TS01: Franquicia con sucursales y productos → lista correcta
    @Test
    void shouldReturnTopStockPerBranch_returns200() {
        String fId = createFranchise();
        String b1 = createBranch(fId);
        String b2 = createBranch(fId);
        addProduct(fId, b1, "P1-low", 10);
        addProduct(fId, b1, "P1-high", 150);
        addProduct(fId, b2, "P2-only", 320);

        webTestClient.get().uri("/api/v1/franchises/{fId}/top-stock", fId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2);
    }

    // TC-TS02: Sucursal sin productos no aparece en el resultado
    @Test
    void shouldExcludeBranchesWithNoProducts() {
        String fId = createFranchise();
        String b1 = createBranch(fId);
        createBranch(fId); // b2 sin productos
        addProduct(fId, b1, "Solo producto", 50);

        webTestClient.get().uri("/api/v1/franchises/{fId}/top-stock", fId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.length()").isEqualTo(1);
    }

    // TC-TS03: Franquicia no encontrada → 404 con ProblemDetail
    @Test
    void shouldReturn404ProblemDetail_whenFranchiseNotFound() {
        webTestClient.get().uri("/api/v1/franchises/{fId}/top-stock", UUID.randomUUID())
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.status").isEqualTo(404);
    }

    // TC-TS04: Cada sucursal aparece una sola vez (DISTINCT ON)
    @Test
    void shouldReturnOnlyOneProductPerBranch() {
        String fId = createFranchise();
        String b1 = createBranch(fId);
        addProduct(fId, b1, "PA", 5);
        addProduct(fId, b1, "PB", 50);
        addProduct(fId, b1, "PC", 500);

        webTestClient.get().uri("/api/v1/franchises/{fId}/top-stock", fId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].stock").isEqualTo(500);
    }
}

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
class ProductRouterTest {

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

    private String[] setupBranch() {
        String franchiseName = "Franquicia Prod " + UUID.randomUUID();
        String franchiseId = webTestClient.post().uri("/api/v1/franchises")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", franchiseName))
            .exchange().expectStatus().isCreated()
            .returnResult(Map.class).getResponseBody().blockFirst().get("id").toString();

        String branchId = webTestClient.post().uri("/api/v1/franchises/{fId}/branches", franchiseId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "Sucursal Test"))
            .exchange().expectStatus().isCreated()
            .returnResult(Map.class).getResponseBody().blockFirst().get("id").toString();

        return new String[]{franchiseId, branchId};
    }

    @Test
    void tc_p01_addProduct_returns201() {
        String[] ids = setupBranch();
        webTestClient.post().uri("/api/v1/franchises/{fId}/branches/{bId}/products", ids[0], ids[1])
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "Producto A", "stock", 100))
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.id").isNotEmpty()
            .jsonPath("$.stock").isEqualTo(100);
    }

    @Test
    void tc_p02_addProduct_branchNotFound_returns404() {
        String franchiseId = webTestClient.post().uri("/api/v1/franchises")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "F404 " + UUID.randomUUID()))
            .exchange().expectStatus().isCreated()
            .returnResult(Map.class).getResponseBody().blockFirst().get("id").toString();

        webTestClient.post().uri("/api/v1/franchises/{fId}/branches/{bId}/products", franchiseId, UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "P", "stock", 5))
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void tc_p03_addProduct_negativeStock_returns400() {
        String[] ids = setupBranch();
        webTestClient.post().uri("/api/v1/franchises/{fId}/branches/{bId}/products", ids[0], ids[1])
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "Bad", "stock", -1))
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void tc_p04_updateStock_returns200() {
        String[] ids = setupBranch();
        String productId = webTestClient.post().uri("/api/v1/franchises/{fId}/branches/{bId}/products", ids[0], ids[1])
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "P Stock", "stock", 10))
            .exchange().expectStatus().isCreated()
            .returnResult(Map.class).getResponseBody().blockFirst().get("id").toString();

        webTestClient.patch().uri("/api/v1/franchises/{fId}/branches/{bId}/products/{pId}/stock", ids[0], ids[1], productId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("stock", 99))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.stock").isEqualTo(99);
    }

    @Test
    void tc_p05_deleteProduct_returns204() {
        String[] ids = setupBranch();
        String productId = webTestClient.post().uri("/api/v1/franchises/{fId}/branches/{bId}/products", ids[0], ids[1])
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "P Delete", "stock", 5))
            .exchange().expectStatus().isCreated()
            .returnResult(Map.class).getResponseBody().blockFirst().get("id").toString();

        webTestClient.delete().uri("/api/v1/franchises/{fId}/branches/{bId}/products/{pId}", ids[0], ids[1], productId)
            .exchange()
            .expectStatus().isNoContent();
    }
}

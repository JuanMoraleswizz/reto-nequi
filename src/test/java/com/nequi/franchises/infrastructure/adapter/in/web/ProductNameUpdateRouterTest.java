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
class ProductNameUpdateRouterTest {

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

    private String[] setupAll() {
        String franchiseId = webTestClient.post().uri("/api/v1/franchises")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "PN Franchise " + UUID.randomUUID()))
            .exchange().expectStatus().isCreated()
            .returnResult(Map.class).getResponseBody().blockFirst().get("id").toString();

        String branchId = webTestClient.post().uri("/api/v1/franchises/{fId}/branches", franchiseId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "PN Branch"))
            .exchange().expectStatus().isCreated()
            .returnResult(Map.class).getResponseBody().blockFirst().get("id").toString();

        String productId = webTestClient.post().uri("/api/v1/franchises/{fId}/branches/{bId}/products", franchiseId, branchId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "Producto Original", "stock", 50))
            .exchange().expectStatus().isCreated()
            .returnResult(Map.class).getResponseBody().blockFirst().get("id").toString();

        return new String[]{franchiseId, branchId, productId};
    }

    @Test
    void shouldUpdateProductName_returns200() {
        String[] ids = setupAll();
        String newName = "Producto Updated " + UUID.randomUUID();

        webTestClient.patch().uri("/api/v1/franchises/{fId}/branches/{bId}/products/{pId}/name", ids[0], ids[1], ids[2])
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", newName))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo(newName);
    }

    @Test
    void shouldReturn404_whenProductNotFound() {
        String[] ids = setupAll();

        webTestClient.patch().uri("/api/v1/franchises/{fId}/branches/{bId}/products/{pId}/name", ids[0], ids[1], UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "Ghost"))
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }
}

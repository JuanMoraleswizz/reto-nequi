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
class BranchRouterTest {

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

    private String createFranchise(String name) {
        return webTestClient.post().uri("/api/v1/franchises")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", name))
            .exchange()
            .expectStatus().isCreated()
            .returnResult(Map.class)
            .getResponseBody()
            .blockFirst()
            .get("id").toString();
    }

    @Test
    void tc_b01_createBranch_returns201() {
        String franchiseId = createFranchise("Franquicia Branch Test " + UUID.randomUUID());
        webTestClient.post().uri("/api/v1/franchises/{fId}/branches", franchiseId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "Sucursal A"))
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.id").isNotEmpty()
            .jsonPath("$.franchiseId").isEqualTo(franchiseId);
    }

    @Test
    void tc_b02_createBranch_franchiseNotFound_returns404() {
        webTestClient.post().uri("/api/v1/franchises/{fId}/branches", UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "Sucursal X"))
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void tc_b03_listBranches_returns200() {
        String franchiseId = createFranchise("Franquicia List " + UUID.randomUUID());
        webTestClient.get().uri("/api/v1/franchises/{fId}/branches", franchiseId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray();
    }
}

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
class FranchiseRouterTest {

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

    @Test
    void tc_f01_createFranchise_returns201() {
        webTestClient.post().uri("/api/v1/franchises")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "Franquicia Test " + UUID.randomUUID()))
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.id").isNotEmpty()
            .jsonPath("$.name").isNotEmpty();
    }

    @Test
    void tc_f02_createFranchise_duplicateName_returns409() {
        String name = "Franquicia Duplicate " + UUID.randomUUID();
        webTestClient.post().uri("/api/v1/franchises")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", name))
            .exchange()
            .expectStatus().isCreated();

        webTestClient.post().uri("/api/v1/franchises")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", name))
            .exchange()
            .expectStatus().isEqualTo(409);
    }

    @Test
    void tc_f03_listFranchises_returns200() {
        webTestClient.get().uri("/api/v1/franchises")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray();
    }
}

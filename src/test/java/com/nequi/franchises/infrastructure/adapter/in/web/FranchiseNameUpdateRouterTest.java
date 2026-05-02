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
class FranchiseNameUpdateRouterTest {

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
            .exchange().expectStatus().isCreated()
            .returnResult(Map.class).getResponseBody().blockFirst().get("id").toString();
    }

    // TC-FN01: Actualizar nombre de franquicia existente → 200
    @Test
    void shouldUpdateFranchiseName_returns200() {
        String franchiseId = createFranchise("FN Original " + UUID.randomUUID());
        String newName = "FN Updated " + UUID.randomUUID();

        webTestClient.patch().uri("/api/v1/franchises/{id}/name", franchiseId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", newName))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo(newName);
    }

    // TC-FN02: Franquicia no encontrada → 404 con ProblemDetail
    @Test
    void shouldReturn404ProblemDetail_whenFranchiseNotFound() {
        webTestClient.patch().uri("/api/v1/franchises/{id}/name", UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "Whatever"))
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.title").isEqualTo("Franchise Not Found");
    }

    // TC-FN03: Nombre duplicado → 409 con ProblemDetail
    @Test
    void shouldReturn409ProblemDetail_whenNameAlreadyExists() {
        String existingName = "FN Existing " + UUID.randomUUID();
        createFranchise(existingName);
        String franchiseId = createFranchise("FN ToRename " + UUID.randomUUID());

        webTestClient.patch().uri("/api/v1/franchises/{id}/name", franchiseId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", existingName))
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.status").isEqualTo(409);
    }

    // TC-FN04: Nombre vacío → 400 (deserialización o validación)
    @Test
    void shouldReturn400ProblemDetail_whenNameIsBlank() {
        String franchiseId = createFranchise("FN Blank " + UUID.randomUUID());

        webTestClient.patch().uri("/api/v1/franchises/{id}/name", franchiseId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"name\":\"\"}")
            .exchange()
            .expectStatus().isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST.value());
    }
}

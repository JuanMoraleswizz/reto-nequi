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
class GlobalErrorHandlerTest {

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

    // TC-EH01: Respuesta de error tiene Content-Type application/problem+json
    @Test
    void shouldReturnProblemJsonContentType_on404() {
        webTestClient.get().uri("/api/v1/franchises/{id}", UUID.randomUUID())
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    // TC-EH02: Body tiene campos type, title, status, detail
    @Test
    void shouldReturnProblemDetailFields_on404() {
        webTestClient.get().uri("/api/v1/franchises/{id}", UUID.randomUUID())
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.type").isNotEmpty()
            .jsonPath("$.title").isEqualTo("Franchise Not Found")
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.detail").isNotEmpty();
    }

    // TC-EH03: 409 retorna status 409 y título correcto
    @Test
    void shouldReturn409_withCorrectTitle() {
        String name = "EH Conflict " + UUID.randomUUID();
        webTestClient.post().uri("/api/v1/franchises")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", name))
            .exchange().expectStatus().isCreated();

        webTestClient.post().uri("/api/v1/franchises")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", name))
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.title").isEqualTo("Franchise Name Conflict");
    }

    // TC-EH04: 400 retorna status 400 y detalle con el valor inválido
    @Test
    void shouldReturn400_withInvalidValueInDetail() {
        String franchiseId = webTestClient.post().uri("/api/v1/franchises")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "EH400 " + UUID.randomUUID()))
            .exchange().expectStatus().isCreated()
            .returnResult(Map.class).getResponseBody().blockFirst().get("id").toString();

        String branchId = webTestClient.post().uri("/api/v1/franchises/{fId}/branches", franchiseId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "B"))
            .exchange().expectStatus().isCreated()
            .returnResult(Map.class).getResponseBody().blockFirst().get("id").toString();

        webTestClient.post().uri("/api/v1/franchises/{fId}/branches/{bId}/products", franchiseId, branchId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("name", "Bad", "stock", -5))
            .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.detail").value(detail -> {
                assert detail.toString().contains("-5");
            });
    }
}

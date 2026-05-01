package com.nequi.franchises.infrastructure.adapter.in.web;

import com.nequi.franchises.shared.PostgresTestContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class FranchiseRouterTest {

    @BeforeAll
    static void startContainer() {
        PostgresTestContainer.getInstance();
    }

    @Autowired
    WebTestClient webTestClient;

    // TC-F01: POST /api/v1/franchises → 201
    @Test
    void tc_f01_createFranchise_returns201() {
        webTestClient.post().uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Franquicia Test TC-F01\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.name").isEqualTo("Franquicia Test TC-F01");
    }

    // TC-F02: POST /api/v1/franchises duplicado → 409
    @Test
    void tc_f02_createFranchise_duplicateName_returns409() {
        String body = "{\"name\":\"Franquicia Dup TC-F02\"}";
        webTestClient.post().uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post().uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    // TC-F03: GET /api/v1/franchises → 200 con lista
    @Test
    void tc_f03_listFranchises_returns200() {
        webTestClient.get().uri("/api/v1/franchises")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray();
    }
}

package com.nequi.franchises.infrastructure.adapter.in.web;

import com.nequi.franchises.shared.PostgresTestContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BranchRouterTest {

    @BeforeAll
    static void startContainer() {
        PostgresTestContainer.getInstance();
    }

    @Autowired
    WebTestClient webTestClient;

    private Long createFranchise(String name) {
        return webTestClient.post().uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"" + name + "\"}")
                .exchange()
                .expectStatus().isCreated()
                .returnResult(Long.class)
                .getResponseBody()
                .blockFirst();
    }

    // TC-B01: POST /api/v1/franchises/{id}/branches → 201
    @Test
    @Order(1)
    void tc_b01_createBranch_returns201() {
        // Create franchise first
        Long franchiseId = webTestClient.post().uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Franchise for Branch TC-B01\"}")
                .exchange()
                .expectStatus().isCreated()
                .returnResult(java.util.Map.class)
                .getResponseBody()
                .map(m -> ((Number) m.get("id")).longValue())
                .blockFirst();

        webTestClient.post().uri("/api/v1/franchises/{fid}/branches", franchiseId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Sucursal TC-B01\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.name").isEqualTo("Sucursal TC-B01")
                .jsonPath("$.franchiseId").isEqualTo(franchiseId.intValue());
    }

    // TC-B02: POST branch on non-existing franchise → 404
    @Test
    @Order(2)
    void tc_b02_createBranch_franchiseNotFound_returns404() {
        webTestClient.post().uri("/api/v1/franchises/99999/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Sucursal TC-B02\"}")
                .exchange()
                .expectStatus().isNotFound();
    }

    // TC-B03: GET /api/v1/franchises/{id}/branches → 200 con lista
    @Test
    @Order(3)
    void tc_b03_listBranches_returns200() {
        Long franchiseId = webTestClient.post().uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Franchise for List TC-B03\"}")
                .exchange()
                .expectStatus().isCreated()
                .returnResult(java.util.Map.class)
                .getResponseBody()
                .map(m -> ((Number) m.get("id")).longValue())
                .blockFirst();

        webTestClient.get().uri("/api/v1/franchises/{fid}/branches", franchiseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray();
    }
}

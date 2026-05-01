package com.nequi.franchises.infrastructure.adapter.in.web;

import com.nequi.franchises.shared.PostgresTestContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
class ProductRouterTest {

    @BeforeAll
    static void startContainer() {
        PostgresTestContainer.getInstance();
    }

    @Autowired
    WebTestClient webTestClient;

    private Long setupBranch() {
        Long franchiseId = webTestClient.post().uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"F-Product-" + System.nanoTime() + "\"}")
                .exchange()
                .expectStatus().isCreated()
                .returnResult(java.util.Map.class)
                .getResponseBody()
                .map(m -> ((Number) m.get("id")).longValue())
                .blockFirst();

        return webTestClient.post().uri("/api/v1/franchises/{fid}/branches", franchiseId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"B-" + System.nanoTime() + "\"}")
                .exchange()
                .expectStatus().isCreated()
                .returnResult(java.util.Map.class)
                .getResponseBody()
                .map(m -> ((Number) m.get("id")).longValue())
                .blockFirst();
    }

    // TC-P01: POST product → 201
    @Test
    @Order(1)
    void tc_p01_addProduct_returns201() {
        Long branchId = setupBranch();
        webTestClient.post().uri("/api/v1/branches/{bid}/products", branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Producto TC-P01\",\"stock\":10}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.name").isEqualTo("Producto TC-P01")
                .jsonPath("$.stock").isEqualTo(10);
    }

    // TC-P02: POST product on non-existing branch → 404
    @Test
    @Order(2)
    void tc_p02_addProduct_branchNotFound_returns404() {
        webTestClient.post().uri("/api/v1/branches/99999/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Producto TC-P02\",\"stock\":10}")
                .exchange()
                .expectStatus().isNotFound();
    }

    // TC-P03: POST product negative stock → 400
    @Test
    @Order(3)
    void tc_p03_addProduct_negativeStock_returns400() {
        Long branchId = setupBranch();
        webTestClient.post().uri("/api/v1/branches/{bid}/products", branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Producto TC-P03\",\"stock\":-1}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    // TC-P04: PUT stock → 200
    @Test
    @Order(4)
    void tc_p04_updateStock_returns200() {
        Long branchId = setupBranch();
        Long productId = webTestClient.post().uri("/api/v1/branches/{bid}/products", branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Producto TC-P04\",\"stock\":5}")
                .exchange()
                .expectStatus().isCreated()
                .returnResult(java.util.Map.class)
                .getResponseBody()
                .map(m -> ((Number) m.get("id")).longValue())
                .blockFirst();

        webTestClient.put().uri("/api/v1/products/{pid}/stock", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":99}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.stock").isEqualTo(99);
    }

    // TC-P05: DELETE product → 204
    @Test
    @Order(5)
    void tc_p05_deleteProduct_returns204() {
        Long branchId = setupBranch();
        Long productId = webTestClient.post().uri("/api/v1/branches/{bid}/products", branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Producto TC-P05\",\"stock\":5}")
                .exchange()
                .expectStatus().isCreated()
                .returnResult(java.util.Map.class)
                .getResponseBody()
                .map(m -> ((Number) m.get("id")).longValue())
                .blockFirst();

        webTestClient.delete().uri("/api/v1/products/{pid}", productId)
                .exchange()
                .expectStatus().isNoContent();
    }

    // TC-P06: GET top-stock → 501 (placeholder)
    @Test
    @Order(6)
    void tc_p06_topStock_returns501() {
        webTestClient.get().uri("/api/v1/franchises/1/top-stock")
                .exchange()
                .expectStatus().isEqualTo(501);
    }
}

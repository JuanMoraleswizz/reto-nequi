package com.nequi.franchises.infrastructure.adapter.in.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ProductRouter {

    @Bean
    public RouterFunction<ServerResponse> productRoutes(ProductHandler handler) {
        return RouterFunctions.route()
            .POST("/api/v1/franchises/{franchiseId}/branches/{branchId}/products", handler::create)
            .GET("/api/v1/franchises/{franchiseId}/branches/{branchId}/products", handler::listByBranch)
            .DELETE("/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}", handler::delete)
            .PATCH("/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock", handler::updateStock)
            .PATCH("/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name", handler::updateProductName)
            .build();
    }
}

package com.nequi.franchises.infrastructure.adapter.in.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class FranchiseRouter {

    @Bean
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler handler) {
        return RouterFunctions.route()
            .POST("/api/v1/franchises", handler::create)
            .GET("/api/v1/franchises", handler::list)
            .GET("/api/v1/franchises/{franchiseId}", handler::getById)
            .PATCH("/api/v1/franchises/{franchiseId}/name", handler::updateFranchiseName)
            .GET("/api/v1/franchises/{franchiseId}/top-stock", handler::getTopStock)
            .build();
    }
}

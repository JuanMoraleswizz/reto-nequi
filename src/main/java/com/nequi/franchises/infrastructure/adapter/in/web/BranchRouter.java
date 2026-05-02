package com.nequi.franchises.infrastructure.adapter.in.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class BranchRouter {

    @Bean
    public RouterFunction<ServerResponse> branchRoutes(BranchHandler handler) {
        return RouterFunctions.route()
            .POST("/api/v1/franchises/{franchiseId}/branches", handler::create)
            .GET("/api/v1/franchises/{franchiseId}/branches", handler::listByFranchise)
            .GET("/api/v1/franchises/{franchiseId}/branches/{branchId}", handler::getById)
            .PATCH("/api/v1/franchises/{franchiseId}/branches/{branchId}/name", handler::updateBranchName)
            .build();
    }
}

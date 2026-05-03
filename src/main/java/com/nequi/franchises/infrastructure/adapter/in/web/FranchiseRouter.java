package com.nequi.franchises.infrastructure.adapter.in.web;

import com.nequi.franchises.infrastructure.adapter.in.web.dto.CreateFranchiseRequest;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.TopStockResponse;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.UpdateNameRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class FranchiseRouter {

    @RouterOperations({
        @RouterOperation(
            path = "/api/v1/franchises",
            method = RequestMethod.POST,
            beanClass = FranchiseHandler.class,
            beanMethod = "create",
            operation = @Operation(
                operationId = "createFranchise",
                summary = "Crear franquicia",
                tags = {"Franchises"},
                requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateFranchiseRequest.class))),
                responses = @ApiResponse(responseCode = "201", description = "Franquicia creada")
            )
        ),
        @RouterOperation(
            path = "/api/v1/franchises",
            method = RequestMethod.GET,
            beanClass = FranchiseHandler.class,
            beanMethod = "list",
            operation = @Operation(
                operationId = "listFranchises",
                summary = "Listar franquicias",
                tags = {"Franchises"},
                responses = @ApiResponse(responseCode = "200", description = "Lista de franquicias")
            )
        ),
        @RouterOperation(
            path = "/api/v1/franchises/{franchiseId}",
            method = RequestMethod.GET,
            beanClass = FranchiseHandler.class,
            beanMethod = "getById",
            operation = @Operation(
                operationId = "getFranchiseById",
                summary = "Obtener franquicia por ID",
                tags = {"Franchises"},
                parameters = @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                responses = @ApiResponse(responseCode = "200", description = "Franquicia encontrada")
            )
        ),
        @RouterOperation(
            path = "/api/v1/franchises/{franchiseId}/name",
            method = RequestMethod.PATCH,
            beanClass = FranchiseHandler.class,
            beanMethod = "updateFranchiseName",
            operation = @Operation(
                operationId = "updateFranchiseName",
                summary = "Actualizar nombre de franquicia",
                tags = {"Franchises"},
                parameters = @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UpdateNameRequest.class))),
                responses = @ApiResponse(responseCode = "200", description = "Nombre actualizado")
            )
        ),
        @RouterOperation(
            path = "/api/v1/franchises/{franchiseId}/top-stock",
            method = RequestMethod.GET,
            beanClass = FranchiseHandler.class,
            beanMethod = "getTopStock",
            operation = @Operation(
                operationId = "getTopStock",
                summary = "Producto con mayor stock por sucursal",
                tags = {"Franchises"},
                parameters = @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                responses = @ApiResponse(responseCode = "200", description = "Top stock por sucursal",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TopStockResponse.class))))
            )
        )
    })
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

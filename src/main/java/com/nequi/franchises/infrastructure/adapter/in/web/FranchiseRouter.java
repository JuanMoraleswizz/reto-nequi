package com.nequi.franchises.infrastructure.adapter.in.web;

import com.nequi.franchises.domain.model.Franchise;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.CreateFranchiseRequest;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class FranchiseRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/franchises",
                    method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class,
                    beanMethod = "create",
                    operation = @io.swagger.v3.oas.annotations.Operation(
                            operationId = "createFranchise",
                            summary = "EP-01 Crear franquicia",
                            tags = {"Franquicias"},
                            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                    content = @io.swagger.v3.oas.annotations.media.Content(
                                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                                    implementation = CreateFranchiseRequest.class))),
                            responses = {
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                                            description = "Franquicia creada",
                                            content = @io.swagger.v3.oas.annotations.media.Content(
                                                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                                                            implementation = Franchise.class))),
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                                            description = "Nombre ya existe")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/franchises",
                    method = RequestMethod.GET,
                    beanClass = FranchiseHandler.class,
                    beanMethod = "list",
                    operation = @io.swagger.v3.oas.annotations.Operation(
                            operationId = "listFranchises",
                            summary = "EP-02 Listar franquicias",
                            tags = {"Franquicias"},
                            responses = {
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                                            description = "Lista de franquicias")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/franchises/{id}",
                    method = RequestMethod.GET,
                    beanClass = FranchiseHandler.class,
                    beanMethod = "getById",
                    operation = @io.swagger.v3.oas.annotations.Operation(
                            operationId = "getFranchiseById",
                            summary = "Obtener franquicia por ID",
                            tags = {"Franquicias"},
                            parameters = @io.swagger.v3.oas.annotations.Parameter(name = "id", in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH),
                            responses = {
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                                            description = "Franquicia encontrada"),
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                                            description = "No encontrada")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler handler) {
        return RouterFunctions.route()
                .POST("/api/v1/franchises", accept(MediaType.APPLICATION_JSON), handler::create)
                .GET("/api/v1/franchises", handler::list)
                .GET("/api/v1/franchises/{id}", handler::getById)
                .build();
    }
}

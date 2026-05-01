package com.nequi.franchises.infrastructure.adapter.in.web;

import com.nequi.franchises.domain.model.Branch;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.CreateBranchRequest;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class BranchRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/franchises/{franchiseId}/branches",
                    method = RequestMethod.POST,
                    beanClass = BranchHandler.class,
                    beanMethod = "create",
                    operation = @io.swagger.v3.oas.annotations.Operation(
                            operationId = "createBranch",
                            summary = "EP-03 Agregar sucursal a franquicia",
                            tags = {"Sucursales"},
                            parameters = @io.swagger.v3.oas.annotations.Parameter(name = "franchiseId", in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH),
                            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                    content = @io.swagger.v3.oas.annotations.media.Content(
                                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                                    implementation = CreateBranchRequest.class))),
                            responses = {
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                                            description = "Sucursal creada",
                                            content = @io.swagger.v3.oas.annotations.media.Content(
                                                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                                                            implementation = Branch.class))),
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                                            description = "Franquicia no encontrada")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/franchises/{franchiseId}/branches",
                    method = RequestMethod.GET,
                    beanClass = BranchHandler.class,
                    beanMethod = "listByFranchise",
                    operation = @io.swagger.v3.oas.annotations.Operation(
                            operationId = "listBranches",
                            summary = "Listar sucursales de una franquicia",
                            tags = {"Sucursales"},
                            parameters = @io.swagger.v3.oas.annotations.Parameter(name = "franchiseId", in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH),
                            responses = {
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                                            description = "Lista de sucursales")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/branches/{id}",
                    method = RequestMethod.GET,
                    beanClass = BranchHandler.class,
                    beanMethod = "getById",
                    operation = @io.swagger.v3.oas.annotations.Operation(
                            operationId = "getBranchById",
                            summary = "Obtener sucursal por ID",
                            tags = {"Sucursales"},
                            parameters = @io.swagger.v3.oas.annotations.Parameter(name = "id", in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH),
                            responses = {
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                                            description = "Sucursal encontrada"),
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                                            description = "No encontrada")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> branchRoutes(BranchHandler handler) {
        return RouterFunctions.route()
                .POST("/api/v1/franchises/{franchiseId}/branches", accept(MediaType.APPLICATION_JSON), handler::create)
                .GET("/api/v1/franchises/{franchiseId}/branches", handler::listByFranchise)
                .GET("/api/v1/branches/{id}", handler::getById)
                .build();
    }
}

package com.nequi.franchises.infrastructure.adapter.in.web;

import com.nequi.franchises.infrastructure.adapter.in.web.dto.CreateBranchRequest;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.UpdateNameRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class BranchRouter {

    @RouterOperations({
        @RouterOperation(
            path = "/api/v1/franchises/{franchiseId}/branches",
            method = RequestMethod.POST,
            beanClass = BranchHandler.class,
            beanMethod = "create",
            operation = @Operation(
                operationId = "createBranch",
                summary = "Crear sucursal",
                tags = {"Branches"},
                parameters = @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateBranchRequest.class))),
                responses = @ApiResponse(responseCode = "201", description = "Sucursal creada")
            )
        ),
        @RouterOperation(
            path = "/api/v1/franchises/{franchiseId}/branches",
            method = RequestMethod.GET,
            beanClass = BranchHandler.class,
            beanMethod = "listByFranchise",
            operation = @Operation(
                operationId = "listBranches",
                summary = "Listar sucursales de una franquicia",
                tags = {"Branches"},
                parameters = @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                responses = @ApiResponse(responseCode = "200", description = "Lista de sucursales")
            )
        ),
        @RouterOperation(
            path = "/api/v1/franchises/{franchiseId}/branches/{branchId}",
            method = RequestMethod.GET,
            beanClass = BranchHandler.class,
            beanMethod = "getById",
            operation = @Operation(
                operationId = "getBranchById",
                summary = "Obtener sucursal por ID",
                tags = {"Branches"},
                parameters = {
                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true)
                },
                responses = @ApiResponse(responseCode = "200", description = "Sucursal encontrada")
            )
        ),
        @RouterOperation(
            path = "/api/v1/franchises/{franchiseId}/branches/{branchId}/name",
            method = RequestMethod.PATCH,
            beanClass = BranchHandler.class,
            beanMethod = "updateBranchName",
            operation = @Operation(
                operationId = "updateBranchName",
                summary = "Actualizar nombre de sucursal",
                tags = {"Branches"},
                parameters = {
                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true)
                },
                requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UpdateNameRequest.class))),
                responses = @ApiResponse(responseCode = "200", description = "Nombre actualizado")
            )
        )
    })
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

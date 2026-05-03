package com.nequi.franchises.infrastructure.adapter.in.web;

import com.nequi.franchises.infrastructure.adapter.in.web.dto.CreateProductRequest;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.UpdateNameRequest;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.UpdateStockRequest;
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
public class ProductRouter {

    @RouterOperations({
        @RouterOperation(
            path = "/api/v1/franchises/{franchiseId}/branches/{branchId}/products",
            method = RequestMethod.POST,
            beanClass = ProductHandler.class,
            beanMethod = "create",
            operation = @Operation(
                operationId = "createProduct",
                summary = "Crear producto",
                tags = {"Products"},
                parameters = {
                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true)
                },
                requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateProductRequest.class))),
                responses = @ApiResponse(responseCode = "201", description = "Producto creado")
            )
        ),
        @RouterOperation(
            path = "/api/v1/franchises/{franchiseId}/branches/{branchId}/products",
            method = RequestMethod.GET,
            beanClass = ProductHandler.class,
            beanMethod = "listByBranch",
            operation = @Operation(
                operationId = "listProducts",
                summary = "Listar productos de una sucursal",
                tags = {"Products"},
                parameters = {
                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true)
                },
                responses = @ApiResponse(responseCode = "200", description = "Lista de productos")
            )
        ),
        @RouterOperation(
            path = "/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}",
            method = RequestMethod.DELETE,
            beanClass = ProductHandler.class,
            beanMethod = "delete",
            operation = @Operation(
                operationId = "deleteProduct",
                summary = "Eliminar producto",
                tags = {"Products"},
                parameters = {
                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "productId", in = ParameterIn.PATH, required = true)
                },
                responses = @ApiResponse(responseCode = "204", description = "Producto eliminado")
            )
        ),
        @RouterOperation(
            path = "/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock",
            method = RequestMethod.PATCH,
            beanClass = ProductHandler.class,
            beanMethod = "updateStock",
            operation = @Operation(
                operationId = "updateProductStock",
                summary = "Actualizar stock de producto",
                tags = {"Products"},
                parameters = {
                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "productId", in = ParameterIn.PATH, required = true)
                },
                requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UpdateStockRequest.class))),
                responses = @ApiResponse(responseCode = "200", description = "Stock actualizado")
            )
        ),
        @RouterOperation(
            path = "/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name",
            method = RequestMethod.PATCH,
            beanClass = ProductHandler.class,
            beanMethod = "updateProductName",
            operation = @Operation(
                operationId = "updateProductName",
                summary = "Actualizar nombre de producto",
                tags = {"Products"},
                parameters = {
                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "productId", in = ParameterIn.PATH, required = true)
                },
                requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UpdateNameRequest.class))),
                responses = @ApiResponse(responseCode = "200", description = "Nombre actualizado")
            )
        )
    })
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

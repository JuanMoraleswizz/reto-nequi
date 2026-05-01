package com.nequi.franchises.infrastructure.adapter.in.web;

import com.nequi.franchises.domain.model.Product;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.CreateProductRequest;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.UpdateStockRequest;
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
public class ProductRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/branches/{branchId}/products",
                    method = RequestMethod.POST,
                    beanClass = ProductHandler.class,
                    beanMethod = "create",
                    operation = @io.swagger.v3.oas.annotations.Operation(
                            operationId = "addProduct",
                            summary = "EP-04 Agregar producto a sucursal",
                            tags = {"Productos"},
                            parameters = @io.swagger.v3.oas.annotations.Parameter(name = "branchId", in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH),
                            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                    content = @io.swagger.v3.oas.annotations.media.Content(
                                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                                    implementation = CreateProductRequest.class))),
                            responses = {
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                                            description = "Producto creado",
                                            content = @io.swagger.v3.oas.annotations.media.Content(
                                                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                                                            implementation = Product.class))),
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                                            description = "Sucursal no encontrada")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/products/{productId}",
                    method = RequestMethod.DELETE,
                    beanClass = ProductHandler.class,
                    beanMethod = "delete",
                    operation = @io.swagger.v3.oas.annotations.Operation(
                            operationId = "deleteProduct",
                            summary = "EP-05 Eliminar producto de sucursal",
                            tags = {"Productos"},
                            parameters = @io.swagger.v3.oas.annotations.Parameter(name = "productId", in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH),
                            responses = {
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204",
                                            description = "Producto eliminado"),
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                                            description = "Producto no encontrado")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/products/{productId}/stock",
                    method = RequestMethod.PUT,
                    beanClass = ProductHandler.class,
                    beanMethod = "updateStock",
                    operation = @io.swagger.v3.oas.annotations.Operation(
                            operationId = "updateStock",
                            summary = "EP-07 Modificar stock de un producto",
                            tags = {"Productos"},
                            parameters = @io.swagger.v3.oas.annotations.Parameter(name = "productId", in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH),
                            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                    content = @io.swagger.v3.oas.annotations.media.Content(
                                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                                    implementation = UpdateStockRequest.class))),
                            responses = {
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                                            description = "Stock actualizado"),
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                                            description = "Stock inválido"),
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                                            description = "Producto no encontrado")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/branches/{branchId}/products",
                    method = RequestMethod.GET,
                    beanClass = ProductHandler.class,
                    beanMethod = "listByBranch",
                    operation = @io.swagger.v3.oas.annotations.Operation(
                            operationId = "listProducts",
                            summary = "Listar productos de una sucursal",
                            tags = {"Productos"},
                            parameters = @io.swagger.v3.oas.annotations.Parameter(name = "branchId", in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH),
                            responses = {
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                                            description = "Lista de productos")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/franchises/{franchiseId}/top-stock",
                    method = RequestMethod.GET,
                    beanClass = ProductHandler.class,
                    beanMethod = "topStock",
                    operation = @io.swagger.v3.oas.annotations.Operation(
                            operationId = "topStockByFranchise",
                            summary = "EP-06 Producto con mayor stock por sucursal (placeholder)",
                            tags = {"Productos"},
                            parameters = @io.swagger.v3.oas.annotations.Parameter(name = "franchiseId", in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH),
                            responses = {
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "501",
                                            description = "No implementado aún — disponible en SPEC-003")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> productRoutes(ProductHandler handler) {
        return RouterFunctions.route()
                .POST("/api/v1/branches/{branchId}/products", accept(MediaType.APPLICATION_JSON), handler::create)
                .DELETE("/api/v1/products/{productId}", handler::delete)
                .PUT("/api/v1/products/{productId}/stock", accept(MediaType.APPLICATION_JSON), handler::updateStock)
                .GET("/api/v1/branches/{branchId}/products", handler::listByBranch)
                .GET("/api/v1/franchises/{franchiseId}/top-stock", handler::topStock)
                .build();
    }
}

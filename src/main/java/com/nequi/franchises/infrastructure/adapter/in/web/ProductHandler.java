package com.nequi.franchises.infrastructure.adapter.in.web;

import com.nequi.franchises.application.port.in.ProductUseCase;
import com.nequi.franchises.domain.model.Product;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.CreateProductRequest;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.UpdateNameRequest;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.UpdateStockRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ProductHandler {

    private final ProductUseCase productUseCase;

    public ProductHandler(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        UUID branchId = UUID.fromString(request.pathVariable("branchId"));
        return request.bodyToMono(CreateProductRequest.class)
            .flatMap(body -> productUseCase.addProduct(branchId, body.getName(), body.getStock()))
            .flatMap(product -> ServerResponse.status(201).bodyValue(product));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        UUID productId = UUID.fromString(request.pathVariable("productId"));
        return productUseCase.deleteProduct(productId)
            .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> updateStock(ServerRequest request) {
        UUID productId = UUID.fromString(request.pathVariable("productId"));
        return request.bodyToMono(UpdateStockRequest.class)
            .flatMap(body -> productUseCase.updateStock(productId, body.getStock()))
            .flatMap(product -> ServerResponse.ok().bodyValue(product));
    }

    public Mono<ServerResponse> listByBranch(ServerRequest request) {
        UUID branchId = UUID.fromString(request.pathVariable("branchId"));
        return ServerResponse.ok().body(productUseCase.listProductsByBranch(branchId), Product.class);
    }

    public Mono<ServerResponse> updateProductName(ServerRequest request) {
        UUID franchiseId = UUID.fromString(request.pathVariable("franchiseId"));
        UUID branchId    = UUID.fromString(request.pathVariable("branchId"));
        UUID productId   = UUID.fromString(request.pathVariable("productId"));
        return request.bodyToMono(UpdateNameRequest.class)
            .flatMap(body -> productUseCase.updateProductName(franchiseId, branchId, productId, body.name()))
            .flatMap(product -> ServerResponse.ok().bodyValue(product));
    }
}

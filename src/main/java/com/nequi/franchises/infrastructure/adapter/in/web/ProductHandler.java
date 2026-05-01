package com.nequi.franchises.infrastructure.adapter.in.web;

import com.nequi.franchises.application.exception.BranchNotFoundException;
import com.nequi.franchises.application.exception.InvalidStockException;
import com.nequi.franchises.application.exception.ProductNotFoundException;
import com.nequi.franchises.application.port.in.ProductUseCase;
import com.nequi.franchises.domain.model.Product;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.CreateProductRequest;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.UpdateStockRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ProductHandler {

    private final ProductUseCase productUseCase;

    public ProductHandler(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        Long branchId = Long.parseLong(request.pathVariable("branchId"));
        return request.bodyToMono(CreateProductRequest.class)
                .flatMap(req -> productUseCase.addProduct(branchId, req.getName(), req.getStock()))
                .flatMap(product -> ServerResponse.status(HttpStatus.CREATED).bodyValue(product))
                .onErrorResume(BranchNotFoundException.class, e -> ServerResponse.notFound().build())
                .onErrorResume(InvalidStockException.class,
                        e -> ServerResponse.badRequest().bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Long productId = Long.parseLong(request.pathVariable("productId"));
        return productUseCase.deleteProduct(productId)
                .then(ServerResponse.noContent().build())
                .onErrorResume(ProductNotFoundException.class, e -> ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> updateStock(ServerRequest request) {
        Long productId = Long.parseLong(request.pathVariable("productId"));
        return request.bodyToMono(UpdateStockRequest.class)
                .flatMap(req -> productUseCase.updateStock(productId, req.getStock()))
                .flatMap(product -> ServerResponse.ok().bodyValue(product))
                .onErrorResume(ProductNotFoundException.class, e -> ServerResponse.notFound().build())
                .onErrorResume(InvalidStockException.class,
                        e -> ServerResponse.badRequest().bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> listByBranch(ServerRequest request) {
        Long branchId = Long.parseLong(request.pathVariable("branchId"));
        return ServerResponse.ok().body(productUseCase.listProductsByBranch(branchId), Product.class)
                .onErrorResume(BranchNotFoundException.class, e -> ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> topStock(ServerRequest request) {
        return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}

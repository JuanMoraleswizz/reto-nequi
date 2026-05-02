package com.nequi.franchises.application.port.in;

import com.nequi.franchises.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductUseCase {
    Mono<Product> addProduct(UUID branchId, String name, Integer stock);
    Mono<Void> deleteProduct(UUID productId);
    Mono<Product> updateStock(UUID productId, Integer stock);
    Flux<Product> listProductsByBranch(UUID branchId);
    Mono<Product> getProductById(UUID id);
    Mono<Product> updateProductName(UUID franchiseId, UUID branchId, UUID productId, String newName);
}

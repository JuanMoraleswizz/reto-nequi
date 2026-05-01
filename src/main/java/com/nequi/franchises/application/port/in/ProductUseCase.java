package com.nequi.franchises.application.port.in;

import com.nequi.franchises.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductUseCase {
    Mono<Product> addProduct(Long branchId, String name, Integer stock);
    Mono<Void> deleteProduct(Long productId);
    Mono<Product> updateStock(Long productId, Integer stock);
    Flux<Product> listProductsByBranch(Long branchId);
    Mono<Product> getProductById(Long id);
}

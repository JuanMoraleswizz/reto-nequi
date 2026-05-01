package com.nequi.franchises.application.port.out;

import com.nequi.franchises.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository {
    Mono<Product> save(Product product);
    Mono<Product> findById(Long id);
    Flux<Product> findByBranchId(Long branchId);
    Mono<Void> deleteById(Long id);
}

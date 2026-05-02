package com.nequi.franchises.application.port.out;

import com.nequi.franchises.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductRepository {
    Mono<Product> save(Product product);
    Mono<Product> findById(UUID id);
    Flux<Product> findByBranchId(UUID branchId);
    Mono<Void> deleteById(UUID id);
}

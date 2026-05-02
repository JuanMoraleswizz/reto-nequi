package com.nequi.franchises.application.port.out;

import com.nequi.franchises.domain.model.Franchise;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FranchiseRepository {
    Mono<Franchise> save(Franchise franchise);
    Mono<Franchise> findById(UUID id);
    Flux<Franchise> findAll();
    Mono<Boolean> existsByName(String name);
}

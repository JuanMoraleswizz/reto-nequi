package com.nequi.franchises.application.port.in;

import com.nequi.franchises.domain.model.Franchise;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FranchiseUseCase {
    Mono<Franchise> createFranchise(String name);
    Flux<Franchise> listFranchises();
    Mono<Franchise> getFranchiseById(Long id);
}

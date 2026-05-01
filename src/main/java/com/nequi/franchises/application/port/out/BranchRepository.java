package com.nequi.franchises.application.port.out;

import com.nequi.franchises.domain.model.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BranchRepository {
    Mono<Branch> save(Branch branch);
    Mono<Branch> findById(Long id);
    Flux<Branch> findByFranchiseId(Long franchiseId);
}

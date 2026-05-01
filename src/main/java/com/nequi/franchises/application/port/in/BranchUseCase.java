package com.nequi.franchises.application.port.in;

import com.nequi.franchises.domain.model.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BranchUseCase {
    Mono<Branch> createBranch(Long franchiseId, String name);
    Flux<Branch> listBranchesByFranchise(Long franchiseId);
    Mono<Branch> getBranchById(Long id);
}

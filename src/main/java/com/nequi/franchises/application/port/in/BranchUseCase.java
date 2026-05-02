package com.nequi.franchises.application.port.in;

import com.nequi.franchises.domain.model.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BranchUseCase {
    Mono<Branch> createBranch(UUID franchiseId, String name);
    Flux<Branch> listBranchesByFranchise(UUID franchiseId);
    Mono<Branch> getBranchById(UUID id);
    Mono<Branch> updateBranchName(UUID franchiseId, UUID branchId, String newName);
}

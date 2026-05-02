package com.nequi.franchises.application.port.in;

import com.nequi.franchises.domain.model.Franchise;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.TopStockResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FranchiseUseCase {
    Mono<Franchise> createFranchise(String name);
    Flux<Franchise> listFranchises();
    Mono<Franchise> getFranchiseById(UUID id);
    Mono<Franchise> updateFranchiseName(UUID franchiseId, String newName);
    Flux<TopStockResponse> getTopStockPerBranch(UUID franchiseId);
}

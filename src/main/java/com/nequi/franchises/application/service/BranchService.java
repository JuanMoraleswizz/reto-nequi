package com.nequi.franchises.application.service;

import com.nequi.franchises.application.exception.BranchNotFoundException;
import com.nequi.franchises.application.exception.FranchiseNotFoundException;
import com.nequi.franchises.application.port.in.BranchUseCase;
import com.nequi.franchises.application.port.out.BranchRepository;
import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.model.Branch;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BranchService implements BranchUseCase {

    private final BranchRepository branchRepository;
    private final FranchiseRepository franchiseRepository;

    public BranchService(BranchRepository branchRepository, FranchiseRepository franchiseRepository) {
        this.branchRepository = branchRepository;
        this.franchiseRepository = franchiseRepository;
    }

    @Override
    public Mono<Branch> createBranch(Long franchiseId, String name) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
                .flatMap(f -> branchRepository.save(
                        Branch.builder().name(name).franchiseId(franchiseId).build()));
    }

    @Override
    public Flux<Branch> listBranchesByFranchise(Long franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
                .thenMany(branchRepository.findByFranchiseId(franchiseId));
    }

    @Override
    public Mono<Branch> getBranchById(Long id) {
        return branchRepository.findById(id)
                .switchIfEmpty(Mono.error(new BranchNotFoundException(id)));
    }
}

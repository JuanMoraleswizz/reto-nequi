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

import java.util.UUID;

@Service
public class BranchService implements BranchUseCase {

    private final BranchRepository branchRepository;
    private final FranchiseRepository franchiseRepository;

    public BranchService(BranchRepository branchRepository, FranchiseRepository franchiseRepository) {
        this.branchRepository = branchRepository;
        this.franchiseRepository = franchiseRepository;
    }

    @Override
    public Mono<Branch> createBranch(UUID franchiseId, String name) {
        return franchiseRepository.findById(franchiseId)
            .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
            .flatMap(franchise -> {
                Branch branch = Branch.builder().franchiseId(franchiseId).name(name).build();
                return branchRepository.save(branch);
            });
    }

    @Override
    public Flux<Branch> listBranchesByFranchise(UUID franchiseId) {
        return franchiseRepository.findById(franchiseId)
            .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
            .flatMapMany(f -> branchRepository.findByFranchiseId(franchiseId));
    }

    @Override
    public Mono<Branch> getBranchById(UUID id) {
        return branchRepository.findById(id)
            .switchIfEmpty(Mono.error(new BranchNotFoundException(id)));
    }

    @Override
    public Mono<Branch> updateBranchName(UUID franchiseId, UUID branchId, String newName) {
        return franchiseRepository.findById(franchiseId)
            .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
            .flatMap(franchise -> branchRepository.findById(branchId))
            .switchIfEmpty(Mono.error(new BranchNotFoundException(branchId)))
            .flatMap(branch -> {
                branch.setName(newName);
                return branchRepository.save(branch);
            });
    }
}

package com.nequi.franchises.application.service;

import com.nequi.franchises.application.exception.FranchiseNameAlreadyExistsException;
import com.nequi.franchises.application.exception.FranchiseNotFoundException;
import com.nequi.franchises.application.port.in.FranchiseUseCase;
import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.model.Franchise;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.TopStockResponse;
import com.nequi.franchises.infrastructure.adapter.out.persistence.ProductCustomRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class FranchiseService implements FranchiseUseCase {

    private final FranchiseRepository franchiseRepository;
    private final ProductCustomRepository productCustomRepository;

    public FranchiseService(FranchiseRepository franchiseRepository,
                            ProductCustomRepository productCustomRepository) {
        this.franchiseRepository = franchiseRepository;
        this.productCustomRepository = productCustomRepository;
    }

    @Override
    public Mono<Franchise> createFranchise(String name) {
        return franchiseRepository.existsByName(name)
            .flatMap(exists -> {
                if (exists) return Mono.error(new FranchiseNameAlreadyExistsException(name));
                Franchise franchise = Franchise.builder().name(name).build();
                return franchiseRepository.save(franchise);
            });
    }

    @Override
    public Flux<Franchise> listFranchises() {
        return franchiseRepository.findAll();
    }

    @Override
    public Mono<Franchise> getFranchiseById(UUID id) {
        return franchiseRepository.findById(id)
            .switchIfEmpty(Mono.error(new FranchiseNotFoundException(id)));
    }

    @Override
    public Mono<Franchise> updateFranchiseName(UUID franchiseId, String newName) {
        return franchiseRepository.findById(franchiseId)
            .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
            .flatMap(franchise -> franchiseRepository.existsByName(newName)
                .flatMap(exists -> {
                    if (exists) return Mono.error(new FranchiseNameAlreadyExistsException(newName));
                    franchise.setName(newName);
                    return franchiseRepository.save(franchise);
                })
            );
    }

    @Override
    public Flux<TopStockResponse> getTopStockPerBranch(UUID franchiseId) {
        return franchiseRepository.findById(franchiseId)
            .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
            .flatMapMany(franchise -> productCustomRepository.findTopStockPerBranch(franchiseId));
    }
}

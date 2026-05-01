package com.nequi.franchises.application.service;

import com.nequi.franchises.application.exception.FranchiseNameAlreadyExistsException;
import com.nequi.franchises.application.exception.FranchiseNotFoundException;
import com.nequi.franchises.application.port.in.FranchiseUseCase;
import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.model.Franchise;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FranchiseService implements FranchiseUseCase {

    private final FranchiseRepository franchiseRepository;

    public FranchiseService(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = franchiseRepository;
    }

    @Override
    public Mono<Franchise> createFranchise(String name) {
        return franchiseRepository.existsByName(name)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new FranchiseNameAlreadyExistsException(name));
                    }
                    return franchiseRepository.save(Franchise.builder().name(name).build());
                });
    }

    @Override
    public Flux<Franchise> listFranchises() {
        return franchiseRepository.findAll();
    }

    @Override
    public Mono<Franchise> getFranchiseById(Long id) {
        return franchiseRepository.findById(id)
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException(id)));
    }
}

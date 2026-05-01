package com.nequi.franchises.infrastructure.adapter.in.web;

import com.nequi.franchises.application.exception.FranchiseNameAlreadyExistsException;
import com.nequi.franchises.application.exception.FranchiseNotFoundException;
import com.nequi.franchises.application.port.in.FranchiseUseCase;
import com.nequi.franchises.domain.model.Franchise;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.CreateFranchiseRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class FranchiseHandler {

    private final FranchiseUseCase franchiseUseCase;

    public FranchiseHandler(FranchiseUseCase franchiseUseCase) {
        this.franchiseUseCase = franchiseUseCase;
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(CreateFranchiseRequest.class)
                .flatMap(req -> franchiseUseCase.createFranchise(req.getName()))
                .flatMap(franchise -> ServerResponse.status(HttpStatus.CREATED).bodyValue(franchise))
                .onErrorResume(FranchiseNameAlreadyExistsException.class,
                        e -> ServerResponse.status(HttpStatus.CONFLICT).bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> list(ServerRequest request) {
        return ServerResponse.ok().body(franchiseUseCase.listFranchises(), Franchise.class);
    }

    public Mono<ServerResponse> getById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return franchiseUseCase.getFranchiseById(id)
                .flatMap(franchise -> ServerResponse.ok().bodyValue(franchise))
                .onErrorResume(FranchiseNotFoundException.class,
                        e -> ServerResponse.notFound().build());
    }
}

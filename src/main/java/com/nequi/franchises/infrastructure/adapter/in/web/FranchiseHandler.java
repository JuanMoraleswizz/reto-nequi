package com.nequi.franchises.infrastructure.adapter.in.web;

import com.nequi.franchises.application.port.in.FranchiseUseCase;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.CreateFranchiseRequest;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.TopStockResponse;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.UpdateNameRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class FranchiseHandler {

    private final FranchiseUseCase franchiseUseCase;

    public FranchiseHandler(FranchiseUseCase franchiseUseCase) {
        this.franchiseUseCase = franchiseUseCase;
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(CreateFranchiseRequest.class)
            .flatMap(body -> franchiseUseCase.createFranchise(body.getName()))
            .flatMap(franchise -> ServerResponse.status(201).bodyValue(franchise));
    }

    public Mono<ServerResponse> list(ServerRequest request) {
        return ServerResponse.ok().body(franchiseUseCase.listFranchises(), com.nequi.franchises.domain.model.Franchise.class);
    }

    public Mono<ServerResponse> getById(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("franchiseId"));
        return franchiseUseCase.getFranchiseById(id)
            .flatMap(franchise -> ServerResponse.ok().bodyValue(franchise));
    }

    public Mono<ServerResponse> updateFranchiseName(ServerRequest request) {
        UUID franchiseId = UUID.fromString(request.pathVariable("franchiseId"));
        return request.bodyToMono(UpdateNameRequest.class)
            .flatMap(body -> {
                if (body.name() == null || body.name().isBlank()) {
                    return ServerResponse.badRequest().build();
                }
                return franchiseUseCase.updateFranchiseName(franchiseId, body.name())
                    .flatMap(franchise -> ServerResponse.ok().bodyValue(franchise));
            });
    }

    public Mono<ServerResponse> getTopStock(ServerRequest request) {
        UUID franchiseId = UUID.fromString(request.pathVariable("franchiseId"));
        return ServerResponse.ok()
            .body(franchiseUseCase.getTopStockPerBranch(franchiseId), TopStockResponse.class);
    }
}

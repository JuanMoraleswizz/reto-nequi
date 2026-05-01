package com.nequi.franchises.infrastructure.adapter.in.web;

import com.nequi.franchises.application.exception.BranchNotFoundException;
import com.nequi.franchises.application.exception.FranchiseNotFoundException;
import com.nequi.franchises.application.port.in.BranchUseCase;
import com.nequi.franchises.domain.model.Branch;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.CreateBranchRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class BranchHandler {

    private final BranchUseCase branchUseCase;

    public BranchHandler(BranchUseCase branchUseCase) {
        this.branchUseCase = branchUseCase;
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        Long franchiseId = Long.parseLong(request.pathVariable("franchiseId"));
        return request.bodyToMono(CreateBranchRequest.class)
                .flatMap(req -> branchUseCase.createBranch(franchiseId, req.getName()))
                .flatMap(branch -> ServerResponse.status(HttpStatus.CREATED).bodyValue(branch))
                .onErrorResume(FranchiseNotFoundException.class,
                        e -> ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> listByFranchise(ServerRequest request) {
        Long franchiseId = Long.parseLong(request.pathVariable("franchiseId"));
        return ServerResponse.ok().body(branchUseCase.listBranchesByFranchise(franchiseId), Branch.class)
                .onErrorResume(FranchiseNotFoundException.class,
                        e -> ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return branchUseCase.getBranchById(id)
                .flatMap(branch -> ServerResponse.ok().bodyValue(branch))
                .onErrorResume(BranchNotFoundException.class,
                        e -> ServerResponse.notFound().build());
    }
}

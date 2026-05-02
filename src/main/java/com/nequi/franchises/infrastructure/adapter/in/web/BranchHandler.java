package com.nequi.franchises.infrastructure.adapter.in.web;

import com.nequi.franchises.application.port.in.BranchUseCase;
import com.nequi.franchises.domain.model.Branch;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.CreateBranchRequest;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.UpdateNameRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class BranchHandler {

    private final BranchUseCase branchUseCase;

    public BranchHandler(BranchUseCase branchUseCase) {
        this.branchUseCase = branchUseCase;
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        UUID franchiseId = UUID.fromString(request.pathVariable("franchiseId"));
        return request.bodyToMono(CreateBranchRequest.class)
            .flatMap(body -> branchUseCase.createBranch(franchiseId, body.getName()))
            .flatMap(branch -> ServerResponse.status(201).bodyValue(branch));
    }

    public Mono<ServerResponse> listByFranchise(ServerRequest request) {
        UUID franchiseId = UUID.fromString(request.pathVariable("franchiseId"));
        return ServerResponse.ok().body(branchUseCase.listBranchesByFranchise(franchiseId), Branch.class);
    }

    public Mono<ServerResponse> getById(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("branchId"));
        return branchUseCase.getBranchById(id)
            .flatMap(branch -> ServerResponse.ok().bodyValue(branch));
    }

    public Mono<ServerResponse> updateBranchName(ServerRequest request) {
        UUID franchiseId = UUID.fromString(request.pathVariable("franchiseId"));
        UUID branchId    = UUID.fromString(request.pathVariable("branchId"));
        return request.bodyToMono(UpdateNameRequest.class)
            .flatMap(body -> branchUseCase.updateBranchName(franchiseId, branchId, body.name()))
            .flatMap(branch -> ServerResponse.ok().bodyValue(branch));
    }
}

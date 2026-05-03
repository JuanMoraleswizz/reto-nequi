package com.nequi.franchises.application.service;

import com.nequi.franchises.application.exception.BranchNotFoundException;
import com.nequi.franchises.application.exception.FranchiseNotFoundException;
import com.nequi.franchises.application.port.out.BranchRepository;
import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.model.Branch;
import com.nequi.franchises.domain.model.Franchise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private FranchiseRepository franchiseRepository;

    private BranchService branchService;

    @BeforeEach
    void setUp() {
        branchService = new BranchService(branchRepository, franchiseRepository);
    }

    // ─── createBranch ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createBranch: franquicia existente → guarda y retorna la sucursal")
    void createBranch_whenFranchiseExists_shouldSaveAndReturn() {
        UUID franchiseId = UUID.randomUUID();
        String branchName = "Sucursal Norte";
        Franchise franchise = Franchise.builder().id(franchiseId).name("Franquicia A").build();
        Branch saved = Branch.builder().id(UUID.randomUUID()).franchiseId(franchiseId).name(branchName).build();

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(franchise));
        when(branchRepository.save(any(Branch.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(branchService.createBranch(franchiseId, branchName))
            .expectNextMatches(b -> branchName.equals(b.getName()) && franchiseId.equals(b.getFranchiseId()))
            .verifyComplete();
    }

    @Test
    @DisplayName("createBranch: franquicia no existe → FranchiseNotFoundException")
    void createBranch_whenFranchiseNotFound_shouldThrow() {
        UUID franchiseId = UUID.randomUUID();

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.empty());

        StepVerifier.create(branchService.createBranch(franchiseId, "Sucursal"))
            .expectError(FranchiseNotFoundException.class)
            .verify();
    }

    // ─── listBranchesByFranchise ──────────────────────────────────────────────

    @Test
    @DisplayName("listBranchesByFranchise: franquicia existe → retorna sucursales")
    void listBranchesByFranchise_whenFranchiseExists_shouldReturnBranches() {
        UUID franchiseId = UUID.randomUUID();
        Franchise franchise = Franchise.builder().id(franchiseId).name("Franquicia A").build();
        Branch b1 = Branch.builder().id(UUID.randomUUID()).franchiseId(franchiseId).name("Norte").build();
        Branch b2 = Branch.builder().id(UUID.randomUUID()).franchiseId(franchiseId).name("Sur").build();

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(franchise));
        when(branchRepository.findByFranchiseId(franchiseId)).thenReturn(Flux.just(b1, b2));

        StepVerifier.create(branchService.listBranchesByFranchise(franchiseId))
            .expectNext(b1)
            .expectNext(b2)
            .verifyComplete();
    }

    @Test
    @DisplayName("listBranchesByFranchise: franquicia no existe → FranchiseNotFoundException")
    void listBranchesByFranchise_whenFranchiseNotFound_shouldThrow() {
        UUID franchiseId = UUID.randomUUID();

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.empty());

        StepVerifier.create(branchService.listBranchesByFranchise(franchiseId))
            .expectError(FranchiseNotFoundException.class)
            .verify();
    }

    // ─── getBranchById ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getBranchById: ID existente → retorna la sucursal")
    void getBranchById_whenExists_shouldReturn() {
        UUID id = UUID.randomUUID();
        Branch branch = Branch.builder().id(id).name("Sucursal X").build();

        when(branchRepository.findById(id)).thenReturn(Mono.just(branch));

        StepVerifier.create(branchService.getBranchById(id))
            .expectNextMatches(b -> id.equals(b.getId()))
            .verifyComplete();
    }

    @Test
    @DisplayName("getBranchById: ID inexistente → BranchNotFoundException")
    void getBranchById_whenNotFound_shouldThrow() {
        UUID id = UUID.randomUUID();

        when(branchRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(branchService.getBranchById(id))
            .expectError(BranchNotFoundException.class)
            .verify();
    }

    // ─── updateBranchName ─────────────────────────────────────────────────────

    @Test
    @DisplayName("updateBranchName: franquicia y sucursal existen → actualiza y retorna")
    void updateBranchName_whenBothExist_shouldUpdate() {
        UUID franchiseId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        String newName = "Sucursal Actualizada";
        Franchise franchise = Franchise.builder().id(franchiseId).name("Franquicia A").build();
        Branch existing = Branch.builder().id(branchId).franchiseId(franchiseId).name("Viejo Nombre").build();
        Branch updated  = Branch.builder().id(branchId).franchiseId(franchiseId).name(newName).build();

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(franchise));
        when(branchRepository.findById(branchId)).thenReturn(Mono.just(existing));
        when(branchRepository.save(any(Branch.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(branchService.updateBranchName(franchiseId, branchId, newName))
            .expectNextMatches(b -> newName.equals(b.getName()))
            .verifyComplete();
    }

    @Test
    @DisplayName("updateBranchName: franquicia no existe → FranchiseNotFoundException")
    void updateBranchName_whenFranchiseNotFound_shouldThrow() {
        UUID franchiseId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.empty());

        StepVerifier.create(branchService.updateBranchName(franchiseId, branchId, "Nuevo"))
            .expectError(FranchiseNotFoundException.class)
            .verify();
    }

    @Test
    @DisplayName("updateBranchName: sucursal no existe → BranchNotFoundException")
    void updateBranchName_whenBranchNotFound_shouldThrow() {
        UUID franchiseId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        Franchise franchise = Franchise.builder().id(franchiseId).name("Franquicia A").build();

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(franchise));
        when(branchRepository.findById(branchId)).thenReturn(Mono.empty());

        StepVerifier.create(branchService.updateBranchName(franchiseId, branchId, "Nuevo"))
            .expectError(BranchNotFoundException.class)
            .verify();
    }
}

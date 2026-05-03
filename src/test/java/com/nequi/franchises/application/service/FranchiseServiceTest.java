package com.nequi.franchises.application.service;

import com.nequi.franchises.application.exception.FranchiseNameAlreadyExistsException;
import com.nequi.franchises.application.exception.FranchiseNotFoundException;
import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.model.Franchise;
import com.nequi.franchises.infrastructure.adapter.in.web.dto.TopStockResponse;
import com.nequi.franchises.application.port.out.TopStockRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseServiceTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private TopStockRepository productCustomRepository;

    private FranchiseService franchiseService;

    @BeforeEach
    void setUp() {
        franchiseService = new FranchiseService(franchiseRepository, productCustomRepository);
    }

    // ─── createFranchise ───────────────────────────────────────────────────────

    @Test
    @DisplayName("createFranchise: nombre nuevo → guarda y retorna la franquicia")
    void createFranchise_whenNameIsNew_shouldSaveAndReturn() {
        String name = "McDonald's";
        Franchise saved = Franchise.builder().id(UUID.randomUUID()).name(name).build();

        when(franchiseRepository.existsByName(name)).thenReturn(Mono.just(false));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(franchiseService.createFranchise(name))
            .expectNextMatches(f -> name.equals(f.getName()))
            .verifyComplete();
    }

    @Test
    @DisplayName("createFranchise: nombre duplicado → FranchiseNameAlreadyExistsException")
    void createFranchise_whenNameExists_shouldThrowFranchiseNameAlreadyExistsException() {
        String name = "McDonald's";

        when(franchiseRepository.existsByName(name)).thenReturn(Mono.just(true));

        StepVerifier.create(franchiseService.createFranchise(name))
            .expectError(FranchiseNameAlreadyExistsException.class)
            .verify();
    }

    // ─── listFranchises ────────────────────────────────────────────────────────

    @Test
    @DisplayName("listFranchises: retorna todas las franquicias del repositorio")
    void listFranchises_shouldReturnAll() {
        Franchise f1 = Franchise.builder().id(UUID.randomUUID()).name("A").build();
        Franchise f2 = Franchise.builder().id(UUID.randomUUID()).name("B").build();

        when(franchiseRepository.findAll()).thenReturn(Flux.just(f1, f2));

        StepVerifier.create(franchiseService.listFranchises())
            .expectNext(f1)
            .expectNext(f2)
            .verifyComplete();
    }

    @Test
    @DisplayName("listFranchises: repositorio vacío → Flux vacío")
    void listFranchises_whenEmpty_shouldReturnEmptyFlux() {
        when(franchiseRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(franchiseService.listFranchises())
            .verifyComplete();
    }

    // ─── getFranchiseById ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getFranchiseById: ID existente → retorna la franquicia")
    void getFranchiseById_whenExists_shouldReturn() {
        UUID id = UUID.randomUUID();
        Franchise franchise = Franchise.builder().id(id).name("KFC").build();

        when(franchiseRepository.findById(id)).thenReturn(Mono.just(franchise));

        StepVerifier.create(franchiseService.getFranchiseById(id))
            .expectNextMatches(f -> id.equals(f.getId()))
            .verifyComplete();
    }

    @Test
    @DisplayName("getFranchiseById: ID inexistente → FranchiseNotFoundException")
    void getFranchiseById_whenNotFound_shouldThrowFranchiseNotFoundException() {
        UUID id = UUID.randomUUID();

        when(franchiseRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(franchiseService.getFranchiseById(id))
            .expectError(FranchiseNotFoundException.class)
            .verify();
    }

    // ─── updateFranchiseName ──────────────────────────────────────────────────

    @Test
    @DisplayName("updateFranchiseName: franquicia existe y nombre nuevo → actualiza y retorna")
    void updateFranchiseName_whenFranchiseExistsAndNewName_shouldUpdate() {
        UUID id = UUID.randomUUID();
        String newName = "Subway";
        Franchise existing = Franchise.builder().id(id).name("Old Name").build();
        Franchise updated  = Franchise.builder().id(id).name(newName).build();

        when(franchiseRepository.findById(id)).thenReturn(Mono.just(existing));
        when(franchiseRepository.existsByName(newName)).thenReturn(Mono.just(false));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(franchiseService.updateFranchiseName(id, newName))
            .expectNextMatches(f -> newName.equals(f.getName()))
            .verifyComplete();
    }

    @Test
    @DisplayName("updateFranchiseName: franquicia no existe → FranchiseNotFoundException")
    void updateFranchiseName_whenFranchiseNotFound_shouldThrow() {
        UUID id = UUID.randomUUID();

        when(franchiseRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(franchiseService.updateFranchiseName(id, "Nuevo"))
            .expectError(FranchiseNotFoundException.class)
            .verify();
    }

    @Test
    @DisplayName("updateFranchiseName: nuevo nombre ya existe → FranchiseNameAlreadyExistsException")
    void updateFranchiseName_whenNewNameAlreadyExists_shouldThrow() {
        UUID id = UUID.randomUUID();
        String newName = "Existing Name";
        Franchise existing = Franchise.builder().id(id).name("Old Name").build();

        when(franchiseRepository.findById(id)).thenReturn(Mono.just(existing));
        when(franchiseRepository.existsByName(newName)).thenReturn(Mono.just(true));

        StepVerifier.create(franchiseService.updateFranchiseName(id, newName))
            .expectError(FranchiseNameAlreadyExistsException.class)
            .verify();
    }

    // ─── getTopStockPerBranch ─────────────────────────────────────────────────

    @Test
    @DisplayName("getTopStockPerBranch: franquicia existe → retorna top stock")
    void getTopStockPerBranch_whenFranchiseExists_shouldReturnTopStock() {
        UUID franchiseId = UUID.randomUUID();
        Franchise franchise = Franchise.builder().id(franchiseId).name("Franquicia X").build();
        TopStockResponse r1 = new TopStockResponse(UUID.randomUUID(), "Sucursal A", UUID.randomUUID(), "Producto 1", 100);
        TopStockResponse r2 = new TopStockResponse(UUID.randomUUID(), "Sucursal B", UUID.randomUUID(), "Producto 2", 200);

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.just(franchise));
        when(productCustomRepository.findTopStockPerBranch(franchiseId)).thenReturn(Flux.just(r1, r2));

        StepVerifier.create(franchiseService.getTopStockPerBranch(franchiseId))
            .expectNext(r1)
            .expectNext(r2)
            .verifyComplete();
    }

    @Test
    @DisplayName("getTopStockPerBranch: franquicia no existe → FranchiseNotFoundException")
    void getTopStockPerBranch_whenFranchiseNotFound_shouldThrow() {
        UUID franchiseId = UUID.randomUUID();

        when(franchiseRepository.findById(franchiseId)).thenReturn(Mono.empty());

        StepVerifier.create(franchiseService.getTopStockPerBranch(franchiseId))
            .expectError(FranchiseNotFoundException.class)
            .verify();
    }
}

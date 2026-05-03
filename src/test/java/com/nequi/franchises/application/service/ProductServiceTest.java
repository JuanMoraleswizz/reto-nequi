package com.nequi.franchises.application.service;

import com.nequi.franchises.application.exception.BranchNotFoundException;
import com.nequi.franchises.application.exception.InvalidStockException;
import com.nequi.franchises.application.exception.ProductNotFoundException;
import com.nequi.franchises.application.port.out.BranchRepository;
import com.nequi.franchises.application.port.out.ProductRepository;
import com.nequi.franchises.domain.model.Branch;
import com.nequi.franchises.domain.model.Product;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BranchRepository branchRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, branchRepository);
    }

    // ─── addProduct ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("addProduct: sucursal existente y stock válido → guarda y retorna el producto")
    void addProduct_whenBranchExistsAndStockValid_shouldSaveAndReturn() {
        UUID branchId = UUID.randomUUID();
        String name = "Hamburguesa";
        int stock = 50;
        Branch branch = Branch.builder().id(branchId).name("Sucursal Norte").build();
        Product saved = Product.builder().id(UUID.randomUUID()).branchId(branchId).name(name).stock(stock).build();

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(productService.addProduct(branchId, name, stock))
            .expectNextMatches(p -> name.equals(p.getName()) && stock == p.getStock())
            .verifyComplete();
    }

    @Test
    @DisplayName("addProduct: stock negativo → InvalidStockException inmediato (sin consultar repo)")
    void addProduct_whenStockIsNegative_shouldThrowImmediately() {
        UUID branchId = UUID.randomUUID();

        StepVerifier.create(productService.addProduct(branchId, "Producto", -1))
            .expectError(InvalidStockException.class)
            .verify();
    }

    @Test
    @DisplayName("addProduct: stock cero → es válido, guarda el producto")
    void addProduct_whenStockIsZero_shouldBeValid() {
        UUID branchId = UUID.randomUUID();
        Branch branch = Branch.builder().id(branchId).name("Sucursal").build();
        Product saved = Product.builder().id(UUID.randomUUID()).branchId(branchId).name("Prod").stock(0).build();

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(productService.addProduct(branchId, "Prod", 0))
            .expectNextMatches(p -> p.getStock() == 0)
            .verifyComplete();
    }

    @Test
    @DisplayName("addProduct: sucursal no existe → BranchNotFoundException")
    void addProduct_whenBranchNotFound_shouldThrow() {
        UUID branchId = UUID.randomUUID();

        when(branchRepository.findById(branchId)).thenReturn(Mono.empty());

        StepVerifier.create(productService.addProduct(branchId, "Producto", 10))
            .expectError(BranchNotFoundException.class)
            .verify();
    }

    // ─── deleteProduct ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteProduct: producto existe → lo elimina y completa sin valor")
    void deleteProduct_whenExists_shouldDeleteAndComplete() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder().id(productId).name("Prod").stock(5).build();

        when(productRepository.findById(productId)).thenReturn(Mono.just(product));
        when(productRepository.deleteById(productId)).thenReturn(Mono.empty());

        StepVerifier.create(productService.deleteProduct(productId))
            .verifyComplete();
    }

    @Test
    @DisplayName("deleteProduct: producto no existe → ProductNotFoundException")
    void deleteProduct_whenNotFound_shouldThrow() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId)).thenReturn(Mono.empty());

        StepVerifier.create(productService.deleteProduct(productId))
            .expectError(ProductNotFoundException.class)
            .verify();
    }

    // ─── updateStock ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStock: stock válido y producto existente → actualiza y retorna")
    void updateStock_whenValidStockAndProductExists_shouldUpdate() {
        UUID productId = UUID.randomUUID();
        int newStock = 99;
        Product existing = Product.builder().id(productId).name("Prod").stock(10).build();
        Product updated  = Product.builder().id(productId).name("Prod").stock(newStock).build();

        when(productRepository.findById(productId)).thenReturn(Mono.just(existing));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(productService.updateStock(productId, newStock))
            .expectNextMatches(p -> p.getStock() == newStock)
            .verifyComplete();
    }

    @Test
    @DisplayName("updateStock: stock negativo → InvalidStockException inmediato")
    void updateStock_whenNegativeStock_shouldThrowImmediately() {
        UUID productId = UUID.randomUUID();

        StepVerifier.create(productService.updateStock(productId, -5))
            .expectError(InvalidStockException.class)
            .verify();
    }

    @Test
    @DisplayName("updateStock: producto no existe → ProductNotFoundException")
    void updateStock_whenProductNotFound_shouldThrow() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId)).thenReturn(Mono.empty());

        StepVerifier.create(productService.updateStock(productId, 10))
            .expectError(ProductNotFoundException.class)
            .verify();
    }

    // ─── listProductsByBranch ─────────────────────────────────────────────────

    @Test
    @DisplayName("listProductsByBranch: sucursal existe → retorna productos")
    void listProductsByBranch_whenBranchExists_shouldReturnProducts() {
        UUID branchId = UUID.randomUUID();
        Branch branch = Branch.builder().id(branchId).name("Sucursal X").build();
        Product p1 = Product.builder().id(UUID.randomUUID()).branchId(branchId).name("P1").stock(1).build();
        Product p2 = Product.builder().id(UUID.randomUUID()).branchId(branchId).name("P2").stock(2).build();

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));
        when(productRepository.findByBranchId(branchId)).thenReturn(Flux.just(p1, p2));

        StepVerifier.create(productService.listProductsByBranch(branchId))
            .expectNext(p1)
            .expectNext(p2)
            .verifyComplete();
    }

    @Test
    @DisplayName("listProductsByBranch: sucursal no existe → BranchNotFoundException")
    void listProductsByBranch_whenBranchNotFound_shouldThrow() {
        UUID branchId = UUID.randomUUID();

        when(branchRepository.findById(branchId)).thenReturn(Mono.empty());

        StepVerifier.create(productService.listProductsByBranch(branchId))
            .expectError(BranchNotFoundException.class)
            .verify();
    }

    // ─── getProductById ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getProductById: ID existente → retorna el producto")
    void getProductById_whenExists_shouldReturn() {
        UUID id = UUID.randomUUID();
        Product product = Product.builder().id(id).name("Prod").stock(5).build();

        when(productRepository.findById(id)).thenReturn(Mono.just(product));

        StepVerifier.create(productService.getProductById(id))
            .expectNextMatches(p -> id.equals(p.getId()))
            .verifyComplete();
    }

    @Test
    @DisplayName("getProductById: ID inexistente → ProductNotFoundException")
    void getProductById_whenNotFound_shouldThrow() {
        UUID id = UUID.randomUUID();

        when(productRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(productService.getProductById(id))
            .expectError(ProductNotFoundException.class)
            .verify();
    }

    // ─── updateProductName ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateProductName: sucursal y producto existen → actualiza y retorna")
    void updateProductName_whenBothExist_shouldUpdate() {
        UUID franchiseId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String newName = "Producto Actualizado";
        Branch branch = Branch.builder().id(branchId).name("Sucursal").build();
        Product existing = Product.builder().id(productId).branchId(branchId).name("Viejo").stock(10).build();
        Product updated  = Product.builder().id(productId).branchId(branchId).name(newName).stock(10).build();

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));
        when(productRepository.findById(productId)).thenReturn(Mono.just(existing));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(productService.updateProductName(franchiseId, branchId, productId, newName))
            .expectNextMatches(p -> newName.equals(p.getName()))
            .verifyComplete();
    }

    @Test
    @DisplayName("updateProductName: sucursal no existe → BranchNotFoundException")
    void updateProductName_whenBranchNotFound_shouldThrow() {
        UUID franchiseId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(branchRepository.findById(branchId)).thenReturn(Mono.empty());

        StepVerifier.create(productService.updateProductName(franchiseId, branchId, productId, "Nuevo"))
            .expectError(BranchNotFoundException.class)
            .verify();
    }

    @Test
    @DisplayName("updateProductName: producto no existe → ProductNotFoundException")
    void updateProductName_whenProductNotFound_shouldThrow() {
        UUID franchiseId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Branch branch = Branch.builder().id(branchId).name("Sucursal").build();

        when(branchRepository.findById(branchId)).thenReturn(Mono.just(branch));
        when(productRepository.findById(productId)).thenReturn(Mono.empty());

        StepVerifier.create(productService.updateProductName(franchiseId, branchId, productId, "Nuevo"))
            .expectError(ProductNotFoundException.class)
            .verify();
    }
}

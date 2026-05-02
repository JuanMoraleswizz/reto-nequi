package com.nequi.franchises.application.service;

import com.nequi.franchises.application.exception.BranchNotFoundException;
import com.nequi.franchises.application.exception.InvalidStockException;
import com.nequi.franchises.application.exception.ProductNotFoundException;
import com.nequi.franchises.application.port.in.ProductUseCase;
import com.nequi.franchises.application.port.out.BranchRepository;
import com.nequi.franchises.application.port.out.ProductRepository;
import com.nequi.franchises.domain.model.Product;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ProductService implements ProductUseCase {

    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;

    public ProductService(ProductRepository productRepository, BranchRepository branchRepository) {
        this.productRepository = productRepository;
        this.branchRepository = branchRepository;
    }

    @Override
    public Mono<Product> addProduct(UUID branchId, String name, Integer stock) {
        if (stock < 0) return Mono.error(new InvalidStockException(stock));
        return branchRepository.findById(branchId)
            .switchIfEmpty(Mono.error(new BranchNotFoundException(branchId)))
            .flatMap(branch -> {
                Product product = Product.builder().branchId(branchId).name(name).stock(stock).build();
                return productRepository.save(product);
            });
    }

    @Override
    public Mono<Void> deleteProduct(UUID productId) {
        return productRepository.findById(productId)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
            .flatMap(p -> productRepository.deleteById(productId));
    }

    @Override
    public Mono<Product> updateStock(UUID productId, Integer stock) {
        if (stock < 0) return Mono.error(new InvalidStockException(stock));
        return productRepository.findById(productId)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
            .flatMap(product -> {
                product.setStock(stock);
                return productRepository.save(product);
            });
    }

    @Override
    public Flux<Product> listProductsByBranch(UUID branchId) {
        return branchRepository.findById(branchId)
            .switchIfEmpty(Mono.error(new BranchNotFoundException(branchId)))
            .flatMapMany(b -> productRepository.findByBranchId(branchId));
    }

    @Override
    public Mono<Product> getProductById(UUID id) {
        return productRepository.findById(id)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(id)));
    }

    @Override
    public Mono<Product> updateProductName(UUID franchiseId, UUID branchId, UUID productId, String newName) {
        return branchRepository.findById(branchId)
            .switchIfEmpty(Mono.error(new BranchNotFoundException(branchId)))
            .flatMap(branch -> productRepository.findById(productId))
            .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
            .flatMap(product -> {
                product.setName(newName);
                return productRepository.save(product);
            });
    }
}

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

@Service
public class ProductService implements ProductUseCase {

    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;

    public ProductService(ProductRepository productRepository, BranchRepository branchRepository) {
        this.productRepository = productRepository;
        this.branchRepository = branchRepository;
    }

    @Override
    public Mono<Product> addProduct(Long branchId, String name, Integer stock) {
        if (stock < 0) return Mono.error(new InvalidStockException(stock));
        return branchRepository.findById(branchId)
                .switchIfEmpty(Mono.error(new BranchNotFoundException(branchId)))
                .flatMap(b -> productRepository.save(
                        Product.builder().name(name).stock(stock).branchId(branchId).build()));
    }

    @Override
    public Mono<Void> deleteProduct(Long productId) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                .flatMap(p -> productRepository.deleteById(p.getId()));
    }

    @Override
    public Mono<Product> updateStock(Long productId, Integer stock) {
        if (stock < 0) return Mono.error(new InvalidStockException(stock));
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                .flatMap(p -> {
                    p.setStock(stock);
                    return productRepository.save(p);
                });
    }

    @Override
    public Flux<Product> listProductsByBranch(Long branchId) {
        return branchRepository.findById(branchId)
                .switchIfEmpty(Mono.error(new BranchNotFoundException(branchId)))
                .thenMany(productRepository.findByBranchId(branchId));
    }

    @Override
    public Mono<Product> getProductById(Long id) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(id)));
    }
}

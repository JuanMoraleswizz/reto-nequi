package com.nequi.franchises.infrastructure.adapter.out.persistence;

import com.nequi.franchises.application.port.out.ProductRepository;
import com.nequi.franchises.domain.model.Product;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProductR2dbcRepository implements ProductRepository {

    private final R2dbcEntityTemplate template;

    public ProductR2dbcRepository(R2dbcEntityTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<Product> save(Product product) {
        if (product.getId() == null) {
            return template.insert(product);
        }
        return template.update(product);
    }

    @Override
    public Mono<Product> findById(Long id) {
        return template.selectOne(Query.query(Criteria.where("id").is(id)), Product.class);
    }

    @Override
    public Flux<Product> findByBranchId(Long branchId) {
        return template.select(Query.query(Criteria.where("branch_id").is(branchId)), Product.class);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return template.delete(Query.query(Criteria.where("id").is(id)), Product.class).then();
    }
}

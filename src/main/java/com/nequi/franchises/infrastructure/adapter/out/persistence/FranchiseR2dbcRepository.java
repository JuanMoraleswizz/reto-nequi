package com.nequi.franchises.infrastructure.adapter.out.persistence;

import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.model.Franchise;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class FranchiseR2dbcRepository implements FranchiseRepository {

    private final R2dbcEntityTemplate template;

    public FranchiseR2dbcRepository(R2dbcEntityTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return template.insert(franchise);
    }

    @Override
    public Mono<Franchise> findById(Long id) {
        return template.selectOne(Query.query(Criteria.where("id").is(id)), Franchise.class);
    }

    @Override
    public Flux<Franchise> findAll() {
        return template.select(Franchise.class).all();
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return template.exists(Query.query(Criteria.where("name").is(name)), Franchise.class);
    }
}

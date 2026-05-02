package com.nequi.franchises.infrastructure.adapter.out.persistence;

import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.model.Franchise;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class FranchiseR2dbcRepository implements FranchiseRepository {

    private final R2dbcEntityTemplate template;

    public FranchiseR2dbcRepository(R2dbcEntityTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        if (franchise.getId() == null) {
            franchise.setId(UUID.randomUUID());
            return template.insert(franchise);
        }
        return template.update(franchise);
    }

    @Override
    public Mono<Franchise> findById(UUID id) {
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

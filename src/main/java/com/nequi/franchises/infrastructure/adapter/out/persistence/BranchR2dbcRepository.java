package com.nequi.franchises.infrastructure.adapter.out.persistence;

import com.nequi.franchises.application.port.out.BranchRepository;
import com.nequi.franchises.domain.model.Branch;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class BranchR2dbcRepository implements BranchRepository {

    private final R2dbcEntityTemplate template;

    public BranchR2dbcRepository(R2dbcEntityTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<Branch> save(Branch branch) {
        if (branch.getId() == null) {
            branch.setId(UUID.randomUUID());
            return template.insert(branch);
        }
        return template.update(branch);
    }

    @Override
    public Mono<Branch> findById(UUID id) {
        return template.selectOne(Query.query(Criteria.where("id").is(id)), Branch.class);
    }

    @Override
    public Flux<Branch> findByFranchiseId(UUID franchiseId) {
        return template.select(Query.query(Criteria.where("franchise_id").is(franchiseId)), Branch.class);
    }
}

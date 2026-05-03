package com.nequi.franchises.infrastructure.adapter.out.persistence;

import com.nequi.franchises.infrastructure.adapter.in.web.dto.TopStockResponse;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public class ProductCustomRepository implements com.nequi.franchises.application.port.out.TopStockRepository {

    private final DatabaseClient databaseClient;

    public ProductCustomRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    private static final String TOP_STOCK_QUERY = """
        SELECT DISTINCT ON (b.id)
            b.id          AS branch_id,
            b.name        AS branch_name,
            p.id          AS product_id,
            p.name        AS product_name,
            p.stock
        FROM branches b
        JOIN products p ON p.branch_id = b.id
        WHERE b.franchise_id = :franchiseId
        ORDER BY b.id, p.stock DESC
        """;

    public Flux<TopStockResponse> findTopStockPerBranch(UUID franchiseId) {
        return databaseClient.sql(TOP_STOCK_QUERY)
            .bind("franchiseId", franchiseId)
            .map((row, metadata) -> new TopStockResponse(
                row.get("branch_id",    UUID.class),
                row.get("branch_name",  String.class),
                row.get("product_id",   UUID.class),
                row.get("product_name", String.class),
                row.get("stock",        Integer.class)
            ))
            .all();
    }
}

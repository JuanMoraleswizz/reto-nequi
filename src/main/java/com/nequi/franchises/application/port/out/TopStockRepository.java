package com.nequi.franchises.application.port.out;

import com.nequi.franchises.infrastructure.adapter.in.web.dto.TopStockResponse;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface TopStockRepository {
    Flux<TopStockResponse> findTopStockPerBranch(UUID franchiseId);
}

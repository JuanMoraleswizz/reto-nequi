package com.nequi.franchises.infrastructure.adapter.in.web.dto;

import java.util.UUID;

public record TopStockResponse(
    UUID   branchId,
    String branchName,
    UUID   productId,
    String productName,
    int    stock
) {}

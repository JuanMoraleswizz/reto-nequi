package com.nequi.franchises.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "Request para actualizar el stock de un producto")
public class UpdateStockRequest {

    @Min(0)
    @Schema(description = "Nuevo stock del producto", example = "200", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer stock;

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}

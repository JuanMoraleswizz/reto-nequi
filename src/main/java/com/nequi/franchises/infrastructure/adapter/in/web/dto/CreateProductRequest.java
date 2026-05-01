package com.nequi.franchises.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request para agregar un producto a una sucursal")
public class CreateProductRequest {

    @NotBlank
    @Schema(description = "Nombre del producto", example = "Producto A", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Min(0)
    @Schema(description = "Stock inicial del producto", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer stock;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}

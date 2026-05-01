package com.nequi.franchises.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("products")
@Schema(description = "Producto disponible en una sucursal")
public class Product {

    @Id
    @Schema(description = "Identificador único del producto", example = "1")
    private Long id;

    @Schema(description = "Nombre del producto", example = "Producto A")
    private String name;

    @Schema(description = "Stock disponible del producto", example = "100")
    private Integer stock;

    @Column("branch_id")
    @Schema(description = "ID de la sucursal a la que pertenece", example = "1")
    private Long branchId;

    public Product() {}

    public Product(Long id, String name, Integer stock, Long branchId) {
        this.id = id;
        this.name = name;
        this.stock = stock;
        this.branchId = branchId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private Integer stock;
        private Long branchId;
        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder stock(Integer stock) { this.stock = stock; return this; }
        public Builder branchId(Long branchId) { this.branchId = branchId; return this; }
        public Product build() { return new Product(id, name, stock, branchId); }
    }
}

package com.nequi.franchises.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("products")
public class Product {

    @Id
    private UUID id;
    private String name;
    private Integer stock;
    private UUID branchId;

    public Product() {}

    public Product(UUID id, String name, Integer stock, UUID branchId) {
        this.id = id;
        this.name = name;
        this.stock = stock;
        this.branchId = branchId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public UUID getBranchId() { return branchId; }
    public void setBranchId(UUID branchId) { this.branchId = branchId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String name;
        private Integer stock;
        private UUID branchId;
        public Builder id(UUID id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder stock(Integer stock) { this.stock = stock; return this; }
        public Builder branchId(UUID branchId) { this.branchId = branchId; return this; }
        public Product build() { return new Product(id, name, stock, branchId); }
    }
}

package com.nequi.franchises.infrastructure.adapter.in.web.dto;

public class CreateProductRequest {
    private String name;
    private Integer stock;
    public CreateProductRequest() {}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}

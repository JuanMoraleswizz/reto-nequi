package com.nequi.franchises.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("branches")
@Schema(description = "Sucursal perteneciente a una franquicia")
public class Branch {

    @Id
    @Schema(description = "Identificador único de la sucursal", example = "1")
    private Long id;

    @Schema(description = "Nombre de la sucursal", example = "Sucursal Centro")
    private String name;

    @Column("franchise_id")
    @Schema(description = "ID de la franquicia a la que pertenece", example = "1")
    private Long franchiseId;

    public Branch() {}

    public Branch(Long id, String name, Long franchiseId) {
        this.id = id;
        this.name = name;
        this.franchiseId = franchiseId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getFranchiseId() { return franchiseId; }
    public void setFranchiseId(Long franchiseId) { this.franchiseId = franchiseId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private Long franchiseId;
        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder franchiseId(Long franchiseId) { this.franchiseId = franchiseId; return this; }
        public Branch build() { return new Branch(id, name, franchiseId); }
    }
}

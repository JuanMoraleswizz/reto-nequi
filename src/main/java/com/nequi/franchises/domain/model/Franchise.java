package com.nequi.franchises.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("franchises")
@Schema(description = "Franquicia registrada en el sistema")
public class Franchise {

    @Id
    @Schema(description = "Identificador único de la franquicia", example = "1")
    private Long id;

    @Schema(description = "Nombre de la franquicia", example = "Franquicia Norte")
    private String name;

    public Franchise() {}

    public Franchise(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Franchise build() { return new Franchise(id, name); }
    }
}

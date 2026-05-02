package com.nequi.franchises.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("branches")
public class Branch {

    @Id
    private UUID id;
    private String name;
    private UUID franchiseId;

    public Branch() {}

    public Branch(UUID id, String name, UUID franchiseId) {
        this.id = id;
        this.name = name;
        this.franchiseId = franchiseId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getFranchiseId() { return franchiseId; }
    public void setFranchiseId(UUID franchiseId) { this.franchiseId = franchiseId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String name;
        private UUID franchiseId;
        public Builder id(UUID id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder franchiseId(UUID franchiseId) { this.franchiseId = franchiseId; return this; }
        public Branch build() { return new Branch(id, name, franchiseId); }
    }
}

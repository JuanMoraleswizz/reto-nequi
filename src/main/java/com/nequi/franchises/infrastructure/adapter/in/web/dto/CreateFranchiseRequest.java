package com.nequi.franchises.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request para crear una franquicia")
public class CreateFranchiseRequest {

    @NotBlank
    @Schema(description = "Nombre de la franquicia", example = "Franquicia Norte", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

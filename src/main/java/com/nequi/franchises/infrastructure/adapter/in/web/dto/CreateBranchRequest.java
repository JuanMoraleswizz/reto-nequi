package com.nequi.franchises.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request para agregar una sucursal a una franquicia")
public class CreateBranchRequest {

    @NotBlank
    @Schema(description = "Nombre de la sucursal", example = "Sucursal Centro", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

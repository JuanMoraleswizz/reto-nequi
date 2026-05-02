package com.nequi.franchises.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI franchisesOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API de Gestión de Franquicias — Nequi")
                .description("RFC-001 rev 2 · Spring WebFlux + R2DBC + PostgreSQL")
                .version("1.0.0"));
    }
}

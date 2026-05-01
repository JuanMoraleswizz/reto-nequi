package com.nequi.franchises.infrastructure.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI franchisesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Franchises API — Nequi")
                        .description("API REST reactiva para gestión de franquicias, sucursales y productos. RFC-001 rev 2.")
                        .version("0.0.1-SNAPSHOT")
                        .contact(new Contact()
                                .name("Nequi Engineering")
                                .email("dev@nequi.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("RFC-001 rev 2 — Documento de requisitos")
                        .url("https://github.com/JuanMoraleswizz/reto-nequi"));
    }
}

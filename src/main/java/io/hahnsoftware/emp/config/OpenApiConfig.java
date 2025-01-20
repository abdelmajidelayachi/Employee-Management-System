package io.hahnsoftware.emp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT token authentication");

        return new OpenAPI()
                .info(new Info()
                        .title("Employee Management System API")
                        .version("1.0.0")
                        .description("REST API for managing employee records")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your.email@example.com")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", securityScheme))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
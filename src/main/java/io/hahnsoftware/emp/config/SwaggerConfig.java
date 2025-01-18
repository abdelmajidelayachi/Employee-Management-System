package io.hahnsoftware.emp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Employee Management System API")
                        .version("3.0.0")  // Explicitly set OpenAPI version
                        .description("REST API for managing employee records")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your.email@example.com")));
    }
}
package com.tarique.bankstatement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SwaggerConfig — OpenAPI 3 configuration (springdoc).
 *
 * <p>Accessible at: http://localhost:8080/swagger-ui/index.html
 * Mirrors the reference my-project Springfox setup, upgraded to OpenAPI 3.
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .components(securityComponents())
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    private Info apiInfo() {
        return new Info()
                .title("Bank Statement API")
                .description("""
                        **Production-ready** bank statement retrieval service.
                        
                        ### Key Features
                        - 🔐 JWT Bearer authentication
                        - 📄 Paginated statements with date-range filter
                        - 📊 Summary stats (total credits / debits / net balance)
                        - ⚡ Caffeine caching — sub-millisecond response for hot accounts
                        - 🐘 PostgreSQL with composite indexes for 50M+ customer scale
                        
                        ### Primary Endpoint
                        `GET /api/v1/statements/{accountId}?from=yyyy-MM-dd&to=yyyy-MM-dd`
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Tarique")
                        .email("tarique@bank.com"))
                .license(new License().name("Proprietary"));
    }

    private List<Server> servers() {
        Server devServer = new Server()
                .url("http://localhost:8080")
                .description("Local Development Server");
        Server prodServer = new Server()
                .url("https://api.bank.com")
                .description("Production Server");
        return List.of(devServer, prodServer);
    }

    private Components securityComponents() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT token (obtained from POST /api/v1/auth/login)");

        return new Components().addSecuritySchemes("bearerAuth", bearerScheme);
    }
}

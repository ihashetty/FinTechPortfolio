package com.niveshtrack.portfolio.config;

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
 * Configures Swagger / OpenAPI 3 documentation accessible at
 * {@code /swagger-ui.html} and {@code /v3/api-docs}.
 *
 * <p>JWT Bearer token authentication is pre-configured so testers can
 * authenticate directly from the Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Value("${server.port:8081}")
    private int serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServers())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, buildSecurityScheme()));
    }

    private Info buildApiInfo() {
        return new Info()
                .title("NiveshTrack Portfolio API")
                .version("1.0.0")
                .description("""
                        **REST API for Stock Portfolio Management System**
                        
                        Built for Indian retail investors trading on NSE/BSE.
                        
                        ## Features
                        - JWT-based authentication
                        - Transaction management (BUY/SELL)
                        - Holdings calculation with weighted average cost
                        - Portfolio analytics (XIRR, sector allocation)
                        - STCG/LTCG tax summary for Indian tax rules
                        - Watchlist and price alert management
                        - Automated price updates and portfolio snapshots
                        
                        ## Authentication
                        1. Call `POST /api/auth/register` or `POST /api/auth/login`
                        2. Copy the `accessToken` from the response
                        3. Click **Authorize** and enter: `Bearer <your_token>`
                        """)
                .contact(new Contact()
                        .name("NiveshTrack Support")
                        .email("support@niveshtrack.com")
                        .url("https://niveshtrack.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private List<Server> buildServers() {
        return List.of(
                new Server().url("http://localhost:" + serverPort).description("Local Development"),
                new Server().url("https://api.niveshtrack.com").description("Production")
        );
    }

    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter your JWT access token obtained from /api/auth/login");
    }
}

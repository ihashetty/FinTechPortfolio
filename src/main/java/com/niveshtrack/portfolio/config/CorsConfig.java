package com.niveshtrack.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration that allows the React frontend (Vite on 5173, CRA on 3000)
 * to call the API during development.
 *
 * <p>In production, restrict {@code allowedOrigins} to the actual domain.
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                                "http://localhost:8080",   // React frontend (current)
                                "http://localhost:5173",   // Vite dev server
                                "http://localhost:3000",   // Create React App
                                "http://localhost:4200",   // Angular (just in case)
                                "http://localhost",        // Docker frontend (nginx on port 80)
                                "http://frontend"          // Docker internal service name
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}

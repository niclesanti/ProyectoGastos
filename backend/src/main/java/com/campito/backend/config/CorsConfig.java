package com.campito.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración adicional de CORS a nivel de Spring MVC.
 * Complementa la configuración de SecurityConfig para asegurar que todas las peticiones
 * cross-domain funcionen correctamente, especialmente para cookies de sesión.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(frontendUrl)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders(
                    "Authorization",
                    "Content-Type",
                    "X-Requested-With",
                    "Accept",
                    "Origin",
                    "Access-Control-Request-Method",
                    "Access-Control-Request-Headers",
                    "Cookie"
                )
                .exposedHeaders(
                    "Access-Control-Allow-Origin",
                    "Access-Control-Allow-Credentials",
                    "Set-Cookie"
                )
                .allowCredentials(true)
                .maxAge(3600);
    }
}

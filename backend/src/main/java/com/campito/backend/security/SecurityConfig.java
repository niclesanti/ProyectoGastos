package com.campito.backend.security;

import com.campito.backend.usuarios.service.CustomOidcUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.DispatcherType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    @Value("${springdoc.swagger-ui.enabled:false}")
    private boolean swaggerEnabled;
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    SecurityConfig(CustomOidcUserService customOidcUserService, JwtAuthenticationFilter jwtAuthenticationFilter, OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler) {
        this.customOidcUserService = customOidcUserService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(frontendUrl));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Lista base de endpoints públicos
        List<String> publicEndpoints = new ArrayList<>(Arrays.asList(
            "/",
            "/login.html",
            "/script.js",
            "/styles.css",
            "/logo.png",
            "/logo_login.png",
            "/manifest.json",
            "/api/auth/**",
            "/oauth2/**",
            "/login/oauth2/**"
        ));
        
        // Agregar endpoints de Swagger solo si está habilitado (desarrollo)
        if (swaggerEnabled) {
            publicEndpoints.addAll(Arrays.asList(
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/swagger-resources/**"
            ));
        }
        
        // Los endpoints de Actuator son públicos para permitir el monitoreo.
        // La seguridad en producción se garantiza porque el puerto 9090
        // solo es accesible localmente (127.0.0.1) dentro del servidor.
        publicEndpoints.add("/actuator/**");
        
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(Customizer.withDefaults())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
            )
            // Autenticación sin estado (stateless) usando JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Los dispatches ASYNC internos de Tomcat (al completar una respuesta SSE)
                // no tienen SecurityContext. Sin esta regla, Spring Security los bloquea con
                // AuthorizationDeniedException, causando ERR_INCOMPLETE_CHUNKED_ENCODING.
                .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                .requestMatchers(publicEndpoints.toArray(new String[0])).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOidcUserService)
                )
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureUrl(frontendUrl + "/login?error=true")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl(frontendUrl + "/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // Agregar el filtro JWT antes del filtro de autenticación de Spring
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // Para endpoints /api/** retornar 401 en vez de redirigir a OAuth2.
            // Esto evita que EventSource (SSE) reciba un redirect a Google cuando
            // el JWT no es reconocido, lo que causaría un error de CORS.
            .exceptionHandling(exceptions -> exceptions
                .defaultAuthenticationEntryPointFor(
                    (request, response, authException) ->
                        response.sendError(
                            jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED,
                            "No autenticado: " + authException.getMessage()
                        ),
                    new AntPathRequestMatcher("/api/**")
                )
            );
        
        return http.build();
    }
}

package com.campito.backend.security;

import lombok.extern.slf4j.Slf4j;
import com.campito.backend.usuarios.domain.entity.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Handler personalizado para el éxito de autenticación OAuth2.
 * Genera un token JWT y redirige al frontend con el token en la URL.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            log.debug("Response ya fue committed. No se puede redirigir a {}", targetUrl);
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Authentication authentication) {
        
        // Obtener el usuario autenticado
        CustomOAuth2User customUser = (CustomOAuth2User) authentication.getPrincipal();
        
        // Generar token JWT
        String token = jwtTokenProvider.generateToken(
            customUser.getUsuario().getId(),
            customUser.getUsuario().getEmail()
        );

        log.info("Token JWT generado exitosamente para el usuario: {}", customUser.getUsuario().getEmail());

        // Redirigir al frontend con el token como parámetro de consulta
        // El frontend lo capturará y lo guardará en localStorage
        return UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-callback")
                .queryParam("token", token)
                .build()
                .toUriString();
    }
}

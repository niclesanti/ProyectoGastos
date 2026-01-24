package com.campito.backend.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler personalizado para manejar el éxito de la autenticación OAuth2.
 * Configura las cookies de sesión con los atributos necesarios para cross-domain (SameSite=None; Secure).
 */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        logger.info("Usuario autenticado exitosamente: {}", authentication.getName());
        
        // Obtener o crear sesión
        HttpSession session = request.getSession(true);
        String sessionId = session.getId();
        
        logger.info("Session ID creado: {}", sessionId);
        logger.info("Frontend URL configurado: {}", frontendUrl);
        
        // Configurar cookie JSESSIONID manualmente con atributos cross-domain
        Cookie sessionCookie = new Cookie("JSESSIONID", sessionId);
        sessionCookie.setPath("/");
        sessionCookie.setHttpOnly(true);
        sessionCookie.setSecure(true); // HTTPS obligatorio
        sessionCookie.setMaxAge(60 * 60 * 24); // 24 horas
        
        // Agregar atributos SameSite=None para cross-domain
        String cookieValue = String.format("JSESSIONID=%s; Path=/; HttpOnly; Secure; SameSite=None", sessionId);
        response.addHeader("Set-Cookie", cookieValue);
        
        logger.info("Cookie de sesión configurada con SameSite=None y Secure");
        
        // Redirigir al frontend
        String targetUrl = frontendUrl + "/";
        logger.info("Redirigiendo a: {}", targetUrl);
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

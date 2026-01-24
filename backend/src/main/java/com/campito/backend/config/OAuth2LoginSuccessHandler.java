package com.campito.backend.config;

import jakarta.servlet.ServletException;
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
 * Spring Boot se encarga de configurar las cookies según application-prod.properties.
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
        
        logger.info("🔐 Usuario autenticado exitosamente: {}", authentication.getName());
        
        // Obtener o crear sesión (Spring Boot configurará la cookie automáticamente)
        HttpSession session = request.getSession(true);
        String sessionId = session.getId();
        
        logger.info("📝 Session ID creado: {}", sessionId);
        logger.info("🌍 Frontend URL configurado: {}", frontendUrl);
        logger.info("🍪 Cookie JSESSIONID será configurada automáticamente por Spring Boot con SameSite=None y Secure");
        
        // Redirigir a página de callback en el frontend para evitar timing issues
        String targetUrl = frontendUrl + "/oauth-callback";
        logger.info("➡️  Redirigiendo a callback page: {}", targetUrl);
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

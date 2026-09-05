package com.campito.backend.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.campito.backend.usuarios.domain.entity.Usuario;
import com.campito.backend.usuarios.domain.entity.CustomOAuth2User;
import com.campito.backend.usuarios.domain.entity.ProveedorAutenticacion;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private org.springframework.security.web.RedirectStrategy redirectStrategy;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler handler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Authentication authentication;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "frontendUrl", "http://localhost:3100");
        handler.setRedirectStrategy(redirectStrategy);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        usuario = Usuario.builder()
            .id(UUID.randomUUID())
            .email("test@test.com")
            .nombre("Test User")
            .proveedor(ProveedorAutenticacion.GOOGLE)
            .rol("USER")
            .activo(true)
            .fechaRegistro(LocalDateTime.now())
            .build();

        CustomOAuth2User oauthUser = new CustomOAuth2User(null, usuario);
        authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oauthUser);
    }

    @Test
    void determineTargetUrl_generaUrlConToken() {
        when(jwtTokenProvider.generateToken(usuario.getId(), usuario.getEmail()))
            .thenReturn("jwt-token-abc");

        String targetUrl = handler.determineTargetUrl(request, response, authentication);

        assertTrue(targetUrl.startsWith("http://localhost:3100/oauth-callback"));
        assertTrue(targetUrl.contains("token=jwt-token-abc"));
    }

    @Test
    void onAuthenticationSuccess_responseNoCommitted_redirige() throws IOException {
        when(jwtTokenProvider.generateToken(usuario.getId(), usuario.getEmail()))
            .thenReturn("jwt-token-abc");

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), anyString());
    }

    @Test
    void onAuthenticationSuccess_responseCommitted_noRedirige() throws IOException {
        response.setCommitted(true);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(redirectStrategy, never()).sendRedirect(any(), any(), anyString());
    }
}

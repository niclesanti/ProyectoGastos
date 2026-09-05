package com.campito.backend.common.test;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.campito.backend.security.JwtTokenProvider;
import com.campito.backend.security.OAuth2AuthenticationSuccessHandler;
import com.campito.backend.usuarios.repository.UsuarioRepository;
import com.campito.backend.usuarios.service.CustomOidcUserService;

/**
 * Base class for @WebMvcTest controller tests.
 * Provides common @MockBean declarations needed by SecurityConfig
 * to load the application context successfully.
 * 
 * Subclasses must also add @AutoConfigureMockMvc(addFilters = false)
 * to disable security filter enforcement in MockMvc.
 */
public abstract class BaseWebMvcTest {

    @MockBean
    protected JwtTokenProvider jwtTokenProvider;

    @MockBean
    protected OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @MockBean
    protected CustomOidcUserService customOidcUserService;

    @MockBean
    protected UsuarioRepository usuarioRepository;
}

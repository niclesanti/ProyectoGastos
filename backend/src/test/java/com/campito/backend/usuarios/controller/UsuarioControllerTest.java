package com.campito.backend.usuarios.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.campito.backend.common.test.BaseWebMvcTest;
import com.campito.backend.common.test.UsuariosTestDataFactory;
import com.campito.backend.security.SecurityService;
import com.campito.backend.usuarios.service.UsuarioService;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest extends BaseWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityService securityService;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    void getUsuarioAutenticado_valido_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(securityService.getAuthenticatedUserId()).thenReturn(userId);
        when(usuarioService.getUsuarioAutenticado(userId))
            .thenReturn(UsuariosTestDataFactory.crearUsuarioDTOResponse(userId));

        mockMvc.perform(get("/api/usuarios/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value("test@test.com"));
    }
}

package com.campito.backend.usuarios.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.campito.backend.common.test.BaseWebMvcTest;
import com.campito.backend.common.test.TestIds;
import com.campito.backend.common.test.UsuariosTestDataFactory;
import com.campito.backend.security.SecurityService;
import com.campito.backend.usuarios.domain.dto.*;
import com.campito.backend.usuarios.service.EspacioTrabajoService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(EspacioTrabajoController.class)
@AutoConfigureMockMvc(addFilters = false)
class EspacioTrabajoControllerTest extends BaseWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EspacioTrabajoService espacioTrabajoService;

    @MockBean
    private SecurityService securityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registrarEspacioTrabajo_valido_retorna201() throws Exception {
        EspacioTrabajoDTORequest request = new EspacioTrabajoDTORequest("Mi Espacio", TestIds.USUARIO_ADMIN_ID);

        mockMvc.perform(post("/api/espacios-trabajo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    void listarMisEspaciosTrabajo_valido_retorna200() throws Exception {
        when(securityService.getAuthenticatedUserId()).thenReturn(TestIds.USUARIO_ADMIN_ID);
        when(espacioTrabajoService.listarEspaciosTrabajoPorUsuario(TestIds.USUARIO_ADMIN_ID))
            .thenReturn(List.of(new EspacioTrabajoDTOResponse(
                TestIds.ESPACIO_TRABAJO_ID, "Mi Espacio", new BigDecimal("1000.00"), TestIds.USUARIO_ADMIN_ID
            )));

        mockMvc.perform(get("/api/espacios-trabajo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nombre").value("Mi Espacio"));
    }

    @Test
    void compartirEspacioTrabajo_admin_retorna200() throws Exception {
        CompartirRequest request = new CompartirRequest("nuevo@email.com");

        mockMvc.perform(post("/api/espacios-trabajo/{id}/miembros", TestIds.ESPACIO_TRABAJO_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void responderSolicitud_valido_retorna200() throws Exception {
        ResponderSolicitudRequest request = new ResponderSolicitudRequest(true);

        mockMvc.perform(post("/api/espacios-trabajo/solicitudes/{id}/responder", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void obtenerMiembros_valido_retorna200() throws Exception {
        when(espacioTrabajoService.obtenerMiembrosEspacioTrabajo(any(UUID.class)))
            .thenReturn(List.of(UsuariosTestDataFactory.crearUsuarioDTOResponse(UUID.randomUUID())));

        mockMvc.perform(get("/api/espacios-trabajo/{id}/miembros", TestIds.ESPACIO_TRABAJO_ID))
            .andExpect(status().isOk());
    }

    @Test
    void listarSolicitudesPendientes_valido_retorna200() throws Exception {
        when(securityService.getAuthenticatedUserId()).thenReturn(TestIds.USUARIO_ADMIN_ID);
        when(espacioTrabajoService.listarSolicitudesPendientes(TestIds.USUARIO_ADMIN_ID))
            .thenReturn(List.of(new SolicitudPendienteEspacioTrabajoDTOResponse(
                1L, "Mi Espacio", "Admin", null, LocalDateTime.now()
            )));

        mockMvc.perform(get("/api/espacios-trabajo/solicitudes/pendientes"))
            .andExpect(status().isOk());
    }
}

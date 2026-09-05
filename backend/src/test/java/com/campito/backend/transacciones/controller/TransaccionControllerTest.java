package com.campito.backend.transacciones.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.campito.backend.common.test.TransaccionesTestDataFactory;
import com.campito.backend.security.SecurityService;
import com.campito.backend.transacciones.domain.dto.*;
import com.campito.backend.transacciones.service.TransaccionService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TransaccionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransaccionControllerTest extends BaseWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransaccionService transaccionService;

    @MockBean
    private SecurityService securityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registrarTransaccion_valido_retorna201() throws Exception {
        TransaccionDTOResponse response = TransaccionesTestDataFactory.crearTransaccionResponse(1L);
        when(transaccionService.registrarTransaccion(any())).thenReturn(response);

        TransaccionDTORequest request = TransaccionesTestDataFactory.crearTransaccionRequest(TestIds.ESPACIO_TRABAJO_ID);

        mockMvc.perform(post("/api/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void removerTransaccion_valido_retorna204() throws Exception {
        mockMvc.perform(delete("/api/transacciones/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void buscarTransaccion_valido_retorna200() throws Exception {
        PaginatedResponse<TransaccionDTOResponse> paginatedResponse = new PaginatedResponse<>(
            new org.springframework.data.domain.PageImpl<>(List.of(
                TransaccionesTestDataFactory.crearTransaccionResponse(1L)
            ))
        );
        when(transaccionService.buscarTransaccion(any())).thenReturn(paginatedResponse);

        TransaccionBusquedaDTO busqueda = new TransaccionBusquedaDTO(
            null, null, null, null, TestIds.ESPACIO_TRABAJO_ID, null, null
        );

        mockMvc.perform(post("/api/transacciones/buscar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(busqueda)))
            .andExpect(status().isOk());
    }

    @Test
    void listarContactos_valido_retorna200() throws Exception {
        when(transaccionService.listarContactos(any(UUID.class)))
            .thenReturn(List.of(TransaccionesTestDataFactory.crearContactoResponse(1L)));

        mockMvc.perform(get("/api/transacciones/contactos/espacio/{idEspacioTrabajo}", TestIds.ESPACIO_TRABAJO_ID))
            .andExpect(status().isOk());
    }

    @Test
    void listarMotivos_valido_retorna200() throws Exception {
        when(transaccionService.listarMotivos(any(UUID.class)))
            .thenReturn(List.of(TransaccionesTestDataFactory.crearMotivoResponse(1L)));

        mockMvc.perform(get("/api/transacciones/motivos/espacio/{idEspacioTrabajo}", TestIds.ESPACIO_TRABAJO_ID))
            .andExpect(status().isOk());
    }

    @Test
    void buscarTransaccionesRecientes_valido_retorna200() throws Exception {
        when(transaccionService.buscarTransaccionesRecientes(any(UUID.class)))
            .thenReturn(List.of(TransaccionesTestDataFactory.crearTransaccionResponse(1L)));

        mockMvc.perform(get("/api/transacciones/recientes/{idEspacio}", TestIds.ESPACIO_TRABAJO_ID))
            .andExpect(status().isOk());
    }

    @Test
    void registrarContacto_valido_retorna201() throws Exception {
        when(transaccionService.registrarContactoTransferencia(any()))
            .thenReturn(TransaccionesTestDataFactory.crearContactoResponse(1L));

        ContactoDTORequest request = TransaccionesTestDataFactory.crearContactoRequest(TestIds.ESPACIO_TRABAJO_ID);

        mockMvc.perform(post("/api/transacciones/contactos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    void nuevoMotivo_valido_retorna201() throws Exception {
        when(transaccionService.nuevoMotivoTransaccion(any()))
            .thenReturn(TransaccionesTestDataFactory.crearMotivoResponse(1L));

        MotivoDTORequest request = TransaccionesTestDataFactory.crearMotivoRequest(TestIds.ESPACIO_TRABAJO_ID);

        mockMvc.perform(post("/api/transacciones/motivos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}

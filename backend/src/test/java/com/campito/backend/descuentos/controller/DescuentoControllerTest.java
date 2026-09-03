package com.campito.backend.descuentos.controller;

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
import com.campito.backend.common.test.DescuentosTestDataFactory;
import com.campito.backend.common.test.TestIds;
import com.campito.backend.descuentos.domain.dto.DescuentoDTORequest;
import com.campito.backend.descuentos.service.DescuentoService;
import com.campito.backend.security.SecurityService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(DescuentoController.class)
@AutoConfigureMockMvc(addFilters = false)
class DescuentoControllerTest extends BaseWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityService securityService;

    @MockBean
    private DescuentoService descuentoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void crearDescuento_valido_retorna201() throws Exception {
        DescuentoDTORequest request = DescuentosTestDataFactory.crearDescuentoRequest(TestIds.ESPACIO_TRABAJO_ID);

        mockMvc.perform(post("/api/descuento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    void listarDescuentos_valido_retorna200() throws Exception {
        when(descuentoService.listarDescuentos(any(UUID.class)))
            .thenReturn(List.of(DescuentosTestDataFactory.crearDescuentoResponse(1L)));

        mockMvc.perform(get("/api/descuento/espacio/{idEspacioTrabajo}", TestIds.ESPACIO_TRABAJO_ID))
            .andExpect(status().isOk());
    }

    @Test
    void eliminarDescuento_valido_retorna204() throws Exception {
        mockMvc.perform(delete("/api/descuento/1"))
            .andExpect(status().isNoContent());
    }
}

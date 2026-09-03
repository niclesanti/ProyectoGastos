package com.campito.backend.transacciones.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
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
import com.campito.backend.transacciones.service.CuentaBancariaService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CuentaBancariaController.class)
@AutoConfigureMockMvc(addFilters = false)
class CuentaBancariaControllerTest extends BaseWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CuentaBancariaService cuentaBancariaService;

    @MockBean
    private SecurityService securityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void crearCuentaBancaria_valido_retorna201() throws Exception {
        CuentaBancariaDTORequest request = TransaccionesTestDataFactory.crearCuentaBancariaRequest(TestIds.ESPACIO_TRABAJO_ID);

        mockMvc.perform(post("/api/cuentas-bancarias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    void listarCuentasBancarias_valido_retorna200() throws Exception {
        when(cuentaBancariaService.listarCuentasBancarias(any(UUID.class)))
            .thenReturn(List.of(TransaccionesTestDataFactory.crearCuentaBancariaResponse(1L)));

        mockMvc.perform(get("/api/cuentas-bancarias/espacio/{idEspacioTrabajo}", TestIds.ESPACIO_TRABAJO_ID))
            .andExpect(status().isOk());
    }

    @Test
    void realizarTransaccion_valido_retorna200() throws Exception {
        TransaccionCuentaRequest request = new TransaccionCuentaRequest(1L, 2L, new BigDecimal("500.00"));

        mockMvc.perform(post("/api/cuentas-bancarias/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }
}

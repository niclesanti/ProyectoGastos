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
import com.campito.backend.transacciones.domain.dto.CompraCreditoBusquedaDTO;
import com.campito.backend.transacciones.service.CompraCreditoService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ComprasCreditoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ComprasCreditoControllerTest extends BaseWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompraCreditoService comprasCreditoService;

    @MockBean
    private SecurityService securityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registrarCompraCredito_valido_retorna201() throws Exception {
        when(comprasCreditoService.registrarCompraCredito(any()))
            .thenReturn(TransaccionesTestDataFactory.crearCompraCreditoResponse(1L));

        CompraCreditoDTORequest request = TransaccionesTestDataFactory.crearCompraCreditoRequest(TestIds.ESPACIO_TRABAJO_ID);

        mockMvc.perform(post("/api/compras-credito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void registrarTarjeta_valido_retorna201() throws Exception {
        when(comprasCreditoService.registrarTarjeta(any()))
            .thenReturn(TransaccionesTestDataFactory.crearTarjetaResponse(1L));

        TarjetaDTORequest request = TransaccionesTestDataFactory.crearTarjetaRequest(TestIds.ESPACIO_TRABAJO_ID);

        mockMvc.perform(post("/api/compras-credito/tarjetas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    void removerCompraCredito_valido_retorna204() throws Exception {
        mockMvc.perform(delete("/api/compras-credito/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void listarTarjetas_valido_retorna200() throws Exception {
        when(comprasCreditoService.listarTarjetas(any(UUID.class)))
            .thenReturn(List.of(TransaccionesTestDataFactory.crearTarjetaResponse(1L)));

        mockMvc.perform(get("/api/compras-credito/tarjetas/{idEspacioTrabajo}", TestIds.ESPACIO_TRABAJO_ID))
            .andExpect(status().isOk());
    }

    @Test
    void removerTarjeta_valido_retorna204() throws Exception {
        mockMvc.perform(delete("/api/compras-credito/tarjeta/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void pagarResumenTarjeta_valido_retorna200() throws Exception {
        PagarResumenTarjetaRequest request = new PagarResumenTarjetaRequest(
            1L, java.time.LocalDate.now(), new BigDecimal("100.00"),
            "Test User", TestIds.ESPACIO_TRABAJO_ID, null
        );

        mockMvc.perform(post("/api/compras-credito/pagar-resumen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void buscarComprasCredito_valido_retorna200() throws Exception {
        when(comprasCreditoService.BuscarComprasCredito(any(UUID.class)))
            .thenReturn(List.of(TransaccionesTestDataFactory.crearCompraCreditoResponse(1L)));

        mockMvc.perform(get("/api/compras-credito/buscar/{idEspacioTrabajo}", TestIds.ESPACIO_TRABAJO_ID))
            .andExpect(status().isOk());
    }

    @Test
    void listarComprasCreditoPendientes_valido_retorna200() throws Exception {
        PaginatedResponse<CompraCreditoDTOResponse> paginatedResponse = new PaginatedResponse<>(
            new org.springframework.data.domain.PageImpl<>(List.of(
                TransaccionesTestDataFactory.crearCompraCreditoResponse(1L)
            ))
        );
        when(comprasCreditoService.listarComprasCreditoDebeCuotas(any(UUID.class), any(), any()))
            .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/compras-credito/pendientes/{idEspacioTrabajo}", TestIds.ESPACIO_TRABAJO_ID))
            .andExpect(status().isOk());
    }

    @Test
    void modificarTarjeta_valido_retorna200() throws Exception {
        when(comprasCreditoService.modificarTarjeta(any(Long.class), any()))
            .thenReturn(TransaccionesTestDataFactory.crearTarjetaResponse(1L));

        TarjetaDTOUpdate update = new TarjetaDTOUpdate(20, 10);

        mockMvc.perform(put("/api/compras-credito/tarjetas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk());
    }

    @Test
    void buscarComprasCreditoPaginadas_valido_retorna200() throws Exception {
        CompraCreditoBusquedaDTO busqueda = new CompraCreditoBusquedaDTO(
            6, 2025, null, null, TestIds.ESPACIO_TRABAJO_ID, 0, 10);

        PaginatedResponse<CompraCreditoDTOResponse> paginatedResponse = new PaginatedResponse<>(
            new org.springframework.data.domain.PageImpl<>(List.of(
                TransaccionesTestDataFactory.crearCompraCreditoResponse(1L)
            ))
        );
        when(comprasCreditoService.buscarComprasCredito(any())).thenReturn(paginatedResponse);

        mockMvc.perform(post("/api/compras-credito/buscar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(busqueda)))
            .andExpect(status().isOk());
    }

    @Test
    void listarCuotasPorTarjeta_valido_retorna200() throws Exception {
        when(comprasCreditoService.listarCuotasPorTarjeta(any(Long.class)))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/compras-credito/cuotas/1"))
            .andExpect(status().isOk());
    }

    @Test
    void listarResumenesPorTarjeta_valido_retorna200() throws Exception {
        when(comprasCreditoService.listarResumenesPorTarjeta(any(Long.class)))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/compras-credito/resumenes/tarjeta/1"))
            .andExpect(status().isOk());
    }

    @Test
    void listarResumenesPorEspacioTrabajo_valido_retorna200() throws Exception {
        when(comprasCreditoService.listarResumenesPorEspacioTrabajo(any(UUID.class)))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/compras-credito/resumenes/espacio/{idEspacioTrabajo}", TestIds.ESPACIO_TRABAJO_ID))
            .andExpect(status().isOk());
    }
}

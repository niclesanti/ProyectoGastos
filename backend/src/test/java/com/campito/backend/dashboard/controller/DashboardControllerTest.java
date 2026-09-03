package com.campito.backend.dashboard.controller;

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
import org.springframework.test.web.servlet.MockMvc;

import com.campito.backend.common.test.BaseWebMvcTest;
import com.campito.backend.common.test.TestIds;
import com.campito.backend.common.test.DashboardTestDataFactory;
import com.campito.backend.dashboard.domain.dto.*;
import com.campito.backend.dashboard.service.DashboardService;
import com.campito.backend.security.SecurityService;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest extends BaseWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private SecurityService securityService;

    @Test
    void obtenerDashboardStats_valido_retorna200() throws Exception {
        DashboardStatsDTO stats = new DashboardStatsDTO(
            new BigDecimal("1000.00"),
            new BigDecimal("500.00"),
            new BigDecimal("200.00"),
            new BigDecimal("300.00"),
            List.of(),
            List.of(DashboardTestDataFactory.crearDistribucionGasto("Alimentación", new BigDecimal("250.00"))),
            List.of(),
            List.of()
        );
        when(dashboardService.obtenerDashboardStats(any(UUID.class))).thenReturn(stats);

        mockMvc.perform(get("/api/dashboard/stats/{idEspacio}", TestIds.ESPACIO_TRABAJO_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balanceTotal").value(1000))
            .andExpect(jsonPath("$.gastosMensuales").value(500));
    }
}

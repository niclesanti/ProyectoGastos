package com.campito.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campito.backend.dto.DashboardStatsDTO;
import com.campito.backend.service.DashboardService;
import com.campito.backend.service.SecurityService;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Operaciones para obtener estadísticas del dashboard")
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
@Validated
public class DashboardController {

    private final DashboardService dashboardService;
    private final SecurityService securityService;

    @Operation(summary = "Obtener estadísticas consolidadas del dashboard",
                description = "Obtiene todas las estadísticas del dashboard (KPIs + charts) en una sola llamada.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Estadísticas del dashboard obtenidas correctamente"),
                    @ApiResponse(responseCode = "404", description = "Espacio de trabajo no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                })
    @GetMapping("/stats/{idEspacio}")
    public ResponseEntity<DashboardStatsDTO> obtenerDashboardStats(
        @PathVariable @NotNull(message = "El id del espacio es obligatorio") UUID idEspacio) {
        
        securityService.validateWorkspaceAccess(idEspacio);
        DashboardStatsDTO dashboardStats = dashboardService.obtenerDashboardStats(idEspacio);
        return new ResponseEntity<>(dashboardStats, HttpStatus.OK);
    }

}

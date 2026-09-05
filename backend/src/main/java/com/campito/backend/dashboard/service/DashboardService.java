package com.campito.backend.dashboard.service;

import java.util.UUID;

import com.campito.backend.dashboard.domain.dto.DashboardStatsDTO;

public interface DashboardService {
    public DashboardStatsDTO obtenerDashboardStats(UUID idEspacio);
}

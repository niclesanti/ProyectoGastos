package com.campito.backend.service;

import com.campito.backend.dto.DashboardStatsDTO;

public interface DashboardService {
    public DashboardStatsDTO obtenerDashboardStats(Long idEspacio);
}

package com.campito.backend.service;

import java.util.UUID;

import com.campito.backend.dto.DashboardStatsDTO;

public interface DashboardService {
    public DashboardStatsDTO obtenerDashboardStats(UUID idEspacio);
}

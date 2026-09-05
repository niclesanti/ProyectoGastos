package com.campito.backend.dashboard.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.campito.backend.dashboard.domain.entity.GastosIngresosMensuales;
import com.campito.backend.dashboard.domain.entity.ResumenFinanciero;
import com.campito.backend.dashboard.repository.GastosIngresosMensualesRepository;
import com.campito.backend.dashboard.repository.ResumenFinancieroRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardReadModelService {

    private final ResumenFinancieroRepository resumenFinancieroRepository;
    private final GastosIngresosMensualesRepository gastosIngresosMensualesRepository;

    @Cacheable(cacheNames = DashboardCacheNames.RESUMEN_FINANCIERO, key = "#idEspacio")
    public ResumenFinanciero obtenerResumenFinanciero(UUID idEspacio) {
        return resumenFinancieroRepository.findById(idEspacio)
            .orElseThrow(() -> new EntityNotFoundException("Espacio de trabajo no encontrado para ID: " + idEspacio));
    }

    @Cacheable(cacheNames = DashboardCacheNames.GASTOS_INGRESOS_MENSUALES, key = "#idEspacio")
    public List<GastosIngresosMensuales> obtenerRegistrosMensuales(UUID idEspacio, List<String> meses) {
        return gastosIngresosMensualesRepository.findByEspacioTrabajoAndMeses(idEspacio, meses);
    }

    public BigDecimal gastosMesActual(UUID idEspacio, Integer anio, Integer mes) {
        Optional<GastosIngresosMensuales> opt = gastosIngresosMensualesRepository
            .findByIdEspacioTrabajoAndAnioAndMes(idEspacio, anio, mes);
        return opt.map(GastosIngresosMensuales::getGastos).orElse(BigDecimal.ZERO);
    }
}

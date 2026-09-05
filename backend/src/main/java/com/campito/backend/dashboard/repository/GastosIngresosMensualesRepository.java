package com.campito.backend.dashboard.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.campito.backend.dashboard.domain.entity.GastosIngresosMensuales;

public interface GastosIngresosMensualesRepository extends JpaRepository<GastosIngresosMensuales, Long> {

    Optional<GastosIngresosMensuales> findByIdEspacioTrabajoAndAnioAndMes(UUID espacioTrabajoId, Integer anio, Integer mes);

    /**
     * Busca todos los registros de gastos e ingresos mensuales para un espacio de trabajo
     * que coincidan con los meses especificados.
     * 
     * OPTIMIZACIÓN: Query nativa simplificada que usa IN con subconsulta generada dinámicamente.
     * Permite uso del índice compuesto idx_gastos_ingresos_espacio_periodo(espacio_trabajo_id, anio, mes).
     * 
     * NOTA: Como PostgreSQL con JPA no soporta bien arrays dinámicos, usamos una estrategia alternativa:
     * parsear los strings "YYYY-MM" en la aplicación y generar condiciones separadas.
     * Sin embargo, para mantener compatibilidad, generamos pares (anio,mes) desde los strings.
     * 
     * @param espacioTrabajoId ID del espacio de trabajo
     * @param anioMeses Lista de strings en formato "YYYY-MM" para buscar
     * @return Lista de registros encontrados, ordenados por año y mes descendente
     */
    @Query(value = """
        SELECT g.* FROM dashboard.gastos_ingresos_mensuales g
        WHERE g.espacio_trabajo_id = :espacioTrabajoId
          AND CONCAT(LPAD(CAST(g.anio AS TEXT), 4, '0'), '-', LPAD(CAST(g.mes AS TEXT), 2, '0')) IN (:anioMeses)
        ORDER BY g.anio DESC, g.mes DESC
        """, nativeQuery = true)
    List<GastosIngresosMensuales> findByEspacioTrabajoAndMeses(
        @Param("espacioTrabajoId") UUID espacioTrabajoId,
        @Param("anioMeses") List<String> anioMeses);
} 

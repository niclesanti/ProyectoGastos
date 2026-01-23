package com.campito.backend.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.campito.backend.model.GastosIngresosMensuales;

@Repository
public interface GastosIngresosMensualesRepository extends JpaRepository<GastosIngresosMensuales, Long> {

    Optional<GastosIngresosMensuales> findByEspacioTrabajo_IdAndAnioAndMes(UUID espacioTrabajoId, Integer anio, Integer mes);

    /**
     * Busca todos los registros de gastos e ingresos mensuales para un espacio de trabajo
     * que coincidan con los meses especificados.
     * 
     * @param espacioTrabajoId ID del espacio de trabajo
     * @param anioMeses Lista de strings en formato "YYYY-MM" para buscar
     * @return Lista de registros encontrados
     */
    @Query("SELECT g FROM GastosIngresosMensuales g WHERE g.espacioTrabajo.id = :espacioTrabajoId " +
           "AND CONCAT(CAST(g.anio AS string), '-', CASE WHEN g.mes < 10 THEN CONCAT('0', CAST(g.mes AS string)) ELSE CAST(g.mes AS string) END) IN :anioMeses " +
           "ORDER BY g.anio DESC, g.mes DESC")
    List<GastosIngresosMensuales> findByEspacioTrabajoAndMeses(
        @Param("espacioTrabajoId") UUID espacioTrabajoId,
        @Param("anioMeses") List<String> anioMeses);
} 

package com.campito.backend.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.campito.backend.model.EstadoResumen;
import com.campito.backend.model.Resumen;

public interface ResumenRepository extends JpaRepository<Resumen, Long> {
    
    /**
     * Busca un resumen por tarjeta, año y mes
     */
    @Query("SELECT r FROM Resumen r WHERE r.tarjeta.id = :idTarjeta " +
           "AND r.anio = :anio AND r.mes = :mes")
    Optional<Resumen> findByTarjetaAndAnioAndMes(
        @Param("idTarjeta") Long idTarjeta, 
        @Param("anio") Integer anio, 
        @Param("mes") Integer mes
    );
    
    /**
     * Lista todos los resúmenes de una tarjeta
     */
    @Query("SELECT r FROM Resumen r WHERE r.tarjeta.id = :idTarjeta " +
           "ORDER BY r.anio DESC, r.mes DESC")
    List<Resumen> findByTarjetaId(@Param("idTarjeta") Long idTarjeta);
    
    /**
     * Lista todos los resúmenes de un espacio de trabajo
     */
    @Query("SELECT r FROM Resumen r WHERE r.tarjeta.espacioTrabajo.id = :idEspacioTrabajo " +
           "ORDER BY r.anio DESC, r.mes DESC")
    List<Resumen> findByEspacioTrabajoId(@Param("idEspacioTrabajo") Long idEspacioTrabajo);
    
    /**
     * Lista resúmenes por estado
     */
    @Query("SELECT r FROM Resumen r WHERE r.tarjeta.id = :idTarjeta " +
           "AND r.estado = :estado ORDER BY r.anio DESC, r.mes DESC")
    List<Resumen> findByTarjetaIdAndEstado(
        @Param("idTarjeta") Long idTarjeta, 
        @Param("estado") EstadoResumen estado
    );
    
    /**
     * Lista resúmenes por tarjeta filtrando por múltiples estados
     */
    @Query("SELECT r FROM Resumen r WHERE r.tarjeta.id = :idTarjeta " +
           "AND r.estado IN :estados ORDER BY r.anio DESC, r.mes DESC")
    List<Resumen> findByTarjetaIdAndEstadoIn(
        @Param("idTarjeta") Long idTarjeta,
        @Param("estados") List<EstadoResumen> estados
    );
}

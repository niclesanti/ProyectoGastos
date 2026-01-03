package com.campito.backend.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.campito.backend.model.CuotaCredito;

public interface CuotaCreditoRepository extends JpaRepository<CuotaCredito, Long> {
    List<CuotaCredito> findByCompraCredito_Id(Long idCompraCredito);
    
    List<CuotaCredito> findByCompraCredito_IdAndPagada(Long idCompraCredito, boolean pagada);
    
    @Query("SELECT c FROM CuotaCredito c WHERE c.compraCredito.tarjeta.id = :idTarjeta " +
           "AND c.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY c.fechaVencimiento ASC, c.compraCredito.id ASC, c.numeroCuota ASC")
    List<CuotaCredito> findByTarjetaAndFechaVencimientoBetween(
        @Param("idTarjeta") Long idTarjeta, 
        @Param("fechaInicio") LocalDate fechaInicio, 
        @Param("fechaFin") LocalDate fechaFin
    );
    
    @Query("SELECT COALESCE(SUM(c.montoCuota), 0.0) FROM CuotaCredito c " +
           "JOIN c.compraCredito cc " +
           "WHERE cc.espacioTrabajo.id = :idEspacioTrabajo " +
           "AND c.pagada = false " +
           "AND c.fechaVencimiento <= :fechaLimite")
    Float calcularResumenTarjeta(@Param("idEspacioTrabajo") Long idEspacioTrabajo, @Param("fechaLimite") LocalDate fechaLimite);
    
    @Query("SELECT COALESCE(SUM(c.montoCuota), 0.0) FROM CuotaCredito c " +
           "JOIN c.compraCredito cc " +
           "WHERE cc.espacioTrabajo.id = :idEspacioTrabajo " +
           "AND c.pagada = false")
    Float calcularDeudaTotalPendiente(@Param("idEspacioTrabajo") Long idEspacioTrabajo);
    
    /**
     * Busca cuotas sin resumen asociado para una tarjeta en un rango de fechas.
     * IMPORTANTE: Busca por la FECHA DE VENCIMIENTO de la cuota, ya que cada cuota
     * debe aparecer en el resumen del mes en que vence, no en el mes de la compra.
     */
    @Query("SELECT c FROM CuotaCredito c WHERE c.compraCredito.tarjeta.id = :idTarjeta " +
           "AND c.resumenAsociado IS NULL " +
           "AND c.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY c.fechaVencimiento ASC")
    List<CuotaCredito> findByTarjetaSinResumenEnRango(
        @Param("idTarjeta") Long idTarjeta,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
    
    /**
     * Busca cuotas asociadas a un resumen
     */
    @Query("SELECT c FROM CuotaCredito c WHERE c.resumenAsociado.id = :idResumen")
    List<CuotaCredito> findByResumenAsociado_Id(@Param("idResumen") Long idResumen);
    
    void deleteByCompraCredito_Id(Long idCompraCredito);
}

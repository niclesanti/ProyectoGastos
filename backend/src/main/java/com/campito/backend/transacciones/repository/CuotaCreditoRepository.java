package com.campito.backend.transacciones.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.campito.backend.transacciones.domain.entity.CuotaCredito;

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
    
    @Query("SELECT COALESCE(SUM(c.montoCuota), 0) FROM CuotaCredito c " +
           "JOIN c.compraCredito cc " +
           "WHERE cc.espacioTrabajo.id = :idEspacioTrabajo " +
           "AND c.pagada = false")
    BigDecimal calcularDeudaTotalPendiente(@Param("idEspacioTrabajo") UUID idEspacioTrabajo);
    
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
     * Busca cuotas sin resumen asociado para TODAS las tarjetas de un espacio en un rango de fechas.
     * 
     * OPTIMIZACIÓN: Query batch que reemplaza N queries individuales (una por tarjeta).
     * Útil para el método resumenMensual() que necesita cuotas de todas las tarjetas.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo
     * @param fechaInicio Fecha inicio del rango (inclusive)
     * @param fechaFin Fecha fin del rango (inclusive)
     * @return Lista de cuotas sin resumen en el rango, con tarjetas eager-loaded
     */
    @Query("SELECT c FROM CuotaCredito c " +
           "JOIN FETCH c.compraCredito cc " +
           "JOIN FETCH cc.tarjeta t " +
           "WHERE t.espacioTrabajo.id = :idEspacioTrabajo " +
           "AND c.resumenAsociado IS NULL " +
           "AND c.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY c.fechaVencimiento ASC")
    List<CuotaCredito> findByEspacioTrabajoSinResumenEnRango(
        @Param("idEspacioTrabajo") UUID idEspacioTrabajo,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
    
    /**
     * Busca cuotas asociadas a un resumen
     */
    @Query("SELECT c FROM CuotaCredito c WHERE c.resumenAsociado.id = :idResumen")
    List<CuotaCredito> findByResumenAsociado_Id(@Param("idResumen") Long idResumen);
    
    /**
     * Calcula el total de cuotas pendientes (sin resumen) para todas las tarjetas de un espacio
     * que vencen en un rango de fechas específico.
     * 
     * OPTIMIZACIÓN: Reemplaza el N+1 query problem del método resumenMensual().
     * En lugar de hacer 1 query para traer tarjetas + N queries (una por tarjeta),
     * esta query hace una sola agregación directa.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo
     * @param fechaInicio Fecha inicio del rango (inclusive)
     * @param fechaFin Fecha fin del rango (inclusive)
     * @return Suma total de montos de cuotas pendientes en el rango
     */
    @Query("SELECT COALESCE(SUM(c.montoCuota), 0) FROM CuotaCredito c " +
           "JOIN c.compraCredito cc " +
           "JOIN cc.tarjeta t " +
           "WHERE t.espacioTrabajo.id = :idEspacioTrabajo " +
           "AND c.resumenAsociado IS NULL " +
           "AND c.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularResumenMensualPendiente(
        @Param("idEspacioTrabajo") UUID idEspacioTrabajo,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
    
    void deleteByCompraCredito_Id(Long idCompraCredito);
}

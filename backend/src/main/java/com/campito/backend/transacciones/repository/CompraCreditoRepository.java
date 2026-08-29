package com.campito.backend.transacciones.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.campito.backend.transacciones.domain.entity.CompraCredito;

public interface CompraCreditoRepository extends JpaRepository<CompraCredito, Long>, JpaSpecificationExecutor<CompraCredito> {
    
    List<CompraCredito> findByIdEspacioTrabajo(UUID idEspacioTrabajo);
    
    @Query("SELECT DISTINCT c FROM CompraCredito c " +
           "LEFT JOIN FETCH c.motivo " +
           "LEFT JOIN FETCH c.comercio " +
           "LEFT JOIN FETCH c.tarjeta " +
           "WHERE c.idEspacioTrabajo = :idEspacioTrabajo " +
           "AND c.cuotasPagadas < c.cantidadCuotas")
    List<CompraCredito> findByIdEspacioTrabajoAndCuotasPendientes(@Param("idEspacioTrabajo") UUID idEspacioTrabajo);
    
    @Query("SELECT c FROM CompraCredito c " +
           "WHERE c.idEspacioTrabajo = :idEspacioTrabajo " +
           "AND c.cuotasPagadas < c.cantidadCuotas")
    Page<CompraCredito> findByIdEspacioTrabajoAndCuotasPendientesPageable(
        @Param("idEspacioTrabajo") UUID idEspacioTrabajo, 
        Pageable pageable);
    
    boolean existsByTarjeta_Id(Long idTarjeta);
}

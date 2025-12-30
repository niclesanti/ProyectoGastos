package com.campito.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.campito.backend.model.CompraCredito;

public interface CompraCreditoRepository extends JpaRepository<CompraCredito, Long> {
    List<CompraCredito> findByEspacioTrabajo_Id(Long idEspacioTrabajo);
    
    @Query("SELECT DISTINCT c FROM CompraCredito c " +
           "LEFT JOIN FETCH c.espacioTrabajo " +
           "LEFT JOIN FETCH c.motivo " +
           "LEFT JOIN FETCH c.comercio " +
           "LEFT JOIN FETCH c.tarjeta " +
           "WHERE c.espacioTrabajo.id = :idEspacioTrabajo " +
           "AND c.cuotasPagadas < c.cantidadCuotas")
    List<CompraCredito> findByEspacioTrabajo_IdAndCuotasPendientes(@Param("idEspacioTrabajo") Long idEspacioTrabajo);
    
    boolean existsByTarjeta_Id(Long idTarjeta);
}

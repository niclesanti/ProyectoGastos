package com.campito.backend.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campito.backend.model.Tarjeta;

@Repository
public interface TarjetaRepository extends JpaRepository<Tarjeta, Long> {
    
    List<Tarjeta> findByEspacioTrabajo_Id(Long idEspacioTrabajo);
    
    // Método para obtener tarjetas ordenadas por última modificación (más recientes primero)
    List<Tarjeta> findByEspacioTrabajo_IdOrderByFechaModificacionDesc(Long idEspacioTrabajo);
    
    Optional<Tarjeta> findFirstByNumeroTarjetaAndEntidadFinancieraAndRedDePagoAndEspacioTrabajo_Id(
        String numeroTarjeta, String entidadFinanciera, String redDePago, Long idEspacioTrabajo);
}

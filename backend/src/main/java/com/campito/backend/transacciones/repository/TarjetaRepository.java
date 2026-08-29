package com.campito.backend.transacciones.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campito.backend.transacciones.domain.entity.Tarjeta;

public interface TarjetaRepository extends JpaRepository<Tarjeta, Long> {

    List<Tarjeta> findByEspacioTrabajo_Id(UUID idEspacioTrabajo);

    // Método para obtener tarjetas ordenadas por última modificación (más recientes
    // primero)
    List<Tarjeta> findByEspacioTrabajo_IdOrderByFechaModificacionDesc(UUID idEspacioTrabajo);

    Optional<Tarjeta> findFirstByNumeroTarjetaAndEntidadFinancieraAndRedDePagoAndEspacioTrabajo_Id(
            String numeroTarjeta, String entidadFinanciera, String redDePago, UUID idEspacioTrabajo);

    // Método para obtener tarjetas que cierran en un día específico
    List<Tarjeta> findByDiaCierre(Integer diaCierre);
}

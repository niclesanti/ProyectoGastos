package com.campito.backend.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campito.backend.model.Descuento;

public interface DescuentoRepository extends JpaRepository<Descuento, Long> {

    /**
     * Lista todos los descuentos de un espacio de trabajo.
     *
     * @param idEspacioTrabajo UUID del espacio de trabajo
     * @return Lista de descuentos del espacio de trabajo, ordenados por día
     */
    List<Descuento> findByEspacioTrabajo_IdOrderByDiaAsc(UUID idEspacioTrabajo);

    /**
     * Verifica si un descuento existe y pertenece a un espacio de trabajo
     * donde el usuario dado es participante.
     *
     * @param descuentoId ID del descuento
     * @param userId ID del usuario
     * @return true si el descuento existe y el usuario es participante del espacio de trabajo
     */
    boolean existsByIdAndEspacioTrabajo_UsuariosParticipantes_Id(Long descuentoId, UUID userId);
}

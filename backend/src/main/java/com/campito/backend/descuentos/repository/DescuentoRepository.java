package com.campito.backend.descuentos.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campito.backend.descuentos.domain.entity.Descuento;

public interface DescuentoRepository extends JpaRepository<Descuento, Long> {

    /**
     * Lista todos los descuentos de un espacio de trabajo.
     *
     * @param idEspacioTrabajo UUID del espacio de trabajo
     * @return Lista de descuentos del espacio de trabajo, ordenados por día
     */
    List<Descuento> findByIdEspacioTrabajoOrderByDiaAsc(UUID idEspacioTrabajo);
}

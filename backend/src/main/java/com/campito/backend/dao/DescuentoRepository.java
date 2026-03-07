package com.campito.backend.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campito.backend.model.Descuento;

@Repository
public interface DescuentoRepository extends JpaRepository<Descuento, Long> {

    /**
     * Lista todos los descuentos de un espacio de trabajo.
     *
     * @param idEspacioTrabajo UUID del espacio de trabajo
     * @return Lista de descuentos del espacio de trabajo, ordenados por día
     */
    List<Descuento> findByEspacioTrabajo_IdOrderByDiaAsc(UUID idEspacioTrabajo);
}

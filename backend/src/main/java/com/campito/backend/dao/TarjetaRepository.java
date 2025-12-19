package com.campito.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campito.backend.model.Tarjeta;

public interface TarjetaRepository extends JpaRepository<Tarjeta, Long> {
    List<Tarjeta> findByEspacioTrabajo_Id(Long idEspacioTrabajo);
}

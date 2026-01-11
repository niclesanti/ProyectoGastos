package com.campito.backend.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campito.backend.model.GastosIngresosMensuales;

@Repository
public interface GastosIngresosMensualesRepository extends JpaRepository<GastosIngresosMensuales, Long> {

    Optional<GastosIngresosMensuales> findByEspacioTrabajo_IdAndAnioAndMes(Long espacioTrabajoId, Integer anio, Integer mes);
} 

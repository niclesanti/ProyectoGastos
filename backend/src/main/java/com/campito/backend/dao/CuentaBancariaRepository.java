package com.campito.backend.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campito.backend.model.CuentaBancaria;

@Repository
public interface CuentaBancariaRepository extends JpaRepository<CuentaBancaria, Long> {
    
    List<CuentaBancaria> findByEspacioTrabajo_Id(Long idEspacioTrabajo);
    
    Optional<CuentaBancaria> findFirstByNombreAndEspacioTrabajo_Id(String nombre, Long idEspacioTrabajo);
}

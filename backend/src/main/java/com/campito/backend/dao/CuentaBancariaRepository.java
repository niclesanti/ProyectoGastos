package com.campito.backend.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campito.backend.model.CuentaBancaria;

@Repository
public interface CuentaBancariaRepository extends JpaRepository<CuentaBancaria, Long> {
    
    List<CuentaBancaria> findByEspacioTrabajo_Id(UUID idEspacioTrabajo);
    
    // Método para obtener cuentas bancarias ordenadas por última modificación (más recientes primero)
    List<CuentaBancaria> findByEspacioTrabajo_IdOrderByFechaModificacionDesc(UUID idEspacioTrabajo);
    
    Optional<CuentaBancaria> findFirstByNombreAndEspacioTrabajo_Id(String nombre, UUID idEspacioTrabajo);
}

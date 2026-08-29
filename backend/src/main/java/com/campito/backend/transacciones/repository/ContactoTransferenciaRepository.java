package com.campito.backend.transacciones.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campito.backend.transacciones.domain.entity.ContactoTransferencia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactoTransferenciaRepository extends JpaRepository<ContactoTransferencia, Long> {
    
    List<ContactoTransferencia> findByEspacioTrabajo_Id(UUID idEspacioTrabajo);
    
    // Método para obtener contactos ordenados por última modificación (más recientes primero)
    List<ContactoTransferencia> findByEspacioTrabajo_IdOrderByFechaModificacionDesc(UUID idEspacioTrabajo);
    
    Optional<ContactoTransferencia> findFirstByNombreAndEspacioTrabajo_Id(String nombre, UUID idEspacioTrabajo);
}

package com.campito.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campito.backend.model.ContactoTransferencia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactoTransferenciaRepository extends JpaRepository<ContactoTransferencia, Long> {
    
    List<ContactoTransferencia> findByEspacioTrabajo_Id(UUID idEspacioTrabajo);
    
    // Método para obtener contactos ordenados por última modificación (más recientes primero)
    List<ContactoTransferencia> findByEspacioTrabajo_IdOrderByFechaModificacionDesc(UUID idEspacioTrabajo);
    
    Optional<ContactoTransferencia> findFirstByNombreAndEspacioTrabajo_Id(String nombre, UUID idEspacioTrabajo);
}

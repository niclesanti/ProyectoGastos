package com.campito.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campito.backend.model.ContactoTransferencia;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactoTransferenciaRepository extends JpaRepository<ContactoTransferencia, Long> {
    
    List<ContactoTransferencia> findByEspacioTrabajo_Id(Long idEspacioTrabajo);
    
    Optional<ContactoTransferencia> findFirstByNombreAndEspacioTrabajo_Id(String nombre, Long idEspacioTrabajo);
}

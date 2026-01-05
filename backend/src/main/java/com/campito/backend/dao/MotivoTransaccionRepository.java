package com.campito.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campito.backend.model.MotivoTransaccion;

import java.util.List;
import java.util.Optional;

public interface MotivoTransaccionRepository extends JpaRepository<MotivoTransaccion, Long> {
    List<MotivoTransaccion> findByEspacioTrabajo_Id(Long idEspacioTrabajo);
    Optional<MotivoTransaccion> findFirstByMotivoAndEspacioTrabajo_Id(String motivo, Long idEspacioTrabajo);
}

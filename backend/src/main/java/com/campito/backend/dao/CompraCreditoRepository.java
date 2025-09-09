package com.campito.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campito.backend.model.CompraCredito;

public interface CompraCreditoRepository extends JpaRepository<CompraCredito, Long> {
    List<CompraCredito> findByEspacioTrabajo_Id(Long idEspacioTrabajo);
}

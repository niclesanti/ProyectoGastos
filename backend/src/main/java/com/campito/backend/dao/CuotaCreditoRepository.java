package com.campito.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campito.backend.model.CuotaCredito;

public interface CuotaCreditoRepository extends JpaRepository<CuotaCredito, Long> {
    List<CuotaCredito> findByCompraCredito_Id(Long idCompraCredito);
}

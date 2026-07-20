package com.campito.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.campito.backend.model.Transaccion;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long>, JpaSpecificationExecutor<Transaccion> {

}

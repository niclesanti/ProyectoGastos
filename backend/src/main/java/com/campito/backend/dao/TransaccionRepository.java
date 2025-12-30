package com.campito.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.campito.backend.model.Transaccion;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long>, JpaSpecificationExecutor<Transaccion> {

    @Query("SELECT COALESCE(SUM(t.monto), 0.0) FROM Transaccion t "
           + "WHERE t.espacioTrabajo.id = :idEspacioTrabajo "
           + "AND t.tipo = 'GASTO' "
           + "AND EXTRACT(YEAR FROM t.fecha) = :anio "
           + "AND EXTRACT(MONTH FROM t.fecha) = :mes")
    Float calcularGastosMensuales(@Param("idEspacioTrabajo") Long idEspacioTrabajo, @Param("anio") int anio, @Param("mes") int mes);
}

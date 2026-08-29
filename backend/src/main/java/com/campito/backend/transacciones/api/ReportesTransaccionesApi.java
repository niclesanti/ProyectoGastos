package com.campito.backend.transacciones.api;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.campito.backend.common.dto.DistribucionGastoDTO;
import com.campito.backend.transacciones.domain.entity.Transaccion;

/**
 * Facade de lectura del módulo de transacciones para los reportes de
 * distribución de gastos y compras a crédito usados por el dashboard.
 * 
 * Las queries nativas viven en este módulo para que el dashboard no
 * dependa de tablas de transacciones.
 */
public interface ReportesTransaccionesApi extends JpaRepository<Transaccion, Long> {

    @Query(value = """
            SELECT
                mt.motivo,
                (SUM(t.monto) * 100.0 / (SELECT SUM(t2.monto) FROM transacciones t2 WHERE t2.espacio_trabajo_id = :idEspacio AND t2.tipo = 'GASTO' AND t2.fecha >= :fechaLimite)) AS porcentaje
            FROM transacciones t
            JOIN motivos_transaccion mt ON t.motivo_transaccion_id = mt.id
            WHERE t.espacio_trabajo_id = :idEspacio
              AND t.tipo = 'GASTO'
              AND t.fecha >= :fechaLimite
            GROUP BY mt.motivo
            ORDER BY SUM(t.monto) DESC
            """, nativeQuery = true)
    List<DistribucionGastoDTO> findDistribucionGastos(@Param("idEspacio") UUID idEspacio,
            @Param("fechaLimite") LocalDate fechaLimite);

    @Query(value = """
            SELECT
                mt.motivo,
                ROUND(SUM(cc.monto_total) * 100.0 / SUM(SUM(cc.monto_total)) OVER (), 2) AS porcentaje
            FROM compras_credito cc
            JOIN motivos_transaccion mt ON cc.motivo_transaccion_id = mt.id
            WHERE cc.espacio_trabajo_id = :idEspacio
              AND cc.fecha_compra >= :fechaLimite
            GROUP BY mt.motivo
            ORDER BY SUM(cc.monto_total) DESC
            """, nativeQuery = true)
    List<DistribucionGastoDTO> findDistribucionComprasCredito(@Param("idEspacio") UUID idEspacio,
            @Param("fechaLimite") LocalDate fechaLimite);

}

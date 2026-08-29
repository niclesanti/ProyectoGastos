package com.campito.backend.dashboard.event;

import com.campito.backend.dashboard.domain.entity.GastosIngresosMensuales;
import com.campito.backend.dashboard.repository.GastosIngresosMensualesRepository;
import com.campito.backend.exception.SaldoInsuficienteException;
import com.campito.backend.shared.event.CompraCreditoEliminadaEvent;
import com.campito.backend.shared.event.CompraCreditoRegistradaEvent;
import com.campito.backend.shared.event.ResumenPagadoEvent;
import com.campito.backend.shared.event.TransaccionEliminadaEvent;
import com.campito.backend.shared.event.TransaccionRegistradaEvent;
import com.campito.backend.transacciones.domain.entity.TipoTransaccion;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Listener del dashboard que mantiene la desnormalización de
 * {@link GastosIngresosMensuales} ante eventos de los módulos productores.
 * 
 * Los listeners son SÍNCRONOS y corren dentro de la transacción del servicio
 * productor para preservar la consistencia (p. ej. {@link SaldoInsuficienteException}
 * al eliminar transacciones provoca el rollback de toda la operación).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardEventListener {

    private final GastosIngresosMensualesRepository gastosIngresosMensualesRepository;

    @EventListener
    public void onTransaccionRegistrada(TransaccionRegistradaEvent event) {
        gastosIgresosMesAnotar(event.tipo(), event.monto(), event.idEspacioTrabajo(), event.fecha());
    }

    @EventListener
    public void onTransaccionEliminada(TransaccionEliminadaEvent event) {
        gastosIngresosMesDelete(event.tipo(), event.monto(), event.idEspacioTrabajo(), event.fecha());
    }

    @EventListener
    public void onCompraCreditoRegistrada(CompraCreditoRegistradaEvent event) {
        compraCreditoMesAnotar(event.montoTotal(), event.idEspacioTrabajo(), event.fechaCompra());
    }

    @EventListener
    public void onCompraCreditoEliminada(CompraCreditoEliminadaEvent event) {
        compraCreditoMesDelete(event.montoTotal(), event.idEspacioTrabajo(), event.fechaCompra());
    }

    @EventListener
    public void onResumenPagado(ResumenPagadoEvent event) {
        pagoResumenMesAnotar(event.montoTotal(), event.idEspacioTrabajo(), LocalDate.of(event.anio(), event.mes(), 1));
    }

    /**
     * Anota gastos e ingresos por mes. Usa la fecha real de la transacción
     * para determinar el anio/mes del registro.
     */
    private void gastosIgresosMesAnotar(TipoTransaccion tipo, BigDecimal monto, UUID idEspacioTrabajo, LocalDate fecha) {

        Integer anio = fecha.getYear();
        Integer mes = fecha.getMonthValue();

        Optional<GastosIngresosMensuales> opt = gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacioTrabajo, anio, mes);

        GastosIngresosMensuales registro = opt.orElseGet(() ->
            GastosIngresosMensuales.builder()
                    .anio(anio)
                    .mes(mes)
                    .gastos(BigDecimal.ZERO)
                    .ingresos(BigDecimal.ZERO)
                    .comprasCredito(BigDecimal.ZERO)
                    .pagoResumen(BigDecimal.ZERO)
                    .idEspacioTrabajo(idEspacioTrabajo)
                    .build()
        );

        if (tipo.equals(TipoTransaccion.GASTO)) {
            registro.actualizarGastos(monto);
        } else {
            registro.actualizarIngresos(monto);
        }

        gastosIngresosMensualesRepository.save(registro);
        log.info("Gastos/Ingresos mensuales anotados: espacioId={}, anio={}, mes={}, gastos={}, ingresos={}",
                idEspacioTrabajo, anio, mes, registro.getGastos(), registro.getIngresos());
    }

    /**
     * Elimina gastos e ingresos por mes porque se eliminó una transacción.
     * Usa la fecha real de la transacción para determinar el anio/mes del registro.
     */
    private void gastosIngresosMesDelete(TipoTransaccion tipo, BigDecimal monto, UUID idEspacioTrabajo, LocalDate fecha) {

        Integer anio = fecha.getYear();
        Integer mes = fecha.getMonthValue();

        Optional<GastosIngresosMensuales> opt = gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacioTrabajo, anio, mes);

        GastosIngresosMensuales registro = opt.orElseThrow(() -> {
            String msg = "Registro de GastosIngresosMensuales no encontrado para espacioId=" + idEspacioTrabajo + ", anio=" + anio + ", mes=" + mes;
            log.warn(msg);
            return new EntityNotFoundException(msg);
        });

        if (tipo.equals(TipoTransaccion.GASTO)) {
            if (registro.getGastos().compareTo(monto) < 0) {
                String msg = String.format("No se puede eliminar la transacción. El monto a eliminar ($%.2f) es mayor que los gastos registrados en este mes ($%.2f).", monto, registro.getGastos());
                log.warn(msg);
                throw new SaldoInsuficienteException(msg);
            }
            registro.eliminarGastos(monto);
        } else {
            if (registro.getIngresos().compareTo(monto) < 0) {
                String msg = String.format("No se puede eliminar la transacción. El monto a eliminar ($%.2f) es mayor que los ingresos registrados en este mes ($%.2f).", monto, registro.getIngresos());
                log.warn(msg);
                throw new SaldoInsuficienteException(msg);
            }
            registro.eliminarIngresos(monto);
        }

        gastosIngresosMensualesRepository.save(registro);
        log.info("Gastos/Ingresos mensuales anotados: espacioId={}, anio={}, mes={}, gastos={}, ingresos={}",
                idEspacioTrabajo, anio, mes, registro.getGastos(), registro.getIngresos());
    }

    /**
     * Anota el monto de una compra con crédito en el registro mensual del espacio de trabajo.
     * Usa la fecha real de la compra para determinar el anio/mes del registro.
     */
    private void compraCreditoMesAnotar(BigDecimal monto, UUID idEspacioTrabajo, LocalDate fecha) {
        Integer anio = fecha.getYear();
        Integer mes = fecha.getMonthValue();

        Optional<GastosIngresosMensuales> opt = gastosIngresosMensualesRepository
                .findByIdEspacioTrabajoAndAnioAndMes(idEspacioTrabajo, anio, mes);

        GastosIngresosMensuales registro = opt.orElseGet(() ->
            GastosIngresosMensuales.builder()
                    .anio(anio)
                    .mes(mes)
                    .gastos(BigDecimal.ZERO)
                    .ingresos(BigDecimal.ZERO)
                    .comprasCredito(BigDecimal.ZERO)
                    .pagoResumen(BigDecimal.ZERO)
                    .idEspacioTrabajo(idEspacioTrabajo)
                    .build()
        );

        registro.actualizarComprasCredito(monto);
        gastosIngresosMensualesRepository.save(registro);
        log.info("Compras crédito mensuales anotadas: espacioId={}, anio={}, mes={}, comprasCredito={}",
                idEspacioTrabajo, anio, mes, registro.getComprasCredito());
    }

    /**
     * Elimina el monto de una compra con crédito del registro mensual (usada al remover una compra).
     * Usa la fecha real de la compra para determinar el anio/mes del registro.
     */
    private void compraCreditoMesDelete(BigDecimal monto, UUID idEspacioTrabajo, LocalDate fecha) {
        Integer anio = fecha.getYear();
        Integer mes = fecha.getMonthValue();

        Optional<GastosIngresosMensuales> opt = gastosIngresosMensualesRepository
                .findByIdEspacioTrabajoAndAnioAndMes(idEspacioTrabajo, anio, mes);

        GastosIngresosMensuales registro = opt.orElseThrow(() -> {
            String msg = "Registro de GastosIngresosMensuales no encontrado para espacioId=" + idEspacioTrabajo + ", anio=" + anio + ", mes=" + mes;
            log.warn(msg);
            return new EntityNotFoundException(msg);
        });

        registro.eliminarComprasCredito(monto);
        gastosIngresosMensualesRepository.save(registro);
        log.info("Compras crédito mensuales eliminadas: espacioId={}, anio={}, mes={}, comprasCredito={}",
                idEspacioTrabajo, anio, mes, registro.getComprasCredito());
    }

    /**
     * Anota el pago de un resumen en el registro del mes al que corresponde dicho resumen.
     * Usa la fecha del resumen (anio/mes del ciclo) para determinar el registro a actualizar.
     */
    private void pagoResumenMesAnotar(BigDecimal monto, UUID idEspacioTrabajo, LocalDate fecha) {
        Integer anio = fecha.getYear();
        Integer mes = fecha.getMonthValue();

        Optional<GastosIngresosMensuales> opt = gastosIngresosMensualesRepository
                .findByIdEspacioTrabajoAndAnioAndMes(idEspacioTrabajo, anio, mes);

        GastosIngresosMensuales registro = opt.orElseGet(() ->
            GastosIngresosMensuales.builder()
                    .anio(anio)
                    .mes(mes)
                    .gastos(BigDecimal.ZERO)
                    .ingresos(BigDecimal.ZERO)
                    .comprasCredito(BigDecimal.ZERO)
                    .pagoResumen(BigDecimal.ZERO)
                    .idEspacioTrabajo(idEspacioTrabajo)
                    .build()
        );

        registro.actualizarPagoResumen(monto);
        gastosIngresosMensualesRepository.save(registro);
        log.info("Pago de resumen mensual anotado: espacioId={}, anio={}, mes={}, pagoResumen={}",
                idEspacioTrabajo, anio, mes, registro.getPagoResumen());
    }
}

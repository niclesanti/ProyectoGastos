package com.campito.backend.transacciones.api;

import com.campito.backend.transacciones.domain.entity.CuotaCredito;
import com.campito.backend.transacciones.repository.CuotaCreditoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CuotasCreditoApiImpl implements CuotasCreditoApi {

    private final CuotaCreditoRepository cuotaCreditoRepository;
    private final TarjetaApi tarjetaApi;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularDeudaTotalPendiente(UUID idEspacio) {
        return cuotaCreditoRepository.calcularDeudaTotalPendiente(idEspacio);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal resumenMensual(UUID idEspacio, LocalDate now) {
        List<TarjetaApi.TarjetaResumen> tarjetas = tarjetaApi.listarParaCierre(idEspacio);

        if (tarjetas.isEmpty()) {
            return BigDecimal.ZERO;
        }

        LocalDate fechaInicioMinima = now;
        LocalDate fechaFinMaxima = now;

        for (TarjetaApi.TarjetaResumen tarjeta : tarjetas) {
            YearMonth ym = YearMonth.from(now);
            int diaAjustadoCierre = Math.min(tarjeta.diaCierre(), ym.lengthOfMonth());
            LocalDate fechaCierre = ym.atDay(diaAjustadoCierre);

            if (!fechaCierre.isAfter(now)) {
                YearMonth siguiente = ym.plusMonths(1);
                diaAjustadoCierre = Math.min(tarjeta.diaCierre(), siguiente.lengthOfMonth());
                fechaCierre = siguiente.atDay(diaAjustadoCierre);
            }

            LocalDate fechaInicio = fechaCierre.plusDays(1);
            LocalDate fechaFin = calcularFechaVencimiento(fechaCierre, tarjeta.diaVencimientoPago());

            if (fechaInicio.isBefore(fechaInicioMinima)) {
                fechaInicioMinima = fechaInicio;
            }
            if (fechaFin.isAfter(fechaFinMaxima)) {
                fechaFinMaxima = fechaFin;
            }
        }

        List<CuotaCredito> todasLasCuotasPendientes = cuotaCreditoRepository
            .findByEspacioTrabajoSinResumenEnRango(idEspacio, fechaInicioMinima, fechaFinMaxima);

        BigDecimal resumenMensual = BigDecimal.ZERO;

        for (TarjetaApi.TarjetaResumen tarjeta : tarjetas) {
            int diaCierre = tarjeta.diaCierre();

            YearMonth ym = YearMonth.from(now);
            int diaAjustadoCierre = Math.min(diaCierre, ym.lengthOfMonth());
            LocalDate fechaCierre = ym.atDay(diaAjustadoCierre);

            if (!fechaCierre.isAfter(now)) {
                YearMonth siguiente = ym.plusMonths(1);
                diaAjustadoCierre = Math.min(diaCierre, siguiente.lengthOfMonth());
                fechaCierre = siguiente.atDay(diaAjustadoCierre);
            }

            LocalDate fechaInicio = fechaCierre.plusDays(1);
            LocalDate fechaFin = calcularFechaVencimiento(fechaCierre, tarjeta.diaVencimientoPago());

            BigDecimal montoTarjeta = todasLasCuotasPendientes.stream()
                .filter(cuota -> cuota.getCompraCredito().getTarjeta().getId().equals(tarjeta.id()))
                .filter(cuota -> !cuota.getFechaVencimiento().isBefore(fechaInicio)
                              && !cuota.getFechaVencimiento().isAfter(fechaFin))
                .map(CuotaCredito::getMontoCuota)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            resumenMensual = resumenMensual.add(montoTarjeta);
        }

        return resumenMensual;
    }

    private LocalDate calcularFechaVencimiento(LocalDate fechaCierre, int diaVencimiento) {
        YearMonth mesActual = YearMonth.from(fechaCierre);
        YearMonth mesSiguiente = mesActual.plusMonths(1);
        int diaAjustado = Math.min(diaVencimiento, mesSiguiente.lengthOfMonth());
        return mesSiguiente.atDay(diaAjustado);
    }
}

package com.campito.backend.transacciones.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.campito.backend.common.domain.TipoTransaccion;

class CuentaBancariaBusinessTest {

    @Test
    void actualizarSaldoNuevaTransaccion_ingreso_suma() {
        CuentaBancaria cuenta = CuentaBancaria.builder()
            .saldoActual(new BigDecimal("1000.00"))
            .build();

        cuenta.actualizarSaldoNuevaTransaccion(new BigDecimal("500.00"), TipoTransaccion.INGRESO);

        assertEquals(new BigDecimal("1500.00"), cuenta.getSaldoActual());
    }

    @Test
    void actualizarSaldoNuevaTransaccion_gasto_resta() {
        CuentaBancaria cuenta = CuentaBancaria.builder()
            .saldoActual(new BigDecimal("1000.00"))
            .build();

        cuenta.actualizarSaldoNuevaTransaccion(new BigDecimal("300.00"), TipoTransaccion.GASTO);

        assertEquals(new BigDecimal("700.00"), cuenta.getSaldoActual());
    }

    @Test
    void actualizarSaldoEliminarTransaccion_ingreso_resta() {
        CuentaBancaria cuenta = CuentaBancaria.builder()
            .saldoActual(new BigDecimal("1500.00"))
            .build();

        cuenta.actualizarSaldoEliminarTransaccion(new BigDecimal("500.00"), TipoTransaccion.INGRESO);

        assertEquals(new BigDecimal("1000.00"), cuenta.getSaldoActual());
    }

    @Test
    void actualizarSaldoEliminarTransaccion_gasto_suma() {
        CuentaBancaria cuenta = CuentaBancaria.builder()
            .saldoActual(new BigDecimal("700.00"))
            .build();

        cuenta.actualizarSaldoEliminarTransaccion(new BigDecimal("300.00"), TipoTransaccion.GASTO);

        assertEquals(new BigDecimal("1000.00"), cuenta.getSaldoActual());
    }
}

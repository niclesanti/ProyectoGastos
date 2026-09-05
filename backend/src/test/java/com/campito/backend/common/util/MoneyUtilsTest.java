package com.campito.backend.common.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

class MoneyUtilsTest {

    // ===== of(double) =====

    @Test
    void of_double_retornaBigDecimalConScale2() {
        BigDecimal result = MoneyUtils.of(10.5);
        assertEquals(new BigDecimal("10.50"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void of_double_cero_retornaZero() {
        BigDecimal result = MoneyUtils.of(0.0);
        assertEquals(MoneyUtils.ZERO, result);
    }

    @Test
    void of_double_redondeaHalfUp() {
        // 1.005 con HALF_UP → 1.01
        BigDecimal result = MoneyUtils.of(1.005);
        assertEquals(new BigDecimal("1.01"), result);
    }

    // ===== of(String) =====

    @Test
    void of_string_retornaBigDecimalConScale2() {
        BigDecimal result = MoneyUtils.of("123.456");
        assertEquals(new BigDecimal("123.46"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void of_string_invalido_lanzaNumberFormatException() {
        assertThrows(NumberFormatException.class, () -> MoneyUtils.of("abc"));
    }

    @Test
    void of_string_vacio_lanzaNumberFormatException() {
        assertThrows(NumberFormatException.class, () -> MoneyUtils.of(""));
    }

    // ===== sum(List) =====

    @Test
    void sum_listaVacia_retornaZero() {
        assertEquals(MoneyUtils.ZERO, MoneyUtils.sum(List.of()));
    }

    @Test
    void sum_null_retornaZero() {
        assertEquals(MoneyUtils.ZERO, MoneyUtils.sum(null));
    }

    @Test
    void sum_sumaValoresCorrectamente() {
        List<BigDecimal> values = List.of(
            new BigDecimal("10.50"),
            new BigDecimal("20.30"),
            new BigDecimal("5.20")
        );
        BigDecimal result = MoneyUtils.sum(values);
        assertEquals(new BigDecimal("36.00"), result);
    }

    @Test
    void sum_unSoloElemento_retornaEseElementoEscalado() {
        List<BigDecimal> values = List.of(new BigDecimal("99.999"));
        BigDecimal result = MoneyUtils.sum(values);
        assertEquals(new BigDecimal("100.00"), result);
    }

    // ===== divide(BigDecimal, int) =====

    @Test
    void divide_divideCorrectamente() {
        BigDecimal result = MoneyUtils.divide(new BigDecimal("100.00"), 3);
        assertEquals(new BigDecimal("33.33"), result); // 33.333... → HALF_UP → 33.33
    }

    @Test
    void divide_divisorCero_lanzaArithmeticException() {
        assertThrows(ArithmeticException.class,
            () -> MoneyUtils.divide(new BigDecimal("100.00"), 0));
    }

    @Test
    void divide_divisorUno_retornaMismoValor() {
        BigDecimal result = MoneyUtils.divide(new BigDecimal("50.00"), 1);
        assertEquals(new BigDecimal("50.00"), result);
    }

    // ===== Comparaciones =====

    @Test
    void isGreaterThan_mayor_retornaTrue() {
        assertTrue(MoneyUtils.isGreaterThan(new BigDecimal("10.00"), new BigDecimal("5.00")));
    }

    @Test
    void isGreaterThan_igual_retornaFalse() {
        assertFalse(MoneyUtils.isGreaterThan(new BigDecimal("5.00"), new BigDecimal("5.00")));
    }

    @Test
    void isGreaterThanOrEqual_igual_retornaTrue() {
        assertTrue(MoneyUtils.isGreaterThanOrEqual(new BigDecimal("5.00"), new BigDecimal("5.00")));
    }

    @Test
    void isGreaterThanOrEqual_mayor_retornaTrue() {
        assertTrue(MoneyUtils.isGreaterThanOrEqual(new BigDecimal("10.00"), new BigDecimal("5.00")));
    }

    @Test
    void isLessThan_menor_retornaTrue() {
        assertTrue(MoneyUtils.isLessThan(new BigDecimal("3.00"), new BigDecimal("5.00")));
    }

    @Test
    void isLessThan_igual_retornaFalse() {
        assertFalse(MoneyUtils.isLessThan(new BigDecimal("5.00"), new BigDecimal("5.00")));
    }

    @Test
    void isEqual_mismoValor_retornaTrue() {
        assertTrue(MoneyUtils.isEqual(new BigDecimal("10.00"), new BigDecimal("10.00")));
    }

    @Test
    void isEqual_distintoValor_retornaFalse() {
        assertFalse(MoneyUtils.isEqual(new BigDecimal("10.00"), new BigDecimal("10.01")));
    }

    // ===== scale =====

    @Test
    void scale_normalizaEscala() {
        BigDecimal result = MoneyUtils.scale(new BigDecimal("10.5"));
        assertEquals(new BigDecimal("10.50"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void scale_null_retornaZero() {
        assertEquals(MoneyUtils.ZERO, MoneyUtils.scale(null));
    }

    // ===== ZERO =====

    @Test
    void zero_esCeroConScale2() {
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), MoneyUtils.ZERO);
    }
}

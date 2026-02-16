package com.campito.backend.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Clase utilitaria para operaciones seguras con valores monetarios.
 * 
 * Centraliza la lógica de redondeo y operaciones aritméticas con BigDecimal
 * para garantizar precisión en cálculos financieros.
 * 
 * Todas las operaciones usan escala de 2 decimales y RoundingMode.HALF_UP
 * (estándar bancario argentino).
 */
public final class MoneyUtils {

    /**
     * Constante para valores monetarios en cero.
     */
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    /**
     * Escala estándar para valores monetarios (2 decimales).
     */
    private static final int MONEY_SCALE = 2;

    /**
     * Modo de redondeo estándar: HALF_UP (medio redondea hacia arriba).
     */
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Constructor privado para prevenir instanciación.
     */
    private MoneyUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Crea un BigDecimal monetario a partir de un valor double.
     *
     * @param value valor numérico
     * @return BigDecimal escalado a 2 decimales con redondeo HALF_UP
     */
    public static BigDecimal of(double value) {
        return BigDecimal.valueOf(value).setScale(MONEY_SCALE, ROUNDING_MODE);
    }

    /**
     * Crea un BigDecimal monetario a partir de un String.
     *
     * @param value valor numérico como String
     * @return BigDecimal escalado a 2 decimales con redondeo HALF_UP
     * @throws NumberFormatException si el String no es un número válido
     */
    public static BigDecimal of(String value) {
        return new BigDecimal(value).setScale(MONEY_SCALE, ROUNDING_MODE);
    }

    /**
     * Suma una lista de valores monetarios de forma segura.
     *
     * @param values lista de BigDecimal a sumar
     * @return suma total con escala de 2 decimales
     */
    public static BigDecimal sum(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return ZERO;
        }
        return values.stream()
                .reduce(ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, ROUNDING_MODE);
    }

    /**
     * Divide un monto entre un número entero con redondeo seguro.
     *
     * @param amount   monto a dividir
     * @param divisor  número de partes
     * @return resultado de la división escalado a 2 decimales con redondeo HALF_UP
     * @throws ArithmeticException si divisor es 0
     */
    public static BigDecimal divide(BigDecimal amount, int divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return amount.divide(BigDecimal.valueOf(divisor), MONEY_SCALE, ROUNDING_MODE);
    }

    /**
     * Compara si el primer valor es mayor que el segundo.
     *
     * @param first  primer valor
     * @param second segundo valor
     * @return true si first > second
     */
    public static boolean isGreaterThan(BigDecimal first, BigDecimal second) {
        return first.compareTo(second) > 0;
    }

    /**
     * Compara si el primer valor es mayor o igual que el segundo.
     *
     * @param first  primer valor
     * @param second segundo valor
     * @return true si first >= second
     */
    public static boolean isGreaterThanOrEqual(BigDecimal first, BigDecimal second) {
        return first.compareTo(second) >= 0;
    }

    /**
     * Compara si el primer valor es menor que el segundo.
     *
     * @param first  primer valor
     * @param second segundo valor
     * @return true si first < second
     */
    public static boolean isLessThan(BigDecimal first, BigDecimal second) {
        return first.compareTo(second) < 0;
    }

    /**
     * Compara si dos valores monetarios son iguales.
     *
     * @param first  primer valor
     * @param second segundo valor
     * @return true si ambos valores son iguales (considerando escala)
     */
    public static boolean isEqual(BigDecimal first, BigDecimal second) {
        return first.compareTo(second) == 0;
    }

    /**
     * Asegura que un BigDecimal tenga la escala monetaria correcta.
     *
     * @param value valor a escalar
     * @return valor con escala de 2 decimales
     */
    public static BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return value.setScale(MONEY_SCALE, ROUNDING_MODE);
    }
}

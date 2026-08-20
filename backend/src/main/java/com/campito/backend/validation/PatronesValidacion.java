package com.campito.backend.validation;

/**
 * Patrones regex centralizados para la validación de texto.
 * Charset permitido: letras (incluidas acentuadas: áéíóú ÁÉÍÓÚ ñÑ üÜ),
 * números, coma, paréntesis, guión bajo, guión, barra y espacios.
 */
public final class PatronesValidacion {

    public static final String NOMBRE_PATTERN = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ,()_\\-/\\s]*$";
    public static final String DESCRIPCION_PATTERN = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ,()_\\-/\\s]*$";

    private PatronesValidacion() {
        // Clase de utilidad, no instanciable
    }
}
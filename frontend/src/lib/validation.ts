/**
 * Expresión regular para validar texto de uso general en inputs.
 * Permite letras (incluidas las acentuadas del español y ñ/Ñ/ü/Ü),
 * números, coma, paréntesis, guiones, barra y espacios.
 * NO incluye '@' ni '.': los emails y campos numéricos tienen su
 * propia validación específica.
 */
export const REGEX_TEXTO_VALIDO = /^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ,()_\-/\s]*$/

/** Caracteres no permitidos por {@link REGEX_TEXTO_VALIDO}, para filtrar entradas. */
const REGEX_CARACTERES_NO_PERMITIDOS = /[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ,()_\-/\s]/g

/**
 * Elimina de un valor de entrada los caracteres que no están permitidos
 * por {@link REGEX_TEXTO_VALIDO}. No aplica a emails ni campos numéricos,
 * que mantienen su propia validación.
 */
export function filtrarTextoPermitido(value: string): string {
  return value.replace(REGEX_CARACTERES_NO_PERMITIDOS, '')
}
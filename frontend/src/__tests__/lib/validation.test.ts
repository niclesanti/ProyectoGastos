import { describe, it, expect } from 'vitest'
import { REGEX_TEXTO_VALIDO, filtrarTextoPermitido } from '@/lib/validation'

describe('filtrarTextoPermitido', () => {
  it('mantiene letras acentuadas y espacios', () => {
    expect(filtrarTextoPermitido('Gómez Ávila')).toBe('Gómez Ávila')
  })

  it('mantiene ñ, ü y mayúsculas acentuadas', () => {
    expect(filtrarTextoPermitido('Ñandú ü Ü')).toBe('Ñandú ü Ü')
  })

  it('mantiene caracteres permitidos: paréntesis, números, coma, guiones, barra y barra baja', () => {
    expect(filtrarTextoPermitido('Café (1/2), hola_mundo - ok')).toBe('Café (1/2), hola_mundo - ok')
  })

  it('elimina caracteres no permitidos como #, ! y @', () => {
    expect(filtrarTextoPermitido('Café#!@')).toBe('Café')
  })
})

describe('REGEX_TEXTO_VALIDO', () => {
  it('acepta texto con acentos del español', () => {
    expect(REGEX_TEXTO_VALIDO.test('Cafetería ñuñez')).toBe(true)
  })

  it('rechaza el carácter #', () => {
    expect(REGEX_TEXTO_VALIDO.test('Café#')).toBe(false)
  })

  it('rechaza el carácter !', () => {
    expect(REGEX_TEXTO_VALIDO.test('hola!')).toBe(false)
  })

  it('acepta string vacío', () => {
    expect(REGEX_TEXTO_VALIDO.test('')).toBe(true)
  })
})
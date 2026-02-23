import { useEffect, useState } from 'react'

/**
 * Hook que detecta el teclado virtual en móviles y proporciona la altura
 * del viewport visible (por encima del teclado).
 *
 * Estrategia:
 *  - Usa `window.visualViewport` (iOS Safari 13+, Chrome 61+, Firefox 63+)
 *    que siempre refleja el área visible real: se reduce cuando el teclado abre.
 *  - Combinado con `interactive-widget=resizes-visual` en el viewport meta,
 *    el layout NO se desplaza cuando el teclado aparece; el teclado simplemente
 *    se superpone. El `visualViewport.height` aun así se reduce, lo que nos
 *    permite calcular el espacio disponible correctamente.
 *
 * Uso típico:
 *  const { isKeyboardOpen, viewportHeight } = useVirtualKeyboard()
 *  // Aplicar viewportHeight como max-height del modal cuando el teclado está abierto
 */
export function useVirtualKeyboard() {
  const getViewportHeight = () =>
    window.visualViewport?.height ?? window.innerHeight

  const [viewportHeight, setViewportHeight] = useState(getViewportHeight)
  const [isKeyboardOpen, setIsKeyboardOpen] = useState(false)

  useEffect(() => {
    const viewport = window.visualViewport
    if (!viewport) return

    // Guardamos la altura "en reposo" para calcular si el teclado está abierto.
    // Se actualiza al cerrar el teclado para contemplar cambios de orientación.
    let restHeight = viewport.height

    const handleResize = () => {
      const currentHeight = viewport.height
      setViewportHeight(currentHeight)

      // Umbral de 150px: teclados en dispositivos pequeños pueden ser muy grandes.
      // Consideramos que el teclado está abierto si el viewport se redujo > 150px.
      const diff = restHeight - currentHeight
      const keyboardOpen = diff > 150

      if (!keyboardOpen) {
        // Al cerrar el teclado, actualizamos la altura de reposo
        // (cubre cambios de orientación de pantalla)
        restHeight = currentHeight
      }

      setIsKeyboardOpen(keyboardOpen)
    }

    viewport.addEventListener('resize', handleResize)
    return () => viewport.removeEventListener('resize', handleResize)
  }, [])

  return { isKeyboardOpen, viewportHeight }
}

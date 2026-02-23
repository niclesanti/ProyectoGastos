import { useEffect, useRef, useState } from 'react'

interface VirtualKeyboardState {
  isKeyboardOpen: boolean
  /** Altura del viewport visible (por encima del teclado) */
  viewportHeight: number
  /**
   * Offset top del visualViewport respecto al layout viewport.
   * En Android siempre es 0. En iOS cambia cuando el sistema hace scroll
   * automático hacia el campo enfocado, por lo que es crítico para calcular
   * el espacio real disponible: maxHeight = viewportHeight - viewportOffsetTop
   */
  viewportOffsetTop: number
}

/**
 * Hook que detecta el teclado virtual en mobile de forma confiable
 * en Android y iOS (incluyendo Chrome en iOS que usa WKWebView).
 *
 * Estrategia:
 * - Usa window.visualViewport para detectar cambios de tamaño y offset
 * - Captura la altura inicial en reposo como referencia estable
 * - Escucha los eventos 'resize' y 'scroll' del visualViewport
 *   ('scroll' es necesario en iOS donde offsetTop cambia al enfocar un input)
 * - Umbral de 150px para evitar falsos positivos por la barra del navegador
 *
 * PC: el hook corre pero isMobile=false en ResponsiveModal, resultado ignorado.
 * Android: offsetTop siempre 0 → fórmula equivalente a la versión anterior.
 * iOS: offsetTop varía → fórmula viewportHeight - offsetTop calcula el espacio real.
 */
export function useVirtualKeyboard(): VirtualKeyboardState {
  const restingHeightRef = useRef<number | null>(null)

  const [state, setState] = useState<VirtualKeyboardState>(() => ({
    isKeyboardOpen: false,
    viewportHeight: window.visualViewport?.height ?? window.innerHeight,
    viewportOffsetTop: window.visualViewport?.offsetTop ?? 0,
  }))

  useEffect(() => {
    const viewport = window.visualViewport
    if (!viewport) return

    // Delay para asegurar que el viewport esté completamente inicializado.
    // Es especialmente importante en iOS donde el valor inicial puede ser incorrecto.
    const initTimer = setTimeout(() => {
      restingHeightRef.current = viewport.height
    }, 300)

    const handleChange = () => {
      const currentHeight = viewport.height
      const offsetTop = viewport.offsetTop
      const resting = restingHeightRef.current ?? currentHeight

      const diff = resting - currentHeight

      // Umbral de 150px: distingue el teclado de cambios menores
      // (como la barra de URL del navegador que se oculta al hacer scroll)
      const keyboardIsOpen = diff > 150

      if (!keyboardIsOpen) {
        // Al cerrar el teclado, actualizamos la referencia de reposo
        // para cubrir cambios de orientación de pantalla
        restingHeightRef.current = currentHeight
      }

      setState({
        isKeyboardOpen: keyboardIsOpen,
        viewportHeight: currentHeight,
        viewportOffsetTop: offsetTop,
      })
    }

    // 'resize': se dispara al abrir/cerrar el teclado en todos los navegadores
    // 'scroll': necesario en iOS donde offsetTop cambia al enfocar un input
    viewport.addEventListener('resize', handleChange)
    viewport.addEventListener('scroll', handleChange)

    return () => {
      clearTimeout(initTimer)
      viewport.removeEventListener('resize', handleChange)
      viewport.removeEventListener('scroll', handleChange)
    }
  }, [])

  return state
}

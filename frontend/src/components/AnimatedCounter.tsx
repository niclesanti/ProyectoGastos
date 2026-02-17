import { useEffect, useRef } from 'react'
import { motion, useSpring, useTransform, useInView } from 'framer-motion'

interface AnimatedCounterProps {
  value: number | string
  /**
   * Formato de número (para locales, etc)
   */
  formatFn?: (value: number) => string
  /**
   * Clase CSS adicional
   */
  className?: string
  /**
   * Animar solo cuando esté en viewport
   * @default true
   */
  animateOnView?: boolean
}

/**
 * AnimatedCounter - Contador animado con spring physics
 * 
 * El contador "corre" desde 0 hasta el valor final de forma orgánica.
 * Perfecto para montos monetarios y estadísticas en el dashboard.
 * 
 * Características:
 * - Spring physics natural
 * - Lazy loading (solo anima cuando está en viewport)
 * - Formato personalizable
 * - Zero layout shift
 */
export function AnimatedCounter({
  value,
  formatFn,
  className = '',
  animateOnView = true,
}: AnimatedCounterProps) {
  const ref = useRef<HTMLSpanElement>(null)
  const isInView = useInView(ref, { once: true, amount: 0.5 })
  
  // Convertir string a número si es necesario
  const numericValue = typeof value === 'string' 
    ? parseFloat(value.replace(/[^0-9.-]/g, '')) 
    : value

  // Spring animation config
  const spring = useSpring(0, {
    stiffness: 80,
    damping: 20,
    mass: 1,
  })

  // Transform spring value to rounded integer
  const display = useTransform(spring, (current) =>
    formatFn ? formatFn(Math.floor(current)) : Math.floor(current).toLocaleString()
  )

  useEffect(() => {
    if (!animateOnView || isInView) {
      // Pequeño delay para efecto staggered cuando hay múltiples contadores
      const timeout = setTimeout(() => {
        spring.set(numericValue)
      }, 50)
      
      return () => clearTimeout(timeout)
    }
  }, [spring, numericValue, isInView, animateOnView])

  return (
    <motion.span
      ref={ref}
      className={className}
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.3 }}
    >
      {display}
    </motion.span>
  )
}

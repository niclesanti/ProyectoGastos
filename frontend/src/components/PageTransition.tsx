import { motion, AnimatePresence } from 'framer-motion'
import { useLocation, useOutlet } from 'react-router-dom'

/**
 * PageTransition - Wrapper para transiciones de página suaves
 * 
 * Características:
 * - Fade in/out suave
 * - Slide vertical sutil
 * - Spring physics para movimiento orgánico
 * - Reduce carga cognitiva al cambiar contexto
 */
export function PageTransition() {
  const location = useLocation()
  const outlet = useOutlet()

  return (
    <AnimatePresence mode="wait" initial={false}>
      <motion.div
        key={location.pathname}
        initial={{ opacity: 0, y: 10 }}
        animate={{ 
          opacity: 1, 
          y: 0,
          transition: {
            type: 'spring',
            stiffness: 380,
            damping: 30,
            mass: 0.8,
          }
        }}
        exit={{ 
          opacity: 0,
          y: -10,
          transition: {
            duration: 0.15,
            ease: 'easeInOut'
          }
        }}
        className="w-full h-full"
      >
        {outlet}
      </motion.div>
    </AnimatePresence>
  )
}

import { cn } from '@/lib/utils'
import { ReactNode } from 'react'

interface EmptyStateProps {
  /**
   * Ilustración SVG o icono a mostrar
   */
  illustration: ReactNode
  /**
   * Título principal del empty state
   */
  title: string
  /**
   * Descripción o mensaje accionable
   */
  description: string
  /**
   * Clases adicionales para el contenedor
   */
  className?: string
  /**
   * Tamaño de la ilustración
   * @default 'md'
   */
  size?: 'sm' | 'md' | 'lg'
}

const sizeMap = {
  sm: 'w-24 h-24 sm:w-28 sm:h-28',
  md: 'w-32 h-32 sm:w-40 sm:h-40',
  lg: 'w-40 h-40 sm:w-48 sm:h-48',
}

/**
 * Componente reutilizable para mostrar estados vacíos de forma profesional
 * 
 * Implementa las recomendaciones de diseño UX para Empty States:
 * - Ilustración minimalista con baja opacidad
 * - Mensaje claro y accionable
 * - Estética consistente con el diseño del sistema
 */
export function EmptyState({
  illustration,
  title,
  description,
  className,
  size = 'md',
}: EmptyStateProps) {
  return (
    <div
      className={cn(
        'flex flex-col items-center justify-center py-8 sm:py-12 px-4 text-center',
        'min-h-[200px] sm:min-h-[250px]',
        className
      )}
    >
      {/* Ilustración con opacidad reducida para estética minimalista */}
      <div className={cn('mb-4 sm:mb-6 opacity-[0.15]', sizeMap[size])}>
        {illustration}
      </div>

      {/* Título - Color muted para indicar estado temporal */}
      <h3 className="text-base sm:text-lg font-semibold text-foreground/70 mb-2">
        {title}
      </h3>

      {/* Descripción - Texto más tenue, accionable */}
      <p className="text-xs sm:text-sm text-muted-foreground/80 max-w-md leading-relaxed">
        {description}
      </p>
    </div>
  )
}

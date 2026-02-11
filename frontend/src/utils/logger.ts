/**
 * Utilidad de logging condicional para desarrollo.
 * 
 * Los logs solo se muestran en entorno de desarrollo (DEV).
 * En producción, estos logs son silenciados por seguridad para evitar
 * exponer información sensible como tokens, URLs internas, etc.
 * 
 * @example
 * devLog('Usuario autenticado:', usuario)
 * devError('Error al procesar:', error)
 * devWarn('Caché expirado')
 */

const isDevelopment = import.meta.env.DEV

/**
 * Log condicional - solo en desarrollo
 */
export const devLog = (...args: any[]): void => {
  if (isDevelopment) {
    console.log(...args)
  }
}

/**
 * Error log condicional - solo en desarrollo
 */
export const devError = (...args: any[]): void => {
  if (isDevelopment) {
    console.error(...args)
  }
}

/**
 * Warning log condicional - solo en desarrollo
 */
export const devWarn = (...args: any[]): void => {
  if (isDevelopment) {
    console.warn(...args)
  }
}

/**
 * Info log condicional - solo en desarrollo
 */
export const devInfo = (...args: any[]): void => {
  if (isDevelopment) {
    console.info(...args)
  }
}

/**
 * Debug log condicional - solo en desarrollo
 */
export const devDebug = (...args: any[]): void => {
  if (isDevelopment) {
    console.debug(...args)
  }
}

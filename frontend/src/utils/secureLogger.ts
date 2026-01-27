/**
 * Sistema de logging seguro que sanitiza información sensible
 * y puede ser deshabilitado en producción.
 */

// Detectar si estamos en producción
const IS_PRODUCTION = import.meta.env.MODE === 'production';

/**
 * Sanitiza tokens JWT para logging seguro.
 * Muestra solo los primeros y últimos 10 caracteres.
 */
export const sanitizeToken = (token: string | null | undefined): string => {
  if (!token) return 'null';
  if (token.length < 20) return '[token-inválido]';
  return `${token.substring(0, 10)}...${token.substring(token.length - 10)}`;
};

/**
 * Sanitiza objetos que puedan contener información sensible
 */
export const sanitizeData = (data: any): any => {
  if (!data || typeof data !== 'object') return data;
  
  const sanitized = { ...data };
  
  // Lista de campos sensibles a sanitizar
  const sensitiveFields = ['token', 'password', 'jwt', 'authorization', 'secret'];
  
  for (const key in sanitized) {
    if (sensitiveFields.some(field => key.toLowerCase().includes(field))) {
      if (typeof sanitized[key] === 'string') {
        sanitized[key] = sanitizeToken(sanitized[key]);
      }
    }
  }
  
  return sanitized;
};

/**
 * Logger seguro para desarrollo.
 * En producción, no emite logs sensibles.
 */
export const secureLog = {
  info: (message: string, data?: any) => {
    if (!IS_PRODUCTION) {
      console.log(message, data ? sanitizeData(data) : '');
    }
  },
  
  warn: (message: string, data?: any) => {
    if (!IS_PRODUCTION) {
      console.warn(message, data ? sanitizeData(data) : '');
    }
  },
  
  error: (message: string, error?: any) => {
    // Los errores siempre se logean, pero sanitizados
    console.error(message, error ? sanitizeData(error) : '');
  },
  
  debug: (message: string, data?: any) => {
    if (!IS_PRODUCTION && import.meta.env.VITE_DEBUG === 'true') {
      console.debug(message, data ? sanitizeData(data) : '');
    }
  }
};

/**
 * Logger específico para autenticación con sanitización automática
 */
export const authLog = {
  info: (message: string, data?: any) => {
    if (!IS_PRODUCTION) {
      const sanitized = data ? sanitizeData(data) : undefined;
      console.log(`🔐 [Auth] ${message}`, sanitized || '');
    }
  },
  
  error: (message: string, error?: any) => {
    console.error(`🔐❌ [Auth] ${message}`, error ? sanitizeData(error) : '');
  },
  
  success: (message: string) => {
    if (!IS_PRODUCTION) {
      console.log(`🔐✅ [Auth] ${message}`);
    }
  }
};

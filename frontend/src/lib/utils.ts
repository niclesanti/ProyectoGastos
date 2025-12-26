import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatCurrency(amount: number): string {
  const formatted = new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    minimumFractionDigits: 2,
  }).format(Math.abs(amount))
  // Eliminar espacio después del símbolo $ para formato más compacto
  return formatted.replace('$ ', '$')
}

export function formatDate(date: string | Date): string {
  const dateObj = typeof date === 'string' ? new Date(date) : date
  return new Intl.DateTimeFormat('es-AR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(dateObj)
}

export function formatDateTime(date: string | Date): string {
  const dateObj = typeof date === 'string' ? new Date(date) : date
  return new Intl.DateTimeFormat('es-AR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(dateObj)
}

/**
 * Genera una paleta de colores HSL profesionales para gráficos en dark mode.
 * Los colores tienen baja saturación (30-40%) para un look sofisticado tipo fintech.
 * 
 * @param count - Número de colores a generar
 * @returns Array de strings HSL en formato "hue saturation% lightness%"
 */
export function generateChartColors(count: number): string[] {
  const colors: string[] = []
  
  // Base de tonos (hue) distribuidos uniformemente en el círculo cromático
  // Evitamos el rango 80-120 (amarillos/verdes muy brillantes que no se ven bien)
  const hueRanges = [
    { start: 200, end: 240 },  // Azules
    { start: 150, end: 180 },  // Verdes esmeralda
    { start: 20, end: 40 },    // Naranjas/ámbar
    { start: 260, end: 290 },  // Violetas
    { start: 320, end: 350 },  // Rosas/magenta
    { start: 0, end: 15 },     // Rojos
    { start: 180, end: 200 },  // Cianes
  ]
  
  for (let i = 0; i < count; i++) {
    // Seleccionar rango de tonos de forma circular
    const rangeIndex = i % hueRanges.length
    const range = hueRanges[rangeIndex]
    
    // Calcular hue específico dentro del rango
    const hueOffset = Math.floor((i / hueRanges.length)) * 10
    let hue = range.start + hueOffset
    
    // Si nos pasamos del rango, volver al inicio con variación
    if (hue > range.end) {
      hue = range.start + ((hue - range.end) % (range.end - range.start))
    }
    
    // Saturación baja para look profesional (30-40%)
    // Alternamos entre 30% y 40% para más variedad sutil
    const saturation = i % 2 === 0 ? 35 : 30
    
    // Luminosidad entre 50-60% para buena visibilidad en dark mode
    // Variamos ligeramente para evitar colores idénticos
    const lightness = 50 + (i % 3) * 3
    
    colors.push(`${hue} ${saturation}% ${lightness}%`)
  }
  
  return colors
}

/**
 * Convierte un color HSL en formato string a formato CSS
 * @param hsl - Color en formato "hue saturation% lightness%"
 * @returns Color en formato CSS "hsl(hue, saturation%, lightness%)"
 */
export function hslToCSS(hsl: string): string {
  const [h, s, l] = hsl.split(' ')
  return `hsl(${h}, ${s}, ${l})`
}

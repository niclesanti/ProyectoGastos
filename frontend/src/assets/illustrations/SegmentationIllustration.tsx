/**
 * Ilustración para Empty State de segmentación y categorías
 * Optimizada para tema dark con colores personalizables
 */
export function SegmentationIllustration({ className = '' }: { className?: string }) {
  return (
    <svg
      className={className}
      viewBox="0 0 706.54 428.76"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      role="img"
      aria-label="Ilustración de segmentación"
    >
      {/* Marco de la ventana/dashboard */}
      <path
        d="M862.452 235.62h-607a8.728 8.728 0 00-8.72 8.72v411.32a8.728 8.728 0 008.72 8.72h607a8.712 8.712 0 006.63-3.06 8.167 8.167 0 001.25-2.11 8.507 8.507 0 00.66-3.31V244.34a8.73 8.73 0 00-8.72-8.72zm6.24 420.04a6.175 6.175 0 01-1.03 3.42 6.446 6.446 0 01-2.36 2.12 6.184 6.184 0 01-2.85.69h-607a6.238 6.238 0 01-6.23-6.23V244.34a6.238 6.238 0 016.23-6.23h607a6.24 6.24 0 016.24 6.23z"
        transform="translate(-246.732 -235.62)"
        fill="hsl(var(--muted))"
        fillOpacity="0.3"
      />
      {/* Header bar */}
      <rect
        x="1.243"
        y="34.949"
        width="621.957"
        height="2.493"
        fill="hsl(var(--muted))"
        fillOpacity="0.4"
      />
      {/* Círculos de control (esquina superior) */}
      <circle cx="22.432" cy="18.696" r="7.478" fill="hsl(var(--muted))" fillOpacity="0.35" />
      <circle cx="43.932" cy="18.696" r="7.478" fill="hsl(var(--muted))" fillOpacity="0.35" />
      <circle cx="65.433" cy="18.696" r="7.478" fill="hsl(var(--muted))" fillOpacity="0.35" />
      {/* Cuadros de categorías */}
      <path
        d="M321.784 427.366h-22a4.505 4.505 0 01-4.5-4.5v-22a4.505 4.505 0 014.5-4.5h22a4.505 4.505 0 014.5 4.5v22a4.505 4.505 0 01-4.5 4.5z"
        transform="translate(-246.732 -235.62)"
        fill="hsl(var(--muted))"
        fillOpacity="0.4"
      />
      <path
        d="M321.784 484.366h-22a4.505 4.505 0 01-4.5-4.5v-22a4.505 4.505 0 014.5-4.5h22a4.505 4.505 0 014.5 4.5v22a4.505 4.505 0 01-4.5 4.5z"
        transform="translate(-246.732 -235.62)"
        fill="hsl(var(--primary))"
        fillOpacity="0.25"
      />
      <path
        d="M321.784 541.366h-22a4.505 4.505 0 01-4.5-4.5v-22a4.505 4.505 0 014.5-4.5h22a4.505 4.505 0 014.5 4.5v22a4.505 4.505 0 01-4.5 4.5z"
        transform="translate(-246.732 -235.62)"
        fill="hsl(var(--muted))"
        fillOpacity="0.35"
      />
      {/* Gráfico de torta (pie chart) */}
      <path
        d="M834.536 470.866c.158-2.647.248-5.313.248-8a132 132 0 00-132-132v140z"
        transform="translate(-246.732 -235.62)"
        fill="hsl(var(--muted))"
        fillOpacity="0.4"
      />
      <path
        d="M692.784 482.866v-140a132 132 0 10131.752 140z"
        transform="translate(-246.732 -235.62)"
        fill="hsl(var(--primary))"
        fillOpacity="0.3"
      />
    </svg>
  )
}

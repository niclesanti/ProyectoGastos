/**
 * Ilustración para Empty State de gráficos y datos visuales
 * Optimizada para tema dark con colores personalizables
 */
export function VisualDataIllustration({ className = '' }: { className?: string }) {
  return (
    <svg
      className={className}
      viewBox="0 0 800 670"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      role="img"
      aria-label="Ilustración de datos visuales"
    >
      {/* Barras del gráfico */}
      <path
        d="M368.748 780.692V410.673a8.593 8.593 0 018.593-8.593h38.345a8.593 8.593 0 018.593 8.593V781.656z"
        transform="translate(451.961 58.271)"
        fill="hsl(var(--muted))"
        fillOpacity="0.6"
      />
      <path
        d="M498.748 783.888V489.673a8.593 8.593 0 018.593-8.593h38.345a8.593 8.593 0 018.593 8.593V783.254z"
        transform="translate(448.61 56.234)"
        fill="hsl(var(--muted))"
        fillOpacity="0.7"
      />
      <path
        d="M758.748 779.008V352.538c0-5.469 3.847-9.9 8.593-9.9h38.345c4.745 0 8.593 4.434 8.593 9.9V780.124z"
        transform="translate(441.908 59.803)"
        fill="hsl(var(--muted))"
        fillOpacity="0.5"
      />
      <path
        d="M238.747 785.574V579.681c0-2.64 3.847-4.781 8.593-4.781h38.345c4.745 0 8.593 2.143 8.593 4.781v206.43z"
        transform="translate(455.312 53.816)"
        fill="hsl(var(--muted))"
        fillOpacity="0.4"
      />
      <path
        d="M628.747 777.3V293.338c0-6.207 3.847-11.239 8.593-11.239h38.345c4.745 0 8.593 5.032 8.593 11.239V778.564z"
        transform="translate(445.259 61.363)"
        fill="hsl(var(--muted))"
        fillOpacity="0.55"
      />
      {/* Línea de tendencia */}
      <path
        d="M267.67 425.274a2.922 2.922 0 01-2.175-4.874L393.707 277.393l126.18 70.855 126.93-218.711L776.124 235.966a2.923 2.923 0 01-3.715 4.513L648.418 138.425 522.05 356.166 394.931 284.784 269.847 424.3a2.915 2.915 0 01-2.177.974z"
        transform="translate(454.642 65.296)"
        fill="hsl(var(--primary))"
        fillOpacity="0.3"
      />
      {/* Puntos de datos */}
      <circle
        cx="27.278"
        cy="27.278"
        r="27.278"
        transform="translate(694.547 460.37)"
        fill="hsl(var(--primary))"
        fillOpacity="0.25"
      />
      <circle
        cx="27.278"
        cy="27.278"
        r="27.278"
        transform="translate(821.196 319.108)"
        fill="hsl(var(--muted-foreground))"
        fillOpacity="0.2"
      />
      <circle
        cx="27.278"
        cy="27.278"
        r="27.278"
        transform="translate(947.845 390.226)"
        fill="hsl(var(--muted-foreground))"
        fillOpacity="0.2"
      />
      <circle
        cx="27.278"
        cy="27.278"
        r="27.278"
        transform="translate(1074.494 172)"
        fill="hsl(var(--primary))"
        fillOpacity="0.25"
      />
      {/* Base line */}
      <path
        d="M988.443 793.849H190.39a.974.974 0 010-1.948h798.052a.974.974 0 010 1.948z"
        transform="translate(456.584 48.222)"
        fill="hsl(var(--border))"
      />
    </svg>
  )
}

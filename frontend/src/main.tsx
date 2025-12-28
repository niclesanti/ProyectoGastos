import React from 'react'
import ReactDOM from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import App from './App.tsx'
import './index.css'

// Apply dark mode by default
document.documentElement.classList.add('dark')

// Configuración del QueryClient con opciones profesionales
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // Los datos son "frescos" por 5 minutos
      gcTime: 1000 * 60 * 10, // Mantener en caché por 10 minutos
      retry: 1, // Reintentar 1 vez en caso de error
      refetchOnWindowFocus: false, // No refrescar al volver a la ventana
    },
  },
})

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </React.StrictMode>,
)

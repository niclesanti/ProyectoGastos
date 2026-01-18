import axios from 'axios'

// Usar variable de entorno para la URL del backend
// En desarrollo: http://localhost:8080 (desde .env.local o docker-compose)
// En producción: https://tu-backend.cloud.run (desde variables de entorno del hosting)
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export const apiClient = axios.create({
  baseURL: `${API_BASE_URL}/api`,
  withCredentials: true, // Vital para OAuth2/Session - envía cookies automáticamente
})

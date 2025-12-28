import axios from 'axios'

export const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true, // Vital para OAuth2/Session - envía cookies automáticamente
})

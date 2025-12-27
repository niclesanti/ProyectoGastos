import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { AuthProvider } from '@/contexts/AuthContext'
import { ProtectedRoute } from '@/components/ProtectedRoute'
import { DashboardLayout } from '@/layouts/DashboardLayout'
import { DashboardPage } from '@/pages/DashboardPage'
import { MovimientosPage } from '@/pages/MovimientosPage'
import { CreditosPage } from '@/pages/CreditosPage'
import { ConfiguracionPage } from '@/pages/ConfiguracionPage'
import { LoginPage } from '@/pages/LoginPage'

function App() {
  return (
    <Router>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <DashboardLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<DashboardPage />} />
            <Route path="movimientos" element={<MovimientosPage />} />
            <Route path="creditos" element={<CreditosPage />} />
            <Route path="configuracion" element={<ConfiguracionPage />} />
          </Route>
        </Routes>
      </AuthProvider>
    </Router>
  )
}

export default App

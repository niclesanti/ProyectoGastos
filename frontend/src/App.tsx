import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { DashboardLayout } from '@/layouts/DashboardLayout'
import { DashboardPage } from '@/pages/DashboardPage'
import { MovimientosPage } from '@/pages/MovimientosPage'
import { CreditosPage } from '@/pages/CreditosPage'
import { ConfiguracionPage } from '@/pages/ConfiguracionPage'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<DashboardLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="movimientos" element={<MovimientosPage />} />
          <Route path="creditos" element={<CreditosPage />} />
          <Route path="configuracion" element={<ConfiguracionPage />} />
        </Route>
      </Routes>
    </Router>
  )
}

export default App

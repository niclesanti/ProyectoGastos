import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import { AuthProvider } from '@/contexts/AuthContext'
import { ProtectedRoute } from '@/components/ProtectedRoute'
import { DashboardLayout } from '@/layouts/DashboardLayout'
import { DashboardPage } from '@/pages/DashboardPage'
import { MovimientosPage } from '@/pages/MovimientosPage'
import { CreditosPage } from '@/pages/CreditosPage'
import { ConfiguracionPage } from '@/pages/ConfiguracionPage'
import { LoginPage } from '@/pages/LoginPage'
import { OAuthCallback } from '@/pages/OAuthCallback'
import { Toaster } from 'sonner'

const router = createBrowserRouter(
  [
    { path: '/login', element: <LoginPage /> },
    { path: '/oauth-callback', element: <OAuthCallback /> },
    {
      path: '/',
      element: (
        <ProtectedRoute>
          <DashboardLayout />
        </ProtectedRoute>
      ),
      children: [
        { index: true, element: <DashboardPage /> },
        { path: 'movimientos', element: <MovimientosPage /> },
        { path: 'creditos', element: <CreditosPage /> },
        { path: 'configuracion', element: <ConfiguracionPage /> },
      ],
    },
  ],
  // TypeScript types for FutureConfig in this version may not include the newer flags yet,
  // cast to `any` to apply the runtime flags without changing behavior.
  ({
    future: {
      v7_startTransition: true,
      v7_relativeSplatPath: true,
    },
  } as any)
)

function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
      <Toaster richColors position="top-right" />
    </AuthProvider>
  )
}

export default App

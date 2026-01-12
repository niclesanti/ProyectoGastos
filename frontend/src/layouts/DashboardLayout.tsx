import { Outlet } from 'react-router-dom'
import { AppSidebar } from '@/components/Sidebar'
import { Header } from '@/components/Header'
import { SidebarProvider, SidebarInset } from '@/components/ui/sidebar'
import { MobileActionsFAB } from '@/components/MobileActionsFAB'

export function DashboardLayout() {
  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset className="overflow-x-hidden">
        <Header />
        <main className="flex flex-1 flex-col gap-4 px-3 py-4 pt-0 sm:px-4 md:p-4 md:pt-0 overflow-x-hidden">
          <Outlet />
        </main>
        {/* Floating Action Button para móviles */}
        <MobileActionsFAB />
      </SidebarInset>
    </SidebarProvider>
  )
}

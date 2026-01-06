import { DashboardStats } from '@/features/dashboard/DashboardStats'
import { MonthlyCashflow } from '@/features/dashboard/MonthlyCashflow'
import { SpendingByCategory } from '@/features/dashboard/SpendingByCategory'
import { BankAccounts } from '@/features/dashboard/BankAccounts'
import { UpcomingPayments } from '@/features/dashboard/UpcomingPayments'
import { RecentTransactions } from '@/features/dashboard/RecentTransactions'
import { WorkspacePlaceholder } from '@/features/dashboard/WorkspacePlaceholder'
import { useAppStore } from '@/store/app-store'

export function DashboardPage() {
  const currentWorkspace = useAppStore((state) => state.currentWorkspace)

  // Si no hay workspace seleccionado, mostrar el placeholder
  if (!currentWorkspace?.id) {
    return <WorkspacePlaceholder />
  }

  // Dashboard normal con datos
  return (
    <div className="overflow-x-hidden w-full max-w-full">
      <div className="grid gap-4 pt-4 md:gap-6 md:pt-6">
        <DashboardStats />
        
        <div className="grid gap-4 md:gap-6 lg:grid-cols-3">
          <MonthlyCashflow />
          <SpendingByCategory />
        </div>

        <div className="grid gap-4 md:gap-6 lg:grid-cols-2">
          <BankAccounts />
          <UpcomingPayments />
        </div>

        <div className="grid gap-4 md:gap-6">
          <RecentTransactions />
        </div>
      </div>
    </div>
  )
}

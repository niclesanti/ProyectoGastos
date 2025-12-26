import { DashboardStats } from '@/features/dashboard/DashboardStats'
import { MonthlyCashflow } from '@/features/dashboard/MonthlyCashflow'
import { SpendingByCategory } from '@/features/dashboard/SpendingByCategory'
import { BankAccounts } from '@/features/dashboard/BankAccounts'
import { UpcomingPayments } from '@/features/dashboard/UpcomingPayments'
import { RecentTransactions } from '@/features/dashboard/RecentTransactions'

export function DashboardPage() {
  return (
    <div className="grid gap-6 pt-6">
      <DashboardStats />
      
      <div className="grid gap-6 lg:grid-cols-3">
        <MonthlyCashflow />
        <SpendingByCategory />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <BankAccounts />
        <UpcomingPayments />
      </div>

      <div className="grid gap-6">
        <RecentTransactions />
      </div>
    </div>
  )
}

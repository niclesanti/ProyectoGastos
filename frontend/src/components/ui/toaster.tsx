import { AnimatePresence } from 'framer-motion'
import {
  Toast,
  ToastAction,
  ToastClose,
  ToastDescription,
  ToastProvider,
  ToastTitle,
  ToastViewport,
  variantConfig,
} from '@/components/ui/toast'
import { useToast } from '@/hooks/useToast'

export function Toaster() {
  const { toasts } = useToast()

  return (
    <ToastProvider>
      <AnimatePresence mode="popLayout">
        {toasts
          .filter((t) => t.open !== false)
          .map(function ({ id, title, description, action, variant, ...props }) {
            const config = variantConfig[variant ?? 'default']

            let actionElement: React.ReactElement | null = null
            if (action) {
              if (typeof action === 'object' && 'label' in action && 'onClick' in action) {
                actionElement = (
                  <ToastAction altText={action.label} onClick={action.onClick}>
                    {action.label}
                  </ToastAction>
                )
              } else {
                actionElement = action as React.ReactElement
              }
            }

            return (
              <Toast key={id} variant={variant} {...props}>
                {config.icon && (
                  <div
                    className={`flex items-center justify-center rounded-xl p-2 ${config.badgeBg} shrink-0`}
                  >
                    <span className={config.iconColor}>{config.icon}</span>
                  </div>
                )}
                <div className="grid gap-1 flex-1 min-w-0">
                  {title && <ToastTitle>{title}</ToastTitle>}
                  {description && (
                    <ToastDescription>{description}</ToastDescription>
                  )}
                </div>
                {actionElement}
                <ToastClose />
              </Toast>
            )
          })}
      </AnimatePresence>
      <ToastViewport />
    </ToastProvider>
  )
}

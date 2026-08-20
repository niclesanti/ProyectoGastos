import * as React from 'react'
import * as ToastPrimitives from '@radix-ui/react-toast'
import { motion } from 'framer-motion'
import { X, CheckCircle2, AlertCircle, Info, AlertTriangle } from 'lucide-react'
import { cn } from '@/lib/utils'

const ToastProvider = ToastPrimitives.Provider

const ToastViewport = React.forwardRef<
  React.ElementRef<typeof ToastPrimitives.Viewport>,
  React.ComponentPropsWithoutRef<typeof ToastPrimitives.Viewport>
>(({ className, ...props }, ref) => (
  <ToastPrimitives.Viewport
    ref={ref}
    className={cn(
      'fixed top-0 left-0 right-0 z-[100] flex w-full flex-col items-center p-4 sm:p-5',
      '[&>*]:w-full [&>*]:max-w-sm [&>*]:data-[state=open]:slide-in-from-top [&>*]:data-[state=closed]:slide-out-to-top',
      className
    )}
    style={{ paddingTop: 'max(1rem, env(safe-area-inset-top))' }}
    {...props}
  />
))
ToastViewport.displayName = ToastPrimitives.Viewport.displayName

const variantConfig = {
  success: {
    icon: <CheckCircle2 className="h-5 w-5 shrink-0" />,
    iconColor: 'text-emerald-400',
    badgeBg: 'bg-emerald-500/20',
  },
  error: {
    icon: <AlertCircle className="h-5 w-5 shrink-0" />,
    iconColor: 'text-rose-400',
    badgeBg: 'bg-rose-500/20',
  },
  warning: {
    icon: <AlertTriangle className="h-5 w-5 shrink-0" />,
    iconColor: 'text-amber-400',
    badgeBg: 'bg-amber-500/20',
  },
  info: {
    icon: <Info className="h-5 w-5 shrink-0" />,
    iconColor: 'text-sky-400',
    badgeBg: 'bg-sky-500/20',
  },
  default: {
    icon: null,
    iconColor: 'text-foreground',
    badgeBg: 'bg-foreground/10',
  },
} as const

const Toast = React.forwardRef<
  React.ElementRef<typeof ToastPrimitives.Root>,
  React.ComponentPropsWithoutRef<typeof ToastPrimitives.Root> & {
    variant?: 'default' | 'success' | 'error' | 'warning' | 'info'
  }
>(({ className, ...props }, ref) => {
  return (
    <ToastPrimitives.Root
      ref={ref}
      asChild
      {...props}
    >
      <motion.div
        layout
        initial={{ opacity: 0, y: -140, scale: 0.9 }}
        animate={{
          opacity: 1,
          y: 0,
          scale: 1,
          transition: {
            type: 'spring',
            stiffness: 400,
            damping: 30,
            mass: 0.8,
          },
        }}
        exit={{
          opacity: 0,
          y: -140,
          scale: 0.9,
          transition: {
            type: 'spring',
            stiffness: 500,
            damping: 40,
            mass: 0.8,
          },
        }}
        drag="y"
        dragSnapToOrigin
        dragConstraints={{ top: 0, bottom: 0 }}
        dragElastic={{ top: 0.6, bottom: 0.05 }}
        onDragEnd={(_e, info) => {
          const { onOpenChange } = props as { onOpenChange?: (open: boolean) => void }
          if (info.offset.y < -80 || info.velocity.y < -400) {
            onOpenChange?.(false)
          }
        }}
        whileDrag={{ scale: 0.98, opacity: 0.92 }}
        className={cn(
          'group pointer-events-auto relative flex w-full items-center justify-between gap-3 overflow-hidden',
          'rounded-2xl border p-4 pr-8',
          'border-white/40 dark:border-white/15',
          'bg-[linear-gradient(180deg,rgba(255,255,255,0.04),rgba(255,255,255,0.005))]',
          'dark:bg-[linear-gradient(180deg,rgba(255,255,255,0.01),rgba(255,255,255,0.002))]',
          'backdrop-blur-xl backdrop-saturate-[1.8] backdrop-brightness-[1.12]',
          'shadow-[0_8px_32px_rgba(0,0,0,0.16),inset_0_1px_0_rgba(255,255,255,0.5)]',
          'text-foreground',
          className
        )}
      >
        <span
          aria-hidden="true"
          className="pointer-events-none absolute inset-0 rounded-2xl bg-gradient-to-b from-white/10 via-white/3 to-transparent dark:from-white/3 dark:via-white/1 dark:to-transparent"
        />
        {props.children}
      </motion.div>
    </ToastPrimitives.Root>
  )
})
Toast.displayName = ToastPrimitives.Root.displayName

const ToastAction = React.forwardRef<
  React.ElementRef<typeof ToastPrimitives.Action>,
  React.ComponentPropsWithoutRef<typeof ToastPrimitives.Action>
>(({ className, ...props }, ref) => (
  <ToastPrimitives.Action
    ref={ref}
    className={cn(
      'inline-flex h-8 shrink-0 items-center justify-center rounded-xl border border-white/20 dark:border-white/10',
      'bg-white/10 dark:bg-white/5 px-3 text-sm font-medium backdrop-blur-sm',
      'transition-colors hover:bg-white/20 dark:hover:bg-white/10',
      'focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2',
      'disabled:pointer-events-none disabled:opacity-50',
      className
    )}
    {...props}
  />
))
ToastAction.displayName = ToastPrimitives.Action.displayName

const ToastClose = React.forwardRef<
  React.ElementRef<typeof ToastPrimitives.Close>,
  React.ComponentPropsWithoutRef<typeof ToastPrimitives.Close>
>(({ className, ...props }, ref) => (
  <ToastPrimitives.Close
    ref={ref}
    className={cn(
      'absolute right-2 top-2 rounded-full p-1 text-foreground/40 opacity-0 transition-all',
      'hover:text-foreground/80 hover:bg-white/10 dark:hover:bg-white/5',
      'focus:opacity-100 focus:outline-none focus:ring-2 group-hover:opacity-100',
      className
    )}
    toast-close=""
    {...props}
  >
    <X className="h-4 w-4" />
  </ToastPrimitives.Close>
))
ToastClose.displayName = ToastPrimitives.Close.displayName

const ToastTitle = React.forwardRef<
  React.ElementRef<typeof ToastPrimitives.Title>,
  React.ComponentPropsWithoutRef<typeof ToastPrimitives.Title>
>(({ className, ...props }, ref) => (
  <ToastPrimitives.Title
    ref={ref}
    className={cn('text-sm font-semibold', className)}
    {...props}
  />
))
ToastTitle.displayName = ToastPrimitives.Title.displayName

const ToastDescription = React.forwardRef<
  React.ElementRef<typeof ToastPrimitives.Description>,
  React.ComponentPropsWithoutRef<typeof ToastPrimitives.Description>
>(({ className, ...props }, ref) => (
  <ToastPrimitives.Description
    ref={ref}
    className={cn('text-sm text-muted-foreground', className)}
    {...props}
  />
))
ToastDescription.displayName = ToastPrimitives.Description.displayName

type ToastProps = React.ComponentPropsWithoutRef<typeof Toast>

type ToastActionElement = React.ReactElement<typeof ToastAction>

const getToastIcon = (variant?: ToastProps['variant']) => {
  return variantConfig[variant ?? 'default'].icon
}

export {
  type ToastProps,
  type ToastActionElement,
  ToastProvider,
  ToastViewport,
  Toast,
  ToastTitle,
  ToastDescription,
  ToastClose,
  ToastAction,
  getToastIcon,
  variantConfig,
}

/**
 * ResponsiveModal Component
 *
 * Modal universal centrado (Dialog) para PC, tablets y móviles.
 * Usa Dialog de shadcn/Radix en todos los dispositivos, eliminando los
 * problemas del teclado virtual que afectaban al Drawer en iOS/Android.
 *
 * USO CORRECTO:
 *
 * <ResponsiveModal open={open} onOpenChange={setOpen}>
 *   <ResponsiveModalContent className="max-w-lg">
 *     <ResponsiveModalHeader>
 *       <ResponsiveModalTitle>Título</ResponsiveModalTitle>
 *       <ResponsiveModalDescription>Descripción</ResponsiveModalDescription>
 *     </ResponsiveModalHeader>
 *
 *     <ResponsiveModalScrollArea>
 *       Contenido scrolleable aquí
 *     </ResponsiveModalScrollArea>
 *
 *     <ResponsiveModalFooter>
 *       Botones aquí
 *     </ResponsiveModalFooter>
 *   </ResponsiveModalContent>
 * </ResponsiveModal>
 *
 * IMPORTANTE:
 * - Solo agrega max-w-* al ResponsiveModalContent para controlar el ancho
 * - NO agregues overflow, padding, ni max-h al ResponsiveModalContent
 * - El padding, scroll y layout ya están manejados internamente
 * - El ResponsiveModalScrollArea se expande automáticamente (flex-1)
 */

import * as React from "react"
import { cn } from "@/lib/utils"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog"

interface ResponsiveModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  children: React.ReactNode
}

export function ResponsiveModal({ open, onOpenChange, children }: ResponsiveModalProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      {children}
    </Dialog>
  )
}

interface ResponsiveModalContentProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode
}

export function ResponsiveModalContent({ children, className, ...props }: ResponsiveModalContentProps) {
  return (
    <DialogContent
      className={cn(
        "max-h-[90dvh] !flex flex-col overflow-hidden gap-4 p-4 sm:p-6",
        className
      )}
      {...props}
    >
      {children}
    </DialogContent>
  )
}

export function ResponsiveModalHeader({ children, className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <DialogHeader className={cn("flex-shrink-0", className)} {...props}>
      {children}
    </DialogHeader>
  )
}

export function ResponsiveModalTitle({ children, ...props }: React.HTMLAttributes<HTMLHeadingElement>) {
  return <DialogTitle {...props}>{children}</DialogTitle>
}

export function ResponsiveModalDescription({ children, ...props }: React.HTMLAttributes<HTMLParagraphElement>) {
  return <DialogDescription {...props}>{children}</DialogDescription>
}

export function ResponsiveModalFooter({ children, className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <DialogFooter className={cn("flex-shrink-0", className)} {...props}>
      {children}
    </DialogFooter>
  )
}

export function ResponsiveModalScrollArea({
  children,
  className,
}: {
  children: React.ReactNode
  className?: string
}) {
  return (
    <div className={cn("flex-1 min-h-0 overflow-y-auto pr-1", className)}>
      <div className="pr-2">
        {children}
      </div>
    </div>
  )
}

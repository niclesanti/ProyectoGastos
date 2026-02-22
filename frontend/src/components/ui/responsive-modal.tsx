/**
 * ResponsiveModal Component
 * 
 * Modal adaptativo que usa Dialog en desktop y Drawer en mobile.
 * Maneja correctamente el scroll interno para mantener header y footer fijos.
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
import { useIsMobile } from "@/hooks/use-mobile"
import { cn } from "@/lib/utils"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog"
import {
  Drawer,
  DrawerContent,
  DrawerDescription,
  DrawerHeader,
  DrawerTitle,
  DrawerFooter,
} from "@/components/ui/drawer"

interface ResponsiveModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  children: React.ReactNode
}

export function ResponsiveModal({ open, onOpenChange, children }: ResponsiveModalProps) {
  const isMobile = useIsMobile()

  if (isMobile) {
    return (
      <Drawer open={open} onOpenChange={onOpenChange}>
        {children}
      </Drawer>
    )
  }

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
  const isMobile = useIsMobile()

  if (isMobile) {
    return (
      <DrawerContent className={cn("max-h-[96vh] flex flex-col overflow-hidden", className)} {...props}>
        {children}
      </DrawerContent>
    )
  }

  return (
    <DialogContent className={cn("max-h-[90vh] flex flex-col overflow-hidden gap-4 p-6", className)} {...props}>
      {children}
    </DialogContent>
  )
}

export function ResponsiveModalHeader({ children, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  const isMobile = useIsMobile()

  if (isMobile) {
    return <DrawerHeader className="flex-shrink-0" {...props}>{children}</DrawerHeader>
  }

  return <DialogHeader className="flex-shrink-0" {...props}>{children}</DialogHeader>
}

export function ResponsiveModalTitle({ children, ...props }: React.HTMLAttributes<HTMLHeadingElement>) {
  const isMobile = useIsMobile()

  if (isMobile) {
    return <DrawerTitle {...props}>{children}</DrawerTitle>
  }

  return <DialogTitle {...props}>{children}</DialogTitle>
}

export function ResponsiveModalDescription({ children, ...props }: React.HTMLAttributes<HTMLParagraphElement>) {
  const isMobile = useIsMobile()

  if (isMobile) {
    return <DrawerDescription {...props}>{children}</DrawerDescription>
  }

  return <DialogDescription {...props}>{children}</DialogDescription>
}

export function ResponsiveModalFooter({ children, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  const isMobile = useIsMobile()

  if (isMobile) {
    return <DrawerFooter className="flex-shrink-0" {...props}>{children}</DrawerFooter>
  }

  return <DialogFooter className="flex-shrink-0" {...props}>{children}</DialogFooter>
}

export function ResponsiveModalScrollArea({ children, className }: { children: React.ReactNode; className?: string }) {
  const isMobile = useIsMobile()

  // Tanto en móvil como en desktop, usamos flex-1 con overflow-y-auto
  // para permitir scroll SOLO en el contenido, manteniendo header y footer fijos
  if (isMobile) {
    return <div className={cn("flex-1 overflow-y-auto px-4", className)}>{children}</div>
  }

  // En desktop, usamos modal-scroll para un scrollbar sutil que se muestra solo al hover
  // Agregamos pr-3 para dar espacio entre el contenido y el scrollbar
  return (
    <div className={cn("flex-1 overflow-y-auto modal-scroll pr-3", className)}>
      {children}
    </div>
  )
}

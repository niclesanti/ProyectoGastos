"use client"

import { useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Calendar } from '@/components/ui/calendar'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { ScrollArea } from '@/components/ui/scroll-area'
import { CalendarIcon, Plus } from 'lucide-react'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'
import { cn } from '@/lib/utils'

interface CreditPurchaseModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function CreditPurchaseModal({ open, onOpenChange }: CreditPurchaseModalProps) {
  const [date, setDate] = useState<Date>()
  const [tarjeta, setTarjeta] = useState<string>('')
  const [cuotas, setCuotas] = useState<string>('')
  const [monto, setMonto] = useState<string>('')
  const [motivo, setMotivo] = useState<string>('')
  const [comercio, setComercio] = useState<string>('')
  const [descripcion, setDescripcion] = useState<string>('')

  // Estados para mostrar/ocultar formularios de creación
  const [showNewMotivo, setShowNewMotivo] = useState(false)
  const [showNewComercio, setShowNewComercio] = useState(false)

  // Estados para los nuevos valores
  const [newMotivo, setNewMotivo] = useState('')
  const [newComercio, setNewComercio] = useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    console.log({
      date,
      tarjeta,
      cuotas,
      monto,
      motivo,
      comercio,
      descripcion,
    })
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Compra con crédito</DialogTitle>
          <DialogDescription>
            Anotar que hiciste una compra con tarjeta de crédito.
          </DialogDescription>
        </DialogHeader>

        <ScrollArea className="max-h-[60vh] pr-6">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Fecha */}
            <div className="space-y-1.5">
              <Label className="text-sm">Fecha</Label>
              <Popover>
                <PopoverTrigger asChild>
                  <Button
                    variant="outline"
                    className={cn(
                      'w-full h-9 justify-start text-left font-normal',
                      !date && 'text-muted-foreground'
                    )}
                  >
                    <CalendarIcon className="mr-2 h-4 w-4" />
                    {date ? format(date, 'PPP', { locale: es }) : 'Elegir fecha de compra...'}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0" align="start">
                  <Calendar
                    mode="single"
                    selected={date}
                    onSelect={setDate}
                    initialFocus
                  />
                </PopoverContent>
              </Popover>
            </div>

            {/* Tarjeta */}
            <div className="space-y-1.5">
              <Label htmlFor="tarjeta" className="text-sm">Tarjeta</Label>
              <Select value={tarjeta} onValueChange={setTarjeta}>
                <SelectTrigger id="tarjeta" className="h-9">
                  <SelectValue placeholder="Seleccionar tarjeta de crédito..." />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="visa-gold">Visa Gold</SelectItem>
                  <SelectItem value="mastercard-platinum">Mastercard Platinum</SelectItem>
                  <SelectItem value="amex">American Express</SelectItem>
                  <SelectItem value="cabal">Cabal</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Cantidad de cuotas */}
            <div className="space-y-1.5">
              <Label htmlFor="cuotas" className="text-sm">Cantidad de cuotas</Label>
              <Select value={cuotas} onValueChange={setCuotas}>
                <SelectTrigger id="cuotas" className="h-9">
                  <SelectValue placeholder="Elegir cantidad de cuotas..." />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">1 cuota</SelectItem>
                  <SelectItem value="3">3 cuotas</SelectItem>
                  <SelectItem value="6">6 cuotas</SelectItem>
                  <SelectItem value="9">9 cuotas</SelectItem>
                  <SelectItem value="12">12 cuotas</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Monto final a pagar */}
            <div className="space-y-1.5">
              <Label htmlFor="monto" className="text-sm">Monto final a pagar ($)</Label>
              <Input
                id="monto"
                type="number"
                step="0.01"
                placeholder="0.00"
                className="h-9"
                value={monto}
                onChange={(e) => setMonto(e.target.value)}
              />
            </div>

            {/* Motivo */}
            <div className="space-y-1.5">
              <Label htmlFor="motivo" className="text-sm">Motivo</Label>
              <div className="flex gap-2">
                <Select value={motivo} onValueChange={setMotivo}>
                  <SelectTrigger id="motivo" className="flex-1 h-9">
                    <SelectValue placeholder="Filtrar por motivo..." />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="alimentacion">Alimentación</SelectItem>
                    <SelectItem value="transporte">Transporte</SelectItem>
                    <SelectItem value="vivienda">Vivienda</SelectItem>
                    <SelectItem value="salud">Salud</SelectItem>
                    <SelectItem value="ocio">Ocio</SelectItem>
                    <SelectItem value="educacion">Educación</SelectItem>
                    <SelectItem value="servicios">Servicios</SelectItem>
                    <SelectItem value="otros">Otros</SelectItem>
                  </SelectContent>
                </Select>
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  className="h-9 w-9 p-0"
                  onClick={() => setShowNewMotivo(!showNewMotivo)}
                >
                  <Plus className="h-4 w-4" />
                </Button>
              </div>

              {showNewMotivo && (
                <div className="space-y-2 pt-2">
                  <Input
                    placeholder="Nombre del nuevo motivo"
                    className="h-9"
                    value={newMotivo}
                    onChange={(e) => setNewMotivo(e.target.value)}
                  />
                  <div className="flex justify-end gap-2">
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      className="h-8"
                      onClick={() => {
                        setShowNewMotivo(false)
                        setNewMotivo('')
                      }}
                    >
                      Cancelar
                    </Button>
                    <Button
                      type="button"
                      size="sm"
                      className="h-8"
                      onClick={() => {
                        console.log('Nuevo motivo:', newMotivo)
                        setShowNewMotivo(false)
                        setNewMotivo('')
                      }}
                    >
                      Guardar
                    </Button>
                  </div>
                </div>
              )}
            </div>

            {/* Comercio */}
            <div className="space-y-1.5">
              <Label htmlFor="comercio" className="text-sm">Comercio</Label>
              <div className="flex gap-2">
                <Select value={comercio} onValueChange={setComercio}>
                  <SelectTrigger id="comercio" className="flex-1 h-9">
                    <SelectValue placeholder="Seleccionar comercio..." />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="supermercado1">Supermercado XYZ</SelectItem>
                    <SelectItem value="tienda1">Tienda ABC</SelectItem>
                    <SelectItem value="farmacia1">Farmacia 123</SelectItem>
                    <SelectItem value="restaurante1">Restaurante El Buen Sabor</SelectItem>
                  </SelectContent>
                </Select>
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  className="h-9 w-9 p-0"
                  onClick={() => setShowNewComercio(!showNewComercio)}
                >
                  <Plus className="h-4 w-4" />
                </Button>
              </div>

              {showNewComercio && (
                <div className="space-y-2 pt-2">
                  <Input
                    placeholder="Nombre del nuevo comercio"
                    className="h-9"
                    value={newComercio}
                    onChange={(e) => setNewComercio(e.target.value)}
                  />
                  <div className="flex justify-end gap-2">
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      className="h-8"
                      onClick={() => {
                        setShowNewComercio(false)
                        setNewComercio('')
                      }}
                    >
                      Cancelar
                    </Button>
                    <Button
                      type="button"
                      size="sm"
                      className="h-8"
                      onClick={() => {
                        console.log('Nuevo comercio:', newComercio)
                        setShowNewComercio(false)
                        setNewComercio('')
                      }}
                    >
                      Guardar
                    </Button>
                  </div>
                </div>
              )}
            </div>

            {/* Descripción */}
            <div className="space-y-1.5">
              <Label htmlFor="descripcion" className="text-sm">Descripción</Label>
              <Textarea
                id="descripcion"
                placeholder="Agregar notas adicionales sobre esta compra..."
                rows={3}
                className="resize-none"
                value={descripcion}
                onChange={(e) => setDescripcion(e.target.value)}
              />
            </div>
          </form>
        </ScrollArea>

        <DialogFooter className="mt-4">
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
          >
            Cancelar
          </Button>
          <Button type="submit" onClick={handleSubmit}>Guardar compra</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

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

interface TransactionModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function TransactionModal({ open, onOpenChange }: TransactionModalProps) {
  const [date, setDate] = useState<Date>()
  const [tipo, setTipo] = useState<string>('')
  const [monto, setMonto] = useState<string>('')
  const [motivo, setMotivo] = useState<string>('')
  const [cuenta, setCuenta] = useState<string>('')
  const [contacto, setContacto] = useState<string>('')
  const [descripcion, setDescripcion] = useState<string>('')
  
  // Estados para mostrar/ocultar formularios de creación
  const [showNewMotivo, setShowNewMotivo] = useState(false)
  const [showNewCuenta, setShowNewCuenta] = useState(false)
  const [showNewContacto, setShowNewContacto] = useState(false)
  
  // Estados para los nuevos valores
  const [newMotivo, setNewMotivo] = useState('')
  const [newCuentaNombre, setNewCuentaNombre] = useState('')
  const [newCuentaEntidad, setNewCuentaEntidad] = useState('')
  const [newContacto, setNewContacto] = useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    // Por ahora no hace nada
    console.log({
      tipo,
      date,
      monto,
      motivo,
      cuenta,
      contacto,
      descripcion,
    })
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Registrar una transacción</DialogTitle>
          <DialogDescription>
            Anotar una nueva salida o ingreso de dinero con todos sus detalles.
          </DialogDescription>
        </DialogHeader>

        <ScrollArea className="max-h-[60vh] pr-6">
          <form onSubmit={handleSubmit} className="space-y-3">
            {/* Tipo */}
            <div className="space-y-1.5">
              <Label htmlFor="tipo" className="text-sm">Tipo</Label>
              <Select value={tipo} onValueChange={setTipo}>
                <SelectTrigger id="tipo" className="h-9">
                  <SelectValue placeholder="Seleccionar tipo" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="gasto">Gasto</SelectItem>
                  <SelectItem value="ingreso">Ingreso</SelectItem>
                </SelectContent>
              </Select>
            </div>

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
                    {date ? format(date, 'PPP', { locale: es }) : 'Seleccionar fecha'}
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

            {/* Monto */}
            <div className="space-y-1.5">
              <Label htmlFor="monto" className="text-sm">Monto ($)</Label>
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
                    <SelectValue placeholder="Seleccionar motivo" />
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
                  variant="outline" 
                  size="sm" 
                  className="h-9 px-2"
                  onClick={() => setShowNewMotivo(!showNewMotivo)}
                >
                  <Plus className="h-4 w-4 mr-1" />
                  Nuevo
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

            {/* Cuenta Bancaria */}
            <div className="space-y-1.5">
              <Label htmlFor="cuenta" className="text-sm">Cuenta bancaria (opcional)</Label>
              <div className="flex gap-2">
                <Select value={cuenta} onValueChange={setCuenta}>
                  <SelectTrigger id="cuenta" className="flex-1 h-9">
                    <SelectValue placeholder="Seleccionar cuenta" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="principal">Cuenta Principal</SelectItem>
                    <SelectItem value="ahorros">Ahorros</SelectItem>
                    <SelectItem value="gastos">Gastos</SelectItem>
                    <SelectItem value="inversiones">Inversiones</SelectItem>
                  </SelectContent>
                </Select>
                <Button 
                  type="button" 
                  variant="outline" 
                  size="sm" 
                  className="h-9 px-2"
                  onClick={() => setShowNewCuenta(!showNewCuenta)}
                >
                  <Plus className="h-4 w-4 mr-1" />
                  Nuevo
                </Button>
              </div>
              
              {showNewCuenta && (
                <div className="space-y-2 pt-2">
                  <Input
                    placeholder="Nombre de la cuenta"
                    className="h-9"
                    value={newCuentaNombre}
                    onChange={(e) => setNewCuentaNombre(e.target.value)}
                  />
                  <Select value={newCuentaEntidad} onValueChange={setNewCuentaEntidad}>
                    <SelectTrigger className="h-9">
                      <SelectValue placeholder="Seleccionar entidad" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="banco1">Banco Nación</SelectItem>
                      <SelectItem value="banco2">Banco Provincia</SelectItem>
                      <SelectItem value="banco3">Banco Galicia</SelectItem>
                      <SelectItem value="banco4">Banco Santander</SelectItem>
                      <SelectItem value="banco5">BBVA</SelectItem>
                      <SelectItem value="banco6">Banco Macro</SelectItem>
                      <SelectItem value="efectivo">Efectivo</SelectItem>
                    </SelectContent>
                  </Select>
                  <div className="flex justify-end gap-2">
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      className="h-8"
                      onClick={() => {
                        setShowNewCuenta(false)
                        setNewCuentaNombre('')
                        setNewCuentaEntidad('')
                      }}
                    >
                      Cancelar
                    </Button>
                    <Button
                      type="button"
                      size="sm"
                      className="h-8"
                      onClick={() => {
                        console.log('Nueva cuenta:', { nombre: newCuentaNombre, entidad: newCuentaEntidad })
                        setShowNewCuenta(false)
                        setNewCuentaNombre('')
                        setNewCuentaEntidad('')
                      }}
                    >
                      Guardar
                    </Button>
                  </div>
                </div>
              )}
            </div>

            {/* Contacto */}
            <div className="space-y-1.5">
              <Label htmlFor="contacto" className="text-sm">Contacto emisor/destinatario (opcional)</Label>
              <div className="flex gap-2">
                <Select value={contacto} onValueChange={setContacto}>
                  <SelectTrigger id="contacto" className="flex-1 h-9">
                    <SelectValue placeholder="Seleccionar contacto" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="empresa1">Empresa ABC</SelectItem>
                    <SelectItem value="persona1">Juan Pérez</SelectItem>
                    <SelectItem value="persona2">María García</SelectItem>
                    <SelectItem value="tienda1">Supermercado XYZ</SelectItem>
                  </SelectContent>
                </Select>
                <Button 
                  type="button" 
                  variant="outline" 
                  size="sm" 
                  className="h-9 px-2"
                  onClick={() => setShowNewContacto(!showNewContacto)}
                >
                  <Plus className="h-4 w-4 mr-1" />
                  Nuevo
                </Button>
              </div>
              
              {showNewContacto && (
                <div className="space-y-2 pt-2">
                  <Input
                    placeholder="Ingrese el nuevo contacto"
                    className="h-9"
                    value={newContacto}
                    onChange={(e) => setNewContacto(e.target.value)}
                  />
                  <div className="flex justify-end gap-2">
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      className="h-8"
                      onClick={() => {
                        setShowNewContacto(false)
                        setNewContacto('')
                      }}
                    >
                      Cancelar
                    </Button>
                    <Button
                      type="button"
                      size="sm"
                      className="h-8"
                      onClick={() => {
                        console.log('Nuevo contacto:', newContacto)
                        setShowNewContacto(false)
                        setNewContacto('')
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
                placeholder="Agregar notas adicionales sobre esta transacción..."
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
          <Button type="submit" onClick={handleSubmit}>Guardar transacción</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

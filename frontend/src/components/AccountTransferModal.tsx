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
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

interface AccountTransferModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function AccountTransferModal({ open, onOpenChange }: AccountTransferModalProps) {
  const [cuentaOrigen, setCuentaOrigen] = useState<string>('')
  const [cuentaDestino, setCuentaDestino] = useState<string>('')
  const [monto, setMonto] = useState<string>('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    // Por ahora no hace nada
    console.log({
      cuentaOrigen,
      cuentaDestino,
      monto,
    })
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Movimiento entre cuentas</DialogTitle>
          <DialogDescription>
            Anotar que realizaste un movimiento de dinero entre dos cuentas bancarias registradas en el sistema.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-3">
          {/* Cuenta de origen */}
          <div className="space-y-1.5">
            <Label htmlFor="cuentaOrigen" className="text-sm">Cuenta de origen</Label>
            <Select value={cuentaOrigen} onValueChange={setCuentaOrigen}>
              <SelectTrigger id="cuentaOrigen" className="h-9">
                <SelectValue placeholder="Seleccionar cuenta de origen" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="principal">Cuenta principal</SelectItem>
                <SelectItem value="ahorros">Ahorros</SelectItem>
                <SelectItem value="gastos">Gastos</SelectItem>
                <SelectItem value="inversiones">Inversiones</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Cuenta Destino */}
          <div className="space-y-1.5">
            <Label htmlFor="cuentaDestino" className="text-sm">Cuenta de destino</Label>
            <Select value={cuentaDestino} onValueChange={setCuentaDestino}>
              <SelectTrigger id="cuentaDestino" className="h-9">
                <SelectValue placeholder="Seleccionar cuenta destino" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="principal">Cuenta principal</SelectItem>
                <SelectItem value="ahorros">Ahorros</SelectItem>
                <SelectItem value="gastos">Gastos</SelectItem>
                <SelectItem value="inversiones">Inversiones</SelectItem>
              </SelectContent>
            </Select>
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
        </form>

        <DialogFooter className="mt-4">
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
          >
            Cancelar
          </Button>
          <Button type="submit" onClick={handleSubmit}>Realizar movimiento</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

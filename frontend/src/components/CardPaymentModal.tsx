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
import { ScrollArea } from '@/components/ui/scroll-area'
import { CalendarIcon, Plus, MoreHorizontal, GripVertical } from 'lucide-react'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'
import { cn } from '@/lib/utils'
import {
  ColumnDef,
  flexRender,
  getCoreRowModel,
  getPaginationRowModel,
  useReactTable,
  VisibilityState,
} from '@tanstack/react-table'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuCheckboxItem,
} from '@/components/ui/dropdown-menu'
import { Checkbox } from '@/components/ui/checkbox'
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination'
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragEndEvent,
} from '@dnd-kit/core'
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { ChevronDown } from 'lucide-react'

interface CardPaymentModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

interface Installment {
  id: string
  numero: number
  vencimiento: string
  motivo: string
  monto: number
}

function SortableRow({ row, children }: any) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: row.original.id,
  })

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  }

  return (
    <TableRow 
      ref={setNodeRef} 
      style={style} 
      data-state={row.getIsSelected() && 'selected'}
      className={cn(row.getIsSelected() && 'bg-zinc-900/50')}
    >
      {row.getVisibleCells().map((cell: any, index: number) => (
        <TableCell
          key={cell.id}
          {...(index === 0 ? { ...attributes, ...listeners } : {})}
        >
          {flexRender(cell.column.columnDef.cell, cell.getContext())}
        </TableCell>
      ))}
    </TableRow>
  )
}

export function CardPaymentModal({ open, onOpenChange }: CardPaymentModalProps) {
  const [date, setDate] = useState<Date>()
  const [cuenta, setCuenta] = useState<string>('')
  const [rowSelection, setRowSelection] = useState({})
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({})

  // Estados para mostrar/ocultar formulario de creación
  const [showNewCuenta, setShowNewCuenta] = useState(false)

  // Estados para los nuevos valores
  const [newCuentaNombre, setNewCuentaNombre] = useState('')
  const [newCuentaEntidad, setNewCuentaEntidad] = useState('')

  // Datos de ejemplo para las cuotas
  const [installments, setInstallments] = useState<Installment[]>([
    { id: '1', numero: 1, vencimiento: '15/01/2025', motivo: 'Supermercado', monto: 5000 },
    { id: '2', numero: 2, vencimiento: '15/02/2025', motivo: 'Farmacia', monto: 3200 },
    { id: '3', numero: 3, vencimiento: '15/03/2025', motivo: 'Supermercado', monto: 5000 },
    { id: '4', numero: 4, vencimiento: '15/04/2025', motivo: 'Electrónica', monto: 12000 },
    { id: '5', numero: 5, vencimiento: '15/05/2025', motivo: 'Supermercado', monto: 5000 },
    { id: '6', numero: 6, vencimiento: '15/06/2025', motivo: 'Ropa', monto: 8500 },
  ])

  const columns: ColumnDef<Installment>[] = [
    {
      id: 'drag',
      header: '',
      cell: () => (
        <div className="cursor-grab active:cursor-grabbing">
          <GripVertical className="h-4 w-4 text-muted-foreground" />
        </div>
      ),
      enableHiding: false,
      size: 40,
    },
    {
      id: 'select',
      header: ({ table }) => (
        <Checkbox
          checked={table.getIsAllPageRowsSelected()}
          onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
          aria-label="Seleccionar todas"
        />
      ),
      cell: ({ row }) => (
        <Checkbox
          checked={row.getIsSelected()}
          onCheckedChange={(value) => row.toggleSelected(!!value)}
          aria-label="Seleccionar fila"
        />
      ),
      enableSorting: false,
      enableHiding: false,
      size: 40,
    },
    {
      accessorKey: 'numero',
      header: 'Número',
      cell: ({ row }) => <div>Cuota {row.getValue('numero')}</div>,
    },
    {
      accessorKey: 'vencimiento',
      header: 'Vencimiento',
    },
    {
      accessorKey: 'motivo',
      header: 'Motivo',
    },
    {
      accessorKey: 'monto',
      header: () => <div className="text-right">Monto</div>,
      cell: ({ row }) => {
        const amount = parseFloat(row.getValue('monto'))
        const formatted = new Intl.NumberFormat('es-AR', {
          style: 'currency',
          currency: 'ARS',
        }).format(amount).replace('$ ', '$')
        return <div className="text-right font-mono font-medium tabular-nums">{formatted}</div>
      },
    },
    {
      id: 'actions',
      cell: ({ row }) => {
        return (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="h-8 w-8 p-0">
                <span className="sr-only">Abrir menú</span>
                <MoreHorizontal className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => console.log('Ver detalles:', row.original)}>
                Ver detalles compra
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        )
      },
      enableHiding: false,
      size: 40,
    },
  ]

  const table = useReactTable({
    data: installments,
    columns,
    getCoreRowModel: getCoreRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    onRowSelectionChange: setRowSelection,
    onColumnVisibilityChange: setColumnVisibility,
    state: {
      rowSelection,
      columnVisibility,
    },
    initialState: {
      pagination: {
        pageSize: 5,
      },
    },
  })

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  )

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event

    if (over && active.id !== over.id) {
      setInstallments((items) => {
        const oldIndex = items.findIndex((item) => item.id === active.id)
        const newIndex = items.findIndex((item) => item.id === over.id)
        return arrayMove(items, oldIndex, newIndex)
      })
    }
  }

  // Calcular total de filas seleccionadas
  const selectedRows = table.getFilteredSelectedRowModel().rows
  const total = selectedRows.reduce((sum, row) => sum + row.original.monto, 0)

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    console.log({
      date,
      cuenta,
      selectedInstallments: selectedRows.map(row => row.original),
      total,
    })
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-4xl">
        <DialogHeader>
          <DialogTitle>Pagar resumen tarjeta</DialogTitle>
          <DialogDescription>
            Anotar que pagaste el resumen de este mes de la tarjeta. Puedes seleccionar qué cuotas pendientes pagaste.
          </DialogDescription>
        </DialogHeader>

        <ScrollArea className="max-h-[60vh] pr-6">
          <form onSubmit={handleSubmit} className="space-y-4">
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

          {/* Cuenta Bancaria */}
          <div className="space-y-1.5">
            <Label htmlFor="cuenta" className="text-sm">Cuenta bancaria (opcional)</Label>
            <div className="flex gap-2">
              <Select value={cuenta} onValueChange={setCuenta}>
                <SelectTrigger id="cuenta" className="flex-1 h-9">
                  <SelectValue placeholder="Seleccionar cuenta bancaria..." />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="principal">Cuenta principal</SelectItem>
                  <SelectItem value="ahorros">Ahorros</SelectItem>
                  <SelectItem value="gastos">Gastos</SelectItem>
                  <SelectItem value="inversiones">Inversiones</SelectItem>
                </SelectContent>
              </Select>
              <Button
                type="button"
                variant="ghost"
                size="sm"
                className="h-9 w-9 p-0"
                onClick={() => setShowNewCuenta(!showNewCuenta)}
              >
                <Plus className="h-4 w-4" />
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

          {/* Data Table */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold">Cuotas pendientes</h3>
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="outline" size="sm" className="h-8">
                    Columnas <ChevronDown className="ml-2 h-4 w-4" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  {table
                    .getAllColumns()
                    .filter((column) => column.getCanHide())
                    .map((column) => {
                      return (
                        <DropdownMenuCheckboxItem
                          key={column.id}
                          className="capitalize"
                          checked={column.getIsVisible()}
                          onCheckedChange={(value) => column.toggleVisibility(!!value)}
                        >
                          {column.id === 'numero' ? 'Número' : column.id === 'vencimiento' ? 'Vencimiento' : column.id === 'motivo' ? 'Motivo' : column.id === 'monto' ? 'Monto' : column.id}
                        </DropdownMenuCheckboxItem>
                      )
                    })}
                </DropdownMenuContent>
              </DropdownMenu>
            </div>

            <div className="rounded-md border">
              <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
                <Table>
                  <TableHeader className="bg-sidebar">
                    {table.getHeaderGroups().map((headerGroup) => (
                      <TableRow key={headerGroup.id}>
                        {headerGroup.headers.map((header) => (
                          <TableHead key={header.id} style={{ width: header.getSize() }}>
                            {header.isPlaceholder
                              ? null
                              : flexRender(header.column.columnDef.header, header.getContext())}
                          </TableHead>
                        ))}
                      </TableRow>
                    ))}
                  </TableHeader>
                  <TableBody>
                    <SortableContext items={installments.map((i) => i.id)} strategy={verticalListSortingStrategy}>
                      {table.getRowModel().rows?.length ? (
                        table.getRowModel().rows.map((row) => (
                          <SortableRow key={row.id} row={row} />
                        ))
                      ) : (
                        <TableRow>
                          <TableCell colSpan={columns.length} className="h-24 text-center">
                            No hay cuotas pendientes.
                          </TableCell>
                        </TableRow>
                      )}
                    </SortableContext>
                  </TableBody>
                </Table>
              </DndContext>
            </div>

            {/* Pagination */}
            {table.getPageCount() > 1 && (
              <div className="flex justify-end">
                <Pagination>
                  <PaginationContent>
                    <PaginationItem>
                      <PaginationPrevious
                        onClick={() => table.previousPage()}
                        className={!table.getCanPreviousPage() ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                      />
                    </PaginationItem>
                    {Array.from({ length: table.getPageCount() }, (_, i) => (
                      <PaginationItem key={i}>
                        <PaginationLink
                          onClick={() => table.setPageIndex(i)}
                          isActive={table.getState().pagination.pageIndex === i}
                          className="cursor-pointer"
                        >
                          {i + 1}
                        </PaginationLink>
                      </PaginationItem>
                    ))}
                    <PaginationItem>
                      <PaginationNext
                        onClick={() => table.nextPage()}
                        className={!table.getCanNextPage() ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                      />
                    </PaginationItem>
                  </PaginationContent>
                </Pagination>
              </div>
            )}

            {/* Total */}
            {selectedRows.length > 0 && (
              <div className="flex justify-end items-center gap-2 pt-2 border-t">
                <span className="text-sm font-medium">Total seleccionado:</span>
                <span className="text-lg font-bold">
                  {new Intl.NumberFormat('es-AR', {
                    style: 'currency',
                    currency: 'ARS',
                  }).format(total)}
                </span>
              </div>
            )}
          </div>
          </form>
        </ScrollArea>

        <DialogFooter className="mt-4">
          <Button
            type="button"
            variant="outline"
            onClick={() => {
              onOpenChange(false)
              setRowSelection({})
              setDate(undefined)
              setCuenta('')
            }}
          >
            Cancelar
          </Button>
          <Button type="submit" onClick={handleSubmit}>Pagar resumen</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

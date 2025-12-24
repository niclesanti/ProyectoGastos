import { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
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
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuCheckboxItem,
} from '@/components/ui/dropdown-menu'
import { Badge } from '@/components/ui/badge'
import { MoreHorizontal, GripVertical, ChevronDown } from 'lucide-react'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'
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

interface Transaction {
  id: string
  tipo: 'Ingreso' | 'Gasto'
  fecha: string
  motivo: string
  contacto: string
  cuenta: string
  monto: number
}

const mockTransactions: Transaction[] = [
  {
    id: '1',
    tipo: 'Ingreso',
    fecha: '2025-01-15',
    motivo: 'Salario',
    contacto: 'Empresa ABC',
    cuenta: 'Cuenta Principal',
    monto: 45000,
  },
  {
    id: '2',
    tipo: 'Gasto',
    fecha: '2025-01-10',
    motivo: 'Supermercado',
    contacto: 'Carrefour',
    cuenta: 'Cuenta Principal',
    monto: 8500,
  },
  {
    id: '3',
    tipo: 'Gasto',
    fecha: '2025-01-08',
    motivo: 'Transporte',
    contacto: 'YPF',
    cuenta: 'Gastos',
    monto: 6000,
  },
  {
    id: '4',
    tipo: 'Ingreso',
    fecha: '2025-01-05',
    motivo: 'Freelance',
    contacto: 'Cliente XYZ',
    cuenta: 'Ahorros',
    monto: 15000,
  },
  {
    id: '5',
    tipo: 'Gasto',
    fecha: '2025-01-03',
    motivo: 'Entretenimiento',
    contacto: 'Netflix',
    cuenta: 'Cuenta Principal',
    monto: 1200,
  },
  {
    id: '6',
    tipo: 'Gasto',
    fecha: '2024-12-28',
    motivo: 'Servicios',
    contacto: 'EDESUR',
    cuenta: 'Cuenta Principal',
    monto: 3200,
  },
  {
    id: '7',
    tipo: 'Gasto',
    fecha: '2024-12-28',
    motivo: 'Alimentos',
    contacto: 'Alvear',
    cuenta: 'Cuenta Principal',
    monto: 10000,
  },
]

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
    <TableRow ref={setNodeRef} style={style} data-state={row.getIsSelected() && 'selected'}>
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

export function MovimientosPage() {
  const [mes, setMes] = useState<string>('todos')
  const [ano, setAno] = useState<string>('2025')
  const [motivo, setMotivo] = useState<string>('todos')
  const [contacto, setContacto] = useState<string>('todos')
  const [ordenarPor, setOrdenarPor] = useState<string>('fecha-desc')

  const [transactions, setTransactions] = useState<Transaction[]>(mockTransactions)
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({})

  const columns: ColumnDef<Transaction>[] = [
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
      accessorKey: 'tipo',
      header: 'Tipo',
      cell: ({ row }) => {
        const tipo = row.getValue('tipo') as string
        return (
          <Badge
            variant={tipo === 'Ingreso' ? 'default' : 'destructive'}
            className={tipo === 'Ingreso' ? 'bg-green-500 hover:bg-green-600' : ''}
          >
            {tipo}
          </Badge>
        )
      },
    },
    {
      accessorKey: 'motivo',
      header: 'Motivo',
    },
    {
      accessorKey: 'cuenta',
      header: 'Cuenta',
    },
    {
      accessorKey: 'contacto',
      header: 'Contacto',
    },
    {
      accessorKey: 'fecha',
      header: 'Fecha',
      cell: ({ row }) => {
        const fecha = row.getValue('fecha') as string
        return format(new Date(fecha), 'dd/MM/yyyy', { locale: es })
      },
    },
    {
      accessorKey: 'monto',
      header: () => <div className="text-right">Monto</div>,
      cell: ({ row }) => {
        const amount = parseFloat(row.getValue('monto'))
        const formatted = new Intl.NumberFormat('es-AR', {
          style: 'currency',
          currency: 'ARS',
        }).format(amount)
        return <div className="text-right font-medium">{formatted}</div>
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
                Ver detalles
              </DropdownMenuItem>
              <DropdownMenuItem
                onClick={() => console.log('Eliminar:', row.original)}
                className="text-destructive"
              >
                Eliminar
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
    data: transactions,
    columns,
    getCoreRowModel: getCoreRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    onColumnVisibilityChange: setColumnVisibility,
    state: {
      columnVisibility,
    },
    initialState: {
      pagination: {
        pageSize: 6,
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
      setTransactions((items) => {
        const oldIndex = items.findIndex((item) => item.id === active.id)
        const newIndex = items.findIndex((item) => item.id === over.id)
        return arrayMove(items, oldIndex, newIndex)
      })
    }
  }

  const handleBuscar = () => {
    console.log('Buscar:', { mes, ano, motivo, contacto, ordenarPor })
    // Aquí iría la lógica de búsqueda
  }

  const handleLimpiar = () => {
    setMes('todos')
    setAno('2025')
    setMotivo('todos')
    setContacto('todos')
    setOrdenarPor('fecha-desc')
  }

  // Calcular totales
  const totalIngresos = transactions
    .filter((t) => t.tipo === 'Ingreso')
    .reduce((sum, t) => sum + t.monto, 0)

  const totalGastos = transactions
    .filter((t) => t.tipo === 'Gasto')
    .reduce((sum, t) => sum + t.monto, 0)

  return (
    <div className="space-y-6 pt-6">
      <Card>
        <CardHeader>
          <CardTitle>Ver movimientos</CardTitle>
          <p className="text-sm text-muted-foreground">
            Buscar movimientos de dinero registrados anteriormente. Todos los campos de búsqueda son opcionales. Excepción: si ingresas un "mes" debes especificar su "año".
          </p>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            {/* Mes */}
            <div className="space-y-1.5">
              <Label htmlFor="mes" className="text-sm">Mes</Label>
              <Select value={mes} onValueChange={setMes}>
                <SelectTrigger id="mes" className="h-9">
                  <SelectValue placeholder="Todos" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="todos">Todos</SelectItem>
                  <SelectItem value="1">Enero</SelectItem>
                  <SelectItem value="2">Febrero</SelectItem>
                  <SelectItem value="3">Marzo</SelectItem>
                  <SelectItem value="4">Abril</SelectItem>
                  <SelectItem value="5">Mayo</SelectItem>
                  <SelectItem value="6">Junio</SelectItem>
                  <SelectItem value="7">Julio</SelectItem>
                  <SelectItem value="8">Agosto</SelectItem>
                  <SelectItem value="9">Septiembre</SelectItem>
                  <SelectItem value="10">Octubre</SelectItem>
                  <SelectItem value="11">Noviembre</SelectItem>
                  <SelectItem value="12">Diciembre</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Año */}
            <div className="space-y-1.5">
              <Label htmlFor="ano" className="text-sm">Año</Label>
              <Select value={ano} onValueChange={setAno}>
                <SelectTrigger id="ano" className="h-9">
                  <SelectValue placeholder="2025" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="2025">2025</SelectItem>
                  <SelectItem value="2024">2024</SelectItem>
                  <SelectItem value="2023">2023</SelectItem>
                  <SelectItem value="2022">2022</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Motivo */}
            <div className="space-y-1.5">
              <Label htmlFor="motivo" className="text-sm">Motivo</Label>
              <Select value={motivo} onValueChange={setMotivo}>
                <SelectTrigger id="motivo" className="h-9">
                  <SelectValue placeholder="Seleccionar motivo" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="todos">Seleccionar motivo</SelectItem>
                  <SelectItem value="salario">Salario</SelectItem>
                  <SelectItem value="freelance">Freelance</SelectItem>
                  <SelectItem value="supermercado">Supermercado</SelectItem>
                  <SelectItem value="transporte">Transporte</SelectItem>
                  <SelectItem value="entretenimiento">Entretenimiento</SelectItem>
                  <SelectItem value="servicios">Servicios</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Contacto */}
            <div className="space-y-1.5">
              <Label htmlFor="contacto" className="text-sm">Contacto</Label>
              <Select value={contacto} onValueChange={setContacto}>
                <SelectTrigger id="contacto" className="h-9">
                  <SelectValue placeholder="Seleccionar contacto" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="todos">Seleccionar contacto</SelectItem>
                  <SelectItem value="empresa-abc">Empresa ABC</SelectItem>
                  <SelectItem value="cliente-xyz">Cliente XYZ</SelectItem>
                  <SelectItem value="carrefour">Carrefour</SelectItem>
                  <SelectItem value="ypf">YPF</SelectItem>
                  <SelectItem value="netflix">Netflix</SelectItem>
                  <SelectItem value="edesur">EDESUR</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Ordenar por */}
          <div className="space-y-1.5 mb-4">
            <Label htmlFor="ordenarPor" className="text-sm">Ordenar por</Label>
            <Select value={ordenarPor} onValueChange={setOrdenarPor}>
              <SelectTrigger id="ordenarPor" className="h-9">
                <SelectValue placeholder="Fecha (más recientes)" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="fecha-desc">Fecha (más recientes)</SelectItem>
                <SelectItem value="fecha-asc">Fecha (más antiguos)</SelectItem>
                <SelectItem value="monto-desc">Monto (mayor a menor)</SelectItem>
                <SelectItem value="monto-asc">Monto (menor a mayor)</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Botones */}
          <div className="flex justify-center gap-3">
            <Button
              variant="outline"
              onClick={handleLimpiar}
            >
              Limpiar
            </Button>
            <Button
              onClick={handleBuscar}
            >
              Buscar
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Data Table */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold">Transacciones</h3>
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
                      {column.id === 'tipo' ? 'Tipo' : 
                       column.id === 'motivo' ? 'Motivo' : 
                       column.id === 'cuenta' ? 'Cuenta' : 
                       column.id === 'contacto' ? 'Contacto' : 
                       column.id === 'fecha' ? 'Fecha' : 
                       column.id === 'monto' ? 'Monto' : column.id}
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
                <SortableContext items={transactions.map((i) => i.id)} strategy={verticalListSortingStrategy}>
                  {table.getRowModel().rows?.length ? (
                    table.getRowModel().rows.map((row) => (
                      <SortableRow key={row.id} row={row} />
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={columns.length} className="h-24 text-center">
                        No hay transacciones.
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

        {/* Totales */}
        <div className="flex justify-end gap-8 pt-4 border-t">
          <div className="text-right">
            <p className="text-sm text-muted-foreground mb-1">Total ingresos</p>
            <p className="text-xl font-bold text-green-600">
              {new Intl.NumberFormat('es-AR', {
                style: 'currency',
                currency: 'ARS',
              }).format(totalIngresos)}
            </p>
          </div>
          <div className="text-right">
            <p className="text-sm text-muted-foreground mb-1">Total gastos</p>
            <p className="text-xl font-bold text-red-600">
              {new Intl.NumberFormat('es-AR', {
                style: 'currency',
                currency: 'ARS',
              }).format(totalGastos)}
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

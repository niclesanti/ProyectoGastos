import { useState, useMemo } from 'react'
import { Button } from '@/components/ui/button'
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
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/components/ui/command'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuCheckboxItem,
} from '@/components/ui/dropdown-menu'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { 
  MoreHorizontal, 
  X, 
  Check,
  GripVertical,
  ChevronDown,
  ArrowUpDown,
} from 'lucide-react'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'
import { cn } from '@/lib/utils'
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
  descripcion?: string
}

function SortableRow({ row }: any) {
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

const mockTransactions: Transaction[] = [
  {
    id: '1',
    tipo: 'Ingreso',
    fecha: '2025-01-15',
    motivo: 'Salario',
    contacto: 'Empresa ABC',
    cuenta: 'Cuenta Principal',
    monto: 45000,
    descripcion: 'Salario mensual'
  },
  {
    id: '2',
    tipo: 'Gasto',
    fecha: '2025-01-10',
    motivo: 'Supermercado',
    contacto: 'Carrefour',
    cuenta: 'Cuenta Principal',
    monto: 8500,
    descripcion: 'Compras del mes'
  },
  {
    id: '3',
    tipo: 'Gasto',
    fecha: '2025-01-08',
    motivo: 'Transporte',
    contacto: 'YPF',
    cuenta: 'Gastos',
    monto: 6000,
    descripcion: 'Combustible'
  },
  {
    id: '4',
    tipo: 'Ingreso',
    fecha: '2025-01-05',
    motivo: 'Freelance',
    contacto: 'Cliente XYZ',
    cuenta: 'Ahorros',
    monto: 15000,
    descripcion: 'Proyecto web'
  },
  {
    id: '5',
    tipo: 'Gasto',
    fecha: '2025-01-03',
    motivo: 'Entretenimiento',
    contacto: 'Netflix',
    cuenta: 'Cuenta Principal',
    monto: 1200,
    descripcion: 'Suscripción mensual'
  },
  {
    id: '6',
    tipo: 'Gasto',
    fecha: '2024-12-28',
    motivo: 'Servicios',
    contacto: 'EDESUR',
    cuenta: 'Cuenta Principal',
    monto: 3200,
    descripcion: 'Factura de luz'
  },
  {
    id: '7',
    tipo: 'Ingreso',
    fecha: '2024-12-20',
    motivo: 'Venta',
    contacto: 'MercadoLibre',
    cuenta: 'Cuenta Principal',
    monto: 12000,
    descripcion: 'Venta de artículo'
  },
  {
    id: '8',
    tipo: 'Gasto',
    fecha: '2024-12-15',
    motivo: 'Alimentos',
    contacto: 'Alvear',
    cuenta: 'Cuenta Principal',
    monto: 10000,
    descripcion: 'Compras semanales'
  },
]

const meses = [
  { value: 'todos', label: 'Todos los meses' },
  { value: '1', label: 'Enero' },
  { value: '2', label: 'Febrero' },
  { value: '3', label: 'Marzo' },
  { value: '4', label: 'Abril' },
  { value: '5', label: 'Mayo' },
  { value: '6', label: 'Junio' },
  { value: '7', label: 'Julio' },
  { value: '8', label: 'Agosto' },
  { value: '9', label: 'Septiembre' },
  { value: '10', label: 'Octubre' },
  { value: '11', label: 'Noviembre' },
  { value: '12', label: 'Diciembre' },
]

const anos = [
  { value: 'todos', label: 'Todos los años' },
  { value: '2025', label: '2025' },
  { value: '2024', label: '2024' },
  { value: '2023', label: '2023' },
  { value: '2022', label: '2022' },
]

export function MovimientosPage() {
  const [transactions, setTransactions] = useState<Transaction[]>(mockTransactions)
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({})
  
  // Filtros
  const [mesSeleccionado, setMesSeleccionado] = useState('todos')
  const [anoSeleccionado, setAnoSeleccionado] = useState('todos')
  const [motivoSeleccionado, setMotivoSeleccionado] = useState('todos')
  const [contactoSeleccionado, setContactoSeleccionado] = useState('todos')
  
  // Ordenamiento
  const [ordenamiento, setOrdenamiento] = useState<'fecha-desc' | 'fecha-asc' | 'monto-desc' | 'monto-asc'>('fecha-desc')
  
  // Popovers state
  const [openMotivo, setOpenMotivo] = useState(false)
  const [openContacto, setOpenContacto] = useState(false)

  // Obtener listas únicas de motivos y contactos
  const motivos = useMemo(() => {
    const uniqueMotivos = Array.from(new Set(transactions.map(t => t.motivo)))
    return uniqueMotivos.sort()
  }, [transactions])

  const contactos = useMemo(() => {
    const uniqueContactos = Array.from(new Set(transactions.map(t => t.contacto)))
    return uniqueContactos.sort()
  }, [transactions])

  // Filtrar y ordenar transacciones
  const filteredTransactions = useMemo(() => {
    const filtered = transactions.filter(transaction => {
      // Filtro de mes
      const transactionDate = new Date(transaction.fecha)
      const transactionMonth = (transactionDate.getMonth() + 1).toString()
      const matchesMonth = mesSeleccionado === 'todos' || transactionMonth === mesSeleccionado

      // Filtro de año
      const transactionYear = transactionDate.getFullYear().toString()
      const matchesYear = anoSeleccionado === 'todos' || transactionYear === anoSeleccionado

      // Filtro de motivo
      const matchesMotivo = motivoSeleccionado === 'todos' || transaction.motivo === motivoSeleccionado

      // Filtro de contacto
      const matchesContacto = contactoSeleccionado === 'todos' || transaction.contacto === contactoSeleccionado

      return matchesMonth && matchesYear && matchesMotivo && matchesContacto
    })

    // Ordenar
    const sorted = [...filtered].sort((a, b) => {
      if (ordenamiento === 'fecha-desc') {
        return new Date(b.fecha).getTime() - new Date(a.fecha).getTime()
      } else if (ordenamiento === 'fecha-asc') {
        return new Date(a.fecha).getTime() - new Date(b.fecha).getTime()
      } else if (ordenamiento === 'monto-desc') {
        return b.monto - a.monto
      } else {
        return a.monto - b.monto
      }
    })

    return sorted
  }, [transactions, mesSeleccionado, anoSeleccionado, motivoSeleccionado, contactoSeleccionado, ordenamiento])

  // Calcular totales
  const { totalIngresos, totalGastos, cantidadResultados } = useMemo(() => {
    const ingresos = filteredTransactions
      .filter(t => t.tipo === 'Ingreso')
      .reduce((sum, t) => sum + t.monto, 0)
    
    const gastos = filteredTransactions
      .filter(t => t.tipo === 'Gasto')
      .reduce((sum, t) => sum + t.monto, 0)

    return {
      totalIngresos: ingresos,
      totalGastos: gastos,
      cantidadResultados: filteredTransactions.length
    }
  }, [filteredTransactions])

  // Verificar si hay filtros activos
  const hasActiveFilters = mesSeleccionado !== 'todos' || 
    anoSeleccionado !== 'todos' || 
    motivoSeleccionado !== 'todos' || 
    contactoSeleccionado !== 'todos'

  // Limpiar filtros
  const clearFilters = () => {
    setMesSeleccionado('todos')
    setAnoSeleccionado('todos')
    setMotivoSeleccionado('todos')
    setContactoSeleccionado('todos')
  }

  // Sensores DnD
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

  // Formatear moneda
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS',
      minimumFractionDigits: 2,
    }).format(amount)
  }

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
            className={cn(
              'font-medium',
              tipo === 'Ingreso' && 'bg-green-600 hover:bg-green-700'
            )}
          >
            {tipo}
          </Badge>
        )
      },
    },
    {
      accessorKey: 'motivo',
      header: 'Motivo',
      cell: ({ row }) => <div className="font-medium">{row.getValue('motivo')}</div>,
    },
    {
      accessorKey: 'cuenta',
      header: 'Cuenta',
      cell: ({ row }) => <div className="text-muted-foreground">{row.getValue('cuenta')}</div>,
    },
    {
      accessorKey: 'contacto',
      header: 'Contacto',
      cell: ({ row }) => <div>{row.getValue('contacto')}</div>,
    },
    {
      accessorKey: 'fecha',
      header: 'Fecha',
      cell: ({ row }) => {
        const fecha = new Date(row.getValue('fecha'))
        return <div className="text-muted-foreground">{format(fecha, 'dd/MM/yyyy', { locale: es })}</div>
      },
    },
    {
      accessorKey: 'monto',
      header: () => <div className="text-right">Monto</div>,
      cell: ({ row }) => {
        const monto = row.getValue('monto') as number
        const tipo = row.original.tipo
        return (
          <div className={cn(
            'text-right font-mono font-semibold',
            tipo === 'Ingreso' ? 'text-green-600' : 'text-red-600'
          )}>
            {tipo === 'Ingreso' ? '+' : '-'}{formatCurrency(monto)}
          </div>
        )
      },
    },
    {
      id: 'actions',
      cell: () => {
        return (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="h-8 w-8 p-0">
                <span className="sr-only">Abrir menú</span>
                <MoreHorizontal className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem>Ver detalles</DropdownMenuItem>
              <DropdownMenuItem className="text-destructive">Eliminar</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        )
      },
      enableHiding: false,
      size: 40,
    },
  ]

  const table = useReactTable({
    data: filteredTransactions,
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

  return (
    <div className="space-y-6 pt-6">
      {/* Header con resumen dinámico */}
      <div className="space-y-2">
        <h2 className="text-3xl font-bold tracking-tight">Movimientos</h2>
        <p className="text-muted-foreground">
          Explora y filtra tu historial financiero
        </p>
        
        {/* Summary Bar */}
        <div className="flex items-center gap-4 pt-2">
          <span className="text-sm text-muted-foreground">
            Mostrando <span className="font-semibold text-foreground">{cantidadResultados}</span> resultados
          </span>
          <Separator orientation="vertical" className="h-4" />
          <div className="flex items-center gap-1">
            <span className="text-sm text-muted-foreground">Ingresos:</span>
            <span className="text-sm font-semibold text-green-600">
              +{formatCurrency(totalIngresos)}
            </span>
          </div>
          <Separator orientation="vertical" className="h-4" />
          <div className="flex items-center gap-1">
            <span className="text-sm text-muted-foreground">Gastos:</span>
            <span className="text-sm font-semibold text-red-600">
              -{formatCurrency(totalGastos)}
            </span>
          </div>
        </div>
      </div>

      {/* Smart Toolbar - Filtros */}
      <div className="flex flex-wrap items-center gap-2 rounded-lg border bg-card p-4">
        {/* Selector de Mes */}
        <Select value={mesSeleccionado} onValueChange={setMesSeleccionado}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Seleccionar mes" />
          </SelectTrigger>
          <SelectContent>
            {meses.map((mes) => (
              <SelectItem key={mes.value} value={mes.value}>
                {mes.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        {/* Selector de Año */}
        <Select value={anoSeleccionado} onValueChange={setAnoSeleccionado}>
          <SelectTrigger className="w-[160px]">
            <SelectValue placeholder="Año" />
          </SelectTrigger>
          <SelectContent>
            {anos.map((ano) => (
              <SelectItem key={ano.value} value={ano.value}>
                {ano.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        {/* Filtro facetado de Motivo */}
        <Popover open={openMotivo} onOpenChange={setOpenMotivo}>
          <PopoverTrigger asChild>
            <Button variant="outline" className="w-[180px] justify-between">
              {motivoSeleccionado === 'todos' ? 'Motivo' : motivoSeleccionado}
              <Check className={cn(
                'ml-2 h-4 w-4',
                motivoSeleccionado !== 'todos' ? 'opacity-100' : 'opacity-0'
              )} />
            </Button>
          </PopoverTrigger>
          <PopoverContent className="w-[200px] p-0" align="start">
            <Command>
              <CommandInput placeholder="Buscar motivo..." />
              <CommandList>
                <CommandEmpty>No se encontró el motivo.</CommandEmpty>
                <CommandGroup>
                  <CommandItem
                    onSelect={() => {
                      setMotivoSeleccionado('todos')
                      setOpenMotivo(false)
                    }}
                  >
                    <Check
                      className={cn(
                        'mr-2 h-4 w-4',
                        motivoSeleccionado === 'todos' ? 'opacity-100' : 'opacity-0'
                      )}
                    />
                    Todos los motivos
                  </CommandItem>
                  {motivos.map((motivo) => (
                    <CommandItem
                      key={motivo}
                      onSelect={() => {
                        setMotivoSeleccionado(motivo)
                        setOpenMotivo(false)
                      }}
                    >
                      <Check
                        className={cn(
                          'mr-2 h-4 w-4',
                          motivoSeleccionado === motivo ? 'opacity-100' : 'opacity-0'
                        )}
                      />
                      {motivo}
                    </CommandItem>
                  ))}
                </CommandGroup>
              </CommandList>
            </Command>
          </PopoverContent>
        </Popover>

        {/* Filtro facetado de Contacto */}
        <Popover open={openContacto} onOpenChange={setOpenContacto}>
          <PopoverTrigger asChild>
            <Button variant="outline" className="w-[180px] justify-between">
              {contactoSeleccionado === 'todos' ? 'Contacto' : contactoSeleccionado}
              <Check className={cn(
                'ml-2 h-4 w-4',
                contactoSeleccionado !== 'todos' ? 'opacity-100' : 'opacity-0'
              )} />
            </Button>
          </PopoverTrigger>
          <PopoverContent className="w-[200px] p-0" align="start">
            <Command>
              <CommandInput placeholder="Buscar contacto..." />
              <CommandList>
                <CommandEmpty>No se encontró el contacto.</CommandEmpty>
                <CommandGroup>
                  <CommandItem
                    onSelect={() => {
                      setContactoSeleccionado('todos')
                      setOpenContacto(false)
                    }}
                  >
                    <Check
                      className={cn(
                        'mr-2 h-4 w-4',
                        contactoSeleccionado === 'todos' ? 'opacity-100' : 'opacity-0'
                      )}
                    />
                    Todos los contactos
                  </CommandItem>
                  {contactos.map((contacto) => (
                    <CommandItem
                      key={contacto}
                      onSelect={() => {
                        setContactoSeleccionado(contacto)
                        setOpenContacto(false)
                      }}
                    >
                      <Check
                        className={cn(
                          'mr-2 h-4 w-4',
                          contactoSeleccionado === contacto ? 'opacity-100' : 'opacity-0'
                        )}
                      />
                      {contacto}
                    </CommandItem>
                  ))}
                </CommandGroup>
              </CommandList>
            </Command>
          </PopoverContent>
        </Popover>

        {/* Botón Limpiar Filtros */}
        {hasActiveFilters && (
          <Button
            variant="ghost"
            onClick={clearFilters}
            className="h-10 px-3"
          >
            <X className="mr-2 h-4 w-4" />
            Limpiar
          </Button>
        )}
      </div>

      {/* Data Table */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold">Transacciones</h3>
          <div className="flex items-center gap-2">
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline" size="sm" className="h-8">
                  <ArrowUpDown className="mr-2 h-4 w-4" />
                  Ordenar
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={() => setOrdenamiento('fecha-desc')}>
                  <Check className={cn('mr-2 h-4 w-4', ordenamiento === 'fecha-desc' ? 'opacity-100' : 'opacity-0')} />
                  Fecha (más reciente)
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setOrdenamiento('fecha-asc')}>
                  <Check className={cn('mr-2 h-4 w-4', ordenamiento === 'fecha-asc' ? 'opacity-100' : 'opacity-0')} />
                  Fecha (más antigua)
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setOrdenamiento('monto-desc')}>
                  <Check className={cn('mr-2 h-4 w-4', ordenamiento === 'monto-desc' ? 'opacity-100' : 'opacity-0')} />
                  Monto (mayor a menor)
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setOrdenamiento('monto-asc')}>
                  <Check className={cn('mr-2 h-4 w-4', ordenamiento === 'monto-asc' ? 'opacity-100' : 'opacity-0')} />
                  Monto (menor a mayor)
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
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
                <SortableContext items={filteredTransactions.map((i) => i.id)} strategy={verticalListSortingStrategy}>
                  {table.getRowModel().rows?.length ? (
                    table.getRowModel().rows.map((row) => (
                      <SortableRow key={row.id} row={row} />
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={columns.length} className="h-24 text-center">
                        No se encontraron resultados.
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
      </div>
    </div>
  )
}

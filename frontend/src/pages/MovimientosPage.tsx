import { useState, useMemo } from 'react'
import { useAppStore } from '@/store/app-store'
import { useBuscarTransacciones, useMotivosTransaccion, useContactosTransaccion } from '@/features/selectors/api/selector-queries'
import { toast } from 'sonner'
import { TransactionDetailsModal } from '@/components/TransactionDetailsModal'
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
  Search,
  Eye,
  Trash2,
} from 'lucide-react'
import { format, parseISO } from 'date-fns'
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
  nombreEspacioTrabajo: string
  nombreCompletoAuditoria: string
  fechaCreacion: string
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
  { value: '2030', label: '2030' },
  { value: '2029', label: '2029' },
  { value: '2028', label: '2028' },
  { value: '2027', label: '2027' },
  { value: '2026', label: '2026' },
  { value: '2025', label: '2025' },
  { value: '2024', label: '2024' },
]

export function MovimientosPage() {
  const espacioActual = useAppStore((state) => state.currentWorkspace)
  
  // Hooks de TanStack Query
  const buscarTransaccionesMutation = useBuscarTransacciones()
  const { data: motivosData = [], isLoading: isLoadingMotivos } = useMotivosTransaccion(espacioActual?.id)
  const { data: contactosData = [], isLoading: isLoadingContactos } = useContactosTransaccion(espacioActual?.id)
  
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [hasSearched, setHasSearched] = useState(false)
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({})
  
  // Estado para el modal de detalles
  const [selectedTransaction, setSelectedTransaction] = useState<Transaction | null>(null)
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false)
  
  // Filtros
  const [mesSeleccionado, setMesSeleccionado] = useState('12')
  const [anoSeleccionado, setAnoSeleccionado] = useState('2025')
  const [motivoSeleccionado, setMotivoSeleccionado] = useState('todos')
  const [contactoSeleccionado, setContactoSeleccionado] = useState('todos')
  
  // Ordenamiento
  const [ordenamiento, setOrdenamiento] = useState<'fecha-desc' | 'fecha-asc' | 'monto-desc' | 'monto-asc'>('fecha-desc')
  
  // Popovers state
  const [openMotivo, setOpenMotivo] = useState(false)
  const [openContacto, setOpenContacto] = useState(false)

  // Convertir datos de la BD a formato de la UI
  const motivos = useMemo(() => {
    if (!motivosData || motivosData.length === 0) return []
    return motivosData.map(m => m.motivo).sort()
  }, [motivosData])

  const contactos = useMemo(() => {
    if (!contactosData || contactosData.length === 0) return []
    return contactosData.map(c => c.nombre).sort()
  }, [contactosData])

  // Función para buscar transacciones
  const handleBuscar = async () => {
    if (!espacioActual?.id) {
      toast.error('Error de configuración', {
        description: 'No se pudo identificar el espacio de trabajo. Intenta recargar la página.',
      })
      return
    }

    const busquedaDTO = {
      mes: mesSeleccionado === 'todos' ? null : parseInt(mesSeleccionado),
      anio: anoSeleccionado === 'todos' ? null : parseInt(anoSeleccionado),
      motivo: motivoSeleccionado === 'todos' ? null : motivoSeleccionado,
      contacto: contactoSeleccionado === 'todos' ? null : contactoSeleccionado,
      idEspacioTrabajo: espacioActual.id,
    }

    buscarTransaccionesMutation.mutate(busquedaDTO, {
      onSuccess: (data) => {
        // Transformar los datos de la API al formato de la UI
        const transaccionesTransformadas = data.map(t => ({
          id: t.id.toString(),
          tipo: t.tipo === 'INGRESO' ? 'Ingreso' as const : 'Gasto' as const,
          fecha: t.fecha,
          motivo: t.nombreMotivo || 'Sin motivo',
          contacto: t.nombreContacto || 'Sin contacto',
          cuenta: t.nombreCuentaBancaria || 'Sin cuenta',
          monto: t.monto,
          descripcion: t.descripcion,
          nombreEspacioTrabajo: t.nombreEspacioTrabajo,
          nombreCompletoAuditoria: t.nombreCompletoAuditoria,
          fechaCreacion: t.fechaCreacion,
        }))
        
        setTransactions(transaccionesTransformadas)
        setHasSearched(true)
        
        toast.success('Búsqueda completada', {
          description: `Se encontraron ${transaccionesTransformadas.length} transacciones.`,
        })
      },
      onError: (error: any) => {
        console.error('Error al buscar transacciones:', error)
        toast.error('Error al buscar transacciones', {
          description: error?.message || 'Intenta nuevamente o contacta al soporte.',
        })
      },
    })
  }

  // Filtrar y ordenar transacciones (solo para ordenar, el filtrado lo hace la API)
  const filteredTransactions = useMemo(() => {
    const sorted = [...transactions].sort((a, b) => {
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
  }, [transactions, ordenamiento])

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
    setMesSeleccionado('12')
    setAnoSeleccionado('2025')
    setMotivoSeleccionado('todos')
    setContactoSeleccionado('todos')
  }

  // Handler para cambiar el año y resetear el mes si es necesario
  const handleAnoChange = (value: string) => {
    setAnoSeleccionado(value)
    if (value === 'todos') {
      setMesSeleccionado('todos')
    }
  }

  // Handler para abrir el modal de detalles
  const handleViewDetails = (transaction: Transaction) => {
    setSelectedTransaction(transaction)
    setIsDetailsModalOpen(true)
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
        const fecha = parseISO(row.getValue('fecha'))
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
            'text-right font-mono font-semibold tabular-nums',
            tipo === 'Ingreso' ? 'text-emerald-400' : 'text-rose-400'
          )}>
            {tipo === 'Ingreso' ? '+' : '-'}{formatCurrency(monto)}
          </div>
        )
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
              <DropdownMenuItem onClick={() => handleViewDetails(row.original)}>
                <Eye className="mr-2 h-4 w-4" />
                Ver detalles
              </DropdownMenuItem>
              <DropdownMenuItem className="text-destructive">
                <Trash2 className="mr-2 h-4 w-4" />
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
      <div className="space-y-2 mb-8">
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
            <span className="text-sm font-semibold text-emerald-400/90">
              +{formatCurrency(totalIngresos)}
            </span>
          </div>
          <Separator orientation="vertical" className="h-4" />
          <div className="flex items-center gap-1">
            <span className="text-sm text-muted-foreground">Gastos:</span>
            <span className="text-sm font-semibold text-rose-400/90">
              -{formatCurrency(totalGastos)}
            </span>
          </div>
        </div>
      </div>

      {/* Smart Toolbar - Filtros */}
      <div className="flex flex-wrap items-center gap-2 rounded-lg border bg-card p-4">
        {/* Selector de Mes */}
        <Select value={mesSeleccionado} onValueChange={setMesSeleccionado} disabled={anoSeleccionado === 'todos'}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Filtrar por mes..." />
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
        <Select value={anoSeleccionado} onValueChange={handleAnoChange}>
          <SelectTrigger className="w-[160px]">
            <SelectValue placeholder="Filtrar por año..." />
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
              {motivoSeleccionado === 'todos' ? 'Filtrar por motivo...' : motivoSeleccionado}
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
              {contactoSeleccionado === 'todos' ? 'Filtrar por contacto...' : contactoSeleccionado}
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

        {/* Botón Buscar */}
        <Button
          onClick={handleBuscar}
          disabled={buscarTransaccionesMutation.isPending || !espacioActual}
          size="sm"
        >
          <Search className="mr-2 h-4 w-4" />
          {buscarTransaccionesMutation.isPending ? 'Buscando...' : 'Buscar'}
        </Button>

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
                      <TableCell colSpan={columns.length} className="h-48 text-center">
                        <div className="flex flex-col items-center justify-center gap-2">
                          {!hasSearched ? (
                            <>
                              <Search className="h-12 w-12 text-muted-foreground/50" />
                              <p className="text-lg font-semibold text-muted-foreground">
                                Realiza una búsqueda
                              </p>
                              <p className="text-sm text-muted-foreground">
                                Selecciona los filtros y presiona "Buscar" para ver tus transacciones
                              </p>
                            </>
                          ) : (
                            <>
                              <X className="h-12 w-12 text-muted-foreground/50" />
                              <p className="text-lg font-semibold text-muted-foreground">
                                No se encontraron transacciones
                              </p>
                              <p className="text-sm text-muted-foreground">
                                Intenta ajustar los filtros de búsqueda
                              </p>
                            </>
                          )}
                        </div>
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

      {/* Modal de Detalles de Transacción */}
      <TransactionDetailsModal
        transaction={selectedTransaction}
        open={isDetailsModalOpen}
        onOpenChange={setIsDetailsModalOpen}
      />
    </div>
  )
}

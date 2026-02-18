import { useState, useMemo } from 'react'
import { useAppStore } from '@/store/app-store'
import { useBuscarTransacciones, useMotivosTransaccion, useContactosTransaccion, useRemoverTransaccion } from '@/features/selectors/api/selector-queries'
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
import { Card } from '@/components/ui/card'
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
  ArrowLeftRight,
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
import { MoneyDecimal } from '@/lib/money'
import { useMoney } from '@/hooks/useMoney'
import { Button } from '@/components/ui/button'
import { toast } from '@/hooks/useToast'
import { useQueryClient } from '@tanstack/react-query'
import { TransactionDetailsModal } from '@/components/TransactionDetailsModal'
import { DeleteConfirmDialog } from '@/components/DeleteConfirmDialog'
import { formatCurrency } from '@/lib/utils'

interface Transaction {
  id: string
  tipo: 'Ingreso' | 'Gasto'
  fecha: string
  motivo: string
  contacto: string
  cuenta: string
  monto: MoneyDecimal
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

const currentYear = new Date().getFullYear()
const anos = [
  { value: 'todos', label: 'Todos los años' },
  ...Array.from({ length: 7 }, (_, i) => {
    const y = (currentYear + 3 - i).toString()
    return { value: y, label: y }
  })
]

export function MovimientosPage() {
  const espacioActual = useAppStore((state) => state.currentWorkspace)
  const queryClient = useQueryClient()
  
  // Defaults dinámicos para mes y año
  const today = new Date()
  const currentMonthDefault = (today.getMonth() + 1).toString()
  const currentYearDefault = today.getFullYear().toString()

  // Hooks de TanStack Query
  const buscarTransaccionesMutation = useBuscarTransacciones()
  const removerTransaccionMutation = useRemoverTransaccion()
  const { data: motivosData = [] } = useMotivosTransaccion(espacioActual?.id)
  const { data: contactosData = [] } = useContactosTransaccion(espacioActual?.id)
  
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [hasSearched, setHasSearched] = useState(false)
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({})
  
  // Paginación - Backend
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize] = useState(10)
  const [totalElements, setTotalElements] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  
  // Estado para el modal de detalles
  const [selectedTransaction, setSelectedTransaction] = useState<Transaction | null>(null)
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false)
  
  // Estado para el diálogo de confirmación de eliminación
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false)
  const [transactionToDelete, setTransactionToDelete] = useState<Transaction | null>(null)
  
  // Filtros
  const [mesSeleccionado, setMesSeleccionado] = useState(currentMonthDefault)
  const [anoSeleccionado, setAnoSeleccionado] = useState(currentYearDefault)
  const [motivoSeleccionado, setMotivoSeleccionado] = useState('todos')
  const [contactoSeleccionado, setContactoSeleccionado] = useState('todos')
  
  // Ordenamiento (ya no se usa por ahora, el backend ordena por fechaCreacion DESC)
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

  // Función para buscar transacciones con paginación
  const handleBuscar = async (page: number = 0) => {
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
      page: page,
      size: pageSize,
    }

    buscarTransaccionesMutation.mutate(busquedaDTO, {
      onSuccess: (response) => {
        // Transformar los datos de la API al formato de la UI
        const transaccionesTransformadas = response.content.map(t => ({
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
        setCurrentPage(response.currentPage)
        setTotalElements(response.totalElements)
        setTotalPages(response.totalPages)
        setHasSearched(true)
        
        toast.success('Búsqueda completada', {
          description: `Se encontraron ${response.totalElements} transacciones.`,
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

  const { sum } = useMoney()

  // Las transacciones ya vienen ordenadas del backend por fechaCreacion DESC
  const filteredTransactions = transactions

  // Calcular totales de la página actual
  const { totalIngresos, totalGastos } = useMemo(() => {
    const transaccionesIngresos = filteredTransactions.filter(t => t.tipo === 'Ingreso')
    const transaccionesGastos = filteredTransactions.filter(t => t.tipo === 'Gasto')
    
    return {
      totalIngresos: sum(transaccionesIngresos.map(t => t.monto)),
      totalGastos: sum(transaccionesGastos.map(t => t.monto)),
    }
  }, [filteredTransactions, sum])

  // Verificar si hay filtros activos
  const hasActiveFilters = mesSeleccionado !== 'todos' || 
    anoSeleccionado !== 'todos' || 
    motivoSeleccionado !== 'todos' || 
    contactoSeleccionado !== 'todos'

  // Limpiar filtros
  const clearFilters = () => {
    setMesSeleccionado(currentMonthDefault)
    setAnoSeleccionado(currentYearDefault)
    setMotivoSeleccionado('todos')
    setContactoSeleccionado('todos')
    setCurrentPage(0)
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

  // Handler para eliminar transacción
  const handleDeleteClick = (transaction: Transaction) => {
    setTransactionToDelete(transaction)
    setDeleteConfirmOpen(true)
  }

  const handleDeleteConfirm = () => {
    if (!transactionToDelete) return

    removerTransaccionMutation.mutate(parseInt(transactionToDelete.id), {
      onSuccess: () => {
        toast.success('Transacción eliminada', {
          description: 'La transacción ha sido eliminada correctamente.',
        })
        // Invalidar caché de workspaces para actualizar el saldo en la sidebar
        queryClient.invalidateQueries({ queryKey: ['workspaces'] })
        // Recargar la búsqueda actual
        handleBuscar()
      },
      onError: (error: any) => {
        console.error('Error al eliminar transacción:', error)
        toast.error('Error al eliminar', {
          description: error?.response?.data?.message || 'No se pudo eliminar la transacción. Intenta nuevamente.',
        })
      },
    })
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
      cell: ({ row }) => <div className="font-medium hidden md:block md:table-cell">{row.getValue('motivo')}</div>,
    },
    {
      accessorKey: 'cuenta',
      header: () => <div className="hidden lg:table-cell">Cuenta</div>,
      cell: ({ row }) => <div className="text-muted-foreground hidden lg:block lg:table-cell">{row.getValue('cuenta')}</div>,
    },
    {
      accessorKey: 'contacto',
      header: () => <div className="hidden lg:table-cell">Contacto</div>,
      cell: ({ row }) => <div className="hidden lg:block lg:table-cell">{row.getValue('contacto')}</div>,
    },
    {
      accessorKey: 'fecha',
      header: () => <div className="hidden sm:table-cell">Fecha</div>,
      cell: ({ row }) => {
        const fecha = parseISO(row.getValue('fecha'))
        return <div className="text-muted-foreground hidden sm:block sm:table-cell">{format(fecha, 'dd/MM/yyyy', { locale: es })}</div>
      },
    },
    {
      accessorKey: 'monto',
      header: () => <div className="text-right">Monto</div>,
      cell: ({ row }) => {
        const monto = row.getValue('monto') as MoneyDecimal
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
              <DropdownMenuItem 
                onClick={() => handleDeleteClick(row.original)}
                className="text-destructive focus:text-destructive"
                disabled={removerTransaccionMutation.isPending}
              >
                <Trash2 className="mr-2 h-4 w-4" />
                {removerTransaccionMutation.isPending ? 'Eliminando...' : 'Eliminar'}
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
    onColumnVisibilityChange: setColumnVisibility,
    state: {
      columnVisibility,
    },
    // Sin paginación del cliente ya que la manejamos del lado del servidor
  })

  if (!espacioActual) {
    return (
      <div className="flex items-center justify-center min-h-[calc(100vh-200px)] p-6">
        <Card className="max-w-2xl w-full border-dashed border-2 bg-zinc-950/50 p-12">
          <div className="flex flex-col items-center text-center space-y-6">
            <div className="rounded-full bg-zinc-900 p-6">
              <ArrowLeftRight className="h-16 w-16 text-muted-foreground/50" />
            </div>
            
            <div className="space-y-2">
              <h3 className="text-2xl font-semibold tracking-tight">
                Selecciona un espacio de trabajo
              </h3>
              <p className="text-muted-foreground max-w-md">
                Para explorar tu historial de transacciones, primero elige un espacio en el menú lateral o crea uno nuevo.
              </p>
            </div>

            <div className="pt-4">
              <div className="inline-flex items-center gap-2 text-sm text-muted-foreground">
                <div className="h-1.5 w-1.5 rounded-full bg-muted-foreground/50" />
                <span>Usa el menú lateral para comenzar</span>
              </div>
            </div>
          </div>
        </Card>
      </div>
    )
  }

  return (
    <div className="space-y-4 sm:space-y-6 pt-4 sm:pt-6">
      {/* Header con resumen dinámico */}
      <div className="space-y-2 mb-4 sm:mb-8">
        <h2 className="text-2xl sm:text-3xl font-bold tracking-tight">Movimientos</h2>
        <p className="text-sm sm:text-base text-muted-foreground">
          Explora y filtra tu historial financiero
        </p>
        
        {/* Summary Bar */}
        <div className="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-4 pt-2">
          <span className="text-xs sm:text-sm text-muted-foreground">
            Mostrando <span className="font-semibold text-foreground">{totalElements}</span> resultados
          </span>
          <Separator orientation="vertical" className="hidden sm:block h-4" />
          <div className="flex items-center gap-1">
            <span className="text-xs sm:text-sm text-muted-foreground">Ingresos:</span>
            <span className="text-xs sm:text-sm font-semibold text-emerald-400/90">
              +{formatCurrency(totalIngresos)}
            </span>
          </div>
          <Separator orientation="vertical" className="hidden sm:block h-4" />
          <div className="flex items-center gap-1">
            <span className="text-xs sm:text-sm text-muted-foreground">Gastos:</span>
            <span className="text-xs sm:text-sm font-semibold text-rose-400/90">
              -{formatCurrency(totalGastos)}
            </span>
          </div>
        </div>
      </div>

      {/* Smart Toolbar - Filtros */}
      <div className="flex flex-col sm:flex-row sm:flex-wrap items-stretch sm:items-center gap-2 rounded-lg border bg-card p-3 sm:p-4">
        {/* Selector de Mes */}
        <Select value={mesSeleccionado} onValueChange={setMesSeleccionado} disabled={anoSeleccionado === 'todos'}>
          <SelectTrigger className="w-full sm:w-[180px]">
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
          <SelectTrigger className="w-full sm:w-[160px]">
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
          onClick={() => handleBuscar(0)}
          disabled={buscarTransaccionesMutation.isPending || !espacioActual}
          size="sm"
          className="w-full sm:w-auto"
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

        {/* Pagination - Server Side */}
        {hasSearched && totalPages > 1 && (
          <div className="flex justify-end">
            <Pagination>
              <PaginationContent>
                <PaginationItem>
                  <PaginationPrevious
                    onClick={() => {
                      if (currentPage > 0) {
                        handleBuscar(currentPage - 1)
                      }
                    }}
                    className={currentPage === 0 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                  />
                </PaginationItem>
                {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                  // Mostrar máximo 5 páginas alrededor de la página actual
                  let pageNumber: number
                  if (totalPages <= 5) {
                    pageNumber = i
                  } else if (currentPage < 3) {
                    pageNumber = i
                  } else if (currentPage >= totalPages - 3) {
                    pageNumber = totalPages - 5 + i
                  } else {
                    pageNumber = currentPage - 2 + i
                  }

                  return (
                    <PaginationItem key={pageNumber}>
                      <PaginationLink
                        onClick={() => handleBuscar(pageNumber)}
                        isActive={currentPage === pageNumber}
                        className="cursor-pointer"
                      >
                        {pageNumber + 1}
                      </PaginationLink>
                    </PaginationItem>
                  )
                })}
                <PaginationItem>
                  <PaginationNext
                    onClick={() => {
                      if (currentPage < totalPages - 1) {
                        handleBuscar(currentPage + 1)
                      }
                    }}
                    className={currentPage >= totalPages - 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
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

      <DeleteConfirmDialog
        open={deleteConfirmOpen}
        onOpenChange={setDeleteConfirmOpen}
        onConfirm={handleDeleteConfirm}
        title="¿Eliminar transacción?"
        description="Esta acción no se puede deshacer. La transacción será eliminada permanentemente del sistema."
        isLoading={removerTransaccionMutation.isPending}
      />
    </div>
  )
}

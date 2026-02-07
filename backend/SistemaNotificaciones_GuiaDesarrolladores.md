# Sistema de Notificaciones - Guía para Desarrolladores

## 🔔 Cómo Agregar Notificaciones a tus Servicios

### 1. Inyectar ApplicationEventPublisher

En tu servicio, inyecta el `ApplicationEventPublisher`:

```java
@Service
@RequiredArgsConstructor
public class TuServicio {
    
    private final ApplicationEventPublisher eventPublisher;
    private static final Logger logger = LoggerFactory.getLogger(TuServicio.class);
    
    // ... resto del código
}
```

### 2. Publicar Evento de Notificación

Cuando ocurra una acción importante, publica un evento:

```java
public void tuMetodo(/* parámetros */) {
    // ... tu lógica de negocio ...
    
    try {
        // Publicar evento de notificación
        eventPublisher.publishEvent(new NotificacionEvent(
            this,                              // source: objeto que publica
            usuario.getId(),                   // UUID del destinatario
            TipoNotificacion.TIPO_APROPIADO,  // tipo de notificación
            "Mensaje descriptivo para el usuario"  // mensaje
        ));
    } catch (Exception e) {
        logger.error("Error al enviar notificación: {}", e.getMessage());
        // No propagamos la excepción para que no afecte la operación principal
    }
}
```

### 3. Ejemplo: Nueva Transacción

```java
@Service
@RequiredArgsConstructor
public class TransaccionServiceImpl implements TransaccionService {
    
    private final ApplicationEventPublisher eventPublisher;
    private final TransaccionRepository transaccionRepository;
    private static final Logger logger = LoggerFactory.getLogger(TransaccionServiceImpl.class);
    
    @Transactional
    public Transaccion registrarTransaccion(TransaccionDTORequest dto) {
        // 1. Validaciones
        EspacioTrabajo espacio = validarYObtenerEspacio(dto.getIdEspacioTrabajo());
        
        // 2. Crear y guardar transacción
        Transaccion transaccion = crearTransaccion(dto, espacio);
        transaccion = transaccionRepository.save(transaccion);
        
        // 3. Actualizar saldo del espacio
        actualizarSaldoEspacio(espacio, transaccion);
        
        // 4. Notificar al admin del espacio
        try {
            String mensaje = String.format(
                "Nueva %s de %s: %s",
                transaccion.getTipo() == TipoTransaccion.INGRESO ? "ingreso" : "gasto",
                formatearMonto(transaccion.getMonto()),
                transaccion.getDescripcion()
            );
            
            eventPublisher.publishEvent(new NotificacionEvent(
                this,
                espacio.getUsuarioAdmin().getId(),
                TipoNotificacion.TRANSACCION_ALTA,
                mensaje
            ));
        } catch (Exception e) {
            logger.error("Error al enviar notificación de transacción: {}", e.getMessage());
            // No propagamos la excepción
        }
        
        return transaccion;
    }
}
```

### 4. Ejemplo: Compra a Crédito

```java
@Service
@RequiredArgsConstructor
public class CompraCreditoServiceImpl implements CompraCreditoService {
    
    private final ApplicationEventPublisher eventPublisher;
    private final CompraCreditoRepository compraCreditoRepository;
    private static final Logger logger = LoggerFactory.getLogger(CompraCreditoServiceImpl.class);
    
    @Transactional
    public CompraCredito registrarCompraCredito(CompraCreditoDTORequest dto) {
        // ... lógica de negocio ...
        
        CompraCredito compra = compraCreditoRepository.save(nuevaCompra);
        
        // Notificar
        try {
            String mensaje = String.format(
                "Nueva compra en %d cuotas: %s por %s",
                compra.getCantidadCuotas(),
                compra.getDescripcion(),
                formatearMonto(compra.getMontoTotal())
            );
            
            eventPublisher.publishEvent(new NotificacionEvent(
                this,
                espacio.getUsuarioAdmin().getId(),
                TipoNotificacion.COMPRA_CREDITO,
                mensaje
            ));
        } catch (Exception e) {
            logger.error("Error al enviar notificación de compra: {}", e.getMessage());
        }
        
        return compra;
    }
}
```

### 5. Ejemplo: Pago de Resumen

```java
@Service
@RequiredArgsConstructor
public class ResumenTarjetaServiceImpl implements ResumenTarjetaService {
    
    private final ApplicationEventPublisher eventPublisher;
    private static final Logger logger = LoggerFactory.getLogger(ResumenTarjetaServiceImpl.class);
    
    @Transactional
    public void pagarResumen(Long idResumen) {
        ResumenTarjeta resumen = resumenRepository.findById(idResumen)
            .orElseThrow(() -> new EntityNotFoundException("Resumen no encontrado"));
        
        // Cambiar estado a PAGADO
        resumen.setEstado(EstadoResumen.PAGADO);
        resumenRepository.save(resumen);
        
        // Notificar al usuario
        try {
            String mensaje = String.format(
                "Resumen de tarjeta %s pagado correctamente. Monto: %s",
                resumen.getTarjeta().getRedDePago(),
                formatearMonto(resumen.getMontoTotal())
            );
            
            eventPublisher.publishEvent(new NotificacionEvent(
                this,
                resumen.getTarjeta().getEspacioTrabajo().getUsuarioAdmin().getId(),
                TipoNotificacion.PAGO_RESUMEN,
                mensaje
            ));
        } catch (Exception e) {
            logger.error("Error al enviar notificación de pago: {}", e.getMessage());
        }
    }
}
```

### 6. Ejemplo: Vencimiento Próximo

```java
@Component
@RequiredArgsConstructor
public class VencimientoScheduler {
    
    private final ResumenTarjetaRepository resumenRepository;
    private final ApplicationEventPublisher eventPublisher;
    private static final Logger logger = LoggerFactory.getLogger(VencimientoScheduler.class);
    
    /**
     * Se ejecuta diariamente para verificar resúmenes próximos a vencer.
     * Notifica a los usuarios 3 días antes del vencimiento.
     */
    @Scheduled(cron = "0 0 9 * * *") // Todos los días a las 9:00 AM
    @Transactional(readOnly = true)
    public void verificarVencimientos() {
        LocalDate fechaLimite = LocalDate.now().plusDays(3);
        
        List<ResumenTarjeta> resumenesPorVencer = resumenRepository
            .findByEstadoAndFechaVencimientoBetween(
                EstadoResumen.PENDIENTE,
                LocalDate.now(),
                fechaLimite
            );
        
        for (ResumenTarjeta resumen : resumenesPorVencer) {
            try {
                String mensaje = String.format(
                    "Resumen de tarjeta %s vence el %s. Monto: %s",
                    resumen.getTarjeta().getRedDePago(),
                    resumen.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    formatearMonto(resumen.getMontoTotal())
                );
                
                eventPublisher.publishEvent(new NotificacionEvent(
                    this,
                    resumen.getTarjeta().getEspacioTrabajo().getUsuarioAdmin().getId(),
                    TipoNotificacion.VENCIMIENTO_RESUMEN,
                    mensaje
                ));
                
                logger.info("Notificación de vencimiento enviada para resumen {}", resumen.getId());
            } catch (Exception e) {
                logger.error("Error al enviar notificación de vencimiento para resumen {}: {}", 
                           resumen.getId(), e.getMessage());
                // Continuar con los demás resúmenes
            }
        }
    }
}
```

---

## 🎯 Buenas Prácticas

### ✅ DO (Hacer)

1. **Siempre usar try-catch** al publicar eventos
   ```java
   try {
       eventPublisher.publishEvent(new NotificacionEvent(...));
   } catch (Exception e) {
       logger.error("Error al enviar notificación: {}", e.getMessage());
       // No propagar la excepción
   }
   ```

2. **Mensajes descriptivos y útiles**
   ```java
   // ✅ BIEN: Mensaje específico y accionable
   "Resumen cerrado de tarjeta Visa terminada en 1234. Vencimiento: 15/03/2026"
   
   // ❌ MAL: Mensaje genérico
   "Se cerró el resumen"
   ```

3. **Usar el tipo de notificación apropiado**
   ```java
   // Para notificaciones críticas que requieren acción
   TipoNotificacion.VENCIMIENTO_RESUMEN
   
   // Para notificaciones informativas
   TipoNotificacion.TRANSACCION_ALTA
   ```

4. **Notificar al usuario correcto**
   ```java
   // Admin del espacio de trabajo
   eventPublisher.publishEvent(new NotificacionEvent(
       this,
       espacio.getUsuarioAdmin().getId(),  // ← Usuario correcto
       tipo,
       mensaje
   ));
   ```

### ❌ DON'T (No hacer)

1. **No propagar excepciones de notificación**
   ```java
   // ❌ MAL: Si falla la notificación, falla toda la operación
   eventPublisher.publishEvent(new NotificacionEvent(...));
   
   // ✅ BIEN: La operación continúa aunque falle la notificación
   try {
       eventPublisher.publishEvent(new NotificacionEvent(...));
   } catch (Exception e) {
       logger.error("Error: {}", e.getMessage());
   }
   ```

2. **No publicar notificaciones irrelevantes**
   ```java
   // ❌ MAL: Notificación en cada búsqueda
   public List<Transaccion> buscarTransacciones(...) {
       eventPublisher.publishEvent(...);  // NO!
   }
   
   // ✅ BIEN: Solo en operaciones importantes
   public Transaccion crearTransaccion(...) {
       eventPublisher.publishEvent(...);  // OK
   }
   ```

3. **No incluir información sensible**
   ```java
   // ❌ MAL: Expone número completo de tarjeta
   String mensaje = "Tarjeta 1234567890123456 cerrada";
   
   // ✅ BIEN: Solo últimos 4 dígitos
   String mensaje = "Tarjeta terminada en 3456 cerrada";
   ```

---

## 📋 Tipos de Notificación Disponibles

```java
public enum TipoNotificacion {
    CIERRE_TARJETA,          // Cierre mensual de tarjeta
    VENCIMIENTO_RESUMEN,     // Resumen próximo a vencer
    INVITACION_ESPACIO,      // Invitación a workspace
    MIEMBRO_AGREGADO,        // Nuevo miembro en espacio
    SISTEMA                  // Notificaciones del sistema
}
```

### ¿Cuál usar?

- **CIERRE_TARJETA**: Scheduler cierra tarjeta automáticamente
- **VENCIMIENTO_RESUMEN**: Recordatorio antes de fecha límite
- **INVITACION_ESPACIO**: Usuario invitado a workspace
- **MIEMBRO_AGREGADO**: Nuevo miembro aceptó invitación
- **SISTEMA**: Mantenimiento, actualizaciones, etc.

---

## 🔍 Testing de Notificaciones

### Publicar Evento Manualmente (Testing)

```java
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestNotificacionController {
    
    private final ApplicationEventPublisher eventPublisher;
    
    @PostMapping("/notificacion")
    public ResponseEntity<String> testNotificacion(@RequestParam UUID userId) {
        eventPublisher.publishEvent(new NotificacionEvent(
            this,
            userId,
            TipoNotificacion.SISTEMA,
            "Esta es una notificación de prueba"
        ));
        
        return ResponseEntity.ok("Notificación enviada");
    }
}
```

### Verificar en Frontend

1. Abre DevTools Console
2. Verifica: "Notificación recibida en SSE"
3. Verifica: Toast aparece (si es tipo crítico)
4. Verifica: Badge se actualiza
5. Verifica: Aparece en NotificationBell

---

## 📚 Recursos Adicionales

- **Documentación Backend**: `docs/SistemaNotificaciones_PropuestaFinal.md`
- **Documentación Frontend**: `frontend/src/components/notifications/README.md`
- **Testing**: `frontend/src/components/notifications/TESTING.md`
- **Resumen**: `docs/SistemaNotificaciones_Resumen.md`

---

**Última actualización**: 2026-02-05  
**Versión**: 1.0.0

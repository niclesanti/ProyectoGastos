# Guía de Actualización de Tests: Float → BigDecimal

## Contexto
Como parte de la migración de Float a BigDecimal para valores monetarios, los tests deben actualizarse para usar BigDecimal en lugar de Float.

## Estado Actual
✅ **Código fuente refactorizado completamente** (compila sin errores)
⚠️ **Tests pendientes de actualización** (32 ocurrencias de Float literal en tests)

## Archivos de Tests Identificados
Los siguientes archivos requieren actualización:

1. **TransaccionServiceTest.java** - 20 ocurrencias
2. **DashboardServiceTest.java** - 12 ocurrencias  
3. **CuentaBancariaServiceTest.java** - Posibles ocurrencias
4. **CompraCreditoServiceTest.java** - Posibles ocurrencias
5. **EspacioTrabajoServiceTest.java** - Posibles ocurrencias

## Patrón de Reemplazo

### Antes (Float)
```java
espacioTrabajo.setSaldo(1000.0f);
cuentaBancaria.setSaldoActual(500.0f);
assertEquals(1100.0f, espacioTrabajo.getSaldo());

Transaccion transaccion = Transaccion.builder()
    .monto(100.0f)
    .build();
```

### Después (BigDecimal)
```java
espacioTrabajo.setSaldo(new BigDecimal("1000.00"));
cuentaBancaria.setSaldoActual(new BigDecimal("500.00"));
assertEquals(new BigDecimal("1100.00"), espacioTrabajo.getSaldo());

Transaccion transaccion = Transaccion.builder()
    .monto(new BigDecimal("100.00"))
    .build();
```

## Imports Requeridos
Agregar en cada archivo de test:
```java
import java.math.BigDecimal;
```

## Comparaciones en Assertions

### Comparación de Igualdad
```java
// ❌ INCORRECTO con BigDecimal
assertEquals(1000.0, cuenta.getSaldoActual());

// ✅ CORRECTO con BigDecimal
assertEquals(new BigDecimal("1000.00"), cuenta.getSaldoActual());

// ✅ ALTERNATIVA con compareTo
assertEquals(0, cuenta.getSaldoActual().compareTo(new BigDecimal("1000.00")));
```

### Comparaciones de Rango
```java
// Para validar si un valor es mayor/menor
assertTrue(saldo.compareTo(BigDecimal.ZERO) > 0); // saldo > 0
assertTrue(saldo.compareTo(monto) >= 0); // saldo >= monto
```

## Automatización Recomendada

### Opción 1: Reemplazo con Expresión Regular (IntelliJ/VSCode)
1. Buscar patrón: `(\d+)\.(\d+)f`
2. Reemplazar con: `new BigDecimal("$1.$2")`

### Opción 2: Script PowerShell (Windows)
```powershell
# Crear script de reemplazo
$testFiles = Get-ChildItem -Path "src\test\java" -Recurse -Filter "*Test.java"

foreach ($file in $testFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Reemplazar literales Float  
    $content = $content -replace '(\d+)\.(\d+)f\b', 'new BigDecimal("$1.$2")'
    
    # Guardar archivo
    Set-Content -Path $file.FullName -Value $content
}
```

### Opción 3: Script Bash/Linux
```bash
# Para cada archivo test
find src/test/java -name "*Test.java" -exec sed -i 's/\([0-9]\+\)\.\([0-9]\+\)f\b/new BigDecimal("\1.\2")/g' {} \;
```

## Casos Especiales

### Builder Patterns
```java
// Antes
CompraCredito compra = CompraCredito.builder()
    .montoTotal(1000.0f)
    .build();

// Después
CompraCredito compra = CompraCredito.builder()
    .montoTotal(new BigDecimal("1000.00"))
    .build();
```

### Mocks con when()
```java
// Antes
when(repository.calcularDeudaTotalPendiente(any())).thenReturn(500.0f);

// Después
when(repository.calcularDeudaTotalPendiente(any())).thenReturn(new BigDecimal("500.00"));
```

### GastosIngresosMensuales
```java
// Antes
GastosIngresosMensuales registro = GastosIngresosMensuales.builder()
    .gastos(0f)
    .ingresos(0f)
    .build();

// Después
GastosIngresosMensuales registro = GastosIngresosMensuales.builder()
    .gastos(BigDecimal.ZERO)
    .ingresos(BigDecimal.ZERO)
    .build();
```

## Validación Post-Actualización

### Ejecutar Tests
```bash
mvn test
```

### Verificar Compilación
```bash
mvn clean compile
```

### Buscar Remanentes
```bash
# Buscar Float literals que puedan haber quedado
grep -r "\d\+\.\d\+f" src/test/
```

## Próximos Pasos
1. ✅ Verificar que código fuente compila (completado)
2. ⏳ Actualizar tests sistemáticamente
3. ⏳ Ejecutar suite completa de tests
4. ⏳ Validar que todos los tests pasan
5. ⏳ Documentar cualquier caso especial encontrado

## Notas Importantes
- **Usar strings en constructores BigDecimal**: `new BigDecimal("100.50")` es más preciso que `BigDecimal.valueOf(100.50)`
- **Evitar operadores aritméticos**: Usar métodos `add()`, `subtract()`, `multiply()`, `divide()`
- **Comparaciones**: Siempre usar `compareTo()` en lugar de `==` o `equals()`
- **MoneyUtils**: Usar la clase utilitaria cuando sea apropiado para operaciones comunes

## Referencias
- [MoneyUtils.java](src/main/java/com/campito/backend/util/MoneyUtils.java) - Clase utilitaria creada
- [Migración V13](src/main/resources/db/migration/V13__convert_real_to_numeric.sql) - Migración de DB

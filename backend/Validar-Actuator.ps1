# Script de Validación - Spring Boot Actuator (Fase 1)
# Ejecutar desde: backend\
# Uso: .\Validar-Actuator.ps1

param(
    [string]$BaseUrl = "http://localhost:8080",
    [switch]$Detailed = $false
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Validación de Spring Boot Actuator" -ForegroundColor Cyan
Write-Host "  Fase 1: Observabilidad ProyectoGastos" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$ErrorActionPreference = "SilentlyContinue"
$testsPassed = 0
$testsFailed = 0

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$ExpectedPattern,
        [switch]$ShowResponse
    )
    
    Write-Host "[$Name]" -ForegroundColor Yellow -NoNewline
    Write-Host " Validando... " -NoNewline
    
    try {
        $response = Invoke-RestMethod -Uri $Url -TimeoutSec 5 -ErrorAction Stop
        
        if ($ExpectedPattern) {
            $responseJson = $response | ConvertTo-Json -Depth 5 -Compress
            if ($responseJson -match $ExpectedPattern) {
                Write-Host "✅ OK" -ForegroundColor Green
                $script:testsPassed++
                
                if ($ShowResponse -or $Detailed) {
                    Write-Host "   Respuesta: " -ForegroundColor Gray
                    $response | ConvertTo-Json -Depth 3 | Write-Host -ForegroundColor DarkGray
                }
                return $true
            } else {
                Write-Host "❌ FAIL - Pattern no encontrado" -ForegroundColor Red
                $script:testsFailed++
                return $false
            }
        } else {
            Write-Host "✅ OK" -ForegroundColor Green
            $script:testsPassed++
            
            if ($ShowResponse -or $Detailed) {
                Write-Host "   Respuesta: " -ForegroundColor Gray
                $response | ConvertTo-Json -Depth 3 | Write-Host -ForegroundColor DarkGray
            }
            return $true
        }
    }
    catch {
        Write-Host "❌ FAIL" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
        $script:testsFailed++
        return $false
    }
}

function Get-MetricValue {
    param(
        [string]$MetricName,
        [string]$Tag = $null
    )
    
    $url = "$BaseUrl/actuator/metrics/$MetricName"
    if ($Tag) {
        $url += "?tag=$Tag"
    }
    
    try {
        $response = Invoke-RestMethod -Uri $url -ErrorAction Stop
        $value = $response.measurements[0].value
        
        # Convertir bytes a MB si es una métrica de memoria
        if ($MetricName -like "*memory*" -and $value -gt 1MB) {
            $valueMB = [math]::Round($value / 1MB, 2)
            return "$valueMB MB"
        }
        
        return $value
    }
    catch {
        return "N/A"
    }
}

# ===== VALIDACIONES =====

Write-Host "`n🔍 Test 1: Conectividad Básica" -ForegroundColor Magenta
Write-Host "----------------------------------------" -ForegroundColor DarkGray

Test-Endpoint -Name "Actuator Root" -Url "$BaseUrl/actuator" -ExpectedPattern "health"

Write-Host "`n💚 Test 2: Health Check" -ForegroundColor Magenta
Write-Host "----------------------------------------" -ForegroundColor DarkGray

$healthOk = Test-Endpoint -Name "Health Status" -Url "$BaseUrl/actuator/health" -ExpectedPattern "UP"

if ($healthOk) {
    Write-Host "   ℹ️  La aplicación está saludable (status: UP)" -ForegroundColor Cyan
}

Write-Host "`n📊 Test 3: Métricas de JVM" -ForegroundColor Magenta
Write-Host "----------------------------------------" -ForegroundColor DarkGray

$memoryOk = Test-Endpoint -Name "Memoria JVM" -Url "$BaseUrl/actuator/metrics/jvm.memory.used" -ExpectedPattern "jvm.memory.used"

if ($memoryOk) {
    $heapUsed = Get-MetricValue -MetricName "jvm.memory.used" -Tag "area:heap"
    $heapMax = Get-MetricValue -MetricName "jvm.memory.max" -Tag "area:heap"
    
    Write-Host "   📈 Heap Used: $heapUsed" -ForegroundColor Cyan
    Write-Host "   📊 Heap Max: $heapMax" -ForegroundColor Cyan
}

Test-Endpoint -Name "CPU Usage" -Url "$BaseUrl/actuator/metrics/system.cpu.usage" -ExpectedPattern "system.cpu.usage"

Write-Host "`n🌐 Test 4: Métricas HTTP" -ForegroundColor Magenta
Write-Host "----------------------------------------" -ForegroundColor DarkGray

Test-Endpoint -Name "HTTP Requests" -Url "$BaseUrl/actuator/metrics/http.server.requests" -ExpectedPattern "http.server.requests"

Write-Host "`n🗄️ Test 5: Métricas de Base de Datos" -ForegroundColor Magenta
Write-Host "----------------------------------------" -ForegroundColor DarkGray

$hikariOk = Test-Endpoint -Name "HikariCP Active Connections" -Url "$BaseUrl/actuator/metrics/hikaricp.connections.active" -ExpectedPattern "hikaricp.connections.active"

if ($hikariOk) {
    $activeConns = Get-MetricValue -MetricName "hikaricp.connections.active"
    $maxConns = Get-MetricValue -MetricName "hikaricp.connections.max"
    
    Write-Host "   🔗 Conexiones Activas: $activeConns" -ForegroundColor Cyan
    Write-Host "   📊 Máximo Pool: $maxConns" -ForegroundColor Cyan
}

Write-Host "`n🎯 Test 6: Endpoint Prometheus" -ForegroundColor Magenta
Write-Host "----------------------------------------" -ForegroundColor DarkGray

try {
    $prometheusResponse = Invoke-RestMethod -Uri "$BaseUrl/actuator/prometheus" -ErrorAction Stop
    $lines = $prometheusResponse -split "`n"
    $metricsCount = ($lines | Where-Object { $_ -match "^[a-z]" }).Count
    
    Write-Host "[Prometheus Format] Validando... " -NoNewline
    Write-Host "✅ OK" -ForegroundColor Green
    Write-Host "   📊 $metricsCount métricas exportadas en formato Prometheus" -ForegroundColor Cyan
    $testsPassed++
    
    if ($Detailed) {
        Write-Host "`n   Ejemplo de métricas:" -ForegroundColor Gray
        $lines | Select-Object -First 20 | ForEach-Object {
            Write-Host "   $_" -ForegroundColor DarkGray
        }
    }
}
catch {
    Write-Host "[Prometheus Format] Validando... " -NoNewline
    Write-Host "❌ FAIL" -ForegroundColor Red
    $testsFailed++
}

Write-Host "`n📋 Test 7: Información de la Aplicación" -ForegroundColor Magenta
Write-Host "----------------------------------------" -ForegroundColor DarkGray

Test-Endpoint -Name "App Info" -Url "$BaseUrl/actuator/info" -ShowResponse

# ===== RESUMEN =====

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  RESUMEN DE VALIDACIÓN" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$totalTests = $testsPassed + $testsFailed
$successRate = if ($totalTests -gt 0) { [math]::Round(($testsPassed / $totalTests) * 100, 2) } else { 0 }

Write-Host "`nTests Ejecutados: $totalTests" -ForegroundColor White
Write-Host "✅ Exitosos: $testsPassed" -ForegroundColor Green
Write-Host "❌ Fallidos: $testsFailed" -ForegroundColor Red
Write-Host "📊 Tasa de Éxito: $successRate%" -ForegroundColor $(if ($successRate -eq 100) { "Green" } else { "Yellow" })

if ($testsFailed -eq 0) {
    Write-Host "`n🎉 ¡TODAS LAS VALIDACIONES PASARON!" -ForegroundColor Green
    Write-Host "✅ Spring Boot Actuator está correctamente configurado." -ForegroundColor Green
    Write-Host "`n📖 Próximos pasos:" -ForegroundColor Cyan
    Write-Host "   1. Revisar las métricas en detalle con: .\Validar-Actuator.ps1 -Detailed" -ForegroundColor Gray
    Write-Host "   2. Continuar con Fase 2: Configurar Grafana Cloud" -ForegroundColor Gray
    Write-Host "   3. Ver docs\Validacion_Actuator_Fase1.md para más información" -ForegroundColor Gray
    exit 0
} else {
    Write-Host "`n⚠️  ALGUNAS VALIDACIONES FALLARON" -ForegroundColor Yellow
    Write-Host "❌ Por favor revisa los errores arriba." -ForegroundColor Red
    Write-Host "`n🔧 Troubleshooting:" -ForegroundColor Cyan
    Write-Host "   1. Verifica que la aplicación esté corriendo: http://localhost:8080" -ForegroundColor Gray
    Write-Host "   2. Revisa los logs: mvn spring-boot:run" -ForegroundColor Gray
    Write-Host "   3. Consulta: docs\Validacion_Actuator_Fase1.md" -ForegroundColor Gray
    exit 1
}

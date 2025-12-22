Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   Proyecto Gastos - Docker Manager" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Selecciona una opcion:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Levantar todos los servicios (desarrollo)" -ForegroundColor White
Write-Host "2. Levantar todos los servicios (produccion)" -ForegroundColor White
Write-Host "3. Detener todos los servicios" -ForegroundColor White
Write-Host "4. Ver logs del frontend" -ForegroundColor White
Write-Host "5. Ver logs de todos los servicios" -ForegroundColor White
Write-Host "6. Reconstruir frontend" -ForegroundColor White
Write-Host "7. Limpiar todo (incluye volumenes)" -ForegroundColor White
Write-Host "8. Estado de los servicios" -ForegroundColor White
Write-Host "0. Salir" -ForegroundColor Gray
Write-Host ""

$choice = Read-Host "Ingresa tu opcion"

switch ($choice) {
    "1" {
        Write-Host "`nLevantando servicios en modo desarrollo..." -ForegroundColor Green
        docker-compose up -d
        Write-Host "`nServicios disponibles en:" -ForegroundColor Cyan
        Write-Host "  Frontend: http://localhost:3000" -ForegroundColor White
        Write-Host "  Backend:  http://localhost:8080" -ForegroundColor White
        Write-Host "  pgAdmin:  http://localhost:5050" -ForegroundColor White
    }
    "2" {
        Write-Host "`nLevantando servicios en modo produccion..." -ForegroundColor Green
        docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
        Write-Host "`nServicios disponibles en:" -ForegroundColor Cyan
        Write-Host "  Frontend: http://localhost" -ForegroundColor White
        Write-Host "  Backend:  http://localhost:8080" -ForegroundColor White
    }
    "3" {
        Write-Host "`nDeteniendo servicios..." -ForegroundColor Yellow
        docker-compose down
        Write-Host "Servicios detenidos" -ForegroundColor Green
    }
    "4" {
        Write-Host "`nMostrando logs del frontend (Ctrl+C para salir)..." -ForegroundColor Yellow
        docker-compose logs -f frontend
    }
    "5" {
        Write-Host "`nMostrando logs de todos los servicios (Ctrl+C para salir)..." -ForegroundColor Yellow
        docker-compose logs -f
    }
    "6" {
        Write-Host "`nReconstruyendo frontend..." -ForegroundColor Yellow
        docker-compose up -d --build frontend
        Write-Host "Frontend reconstruido" -ForegroundColor Green
    }
    "7" {
        Write-Host "`nLimpiando todo (esto eliminara los datos)..." -ForegroundColor Red
        $confirm = Read-Host "Estas seguro? (S/N)"
        if ($confirm -eq "S" -or $confirm -eq "s") {
            docker-compose down -v
            Write-Host "Todo limpio" -ForegroundColor Green
        } else {
            Write-Host "Operacion cancelada" -ForegroundColor Yellow
        }
    }
    "8" {
        Write-Host "`nEstado de los servicios:" -ForegroundColor Yellow
        docker-compose ps
    }
    "0" {
        Write-Host "`nHasta luego!" -ForegroundColor Cyan
        exit
    }
    default {
        Write-Host "`nOpcion invalida" -ForegroundColor Red
    }
}

Write-Host ""
Read-Host "Presiona Enter para continuar"

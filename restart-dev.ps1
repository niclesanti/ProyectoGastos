# Script para reiniciar los servicios de desarrollo

Write-Host "🔄 Reiniciando servicios de desarrollo..." -ForegroundColor Cyan

# Detener servicios
Write-Host "`n📦 Deteniendo contenedores..." -ForegroundColor Yellow
docker-compose down

# Reconstruir y levantar servicios
Write-Host "`n🏗️  Reconstruyendo y levantando servicios..." -ForegroundColor Yellow
docker-compose up -d --build

# Esperar a que los servicios estén listos
Write-Host "`n⏳ Esperando a que los servicios estén listos..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Mostrar estado de los servicios
Write-Host "`n📊 Estado de los servicios:" -ForegroundColor Green
docker-compose ps

# Mostrar URLs
Write-Host "`n✅ Servicios listos!" -ForegroundColor Green
Write-Host "`n📍 URLs disponibles:" -ForegroundColor Cyan
Write-Host "   Frontend:  http://localhost:3000" -ForegroundColor White
Write-Host "   Backend:   http://localhost:8080" -ForegroundColor White
Write-Host "   Swagger:   http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "   PgAdmin:   http://localhost:5050" -ForegroundColor White

Write-Host "`n💡 Para ver los logs:" -ForegroundColor Yellow
Write-Host "   docker-compose logs -f [servicio]" -ForegroundColor White
Write-Host "`n   Servicios disponibles: frontend, backend, db, pgadmin" -ForegroundColor Gray

@echo off
echo ========================================
echo  Proyecto Gastos - Frontend Setup
echo ========================================
echo.

echo [1/3] Instalando dependencias...
call npm install

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Fallo la instalacion de dependencias
    echo Por favor verifica tu conexion a internet y vuelve a intentar
    pause
    exit /b 1
)

echo.
echo [2/3] Verificando configuracion...
if exist ".env" (
    echo Archivo .env encontrado
) else (
    echo Creando archivo .env con valores por defecto...
    echo VITE_API_URL=http://localhost:8080/api > .env
)

echo.
echo [3/3] Setup completado exitosamente!
echo.
echo ========================================
echo  Comandos disponibles:
echo ========================================
echo  npm run dev      - Iniciar servidor de desarrollo
echo  npm run build    - Build para produccion
echo  npm run preview  - Preview del build
echo ========================================
echo.
echo Presiona cualquier tecla para iniciar el servidor de desarrollo...
pause > nul

echo.
echo Iniciando servidor de desarrollo en http://localhost:3000
echo Presiona Ctrl+C para detener el servidor
echo.
call npm run dev

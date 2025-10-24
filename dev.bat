@echo off
REM Foglio Development Helper Script for Windows

setlocal enabledelayedexpansion

set "command=%~1"
set "arg=%~2"

if "%command%"=="" (
    call :show_help
    exit /b 0
)

if "%command%"=="up" (
    call :docker_up
) else if "%command%"=="up-d" (
    call :docker_up_detached
) else if "%command%"=="down" (
    call :docker_down
) else if "%command%"=="down-v" (
    call :docker_down_volumes
) else if "%command%"=="logs" (
    call :docker_logs %arg%
) else if "%command%"=="ps" (
    call :docker_ps
) else if "%command%"=="restart" (
    call :docker_restart %arg%
) else if "%command%"=="dev:frontend" (
    call :dev_frontend
) else if "%command%"=="dev:backend" (
    call :dev_backend
) else if "%command%"=="build:frontend" (
    call :build_frontend
) else if "%command%"=="build:backend" (
    call :build_backend
) else if "%command%"=="build:all" (
    call :build_frontend
    call :build_backend
) else if "%command%"=="test:frontend" (
    call :test_frontend
) else if "%command%"=="test:backend" (
    call :test_backend
) else if "%command%"=="test:all" (
    call :test_frontend
    call :test_backend
) else if "%command%"=="clean" (
    call :clean_all
) else if "%command%"=="help" (
    call :show_help
) else if "%command%"=="--help" (
    call :show_help
) else if "%command%"=="-h" (
    call :show_help
) else (
    echo Unknown command: %command%
    echo.
    call :show_help
    exit /b 1
)

exit /b 0

:docker_up
echo ================================
echo   Starting Docker Services
echo ================================
docker-compose up --build
exit /b 0

:docker_up_detached
echo ================================
echo   Starting Docker Services
echo ================================
docker-compose up --build -d
echo [SUCCESS] Services started in background
echo.
echo [INFO] Check logs with: dev.bat logs
echo [INFO] Access the app at: http://localhost
exit /b 0

:docker_down
echo ================================
echo   Stopping Docker Services
echo ================================
docker-compose down
echo [SUCCESS] Services stopped
exit /b 0

:docker_down_volumes
echo ================================
echo   Stopping and Cleaning Up
echo ================================
docker-compose down -v
echo [SUCCESS] Services stopped and volumes removed
exit /b 0

:docker_logs
if "%~1"=="" (
    docker-compose logs -f
) else (
    docker-compose logs -f %~1
)
exit /b 0

:docker_ps
echo ================================
echo   Docker Services Status
echo ================================
docker-compose ps
exit /b 0

:docker_restart
docker-compose restart %~1
echo [SUCCESS] Services restarted
exit /b 0

:dev_frontend
echo ================================
echo   Starting Frontend Dev Server
echo ================================
cd foglio-fe
if not exist "node_modules" (
    echo [INFO] Installing dependencies...
    call npm install
)
if not exist ".env.local" (
    echo [INFO] Creating .env.local from example...
    copy .env.local.example .env.local
)
call npm run dev
cd ..
exit /b 0

:dev_backend
echo ================================
echo   Starting Backend Dev Server
echo ================================
cd foglio-be
call gradlew.bat bootRun
cd ..
exit /b 0

:build_frontend
echo ================================
echo   Building Frontend
echo ================================
cd foglio-fe
call npm install
call npm run build
cd ..
echo [SUCCESS] Frontend built successfully
exit /b 0

:build_backend
echo ================================
echo   Building Backend
echo ================================
cd foglio-be
call gradlew.bat clean build
cd ..
echo [SUCCESS] Backend built successfully
exit /b 0

:test_frontend
echo ================================
echo   Testing Frontend
echo ================================
cd foglio-fe
call npm run lint
cd ..
echo [SUCCESS] Frontend tests passed
exit /b 0

:test_backend
echo ================================
echo   Testing Backend
echo ================================
cd foglio-be
call gradlew.bat test
cd ..
echo [SUCCESS] Backend tests passed
exit /b 0

:clean_all
echo ================================
echo   Cleaning All Build Artifacts
echo ================================
echo [INFO] Cleaning frontend...
cd foglio-fe
if exist ".next" rmdir /s /q .next
if exist "out" rmdir /s /q out
if exist "node_modules" rmdir /s /q node_modules
cd ..

echo [INFO] Cleaning backend...
cd foglio-be
call gradlew.bat clean
cd ..

echo [SUCCESS] All artifacts cleaned
exit /b 0

:show_help
echo Foglio Development Helper Script for Windows
echo.
echo Usage: dev.bat [command]
echo.
echo Docker Commands:
echo   up                Start all services with Docker Compose
echo   up-d              Start all services in detached mode
echo   down              Stop all services
echo   down-v            Stop all services and remove volumes
echo   logs [service]    Show logs (optionally for specific service)
echo   ps                Show status of all services
echo   restart [service] Restart services
echo.
echo Development Commands:
echo   dev:frontend      Start frontend dev server (port 3000)
echo   dev:backend       Start backend dev server (port 8080)
echo.
echo Build Commands:
echo   build:frontend    Build frontend for production
echo   build:backend     Build backend JAR
echo   build:all         Build both frontend and backend
echo.
echo Test Commands:
echo   test:frontend     Run frontend tests
echo   test:backend      Run backend tests
echo   test:all          Run all tests
echo.
echo Maintenance Commands:
echo   clean             Remove all build artifacts
echo   help              Show this help message
echo.
echo Examples:
echo   dev.bat up                    # Start all services
echo   dev.bat logs backend          # Show backend logs
echo   dev.bat dev:frontend          # Start frontend dev server
echo   dev.bat test:all              # Run all tests
exit /b 0

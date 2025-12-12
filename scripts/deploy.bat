@echo off
setlocal enabledelayedexpansion

echo 🚀 Deploying ArtiCurated Order Management System...

REM Parse command line arguments
set ENVIRONMENT=%1
if "%ENVIRONMENT%"=="" set ENVIRONMENT=local

echo [DEPLOY] Deploying to environment: %ENVIRONMENT%

if "%ENVIRONMENT%"=="local" (
    echo [INFO] Starting local deployment...
    docker-compose -f docker/docker-compose.yml down
    docker-compose -f docker/docker-compose.yml up -d
) else if "%ENVIRONMENT%"=="dev" (
    echo [INFO] Starting development deployment...
    docker-compose -f docker/docker-compose.yml -f docker/docker-compose.dev.yml down
    docker-compose -f docker/docker-compose.yml -f docker/docker-compose.dev.yml up -d
) else if "%ENVIRONMENT%"=="prod" (
    echo [WARN] Starting production deployment...
    echo [WARN] Production deployment not yet configured
) else (
    echo [ERROR] Unknown environment: %ENVIRONMENT%
    echo Usage: %0 [local^|dev^|prod]
    exit /b 1
)

echo [INFO] Waiting for services to be ready...
timeout /t 30 /nobreak >nul

echo [INFO] Running health checks...

REM Check application health
curl -f http://localhost:8080/actuator/health >nul 2>nul
if %errorlevel% equ 0 (
    echo ✅ Application is healthy
) else (
    echo ❌ Application health check failed
    exit /b 1
)

REM Check database connectivity
docker exec articurated-db pg_isready -U articurated >nul 2>nul
if %errorlevel% equ 0 (
    echo ✅ Database is ready
) else (
    echo ❌ Database health check failed
    exit /b 1
)

REM Check RabbitMQ
curl -f http://localhost:15672 >nul 2>nul
if %errorlevel% equ 0 (
    echo ✅ RabbitMQ is ready
) else (
    echo ❌ RabbitMQ health check failed
    exit /b 1
)

echo [INFO] Deployment completed successfully! 🎉
echo.
echo [INFO] Service URLs:
echo   📱 Application: http://localhost:8080
echo   📚 API Docs: http://localhost:8080/swagger-ui.html
echo   🐰 RabbitMQ: http://localhost:15672 (admin/password)

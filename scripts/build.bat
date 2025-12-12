@echo off
setlocal enabledelayedexpansion

echo 🚀 Building ArtiCurated Order Management System...

REM Check if Maven is installed
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Maven is not installed. Please install Maven first.
    exit /b 1
)

REM Check if Docker is installed and running
where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not installed. Please install Docker first.
    exit /b 1
)

docker info >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not running. Please start Docker first.
    exit /b 1
)

echo [INFO] Cleaning previous builds...
call mvn clean

echo [INFO] Running tests...
call mvn test

echo [INFO] Generating test coverage report...
call mvn jacoco:report

echo [INFO] Building application...
call mvn package -DskipTests

echo [INFO] Building Docker image...
docker build -f docker/Dockerfile -t articurated/order-system:latest .

echo [INFO] Build completed successfully! ✅
echo [INFO] Docker image: articurated/order-system:latest
echo [INFO] Coverage report: target/site/jacoco/index.html

echo.
echo [INFO] Next steps:
echo   1. Run: docker-compose -f docker/docker-compose.yml up -d
echo   2. Access API: http://localhost:8080
echo   3. View Swagger UI: http://localhost:8080/swagger-ui.html

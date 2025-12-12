#!/bin/bash

# ArtiCurated Order System - Build Script
set -e

echo "🚀 Building ArtiCurated Order Management System..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if Docker is installed and running
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

if ! docker info &> /dev/null; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

print_status "Cleaning previous builds..."
mvn clean

print_status "Running tests..."
mvn test

print_status "Generating test coverage report..."
mvn jacoco:report

print_status "Building application..."
mvn package -DskipTests

print_status "Building Docker image..."
docker build -f docker/Dockerfile -t articurated/order-system:latest .

print_status "Build completed successfully! ✅"
print_status "Docker image: articurated/order-system:latest"
print_status "Coverage report: target/site/jacoco/index.html"

echo ""
print_status "Next steps:"
echo "  1. Run: docker-compose -f docker/docker-compose.yml up -d"
echo "  2. Access API: http://localhost:8080"
echo "  3. View Swagger UI: http://localhost:8080/swagger-ui.html"

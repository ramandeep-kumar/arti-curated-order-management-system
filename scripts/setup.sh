#!/bin/bash

# ArtiCurated Order System - Initial Setup Script
set -e

echo "🔧 Setting up ArtiCurated Order Management System..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[SETUP]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}[INIT]${NC} $1"
}

print_header "Initializing development environment..."

# Check prerequisites
print_status "Checking prerequisites..."

MISSING_TOOLS=()

if ! command -v java &> /dev/null; then
    MISSING_TOOLS+=("java")
fi

if ! command -v mvn &> /dev/null; then
    MISSING_TOOLS+=("maven")
fi

if ! command -v docker &> /dev/null; then
    MISSING_TOOLS+=("docker")
fi

if ! command -v docker-compose &> /dev/null; then
    MISSING_TOOLS+=("docker-compose")
fi

if [ ${#MISSING_TOOLS[@]} -ne 0 ]; then
    print_error "Missing required tools: ${MISSING_TOOLS[*]}"
    echo ""
    echo "Please install the missing tools:"
    echo "  • Java 17+: https://adoptium.net/"
    echo "  • Maven 3.9+: https://maven.apache.org/install.html"
    echo "  • Docker: https://docs.docker.com/get-docker/"
    echo "  • Docker Compose: https://docs.docker.com/compose/install/"
    exit 1
fi

print_status "✅ All prerequisites are installed"

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_VERSION" -lt 17 ]; then
    print_error "Java 17+ is required. Found Java $JAVA_VERSION"
    exit 1
fi

print_status "✅ Java version check passed (Java $JAVA_VERSION)"

# Create necessary directories
print_status "Creating project directories..."
mkdir -p logs
mkdir -p data/postgres
mkdir -p data/rabbitmq

# Make scripts executable
print_status "Setting up executable scripts..."
chmod +x scripts/*.sh

# Download dependencies
print_status "Downloading Maven dependencies..."
mvn dependency:go-offline -q

# Build the application
print_status "Building application..."
mvn clean compile -q

# Create environment file template
if [ ! -f ".env" ]; then
    print_status "Creating environment configuration..."
    cat > .env << EOF
# ArtiCurated Order System Configuration
# Copy this file and customize for your environment

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=articurated
DB_USERNAME=articurated
DB_PASSWORD=password

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=password

# Application
SPRING_PROFILES_ACTIVE=local
SERVER_PORT=8080
EOF
    
    print_status "✅ Environment configuration created (.env)"
fi

print_status "Setup completed successfully! 🎉"
echo ""
print_status "Next steps:"
echo "  1. Review and customize .env file if needed"
echo "  2. Run: ./scripts/build.sh"
echo "  3. Run: ./scripts/deploy.sh"
echo "  4. Access: http://localhost:8080"
echo ""
print_status "Available commands:"
echo "  • ./scripts/build.sh    - Build application and Docker image"
echo "  • ./scripts/deploy.sh   - Deploy application (local/dev/prod)"
echo "  • ./scripts/test.sh     - Run tests (unit/integration/coverage/all)"
echo "  • docker-compose -f docker/docker-compose.yml up -d  - Start all services"
echo "  • docker-compose -f docker/docker-compose.yml logs   - View application logs"

#!/bin/bash

# ArtiCurated Order System - Deployment Script
set -e

echo "🚀 Deploying ArtiCurated Order Management System..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}[DEPLOY]${NC} $1"
}

# Parse command line arguments
ENVIRONMENT=${1:-"local"}

print_header "Deploying to environment: $ENVIRONMENT"

case $ENVIRONMENT in
    "local")
        print_status "Starting local deployment..."
        docker-compose -f docker/docker-compose.yml down
        docker-compose -f docker/docker-compose.yml up -d
        ;;
    "dev")
        print_status "Starting development deployment..."
        docker-compose -f docker/docker-compose.yml -f docker/docker-compose.dev.yml down
        docker-compose -f docker/docker-compose.yml -f docker/docker-compose.dev.yml up -d
        ;;
    "prod")
        print_status "Starting production deployment..."
        # Add production deployment logic here
        print_warning "Production deployment not yet configured"
        ;;
    *)
        print_error "Unknown environment: $ENVIRONMENT"
        echo "Usage: $0 [local|dev|prod]"
        exit 1
        ;;
esac

# Wait for services to be ready
print_status "Waiting for services to be ready..."
sleep 30

# Health checks
print_status "Running health checks..."

# Check application health
if curl -f http://localhost:8080/actuator/health &> /dev/null; then
    print_status "✅ Application is healthy"
else
    print_error "❌ Application health check failed"
    exit 1
fi

# Check database connectivity
if docker exec articurated-db pg_isready -U articurated &> /dev/null; then
    print_status "✅ Database is ready"
else
    print_error "❌ Database health check failed"
    exit 1
fi

# Check RabbitMQ
if curl -f http://localhost:15672 &> /dev/null; then
    print_status "✅ RabbitMQ is ready"
else
    print_error "❌ RabbitMQ health check failed"
    exit 1
fi

print_status "Deployment completed successfully! 🎉"
echo ""
print_status "Service URLs:"
echo "  📱 Application: http://localhost:8080"
echo "  📚 API Docs: http://localhost:8080/swagger-ui.html"
echo "  🐰 RabbitMQ: http://localhost:15672 (admin/password)"

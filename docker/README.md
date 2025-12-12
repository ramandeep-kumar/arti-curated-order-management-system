# ArtiCurated Docker Setup

This directory contains the complete Docker configuration for the ArtiCurated Order Management System.

## 🐳 Services Overview

### Core Application
- **app**: Spring Boot application with multi-stage Docker build
- **db**: PostgreSQL 15 database with optimized settings
- **rabbitmq**: Message queue with management UI

### Monitoring
This repository no longer includes Prometheus and Grafana monitoring containers.

## 🚀 Quick Start

### 1. Build and Start All Services
```bash
# From the project root directory
docker-compose -f docker/docker-compose.yml up -d
```

### 2. Access Services
- **Application API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **RabbitMQ Management**: http://localhost:15672 (admin/password)
<!-- Grafana and Prometheus removed -->

### 3. Health Checks
```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check all services
docker-compose -f docker/docker-compose.yml ps
```

## 📁 Configuration Files

### Dockerfile
- Multi-stage build for optimal image size
- Security: non-root user execution
- Health checks and JVM optimization
- Container-ready JVM settings

### docker-compose.yml
- Complete service orchestration
- Health checks and dependencies
- Persistent volumes for data
- Custom network configuration

### Database
- `postgres/init.sql`: Database initialization
- Optimized PostgreSQL settings
- Connection pooling configuration

### RabbitMQ
- `rabbitmq/rabbitmq.conf`: Message queue settings
- Memory and disk thresholds
- Management interface enabled

<!-- Monitoring assets removed -->

## 🔧 Customization

### Environment Variables
```bash
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
```

### Ports
- **8080**: Application API
- **5432**: PostgreSQL
- **5672**: RabbitMQ AMQP
- **15672**: RabbitMQ Management

## 📊 Monitoring

### Metrics Collected
- Application health and performance
- HTTP request rates and latencies
- Database connection metrics
- RabbitMQ queue statistics

### Dashboards
- Application health overview
- Request rate monitoring
- Database performance
- Message queue status

## 🛠️ Development

### Build Application
```bash
# Build Docker image
docker build -f docker/Dockerfile -t articurated/order-system:latest .

# Run with docker-compose
docker-compose -f docker/docker-compose.yml up -d
```

### Logs
```bash
# View all logs
docker-compose -f docker/docker-compose.yml logs -f

# View specific service logs
docker-compose -f docker/docker-compose.yml logs -f app
```

### Cleanup
```bash
# Stop and remove containers
docker-compose -f docker/docker-compose.yml down

# Remove volumes (WARNING: data loss)
docker-compose -f docker/docker-compose.yml down -v
```

## 🔒 Security Features

- Non-root container execution
- Custom network isolation
- Health check validation
- Secure default configurations

## 📈 Performance

- Optimized JVM settings for containers
- Database connection pooling
- Message queue optimization
- Efficient resource utilization

## 🆘 Troubleshooting

### Common Issues
1. **Port conflicts**: Change ports in docker-compose.yml
2. **Memory issues**: Increase Docker memory allocation
3. **Database connection**: Check PostgreSQL health status
4. **RabbitMQ**: Verify management interface accessibility

### Debug Commands
```bash
# Check service status
docker-compose -f docker/docker-compose.yml ps

# View service logs
docker-compose -f docker/docker-compose.yml logs [service-name]

# Access service shell
docker exec -it [container-name] /bin/sh
```

## 📚 Additional Resources

- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [PostgreSQL Docker](https://hub.docker.com/_/postgres)
- [RabbitMQ Docker](https://hub.docker.com/_/rabbitmq)
<!-- Monitoring links removed -->

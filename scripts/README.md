# ArtiCurated Scripts

This folder contains automation scripts for building, testing, deploying, and setting up the ArtiCurated Order Management System.

## 📁 Scripts Overview

### **Build Scripts**
- **`build.sh`** / **`build.bat`** - Build application and Docker image
- **`deploy.sh`** / **`deploy.bat`** - Deploy application to different environments
- **`test.sh`** / **`test.bat`** - Run various test suites
- **`setup.sh`** - Initial environment setup and validation

## 🚀 Quick Start

### **1. Initial Setup**
```bash
# Linux/macOS
./scripts/setup.sh

# Windows
scripts\setup.bat
```

### **2. Build Application**
```bash
# Linux/macOS
./scripts/build.sh

# Windows
scripts\build.bat
```

### **3. Deploy Services**
```bash
# Linux/macOS
./scripts/deploy.sh [local|dev|prod]

# Windows
scripts\deploy.bat [local|dev|prod]
```

### **4. Run Tests**
```bash
# Linux/macOS
./scripts/test.sh [unit|integration|coverage|all]

# Windows
scripts\test.bat [unit|integration|coverage|all]
```

## 🔧 Script Details

### **build.sh / build.bat**
**Purpose**: Complete application build process
**Features**:
- Maven clean and compile
- Run all tests
- Generate coverage reports
- Build Docker image
- Prerequisite validation

**Usage**:
```bash
./scripts/build.sh
scripts\build.bat
```

### **deploy.sh / deploy.bat**
**Purpose**: Deploy application to different environments
**Environments**:
- `local` - Local development (default)
- `dev` - Development environment
- `prod` - Production environment

**Features**:
- Service orchestration
- Health checks
- Environment-specific configuration
- Service status validation

**Usage**:
```bash
./scripts/deploy.sh local
./scripts/deploy.sh dev
./scripts/deploy.sh prod

scripts\deploy.bat local
scripts\deploy.bat dev
scripts\deploy.bat prod
```

### **test.sh / test.bat**
**Purpose**: Run various test suites
**Test Types**:
- `unit` - Unit tests only
- `integration` - Integration tests only
- `coverage` - Tests with coverage report
- `all` - All tests with coverage (default)

**Features**:
- Test result summary
- Coverage reporting
- Flexible test execution
- Error handling

**Usage**:
```bash
./scripts/test.sh unit
./scripts/test.sh integration
./scripts/test.sh coverage
./scripts/test.sh all

scripts\test.bat unit
scripts\test.bat integration
scripts\test.bat coverage
scripts\test.bat all
```

### **setup.sh**
**Purpose**: Initial environment setup and validation
**Features**:
- Prerequisite checking (Java, Maven, Docker)
- Java version validation
- Directory creation
- Environment file generation
- Dependency download

**Usage**:
```bash
./scripts/setup.sh
```

## 🌍 Cross-Platform Support

### **Linux/macOS**
- Use `.sh` scripts
- Ensure scripts are executable: `chmod +x scripts/*.sh`
- Run with: `./scripts/script-name.sh`

### **Windows**
- Use `.bat` scripts
- Run with: `scripts\script-name.bat`
- Or double-click the `.bat` files

## 📋 Prerequisites

### **Required Tools**
- **Java 17+** - Runtime environment
- **Maven 3.9+** - Build tool
- **Docker** - Containerization
- **Docker Compose** - Service orchestration

### **Installation Links**
- **Java**: https://adoptium.net/
- **Maven**: https://maven.apache.org/install.html
- **Docker**: https://docs.docker.com/get-docker/
- **Docker Compose**: https://docs.docker.com/compose/install/

## 🔍 Troubleshooting

### **Common Issues**

#### **Permission Denied (Linux/macOS)**
```bash
chmod +x scripts/*.sh
```

#### **Script Not Found (Windows)**
- Ensure you're in the project root directory
- Use correct path separators: `scripts\script-name.bat`

#### **Maven Not Found**
- Verify Maven is installed: `mvn --version`
- Check PATH environment variable

#### **Docker Not Running**
- Start Docker Desktop
- Verify with: `docker info`

### **Debug Commands**
```bash
# Check script permissions
ls -la scripts/

# Verify Maven installation
mvn --version

# Check Docker status
docker info

# View script contents
cat scripts/build.sh
```

## 📚 Additional Resources

- **Project README**: ../README.md
- **Docker Setup**: ../docker/README.md
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Postman Collection**: ../docs/POSTMAN_COLLECTION.json

## 🎯 Script Workflow

```
setup.sh → build.sh → test.sh → deploy.sh
    ↓           ↓        ↓        ↓
Environment  Compile  Validate  Deploy
Validation   Package  Quality  Services
```

## 🆘 Support

For script-related issues:
1. Check prerequisites are installed
2. Verify you're in the project root directory
3. Check script permissions (Linux/macOS)
4. Review error messages for specific issues
5. Consult the troubleshooting section above

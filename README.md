# ArtiCurated - Domain-Driven Design Backend

A robust backend system for **ArtiCurated**, a boutique online marketplace for high-value artisanal goods. Built using **Domain-Driven Design (DDD)** principles with Spring Boot, this project manages the complete order lifecycle with enterprise-grade architecture.

## 🚀 Key Features

- **Domain-Driven Design Architecture** - Clean separation of business domains
- **Order State Management** - Granular tracking from placement to closure
- **Returns & Refunds Workflow** - Multi-step manual approvals with audit trails
- **Asynchronous Processing** - Background job processing for notifications and updates
- **State Machine Workflows** - Order and return lifecycle management
- **Docker Ready** - Containerized deployment with PostgreSQL

## 🏗️ Project Structure

```
ArtiCurated/
├── 📁 src/main/java/com/articurated/
│   ├── 🚀 ArtiCuratedApplication.java      # ✅ Main application class
│   ├── 📁 shared/                           # Cross-cutting concerns
│   │   ├── config/                          # 📁 Package structure (empty)
│   │   ├── exception/                       # 📁 Package structure (empty)
│   │   └── events/                          # 📁 Package structure (empty)
│   ├── 📁 order/                            # Order Domain (Bounded Context)
│   │   ├── domain/                          # 📁 Package structure (empty)
│   │   ├── repository/                      # 📁 Package structure (empty)
│   │   ├── service/                         # 📁 Package structure (empty)
│   │   ├── controller/                      # 📁 Package structure (empty)
│   │   ├── dto/                             # 📁 Package structure (empty)
│   │   └── statemachine/                    # 📁 Package structure (empty)
│   ├── 📁 returns/                          # Returns Domain (Bounded Context)
│   │   ├── domain/                          # 📁 Package structure (empty)
│   │   ├── service/                         # 📁 Package structure (empty)
│   │   ├── controller/                      # 📁 Package structure (empty)
│   │   └── statemachine/                    # 📁 Package structure (empty)
│   └── 📁 messaging/                        # Asynchronous Processing
│       ├── producer/                        # 📁 Package structure (empty)
│       └── consumer/                        # 📁 Package structure (empty)
├── 📁 src/main/resources/
│   ├── application.yml                      # ✅ Configuration (configured)
│   ├── static/                              # 📁 Static resources (empty)
│   ├── templates/                           # 📁 Template files (empty)
│   └── db/migration/                        # 📁 Database migrations (empty)
├── 📁 src/test/java/                        # Test structure
│   ├── unit/                                # 📁 Unit tests (empty)
│   ├── integration/                         # 📁 Integration tests (empty)
│   └── com/articurated/                     # 📁 Test package structure
# ArtiCurated — Backend (Domain-Driven Design)

A backend service for ArtiCurated, a boutique marketplace for curated artisanal goods. This repository provides a DDD-styled Spring Boot application that manages orders, returns, and related asynchronous workflows.

## What this README does
- Summarizes the project and tech stack
- Gives quick, platform-friendly commands to build, run, and test
- Documents configuration, API endpoints, and how to contribute

## Quick checklist (what I changed)
- Modernized intro and condensed status
- Added Windows PowerShell-friendly commands
- Clarified Docker and test instructions
- Added contribution and contact notes

## Tech summary
- Spring Boot 3.x, Java 17
- PostgreSQL, Docker, Maven
- Spring Data JPA, Spring Security (configured as project requires), Spring State Machine

## Quick start (Windows PowerShell and Linux/macOS)

This section walks through building the project, provisioning the database & RabbitMQ with Docker Compose, running the application, and running background workers.

1) Prerequisites

- Java 17+
- Maven (or use the included Maven wrapper)
- Docker & Docker Compose (recommended for local dev)

2) Build the project

PowerShell (Windows):

```powershell
# Build the artifact (skip tests to iterate faster)
.\mvnw.cmd clean package -DskipTests
```

Unix / macOS:

```bash
./mvnw clean package -DskipTests
```

3) Start Postgres + RabbitMQ using Docker Compose (recommended)

The repo includes `docker/docker-compose.yml` which defines `db` (Postgres) and `rabbitmq` services. Start them before running the application.

PowerShell:

```powershell
cd docker
docker-compose up -d db rabbitmq
```

Unix / macOS:

```bash
cd docker && docker-compose up -d db rabbitmq
```

Notes:
- Postgres will listen on host port 5432 and RabbitMQ on 5672 (management UI on 15672). The compose file also starts `pgadmin` (8081) if you need a GUI.
- Compose uses environment variables defined inside the file; change credentials by editing `docker/docker-compose.yml` or add an `.env` referenced by the compose file.

4) Database migrations

Flyway migrations are applied automatically on application startup (default Spring Boot + Flyway behavior). If you want to run migrations manually:

PowerShell:

```powershell
.\mvnw.cmd -Dflyway.configFiles=src/main/resources/flyway.conf flyway:migrate
```

Unix:

```bash
./mvnw flyway:migrate
```

5) Run the application (API + embedded background workers)

By default the Spring Boot application hosts the REST API and also runs the asynchronous background components (event handlers, RabbitMQ listeners) in the same process. Running the app will therefore start both the HTTP endpoints and the message consumers.

PowerShell:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=docker
```

Unix:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```

Or run the packaged jar (recommended for parity with production):

PowerShell:

```powershell
java -Dspring.profiles.active=docker -jar target\articurated-order-system-1.0.0.jar
```

Unix:

```bash
java -Dspring.profiles.active=docker -jar target/articurated-order-system-1.0.0.jar
```

6) Run background workers separately (worker-only mode)

If you want to run consumers / background workers in a dedicated process (separate from the API), start the jar with the web environment disabled. This is useful to scale workers independently or to run only message consumers in CI.

PowerShell example (worker-only):

```powershell
java -Dspring.profiles.active=docker -Dspring.main.web-application-type=none -jar target\articurated-order-system-1.0.0.jar
```

Unix example (worker-only):

```bash
java -Dspring.profiles.active=docker -Dspring.main.web-application-type=none -jar target/articurated-order-system-1.0.0.jar
```

Notes:
- `-Dspring.main.web-application-type=none` prevents the embedded servlet container from starting; Spring components (including `@RabbitListener` consumers and `@Async` event handlers) remain active.
- You can run one API instance and N worker-only instances to scale processing.

7) Verify the system is healthy

Check application health endpoint:

PowerShell:

```powershell
curl http://localhost:8080/actuator/health
```

Check RabbitMQ management UI (default): http://localhost:15672 (user/password per compose: admin/password)

Check Postgres (pgAdmin UI default): http://localhost:8081 (admin@articurated.com / admin)

8) Logs & troubleshooting

- Tail Docker compose logs:

```powershell
cd docker; docker-compose logs -f app
```

Or (Unix):

```bash
cd docker && docker-compose logs -f app
```

- If the app fails to start because the DB is unavailable, wait until Postgres healthcheck passes or restart the app after DB is ready. The compose healthchecks defined in `docker/docker-compose.yml` help with this.
- Ensure Flyway migrations applied successfully (check application logs for Flyway output or `target/failsafe-reports`).

## Important configuration notes

- Default DB connection and other configuration live in `src/main/resources/application.yml` (check it before first run).
- Example default database properties (adjust in your environment):

	- database: `articurated`
	- username: `articurated_user`
	- password: `articurated_password`

- When running with Docker Compose the service is wired to the `postgres` service defined in `docker/docker-compose.yml`.

## API (short list)

- GET  /actuator/health — health
- GET  /actuator/info — application info
- POST /api/orders — create order (domain-specific payload)
- POST /api/returns — create return (domain-specific payload)

Note: Check controller tests and `src/main/java/.../controller` for exact request/response shapes.

## Testing

Run unit & integration tests:

PowerShell:

```powershell
.\mvnw.cmd test
```

Build without tests (useful for quick image builds):

```powershell
.\mvnw.cmd package -DskipTests
```

There are integration test runs and reports under `target/failsafe-reports` and `target/surefire-reports` after CI or local runs.

## Logging and MDC

Messaging consumers populate MDC keys `correlationId` and `messageTimestamp` for cross-service correlation. Example pattern to include MDC fields in logs (Logback/application.yml):

```yaml
logging:
	pattern:
		console: "%d{yyyy-MM-dd HH:mm:ss} %-5level [%X{correlationId}] %logger{36} - %msg%n"
```

Include `%X{messageTimestamp}` if you want the message timestamp in logs as well.

## Docker

Build the project image (local):

```powershell
docker build -t articurated-backend .
```

Bring up services (Postgres + app as needed):

```powershell
cd docker; docker-compose up -d
```

Stop and remove:

```powershell
cd docker; docker-compose down
```

## Project status (short)

- Project scaffolding, DDD package structure, configuration, and Docker wiring: Done
- Core domain implementations, service logic, and endpoint wiring: Partial / work in progress (refer to package folders under `src/main/java`) 

## Contributing

- Want to help? Open an issue with a short proposal and link code changes to a branch.
- Follow existing package structure: separate domain, application (app service), infrastructure (repositories), and presentation (controllers).
- Add unit tests for new logic and integration tests for end-to-end flows.

## Where to look next

- `src/main/java/com/articurated` — main application packages and TODO implementations
- `docker/` — docker-compose and Postgres init scripts
- `docs/` — additional documentation and test reports

---

Built with DDD principles and Spring Boot.

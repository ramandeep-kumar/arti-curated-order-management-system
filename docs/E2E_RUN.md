# Running E2E Messaging Tests Locally

This repository includes end-to-end integration tests that exercise RabbitMQ-based background jobs (invoice PDF generation and refund processing).

Prerequisites
- Docker Desktop (running)
- Maven (bundled wrapper `mvnw.cmd` is used on Windows)

Quick run (recommended)
1. Start RabbitMQ via docker-compose (from repo root):

```powershell
cd .\docker
docker-compose up -d rabbitmq
```

2. From repo root run the integration tests (failsafe):

```powershell
# run only integration tests (runs Phase3E2EMessagingTest and other *IntegrationTest.java)
.\mvnw.cmd verify -DskipTests=false
```

Notes
- Unit tests (`*Test.java`) are executed during `mvn test` / `mvn verify` via Surefire. Integration tests (`*IntegrationTest.java` and `*IT.java`) run during the Failsafe phase invoked by `mvn verify`.
- The `Phase3E2EMessagingTest` uses Testcontainers and will start a RabbitMQ container automatically if you don't start one yourself.
- The invoice PDF file is written to the temp dir configured via `invoices.output.dir` (tests set a temp dir). The E2E test will clean up generated files on exit.

Troubleshooting
- If a test fails with a RabbitMQ exchange/queue NOT_FOUND error, ensure either the docker-compose RabbitMQ is running or allow the test's RabbitMQ container to start by itself (Testcontainers).
- On Windows PowerShell, quote `-Dtest` values correctly when running selective tests: `"-Dtest=MyIntegrationTest"`.

If you want, I can add a small script that runs only messaging-related integration tests in sequence and collects artifacts into `target/e2e`.

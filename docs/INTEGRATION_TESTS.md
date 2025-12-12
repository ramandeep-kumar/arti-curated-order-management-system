## Running isolated integration tests (DLQ) in CI

This project keeps fast unit tests as the default `mvn test` target and runs targeted integration tests using the `integration-tests` Maven profile.

Recommended CI (GitHub Actions) job snippet to run only the isolated DLQ integration test:

```yaml
name: Integration Tests (DLQ)

on:
  workflow_dispatch:
  push:
    branches: [ main, feature/* ]

jobs:
  integration-dlq:
    runs-on: ubuntu-latest
    services:
      rabbitmq:
        image: rabbitmq:3.11-management
        ports:
          - 5672:5672
          - 15672:15672
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Build and run DLQ integration test
        run: |
          ./mvnw -B -Pintegration-tests verify
```

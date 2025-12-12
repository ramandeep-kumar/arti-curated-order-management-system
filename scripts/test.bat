@echo off
setlocal enabledelayedexpansion

echo 🧪 Running ArtiCurated Order Management System Tests...

REM Parse command line arguments
set TEST_TYPE=%1
if "%TEST_TYPE%"=="" set TEST_TYPE=all

echo [TESTING] Running test suite: %TEST_TYPE%

if "%TEST_TYPE%"=="unit" (
    echo [TEST] Running unit tests...
    call mvn test -Dtest="**/*Test"
) else if "%TEST_TYPE%"=="integration" (
    echo [TEST] Running integration tests...
    call mvn test -P integration-tests
) else if "%TEST_TYPE%"=="coverage" (
    echo [TEST] Running tests with coverage...
    call mvn clean test jacoco:report
    echo.
    echo [TEST] Coverage report generated: target/site/jacoco/index.html
) else if "%TEST_TYPE%"=="all" (
    echo [TEST] Running all tests...
    call mvn clean test -P integration-tests jacoco:report
    echo.
    echo [TEST] All tests completed!
    echo [TEST] Coverage report: target/site/jacoco/index.html
) else (
    echo [ERROR] Unknown test type: %TEST_TYPE%
    echo Usage: %0 [unit^|integration^|coverage^|all]
    exit /b 1
)

echo.
echo [TEST] Test execution completed!
echo [TEST] Check the output above for results.

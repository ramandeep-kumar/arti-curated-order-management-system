#!/bin/bash

# ArtiCurated Order System - Test Script
set -e

echo "🧪 Running ArtiCurated Order Management System Tests..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[TEST]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}[TESTING]${NC} $1"
}

# Parse command line arguments
TEST_TYPE=${1:-"all"}

print_header "Running test suite: $TEST_TYPE"

case $TEST_TYPE in
    "unit")
        print_status "Running unit tests..."
        mvn test -Dtest="**/*Test"
        ;;
    "integration")
        print_status "Running integration tests..."
        mvn test -P integration-tests
        ;;
    "coverage")
        print_status "Running tests with coverage..."
        mvn clean test jacoco:report
        echo ""
        print_status "Coverage report generated: target/site/jacoco/index.html"
        ;;
    "all")
        print_status "Running all tests..."
        mvn clean test -P integration-tests jacoco:report
        echo ""
        print_status "All tests completed!"
        print_status "Coverage report: target/site/jacoco/index.html"
        ;;
    *)
        print_error "Unknown test type: $TEST_TYPE"
        echo "Usage: $0 [unit|integration|coverage|all]"
        exit 1
        ;;
esac

# Display test results summary
if [ -f "target/surefire-reports/TEST-*.xml" ]; then
    TOTAL_TESTS=$(grep -h 'testsuite.*tests=' target/surefire-reports/TEST-*.xml | sed 's/.*tests="\([0-9]*\)".*/\1/' | awk '{sum+=$1} END {print sum}')
    FAILED_TESTS=$(grep -h 'testsuite.*failures=' target/surefire-reports/TEST-*.xml | sed 's/.*failures="\([0-9]*\)".*/\1/' | awk '{sum+=$1} END {print sum}')
    
    echo ""
    print_status "Test Results Summary:"
    echo "  📊 Total Tests: $TOTAL_TESTS"
    echo "  ✅ Passed: $((TOTAL_TESTS - FAILED_TESTS))"
    echo "  ❌ Failed: $FAILED_TESTS"
    
    if [ "$FAILED_TESTS" -eq 0 ]; then
        print_status "All tests passed! 🎉"
    else
        print_error "Some tests failed! Check the reports for details."
        exit 1
    fi
fi

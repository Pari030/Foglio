#!/bin/bash

# Foglio Development Helper Script

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}  Foglio - $1${NC}"
    echo -e "${BLUE}================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Docker commands
docker_up() {
    print_header "Starting Docker Services"
    docker-compose up --build
}

docker_up_detached() {
    print_header "Starting Docker Services (Detached)"
    docker-compose up --build -d
    print_success "Services started in background"
    echo ""
    print_info "Check logs with: ./dev.sh logs"
    print_info "Access the app at: http://localhost"
}

docker_down() {
    print_header "Stopping Docker Services"
    docker-compose down
    print_success "Services stopped"
}

docker_down_volumes() {
    print_header "Stopping Docker Services and Removing Volumes"
    docker-compose down -v
    print_success "Services stopped and volumes removed"
}

docker_logs() {
    print_header "Docker Logs"
    if [ -z "$1" ]; then
        docker-compose logs -f
    else
        docker-compose logs -f "$1"
    fi
}

docker_ps() {
    print_header "Docker Services Status"
    docker-compose ps
}

docker_restart() {
    print_header "Restarting Docker Services"
    docker-compose restart "$1"
    print_success "Services restarted"
}

# Development commands
dev_frontend() {
    print_header "Starting Frontend Dev Server"
    cd foglio-fe
    if [ ! -d "node_modules" ]; then
        print_info "Installing dependencies..."
        npm install
    fi
    if [ ! -f ".env.local" ]; then
        print_info "Creating .env.local from example..."
        cp .env.local.example .env.local
    fi
    npm run dev
}

dev_backend() {
    print_header "Starting Backend Dev Server"
    cd foglio-be
    if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
        ./gradlew.bat bootRun
    else
        ./gradlew bootRun
    fi
}

# Build commands
build_frontend() {
    print_header "Building Frontend"
    cd foglio-fe
    npm install
    npm run build
    print_success "Frontend built successfully"
}

build_backend() {
    print_header "Building Backend"
    cd foglio-be
    if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
        ./gradlew.bat clean build
    else
        ./gradlew clean build
    fi
    print_success "Backend built successfully"
}

# Test commands
test_frontend() {
    print_header "Testing Frontend"
    cd foglio-fe
    npm run lint
    print_success "Frontend tests passed"
}

test_backend() {
    print_header "Testing Backend"
    cd foglio-be
    if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
        ./gradlew.bat test
    else
        ./gradlew test
    fi
    print_success "Backend tests passed"
}

# Clean commands
clean_all() {
    print_header "Cleaning All Build Artifacts"
    
    print_info "Cleaning frontend..."
    cd foglio-fe
    rm -rf .next out node_modules
    cd ..
    
    print_info "Cleaning backend..."
    cd foglio-be
    if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
        ./gradlew.bat clean
    else
        ./gradlew clean
    fi
    cd ..
    
    print_success "All artifacts cleaned"
}

# Help command
show_help() {
    cat << EOF
Foglio Development Helper Script

Usage: ./dev.sh [command]

Docker Commands:
  up                Start all services with Docker Compose (attached)
  up-d              Start all services in detached mode
  down              Stop all services
  down-v            Stop all services and remove volumes
  logs [service]    Show logs (optionally for specific service)
  ps                Show status of all services
  restart [service] Restart services (optionally specific service)

Development Commands:
  dev:frontend      Start frontend dev server (port 3000)
  dev:backend       Start backend dev server (port 8080)

Build Commands:
  build:frontend    Build frontend for production
  build:backend     Build backend JAR
  build:all         Build both frontend and backend

Test Commands:
  test:frontend     Run frontend tests
  test:backend      Run backend tests
  test:all          Run all tests

Maintenance Commands:
  clean             Remove all build artifacts
  help              Show this help message

Examples:
  ./dev.sh up                    # Start all services
  ./dev.sh logs backend          # Show backend logs
  ./dev.sh dev:frontend          # Start frontend dev server
  ./dev.sh test:all              # Run all tests

EOF
}

# Main script logic
case "$1" in
    up)
        docker_up
        ;;
    up-d)
        docker_up_detached
        ;;
    down)
        docker_down
        ;;
    down-v)
        docker_down_volumes
        ;;
    logs)
        docker_logs "$2"
        ;;
    ps)
        docker_ps
        ;;
    restart)
        docker_restart "$2"
        ;;
    dev:frontend)
        dev_frontend
        ;;
    dev:backend)
        dev_backend
        ;;
    build:frontend)
        build_frontend
        ;;
    build:backend)
        build_backend
        ;;
    build:all)
        build_frontend
        build_backend
        ;;
    test:frontend)
        test_frontend
        ;;
    test:backend)
        test_backend
        ;;
    test:all)
        test_frontend
        test_backend
        ;;
    clean)
        clean_all
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac

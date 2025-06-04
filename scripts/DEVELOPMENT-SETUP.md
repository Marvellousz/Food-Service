# Development Environment Setup

This document provides automated setup scripts and instructions for quickly setting up a complete development environment for the Food Service application.

## Table of Contents

- [Quick Start](#quick-start)
- [Prerequisites](#prerequisites)
- [Automated Setup Scripts](#automated-setup-scripts)
- [Manual Setup Instructions](#manual-setup-instructions)
- [IDE Configuration](#ide-configuration)
- [Docker Development Environment](#docker-development-environment)
- [Local Kubernetes Setup](#local-kubernetes-setup)
- [Testing Environment](#testing-environment)
- [Troubleshooting](#troubleshooting)

## Quick Start

For a complete automated setup, run:

```bash
curl -fsSL https://raw.githubusercontent.com/your-org/food-service/main/scripts/setup-dev.sh | bash
```

Or clone the repository and run:

```bash
git clone https://github.com/your-org/food-service.git
cd food-service
./scripts/setup-dev.sh
```

## Prerequisites

### System Requirements

- **Operating System:** macOS 10.15+, Ubuntu 20.04+, or Windows 10+ with WSL2
- **Memory:** 8GB RAM minimum, 16GB recommended
- **Storage:** 10GB free space
- **Network:** Internet connection for downloading dependencies

### Required Tools

The setup script will install these automatically, but you can install them manually:

- Git 2.30+
- Java 17 or 21
- Maven 3.9+
- Docker 20.10+
- kubectl 1.25+
- curl and jq

## Automated Setup Scripts

### Main Setup Script

Create `scripts/setup-dev.sh`:

```bash
#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Detect OS
detect_os() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        OS="macos"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        OS="linux"
    elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
        OS="windows"
    else
        log_error "Unsupported operating system: $OSTYPE"
        exit 1
    fi
    log_info "Detected OS: $OS"
}

# Check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Install Java
install_java() {
    log_info "Installing Java..."
    
    if command_exists java; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
        if [[ "$JAVA_VERSION" -ge 17 ]]; then
            log_success "Java $JAVA_VERSION is already installed"
            return
        fi
    fi
    
    case $OS in
        macos)
            if command_exists brew; then
                brew install openjdk@21
                echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
            else
                log_error "Homebrew not found. Please install Homebrew first."
                exit 1
            fi
            ;;
        linux)
            if command_exists apt-get; then
                sudo apt-get update
                sudo apt-get install -y openjdk-21-jdk
            elif command_exists yum; then
                sudo yum install -y java-21-openjdk-devel
            else
                log_error "Package manager not found. Please install Java manually."
                exit 1
            fi
            ;;
        windows)
            log_warning "Please install Java manually from https://adoptium.net/"
            ;;
    esac
    
    log_success "Java installation completed"
}

# Install Maven
install_maven() {
    log_info "Installing Maven..."
    
    if command_exists mvn; then
        log_success "Maven is already installed"
        return
    fi
    
    case $OS in
        macos)
            brew install maven
            ;;
        linux)
            if command_exists apt-get; then
                sudo apt-get install -y maven
            elif command_exists yum; then
                sudo yum install -y maven
            fi
            ;;
        windows)
            log_warning "Please install Maven manually from https://maven.apache.org/"
            ;;
    esac
    
    log_success "Maven installation completed"
}

# Install Docker
install_docker() {
    log_info "Installing Docker..."
    
    if command_exists docker; then
        log_success "Docker is already installed"
        return
    fi
    
    case $OS in
        macos)
            log_warning "Please install Docker Desktop from https://www.docker.com/products/docker-desktop"
            ;;
        linux)
            curl -fsSL https://get.docker.com -o get-docker.sh
            sudo sh get-docker.sh
            sudo usermod -aG docker $USER
            rm get-docker.sh
            log_warning "Please log out and back in to use Docker without sudo"
            ;;
        windows)
            log_warning "Please install Docker Desktop from https://www.docker.com/products/docker-desktop"
            ;;
    esac
    
    log_success "Docker installation completed"
}

# Install kubectl
install_kubectl() {
    log_info "Installing kubectl..."
    
    if command_exists kubectl; then
        log_success "kubectl is already installed"
        return
    fi
    
    case $OS in
        macos)
            brew install kubectl
            ;;
        linux)
            curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
            sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
            rm kubectl
            ;;
        windows)
            log_warning "Please install kubectl manually from https://kubernetes.io/docs/tasks/tools/"
            ;;
    esac
    
    log_success "kubectl installation completed"
}

# Install development tools
install_dev_tools() {
    log_info "Installing development tools..."
    
    case $OS in
        macos)
            if ! command_exists brew; then
                log_info "Installing Homebrew..."
                /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
            fi
            brew install jq curl wget git
            ;;
        linux)
            if command_exists apt-get; then
                sudo apt-get update
                sudo apt-get install -y jq curl wget git build-essential
            elif command_exists yum; then
                sudo yum install -y jq curl wget git gcc gcc-c++ make
            fi
            ;;
    esac
    
    log_success "Development tools installation completed"
}

# Setup project
setup_project() {
    log_info "Setting up project..."
    
    # Create directories
    mkdir -p ~/.food-service/{logs,data,certs}
    
    # Generate self-signed certificate for development
    if [[ ! -f ~/.food-service/certs/keystore.p12 ]]; then
        log_info "Generating self-signed SSL certificate for development..."
        keytool -genkeypair -alias food-service -keyalg RSA -keysize 2048 \
            -storetype PKCS12 -keystore ~/.food-service/certs/keystore.p12 \
            -storepass changeit -keypass changeit \
            -dname "CN=localhost,OU=Development,O=Food Service,L=City,ST=State,C=US" \
            -ext SAN=dns:localhost,ip:127.0.0.1
    fi
    
    # Create development configuration
    cat > application-dev.yml << EOF
server:
  port: 8443
  ssl:
    enabled: true
    key-store: file:\${user.home}/.food-service/certs/keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: food-service

spring:
  profiles:
    active: dev
  application:
    name: food-service

logging:
  level:
    com.example.foodservice: DEBUG
  file:
    name: \${user.home}/.food-service/logs/food-service.log

management:
  endpoints:
    web:
      exposure:
        include: "health,info,metrics,env,configprops"
  endpoint:
    health:
      show-details: always
EOF
    
    log_success "Project setup completed"
}

# Install VS Code extensions
install_vscode_extensions() {
    if command_exists code; then
        log_info "Installing VS Code extensions..."
        
        extensions=(
            "vscjava.vscode-java-pack"
            "vscjava.vscode-spring-initializr"
            "pivotal.vscode-spring-boot"
            "redhat.java"
            "ms-kubernetes-tools.vscode-kubernetes-tools"
            "ms-azuretools.vscode-docker"
            "humao.rest-client"
            "streetsidesoftware.code-spell-checker"
        )
        
        for ext in "${extensions[@]}"; do
            code --install-extension "$ext" || true
        done
        
        log_success "VS Code extensions installed"
    else
        log_warning "VS Code not found. Skipping extension installation."
    fi
}

# Setup Git hooks
setup_git_hooks() {
    log_info "Setting up Git hooks..."
    
    mkdir -p .git/hooks
    
    # Pre-commit hook
    cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
# Run tests before commit
echo "Running tests..."
mvn clean test
if [ $? -ne 0 ]; then
    echo "Tests failed. Commit aborted."
    exit 1
fi

# Check code formatting
echo "Checking code formatting..."
mvn spring-javaformat:validate
if [ $? -ne 0 ]; then
    echo "Code formatting issues found. Run 'mvn spring-javaformat:apply' to fix."
    exit 1
fi
EOF
    
    chmod +x .git/hooks/pre-commit
    log_success "Git hooks setup completed"
}

# Create helper scripts
create_helper_scripts() {
    log_info "Creating helper scripts..."
    
    mkdir -p scripts
    
    # Development server script
    cat > scripts/dev-server.sh << 'EOF'
#!/bin/bash
echo "Starting Food Service in development mode..."
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
EOF
    
    # Build script
    cat > scripts/build.sh << 'EOF'
#!/bin/bash
echo "Building Food Service..."
mvn clean package
docker build -t food-service:dev .
EOF
    
    # Test script
    cat > scripts/test.sh << 'EOF'
#!/bin/bash
echo "Running all tests..."
mvn clean test
echo "Generating test report..."
mvn jacoco:report
echo "Test report available at: target/site/jacoco/index.html"
EOF
    
    chmod +x scripts/*.sh
    log_success "Helper scripts created"
}

# Verify installation
verify_installation() {
    log_info "Verifying installation..."
    
    errors=0
    
    # Check Java
    if command_exists java; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
        if [[ "$JAVA_VERSION" -ge 17 ]]; then
            log_success "Java $JAVA_VERSION ✓"
        else
            log_error "Java version $JAVA_VERSION is too old (minimum: 17)"
            ((errors++))
        fi
    else
        log_error "Java not found"
        ((errors++))
    fi
    
    # Check Maven
    if command_exists mvn; then
        MVN_VERSION=$(mvn -version | head -1 | awk '{print $3}')
        log_success "Maven $MVN_VERSION ✓"
    else
        log_error "Maven not found"
        ((errors++))
    fi
    
    # Check Docker
    if command_exists docker; then
        DOCKER_VERSION=$(docker --version | awk '{print $3}' | sed 's/,//')
        log_success "Docker $DOCKER_VERSION ✓"
    else
        log_error "Docker not found"
        ((errors++))
    fi
    
    # Check kubectl
    if command_exists kubectl; then
        KUBECTL_VERSION=$(kubectl version --client --short | awk '{print $3}')
        log_success "kubectl $KUBECTL_VERSION ✓"
    else
        log_warning "kubectl not found (optional for local development)"
    fi
    
    if [[ $errors -eq 0 ]]; then
        log_success "All required tools are installed and ready!"
    else
        log_error "$errors error(s) found. Please fix them before proceeding."
        exit 1
    fi
}

# Main function
main() {
    echo "========================================="
    echo "Food Service Development Environment Setup"
    echo "========================================="
    
    detect_os
    install_dev_tools
    install_java
    install_maven
    install_docker
    install_kubectl
    setup_project
    install_vscode_extensions
    setup_git_hooks
    create_helper_scripts
    verify_installation
    
    echo ""
    log_success "Development environment setup completed!"
    echo ""
    echo "Next steps:"
    echo "1. Build the project: ./scripts/build.sh"
    echo "2. Run tests: ./scripts/test.sh"
    echo "3. Start development server: ./scripts/dev-server.sh"
    echo "4. Access the application: https://localhost:8443/api/foods"
    echo ""
    echo "For debugging, the application will listen on port 5005"
    echo ""
}

# Run main function
main "$@"
EOF

chmod +x scripts/setup-dev.sh
```

### Docker Development Environment Script

Create `scripts/setup-docker-dev.sh`:

```bash
#!/bin/bash

set -e

# Create Docker Compose for development
cat > docker-compose.dev.yml << 'EOF'
version: '3.8'

services:
  food-service:
    build:
      context: .
      dockerfile: Dockerfile.dev
    ports:
      - "8443:8443"
      - "5005:5005"  # Debug port
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
    volumes:
      - ./src:/app/src
      - ./target:/app/target
      - ~/.m2:/root/.m2
      - ./certs:/app/certs
    networks:
      - food-service-network
    healthcheck:
      test: ["CMD", "curl", "-f", "-k", "https://localhost:8443/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  postgres:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=foodservice
      - POSTGRES_USER=fooduser
      - POSTGRES_PASSWORD=foodpass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/init-db.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - food-service-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U fooduser -d foodservice"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    networks:
      - food-service-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 3

volumes:
  postgres_data:

networks:
  food-service-network:
    driver: bridge
EOF

# Create development Dockerfile
cat > Dockerfile.dev << 'EOF'
FROM maven:3.9-openjdk-21-slim

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Install development tools
RUN apt-get update && apt-get install -y curl procps && rm -rf /var/lib/apt/lists/*

# Expose ports
EXPOSE 8443 5005

# Development command with hot reload
CMD ["mvn", "spring-boot:run", "-Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"]
EOF

echo "Docker development environment setup completed!"
echo "Run: docker-compose -f docker-compose.dev.yml up"
```

### Local Kubernetes Setup Script

Create `scripts/setup-k8s-local.sh`:

```bash
#!/bin/bash

set -e

log_info() {
    echo "[INFO] $1"
}

# Install kind (Kubernetes in Docker)
install_kind() {
    if ! command -v kind >/dev/null 2>&1; then
        log_info "Installing kind..."
        curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.20.0/kind-linux-amd64
        chmod +x ./kind
        sudo mv ./kind /usr/local/bin/kind
    fi
}

# Create kind cluster
create_cluster() {
    log_info "Creating kind cluster..."
    
    cat > kind-config.yaml << EOF
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  kubeadmConfigPatches:
  - |
    kind: InitConfiguration
    nodeRegistration:
      kubeletExtraArgs:
        node-labels: "ingress-ready=true"
  extraPortMappings:
  - containerPort: 80
    hostPort: 80
    protocol: TCP
  - containerPort: 443
    hostPort: 443
    protocol: TCP
- role: worker
- role: worker
EOF
    
    kind create cluster --config kind-config.yaml --name food-service-dev
}

# Install ingress controller
install_ingress() {
    log_info "Installing NGINX ingress controller..."
    kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
    kubectl wait --namespace ingress-nginx \
        --for=condition=ready pod \
        --selector=app.kubernetes.io/component=controller \
        --timeout=90s
}

# Create namespace and deploy application
deploy_app() {
    log_info "Deploying Food Service application..."
    
    kubectl create namespace food-service-dev || true
    
    # Create SSL secret
    kubectl create secret tls food-service-ssl \
        --cert=certs/certificate.pem \
        --key=certs/private-key.pem \
        -n food-service-dev || true
    
    # Deploy application
    kubectl apply -f k8s-ssl-deployment.yaml -n food-service-dev
    
    # Wait for deployment
    kubectl wait --for=condition=available --timeout=300s deployment/food-service -n food-service-dev
}

main() {
    install_kind
    create_cluster
    install_ingress
    deploy_app
    
    log_info "Local Kubernetes setup completed!"
    log_info "Access the application at: https://localhost/api/foods"
}

main "$@"
```

## Manual Setup Instructions

If you prefer to set up the environment manually, follow these steps:

### 1. Install Java Development Kit

```bash
# macOS with Homebrew
brew install openjdk@21

# Ubuntu/Debian
sudo apt-get update
sudo apt-get install openjdk-21-jdk

# RHEL/CentOS
sudo yum install java-21-openjdk-devel
```

### 2. Install Maven

```bash
# macOS with Homebrew
brew install maven

# Ubuntu/Debian
sudo apt-get install maven

# Manual installation
wget https://archive.apache.org/dist/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz
tar xzf apache-maven-3.9.5-bin.tar.gz
sudo mv apache-maven-3.9.5 /opt/maven
echo 'export PATH=/opt/maven/bin:$PATH' >> ~/.bashrc
```

### 3. Install Docker

```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# macOS - Install Docker Desktop from https://www.docker.com/products/docker-desktop
```

### 4. Clone and Build Project

```bash
git clone https://github.com/your-org/food-service.git
cd food-service
mvn clean package
```

## IDE Configuration

### VS Code Setup

1. **Install Extensions:**
   ```bash
   code --install-extension vscjava.vscode-java-pack
   code --install-extension pivotal.vscode-spring-boot
   code --install-extension ms-kubernetes-tools.vscode-kubernetes-tools
   ```

2. **Workspace Settings (`.vscode/settings.json`):**
   ```json
   {
     "java.configuration.updateBuildConfiguration": "automatic",
     "java.compile.nullAnalysis.mode": "automatic",
     "spring-boot.ls.checkJVM": false,
     "files.exclude": {
       "**/target": true,
       "**/.classpath": true,
       "**/.project": true,
       "**/.settings": true,
       "**/.factorypath": true
     }
   }
   ```

3. **Launch Configuration (`.vscode/launch.json`):**
   ```json
   {
     "version": "0.2.0",
     "configurations": [
       {
         "type": "java",
         "name": "Food Service",
         "request": "launch",
         "mainClass": "com.example.foodservice.FoodServiceApplication",
         "projectName": "food-service",
         "env": {
           "SPRING_PROFILES_ACTIVE": "dev"
         }
       },
       {
         "type": "java",
         "name": "Food Service Debug",
         "request": "attach",
         "hostName": "localhost",
         "port": 5005
       }
     ]
   }
   ```

### IntelliJ IDEA Setup

1. **Import Project:**
   - File → Open → Select `pom.xml`
   - Import as Maven project

2. **Run Configuration:**
   - Run → Edit Configurations
   - Add new Spring Boot configuration
   - Main class: `com.example.foodservice.FoodServiceApplication`
   - VM options: `-Dspring.profiles.active=dev`
   - Environment variables: `SPRING_PROFILES_ACTIVE=dev`

## Docker Development Environment

### Quick Start with Docker Compose

```bash
# Start all services
docker-compose -f docker-compose.dev.yml up -d

# View logs
docker-compose -f docker-compose.dev.yml logs -f food-service

# Stop services
docker-compose -f docker-compose.dev.yml down
```

### Development with Hot Reload

```bash
# Enable hot reload in IDE, then restart container
docker-compose -f docker-compose.dev.yml restart food-service
```

## Local Kubernetes Setup

### Using kind (Kubernetes in Docker)

```bash
# Create cluster
./scripts/setup-k8s-local.sh

# Deploy application
kubectl apply -f k8s-ssl-deployment.yaml -n food-service-dev

# Port forward for local access
kubectl port-forward svc/food-service 8443:8443 -n food-service-dev
```

### Using minikube

```bash
# Start minikube
minikube start --driver=docker

# Enable ingress
minikube addons enable ingress

# Deploy application
kubectl apply -f k8s-ssl-deployment.yaml

# Get service URL
minikube service food-service --url
```

## Testing Environment

### Unit Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=FoodControllerTest

# Run with coverage
mvn test jacoco:report
```

### Integration Tests

```bash
# Run integration tests
mvn test -Dtest=IntegrationTest

# Run with test profile
mvn test -Dspring.profiles.active=test
```

### API Testing

Create `test-requests.http` for VS Code REST Client:

```http
### Health Check
GET https://localhost:8443/actuator/health
Accept: application/json

### Get all foods
GET https://localhost:8443/api/foods
Accept: application/json

### Get food by ID
GET https://localhost:8443/api/foods/1
Accept: application/json

### Search foods
GET https://localhost:8443/api/foods/search?name=apple
Accept: application/json

### Create new food
POST https://localhost:8443/api/foods
Content-Type: application/json

{
  "name": "Test Food",
  "description": "A test food item",
  "category": "Test Category"
}
```

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   # Find process using port 8443
   lsof -i :8443
   # Kill the process
   kill -9 <PID>
   ```

2. **SSL Certificate Issues**
   ```bash
   # Regenerate certificate
   keytool -delete -alias food-service -keystore ~/.food-service/certs/keystore.p12 -storepass changeit
   # Run setup script again
   ./scripts/setup-dev.sh
   ```

3. **Maven Dependencies**
   ```bash
   # Clean and reinstall dependencies
   mvn clean
   rm -rf ~/.m2/repository/com/example/foodservice
   mvn dependency:resolve
   ```

4. **Docker Issues**
   ```bash
   # Reset Docker
   docker system prune -a
   # Restart Docker service
   sudo systemctl restart docker
   ```

### Debug Mode

```bash
# Start application in debug mode
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

### Log Analysis

```bash
# View application logs
tail -f ~/.food-service/logs/food-service.log

# Filter error logs
grep -i error ~/.food-service/logs/food-service.log

# View Docker logs
docker-compose -f docker-compose.dev.yml logs -f food-service
```

---

**Last Updated:** [Current Date]
**Document Owner:** [Development Team]
**Review Schedule:** Monthly

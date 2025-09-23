# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Common Development Commands

### Building the Application
```powershell
# Clean build all modules
mvn clean install

# Build without running tests
mvn clean install -DskipTests

# Build specific module (run from root)
mvn clean install -pl ngo-nabarun-app
```

### Running the Application
```powershell
# Using the provided PowerShell script (recommended)
.\start-server.ps1

# Direct Java execution (from ngo-nabarun-app directory)
cd ngo-nabarun-app
java -jar target/ngo-nabarun-app-0.0.3.jar

# With environment variables
java -DENVIRONMENT="dev" -DDOPPLER_PROJECT_NAME="nabarun" -DDOPPLER_SERVICE_TOKEN="your-token" -jar target/ngo-nabarun-app-0.0.3.jar
```

### Testing
```powershell
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl ngo-nabarun-businesslogic

# Run tests with coverage (if configured)
mvn clean test jacoco:report
```

### Database Operations
```javascript
# Navigate to migrate scripts for database operations
cd ngo-nabarun-app/migrate

# Run migration scripts (Node.js based)
node migrate.js
node migrate-documents.js
```

### Docker Operations
```powershell
# Build Docker image (from ngo-nabarun-api directory)
cd ngo-nabarun-api
docker build -t ngo-nabarun-api .

# Run deployment script
.\docker-deploy.bat
```

## High-Level Architecture

### Multi-Module Maven Structure
This is a Spring Boot application organized as a multi-module Maven project with clear separation of concerns:

- **ngo-nabarun-app**: Main application module containing the Spring Boot main class and configuration
- **ngo-nabarun-api**: REST API controllers and API documentation (Swagger/OpenAPI)
- **ngo-nabarun-web**: Web-specific components and Thymeleaf templates
- **ngo-nabarun-businesslogic**: Business logic layer with service implementations
- **ngo-nabarun-infrastructure**: Data access layer with MongoDB repositories and external integrations
- **ngo-nabarun-externalintegration**: External service integrations (Auth0, SendGrid, Firebase, etc.)
- **ngo-nabarun-util**: Common utilities and shared components
- **ngo-nabarun-config**: Configuration classes and application setup

### Technology Stack
- **Framework**: Spring Boot 2.7.1 with Java 17
- **Database**: MongoDB with Spring Data MongoDB
- **Caching**: Redis with Spring Data Redis, Caffeine local caching
- **Security**: Spring Security with OAuth2 (Auth0 integration)
- **Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Templating**: Thymeleaf for server-side rendering
- **Deployment**: Google App Engine (GAE) with Docker containerization
- **Build**: Maven with multi-module structure

### Layered Architecture Pattern
The application follows a clean layered architecture:

1. **Presentation Layer** (`ngo-nabarun-api`, `ngo-nabarun-web`): REST controllers and web controllers
2. **Business Layer** (`ngo-nabarun-businesslogic`): Service implementations with business logic
3. **Infrastructure Layer** (`ngo-nabarun-infrastructure`): Repository implementations and data access
4. **Integration Layer** (`ngo-nabarun-externalintegration`): External service integrations

### Key Design Patterns
- **Repository Pattern**: MongoDB repositories for data access
- **Service Layer**: Business logic encapsulation with clear interfaces
- **Configuration-Based**: Extensive use of `AppConfig.json` for dynamic configuration
- **Workflow Engine**: Custom workflow implementation for user onboarding and approval processes

### Package Structure
```
ngo.nabarun.app
├── boot/              # Application bootstrap and main class
├── api/               # REST API controllers
├── web/               # Web controllers and UI
├── businesslogic/     # Service implementations
├── infra/             # Infrastructure services and repositories
├── ext/               # External integrations
├── common/            # Shared utilities and enums
├── security/          # Security configurations
└── config/            # Application configurations
```

### Configuration Management
- **Environment-based profiles**: Uses Spring profiles (dev, stage, prod)
- **External configuration**: Doppler integration for secret management
- **AppConfig.json**: Comprehensive configuration for workflows, user roles, email templates
- **Dynamic configuration**: Runtime configuration updates supported

### Workflow System
The application implements a custom workflow engine for:
- User onboarding processes
- Donation management
- Payment confirmations
- Approval workflows with multi-level approvals
- Request lifecycle management

### External Integrations
- **Auth0**: User authentication and authorization
- **SendGrid**: Email notifications with template-based emails
- **Firebase**: Cloud messaging and Firestore for additional data storage
- **Redis**: Session management and caching
- **Google Cloud Platform**: Application hosting and services

### Development Environment Setup
1. MongoDB instance (local or cloud)
2. Redis instance for caching
3. Environment file (`.env`) with required configurations:
   - `ENVIRONMENT=dev`
   - `DOPPLER_PROJECT_NAME`
   - `DOPPLER_SERVICE_TOKEN`
   - Database and Redis connection strings

### Deployment Pipeline
- **GitHub Actions**: Automated CI/CD with workflows in `.github/workflows/`
- **Semantic Release**: Automated versioning and releases
- **Google App Engine**: Production deployment target
- **Environment-specific**: Separate stage and production environments

### Key Business Domain
This is an NGO (Non-Governmental Organization) management system handling:
- Member registration and onboarding
- Donation collection and tracking
- Payment processing workflows
- Role-based access control
- Communication and notification systems
- Event and meeting management
- Financial transaction tracking

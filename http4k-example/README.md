# http4k Example API

A production-ready RESTful API built with http4k demonstrating clean architecture, JWT authentication, and OpenAPI documentation.

It is hard to find a good example of a production-ready REST API built with Kotlin and http4k. This project aims to fill that gap.

## Features

- **JWT Authentication** - Secure endpoints with bearer token authentication
- **OpenAPI 3.0** - Auto-generated API documentation from contract definitions
- **Swagger UI** - Interactive API documentation interface
- **Clean Architecture** - Domain-driven design with clear separation of concerns
- **Functional Programming** - Uses Arrow-kt for functional error handling
- **Comprehensive Testing** - Unit and integration tests with 121 test cases
- **Code Quality** - ktlint integration for consistent code style

## Tech Stack

- **Framework**: [http4k](https://www.http4k.org/) - Lightweight, functional HTTP toolkit
- **Language**: Kotlin 2.1.20
- **Runtime**: JVM 21
- **Security**: JWT (Auth0 java-jwt), BCrypt password hashing
- **Serialization**: Kotlinx Serialization
- **Testing**: Kotest, MockK, REST Assured
- **Observability**: Micrometer, OpenTelemetry
- **Code Quality**: ktlint

## NOTE

1. The authenticate endpoint should not be taken as best practice. The intent here is to show a flow of business logic across multiple components.
2. This project uses unsafe CORS settings for demonstration only. Do not use this in production.
3. This project has hard coded secrets in it. Do not use hard-coded secrets in production.

## Features not implemented

- Rate limiting
- Metrics
- Resilience strategies

## Architecture

The project follows **Clean Architecture** principles with clear separation between layers:

```
src/main/kotlin/za/co/ee/learning/
├── domain/                    # Domain layer - business logic
│   ├── security/              # JWT and password providers
│   ├── users/                 # User domain entities
│   │   └── usecases/          # Business use cases
│   └── DomainError.kt         # Domain error types
├── infrastructure/            # Infrastructure layer
│   ├── api/                   # HTTP endpoints with contracts
│   ├── database/              # Data persistence
│   ├── routes/                # Route definitions
│   └── security/              # Security implementations
├── Application.kt             # Main entry point
├── Server.kt                  # Server configuration
└── ServerConfig.kt            # Configuration data class
```

## Getting Started

### Prerequisites

- JDK 21 or later
- Gradle (wrapper included)

### Installation

1. Clone the repository
2. Navigate to the project directory:
   ```bash
   cd http4k-example
   ```

3. Build the project:
   ```bash
   ./gradlew build
   ```

### Running the Application

Start the server:
```bash
./gradlew run
```

The API will be available at `http://localhost:8080`

## API Endpoints

### Public Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| POST | `/api/v1/authenticate` | Authenticate user and get JWT token |
| GET | `/swagger` | Swagger UI documentation |
| GET | `/openapi.json` | OpenAPI 3.0 specification |

### Protected Endpoints (Requires JWT)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users` | Get all users |

### Authentication

1. **Get a JWT token:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/authenticate \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@local.com","password":"password123"}'
   ```

2. **Use the token:**
   ```bash
   curl http://localhost:8080/api/v1/users \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

### Interactive Documentation

Visit `http://localhost:8080/swagger` to explore and test the API using Swagger UI.

## Testing

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Suites
```bash
# Unit tests only
./gradlew test --tests "za.co.ee.learning.domain.*"
./gradlew test --tests "za.co.ee.learning.infrastructure.api.*"

# Integration tests only
./gradlew test --tests "za.co.ee.learning.integration.*"
```

### Test Coverage

- **Unit Tests**: 77 tests covering domain logic, infrastructure, and API layers
- **Integration Tests**: 44 tests covering full HTTP stack with authentication
- **Total**: 121 tests

## Code Quality

### ktlint

The project uses ktlint for maintaining consistent Kotlin code style.

**Check code style:**
```bash
./gradlew ktlintCheck
```

**Auto-format code:**
```bash
./gradlew ktlintFormat
```

## Configuration

The application can be configured via `ServerConfig` class:

```kotlin
ServerConfig(
    port = 8080,                    // Server port
    jwtSecret = "your-secret",      // JWT signing secret
    jwtIssuer = "http4k",          // JWT issuer
    jwtExpirationSeconds = 3600,    // Token expiration time
    enableDebugFilters = true,      // Enable request/response logging
    enableMetrics = true            // Enable Micrometer/OpenTelemetry
)
```

## Project Highlights

### Contract-First API Design

Endpoints are defined using http4k's contract DSL, which automatically generates OpenAPI documentation:

```kotlin
val route: ContractRoute = "/api/v1/users" meta {
    summary = "Get Users"
    description = "Retrieves list of all users (requires JWT authentication)"
    security = BearerAuthSecurity("JWT Bearer Token")
    returning(Status.OK, usersResponseLens to listOf(...))
} bindContract Method.GET to handler
```

### Functional Error Handling

Uses Arrow-kt's `Either` type for explicit error handling:

```kotlin
fun authenticate(request: AuthenticateRequest): DomainResult<AuthenticateResponse> =
    either {
        val user = userRepository.findByEmail(request.email)
            .bind()
            .toEither { DomainError.NotFound("User not found") }
            .bind()
        // ... continue with business logic
    }
```

### JWT Security Filter

Conditional JWT authentication with path exclusions:

```kotlin
JWTFilter(
    jwtProvider = jwtProvider,
    excludedPaths = setOf("/health", "/api/v1/authenticate", "/openapi.json")
)
```

## Development

### Project Structure

- `domain/` - Pure business logic, no framework dependencies
- `infrastructure/api/` - HTTP endpoints with contract definitions
- `infrastructure/database/` - Data access implementations
- `infrastructure/routes/` - Route composition and OpenAPI generation
- `infrastructure/security/` - Security implementations (JWT, BCrypt, filters)

### Adding a New Endpoint

1. Create endpoint class in `infrastructure/api/`
2. Define contract with metadata
3. Add route to `ContractRoutes`
4. Update JWT filter exclusions if public

## License

This is an example project for learning purposes.

## Resources

- [http4k Documentation](https://www.http4k.org/)
- [Arrow-kt Documentation](https://arrow-kt.io/)
- [Kotest Documentation](https://kotest.io/)

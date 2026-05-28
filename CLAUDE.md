# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LutorLMS is a Learning Management System REST API built with Java 25 and Spring Boot 4.0.5. It supports student enrollment in courses, task submissions, and PayPal-based one-time and subscription payments.

## Commands

```bash
# Run the application (requires PostgreSQL running)
./mvnw spring-boot:run

# Build
./mvnw clean package

# Run all tests (requires Docker — Testcontainers spins up PostgreSQL, Kafka, RabbitMQ, Redis)
./mvnw test

# Run a single test class
./mvnw test -Dtest=UserServiceTest

# Run a single test method
./mvnw test -Dtest=UserServiceTest#methodName
```

## Architecture

### Package Layout

```
org.jas.ksinxapp/
├── controller/    # REST controllers, @PreAuthorize per-method auth
├── service/       # Business logic
├── repo/          # Spring Data JPA repositories
├── model/         # JPA entities
├── dtos/          # Request/Response DTOs
├── mappers/       # MapStruct DTO↔Entity mappers
├── jwt/           # JWT filter, service, and auth entry point
├── security/      # SecurityConfig, UserPrincipal, UserDetailsService
├── payment/       # PayPal SDK integration (orders + subscriptions)
└── config/        # Jackson, CORS, and file-upload resource handlers
```

### Security

Authentication is dual-mode: JWT (for API clients) and OAuth2 (Google). Both paths converge into the same `UserPrincipal`-backed `SecurityContext`.

- `AuthTokenFilter` — `OncePerRequestFilter` that reads `Authorization: Bearer <token>`, validates via `JwtService`, and populates the `SecurityContext`.
- `JwtService` — Generates and validates HMAC-SHA tokens; secret and expiration are configured via `jwt.secret` and `jwt.expiration` in `application.properties`.
- `MyUserDetailService` — Loads users by `fullName` (not email) for in-memory auth; ensure this matches the login payload field.
- Role hierarchy: `ROLE_ADMIN > ROLE_TEACHER > ROLE_STUDENT`.
- Sessions are `IF_REQUIRED` (used for OAuth2 flow); JWT endpoints are stateless.

### Domain Model

Core relationships:
- `User` → `Enrollment` → `Course` → `Modules` → `Task` → `TaskSubmission`
- `PaymentTransaction` (one-time) and `SubscriptionModel` (recurring) are both linked to `userId` + `courseId` and use `@Version` for optimistic locking.
- Schema is managed by Hibernate `ddl-auto=update` — no migration tooling.

### DTO / Mapper Pattern

MapStruct 1.6 generates mapper implementations at compile time. Mappers live in `mappers/`, and each entity has a corresponding request and response DTO. When adding a new field to an entity, update the entity, the relevant DTOs, and the MapStruct mapper.

### Payment Integration

`payment/` contains five classes for PayPal:
- `PaymentConfig` — creates the `PaypalServerSdkClient` bean from `paypal.client.id`, `paypal.client.secret`, and `paypal.mode` properties.
- `PaymentService` / `PayPalPaymentService` — order creation and capture.
- `PayPalSubscriptionService` — subscription plan management.
- `PayPalWebHookService` — asynchronous webhook event processing.

## Configuration

`application.properties` contains credentials that should be externalized via environment variables in any non-local environment:

| Property | Purpose |
|---|---|
| `spring.datasource.*` | PostgreSQL connection (default: `localhost:5432/postgres`) |
| `jwt.secret` | Base64-encoded 256-bit HMAC key |
| `jwt.expiration` | Token TTL in ms (default: 3600000) |
| `spring.security.oauth2.client.registration.google.*` | Google OAuth2 client credentials |
| `paypal.client.id` / `paypal.client.secret` | PayPal SDK credentials |
| `paypal.mode` | `sandbox` or `live` |
| `file.upload-dir` | Local directory for submitted assignment files (default: `./uploads`) |

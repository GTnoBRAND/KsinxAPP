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
├── service/       # Business logic (incl. MinIoStorageService)
├── repo/          # Spring Data JPA repositories
├── model/         # JPA entities
├── dtos/          # Request/Response DTOs
├── mappers/       # MapStruct DTO↔Entity mappers
├── jwt/           # JWT filter, service, and auth entry point
├── security/      # SecurityConfig, UserPrincipal, UserDetailsService
├── RateLimit/     # SlidingWindowRateLimiter + filter (Redis-backed, per-IP, tiered)
├── payment/       # PayPal SDK integration (orders + subscriptions)
├── redis/         # CacheConfiguration for @Cacheable course lookups
├── listener/      # Async event listeners (e.g. UserRegisteredEvent → verification email)
└── config/        # MinioConfig, AdminBootstrap, etc.
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
- Schema is owned by Flyway (`src/main/resources/db/migration/V*.sql`); Hibernate runs in `validate` mode and must agree with the migrated columns. When you change an `@Column` name (or any other schema-affecting annotation), add a new `V{N}__*.sql` migration in the same change — otherwise the app will fail to start.

### DTO / Mapper Pattern

MapStruct 1.6 generates mapper implementations at compile time. Mappers live in `mappers/`, and each entity has a corresponding request and response DTO. When adding a new field to an entity, update the entity, the relevant DTOs, and the MapStruct mapper.

### File Storage (MinIO)

All binary uploads (course cover photos, module teaser videos, student submissions) go to MinIO — there is no local disk fallback. Two buckets:

- **public bucket** (`minio.bucket-public`) — anonymous read enabled by the `minio-init` compose job; URLs returned by `MinIoStorageService.publicUpload(...)` are permanent and embedded directly in the frontend (`<img>`, `<video>`).
- **private bucket** (`minio.bucket-private`) — student submissions. The API only ever returns the object **key** (not a URL) to clients. Downloads go through `GET /api/v1/submission/{id}/file`, which authorizes the caller (owner OR teacher) and returns a 5-minute presigned URL via `minioClient.getPresignedObjectUrl`. The endpoint rewrites the internal `minio.endpoint` to `minio.public-url` so the browser can actually reach the host.

Local dev: `docker compose up` spins up MinIO at `localhost:9000` (console at `:9001`) and the `minio-init` one-shot job creates both buckets and grants anonymous download on the public one before the app starts.

### Rate Limiting

`RateLimitFilter` (order 60) runs before Spring Security and delegates to `SlidingWindowRateLimiter`, which uses Redis (`StringRedisTemplate`) to keep counters shared across replicas — so the app can be horizontally scaled / load-balanced without each instance keeping its own count. Three tiers, keyed by IP (`X-Forwarded-For` first hop honored):

| Tier | Limit | Window | Applied to |
|---|---|---|---|
| STRICT | 5 | 15 min | login, register, resend-verification, payments |
| MODERATE | 40 | 1 min | any other POST/PUT/DELETE |
| GENEROUS | 200 | 1 min | everything else (mostly GETs) |

429 responses carry a plain-text body; the frontend's `api.js` surfaces `response.text()` as the error message.

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
| `minio.endpoint` | Internal URL the app uses to reach MinIO (e.g. `http://minio:9000`) |
| `minio.public-url` | Browser-facing URL substituted into presigned download links |
| `minio.access-key` / `minio.secret-key` | MinIO credentials (match `MINIO_ROOT_USER` / `MINIO_ROOT_PASSWORD` in compose) |
| `minio.bucket-public` / `minio.bucket-private` | Bucket names; `minio-init` creates them on first start |

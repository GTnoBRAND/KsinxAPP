# LutorLMS — Learning Management System Backend

A RESTful backend for a full-featured Learning Management System (LMS), built with Spring Boot. Designed for language schools or similar institutions, it supports a hierarchical course structure (Courses → Modules → Tasks), JWT + OAuth2 authentication, student enrollment with expiration tracking, homework submission with teacher grading, and PayPal-powered payments and subscriptions.

---

## Features

- **Authentication** — Email/password login with JWT, plus Google OAuth2 social login. OAuth2 sessions can be exchanged for JWTs via a dedicated token endpoint, keeping the backend fully stateless.
- **Role-based access control** — Two roles: `ADMIN` (teachers/staff) and `USER` (students). Endpoint-level and method-level authorization enforced with Spring Security.
- **Course catalog** — Paginated, sortable course listing. Each course contains ordered modules, and each module contains tasks/assignments.
- **Enrollment management** — Students enroll in courses with optional due-date expiration. Enrollment records track access windows automatically.
- **Homework workflow** — Students submit file-based assignments; teachers retrieve ungraded work and post scores with written feedback.
- **File storage** — A dedicated service stores and serves uploaded files (student assignments, course assets).
- **Payments via PayPal** — One-time course payments (create order → user approves → capture) and recurring subscription plans, with webhook support for async confirmation.
- **Password security** — Passwords hashed with Argon2 (Spring Security's `v5_8` defaults).

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.x |
| Security | Spring Security, JWT (jjwt 0.12.6), OAuth2 Client |
| Persistence | Spring Data JPA, PostgreSQL |
| DTO Mapping | MapStruct 1.6 |
| Boilerplate | Lombok |
| Payments | PayPal Server SDK 2.2.0 |
| Crypto | Bouncy Castle |
| Testing | JUnit 5, Testcontainers (PostgreSQL), REST Assured |
| Build | Maven (Maven Wrapper included) |

---

## Project Structure

```
src/main/java/org/jas/ksinxapp/
├── config/          # Jackson & CORS configuration
├── controller/      # REST controllers (see API Reference below)
├── dtos/            # Request/response DTOs
├── jwt/             # JWT filter, service, and auth entry point
├── mappers/         # MapStruct mappers (entity ↔ DTO)
├── model/           # JPA entities (User, Course, Modules, Task, Enrollment, Payment…)
├── payment/         # PayPal service layer (payments, subscriptions, webhooks)
├── repo/            # Spring Data JPA repositories
├── security/        # SecurityConfig, UserDetailsService, UserPrincipal
└── service/         # Business logic services
```

---

## Getting Started

### Prerequisites

- JDK 25
- Maven 3.x (or use the included `mvnw` wrapper)
- A running PostgreSQL instance
- A PayPal Developer account (for payment features)
- A Google Cloud project with OAuth2 credentials (for social login)

### Configuration

Create an `application.properties` (or `application.yml`) in `src/main/resources/` with the following:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/lutorlms
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update

# JWT
app.jwt.secret=YOUR_256_BIT_SECRET
app.jwt.expiration-ms=86400000

# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET

# PayPal
paypal.client-id=YOUR_PAYPAL_CLIENT_ID
paypal.client-secret=YOUR_PAYPAL_CLIENT_SECRET
paypal.mode=sandbox   # switch to 'live' for production

# File storage
file.upload-dir=./uploads
```

### Running the Application

```bash
# Clone the repository
git clone https://github.com/your-org/lutorLMS.git
cd lutorLMS

# Build and run
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080` by default.

> **Schema management:** the database schema is owned by **Flyway** (migrations in
> `src/main/resources/db/migration`). Hibernate runs in `validate` mode and only checks
> that the entities match the migrated schema. To change the schema, add a new
> `V2__*.sql` migration rather than relying on `ddl-auto`.

> **Secrets:** no real credentials are committed. `JWT_SECRET`, PayPal, and Google
> OAuth values come from environment variables. Locally the app boots without them
> (it generates an ephemeral JWT key and disables OAuth/PayPal until configured).

---

## Deployment

### Free one-click deploy to Render

This repo ships a [`render.yaml`](./render.yaml) blueprint that provisions the backend
(+ static frontend), a free PostgreSQL database, and a free Redis (Key Value) instance,
all wired together automatically.

1. Push this repo to GitHub.
2. In the [Render dashboard](https://dashboard.render.com): **New → Blueprint**, select the repo.
3. Render reads `render.yaml`, creates all three resources, and links their connection details.
4. When prompted, set the `sync: false` secrets — at minimum `ADMIN_PASSWORD`
   (and optionally Google/PayPal credentials). `JWT_SECRET` is generated for you.
5. Wait for the build; the app is served at `https://<your-service>.onrender.com`.

**Free-tier caveats:** the web service sleeps after ~15 min idle (cold start on next
request); the free Postgres instance expires ~30 days after creation; the filesystem is
ephemeral, so uploaded files don't survive restarts (attach a disk or object storage for
durability).

### Self-hosted with Docker

A full `docker-compose.yml` (app + Postgres + Redis) is included. See [`DOCKER.md`](./DOCKER.md):

```bash
cp .env.example .env   # edit secrets
docker compose up -d
```

---

## API Reference

All endpoints return JSON. Authenticated endpoints require an `Authorization: Bearer <token>` header.

### Users `/api/v1/users`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/register` | Public | Register a new student account |
| `POST` | `/login` | Public | Email/password login — returns a JWT |
| `GET` | `/auth/token` | Google session | Exchange an active Google OAuth2 session for a JWT |
| `GET` | `/all` | `USER` | List all registered students |
| `PUT` | `/update/{id}` | `USER` or `ADMIN` | Update a student profile |
| `DELETE` | `/delete/{id}` | `ADMIN` | Delete a student account |

### Courses `/api/course`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/all` | Public | List all courses (paginated, sortable) |
| `GET` | `/{id}` | Authenticated | Get a single course by ID |
| `POST` | `/add` | Authenticated | Create a new course |
| `PUT` | `/update/{id}` | Authenticated | Update a course |
| `DELETE` | `/delete/{id}` | `ADMIN` | Delete a course |

Query params for `/all`: `pageNo` (default 1), `pageSize` (default 5), `sortBy` (default `id`), `sortDir` (`asc`/`desc`).

### Modules `/api/v1/modules`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/` | Authenticated | Create a module inside a course |
| `GET` | `/course/{courseId}` | Authenticated | List all modules for a given course |

### Tasks `/api/v1/tasks`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/` | Authenticated | Create a task inside a module |
| `GET` | `/module/{moduleId}` | Authenticated | List all tasks for a given module |

### Enrollments `/api/vi/enrollments`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/` | Authenticated | Enroll a student in a course |
| `GET` | `/all` | Authenticated | List all enrollments |

### Task Submissions `/api/v1/submission`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/` | Authenticated | Submit a task (student) |
| `PUT` | `/{id}/grade` | Authenticated | Grade a submission (teacher) — params: `score`, `teacherFeedback` |
| `GET` | `/ungraded` | Authenticated | List all ungraded submissions |

### Files `/api/v1/files`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/upload` | Authenticated | Upload a file; returns a `fileDownloadUri` |

### Payments `/api/payments`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/orders/create` | Authenticated | Create a PayPal order for a course |
| `POST` | `/orders/{orderId}/capture` | Authenticated | Capture a PayPal order after user approval |
| `GET` | `/return` | Public | PayPal return URL handler (params: `tokenId`, `userId`, `courseId`) |
| `POST` | `/subscriptions/create` | Authenticated | Create a PayPal subscription plan |
| `POST` | `/subscriptions/{subscriptionId}/cancel` | Authenticated | Cancel an active subscription |
| `POST` | `/webhook` | Public | PayPal webhook receiver for async payment events |

---

## Authentication Flow

### Standard Login
1. `POST /api/v1/users/register` to create an account.
2. `POST /api/v1/users/login` with `{ "email": "...", "password": "..." }`.
3. Receive a JWT. Include it as `Authorization: Bearer <token>` on subsequent requests.

### Google OAuth2 Login
1. Navigate to `/oauth2/authorization/google` — Spring Security handles the redirect.
2. After Google authenticates the user, call `GET /api/v1/users/auth/token` while the session cookie is active.
3. Receive a JWT and use it the same way as above.

---

## Running Tests

Tests use Testcontainers to spin up a real PostgreSQL instance automatically — no manual database setup required for testing.

```bash
./mvnw test
```

Make sure Docker is running before executing tests.

---

## License

This project is not yet licensed. Add a `LICENSE` file before open-sourcing or distributing.

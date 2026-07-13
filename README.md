# 🎓 LutorLMS

> A modern **Learning Management System** for language schools, tutors, and small academies — courses, modules, video lessons, homework, grading, and PayPal payments, all in one self-hostable Spring Boot backend.

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?logo=springboot&logoColor=white">
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white">
  <img alt="Redis" src="https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white">
  <img alt="MinIO" src="https://img.shields.io/badge/MinIO-S3-C72E49?logo=minio&logoColor=white">
  <img alt="Docker" src="https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white">
</p>

---

## ✨ What is it?

LutorLMS gives you everything you need to sell and run **online courses** without stitching together five SaaS products:

- 📚 **Structured content** — Courses → Modules → Tasks, with teaser videos and cover images.
- 👩‍🎓 **Students & teachers** — role-based access (`ADMIN` / `TEACHER` / `STUDENT`), email verification, Google sign-in.
- 📝 **Homework loop** — students upload submissions, teachers grade with written feedback, students see their progress.
- 💳 **PayPal payments** — one-time course purchases *and* recurring subscriptions, with webhook confirmation.
- 🎞️ **Video & file storage** — MinIO-backed (S3-compatible), with public streams for lessons and private presigned URLs for student work.
- 🛡️ **Production-grade security** — JWT + OAuth2, Argon2 password hashing, Redis-backed rate limiting, Flyway-owned schema.

---

## 🚀 Run it in under 2 minutes

You only need **Docker**. That's it — no Java, no Postgres, no Redis installed on your machine.

```bash
# 1. Clone
git clone https://github.com/gtnobrand/lutorLMS.git
cd lutorLMS

# 2. Start everything (app + Postgres + Redis + MinIO)
docker compose up -d

# 3. Open the app
#    → http://localhost:8080        (API + frontend)
#    → http://localhost:9001        (MinIO console — user/pass: minioadmin/minioadmin)
```

That's it. Flyway runs the migrations, an admin user is bootstrapped from env vars, and MinIO's buckets are created automatically.

> **Default admin login:** set `ADMIN_EMAIL` and `ADMIN_PASSWORD` in a `.env` file before starting — otherwise the defaults from `docker-compose.yml` are used.

See [`DOCKER.md`](./DOCKER.md) for pulling the prebuilt image (`gtnobrand/lutorlms`), publishing your own, or running the stack without cloning the repo.

---

## 🧑‍💻 Local development (running from source)

Prefer working against the raw code? You'll need **JDK 25** and **Docker** (for Postgres/Redis/MinIO):

```bash
# Start only the infra
docker compose up -d db redis minio minio-init

# Run the app locally with hot reload
./mvnw spring-boot:run
```

Run the tests (Testcontainers spins up the dependencies automatically):

```bash
./mvnw test
```

---

## 🧩 Tech stack

| Layer          | Technology                                          |
|----------------|-----------------------------------------------------|
| Language       | Java 25                                             |
| Framework      | Spring Boot 4.0                                     |
| Security       | Spring Security · JWT (jjwt) · OAuth2 (Google)      |
| Persistence    | Spring Data JPA · PostgreSQL 16 · Flyway migrations |
| Cache & rate limit | Redis 7 (sliding-window per IP)                 |
| Object storage | MinIO (S3-compatible) — public & private buckets    |
| Payments       | PayPal Server SDK 2.2                               |
| DTO mapping    | MapStruct 1.6                                       |
| Testing        | JUnit 5 · Testcontainers · REST Assured             |
| Build          | Maven (wrapper included)                            |

---

## 🗺️ API at a glance

All endpoints are JSON. Authenticated calls require `Authorization: Bearer <token>`.

| Area              | Base path                | Highlights                                                 |
|-------------------|--------------------------|------------------------------------------------------------|
| Auth & users      | `/api/v1/users`          | `register`, `login`, `verify`, Google `auth/token`, roles  |
| Courses           | `/api/course`            | CRUD + paginated `/all`, ratings, status, cover uploads    |
| Modules           | `/api/v1/modules`        | Create modules with teaser video, list by course           |
| Tasks             | `/api/v1/tasks`          | Create tasks inside modules, list by module                |
| Enrollments       | `/api/vi/enrollments`    | Enroll, list mine, view progress                           |
| Submissions       | `/api/v1/submission`     | Upload work, grade, list ungraded, presigned downloads     |
| Payments          | `/api/v1/payments`       | Create order · PayPal return / cancel handlers             |
| Files             | `/api/v1/files`          | Generic authenticated upload                               |
| Sitemap           | `/sitemap.xml`           | SEO sitemap                                                |

---

## 🔐 Auth flows

**Email + password**
1. `POST /api/v1/users/register` → verification email is sent.
2. `GET /api/v1/users/verify?token=…` → account activated.
3. `POST /api/v1/users/login` → receive a JWT.

**Google OAuth2**
1. Redirect the user to `/oauth2/authorization/google`.
2. After Google returns, call `GET /api/v1/users/auth/token` while the session is active.
3. Receive a JWT and use it like any other Bearer token.

---

## ⚙️ Configuration

Every secret is read from environment variables — nothing sensitive is committed. The most common ones:

| Variable | Purpose |
|---|---|
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | Postgres connection |
| `JWT_SECRET` · `JWT_EXPIRATION` | Signing key (base64) + TTL (ms) |
| `GOOGLE_CLIENT_ID` · `GOOGLE_CLIENT_SECRET` | Google OAuth2 |
| `PAYPAL_CLIENT_ID` · `PAYPAL_CLIENT_SECRET` · `PAYPAL_MODE` | `sandbox` or `live` |
| `MINIO_ENDPOINT` · `MINIO_PUBLIC_URL` · `MINIO_ACCESS_KEY` · `MINIO_SECRET_KEY` | Object storage |
| `SPRING_DATA_REDIS_HOST` · `_PORT` | Cache + rate limiter |
| `ADMIN_EMAIL` · `ADMIN_PASSWORD` | Bootstrapped admin on first start |

For local Docker runs the defaults in `docker-compose.yml` work out-of-the-box.

---

## ☁️ Deploy to Render (free tier)

The included [`render.yaml`](./render.yaml) blueprint provisions the app, a free PostgreSQL, and a free Redis in one click:

1. Push this repo to GitHub.
2. In [Render](https://dashboard.render.com): **New → Blueprint → select the repo**.
3. Fill the `sync: false` secrets (at least `ADMIN_PASSWORD`). `JWT_SECRET` is generated for you.
4. Wait for the build — done.

Free-tier notes: the service sleeps after ~15 min idle, the free Postgres expires after ~30 days, and the filesystem is ephemeral (attach MinIO / S3 for durable uploads).

---

## 📁 Project layout

```
src/main/java/org/jas/ksinxapp/
├── controller/    REST controllers (course, modules, tasks, submissions, payments, users…)
├── service/       Business logic (incl. MinIoStorageService)
├── repo/          Spring Data JPA repositories
├── model/         JPA entities
├── dtos/          Request / response DTOs
├── mappers/       MapStruct entity ↔ DTO mappers
├── jwt/           JWT filter, service, auth entry point
├── security/      SecurityConfig, UserPrincipal, UserDetailsService
├── RateLimit/     Redis-backed sliding-window rate limiter
├── payment/       PayPal orders, subscriptions, webhooks
├── redis/         Cache configuration
├── listener/      Async event listeners (e.g. verification email)
└── config/        MinIO, admin bootstrap, CORS
```

Schema lives in `src/main/resources/db/migration/V*.sql` (Flyway). Hibernate runs in `validate` mode — always ship a new migration alongside any entity change.

---


<img width="2467" height="1285" alt="Screenshot From 2026-07-13 17-47-02" src="https://github.com/user-attachments/assets/678a14a8-444a-48f2-af5a-573ecd718f11" />
<img width="2467" height="1285" alt="Screenshot From 2026-07-13 17-47-10" src="https://github.com/user-attachments/assets/c68296d5-a4f4-4c47-a603-b0c2c6d9e7b6" />
<img width="2467" height="1285" alt="Screenshot From 2026-07-13 17-47-26" src="https://github.com/user-attachments/assets/38cee3eb-cebb-41c8-851d-42b4d9e52190" />
<img width="2467" height="1285" alt="Screenshot From 2026-07-13 17-52-51" src="https://github.com/user-attachments/assets/9bd77665-b4a9-43d2-9ff2-f39f2bdf282b" />
<img width="2467" height="1285" alt="Screenshot From 2026-07-13 17-53-00" src="https://github.com/user-attachments/assets/37ff377c-f279-4b84-b787-60be75921ee5" />
<img width="2467" height="1285" alt="Screenshot From 2026-07-13 18-11-05" src="https://github.com/user-attachments/assets/961d0b40-521f-44f1-a75b-1036bf177483" />
<img width="2467" height="1285" alt="Screenshot From 2026-07-13 18-11-17" src="https://github.com/user-attachments/assets/d2c1879d-33a4-4a0e-b55e-9cdf4444cdfc" />
<img width="2467" height="1285" alt="Screenshot From 2026-07-13 18-11-40" src="https://github.com/user-attachments/assets/45461d8d-d134-4725-9636-0114f27ab511" />
<img width="2467" height="1285" alt="Screenshot From 2026-07-13 18-12-19" src="https://github.com/user-attachments/assets/4b8f5ea6-f54b-47be-9db5-9295d7232130" />
<img width="2467" height="1285" alt="Screenshot From 2026-07-13 18-12-32" src="https://github.com/user-attachments/assets/9e1d9d3c-b720-4a63-8fbb-140704e78126" />
<img width="2467" height="1285" alt="Screenshot From 2026-07-13 18-12-40" src="https://github.com/user-attachments/assets/ca9d1cb8-dc84-4e0f-bc5d-cb12468df441" />
<img width="2467" height="1285" alt="Screenshot From 2026-07-13 18-14-53" src="https://github.com/user-attachments/assets/4145a502-cc13-4d05-a1e1-2c8e7a88bdaf" />
<img width="2467" height="1285" alt="Screenshot From 2026-07-13 18-14-57" src="https://github.com/user-attachments/assets/437e9ad1-bba2-43e8-94a4-f60a2990af2c" />
<img width="2467" height="1285" alt="image" src="https://github.com/user-attachments/assets/c18da9a4-1424-4ae5-84f5-4f3095abdaf8" />


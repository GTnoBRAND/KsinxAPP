# Running LutorLMS with Docker

This repo ships a self-contained Docker setup that runs the Spring Boot app,
PostgreSQL, and a Redis cache. The image is published to Docker Hub as
`gtnobrand/lutorlms` — on a fresh machine you can pull and run with one command.

## Stack

| Service | Image                  | Purpose                          |
|---------|------------------------|----------------------------------|
| `app`   | `gtnobrand/lutorlms`   | Spring Boot REST API + frontend  |
| `db`    | `postgres:16-alpine`   | Primary database (JPA)           |
| `redis` | `redis:7-alpine`       | Spring cache (`spring.cache.type=redis`) |

Data is kept in named volumes so it survives `docker compose down`:
- `pgdata`   → Postgres data files
- `redisdata`→ Redis AOF
- `uploads`  → uploaded course covers / teaser videos / submissions

## Quick start (pull and run)

On any machine with Docker:

```bash
# 1. Grab the compose file + env template
curl -O https://raw.githubusercontent.com/<you>/lutorLMSGIT/master/docker-compose.yml
curl -O https://raw.githubusercontent.com/<you>/lutorLMSGIT/master/.env.example
mv .env.example .env   # then edit any secrets you want to override

# 2. Pull and start everything
docker compose pull
docker compose up -d

# 3. Open the app
open http://localhost:8080
```

On first boot, **Flyway** applies the baseline migration to create the schema, then
the app bootstraps an admin user from `ADMIN_EMAIL` / `ADMIN_PASSWORD`.

## Build from source

From this repo:

```bash
docker compose build
docker compose up -d
```

`docker compose up` will use the locally built image if present, or pull from
Docker Hub if you set `DOCKERHUB_USER` to a published repo.

## Publish to Docker Hub

```bash
# Build & tag in one shot
docker compose build app
docker tag gtnobrand/lutorlms:latest gtnobrand/lutorlms:$(git rev-parse --short HEAD)

# Push both tags
docker login
docker push gtnobrand/lutorlms:latest
docker push gtnobrand/lutorlms:$(git rev-parse --short HEAD)
```

For a multi-arch image (linux/amd64 + linux/arm64), prefer `buildx`:

```bash
docker buildx create --use --name lutorlms-builder 2>/dev/null || true
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t gtnobrand/lutorlms:latest \
  -t gtnobrand/lutorlms:$(git rev-parse --short HEAD) \
  --push .
```

## Common operations

```bash
docker compose logs -f app     # follow app logs
docker compose ps              # service status
docker compose restart app     # restart only the app
docker compose down            # stop services (data volumes preserved)
docker compose down -v         # nuke data volumes too
```

## Configuration

Every secret/credential listed in `.env.example` is forwarded to the container
as an environment variable. Spring Boot translates them to property names
automatically (e.g. `SPRING_JPA_HIBERNATE_DDL_AUTO` → `spring.jpa.hibernate.ddl-auto`).
You can also pass any other Spring property the same way without changing the
image — e.g. `LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=INFO`.

### Notes on external URLs

`FileStorageService` and `PaymentService` currently emit hard-coded
`http://localhost:8080/...` URLs. That works for a local `docker compose up`
because port 8080 is published to the host. If you deploy this behind a domain
or a different port, you'll want to refactor those to read from a
`app.public-base-url` property — out of scope for the Docker packaging itself.

### Schema management

The schema is managed by **Flyway** (migrations in `src/main/resources/db/migration`),
and Hibernate runs in `validate` mode (`DDL_AUTO=validate`). On a fresh database Flyway
applies the baseline; on a database previously created by `ddl-auto`, Flyway baselines it
in place (`spring.flyway.baseline-on-migrate=true`). To evolve the schema, add a new
`V2__*.sql` migration. You can still override `DDL_AUTO` in `.env` for experiments.

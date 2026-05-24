# Pawsitters

Pawsitters is a Spring Boot and static frontend prototype for a pet sitting marketplace. This version hardens the original prototype with session authentication, password hashing, DTO-based API responses, owner/sitter authorization checks, Flyway migrations, and a redesigned customer-facing frontend.

## Run Locally

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Start the backend:

```bash
cd backend
./mvnw spring-boot:run
```

Start the frontend from the repository root:

```bash
python3 -m http.server 3000 -d frontend
```

Open `http://localhost:3000`.

## Admin Access

Admin APIs are role-protected. To create a first admin account, set these environment variables before starting the backend:

```bash
export PAWSITTERS_ADMIN_EMAIL=admin@example.com
export PAWSITTERS_ADMIN_PASSWORD='UseAStrongPassword123'
```

The bootstrap creates the admin once if that email does not already exist.

## Tests

```bash
cd backend
./mvnw test
node --check ../frontend/app.js
node --check ../frontend/admin/admin.js
```

## Production Notes

- PostgreSQL is the main database for dev, staging, and production.
- H2 is reserved for the explicit `test` profile only.
- Configure `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` for PostgreSQL with the `staging` or `prod` profile.
- Configure `PAWSITTERS_ALLOWED_ORIGINS` to the deployed frontend origin.
- Keep payment handling, messaging, background jobs, email verification, monitoring, and legal/GDPR workflows as separate production integration workstreams.

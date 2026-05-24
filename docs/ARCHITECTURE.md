# Architecture

## Backend

- Spring Boot 4, Spring MVC, Spring Data JPA, Spring Security, Flyway.
- Public auth endpoints: `/api/auth/register`, `/api/auth/login`, `/api/auth/csrf`, `/api/auth/logout`.
- User-scoped endpoints: `/api/me`, `/api/me/pets`, `/api/requests`, `/api/requests/mine`, `/api/requests/available`.
- Admin endpoints: `/api/admin/users`, `/api/admin/pets`, `/api/admin/requests`.
- API responses use DTOs. JPA entities are not returned directly.

## Data

- PostgreSQL is the main database for development, staging, and production.
- H2 is used only by the explicit `test` profile for fast local and CI regression tests.
- `app_users` stores account identity, role, and password hash.
- `pets` belongs to one owner.
- `sitting_requests` belongs to a requester, may have one sitter, and uses optimistic versioning plus a locked accept flow.
- Flyway migration `V1__initial_schema.sql` creates the baseline schema.

## Frontend

- Static HTML/CSS/JS served separately in development.
- Frontend fetch calls use `credentials: include` and CSRF headers for mutating requests.
- Owner, sitter, and admin workflows are separated in the UI.
- User-provided values are rendered through DOM APIs and `textContent`, not HTML string insertion.

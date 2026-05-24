# Test Documentation

## Automated Tests

Run:

```bash
cd backend
./mvnw test
```

Current backend tests cover:

- Registration stores BCrypt hashes and omits password fields from responses.
- Protected endpoints reject unauthenticated users.
- Users cannot create sitting requests for pets they do not own.
- Owners cannot accept their own requests.
- Non-owners cannot delete another owner's request.
- Admin endpoints reject normal users and allow admin users.

Frontend syntax checks:

```bash
node --check frontend/app.js
node --check frontend/admin/admin.js
```

## Recommended Next Tests

- Browser E2E tests for the full owner and sitter flows.
- Accessibility checks for keyboard navigation and contrast.
- Concurrency tests around two sitters accepting the same request.
- PostgreSQL integration tests with Testcontainers once the project is ready to add containerized test infrastructure.
- Production smoke tests against PostgreSQL.

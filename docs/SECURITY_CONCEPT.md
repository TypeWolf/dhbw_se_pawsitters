# Security Concept - Pawsitters

## Current Implementation

- Spring Security protects all `/api/**` endpoints except registration, login, and CSRF bootstrap.
- Authentication uses server-side HTTP sessions with HttpOnly cookies and CSRF protection for mutating requests.
- Passwords are stored as BCrypt hashes. Password hashes are excluded from JSON and API responses use DTOs.
- CORS is centralized and restricted through `pawsitters.security.allowed-origins` instead of wildcard controller annotations.
- Pet creation, request creation, request acceptance, and deletion derive identity from the authenticated principal.
- Admin data lives under `/api/admin/**` and requires `ROLE_ADMIN`.
- Flyway owns schema creation. Dev uses H2; production is configured for PostgreSQL.

## Remaining Production Work

- Add email verification and password reset emails with expiring, single-use tokens.
- Add managed rate limiting at the edge or API gateway in addition to the in-memory login lockout.
- Add audit log persistence for security events, admin actions, and booking state changes.
- Review CSP once the frontend and backend are deployed under the final domains.
- Complete GDPR/DSGVO workflows: consent, privacy policy, data export, deletion, retention, and processor agreements.
- Add payment provider integration without storing card data in Pawsitters.

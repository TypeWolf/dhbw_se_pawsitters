# Development Process

## Local Workflow

1. Start local PostgreSQL with `docker compose up -d postgres`.
2. Run backend tests with `./mvnw test` from `backend/`.
3. Run frontend syntax checks with `node --check frontend/app.js` and `node --check frontend/admin/admin.js`.
4. Start the backend and serve the frontend on `localhost:3000` for manual QA.
5. Do not commit local files such as `.DS_Store`, `.claude/`, or build output.

## Release Checklist

- Confirm `staging` and `prod` profiles have PostgreSQL credentials through environment variables.
- Confirm `PAWSITTERS_ALLOWED_ORIGINS` matches the deployed frontend origin.
- Confirm `PAWSITTERS_ADMIN_EMAIL` and `PAWSITTERS_ADMIN_PASSWORD` are set only during controlled bootstrap.
- Run backend tests and frontend syntax checks.
- Review security logs after deployment.

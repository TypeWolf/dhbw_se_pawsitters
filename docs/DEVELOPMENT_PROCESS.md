# Development Process

## Local Workflow

1. Run backend tests with `sh ./mvnw test` from `backend/`.
2. Run frontend syntax checks with `node --check frontend/app.js` and `node --check frontend/admin/admin.js`.
3. Start the backend and serve the frontend on `localhost:3000` for manual QA.
4. Do not commit local files such as `.DS_Store`, `.claude/`, or build output.

## Release Checklist

- Confirm `prod` profile has PostgreSQL credentials through environment variables.
- Confirm `PAWSITTERS_ALLOWED_ORIGINS` matches the deployed frontend origin.
- Confirm `PAWSITTERS_ADMIN_EMAIL` and `PAWSITTERS_ADMIN_PASSWORD` are set only during controlled bootstrap.
- Run backend tests and frontend syntax checks.
- Review security logs after deployment.

# Airnest Host Dashboard

Next.js host dashboard frontend backed by a Spring Boot API.

## Stack

- Frontend: Next.js App Router
- Backend: Spring Boot, Spring Web, Spring Data JPA, Spring Security
- Database: PostgreSQL
- Migrations: Flyway
- Auth scaffold: JWT access token flow

## Frontend

Create `C:\CloneCoding\host-dashboard\.env.local`:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

The frontend API helper lives in `lib/api.ts` and the main screen is `app/page.tsx`.

## Backend profiles

The Spring backend now uses profile-specific configuration:

- `local`: local PostgreSQL defaults, seed enabled, local JWT secret default
- `dev`: environment-driven database and JWT config, seed optional
- `prod`: environment-driven database and JWT config, seed disabled

Default profile is `local`.

Key config files:

- `spring-backend/src/main/resources/application.yml`
- `spring-backend/src/main/resources/application-local.yml`
- `spring-backend/src/main/resources/application-dev.yml`
- `spring-backend/src/main/resources/application-prod.yml`

## Database

Local defaults:

```text
host=localhost
port=5432
database=airnest
username=airnest_user
password=airnest_pass
```

Flyway migration scripts live under `spring-backend/src/main/resources/db/migration`.

## Local auth seed

When `local` profile runs with seed enabled, the backend creates a default host account if it does not already exist.

```text
email=host@airnest.local
password=host1234!
role=HOST
```

Override with:

- `APP_SEED_HOST_EMAIL`
- `APP_SEED_HOST_PASSWORD`
- `APP_SEED_HOST_DISPLAY_NAME`
- `APP_JWT_SECRET`

## Run

Start the backend:

```bash
cd C:\CloneCoding\host-dashboard
npm run dev:backend
```

Start the frontend in a separate terminal:

```bash
cd C:\CloneCoding\host-dashboard
npm run dev
```

Open:

- Frontend: http://localhost:3000
- Backend health: http://localhost:8080/health

## Verify

Run backend tests:

```bash
npm run test:backend:spring
```

Manual API checks:

- `GET /health`
- `GET /api/inbox`
- `GET /api/reservations`
- `GET /api/listings`
- `POST /api/auth/login`
- `GET /api/auth/me` with `Authorization: Bearer <token>`

Example login payload:

```json
{
  "email": "host@airnest.local",
  "password": "host1234!"
}
```

## Notes

- Existing dashboard APIs remain open for now so the current frontend flow is not broken.
- JWT login and token validation are in place for staged auth rollout.
- Flyway is configured with `baseline-on-migrate` to transition existing local schemas without dropping data.
- The current reservation/listing/inbox entities still reflect frontend-oriented fields. Converting them to richer domain models should be a later refactor.

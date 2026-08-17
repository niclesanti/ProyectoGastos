# AGENTS.md

## Project Overview

Personal finance management full-stack app. Backend: Spring Boot 3.5.3 / Java 21 / PostgreSQL 14 / Flyway. Frontend: React 18 / TypeScript / Vite / Tailwind + shadcn/ui. Auth: Google OAuth2 + JWT.

## Quick Start (Docker)

```bash
cp .env.example .env   # fill in Google OAuth2 credentials
docker-compose up -d --build
# Frontend: http://localhost:3100 | Backend: http://localhost:8080 | Swagger: http://localhost:8080/swagger-ui/index.html
```

## Commands

### Backend (from `backend/`)

```bash
./mvnw clean test          # run all tests (Linux/Mac)
.\mvnw.cmd clean test      # run all tests (Windows)
./mvnw clean package -Dmaven.test.skip=true   # build JAR without tests
```

- Tests use **H2 in-memory DB** by default (Flyway disabled, schema created by Hibernate). No PostgreSQL needed locally for tests.
- The `test` profile is active during tests; set via `src/test/resources/application.properties`.
- Flyway migrations are in `src/main/resources/db/migration/` with naming `V{number}__{description}.sql`.

### Frontend (from `frontend/`)

```bash
npm install
npm run dev          # Vite dev server on port 3000 (proxied to backend :8080)
npm run build        # tsc + vite build
npm run lint         # ESLint (ts, tsx) — zero warnings enforced
npm run test         # Vitest (watch mode)
npm run test:run     # Vitest single run
npm run test:coverage # Vitest with v8 coverage
```

- Path alias `@/` maps to `./src/` (configured in tsconfig.json, vite.config.ts, vitest.config.ts).
- Vitest setup file: `src/test/setup.ts` — extends expect with jest-dom matchers and runs cleanup after each test.
- Test files live in `src/__tests__/` and colocated `*.test.{ts,tsx}` files.

## Architecture Notes

### Backend Layered Architecture

```
controller/ → service/ → dao/ (repositories)
     ↓           ↓          ↓
  REST API   Business   JPA/Hibernate
             logic      + Flyway
```

- **DTOs** (`dto/`) are separate from JPA entities (`model/`).
- **MapStruct** (`mapper/`) handles entity↔DTO mapping. Mappers are interfaces with `@Mapper(componentModel="spring")`.
- **Lombok** used extensively (`@RequiredArgsConstructor`, `@Data`, etc.).
- **Custom validators** in `validation/` (e.g., `@ValidMonto`, `@ValidSaldoActual`).
- **Scheduler tasks** in `scheduler/` (e.g., `ResumenScheduler`, `NotificacionScheduler`).
- **SSE notifications** via `SseEmitterService` + event-driven architecture (`event/` package).

### Frontend Structure

- `components/ui/` — shadcn/ui primitives (Radix-based, accessible).
- `components/` — reusable app-specific components.
- `features/` — domain logic per module.
- `pages/` — route-level page components.
- `store/` — Zustand state management.
- `services/` — API client layer (axios + React Query).
- `hooks/` — custom React hooks.

### Key Config Details

- **Backend CORS**: configured in `CorsConfig.java`, frontend URL from `frontend.url` property.
- **Backend Actuator**: dev profile exposes all endpoints (`*`); prod exposes only `health,metrics,prometheus,info` on a separate management port (9090).
- **Frontend proxy**: Vite proxies `/api` requests to `http://localhost:8080`.
- **Docker dev**: uses `Dockerfile.dev` with hot-reload and volume mounts for `src/` and `public/`.
- **Docker prod**: multi-stage build (Maven builder → JRE Alpine for backend; Node builder → Nginx for frontend).

## CI/CD

- **CI** (`.github/workflows/ci.yml`): runs on push/PR to `develop` and `main`. Executes `./mvnw clean test` in backend only.
- **CD** (`.github/workflows/cd.yml`): runs on push to `main`. Pipeline: test → build Docker image → push to Docker Hub → deploy to Oracle Cloud via SSH.
- Frontend deploys to **Vercel** (vercel.json has SPA rewrite rule).

## Conventions

- **Branches**: `feature/`, `fix/`, `docs/`, `refactor/`, `test/` prefixes.
- **Commits**: [Conventional Commits](https://www.conventionalcommits.org/) — `feat(scope):`, `fix(scope):`, etc.
- **Language**: Code and docs are in Spanish (variable names, comments, commit messages, READMEs).
- **UI library**: shadcn/ui with `new-york` style variant. Use `npx shadcn-ui@latest add <component>` to add components.
- **Decimal precision**: financial calculations use `decimal.js` (frontend) and `BigDecimal` (backend). Never use floating point for money.

## Custom Agents

### backend-expert

Subagent for backend Java/Spring Boot work. Has 9 skills installed locally at `.agents/skills/` (no global skills).

- **Invoke manually**: `/backend-expert <prompt>`
- **Invoke via Task tool**: `task(subagent="backend-expert", prompt="...")`
- **Modes**: Plan (design only) and Build (implement)
- **Permissions**: can edit `*.java`, `*.xml`, `*.properties`, `*.yml`, `*.yaml`, `*.sql`, `*.md`; can run `./mvnw`, `docker compose`, `npm`

**Skills (project-local only):**

| Skill | When to activate |
|-------|-----------------|
| `java-springboot` | Spring Boot best practices, REST APIs, Actuator, profiles, configuration |
| `spring-boot-security-jwt` | Spring Security, OAuth2, JWT, filter chains, CORS, auth flows |
| `spring-data-jpa` | Repositories, Specifications, auditing, query methods, JPA entities |
| `313-frameworks-spring-db-migrations-flyway` | Flyway migrations, schema versioning, database evolution |
| `java-junit` | JUnit 5, assertions, parameterized tests, test lifecycle |
| `unit-test-service-layer` | Service layer testing patterns, mocking with Mockito, AAA structure |
| `spring-boot-patterns` | Architecture patterns, layered services, DTOs, exception handling |
| `docker-compose-production` | Docker Compose configuration, networking, volumes, production setup |
| `github-actions` | GitHub Actions CI/CD pipelines, workflows, secrets management |

### frontend-expert

Subagent for frontend React/TypeScript work. Has 6 skills installed locally at `.agents/skills/` (no global skills).

- **Invoke manually**: `/frontend-expert <prompt>`
- **Invoke via Task tool**: `task(subagent="frontend-expert", prompt="...")`
- **Modes**: Plan (design only) and Build (implement)
- **Permissions**: can edit `*.ts`, `*.tsx`, `*.js`, `*.jsx`, `*.css`, `*.json`, `*.md`; can run `npm`

**Skills (project-local only):**

| Skill | When to activate |
|-------|-----------------|
| `react-dev` | React best practices, hooks, components, performance, TypeScript patterns |
| `react-state-management` | Zustand stores, React Query, global state, server state patterns |
| `zustand-patterns` | Zustand-specific patterns, slices, middleware, store architecture |
| `react-hook-form-zod` | Form handling with react-hook-form, Zod validation, shadcn/ui Form integration |
| `web-ui-shadcn-ui` | shadcn/ui components, Radix UI primitives, Tailwind styling patterns |
| `vitest` | Vitest testing, Testing Library, jsdom, component/unit test patterns |

### backend-reviewer

Read-only Code Reviewer subagent. Reviews backend implementations against the same 9 backend skills' best practices. Cannot edit files — only produces review reports with severity levels, corrections, and scores.

- **Invoke manually**: `/backend-reviewer <path or description>`
- **Invoke via Task tool**: `task(subagent="backend-reviewer", prompt="revisar path/to/File.java")`
- **Mode**: Read-only (no file edits)
- **Permissions**: read only; no bash, no edit

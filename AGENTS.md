# AGENTS.md

## Project Overview

Personal finance management full-stack app. Backend: Spring Boot 3.5.3 / Java 21 / PostgreSQL 14 / Flyway. Frontend: React 18 / TypeScript / Vite / Tailwind + shadcn/ui. Auth: Google OAuth2 + JWT.

## Codebase Memory (Knowledge Graph)

This project is indexed by **codebase-memory-mcp**, which maintains a knowledge graph of the codebase. **Always prefer the MCP graph tools over grep/glob/file-search for code discovery.**

### Tool priority order

1. `search_graph` — find functions, classes, routes, variables by pattern/query (e.g. `search_graph(query="update settings")`, `search_graph(name_pattern=".*OrderHandler.*")`)
2. `trace_path` — trace who calls a function (`direction="inbound"`) or what it calls (`direction="outbound"`)
3. `get_code_snippet` — read the exact source of a function/class (first find its `qualified_name` via `search_graph`)
4. `check_index_coverage` — validate candidate paths and missed ranges before making claims
5. `query_graph` — run Cypher queries for complex multi-hop patterns
6. `get_architecture` — high-level project summary (structure, dependencies, routes, hotspots)

### Rules of use

- Call `list_projects` / `index_status` at session start or after compaction to confirm the project is indexed, then follow the tier flow: **Scout** (quick provisional lookup), **Verify** (default, task-directed evidence), **Auditor** (bounded-scope full verification).
- After discovering candidate paths, call `check_index_coverage` once with every evidence path before citing or operating on files. A clean result means no recorded gap, not proof of completeness.
- **Pagination**: responses carry `has_more`/`nextCursor`/`next` — paginate when present; never assume a result set is complete without checking.
- **Best-effort**: absence of a coverage flag is NOT a completeness guarantee. Treat results as provisional until verified against source when the claim is material.
- Subagents do NOT inherit MCP access — query the graph in the parent agent first and pass findings (tier, symbols, paths, coverage evidence) to the child.

### When to fall back to grep/glob

- Searching for string literals, error messages, or config values.
- Searching non-code files (Dockerfiles, shell scripts, YAML, etc.).
- When MCP tools return insufficient results or coverage reports flag gaps (`parse_partial`/`skipped` ranges) — grep those exact ranges.

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

### Backend Modular Architecture (Modulith First)

The backend follows a **Modulith First** pattern: a single deployable Spring Boot application organized into cohesive functional modules with a shared kernel and infrastructure layer. This structure reduces inter-module coupling and prepares the codebase for a future migration to microservices when scalability becomes critical.

```
com.campito.backend
├── BackendApplication.java
├── common/                              ← shared kernel (pure types, no Spring wiring)
│   ├── domain/                          ← shared enums (TipoTransaccion)
│   ├── exception/                       ← global exceptions + ControllerAdvisor
│   ├── event/                           ← cross-module events + TipoNotificacion enum
│   ├── dto/                             ← shared DTOs (DistribucionGastoDTO)
│   ├── util/                            ← utilities (MoneyUtils)
│   └── validation/                      ← custom Jakarta validators (@ValidMonto, etc.)
├── config/                              ← infrastructure configuration
│   ├── AsyncConfig                      ← thread pool for CompletableFuture
│   ├── JpaAuditingConfig                ← @EnableJpaAuditing
│   ├── MetricsConfig                    ← business metrics (Micrometer/Prometheus)
│   └── MapstructConfig                  ← MapStruct global config
├── security/                            ← unified security layer
│   ├── SecurityConfig                   ← Spring Security filter chain
│   ├── JwtAuthenticationFilter          ← JWT validation filter
│   ├── JwtTokenProvider                 ← JWT generation/parsing
│   ├── OAuth2AuthenticationSuccessHandler ← OAuth2 redirect with JWT
│   ├── SecurityService (interface)      ← ownership/access validation
│   └── SecurityServiceImpl             ← implementation (cross-module repos)
├── dashboard/                           ← dashboard module
├── descuentos/                          ← discounts module
├── notificaciones/                      ← notifications module
│   └── service/
│       ├── SseEmitterService            ← SSE real-time connections
│       └── SseEmitterServiceImpl
├── transacciones/                       ← transactions module
└── usuarios/                            ← users/workspaces module
```

#### Module internal structure

Each functional module follows this layered convention:

```
{module}/
├── api/             ← module facade (interfaces + impl for inter-module calls)
├── controller/      ← REST controllers
├── service/         ← business logic (interface + impl)
├── repository/      ← Spring Data JPA repositories
├── mapper/          ← MapStruct mappers (entity↔DTO)
├── domain/
│   ├── dto/         ← request/response DTOs
│   └── entity/      ← JPA entities
└── event/           ← event listeners (async consumers)
```

#### Inter-module communication

- **Asynchronous events** (`common/event/`): modules publish domain events (e.g., `TransaccionRegistradaEvent`, `CompraCreditoRegistradaEvent`) that other modules consume via `@EventListener`. This decouples modules without direct dependencies.
- **Module facades** (`api/` packages): when synchronous data access is needed across modules, each module exposes an `Api` interface + `ApiImpl` implementation. Examples: `EspacioTrabajoApi`, `CuotasCreditoApi`, `TarjetaApi`, `ReportesTransaccionesApi`.
- **Shared kernel** (`common/`): exceptions, events, validation annotations, DTOs, and utilities used across multiple modules. These are the "shared contract" that all modules depend on.
- **Cross-module validation**: `SecurityServiceImpl` depends on repositories from multiple modules to validate resource ownership. This is an intentional "security nexus" — each `validate*Ownership` method checks workspace membership before allowing access.

#### Key patterns

- **DTOs** are separate from JPA entities. Request DTOs use validation annotations from `common/validation/`.
- **MapStruct** (`config/MapstructConfig`) handles entity↔DTO mapping. Mappers are interfaces with `@Mapper(config = MapstructConfig.class)`.
- **Lombok** used extensively (`@RequiredArgsConstructor`, `@Data`, etc.).
- **Scheduler tasks** in `scheduler/` (e.g., `ResumenScheduler`, `TarjetaCierreScheduler`, `NotificacionScheduler`).
- **SSE notifications** via `SseEmitterService` in `notificaciones/service/` + event-driven architecture.
- **Business metrics** centralized in `MetricsConfig` (counters, timers, gauges via Micrometer).

### Frontend Structure

- `components/ui/` — shadcn/ui primitives (Radix-based, accessible).
- `components/` — reusable app-specific components.
- `features/` — domain logic per module.
- `pages/` — route-level page components.
- `store/` — Zustand state management.
- `services/` — API client layer (axios + React Query).
- `hooks/` — custom React hooks.

### Key Config Details

- **Backend CORS**: configured in `SecurityConfig.java` (`security/`), frontend URL from `frontend.url` property.
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
| `liquid-glass-design` | Patterns for implementing Apple's Liquid Glass |
| `vitest` | Vitest testing, Testing Library, jsdom, component/unit test patterns |

### backend-reviewer

Read-only Code Reviewer subagent. Reviews backend implementations against the same 9 backend skills' best practices. Cannot edit files — only produces review reports with severity levels, corrections, and scores.

- **Invoke manually**: `/backend-reviewer <path or description>`
- **Invoke via Task tool**: `task(subagent="backend-reviewer", prompt="revisar path/to/File.java")`
- **Mode**: Read-only (no file edits)
- **Permissions**: read only; no bash, no edit

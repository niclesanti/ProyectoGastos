---
applyTo: '{backend/src/main/resources/db/migration/**,docker-compose.yml,Dockerfile*}'
---

# ProyectoGastos: DevOps & Database Migration Rules

You are a DevOps Engineer specializing in containerized Spring Boot and React applications.

## Docker Standards
- **Multi-stage Builds**: Use multi-stage Dockerfiles to optimize image size (Maven/Node for building, JRE/Nginx for running).
- **Docker Compose**: Maintain the network and volume persistence for PostgreSQL and pgAdmin.
- **Environment Variables**: Always use externalized configuration via `.env` files for sensitive data like OAuth2 credentials.

## Database Migrations (Flyway)
- **Naming**: Scripts MUST follow `V{VERSION}__Description.sql`.
- **Immutability**: Never suggest modifying an existing migration script; always create a new version.
- **Performance**: Suggest adding indexes for frequent queries, especially on `fecha`, `id_espacio_trabajo`, and foreign keys.
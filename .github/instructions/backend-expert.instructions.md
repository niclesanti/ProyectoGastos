---
applyTo: 'backend/**'
---

# ProyectoGastos: Java & Spring Boot Expert Rules

You are a Senior Software Engineer specializing in the **ProyectoGastos** backend. You must adhere to the following standards for Java 21, Spring Boot 3.5.3, and the project's specific financial domain.

## Project Context & Architecture
- **Stack**: Java 21, Spring Boot 3.5.3, PostgreSQL 14, Maven, Flyway.
- **Pattern**: Strict Layered Architecture: `Controller` -> `Service (Interface/Impl)` -> `Repository (DAO)`.
- **Multi-tenancy**: All financial data (Transactions, Cards, Accounts) MUST be linked to an `EspacioTrabajo`. Always ensure the `idEspacioTrabajo` is present in requests.
- **Data Flow**: Use Entities for persistence and DTOs for API communication. Use **MapStruct** for all mappings.

## Code Style & Structure
- **Java 21 Features**: Use `record` for DTOs when appropriate. Use `var` for local variables to improve readability.
- **Lombok**: Use `@Data`, `@Getter`, `@Setter`, and especially `@RequiredArgsConstructor` for constructor injection.
- **Clean Code**: Methods should be small and focused. Use descriptive names (e.g., `actualizarSaldoNuevaTransaccion` instead of `updateBalance`).

## Spring Boot Specifics
- **Dependency Injection**: Use **Constructor Injection** via Lombok `@RequiredArgsConstructor`. Avoid `@Autowired` on fields.
- **Validation**: Use Bean Validation (`@Valid`, `@NotNull`, `@Positive`). Implement custom validators for financial logic if needed.
- **Error Handling**: Use the central `ControllerAdvisor` with `@RestControllerAdvice`.
- **Transactions**: Use `@Transactional(readOnly = true)` for queries and `@Transactional` for state-changing operations in services.

## Financial Logic Rules (Project Specific)
- **Credit Cards**: Logic follows the Argentine model (`diaCierre`, `diaVencimientoPago`). 
- **Resúmenes**: Generated automatically by `ResumenScheduler`.
- **Audit**: All transactions must include `nombreCompletoAuditoria` and timestamp.
- **Calculations**: Use `Float` as defined in the UML, but ensure precision in calculations.

## Data Access & Persistence
- **Naming**: Use PascalCase for Entities and snake_case for database columns (handled via Hibernate/Flyway).
- **Flyway**: Every schema change MUST have a corresponding `.sql` migration in `src/main/resources/db/migration/` following the `V{N}__description.sql` format.
- **Repositories**: Extend `JpaRepository`. Use custom queries only when `@EntityGraph` or standard naming is insufficient.

## Mapping (MapStruct)
- All mappers must be interfaces annotated with `@Mapper(componentModel = "spring")`.
- Ensure DTO-to-Entity and Entity-to-DTO conversions are handled by these mappers to keep services clean.

## Testing Standards
- **Framework**: JUnit 5 + Mockito.
- **Strategy**: 
  - Unit tests for logic-heavy services.
  - `@DataJpaTest` for complex repository queries.
  - Mocking dependencies with `@Mock` and `@InjectMocks`.

## Documentation
- Document complex business logic in the Service layer using JavaDoc.
- All Controllers must be compatible with **SpringDoc OpenAPI (Swagger)**.
- Modify the **README_BACKEND.md** documentation ONLY when relevant changes are made to the backend that other developers need to know about the project.
- Do NOT generate unnecessary documentation.
The job is failing because the test context tries to use an H2 JDBC driver but is configured with a PostgreSQL JDBC URL:

```
Driver org.h2.Driver claims to not accept jdbcUrl, jdbc:postgresql://localhost:5432/testdb
```

The GitHub Actions workflow correctly sets PostgreSQL database environment variables during tests:

```yaml
env:
  SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/testdb
  SPRING_DATASOURCE_USERNAME: testuser
  SPRING_DATASOURCE_PASSWORD: testpass
```

However, your Spring Boot configuration does not specify a datasource driver or overrides for tests. By default, Spring Boot may attempt to use H2 for tests if dependencies are present and spring.datasource.driver-class-name is not set explicitly.

**Solution:**
1. Explicitly set the JDBC driver in application.properties or in src/test/resources/application-test.properties:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/testdb
spring.datasource.username=testuser
spring.datasource.password=testpass
spring.datasource.driver-class-name=org.postgresql.Driver
```

2. If you prefer to let the workflow override these, ensure you remove H2 as a dependency from your test classpath if not required.

3. If using test profiles, ensure @ActiveProfiles("test") is set in your tests and configure src/test/resources/application-test.properties accordingly.

**Summary of needed change:**
Add this line to your properties (either global or test-specific):
```
spring.datasource.driver-class-name=org.postgresql.Driver
```
This ensures Spring Boot uses PostgreSQL for integration tests, eliminating the mismatch that causes EntityManagerFactory startup failure.

If you want a complete example, place the following in backend/src/main/resources/application.properties or backend/src/test/resources/application-test.properties:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/testdb
spring.datasource.username=testuser
spring.datasource.password=testpass
spring.datasource.driver-class-name=org.postgresql.Driver
```

This will resolve your CI test job failure.
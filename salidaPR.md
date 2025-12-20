Root cause (from the logs)
- Spring tests fail while building the EntityManagerFactory with a JDBCConnectionException: "Connection to localhost:5432 refused...".
- Postgres service logs show the DB started, then repeated "FATAL: role \"root\" does not exist".
- That shows the application attempted to connect to Postgres as the OS user root (Postgres JDBC will use the current OS username if no username is provided). In short: the test JVM did not supply the DB username/password to the JDBC driver, so the driver attempted to connect as "root" and was rejected.

What to change (quick, concrete fixes)
1) Make sure Spring receives the datasource username & password as properties when Maven runs tests. The most reliable change for CI is to pass them as system properties to the mvn command so the test JVM definitely sees them:

Patch the Run tests step in .github/workflows/ci.yml to run Maven with -Dspring.datasource.* system properties. Example change:

Current step (lines ~56-63 in your ref):
  run: ./mvnw clean test
  env:
    SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/testdb
    SPRING_DATASOURCE_USERNAME: testuser
    SPRING_DATASOURCE_PASSWORD: testpass

Replace with (safe multi-line form):

- name: Run tests
  working-directory: ./backend
  run: |
    ./mvnw \
      -Dspring.datasource.url=jdbc:postgresql://localhost:5432/testdb \
      -Dspring.datasource.username=testuser \
      -Dspring.datasource.password=testpass \
      clean test

(You can still keep the env: keys if you want, but passing -D ensures the JVM running tests receives the properties.)

Link to the workflow file you will edit:
https://github.com/niclesanti/ProyectoGastos/blob/461505e4afe69699e32d537954363e5663f014af/.github/workflows/ci.yml

2) (Recommended longer-term) Make tests explicitly read env vars (and fail fast if not set)
- Add test property resolution that defaults to environment variables so local dev and CI behave the same. In src/test/resources/application.properties (or application-test.properties) do:

spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/testdb}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:testuser}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:testpass}

This ensures Spring uses env variables when present and falls back to sane defaults for local dev.

3) Optional improvements
- Add a simple wait step to ensure Postgres is healthy before running tests (usually not necessary because service health checks do work, but can help flaky starts):

- name: Wait for Postgres
  run: |
    for i in {1..30}; do
      pg_isready -h localhost -p 5432 && break
      sleep 1
    done

- Or increase the postgres service health-retries in the workflow.

Why this will fix it
- The logs show the DB user was "root" during connection attempts, which only happens when the JDBC driver did not receive a username (it uses the OS username). Passing -Dspring.datasource.username (or making sure application-test.properties reads the env var) ensures the test JVM supplies the correct username (testuser) and password (testpass) and Postgres will accept the connection.

If you want, I can produce a full patch diff for .github/workflows/ci.yml that applies the -D changes, or prepare the test properties file snippet to add to src/test/resources.
# Plan: Integración de JaCoCo en el Backend Spring Boot

## Resumen Ejecutivo

Integrar el plugin JaCoCo en el proyecto backend para medir y controlar la cobertura de código de los tests automatizados. El plan incluye configuración Maven, exclusiones para código generado (Lombok/MapStruct), umbrales de cobertura, integración CI/CD y uso local.

---

## 1. Análisis del Estado Actual

### Archivos a modificar
| Archivo | Cambio |
|---------|--------|
| `backend/pom.xml` (líneas 133-184) | Agregar plugin JaCoCo en `<plugins>` |
| `.github/workflows/ci.yml` (líneas 31-34) | Agregar generación de reporte + upload artifact |
| `.github/workflows/cy.yml` (líneas 29-32) | Agregar `mvn verify` con `jacoco:check` + upload |

### Stack confirmado
- **Java 21** / Spring Boot 3.5.3 / Maven (wrapper)
- **Lombok** 1.18.38 — `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder` en entidades JPA
- **MapStruct** 1.5.5.Final — 13 mappers con `@Mapper(config = MapstructConfig.class)`
- **Tests**: JUnit 5 + Mockito, H2 in-memory, Flyway deshabilitado en test profile
- **Docker**: multi-stage build (`maven:3.9-eclipse-temurin-21` → `eclipse-temurin:21-jre-alpine`), tests skipped en Dockerfile
- **CI**: `./mvnw clean test` en `ci.yml` y `cd.yml`

---

## 2. Configuración JaCoCo en `backend/pom.xml`

### 2.1. Versión de JaCoCo

Usar **JaCoCo 0.8.12** (última estable, soporte completo Java 21 con records, sealed classes, etc.).

> Spring Boot 3.5.3 parent no gestiona la versión de JaCoCo, así que se especifica explícitamente.

### 2.2. Ubicación en el POM

Agregar el plugin **dentro de `<build><plugins>`** (no en `<pluginManagement>`) para que se ejecute automáticamente en el ciclo de vida `test`. Se coloca **después del `dockerfile-maven-plugin`** (antes del cierre de `</plugins>`) para mantener el orden lógico: Spring Boot → Compiler → Docker → JaCoCo.

### 2.3. Configuración XML completa

Insertar entre la línea 183 (cierre del plugin dockerfile) y la línea 184 (cierre de `</plugins>`):

```xml
<!-- JaCoCo - Coverage analysis -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <!-- Prepara el agente JaCoCo para inyectar en los tests -->
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <!-- Genera el reporte de cobertura después de los tests -->
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <!-- Verifica umbrales mínimos de cobertura -->
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.50</minimum>
                            </limit>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.40</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
    <configuration>
        <excludes>
            <!-- Lombok generated code -->
            <exclude>**/*_*</exclude>
            <!-- Model classes with Lombok annotations (generated getters/setters/equals/hashCode) -->
            <exclude>com/campito/backend/model/**</exclude>
            <!-- MapStruct generated mapper implementations -->
            <exclude>com/campito/backend/mapper/**/*Impl_*</exclude>
        </excludes>
    </configuration>
</plugin>
```

### 2.4. Explicación de las Exclusiones

| Exclusión | Razón |
|-----------|-------|
| `**/*_*` | Patrón general para archivos generados con guión bajo (Lombok y MapStruct generan `*_` o `*Impl_`) |
| `com/campito/backend/model/**` | Entidades JPA con `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` — los getters, setters, equals, hashCode, toString, builder y constructor son 100% generados. Incluirlos distorsiona la métrica de cobertura real. |
| `com/campito/backend/mapper/**/*Impl_*` | Implementaciones generadas por MapStruct (ej: `TransaccionMapperImpl_`). Son código sintetizado a partir de las anotaciones `@Mapping`. |

### 2.5. Configuración de Reportes

El plugin `report` genera automáticamente:
- **HTML**: `backend/target/site/jacoco/index.html` (para revisión local)
- **XML**: `backend/target/site/jacoco/jacoco.xml` (para herramientas de CI como GitHub, SonarQube)

No se necesita configuración adicional para formatos — JaCoCo genera ambos por defecto.

---

## 3. Configuración de Maven Surefire

### 3.1. Análisis

El `spring-boot-starter-parent` 3.5.3 incluye `maven-surefire-plugin` 3.5.2 que ya tiene soporte completo para JUnit 5 + Mockito. **No se requiere configuración adicional de Surefire.**

### 3.2. Verificación

El `spring-boot-starter-test` incluye:
- `junit-jupiter` (JUnit 5)
- `mockito-core` + `mockito-junit-jupiter`
- `assertj`

Surefire detecta automáticamente los tests por la convención `*Test.java` o `Test*.java`.

---

## 4. Integración CI/CD

### 4.1. Cambios en `ci.yml`

Reemplazar las líneas 31-34 del workflow actual:

```yaml
    - name: Ejecutar tests con cobertura
      run: |
        cd backend
        ./mvnw clean verify
      
    - name: Subir reporte de cobertura
      if: always()
      uses: actions/upload-artifact@v4
      with:
        name: jacoco-coverage-report
        path: backend/target/site/jacoco/
        retention-days: 30
```

**Cambios clave:**
- `clean test` → `clean verify` — El goal `verify` ejecuta JaCoCo `check` (que valida los umbrales). Si la cobertura cae por debajo del mínimo, el build falla.
- Se agrega paso para subir el reporte HTML como artifact de GitHub Actions (retención de 30 días).
- `if: always()`确保 el reporte se suba incluso si el check de umbrales falla.

### 4.2. Cambios en `cd.yml`

Reemplazar las líneas 29-32 del job `test`:

```yaml
    - name: Ejecutar tests con verificación de cobertura
      run: |
        cd backend
        ./mvnw clean verify
    
    - name: Subir reporte de cobertura
      if: always()
      uses: actions/upload-artifact@v4
      with:
        name: jacoco-coverage-report-prod
        path: backend/target/site/jacoco/
        retention-days: 90
```

**Nota:** En CD se usa retención de 90 días para propósitos de auditoría.

---

## 5. Umbrales de Cobertura

### 5.1. Recomendación para App de Finanzas Personales

| Métrica | Umbral Mínimo | Razón |
|---------|---------------|-------|
| **Líneas (LINE)** | 50% | App personal con 7 tests de servicio. Umbral realista que se puede incrementar gradualmente. |
| **Branches (BRANCH)** | 40% | Los servicios tienen condicionales (if/else) y validaciones. 40% es alcanzable sin tests exhaustivos de edge cases. |

### 5.2. Razonamiento

- Con 6 tests de servicio + 1 test de contexto, la cobertura actual probablemente ronda 30-45%.
- Un umbral del 50% en líneas permite crecer orgánicamente sin romper el CI.
- Si se quiere un umbral más estricto (70%+), primero se necesitan tests de controller y repository.

### 5.3. Evolución Sugerida

| Fase | Líneas | Branches | Criterio |
|------|--------|----------|----------|
| **Actual** (este plan) | 50% | 40% | Tests de servicio existentes |
| **Corto plazo** | 65% | 55% | Agregar tests de controller |
| **Mediano plazo** | 80% | 70% | Tests de integración + edge cases |

---

## 6. Uso Local

### 6.1. Generar Reporte

```bash
# Desde backend/
./mvnw clean test              # Genera reporte automáticamente
# Reporte HTML: target/site/jacoco/index.html
```

### 6.2. Verificar Umbrales

```bash
./mvnw verify                  # Ejecuta tests + jacoco:check
# Si la cobertura es menor al mínimo, el build falla
```

### 6.3. Solo Generar Reporte (sin tests)

```bash
./mvnw jacoco:report           # Solo genera el reporte (requiere datos de ejecución previos)
```

### 6.4. Abrir Reporte Local

```bash
# Windows
start target\site\jacoco\index.html

# Linux/Mac
open target/site/jacoco/index.html
```

---

## 7. Edge Cases

### 7.1. Lombok + JaCoCo

**Problema**: Lombok genera bytecode en tiempo de compilación. JaCoCo instrumenta bytecode. Si JaCoCo intenta instrumentar los métodos generados por Lombok, puede producir:
- Errores de `ClassNotFoundException`
- Cobertura inflada (código generado que nunca se "cubre" manualmente)

**Solución**: Las exclusiones en la configuración (punto 2.3) excluyen:
- `com/campito/backend/model/**` — Todas las entidades con `@Data`, `@Builder`, etc.
- `**/*_*` — Patrón general para archivos generados

### 7.2. H2 + JaCoCo

**Problema**: Ninguno. JaCoCo opera a nivel de bytecode Java, no a nivel de base de datos. Los tests con H2 in-memory funcionan normalmente con JaCoCo.

### 7.3. Docker Build

**Problema**: Si JaCoCo se ejecuta durante `mvn package`, el agente instrumentado queda en el JAR, aumentando el tamaño y potencialmente causando overhead.

**Solución actual**: El Dockerfile ya usa `-Dmaven.test.skip=true`, lo que evita que JaCoCo se ejecute durante el build Docker. **No se necesita ningún cambio en el Dockerfile.**

### 7.4. Flyway Deshabilitado en Tests

**Problema**: Ninguno. Flyway y JaCoCo son completamente independientes. Flyway gestiona el esquema de BD; JaCoCo mide cobertura de código.

### 7.5. Test Profile

**Problema**: Ninguno. JaCoCo instrumenta el bytecode de todas las clases compiladas, independientemente del profile activo. Los tests con H2 (test profile) ejecutan el código instrumentado correctamente.

---

## 8. Resumen de Cambios

### Archivo: `backend/pom.xml`
- **Línea 183-184**: Insertar bloque JaCoCo plugin (aprox. 50 líneas XML)
- **Resultado**: 3 ejecuciones: `prepare-agent`, `report`, `check`
- **Exclusiones**: model, mappers, archivos generados

### Archivo: `.github/workflows/ci.yml`
- **Líneas 31-34**: Cambiar `clean test` → `clean verify`
- **Línea 34+**: Agregar paso de upload artifact (jacoco-coverage-report)

### Archivo: `.github/workflows/cd.yml`
- **Líneas 29-32**: Cambiar `clean test` → `clean verify`
- **Línea 32+**: Agregar paso de upload artifact (jacoco-coverage-report-prod)

### Sin cambios necesarios en:
- `backend/Dockerfile` — Ya usa `-Dmaven.test.skip=true`
- `backend/src/test/` — Tests existentes funcionan sin modificaciones
- `backend/src/main/` — Código fuente no necesita cambios

---

## 9. Validación del Plan

### Checks previos a merge:
1. `./mvnw clean verify` pasa con cobertura >= 50% líneas, >= 40% branches
2. `target/site/jacoco/index.html` se genera correctamente
3. Docker build sigue funcionando: `docker build -t test-backend ./backend`
4. CI workflow pasa con el artifact upload
5. Los tests existentes (7 archivos) pasan sin cambios

### Comandos de verificación:
```bash
cd backend
./mvnw clean verify                    # Build completo con verificación
ls target/site/jacoco/index.html       # Verificar que el reporte existe
cat target/site/jacoco/jacoco.xml | head -5  # Verificar reporte XML
```

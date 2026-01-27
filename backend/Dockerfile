# --- Etapa 1: Construcción (Builder) ---
# Usamos una imagen base que tenga Java JDK. La de Maven sirve perfecto.
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# --- INICIO DE CAMBIOS PARA USAR EL WRAPPER ---

# 1. Copiar los archivos del Maven Wrapper
COPY .mvn/ .mvn
COPY mvnw .

# 2. Dar permisos de ejecución al script del wrapper (muy importante en Linux/Docker)
RUN chmod +x mvnw

# --- FIN DE CAMBIOS ---

# Copiamos primero el pom.xml para aprovechar la caché de Docker
COPY pom.xml .

# 3. Usamos el wrapper para ejecutar los comandos de Maven
RUN ./mvnw dependency:go-offline

# Copiamos el resto del código fuente del backend
COPY src ./src

# Ejecutamos el comando de empaquetado usando el wrapper (sin compilar tests)
RUN ./mvnw package -Dmaven.test.skip=true

# --- Etapa 2: Ejecución (Runner) ---
# Esta etapa no cambia. Sigue siendo igual de ligera y eficiente.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

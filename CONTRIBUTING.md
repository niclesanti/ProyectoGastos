# Guía de Contribución

¡Gracias por tu interés en contribuir a ProyectoGastos! 🎉

## 📋 Tabla de Contenidos

- [Código de Conducta](#código-de-conducta)
- [¿Cómo puedo contribuir?](#cómo-puedo-contribuir)
- [Proceso de Desarrollo](#proceso-de-desarrollo)
- [Estándares de Código](#estándares-de-código)
- [Convenciones de Commits](#convenciones-de-commits)
- [Pull Requests](#pull-requests)

## 📜 Código de Conducta

Este proyecto se adhiere a un código de conducta. Al participar, se espera que mantengas este código. Por favor reporta comportamientos inaceptables.

## 🤝 ¿Cómo puedo contribuir?

### Reportar Bugs

- Usa el template de issue para bugs
- Describe claramente el problema y los pasos para reproducirlo
- Incluye logs, capturas de pantalla si son relevantes
- Menciona la versión donde ocurre el bug

### Sugerir Mejoras

- Usa el template de issue para features
- Explica claramente el problema que resuelve
- Describe la solución propuesta
- Considera alternativas

### Contribuir Código

1. Fork el repositorio
2. Crea una rama desde `develop`
3. Haz tus cambios
4. Escribe o actualiza tests
5. Asegúrate que todos los tests pasen
6. Haz commit de tus cambios
7. Push a tu fork
8. Abre un Pull Request

## 🔄 Proceso de Desarrollo

### Configuración del Entorno

```bash
# Clonar el repositorio
git clone https://github.com/niclesanti/ProyectoGastos.git
cd ProyectoGastos

# Instalar dependencias
cd backend
./mvnw clean install

# Ejecutar tests
./mvnw test

# Ejecutar la aplicación
docker-compose up -d --build
```

### Estructura de Ramas

- `main`: Producción estable
- `develop`: Rama de desarrollo principal
- `feature/*`: Nuevas características
- `bugfix/*`: Corrección de bugs
- `hotfix/*`: Correcciones urgentes de producción

### Workflow

```
main
  ↑
  └── develop
        ↑
        ├── feature/nueva-funcionalidad
        ├── bugfix/corregir-error
        └── feature/otra-funcionalidad
```

## 💻 Estándares de Código

### Java

- Sigue las convenciones de Java
- Usa nombres descriptivos para variables y métodos
- Mantén métodos pequeños y enfocados
- Documenta código complejo con comentarios
- Usa Optional en lugar de null cuando sea apropiado


### Testing

- Escribe tests unitarios para nueva funcionalidad
- Mantén cobertura de código > 80%
- Usa nombres descriptivos para tests
- Sigue el patrón AAA (Arrange, Act, Assert)

```java
@Test
@DisplayName("Debería crear una nueva transacción correctamente")
void deberiaCrearNuevaTransaccionCorrectamente() {
    // Arrange
    TransaccionDTO dto = crearTransaccionDTO();
    
    // Act
    Transaccion resultado = transaccionService.crear(dto);
    
    // Assert
    assertNotNull(resultado.getId());
    assertEquals(dto.getMonto(), resultado.getMonto());
}
```

### SQL y Migraciones

- Usa migraciones de Flyway versionadas
- Nunca modifiques migraciones ya aplicadas
- Incluye scripts de rollback cuando sea posible
- Usa nombres descriptivos: `V{version}__{descripcion}.sql`

## 📝 Convenciones de Commits

Seguimos [Conventional Commits](https://www.conventionalcommits.org/):

```
<tipo>(<scope>): <descripción>

[cuerpo opcional]

[footer opcional]
```

### Tipos:

- `feat`: Nueva característica
- `fix`: Corrección de bug
- `docs`: Cambios en documentación
- `style`: Formato, punto y coma faltante, etc.
- `refactor`: Refactorización de código
- `test`: Agregar o modificar tests
- `chore`: Mantenimiento, actualizar dependencias
- `perf`: Mejora de rendimiento
- `ci`: Cambios en CI/CD

### Ejemplos:

```bash
feat(transacciones): agregar filtro por fecha
fix(auth): corregir validación de token expirado
docs(readme): actualizar instrucciones de instalación
test(cuentas): agregar tests para servicio de cuentas
```

## 🔀 Pull Requests

### Antes de Enviar

- [ ] Los tests pasan localmente
- [ ] El código sigue los estándares del proyecto
- [ ] Has agregado/actualizado tests si es necesario
- [ ] Has actualizado la documentación
- [ ] El commit sigue las convenciones
- [ ] Has probado los cambios manualmente

### Proceso de Revisión

1. **Automático**: Los workflows de CI deben pasar
   - Tests unitarios
   - Build de Docker
   - Análisis de código

2. **Manual**: Al menos un revisor debe aprobar
   - Código cumple estándares
   - Tests son adecuados
   - Documentación está actualizada

3. **Merge**: Una vez aprobado
   - Squash commits si hay muchos commits pequeños
   - Merge a `develop`
   - Delete la rama de feature

### Descripción del PR

Usa el template proporcionado e incluye:

- **Qué** cambios se hicieron
- **Por qué** se hicieron estos cambios
- **Cómo** se probaron los cambios
- **Referencias** a issues relacionados
- **Capturas** de pantalla si hay cambios visuales

## 🐛 Debugging

### Logs

```bash
# Ver logs del backend
docker-compose logs -f backend

# Ver logs de la base de datos
docker-compose logs -f postgres
```

### Debugging en IDE

Configura tu IDE para debug remoto:
- Puerto: 5005
- Host: localhost

## 📚 Recursos

- [Documentación de Spring Boot](https://spring.io/projects/spring-boot)
- [Guía de Java Code Conventions](https://www.oracle.com/java/technologies/javase/codeconventions-contents.html)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Conventional Commits](https://www.conventionalcommits.org/)

## ❓ Preguntas

Si tienes preguntas:
1. Revisa la documentación
2. Busca en issues existentes
3. Abre un issue con la etiqueta `question`
4. Únete a las discusiones del proyecto

## 🙏 Agradecimientos

¡Gracias por contribuir a ProyectoGastos! Cada contribución, grande o pequeña, es valiosa.

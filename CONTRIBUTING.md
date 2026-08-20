# 🤝 Guía de Contribución

¡Gracias por tu interés en contribuir al Sistema de Gestión de Finanzas Personales! Esta guía te ayudará a configurar tu entorno de desarrollo y entender el flujo de trabajo del proyecto.

---

## 📋 Tabla de Contenidos

- [Código de Conducta](#código-de-conducta)
- [¿Cómo Puedo Contribuir?](#cómo-puedo-contribuir)
- [Configuración del Entorno de Desarrollo](#configuración-del-entorno-de-desarrollo)
- [Flujo de Trabajo con Git](#flujo-de-trabajo-con-git)
- [Guía de Estilo de Código](#guía-de-estilo-de-código)
- [Flujo de Trabajo con Docker](#flujo-de-trabajo-con-docker)
- [Testing](#testing)
- [Proceso de Pull Request](#proceso-de-pull-request)

---

## 📜 Código de Conducta

Este proyecto se adhiere a un código de conducta profesional. Al participar, se espera que mantengas un ambiente respetuoso y colaborativo.

### Principios Básicos
- Sé respetuoso con otros colaboradores
- Acepta críticas constructivas
- Enfócate en lo que es mejor para la comunidad
- Muestra empatía hacia otros miembros de la comunidad

---

## 🎯 ¿Cómo Puedo Contribuir?

### Reportar Bugs

Si encuentras un bug, por favor crea un issue con:
- **Descripción clara** del problema
- **Pasos para reproducir** el comportamiento
- **Comportamiento esperado** vs **comportamiento actual**
- **Screenshots** (si aplica)
- **Información del entorno** (OS, versión de navegador, etc.)

### Sugerir Mejoras

Las sugerencias son bienvenidas. Para proponer una mejora:
- Verifica que no exista un issue similar
- Describe claramente la mejora propuesta
- Explica por qué sería útil para el proyecto

### Implementar Features

Revisa los issues etiquetados como:
- `good first issue` - Ideal para nuevos colaboradores
- `help wanted` - Necesitamos ayuda con estos
- `enhancement` - Nuevas características

---

## ⚙️ Configuración del Entorno de Desarrollo

### Prerrequisitos

Asegúrate de tener instalado:

- **Git** (2.40+)
- **Docker** y **Docker Compose** (última versión)
- **Java 21** 
- **Node.js 18+** y **npm** 
- Un editor de código (recomendado: **VS Code** o **Cursor**)

### 1. Fork y Clonación

```bash
# 1. Haz fork del repositorio desde GitHub

# 2. Clona tu fork
git clone https://github.com/niclesanti/ProyectoCampo.git
cd ProyectoCampo

# 3. Agrega el repositorio original como upstream
git remote add upstream https://github.com/niclesanti/ProyectoCampo.git

# 4. Verifica los remotes
git remote -v
```

### 2. Configuración de Variables de Entorno

Crea un archivo `.env` en la raíz del proyecto:

```env
# Configuración de Base de Datos PostgreSQL
DB_NAME=campito_db
DB_USER=postgres
DB_PASSWORD=postgres123

# Configuración de pgAdmin
PGADMIN_EMAIL=admin@campito.com
PGADMIN_PASSWORD=admin123

# Spring Boot
SPRING_PROFILES_ACTIVE=dev

# Google OAuth2 - Obtener en: https://console.cloud.google.com
GOOGLE_CLIENT_ID=tu_client_id
GOOGLE_CLIENT_SECRET=tu_client_secret
```

> ⚠️ **Importante**: Nunca subas el archivo `.env` al repositorio. Ya está incluido en `.gitignore`.

### 3. Levantar el Entorno de Desarrollo

```bash
# Construir y levantar todos los servicios
docker-compose up -d --build

# Verificar que los servicios estén corriendo
docker-compose ps
```

**Servicios disponibles:**

| Servicio | URL | Descripción |
|----------|-----|-------------|
| Frontend | http://localhost:3100 | Aplicación React |
| Backend | http://localhost:8080 | API REST Spring Boot |
| Swagger UI | http://localhost:8080/swagger-ui/index.html | Documentación API |
| PostgreSQL | localhost:5432 | Base de datos |
| pgAdmin | http://localhost:5050 | Admin de BD |

---

## 🔄 Flujo de Trabajo con Git

### Sincronizar con Upstream

Antes de empezar a trabajar, sincroniza tu fork:

```bash
# Obtener cambios del repositorio original
git fetch upstream

# Cambiar a tu rama main
git checkout main

# Mergear cambios de upstream
git merge upstream/main

# Actualizar tu fork en GitHub
git push origin main
```

### Crear una Rama para tu Feature

```bash
# Crear y cambiar a una nueva rama
git checkout -b feature/nombre-descriptivo

# Ejemplos:
# git checkout -b feature/add-expense-categories
# git checkout -b fix/dashboard-loading-issue
# git checkout -b docs/update-readme
```

**Convención de nombres de ramas:**
- `feature/` - Nuevas características
- `fix/` - Corrección de bugs
- `docs/` - Cambios en documentación
- `refactor/` - Refactorización de código
- `test/` - Añadir o mejorar tests

### Commits

Usa mensajes de commit descriptivos siguiendo [Conventional Commits](https://www.conventionalcommits.org/):

```bash
# Formato: <tipo>(<alcance>): <descripción>

git commit -m "feat(dashboard): add monthly expense chart"
git commit -m "fix(api): resolve credit card calculation error"
git commit -m "docs(readme): update installation instructions"
git commit -m "refactor(service): simplify transaction logic"
```

**Tipos de commits:**
- `feat` - Nueva característica
- `fix` - Corrección de bug
- `docs` - Cambios en documentación
- `style` - Formateo, punto y coma faltantes, etc.
- `refactor` - Refactorización de código
- `test` - Añadir tests
- `chore` - Cambios en build, dependencias, etc.

---

## 📝 Guía de Estilo de Código

### Backend (Java)

- **Estilo**: Sigue las convenciones de Java (Google Java Style Guide)
- **Nombrado**:
  - Clases: `PascalCase` (ej: `TransaccionService`)
  - Métodos: `camelCase` (ej: `obtenerTransaccionesPorFecha()`)
  - Constantes: `UPPER_SNAKE_CASE` (ej: `MAX_RETRY_ATTEMPTS`)
- **Documentación**: Usa JavaDoc para clases y métodos públicos
- **Lombok**: Usa anotaciones de Lombok para reducir boilerplate
- **DTOs**: Separa claramente entre entidades JPA y DTOs

```java
/**
 * Servicio para gestión de transacciones financieras.
 */
@Service
@RequiredArgsConstructor
public class TransaccionServiceImpl implements TransaccionService {
    
    private final TransaccionRepository repository;
    
    /**
     * Obtiene todas las transacciones de un espacio de trabajo.
     *
     * @param espacioId ID del espacio de trabajo
     * @return Lista de transacciones
     */
    @Override
    public List<TransaccionDTOResponse> obtenerPorEspacio(Long espacioId) {
        // Implementación
    }
}
```

### Frontend (React + TypeScript)

- **Estilo**: Sigue las convenciones de React y TypeScript
- **Nombrado**:
  - Componentes: `PascalCase` (ej: `TransactionModal.tsx`)
  - Hooks: `camelCase` con prefijo `use` (ej: `useDashboardStats.ts`)
  - Constantes: `UPPER_SNAKE_CASE`
- **Componentes funcionales**: Usa siempre componentes funcionales con hooks
- **TypeScript**: Define tipos e interfaces explícitos
- **Tailwind CSS**: Usa clases de Tailwind, evita CSS inline

```typescript
interface TransactionModalProps {
  isOpen: boolean;
  onClose: () => void;
  workspaceId: number;
}

export function TransactionModal({ isOpen, onClose, workspaceId }: TransactionModalProps) {
  const [loading, setLoading] = useState(false);
  
  // Implementación
  
  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      {/* JSX */}
    </Dialog>
  );
}
```

### SQL (Migraciones Flyway)

- **Nombrado**: `V{version}__{descripcion}.sql`
- Usa siempre transacciones
- Incluye rollback manual si es necesario
- Documenta cambios complejos

```sql
-- V7__add_notification_system.sql

BEGIN;

CREATE TABLE notificaciones (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    mensaje TEXT NOT NULL,
    leida BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notificaciones_usuario ON notificaciones(usuario_id);

COMMIT;
```

---

## 🐳 Flujo de Trabajo con Docker

### Desarrollo Diario

#### Modificar el Backend

Cuando hagas cambios en el código Java:

```bash
# Opción 1: Reconstruir solo el backend
docker-compose up -d --build backend

# Opción 2: Detener, reconstruir y levantar
docker-compose stop backend
docker-compose build backend
docker-compose up -d backend

# Ver logs en tiempo real
docker-compose logs -f backend
```

#### Modificar el Frontend

Cuando hagas cambios en React:

```bash
# El frontend tiene hot-reload activado en desarrollo
# Los cambios se reflejan automáticamente

# Si necesitas reconstruir:
docker-compose up -d --build frontend

# Ver logs
docker-compose logs -f frontend
```

#### Comandos Útiles

```bash
# Ver estado de todos los servicios
docker-compose ps

# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f backend
docker-compose logs -f db

# Reiniciar un servicio
docker-compose restart backend

# Detener todos los servicios
docker-compose stop

# Detener y eliminar contenedores (preserva volúmenes)
docker-compose down

# Acceder al shell de un contenedor
docker-compose exec backend bash
docker-compose exec db psql -U postgres -d campito_db
```

### Limpiar y Empezar de Cero

#### Limpiar Base de Datos

⚠️ **ADVERTENCIA**: Esto eliminará todos los datos.

```bash
# Detener servicios y eliminar volúmenes
docker-compose down -v

# Levantar servicios desde cero
docker-compose up -d --build
```

#### Limpiar Completamente

```bash
# 1. Detener y eliminar todo
docker-compose down -v

# 2. Eliminar imágenes (opcional)
docker rmi campito/backend campito/frontend

# 3. Limpiar sistema Docker (opcional, libera espacio)
docker system prune -a --volumes

# 4. Reconstruir desde cero
docker-compose up -d --build
```

### Solución de Problemas Comunes

#### Puerto ya en uso

```powershell
# Ver qué proceso usa el puerto
netstat -ano | findstr :8080

# Cambiar el puerto en compose.yaml
# O detener el proceso que usa el puerto
```

#### Backend no conecta a Base de Datos

```bash
# 1. Verificar que PostgreSQL esté corriendo
docker-compose logs db

# 2. Verificar variables de entorno
docker-compose exec backend env | grep DB_

# 3. Reiniciar backend
docker-compose restart backend
```

#### Contenedores Huérfanos

```bash
docker-compose down --remove-orphans
```

---

## 🧪 Testing

### Backend Tests

```bash
# Desde el host (requiere Java 21)
cd backend
./mvnw test

# O con Maven Wrapper en Windows
.\mvnw.cmd test

# Dentro del contenedor Docker
docker-compose exec backend ./mvnw test
```

### Frontend Tests

```bash
# Desde el host (requiere Node.js)
cd frontend
npm test

# Ejecutar en modo watch
npm test -- --watch

# Con coverage
npm test -- --coverage
```

### Convenciones de Testing

- **Backend**: Tests en `src/test/java`
- **Frontend**: Tests junto a los archivos (ej: `Component.test.tsx`)
- **Cobertura mínima**: Apuntar a >80% en lógica de negocio
- **Mocks**: Usa Mockito (backend) y Jest mocks (frontend)

---

## 🔀 Proceso de Pull Request

### Antes de Enviar

Asegúrate de:

1. ✅ Tu código sigue la guía de estilo
2. ✅ Has añadido/actualizado tests
3. ✅ Todos los tests pasan
4. ✅ Has actualizado la documentación si es necesario
5. ✅ Tu commit sigue Conventional Commits
6. ✅ Has sincronizado con `upstream/main`

```bash
# Verificar tests
cd backend && ./mvnw test
cd frontend && npm test

# Sincronizar con upstream
git fetch upstream
git rebase upstream/main

# Push a tu fork
git push origin feature/tu-feature
```

### Crear el Pull Request

1. Ve a tu fork en GitHub
2. Haz clic en "Compare & pull request"
3. **Título**: Usa formato de Conventional Commits
   - `feat: add expense category management`
   - `fix: resolve dashboard loading issue`
4. **Descripción**: Completa la plantilla

```markdown
## Descripción
Breve descripción de los cambios realizados.

## Tipo de Cambio
- [ ] Bug fix
- [ ] Nueva feature
- [ ] Breaking change
- [ ] Documentación

## ¿Cómo se puede probar?
Pasos para probar los cambios:
1. Levantar el entorno con `docker-compose up`
2. Ir a http://localhost:3000/...
3. ...

## Checklist
- [ ] Mi código sigue la guía de estilo
- [ ] He realizado self-review de mi código
- [ ] He comentado código complejo
- [ ] He actualizado la documentación
- [ ] Mis cambios no generan warnings
- [ ] He añadido tests que prueban mi fix/feature
- [ ] Tests nuevos y existentes pasan localmente
```

### Revisión de Código

- Responde a los comentarios de manera constructiva
- Realiza cambios solicitados en commits adicionales
- Mantén la conversación profesional
- Una vez aprobado, el PR será mergeado

---

## 📚 Recursos Adicionales

### Documentación del Proyecto
- [README Principal](README.md)
- [Backend README](backend/README.md)
- [Frontend README](frontend/README_FRONTEND.md)
- [Diagrama UML](docs/DiagramaDeClasesUML.puml)

### Tecnologías
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [Tailwind CSS](https://tailwindcss.com/docs)
- [shadcn/ui Components](https://ui.shadcn.com/)

### Herramientas
- [Docker Documentation](https://docs.docker.com/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Git Flow](https://nvie.com/posts/a-successful-git-branching-model/)

---

## ❓ ¿Necesitas Ayuda?

Si tienes preguntas:

1. Revisa la documentación existente
2. Busca en issues cerrados si alguien tuvo el mismo problema
3. Abre un issue con la etiqueta `question`
4. Contacta al mantenedor: niclesantiago@gmail.com

---

## 🙏 Agradecimientos

Gracias por contribuir al proyecto. Cada contribución, grande o pequeña, es valiosa y apreciada.

---

<div align="center">

**¡Happy coding! 🚀**

</div>

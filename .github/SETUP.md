# Configuración de GitHub CI/CD

Esta guía te ayudará a configurar GitHub CI/CD para ProyectoGastos.

## 📋 Archivos Creados

### Workflows de GitHub Actions

1. **`.github/workflows/ci.yml`** - Integración Continua
   - Ejecuta tests automáticamente en PRs a `develop` y `main`
   - Verifica que el build sea exitoso
   - Genera reportes de tests
   - Valida que Docker build funcione

2. **`.github/workflows/cd.yml`** - Despliegue Continuo
   - Construye y publica imágenes Docker
   - Crea releases en GitHub
   - Placeholder para despliegue a producción

3. **`.github/workflows/codeql.yml`** - Análisis de Seguridad
   - Escanea código Java y JavaScript
   - Ejecuta semanalmente y en cada PR
   - Reporta vulnerabilidades de seguridad

### Templates

4. **`.github/pull_request_template.md`**
   - Template estándar para todos los PRs
   - Checklist de calidad
   - Guía para descripción de cambios

5. **`.github/ISSUE_TEMPLATE/bug_report.yml`**
   - Formulario estructurado para reportar bugs
   - Campos obligatorios y opcionales
   - Clasificación por severidad

6. **`.github/ISSUE_TEMPLATE/feature_request.yml`**
   - Formulario para solicitar nuevas características
   - Priorización y categorización

7. **`.github/ISSUE_TEMPLATE/config.yml`**
   - Configuración general de issues

### Documentación

8. **`CONTRIBUTING.md`**
   - Guía completa para contribuidores
   - Estándares de código
   - Proceso de desarrollo

## 🚀 Pasos de Configuración

### 1. Configurar Secrets (Opcional)

Si planeas usar despliegue automático, configura estos secrets en GitHub:

```
Settings → Secrets and variables → Actions → New repository secret
```

**Secrets sugeridos:**
- `DOCKER_USERNAME`: Tu usuario de Docker Hub (si usas Docker Hub en lugar de GHCR)
- `DOCKER_PASSWORD`: Token de acceso de Docker Hub
- `DEPLOY_KEY`: SSH key para despliegue (si aplica)
- `AWS_ACCESS_KEY_ID`: Para despliegue en AWS (si aplica)
- `AWS_SECRET_ACCESS_KEY`: Para despliegue en AWS (si aplica)

**Nota**: El workflow CD usa GitHub Container Registry (GHCR) por defecto, que no requiere secrets adicionales.

### 2. Configurar Protección de Ramas

Recomendamos proteger las ramas principales:

#### Para `main`:
```
Settings → Branches → Add branch protection rule
```

**Configuración sugerida:**
- [x] Require pull request reviews before merging (1 revisor)
- [x] Require status checks to pass before merging
  - Marcar como obligatorios:
    - `test`
    - `build`
    - `code-quality`
- [x] Require branches to be up to date before merging
- [x] Require conversation resolution before merging
- [x] Do not allow bypassing the above settings
- [x] Restrict who can push to matching branches (solo maintainers)

#### Para `develop`:
```
Settings → Branches → Add branch protection rule
```

**Configuración sugerida:**
- [x] Require pull request reviews before merging (1 revisor)
- [x] Require status checks to pass before merging
  - `test`
  - `build`
- [x] Require conversation resolution before merging

### 3. Habilitar GitHub Pages (Opcional)

Para documentación automática:

```
Settings → Pages
Source: Deploy from a branch
Branch: main / docs
```

### 4. Configurar Environments

Para control de despliegues:

```
Settings → Environments → New environment
```

**Crear environment "production":**
- [x] Required reviewers: Agrega revisores
- [x] Wait timer: 5 minutos (opcional)
- [ ] Deployment branches: Only main

### 5. Configurar CodeQL

```
Settings → Code security and analysis
```

- [x] CodeQL analysis (ya configurado en el workflow)

### 7. Configurar Codecov (Opcional)

Para reportes de cobertura de código:

1. Ve a [codecov.io](https://codecov.io)
2. Conecta tu repositorio
3. Copia el token
4. Agrégalo como secret: `CODECOV_TOKEN`

### 8. Configurar Notificaciones

```
Settings → Notifications
```

Configura notificaciones para:
- [ ] Actions workflows
- [ ] Security alerts

## 📊 Badges para README

Agrega estos badges a tu [README.md](README.md):

```markdown
[![CI](https://github.com/YOUR_USERNAME/ProyectoGastos/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/ProyectoGastos/actions/workflows/ci.yml)
[![CD](https://github.com/YOUR_USERNAME/ProyectoGastos/actions/workflows/cd.yml/badge.svg)](https://github.com/YOUR_USERNAME/ProyectoGastos/actions/workflows/cd.yml)
[![CodeQL](https://github.com/YOUR_USERNAME/ProyectoGastos/actions/workflows/codeql.yml/badge.svg)](https://github.com/YOUR_USERNAME/ProyectoGastos/actions/workflows/codeql.yml)
[![codecov](https://codecov.io/gh/YOUR_USERNAME/ProyectoGastos/branch/main/graph/badge.svg)](https://codecov.io/gh/YOUR_USERNAME/ProyectoGastos)
```

## 🧪 Probar los Workflows

### Prueba CI:
```bash
# Crear una rama de feature
git checkout -b feature/test-ci

# Hacer un cambio
echo "# Test" >> test.md

# Commit y push
git add .
git commit -m "feat: test CI workflow"
git push origin feature/test-ci

# Crear PR desde GitHub UI
```

### Prueba CD:
```bash
# Merge a main (después de aprobar PR)
# El workflow CD se ejecutará automáticamente
```

### Prueba Release:
```bash
git checkout main
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

## 🔍 Monitoreo

### Ver Estado de Workflows

```
Actions tab en GitHub
```

Aquí puedes:
- Ver todos los runs
- Ver logs de cada step
- Re-ejecutar workflows fallidos
- Cancelar runs en progreso

### Security Alerts

```
Security tab → Code scanning alerts
Security tab → Code scanning alerts
```

## 🛠️ Personalización

### Modificar Frecuencia de Tests

En [.github/workflows/ci.yml](.github/workflows/ci.yml):

```yaml
on:
  schedule:
    - cron: '0 0 * * *'  # Ejecutar diariamente a medianoche
```

### Agregar Más Checks

```yaml
- name: Check code style
  run: ./mvnw checkstyle:check

- name: Run integration tests
  run: ./mvnw verify -P integration-tests
```

### Configurar Despliegue Real

Edita [.github/workflows/cd.yml](.github/workflows/cd.yml) en el job `deploy-production`:

**Ejemplo para AWS ECS:**
```yaml
- name: Deploy to ECS
  uses: aws-actions/amazon-ecs-deploy-task-definition@v1
  with:
    task-definition: task-definition.json
    service: my-service
    cluster: my-cluster
```

**Ejemplo para SSH:**
```yaml
- name: Deploy via SSH
  uses: appleboy/ssh-action@master
  with:
    host: ${{ secrets.HOST }}
    username: ${{ secrets.USERNAME }}
    key: ${{ secrets.SSH_KEY }}
    script: |
      cd /app
      docker-compose pull
      docker-compose up -d
```

## 📚 Recursos Adicionales

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitHub Environments](https://docs.github.com/en/actions/deployment/targeting-different-environments/using-environments-for-deployment)
- [Dependabot Documentation](https://docs.github.com/en/code-security/dependabot)
- [CodeQL Documentation](https://codeql.github.com/docs/)

## ✅ Checklist Final

Antes de considerar la configuración completa:

- [ ] Todos los workflows ejecutan exitosamente
- [ ] Protección de ramas configurada
- [ ] Secrets necesarios agregados
- [ ] Dependabot configurado con tu usuario
- [ ] Templates de PR e Issues funcionando
- [ ] Badges agregados al README
- [ ] Documentación actualizada
- [ ] CONTRIBUTING.md revisado por el equipo

## 🐛 Troubleshooting

### Tests fallan en CI pero pasan localmente

**Posibles causas:**
- Diferencias en variables de entorno
- Base de datos no configurada correctamente
- Tests dependientes del orden de ejecución

**Solución:**
```yaml
# Agregar más variables de entorno en el workflow
env:
  SPRING_PROFILES_ACTIVE: test
  TZ: UTC
```

### Docker build falla

**Posibles causas:**
- Dockerfile con rutas incorrectas
- Dependencias no instaladas

**Solución:**
Probar localmente:
```bash
docker build -t test ./backend
```

## 💡 Mejores Prácticas

1. **Commits pequeños y frecuentes**: Facilita code review
2. **Tests antes de push**: Evita fallos en CI
3. **Documentar cambios breaking**: En el PR y commit message
4. **Usar labels consistentemente**: Facilita organización
5. **Review de código obligatorio**: Mejora calidad del código
6. **Monitorear Security alerts**: Actúa rápido en vulnerabilidades

## 🎉 ¡Listo!

Tu proyecto ahora tiene un flujo de CI/CD profesional. Cada PR será:
- ✅ Testeado automáticamente
- ✅ Revisado por CodeQL
- ✅ Validado que el build funciona
- ✅ Revisado por al menos un desarrollador

Y cada merge a main:
- 🚀 Construirá una imagen Docker
- 📦 Creará un release (si es un tag)
- 🎯 Estará listo para despliegue

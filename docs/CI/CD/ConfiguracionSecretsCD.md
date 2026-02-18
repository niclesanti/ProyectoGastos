# 🔐 Guía de Configuración: GitHub Secrets para CD

Esta guía te explica paso a paso cómo configurar los **secretos de GitHub** necesarios para que el workflow de **CD (Continuous Deployment)** funcione correctamente y no expongas credenciales sensibles en tu código.

---

## ¿Qué son los GitHub Secrets?

Los **GitHub Secrets** son variables de entorno cifradas que GitHub utiliza para almacenar información sensible como:
- Contraseñas
- Tokens de acceso
- Claves SSH
- API Keys

**Ventajas:**
- No se muestran en los logs de GitHub Actions
- Solo son accesibles durante la ejecución del workflow
- Se almacenan de forma cifrada en los servidores de GitHub

---

## Secretos necesarios para ProyectoGastos

Debes configurar **5 secretos** en tu repositorio de GitHub:

| Secreto | Descripción | Ejemplo |
|---------|-------------|---------|
| `DOCKERHUB_USERNAME` | Tu nombre de usuario de Docker Hub | `tuusuario` |
| `DOCKERHUB_TOKEN` | Token de acceso de Docker Hub (NO tu contraseña) | `dckr_pat_xxxxxxxxxxxxx` |
| `ORACLE_SSH_HOST` | Dirección IP pública de tu servidor en Oracle Cloud | `123.45.67.89` |
| `ORACLE_SSH_USERNAME` | Usuario SSH del servidor (normalmente `ubuntu`) | `ubuntu` |
| `ORACLE_SSH_KEY` | Clave privada SSH completa para conectarte al servidor | `-----BEGIN RSA PRIVATE KEY-----...` |

---

## Paso 1: Crear un Token de Docker Hub

Para que GitHub Actions pueda subir imágenes a Docker Hub, necesitas un **Access Token** (NO uses tu contraseña).

### Pasos:

1. Ve a [Docker Hub](https://hub.docker.com/) e inicia sesión.
2. Haz clic en tu avatar (esquina superior derecha) → **Account Settings**.
3. En el menú lateral, selecciona **Security** → **New Access Token**.
4. Configura el token:
   - **Description:** `GitHub Actions - ProyectoGastos`
   - **Access permissions:** Selecciona `Read, Write, Delete`
5. Haz clic en **Generate**.
6. **Copia el token inmediatamente** (solo se muestra una vez).

---

## Paso 2: Obtener tu Clave SSH Privada

GitHub Actions necesita tu **clave privada SSH** para conectarse al servidor de Oracle Cloud.

### Opción A: Usar tu clave existente

Si ya tienes una clave SSH que usas para conectarte a Oracle Cloud:

**En Windows (PowerShell):**
```powershell
Get-Content ~\.ssh\id_rsa
```

**En Linux/Mac:**
```bash
cat ~/.ssh/id_rsa
```

**Copia TODO el contenido**, incluyendo:
```
-----BEGIN OPENSSH PRIVATE KEY-----
...todo el contenido...
-----END OPENSSH PRIVATE KEY-----
```

### Opción B: Crear una clave SSH nueva (Recomendado para mayor seguridad)

Si prefieres crear una clave específica solo para GitHub Actions:

```powershell
ssh-keygen -t rsa -b 4096 -C "github-actions-cd" -f $HOME\.ssh\github_actions_key
```

- Presiona **Enter** cuando te pida una frase de paso (déjala vacía).
- Esto generará dos archivos:
  - `github_actions_key` → Clave privada (para GitHub Secret)
  - `github_actions_key.pub` → Clave pública (para el servidor)

**Luego, añade la clave pública al servidor Oracle:**

```powershell
# 1. Ver el contenido de la clave pública
Get-Content $HOME\.ssh\github_actions_key.pub

# 2. Conectarte a tu servidor Oracle por SSH
ssh -i .\tu_llave.key ubuntu@IP_SERVIDOR

# 3. En el servidor, añadir la clave pública
echo "PEGA_AQUÍ_EL_CONTENIDO_DE_github_actions_key.pub" >> ~/.ssh/authorized_keys
```

---

## Paso 3: Configurar Secrets en GitHub

1. Ve a tu repositorio en GitHub: `https://github.com/tu_usuario/ProyectoGastos`
2. Haz clic en **Settings** (Configuración).
3. En el menú lateral izquierdo, selecciona **Secrets and variables** → **Actions**.
4. Haz clic en **New repository secret**.

### Añadir cada secreto:

#### 1. DOCKERHUB_USERNAME
- **Name:** `DOCKERHUB_USERNAME`
- **Value:** Tu nombre de usuario de Docker Hub (por ejemplo: `campito`)

#### 2. DOCKERHUB_TOKEN
- **Name:** `DOCKERHUB_TOKEN`
- **Value:** El token que generaste en el Paso 1

#### 3. ORACLE_SSH_HOST
- **Name:** `ORACLE_SSH_HOST`
- **Value:** La IP pública de tu servidor Oracle (por ejemplo: `123.45.67.89`)

#### 4. ORACLE_SSH_USERNAME
- **Name:** `ORACLE_SSH_USERNAME`
- **Value:** `ubuntu` (o el usuario SSH que uses)

#### 5. ORACLE_SSH_KEY
- **Name:** `ORACLE_SSH_KEY`
- **Value:** TODO el contenido de tu clave privada SSH (incluyendo `-----BEGIN` y `-----END`)

---

## Paso 4: Verificar la configuración

Una vez configurados los 5 secretos, deberías ver algo así en GitHub:

```
DOCKERHUB_USERNAME       Updated X minutes ago
DOCKERHUB_TOKEN          Updated X minutes ago
ORACLE_SSH_HOST          Updated X minutes ago
ORACLE_SSH_USERNAME      Updated X minutes ago
ORACLE_SSH_KEY           Updated X minutes ago
```

---

## Paso 5: Probar el despliegue automático

1. Haz un cambio pequeño en tu código (por ejemplo, un comentario en el backend).
2. Haz commit y push a la rama `main`:
   ```powershell
   git add .
   git commit -m "test: probar CD workflow"
   git push origin main
   ```
3. Ve a la pestaña **Actions** en tu repositorio de GitHub.
4. Deberías ver el workflow `CD - Deploy a Producción` ejecutándose.
5. Observa los logs para ver:
   - ✅ Tests pasando
   - ✅ Imagen Docker siendo construida y subida a Docker Hub
   - ✅ Despliegue en Oracle Cloud

---

## Solución de Problemas Comunes

### Error: "Permission denied (publickey)"
**Causa:** La clave SSH no es válida o no está autorizada en el servidor.

**Solución:**
1. Verifica que copiaste la clave privada completa (con `-----BEGIN` y `-----END`).
2. Asegúrate de que la clave pública correspondiente esté en `~/.ssh/authorized_keys` del servidor.

### Error: "denied: requested access to the resource is denied"
**Causa:** El token de Docker Hub no tiene permisos suficientes.

**Solución:**
1. Genera un nuevo token con permisos `Read, Write, Delete`.
2. Actualiza el secreto `DOCKERHUB_TOKEN` en GitHub.

### Error: "container springboot-campito-prod not found"
**Causa:** El nombre del contenedor en el workflow no coincide con el nombre real.

**Solución:**
1. Verifica el nombre del contenedor con `docker ps` en tu servidor Oracle.
2. Actualiza el archivo [cd.yml](.github/workflows/cd.yml#L76) con el nombre correcto.

---

## Seguridad y Mejores Prácticas

✅ **Nunca** subas las claves privadas o tokens al código fuente.
✅ **Siempre** usa GitHub Secrets para información sensible.
✅ **Rota** tus tokens y claves SSH periódicamente (cada 3-6 meses).
✅ **Limita** los permisos de los tokens solo a lo necesario.
✅ **Usa** claves SSH específicas para automatización (en lugar de tu clave personal).

---

## Recursos Adicionales

- [Documentación de GitHub Secrets](https://docs.github.com/es/actions/security-guides/encrypted-secrets)
- [Docker Hub Access Tokens](https://docs.docker.com/docker-hub/access-tokens/)
- [SSH Key Management](https://docs.github.com/es/authentication/connecting-to-github-with-ssh)

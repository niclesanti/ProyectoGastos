# 🆙 Despliegue en Producción

## 🏗️ Arquitectura de Producción (Multi-Cloud)

Para optimizar los recursos de la capa gratuita y garantizar alta disponibilidad, se implementó una arquitectura distribuida:

* **Base de Datos (Aiven):** PostgreSQL 17 administrado para evitar consumo de RAM en el servidor de aplicaciones.
* **Backend (Oracle Cloud):** Spring Boot 3.5.3 (Java 21) corriendo en contenedores Docker.
* **Frontend (Vercel):** SPA de React + TypeScript servida mediante CDN global.
* **Proxy Inverso (Caddy):** Gestión automática de certificados SSL (HTTPS) y ruteo.

---

## 1. Configuración de Networking (Oracle Cloud)

Antes de crear el servidor, se configuró el entorno de red virtual (VCN) para permitir el tráfico necesario:

1. **VCN Wizard:** Se utilizó el asistente para crear una VCN con conectividad a Internet.
2. **Security Lists (Firewall de Red):** Se añadieron las siguientes **Ingress Rules** (Reglas de entrada) para el bloque CIDR `0.0.0.0/0`:
* **Puerto 22 (TCP):** Acceso administrativo vía SSH.
* **Puerto 80 (TCP):** Tráfico HTTP para validación de certificados SSL.
* **Puerto 443 (TCP):** Tráfico HTTPS cifrado para la comunicación con el frontend.
* **Puerto 8080 (TCP):** Acceso directo al backend (solo para pruebas iniciales).


---

## 2. Instancia de Cómputo y Preparación del Servidor

Debido a la disponibilidad de recursos, se utilizó la siguiente configuración de hardware:

* **Imagen:** Ubuntu 24.04 LTS.
* **Shape:** `VM.Standard.E2.1.Micro` (AMD - 1 OCPU, 1 GB RAM).

### Configuración del Sistema Operativo (SSH)

Dada la limitación de 1 GB de RAM para un entorno Java 21, se realizaron los siguientes ajustes críticos:

1. **Creación de SWAP (Memoria Virtual):** Se añadieron 2 GB de espacio de intercambio para evitar errores *Out of Memory* (OOM).
```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

```


2. **Firewall Interno (iptables):** Ubuntu bloquea puertos por defecto; se habilitaron manualmente:
```bash
sudo iptables -I INPUT 6 -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -p tcp --dport 443 -j ACCEPT
sudo netfilter-persistent save

```

---

## 3. Persistencia de Datos (Aiven Console)

Se delegó la base de datos a Aiven para liberar carga procesadora en Oracle Cloud.

1. **Instancia:** PostgreSQL 17 (Free Tier).
2. **Seguridad:** Se configuró el **IP Filter** para permitir conexiones desde el servidor de Oracle o, temporalmente, desde `0.0.0.0/0`.
3. **SSL:** Se requiere el parámetro `sslmode=require` en la cadena de conexión de Spring Boot para garantizar la privacidad de los datos financieros.

---

## 4. Estrategia de Despliegue del Backend

Para no sobrecargar la CPU del servidor durante la compilación, se utilizó un flujo de **Docker Hub**:

1. **Build Local:** Se generó la imagen en el entorno de desarrollo local (PC).
`docker build -t tu_usuario/proyecto-gastos-backend:latest ./backend`.
2. **Push:** Se subió la imagen al registro de Docker Hub.
`docker push tu_usuario/proyecto-gastos-backend:latest`.
3. **Deploy en Cloud:** El servidor de Oracle descarga la imagen ya compilada mediante `docker-compose pull`.

### Orquestación y Variables de Entorno

Se configuró un archivo `.env` para inyectar los secretos sin exponerlos en el código:

| Variable | Descripción |
| --- | --- |
| `SPRING_DATASOURCE_URL` | URL de conexión a Aiven con SSL. |
| `JWT_SECRET` | Llave privada para la firma de tokens de seguridad. |
| `GOOGLE_CLIENT_ID` | Identificador para la autenticación OAuth2. |
| `JAVA_OPTS` | Limitación de memoria JVM (`-Xmx512m`) para estabilidad. |

---

## 5. Proxy Inverso y SSL (Caddy + DuckDNS)

Se implementó **Caddy** como servidor web frontal por su eficiencia en el uso de RAM comparado con Nginx.

* **DuckDNS:** Proporciona un subdominio gratuito vinculado a la IP estática de Oracle Cloud.
* **SSL Automático:** Caddy gestiona los certificados de Let's Encrypt sin intervención manual.
* **Caddyfile:**
```caddy
proyecto-gastos-backend.duckdns.org {
    reverse_proxy backend:8080
}

```

---

## 6. Despliegue del Frontend (Vercel)

El frontend se desplegó de forma independiente para aprovechar el renderizado optimizado y HTTPS nativo.

1. **Monorepo Config:** Se seleccionó el subdirectorio `/frontend` como raíz.
2. **Variables:** Se configuró `VITE_API_URL` apuntando al subdominio de DuckDNS mediante HTTPS.
3. **SPA Routing:** Se añadió el archivo `vercel.json` con **rewrites** para evitar errores 404 al refrescar páginas como `/movimientos`.

---

## 7. Configuración Final de Seguridad OAuth2

Para completar el ciclo de autenticación:

1. **Google Cloud Console:** Se actualizaron los orígenes autorizados con la URL de Vercel.
2. **Redirect URI:** Se configuró `https://proyecto-gastos-backend.duckdns.org/login/oauth2/code/google` para procesar el inicio de sesión de forma segura.

---


## 🛠️ Mantenimiento y Actualización del Backend

Este proceso permite aplicar cambios en la lógica del negocio o correcciones de errores en el entorno de producción sin comprometer la estabilidad del servidor.

### Fase 1: Preparación y Carga (En PC Local)

La compilación se realiza localmente para aprovechar la potencia de tu máquina y evitar el consumo de CPU/RAM en la instancia Micro de Oracle.

1. **Construir la nueva imagen:**
Abre tu terminal en la raíz del monorepo y ejecuta la construcción apuntando a la carpeta del backend.
```powershell
docker build -t niclesanti/proyecto-gastos-backend:latest ./backend

```


2. **Subir a Docker Hub:**
Envía la imagen actualizada al registro para que esté disponible para el servidor.
```powershell
docker push niclesanti/proyecto-gastos-backend:latest

```



### Fase 2: Despliegue (En Servidor Oracle Cloud)

Una vez que la imagen está en la nube, debemos indicarle al servidor que descargue la versión más reciente.

1. **Conectarse por SSH:**
```powershell
ssh -i .\tu_llave.key ubuntu@ip_servidor

```


2. **Actualizar la imagen y reiniciar:**
Navega a la carpeta del proyecto y utiliza `docker-compose` para descargar solo los cambios.
```bash
cd ~/proyecto-gastos
docker-compose pull backend
docker-compose up -d backend

```



### Fase 3: Verificación y Limpieza

Tras el reinicio, es vital confirmar que Spring Boot 3.5.3 arrancó correctamente y liberar espacio en el disco limitado de la VM.

1. **Monitorear logs de arranque:**
```bash
docker logs -f springboot-campito-prod

```


*Espera a ver el mensaje: `Started BackendApplication in X seconds*`.
2. **Limpiar imágenes antiguas (Opcional pero Recomendado):**
Las imágenes viejas ocupan espacio en disco que es escaso en la capa gratuita.
```bash
docker image prune -f

```



---

### Notas de Arquitecto:

* **Zero Downtime:** Durante el `docker-compose up -d`, habrá unos segundos donde el backend no responderá mientras reinicia el contenedor. Caddy mostrará un error 502 brevemente hasta que Spring Boot esté listo.
* **Migraciones de DB:** Si tus cambios incluyeron nuevos archivos de **Flyway** (`V7__...sql`), estos se ejecutarán automáticamente al iniciar el contenedor, impactando la base de datos de Aiven de forma segura.

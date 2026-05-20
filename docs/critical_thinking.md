# Critical Thinking & Security Responses

Este documento responde a los cuatro apartados críticos exigidos en el proyecto: bypass del backend, fuga de secretos, permisos UID/GID y benchmarking.

## 1. Vulnerabilidad “Bypass”

### Problema

La arquitectura vulnerable consistía en publicar el backend directamente hacia el host mediante el puerto `8080`. Aunque Nginx protegiera `/admin` con Basic Auth, un atacante podía saltarse el proxy accediendo directamente al backend:

```bash
curl http://localhost:8080/api/message
```

Si ese comando devuelve respuesta, significa que el backend está expuesto fuera de Docker. En ese caso, cualquier protección configurada en Nginx no sirve para esa ruta, porque la petición no pasa por Nginx.

### Riesgo

El riesgo principal es que el backend queda disponible desde el exterior sin pasar por:

- HTTPS gestionado por Nginx.
- Basic Auth.
- Rate limiting.
- Cabeceras de seguridad.
- Control centralizado de logs.

Esto rompe la idea de que Nginx sea el único punto de entrada.

### Corrección aplicada

La corrección consiste en eliminar cualquier publicación de puertos del backend y dejarlo solo en una red interna:

```yaml
backend:
  networks:
    - backend_net
  # sin ports
```

Nginx sí se conecta a `backend_net`:

```yaml
nginx:
  networks:
    - frontend_net
    - backend_net
```

De esta manera, el flujo correcto es:

```text
Cliente -> HTTPS -> Nginx -> backend_net -> Backend
```

Y el flujo inseguro queda bloqueado:

```text
Cliente -> Backend:8080 ❌
```

### Comprobación

```bash
curl http://localhost:8080/api/message
```

Debe fallar.

```bash
curl -k https://localhost/api/message
```

Debe funcionar, porque pasa por Nginx.

### Concepto aprendido

La red interna de Docker reduce la superficie de ataque. Un servicio no debe exponerse al host si solo necesita comunicarse con otro contenedor. En este proyecto, el backend solo necesita hablar con Nginx y con la base de datos, no con el exterior.

## 2. Incidente “Leaked Secret”

### Situación simulada

Se simula que durante el desarrollo se sube por error la clave privada TLS:

```text
certs/server.key
```

Una clave privada no debe exponerse, porque permitiría a un atacante suplantar el servidor o comprometer comunicaciones.

### Pasos de respuesta

1. Detectar la fuga durante una revisión del historial o del commit.
2. Crear una rama de hotfix:

   ```bash
   git checkout main
   git checkout -b hotfix/certificados
   ```

3. Generar un nuevo certificado y una nueva clave:

   ```bash
   openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
     -keyout certs/server.key \
     -out certs/server.crt \
     -subj "/C=ES/ST=Sevilla/L=Sevilla/O=Lab/OU=DAW/CN=localhost"
   ```

4. Actualizar `.gitignore` para evitar futuras subidas accidentales de claves:

   ```gitignore
   certs/*.key
   ```

5. Confirmar que Nginx sigue apuntando a los mismos nombres:

   ```nginx
   ssl_certificate     /etc/nginx/certs/server.crt;
   ssl_certificate_key /etc/nginx/certs/server.key;
   ```

6. Probar el arranque:

   ```bash
   docker compose up -d --build
   curl -k -I https://localhost
   ```

7. Hacer commit del hotfix:

   ```bash
   git add certs/server.crt certs/server.key .gitignore docs/critical_thinking.md
   git commit -m "hotfix: regenerar certificados y documentar incidente"
   ```

8. Fusionar el hotfix en `main` y `develop`.

### Nota de laboratorio

En este proyecto se usan certificados autofirmados de laboratorio. En un entorno real, además de regenerar la clave, habría que revocar el certificado comprometido y emitir uno nuevo con una CA confiable.

### Concepto aprendido

Un secreto filtrado no se “arregla” solo ocultándolo después. Hay que rotarlo. La clave comprometida debe considerarse inválida desde el momento en que entra en el repositorio.

## 3. Resolución UID/GID entre SFTP y Nginx

### Problema

El contenedor SFTP escribe archivos en un volumen compartido y Nginx los sirve. Si SFTP crea los archivos con un UID/GID incompatible, Nginx podría no leerlos. La solución insegura sería:

```bash
chmod 777
```

Eso no se debe usar porque da permisos de lectura, escritura y ejecución a todos.

### Diagnóstico

Primero se comprueba el usuario de Nginx:

```bash
docker run --rm nginx:alpine id nginx
```

Salida esperada aproximada:

```text
uid=101(nginx) gid=101(nginx) groups=101(nginx)
```

Después se comprueba el usuario SFTP:

```bash
docker compose exec sftp id sftpuser
```

### Corrección aplicada

El servicio SFTP se define con UID/GID 101:

```yaml
sftp:
  image: atmoz/sftp:latest
  command: sftpuser:password:101:101:upload
  volumes:
    - static_content:/home/sftpuser/upload
```

Nginx monta el mismo volumen en modo lectura:

```yaml
nginx:
  volumes:
    - static_content:/usr/share/nginx/html:ro
```

Con esto, los archivos subidos por SFTP quedan disponibles para Nginx sin abrir permisos de forma insegura.

### Prueba

1. Crear un archivo local:

   ```bash
   echo "Archivo subido por SFTP" > prueba.html
   ```

2. Subirlo:

   ```bash
   sftp -P 2222 sftpuser@localhost
   put prueba.html upload/prueba.html
   ```

3. Consultarlo desde Nginx:

   ```bash
   curl -k https://localhost/prueba.html
   ```

Si se devuelve el contenido, la integración funciona.

### Concepto aprendido

Los volúmenes Docker conservan propietarios numéricos. Por eso importa el UID/GID, no solo el nombre del usuario. Alinear IDs permite compartir archivos de forma segura entre contenedores.

## 4. Benchmarking y tuning

### Objetivo

El objetivo es comprobar si los límites de CPU y memoria definidos para backend y base de datos son razonables.

Configuración inicial:

```yaml
backend:
  deploy:
    resources:
      limits:
        cpus: '0.5'
        memory: 512m

db:
  deploy:
    resources:
      limits:
        cpus: '0.5'
        memory: 512m
```

### Prueba básica

Abrir una terminal para monitorizar:

```bash
docker stats
```

En otra terminal o desde el navegador, generar carga recargando varias veces la página principal y la ruta de API:

```bash
curl -k https://localhost/api/message
```

Para una prueba más fuerte se puede usar `wrk` o `ab`:

```bash
wrk -t4 -c50 -d30s https://localhost/api/message
```

### Qué observar

Durante la prueba se revisa:

- Uso de CPU del backend.
- Memoria consumida por backend y MySQL.
- Reinicios de contenedores.
- Estado `healthy/unhealthy`.
- Respuestas lentas o errores `50x`.

### Decisión de tuning

Si el backend se reinicia por falta de memoria, se justifica subir el límite, por ejemplo:

```yaml
memory: 768m
```

Si MySQL responde lento bajo carga, se puede aumentar CPU:

```yaml
cpus: '0.75'
```

En el estado actual, los límites de `0.5 CPU` y `512m` se mantienen porque son suficientes para una práctica local y evitan consumo excesivo del equipo.

### Evidencia recomendada

Guardar una captura de `docker stats` durante la prueba en:

```text
docs/images/docker-stats.png
```

También conviene incluir una captura de `docker compose ps` mostrando los servicios en estado correcto.

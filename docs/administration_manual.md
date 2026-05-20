# Administration & Hardening Manual

Este documento resume las decisiones de administración, seguridad y mantenimiento aplicadas al despliegue.

## 1. Separación por servicios

El proyecto se divide en servicios independientes:

- `frontend`: genera la interfaz React.
- `backend`: ejecuta la lógica de aplicación con Spring Boot.
- `db`: almacena datos en MySQL.
- `nginx`: actúa como reverse proxy y único punto de entrada web.
- `sftp`: permite gestionar contenido estático mediante SFTP.

Esta separación evita una arquitectura monolítica y permite aplicar controles concretos a cada capa.

## 2. Aislamiento de redes

Se usan redes Docker separadas:

| Red | Servicios conectados | Motivo |
|---|---|---|
| `frontend_net` | `frontend`, `nginx` | Separar la capa de presentación. |
| `backend_net` | `backend`, `db`, `nginx` | Permitir que solo Nginx hable con el backend desde fuera. |
| `sftp_net` | `sftp` | Aislar la gestión de contenidos del resto de la aplicación. |

El backend no publica el puerto `8080`. Solo Nginx puede alcanzarlo por la red interna. Así se evita que un atacante salte la autenticación del proxy accediendo directamente al backend.

## 3. Principio de menor privilegio

Los contenedores personalizados no deben ejecutarse como root:

- El frontend define un usuario no root en su Dockerfile.
- El backend define un usuario no root en su Dockerfile.
- Nginx usa el usuario `nginx` de la imagen oficial.
- SFTP usa un usuario limitado y chrooted.

Esto reduce el impacto de una posible vulnerabilidad dentro de un contenedor.

## 4. Hardening de Nginx

La configuración de Nginx aplica las siguientes medidas:

- `server_tokens off` para ocultar la versión de Nginx.
- `keepalive_timeout` ajustado.
- `client_max_body_size` configurado para limitar subidas.
- Redirección automática de HTTP a HTTPS.
- Certificado autofirmado para TLS en laboratorio.
- Cabeceras de seguridad.
- Páginas de error personalizadas para evitar páginas por defecto.
- Basic Auth en `/admin`.
- Rate limiting en `/admin` para reducir fuerza bruta.

## 5. TLS y certificados

Nginx termina TLS usando los archivos:

```text
certs/server.crt
certs/server.key
```

Son certificados autofirmados de laboratorio. En producción se sustituirían por certificados emitidos por una CA confiable, por ejemplo Let’s Encrypt.

Comprobación básica:

```bash
curl -k -I https://localhost
```

## 6. Protección de `/admin`

La ruta `/admin` está protegida con HTTP Basic Auth:

```text
nginx/conf/.htpasswd
```

Además se aplica rate limiting mediante `limit_req_zone` y `limit_req`, de forma que los intentos repetidos de acceso se reducen.

## 7. Páginas de error personalizadas

Los errores `403`, `404` y `50x` usan páginas HTML propias ubicadas en:

```text
nginx/errors/
```

Esto evita mostrar firmas o páginas por defecto del servidor.

## 8. SFTP y permisos

El servicio SFTP comparte volumen con Nginx:

```text
static_content
```

El volumen se monta en:

```text
Nginx: /usr/share/nginx/html
SFTP:  /home/sftpuser/upload
```

La configuración del usuario SFTP alinea UID/GID con Nginx:

```text
sftpuser:password:101:101:upload
```

Esto evita usar `chmod 777`. La idea es que los archivos subidos queden con propietario/grupo compatible con el usuario de Nginx y puedan ser servidos inmediatamente.

Comprobaciones recomendadas:

```bash
docker run --rm nginx:alpine id nginx
docker compose exec sftp id sftpuser
```

Ambos deben mostrar UID/GID compatibles.

## 9. Límites de recursos

Se aplican límites a los servicios más sensibles:

| Servicio | CPU | Memoria |
|---|---:|---:|
| `backend` | `0.5` | `512m` |
| `db` | `0.5` | `512m` |

Estos límites evitan que un proceso consuma todos los recursos del equipo. Los valores pueden ajustarse si durante las pruebas con `docker stats` se detectan reinicios, OOM o alto consumo sostenido.

## 10. Healthchecks

Los healthchecks permiten saber si un servicio está listo:

- `db`: usa `mysqladmin ping`.
- `backend`: comprueba `/api/message`.
- `nginx`: comprueba que responde en local.
- `sftp`: comprueba que `sshd` está activo.

El backend espera a que la base de datos esté saludable antes de arrancar completamente.

## 11. Rotación de logs

Para evitar crecimiento indefinido de logs se configura el driver `json-file`:

```yaml
logging:
  driver: json-file
  options:
    max-size: "10m"
    max-file: "3"
```

Con esto Docker conserva hasta tres archivos de 10 MB por contenedor.

## 12. Comandos de administración

Ver contenedores:

```bash
docker compose ps
```

Ver logs:

```bash
docker compose logs nginx
```

Entrar en un contenedor:

```bash
docker compose exec backend sh
```

Ver consumo:

```bash
docker stats
```

Reiniciar un servicio:

```bash
docker compose restart nginx
```

Parar todo:

```bash
docker compose down
```

## 13. Recomendaciones de producción

Este proyecto está planteado para laboratorio. Para producción habría que:

- Usar certificados reales.
- Guardar secretos en variables protegidas o Docker secrets.
- No versionar claves privadas.
- Cambiar credenciales de laboratorio.
- Usar imágenes fijadas por versión exacta.
- Activar escaneo de vulnerabilidades.
- Separar logs y métricas en una solución externa.

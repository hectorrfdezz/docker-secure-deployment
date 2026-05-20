# docker-secure-deployment

Proyecto de despliegue seguro multi-tier con Docker Compose.

El objetivo es levantar una aplicación web separada por capas, con Nginx como único punto de entrada, backend protegido en una red interna, base de datos persistente y un servicio SFTP independiente para gestionar contenido estático.

## Arquitectura

| Servicio | Función |
|---|---|
| `frontend` | Aplicación React. Se construye con Dockerfile multi-stage y genera contenido estático. |
| `backend` | Aplicación Java/Spring Boot. Expone la API solo dentro de la red interna. |
| `db` | MySQL con volumen persistente e inicialización automática mediante `init.sql`. |
| `nginx` | Reverse proxy, terminación TLS, redirección HTTP a HTTPS y protección de `/admin`. |
| `sftp` | Servicio de gestión de archivos para subir contenido al volumen servido por Nginx. |

Nginx es el único servicio publicado hacia el exterior mediante los puertos `80` y `443`. El backend no publica el puerto `8080`, por lo que no se puede acceder a él directamente desde fuera del entorno Docker.

## Medidas principales

- Separación de redes: `frontend_net`, `backend_net` y `sftp_net`.
- Dockerfiles multi-stage para frontend y backend.
- Usuarios no root en los contenedores personalizados.
- Nginx con HTTPS, redirección HTTP a HTTPS y `server_tokens off`.
- Páginas de error personalizadas para `403`, `404` y errores `50x`.
- `/admin` protegido con HTTP Basic Auth y rate limiting.
- SFTP con chroot y volumen compartido con Nginx.
- UID/GID alineado entre SFTP y Nginx para evitar permisos inseguros como `chmod 777`.
- Límites de CPU y memoria en backend y base de datos.
- Healthchecks para controlar el arranque y estado de los servicios.
- Documentación del incidente simulado de fuga de `server.key`.

## Puesta en marcha

```bash
docker compose build
docker compose up -d
docker compose ps
```

La aplicación estará disponible en:

```text
https://localhost
```

La zona protegida está en:

```text
https://localhost/admin
```

Credenciales de laboratorio:

```text
Usuario: admin
Contraseña: admin123
```

## SFTP

```bash
sftp -P 2222 sftpuser@localhost
```

Credenciales de laboratorio:

```text
Usuario: sftpuser
Contraseña: password
```

Los archivos subidos al directorio `upload` se almacenan en el volumen compartido y pueden ser servidos por Nginx.

## Documentación

La documentación principal está en la carpeta `docs/`:

| Archivo | Contenido |
|---|---|
| `deployment_manual.md` | Pasos para construir, arrancar, probar y detener el entorno. |
| `administration_manual.md` | Seguridad, redes, hardening, recursos, logs y operación básica. |
| `critical_thinking.md` | Respuestas a los retos de bypass, leaked secret, UID/GID y benchmarking. |
| `git_flow_report.md` | Explicación del flujo Git utilizado y evidencia del historial. |
| `evidence_checklist.md` | Lista de capturas recomendadas para demostrar el funcionamiento. |

## Limpieza del entorno

```bash
docker compose down
```

Para borrar también los volúmenes:

```bash
docker compose down -v
```

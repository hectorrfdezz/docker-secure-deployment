# docker-secure-deployment

Proyecto de despliegue seguro multi-tier con Docker Compose.

El objetivo es levantar una aplicación web separada por capas, con Nginx como único punto de entrada, backend protegido en una red interna, base de datos persistente y un servicio SFTP independiente para gestionar contenido estático.

---

## Arquitectura

| Servicio | Función |
|---|---|
| `frontend` | Aplicación React. Se construye con Dockerfile multi-stage y genera contenido estático. |
| `backend` | Aplicación Java/Spring Boot. Expone la API solo dentro de la red interna. |
| `db` | MySQL con volumen persistente e inicialización automática mediante `init.sql`. |
| `nginx` | Reverse proxy, terminación TLS, redirección HTTP a HTTPS y protección de `/admin`. |
| `sftp` | Servicio de gestión de archivos para subir contenido al volumen servido por Nginx. |

Nginx es el único servicio publicado hacia el exterior mediante los puertos `80` y `443`. El backend no publica el puerto `8080`, por lo que no se puede acceder a él directamente desde fuera del entorno Docker.

---

## Estructura del repositorio

```text
docker-secure-deployment/
├── backend/
├── certs/
├── db/
├── docs/
├── frontend/
├── nginx/
├── docker-compose.yml
├── .gitignore
└── README.md
```

---

## Medidas principales de seguridad

- Separación de redes: `frontend_net`, `backend_net` y `sftp_net`.
- Dockerfiles multi-stage para frontend y backend.
- Usuarios no root en los contenedores personalizados.
- Nginx como único punto de entrada externo.
- HTTPS mediante certificados autofirmados para entorno de laboratorio.
- Redirección automática de HTTP a HTTPS.
- Ocultación de la versión de Nginx con `server_tokens off`.
- Configuración de `keepalive_timeout` y `client_max_body_size`.
- Páginas de error personalizadas para `403`, `404` y errores `50x`.
- Ruta `/admin` protegida con HTTP Basic Auth.
- Rate limiting en `/admin` para reducir intentos de fuerza bruta.
- Backend sin puerto público expuesto.
- Base de datos aislada dentro de la red interna.
- SFTP con chroot y volumen compartido con Nginx.
- UID/GID alineado entre SFTP y Nginx para evitar permisos inseguros como `chmod 777`.
- Límites de CPU y memoria en backend y base de datos.
- Healthchecks para controlar el arranque y estado de los servicios.
- Documentación del incidente simulado de fuga de `server.key`.

---

## Puesta en marcha

Construir los contenedores:

```bash
docker compose build
```

Levantar el entorno:

```bash
docker compose up -d
```

Comprobar el estado de los servicios:

```bash
docker compose ps
```

La aplicación estará disponible en:

```text
https://localhost
```

Al usar certificados autofirmados, el navegador puede mostrar una advertencia de seguridad. Es normal en este entorno de pruebas.

---

## Zona de administración

La zona protegida está en:

```text
https://localhost/admin
```

Credenciales de laboratorio:

```text
Usuario: admin
Contraseña: admin123
```

---

## Acceso por SFTP

El servicio SFTP se expone en el puerto `2222`.

```bash
sftp -P 2222 sftpuser@localhost
```

Credenciales de laboratorio:

```text
Usuario: sftpuser
Contraseña: password
```

Los archivos subidos al directorio `upload` se almacenan en el volumen compartido y pueden ser servidos por Nginx como contenido estático.

---

## Limpieza del entorno

Detener los contenedores:

```bash
docker compose down
```

Eliminar también los volúmenes:

```bash
docker compose down -v
```

---

## Incidente de seguridad simulado

Se simuló una fuga de la clave privada TLS `server.key`.

Medidas tomadas:

1. Generación de nuevos certificados.
2. Sustitución de los archivos comprometidos.
3. Actualización de `.gitignore` para evitar subir claves privadas.
4. Documentación del incidente en `docs/critical_thinking.md`.
5. Resolución mediante una rama `hotfix/`.

---

## Git Flow

El proyecto se desarrolló siguiendo un flujo Git Flow con ramas principales y auxiliares:

```text
main
develop
feature/*
release/*
hotfix/*
```

Comando usado para comprobar el historial:

```bash
git log --graph --oneline --decorate --all
```

La captura del historial debe guardarse en:

```text
docs/images/git-flow.png
```

---

## Documentación

La documentación principal está en la carpeta `docs/`:

| Archivo | Contenido |
|---|---|
| `deployment_manual.md` | Pasos para construir, arrancar, probar y detener el entorno. |
| `administration_manual.md` | Seguridad, redes, hardening, recursos, logs y operación básica. |
| `critical_thinking.md` | Respuestas a los retos de bypass, leaked secret, UID/GID y benchmarking. |
| `git_flow_report.md` | Explicación del flujo Git utilizado y evidencia del historial. |
| `evidence_checklist.md` | Lista de capturas recomendadas para demostrar el funcionamiento. |

---

## Evidencias recomendadas

Para completar la entrega, se recomienda incluir capturas en `docs/images/` mostrando:

- `docker compose ps` con todos los servicios levantados.
- Acceso a `https://localhost`.
- Redirección desde HTTP a HTTPS.
- Acceso protegido a `/admin`.
- Conexión SFTP al puerto `2222`.
- Subida de archivo por SFTP y visualización desde Nginx.
- `docker stats` durante pruebas de carga o recargas del navegador.
- `git log --graph --oneline --decorate --all`.

---

## Autor

Héctor  
Repositorio: `https://github.com/hectorrfdezz/docker-secure-deployment`

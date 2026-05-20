# Deployment Manual

Este manual explica cómo desplegar y comprobar el entorno multi-tier del proyecto.

## 1. Requisitos previos

Antes de arrancar el proyecto deben estar instalados:

- Docker.
- Docker Compose.
- Git.
- Un navegador web.
- Un cliente SFTP, por ejemplo el comando `sftp` o FileZilla.

El proyecto se ha preparado para ejecutarse en local usando Docker Compose.

## 2. Clonar el repositorio

```bash
git clone https://github.com/hectorrfdezz/docker-secure-deployment.git
cd docker-secure-deployment
```

## 3. Revisar la estructura

La estructura esperada es:

```text
docker-secure-deployment/
├── backend/
├── certs/
├── db/
├── docs/
├── frontend/
├── nginx/
├── docker-compose.yml
└── README.md
```

Los archivos clave son:

- `docker-compose.yml`: orquesta todos los servicios.
- `frontend/Dockerfile`: construye la aplicación React.
- `backend/Dockerfile`: construye la aplicación Spring Boot.
- `db/init.sql`: inicializa la base de datos en el primer arranque.
- `nginx/conf/nginx.conf`: configuración segura de Nginx.
- `nginx/conf/.htpasswd`: credenciales de Basic Auth para `/admin`.
- `certs/server.crt` y `certs/server.key`: certificado autofirmado de laboratorio.

## 4. Construir las imágenes

```bash
docker compose build
```

Este comando ejecuta los Dockerfiles multi-stage del frontend y backend y descarga las imágenes necesarias para MySQL, Nginx y SFTP.

## 5. Arrancar el entorno

```bash
docker compose up -d
```

El primer arranque puede tardar un poco porque MySQL debe inicializar la base de datos y el backend espera a que la base de datos esté saludable.

## 6. Comprobar contenedores

```bash
docker compose ps
```

Resultado esperado:

- `db` en estado healthy.
- `backend` en estado healthy.
- `nginx` levantado y publicando `80` y `443`.
- `sftp` levantado y publicando `2222`.
- `frontend` levantado después de copiar los archivos estáticos al volumen compartido.

## 7. Acceder a la aplicación

Abrir en el navegador:

```text
https://localhost
```

Como el certificado es autofirmado, el navegador puede mostrar una advertencia. En este proyecto es normal porque se trata de un entorno de laboratorio.

## 8. Comprobar redirección HTTP a HTTPS

```bash
curl -I http://localhost
```

Debe aparecer una respuesta de redirección hacia HTTPS, por ejemplo `301` o `308`, con una cabecera `Location: https://...`.

## 9. Comprobar API a través de Nginx

```bash
curl -k https://localhost/api/message
```

Debe devolver la respuesta del backend a través del proxy. El backend no debe ser accesible directamente desde el host.

Comprobación del bypass corregido:

```bash
curl http://localhost:8080/api/message
```

Este comando debe fallar, porque el puerto `8080` del backend no se publica fuera de Docker.

## 10. Acceder a la zona protegida

Abrir:

```text
https://localhost/admin
```

Credenciales de laboratorio:

```text
Usuario: admin
Contraseña: admin123
```

La ruta `/admin` está protegida con Basic Auth y rate limiting.

## 11. Probar SFTP

Conectarse al servicio SFTP:

```bash
sftp -P 2222 sftpuser@localhost
```

Credenciales:

```text
Usuario: sftpuser
Contraseña: password
```

Subir un archivo de prueba:

```bash
put prueba.html upload/prueba.html
```

Después, comprobar en el navegador:

```text
https://localhost/prueba.html
```

El archivo debería servirse desde Nginx sin modificar permisos manualmente ni usar `chmod 777`.

## 12. Ver logs

```bash
docker compose logs nginx
docker compose logs backend
docker compose logs db
docker compose logs sftp
```

Para ver logs en tiempo real:

```bash
docker compose logs -f nginx
```

## 13. Monitorizar recursos

```bash
docker stats
```

Este comando permite comprobar el consumo de CPU y memoria. Se usa para validar los límites configurados en backend y base de datos.

## 14. Detener el entorno

```bash
docker compose down
```

## 15. Borrar también los volúmenes

```bash
docker compose down -v
```

Este comando elimina también los datos de MySQL y el contenido subido por SFTP.

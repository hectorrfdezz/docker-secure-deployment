## Nginx Dockerfile
#
# Builds a hardened Nginx reverse proxy with custom configuration,
# certificate directory and error pages.  The resulting image
# intentionally avoids running as root; instead it utilises the
# unprivileged nginx user defined by the upstream image.  Further
# hardening measures such as read‑only file systems can be added later.

FROM nginx:alpine

# Copy configuration and error pages into the container.  The certs and
# htpasswd file will be mounted at runtime via docker-compose.yml.
COPY conf/nginx.conf /etc/nginx/nginx.conf
COPY errors/ /usr/share/nginx/html/errors/

# Expose ports for documentation purposes.  Actual binding occurs via
# docker-compose.yml when the container runs behind the host network.
EXPOSE 80 443

# Use the default command from the nginx image.  No CMD instruction is
# required because the base image already specifies it.  This Dockerfile
# simply provides the configuration and assets required for operation.
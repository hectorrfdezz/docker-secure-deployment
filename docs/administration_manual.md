# Administration & Hardening Manual

This manual documents the security configuration and operational
guidelines for the multi‑tier deployment.  It covers network
segmentation, user privileges, resource limits, SSL/TLS setup and
procedures for log rotation.  Future commits will expand upon this
information with performance tuning and incident response details.

## Network Isolation

Three private Docker networks are defined to enforce isolation between
services:

- **frontend_net**: connects the `frontend` and `nginx` containers.  The
  React build container copies static assets into a shared volume which
  is served by Nginx.  No other service is attached to this network.
- **backend_net**: connects the `backend`, `db` and `nginx` containers.
  Only Nginx can reach the backend and database from outside.
- **sftp_net**: connects the `sftp` container to the host.  The SFTP
  service does not share a network with the application servers.  It
  only writes into the shared `static_content` volume, which is
  mounted at `/home/sftpuser/upload` inside the SFTP container.

This strict segmentation ensures that an attacker cannot bypass Nginx’s
authentication and reach the backend directly.  In future revisions
we will document how this design mitigates the “Bypass” vulnerability
described in the project brief.

## User Privileges

Custom users are created in the frontend and backend Dockerfiles using
the `adduser` command.  These users run the application processes
instead of the root user, following the principle of least privilege.
The Nginx base image already defines an `nginx` user which is used by
default.  The SFTP image will be configured to align its UID/GID with
Nginx in a later commit to resolve ownership issues on the shared
volume.

## Resource Limits
Both the `backend` and `db` services include `deploy` sections with
cgroup resource limits.  The backend is constrained to half a CPU and
512 MB of RAM.  The database is likewise limited to half a CPU and
512 MB.  These limits help prevent a runaway process from exhausting
system resources.  Monitor your containers with `docker stats` and
adjust these values based on observed usage and performance
requirements.

## Healthchecks

Healthchecks ensure that dependent services start only when their
dependencies are ready.  The backend service defines a `healthcheck`
that performs an HTTP request to `/api/message`.  If the request
fails, Docker will mark the container as unhealthy and other
services that depend on it (such as Nginx) will wait until it becomes
healthy before routing traffic.  The database retains its built‑in
healthcheck via `mysqladmin ping`.

## SSL/TLS Termination

Self‑signed certificates reside in the `certs/` directory and are
mounted into the Nginx container at `/etc/nginx/certs`.  The
`nginx.conf` file configures Nginx to listen on port 443, load the
certificates and redirect all HTTP traffic to HTTPS.  In a
production environment you would replace these files with certificates
issued by a trusted Certificate Authority (e.g. via Let’s Encrypt).

## Basic Authentication & Rate Limiting

The `/admin` location is protected with HTTP Basic Auth.  Credentials
are stored in the `.htpasswd` file and mounted read‑only into the
container.  A rate limit of one request per second (with a burst of
five) is enforced via the `limit_req_zone` directive to mitigate
brute‑force attacks.

## Log Rotation

At present this environment does not enable log rotation.  Future
commits will add a scheduled `logrotate` configuration or utilise
Docker’s built‑in logging drivers with size limits to ensure that logs
do not fill the host filesystem.
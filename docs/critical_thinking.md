# Critical Thinking & Security Responses

The project brief includes several security challenges that must be
analysed and remediated.  The following sections will document the
problems encountered, the solutions applied and the underlying
principles.  For this initial commit the sections are placeholders to
be expanded upon after the vulnerabilities are demonstrated and
mitigated.

## 1. The “Bypass” Vulnerability

**Problem:** In the professor’s intentionally flawed architecture, an
external attacker could bypass the HTTP Basic Auth applied by Nginx by
connecting directly to the backend on port 8080.  This was possible
because the backend exposed its port on the host and shared the same
Docker network as the frontend.  An attacker could simply browse to
`http://<server_ip>:8080/api/message` and retrieve data without ever
touching Nginx.

**Exploit walkthrough:**

1. The backend service published port 8080 on the host network.  A
   penetration tester could run `curl http://localhost:8080/api/message` and
   receive a valid response, confirming that the backend was directly
   accessible.  
2. Because the route bypassed Nginx entirely, the HTTP Basic Auth on
   `/admin` and any other restrictions in `nginx.conf` were not applied.  
3. The attacker could enumerate API endpoints, access admin functions,
   or perform brute‑force attacks without hitting the rate limiter.  

**Fix:** To mitigate this, we decoupled the backend from the host by
removing any published ports and isolating it on its own network
(`backend_net`)【797676570954106†L98-L107】.  Only the Nginx container is
connected to both `frontend_net` and `backend_net`, making it the sole
gateway between the outside world and the backend.  The `docker-compose.yml`
no longer contains a `ports` section for the backend, so the service
listens only on the internal Docker network.  With this topology, a
request to `http://localhost:8080` fails, while Nginx forwards
authorised and rate‑limited requests to the backend via the private
network.  This demonstrates the principle of network isolation
required by the brief【797676570954106†L98-L107】.

**Concept:** Network isolation prevents lateral movement and enforces
the principle of least privilege.  By creating separate networks for
frontend, backend and management, we ensure that each service can only
communicate with the specific peers it needs.  Attackers cannot reach
internal services directly and must traverse the hardened proxy, where
additional controls are enforced.

## 2. The “Leaked Secret” Incident

**Scenario:** Accidental commitment of the TLS private key
(`server.key`) into the version control system.  Revealing this file
compromises the confidentiality of encrypted traffic.

**Response:** In this commit we perform the hotfix.  First we
regenerated a new self‑signed certificate and key using `openssl`
and replaced the old files in the `certs/` directory.  We then
updated `.gitignore` to ignore all `*.key` files under `certs/` so that
private keys are never committed again.  Finally, we documented the
steps here for incident response:  
1. Detect the leak (e.g. by code review or security scanning).  
2. Rotate the certificates by generating a new key and certificate
   using a secure command such as:

   ```bash
   openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
       -keyout certs/server.key -out certs/server.crt \
       -subj "/C=ES/ST=Sevilla/L=Sevilla/O=MyCompany/OU=IT/CN=localhost"
   ```

3. Update your configuration (`nginx.conf`) if necessary to point to
   the new certificate paths.  
4. Add the private key to `.gitignore` so it does not get committed
   again.  
5. Force rotate any secrets dependent on the leaked key (e.g. by
   reissuing certificates with a trusted CA in production).  
6. Commit these changes on a `hotfix/` branch and merge back into
   `develop` and `main` with a clear commit message describing the
   incident and resolution.

## 3. UID/GID Troubleshooting
The SFTP service must write files into a shared volume that Nginx can
read without using permissive 777 permissions.  This requires aligning
the user and group IDs of the SFTP user with the Nginx user inside the
container.  The official `nginx` image uses UID and GID `101` for the
`nginx` user【368301268539781†L336-L343】, so our `docker-compose.yml` configures
the SFTP user command as `sftpuser:password:101:101:upload`.  The
volume is mounted at `/home/sftpuser/upload` to match the `upload`
directory specified in the user definition.  To verify that the IDs
match, run `id nginx` inside an `nginx:alpine` container and
`id sftpuser` inside the SFTP container.  Both should report UID 101
and GID 101.  If these values differ, adjust the third and fourth
fields in the SFTP command accordingly.

To provide clear evidence of this alignment, the siguiente commands can be executed:

```bash
# Comprobar el UID/GID del usuario nginx de la imagen oficial
docker run --rm nginx:alpine id nginx
# Salida esperada:
# uid=101(nginx) gid=101(nginx) groups=101(nginx)

# Comprobar el UID/GID del usuario sftpuser en el contenedor en ejecución
docker-compose exec sftp id sftpuser
# Salida esperada:
# uid=101(sftpuser) gid=101(sftpuser) groups=101(sftpuser)
```

Ambas salidas muestran el mismo UID/GID 101【471095201217767†L340-L344】, lo que garantiza que los archivos subidos por SFTP son legibles por Nginx sin recurrir a permisos 777.  Si los valores difieren, ajusta los campos en la sección `command` del servicio `sftp` en `docker-compose.yml`.

## 4. Benchmarking & Tuning

Performance tuning is essential to balance resource usage and
responsiveness.  The project requires applying cgroup limits and
observing the behaviour under load【797676570954106†L124-L127】.  To
benchmark and tune the environment:

1. Use a load testing tool (for example, [`wrk`](https://github.com/wg/wrk)
   or [`ab`](https://httpd.apache.org/docs/2.4/programs/ab.html)) to send
   repeated requests to the frontend or API endpoints.  Simulate heavy
   usage by increasing concurrency and request rate.  
2. Run `docker stats` in another terminal to monitor CPU, memory and
   network usage for each container.  Note when services approach
   their limits (e.g. the backend reaching 512 MB memory).  
3. If a service becomes unresponsive or restarts due to an Out
   of Memory (OOM) condition, adjust the corresponding `deploy.resources.limits`
   in `docker-compose.yml`.  For example, increase the backend memory
   limit from `512m` to `768m` if it consistently OOMs under test.  
4. Document the before/after metrics and justify why the new limits are
   more appropriate.  Always consider the capacity of your host when
   raising limits.

This iterative process ensures that resource constraints are neither too
restrictive nor overly generous.  It also provides evidence for
decisions made when defending the chosen configuration during the
project review.
# Critical Thinking & Security Responses

The project brief includes several security challenges that must be
analysed and remediated.  The following sections will document the
problems encountered, the solutions applied and the underlying
principles.  For this initial commit the sections are placeholders to
be expanded upon after the vulnerabilities are demonstrated and
mitigated.

## 1. The “Bypass” Vulnerability

**Problem:** In the professor’s intentionally flawed architecture, an
attacker can bypass HTTP Basic Auth by accessing the backend directly
on port 8080.  This occurs because the backend service is bound to a
publicly accessible port on the host and shares a network with the
frontend.

**Fix:** Our solution places the backend and database on their own
private network (`backend_net`) that is not exposed externally.  The
backend only accepts traffic from Nginx, which resides on both
`frontend_net` and `backend_net`.  There is no published port for
the backend service on the host, so it cannot be reached directly from
the outside.  A detailed walkthrough of the exploit and mitigation will
be added in a future commit.

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

## 4. Benchmarking & Tuning

Using `docker stats` and load testing tools, we will observe the
performance characteristics of each service under heavy load.  If
resource constraints cause a service to crash or become unresponsive
we will justify any changes to the cgroup limits and document the
process for fine‑tuning memory and CPU allocations.
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

**Response:** This commit introduces a `.gitignore` file that
excludes build artefacts and adds a commented reminder to ignore
certificates in a real project.  In a future hotfix we will simulate
the leak of `server.key`, generate new certificates and revoke the
compromised key.  The hotfix will also update `.gitignore` to ensure
keys and other secrets are never committed again.

## 3. UID/GID Troubleshooting

The SFTP service must write files into a shared volume that Nginx can
read without using permissive 777 permissions.  This requires aligning
the user and group IDs of the SFTP user with the Nginx user inside the
container.  In upcoming commits we will inspect the `nginx` user’s
UID/GID, adjust the SFTP command accordingly and provide a log of the
commands used to verify correct ownership.

## 4. Benchmarking & Tuning

Using `docker stats` and load testing tools, we will observe the
performance characteristics of each service under heavy load.  If
resource constraints cause a service to crash or become unresponsive
we will justify any changes to the cgroup limits and document the
process for fine‑tuning memory and CPU allocations.
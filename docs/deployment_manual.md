# Deployment Manual

This document describes how to build and run the secure multi‑tier web
application using Docker Compose.  The environment comprises a React
frontend, a Spring Boot backend, a MySQL database, a hardened Nginx
reverse proxy, and an SFTP service for content management.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and
  [Docker Compose](https://docs.docker.com/compose/) installed on your
  host machine.
- At least 4 GB of free RAM and 10 GB of free disk space.

## Building the services

1. Clone the repository and change into the project directory:

   ```bash
   git clone <your-repo-url>
   cd secure-deployment-project
   ```

2. Build all services using Docker Compose.  This will perform the
   multi‑stage builds for the frontend and backend and pull the
   required base images for MySQL, Nginx and the SFTP server.

   ```bash
   docker-compose build
   ```

## Running the environment

Start the entire stack by running:

```bash
docker-compose up -d
```

Once the containers are running you can access the application at
`https://localhost`.  The first launch may take several seconds while
the database initialises and the backend performs its healthcheck.

### Credentials

- MySQL root password: `example`
- SFTP user/password: `sftpuser` / `password`
- Admin Basic Auth: `admin` / `admin123`

> **Note:** The credentials above are for demonstration purposes in a
> controlled lab environment.  Do not use them in production.

## Stopping and removing services

To stop the containers without destroying data volumes:

```bash
docker-compose stop
```

To remove all containers and volumes (this will delete the database
contents and uploaded files):

```bash
docker-compose down -v
```

## Next steps

This manual will be expanded in future commits to include troubleshooting
tips, environment variable descriptions, network isolation details and
instructions on how to upload content via SFTP.
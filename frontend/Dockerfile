## Frontend Dockerfile
#
# This multi‑stage Dockerfile builds the React frontend and copies the
# resulting static files into a minimal runtime image.  It then runs
# as a non‑root user and provides the static files under /static.

## Stage 1: build the React application
FROM node:18-alpine AS builder

WORKDIR /app
# Install dependencies first to leverage Docker cache
COPY package.json package.json
RUN npm install
# Copy the remaining source and build
COPY . .
RUN npm run build

## Stage 2: copy built assets into a lightweight container
FROM alpine:3.19

# Create an unprivileged user
RUN adduser -D -u 1001 appuser
USER appuser

WORKDIR /home/appuser

# Copy the compiled React build from the previous stage.  The build is
# placed in /static so that docker-compose can copy it into the
# shared static volume at runtime.
COPY --from=builder /app/build /static

# The container does nothing by itself – it simply holds the static
# assets.  docker-compose will run a custom command to copy these
# files into the shared volume and then keep the container alive.
CMD ["sleep", "infinity"]
## Frontend Dockerfile
# Multi-stage build for the React frontend.
# The final image runs as a non-root user and prepares /static_output
# with the right ownership so the container can copy files into the shared volume.

FROM node:18-alpine AS builder
WORKDIR /app

COPY package.json package.json
RUN npm install

COPY . .
RUN npm run build

FROM alpine:3.19

RUN adduser -D -u 1001 appuser \
    && mkdir -p /static /static_output \
    && touch /static_output/.keep \
    && chown -R appuser:appuser /static /static_output

COPY --from=builder --chown=appuser:appuser /app/build /static

USER appuser
WORKDIR /home/appuser

CMD ["sleep", "infinity"]
# Deployment Guide

This document covers running the CE API (`apps/api`) on **Google App Engine Standard**, deployed via CI/CD.

Deploy scripts and GAE config live under `infra/appengine/` — not in `apps/api`.

## Prerequisites

- Google Cloud project with App Engine enabled
- PostgreSQL database
- Redis (cache, BullMQ queues, and distributed rate limiting)
- Auth0 tenant (JWT JWKS, issuer, audience)
- Doppler project for secrets (`DOPPLER_TOKEN`, `DOPPLER_PROJECT`, `DOPPLER_CONFIG`)
- Required secrets documented in `apps/api/.env.example`

## Pipeline assets (`infra/appengine/`)

| File | Purpose |
|------|---------|
| `app.yaml.template` | GAE service config — render with `envsubst` |
| `migrate.sh` | Run Prisma migrations before deploy |
| `start.sh` | GAE entrypoint — copy into deploy artifact |

## Pipeline flow

1. **Build** — from monorepo root:
   ```bash
   npm ci
   npm run build
   ```
2. **Stage artifact** — assemble deploy directory from `apps/api`:
   - `dist/`, `prisma/`, `prisma.config.ts`, `package.json`, `node_modules` (or production subset)
   - `bin/doppler` (if bundled)
   - Copy runtime entrypoint: `cp infra/appengine/start.sh deploy-artifact/start.sh`
3. **Migrate** — from deploy artifact root (where `prisma/` lives):
   ```bash
   cd deploy-artifact
   sh ../infra/appengine/migrate.sh
   ```
   Requires `DOPPLER_TOKEN`, `DOPPLER_PROJECT`, and `DOPPLER_CONFIG`.
4. **Render GAE config**:
   ```bash
   export GAE_SERVICE=api-staging   # or "api" for production
   envsubst '${GAE_SERVICE}' < infra/appengine/app.yaml.template > app.yaml
   ```
5. **Deploy**:
   ```bash
   gcloud app deploy ./deploy-artifact --appyaml=app.yaml --project="$GCP_PROJECT" --quiet
   ```

| Environment | `GAE_SERVICE` |
|-------------|---------------|
| Staging | `api-staging` |
| Production | `api` |

At runtime, App Engine runs `start.sh` from the artifact root, which loads secrets from Doppler and executes `node dist/main`.

## Health probes

These routes sit **outside** the `/api` global prefix and do not require authentication.

| Endpoint | Purpose | Success | Failure |
|----------|---------|---------|---------|
| `GET /health` | Liveness — process is up | `200 { "status": "ok" }` | n/a |
| `GET /ready` | Readiness — Postgres + Redis | `200 { "ready": true, ... }` | `503 { "ready": false, ... }` |
| `GET /metrics` | Runtime metrics — memory, CPU, uptime | `200 { "memory": { ... }, "cpu": { ... } }` | n/a |

Configure your load balancer or orchestrator:

- **Liveness:** `GET /health`
- **Readiness:** `GET /ready`

Both endpoints are excluded from rate limiting.

`/metrics` exposes process heap/RSS usage, host RAM totals, CPU time, uptime, and load average. It is public and unauthenticated — restrict access at the network layer in production if needed.

## Production checklist

1. Set `NODE_ENV=production` via Doppler
2. Provide strong `APP_SECRET` (32+ characters)
3. Set explicit `CORS_ALLOWED_ORIGIN` (comma-separated)
4. Run `infra/appengine/migrate.sh` on each release before deploy
5. Point probes at `/health` and `/ready`
6. Configure `SLACK_WEBHOOK_URL` for production error alerting (optional but recommended)

## Local development

```bash
npm ci
npm run build
npm run start --workspace=apps/api
```

Redis for local dev can be started with:

```bash
npm run start:redis --workspace=apps/api
```

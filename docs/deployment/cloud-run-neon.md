# Cloud Run + Neon deployment

This backend is prepared to run as a Java 21 Spring Boot container on Google Cloud Run, with Neon PostgreSQL as the database.

## 1. Create the Neon database

1. Create a Neon project and database, for example `tuestudio`.
2. Copy the pooled or direct PostgreSQL connection values.
3. Use a JDBC URL with SSL:

```text
jdbc:postgresql://<neon-host>/<database>?sslmode=require
```

Keep the Cloud Run pool small. The default app settings are:

```text
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=3
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=0
```

For a fresh production DB, keep `SPRING_FLYWAY_BASELINE_ON_MIGRATE=false` so Flyway fails instead of silently adopting an unexpected schema. If an old local DB has tables but no `flyway_schema_history`, reset it or temporarily set `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` only for that local recovery.

Flyway also seeds the public catalog tables (`catalog_universities`, `catalog_careers`, `catalog_subjects`). Demo users/admins are not seeded unless the `dev` profile is enabled.

## 2. Required runtime configuration

Use Cloud Run environment variables and Secret Manager. Do not bake `.env` into the image.

| Variable                                        | Purpose                                                    |
| ----------------------------------------------- | ---------------------------------------------------------- |
| `SPRING_DATASOURCE_URL`                         | Neon JDBC URL, including `sslmode=require`                 |
| `SPRING_DATASOURCE_USERNAME`                    | Neon database user                                         |
| `SPRING_DATASOURCE_PASSWORD`                    | Neon database password, store as a secret                  |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET`     | Google OAuth app credentials                               |
| `LINKEDIN_CLIENT_ID` / `LINKEDIN_CLIENT_SECRET` | LinkedIn OAuth app credentials                             |
| `JWT_SECRET`                                    | Long random JWT signing secret, store as a secret          |
| `OAUTH2_COOKIE_SECRET`                          | Long random OAuth cookie signing secret, store as a secret |
| `APP_BASE_URL`                                  | Public backend URL, `https://api.tuestudio.com`            |
| `OAUTH2_FRONTEND_REDIRECT`                      | Public frontend OAuth callback URL                         |
| `APP_CORS_ALLOWED_ORIGINS`                      | Comma-separated frontend origins                           |
| `APP_COOKIE_SECURE`                             | `true` in production HTTPS                                 |
| `APP_COOKIE_SAME_SITE`                          | Usually `Lax` for OAuth redirects                          |

Local/demo seeders now run only with the `dev` Spring profile. Enable them locally with `SPRING_PROFILES_ACTIVE=dev`; do not set that in Cloud Run.

## 3. Build and deploy

Set these shell variables first:

```bash
PROJECT_ID="your-gcp-project"
REGION="us-central1"
REPOSITORY="tuestudio"
SERVICE="tuestudio-backend"
IMAGE="$REGION-docker.pkg.dev/$PROJECT_ID/$REPOSITORY/$SERVICE:$(git rev-parse --short HEAD)"
```

Create the Artifact Registry repository once:

```bash
gcloud artifacts repositories create "$REPOSITORY" \
  --project "$PROJECT_ID" \
  --repository-format=docker \
  --location "$REGION"
```

Build and push from the repository root:

```bash
gcloud builds submit --project "$PROJECT_ID" --tag "$IMAGE"
```

Create secrets once, then deploy using secret references:

```bash
printf '%s' '<neon-password>' | gcloud secrets create tuestudio-db-password --project "$PROJECT_ID" --data-file=-
printf '%s' '<jwt-secret>' | gcloud secrets create tuestudio-jwt-secret --project "$PROJECT_ID" --data-file=-
printf '%s' '<oauth2-cookie-secret>' | gcloud secrets create tuestudio-oauth2-cookie-secret --project "$PROJECT_ID" --data-file=-
printf '%s' '<google-secret>' | gcloud secrets create tuestudio-google-client-secret --project "$PROJECT_ID" --data-file=-
printf '%s' '<linkedin-secret>' | gcloud secrets create tuestudio-linkedin-client-secret --project "$PROJECT_ID" --data-file=-
```

```bash
gcloud run deploy "$SERVICE" \
  --project "$PROJECT_ID" \
  --region "$REGION" \
  --image "$IMAGE" \
  --allow-unauthenticated \
  --min-instances=0 \
  --max-instances=1 \
  --cpu=1 \
  --memory=512Mi \
  --port=8080 \
  --cpu-throttling \
  --set-env-vars="^~^SPRING_DATASOURCE_URL=jdbc:postgresql://<neon-host>/<database>?sslmode=require~SPRING_DATASOURCE_USERNAME=<neon-user>~GOOGLE_CLIENT_ID=<google-client-id>~LINKEDIN_CLIENT_ID=<linkedin-client-id>~APP_BASE_URL=https://api.tuestudio.com~OAUTH2_FRONTEND_REDIRECT=https://tuestudio.com/oauth2/callback~APP_CORS_ALLOWED_ORIGINS=https://tuestudio.com~APP_COOKIE_SECURE=true~APP_COOKIE_SAME_SITE=Lax~SPRING_FLYWAY_BASELINE_ON_MIGRATE=false" \
  --set-secrets="SPRING_DATASOURCE_PASSWORD=tuestudio-db-password:latest,JWT_SECRET=tuestudio-jwt-secret:latest,OAUTH2_COOKIE_SECRET=tuestudio-oauth2-cookie-secret:latest,GOOGLE_CLIENT_SECRET=tuestudio-google-client-secret:latest,LINKEDIN_CLIENT_SECRET=tuestudio-linkedin-client-secret:latest"
```

If startup memory is tight, redeploy with `--memory=1Gi` and measure cold starts.

## 4. Configure `api.tuestudio.com`

Map the custom domain to the Cloud Run service:

```bash
gcloud beta run domain-mappings create \
  --project "$PROJECT_ID" \
  --region "$REGION" \
  --service "$SERVICE" \
  --domain api.tuestudio.com
```

Then add the DNS records printed by Google Cloud. Wait for certificate provisioning before using the OAuth callbacks.

## 5. OAuth callback URLs

Add these redirect URIs in the OAuth provider consoles:

```text
https://api.tuestudio.com/login/oauth2/code/google
https://api.tuestudio.com/login/oauth2/code/linkedin
```

`server.forward-headers-strategy=framework` is enabled so Spring sees the public HTTPS scheme/host behind Cloud Run.

## 6. Cost guardrails

- Keep `min instances = 0`.
- Start with `max instances = 1`; raise to `2` only if needed.
- Keep CPU request-based with `--cpu-throttling`.
- Start with `512Mi` memory, then move to `1Gi` only if measured startup/runtime needs it.
- Create a Google Cloud budget alert at USD 1-2.
- Periodically delete old Artifact Registry images.

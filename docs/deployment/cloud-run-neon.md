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

### Current temporary public URLs

Until `api.tuestudio.com` is mapped and resolving in DNS, production uses the Cloud Run generated URL and the Cloudflare Pages frontend:

```text
APP_BASE_URL=https://tuestudio-backend-qf3nj3kama-uc.a.run.app
APP_CORS_ALLOWED_ORIGINS=https://tuestudio-front.pages.dev
OAUTH2_FRONTEND_REDIRECT=https://tuestudio-front.pages.dev/oauth2/callback
```

The same values are configured as GitHub repository variables so future CD runs preserve them.

OAuth provider consoles must allow the generated Cloud Run callback while this temporary setup is active:

```text
https://tuestudio-backend-qf3nj3kama-uc.a.run.app/login/oauth2/code/google
https://tuestudio-backend-qf3nj3kama-uc.a.run.app/login/oauth2/code/linkedin
```

Switch these values back to `https://api.tuestudio.com` after the custom domain mapping and DNS are ready.

## 3. GitHub Actions CD

Production deploys are automated by `.github/workflows/cd-cloud-run.yml`.

What it does:

1. waits for the existing `CI` workflow to finish successfully on a push to `main`;
2. checks out the same commit that CI validated;
3. authenticates to Google Cloud through GitHub OIDC / Workload Identity Federation;
4. builds the existing `Dockerfile` image;
5. pushes an immutable `${GITHUB_SHA}` tag to Artifact Registry;
6. deploys that image to Cloud Run with the cost guardrails below.

Manual fallback is available from the GitHub Actions UI with `workflow_dispatch`, but production deploys must be run from `main`.

Configure these GitHub repository or `production` environment variables:

| GitHub variable                     | Purpose                                                           |
| ----------------------------------- | ----------------------------------------------------------------- |
| `GCP_PROJECT_ID`                    | Google Cloud project ID                                           |
| `GCP_REGION`                        | Cloud Run and Artifact Registry region, for example `us-central1` |
| `GAR_REPOSITORY`                    | Artifact Registry Docker repository name                          |
| `CLOUD_RUN_SERVICE`                 | Cloud Run service name                                            |
| `GCP_WORKLOAD_IDENTITY_PROVIDER`    | Full Workload Identity provider resource name                     |
| `GCP_SERVICE_ACCOUNT`               | Deploy service account email used by GitHub Actions               |
| `SPRING_DATASOURCE_URL`             | Neon JDBC URL, including `sslmode=require`                        |
| `SPRING_DATASOURCE_USERNAME`        | Neon database user                                                |
| `GOOGLE_CLIENT_ID`                  | Google OAuth client ID                                            |
| `LINKEDIN_CLIENT_ID`                | LinkedIn OAuth client ID                                          |
| `APP_BASE_URL`                      | Public backend URL, usually `https://api.tuestudio.com`           |
| `OAUTH2_FRONTEND_REDIRECT`          | Public frontend OAuth callback URL                                |
| `APP_CORS_ALLOWED_ORIGINS`          | Comma-separated frontend origins                                  |
| `CLOUD_RUN_RUNTIME_SERVICE_ACCOUNT` | Optional Cloud Run runtime service account email                  |

Keep application secrets in Google Secret Manager, not in GitHub:

```text
tuestudio-db-password
tuestudio-jwt-secret
tuestudio-oauth2-cookie-secret
tuestudio-google-client-secret
tuestudio-linkedin-client-secret
```

One-time Google Cloud setup checklist:

```bash
PROJECT_ID="your-gcp-project"
PROJECT_NUMBER="$(gcloud projects describe "$PROJECT_ID" --format='value(projectNumber)')"
REGION="us-central1"
REPOSITORY="tuestudio"
POOL_ID="github-actions"
PROVIDER_ID="github"
DEPLOYER_SA="tuestudio-github-deployer@$PROJECT_ID.iam.gserviceaccount.com"
RUNTIME_SA="tuestudio-cloud-run@$PROJECT_ID.iam.gserviceaccount.com"
REPO="martelligiovi/TuEstudio-Back"

gcloud services enable \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  secretmanager.googleapis.com \
  iamcredentials.googleapis.com \
  sts.googleapis.com \
  --project "$PROJECT_ID"

gcloud artifacts repositories create "$REPOSITORY" \
  --project "$PROJECT_ID" \
  --repository-format=docker \
  --location "$REGION"

gcloud iam service-accounts create tuestudio-github-deployer --project "$PROJECT_ID"
gcloud iam service-accounts create tuestudio-cloud-run --project "$PROJECT_ID"

gcloud projects add-iam-policy-binding "$PROJECT_ID" \
  --member="serviceAccount:$DEPLOYER_SA" \
  --role="roles/artifactregistry.writer"

gcloud projects add-iam-policy-binding "$PROJECT_ID" \
  --member="serviceAccount:$DEPLOYER_SA" \
  --role="roles/run.admin"

gcloud iam service-accounts add-iam-policy-binding "$RUNTIME_SA" \
  --project "$PROJECT_ID" \
  --member="serviceAccount:$DEPLOYER_SA" \
  --role="roles/iam.serviceAccountUser"

gcloud iam workload-identity-pools create "$POOL_ID" \
  --project "$PROJECT_ID" \
  --location="global" \
  --display-name="GitHub Actions"

gcloud iam workload-identity-pools providers create-oidc "$PROVIDER_ID" \
  --project "$PROJECT_ID" \
  --location="global" \
  --workload-identity-pool="$POOL_ID" \
  --display-name="GitHub" \
  --issuer-uri="https://token.actions.githubusercontent.com" \
  --attribute-mapping="google.subject=assertion.sub,attribute.actor=assertion.actor,attribute.repository=assertion.repository" \
  --attribute-condition="assertion.repository == '$REPO'"

gcloud iam service-accounts add-iam-policy-binding "$DEPLOYER_SA" \
  --project "$PROJECT_ID" \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/projects/$PROJECT_NUMBER/locations/global/workloadIdentityPools/$POOL_ID/attribute.repository/$REPO"
```

Grant the runtime service account access to each Secret Manager secret:

```bash
for secret in \
  tuestudio-db-password \
  tuestudio-jwt-secret \
  tuestudio-oauth2-cookie-secret \
  tuestudio-google-client-secret \
  tuestudio-linkedin-client-secret; do
  gcloud secrets add-iam-policy-binding "$secret" \
    --project "$PROJECT_ID" \
    --member="serviceAccount:$RUNTIME_SA" \
    --role="roles/secretmanager.secretAccessor"
done
```

Then set `GCP_WORKLOAD_IDENTITY_PROVIDER` to:

```text
projects/<PROJECT_NUMBER>/locations/global/workloadIdentityPools/github-actions/providers/github
```

The workflow deploys with `--min-instances=0`, `--max-instances=1`, `--cpu=1`, `--memory=512Mi`, `--port=8080`, `--cpu-throttling`, and `--allow-unauthenticated`.

## 4. Build and deploy manually

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

## 5. Configure `api.tuestudio.com`

Map the custom domain to the Cloud Run service:

```bash
gcloud beta run domain-mappings create \
  --project "$PROJECT_ID" \
  --region "$REGION" \
  --service "$SERVICE" \
  --domain api.tuestudio.com
```

Then add the DNS records printed by Google Cloud. Wait for certificate provisioning before using the OAuth callbacks.

## 6. OAuth callback URLs

Add these redirect URIs in the OAuth provider consoles:

```text
https://api.tuestudio.com/login/oauth2/code/google
https://api.tuestudio.com/login/oauth2/code/linkedin
```

`server.forward-headers-strategy=framework` is enabled so Spring sees the public HTTPS scheme/host behind Cloud Run.

## 7. Cost guardrails

- Keep `min instances = 0`.
- Start with `max instances = 1`; raise to `2` only if needed.
- Keep CPU request-based with `--cpu-throttling`.
- Start with `512Mi` memory, then move to `1Gi` only if measured startup/runtime needs it.
- Create a Google Cloud budget alert at USD 1-2.
- Periodically delete old Artifact Registry images.

# Configuration and Deployment

## Runtime properties

Values shown as placeholders or repository defaults only. Never commit real credentials.

| Property | Current value / source | Required / purpose |
|---|---|---|
| `spring.application.name` | `ai-platform` | Application identity. |
| `spring.data.mongodb.uri` | `mongodb://localhost:27017/aiplatform` | Legacy checked-in connection property; Boot 4.1.1 instead binds `spring.mongodb.uri`. See the database-target warning below before changing it. |
| `spring.data.mongodb.auto-index-creation` | `true` | Enables Spring Data index creation from annotations. |
| `jwt.secret` | `${JWT_SECRET}` | Required signing secret. `JWT_SECRET` must be set in the process environment. |
| `omniroute.base-url` | `http://localhost:20128` | OmniRoute-compatible API base URL. Override for the actual target environment. |
| `omniroute.api-key` | `${OMNIROUTE_API_KEY}` | Required bearer credential for OmniRoute requests. |
| `omniroute.read-timeout` | `120s` (Java property default) | Gateway read/response deadline, from 1 millisecond through `Long.MAX_VALUE` milliseconds; connection timeout remains 30 seconds. |
| `cors.allowed-origins` | `http://localhost:5173` (Java property default) | Explicit browser-origin allowlist. Wildcards are rejected; an empty list disables cross-origin access. |
| `ai.default-model` | `auto/best-coding` | Model selected for absent/blank conversation model; this configured default is accepted without checking gateway membership. |
| `ai.pricing.default-input-price-per-million` | `0.50` | Bound default input price; use is governed by pricing service logic. |
| `ai.pricing.default-output-price-per-million` | `2.00` | Bound default output price; use is governed by pricing service logic. |

No server port override is declared, so Spring Boot's standard default applies unless supplied externally. No explicit logging configuration or deployment profile was found.

## Environment variables

| Variable | Required | Purpose | Safe example |
|---|---|---|---|
| `JWT_SECRET` | Yes | Supplies `jwt.secret` for signing/validating JWTs. Use a strong random secret of adequate length for the configured signing algorithm. | `<generate-a-long-random-secret>` |
| `OMNIROUTE_API_KEY` | Yes for OmniRoute calls | Bearer credential sent to the configured gateway. | `<your-gateway-api-key>` |

`omniroute.base-url` and the Boot 4 connection property `spring.mongodb.uri` support standard Spring configuration overrides (for example `OMNIROUTE_BASE_URL` and `SPRING_MONGODB_URI`). They are not explicit environment-variable placeholders in the checked-in file.

### Database-target warning

The repository declares `spring.data.mongodb.uri`, but the installed Spring Boot 4.1.1 connection configuration uses `spring.mongodb.uri`. Without an effective external override, the legacy property may be ignored and the database may be Boot's `test` default rather than `aiplatform`. A successful connection to localhost alone does not establish the selected database.

No property rename or data migration was performed. Before changing an existing installation, confirm the effective connection configuration and where its data lives; then select the intended database and plan any migration explicitly. Do not log credentials or dump connection strings containing secrets. `spring.data.mongodb.auto-index-creation` is still valid and must not be renamed along with the URI.

## Local setup

1. Install JDK 17.
2. Start MongoDB. Confirm the effective database target as described above; for a fresh installation, explicitly supply the intended Boot 4 `spring.mongodb.uri` value. Do not switch an existing installation's target without reviewing its data location.
3. Start/reach an OmniRoute-compatible service at `omniroute.base-url` and obtain a key.
4. Set `JWT_SECRET` and `OMNIROUTE_API_KEY` in the shell, then run:

Windows PowerShell:

```powershell
$env:JWT_SECRET = '<generate-a-long-random-secret>'
$env:OMNIROUTE_API_KEY = '<your-gateway-api-key>'
.\mvnw.cmd spring-boot:run
```

Linux/macOS or POSIX shell:

```sh
export JWT_SECRET='<generate-a-long-random-secret>'
export OMNIROUTE_API_KEY='<your-gateway-api-key>'
./mvnw spring-boot:run
```

The Maven Wrapper scripts are included. Build with `./mvnw package` (or `mvnw.cmd package` on Windows); execute tests with `./mvnw test` (or `mvnw.cmd test`).

## Security and integration behavior

- JWT authentication is stateless; tokens expire after one hour in `JwtTokenProvider`.
- Passwords are encoded with BCrypt; do not store plaintext passwords.
- CORS defaults to `http://localhost:5173`; replace it per environment with `cors.allowed-origins`, for example `https://frontend.example.com,https://admin.example.com` (or `CORS_ALLOWED_ORIGINS`). It still applies only to `/api/**`, with GET, POST, PUT, PATCH, DELETE, OPTIONS and Authorization/Content-Type headers. No wildcard patterns or broader authorization are introduced.
- OmniRoute calls use HTTP bearer authorization. The JDK client retains its 30-second connect timeout and has a configurable 120-second read/response deadline (`omniroute.read-timeout`, or `OMNIROUTE_READ_TIMEOUT`, e.g. `180s`). Expiry follows existing provider-failure/502 handling. There are no automatic retries; local timeout does not guarantee the upstream provider stopped generation.
- No payment gateway integration, container recipe, orchestration manifest, production profile, or deployment pipeline was found in the inspected repository. Those would require environment-specific setup and validation.

## Safe operations

- Supply secret values through a secret manager or process environment; never put real secrets in source control, screenshots, issue reports, or documentation.
- Use HTTPS and a deployment-appropriate restricted CORS origin outside local development.
- Restrict MongoDB network access and credentials for the deployed environment; the default URI is local development configuration and contains no authentication.
- Set external endpoint and database values for each environment without editing source.
- Verify gateway availability and model/pricing configuration before enabling AI generation. No deployed endpoint or production readiness is asserted here.

Related: [README quick start](../README.md), [architecture](ARCHITECTURE.md), [testing](TESTING.md).

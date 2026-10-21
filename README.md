## AI Platform Backend

A Spring Boot REST backend for account management, conversations, AI-assisted messages through OmniRoute, usage/cost tracking, credit balances, subscriptions, transactions, and support tickets. This describes the implementation present in this repository; it is not a claim that an external payment processor or production deployment is configured.

## Implemented capabilities

- Username/password registration and login with BCrypt password hashing and stateless JWT authentication.
- MongoDB persistence for users, conversations, messages, AI requests, usage records, balances, pricing, plans, subscriptions, transactions, and tickets.
- Paginated model catalog from OmniRoute and conversation model selection.
- AI message generation through OmniRoute, token-based cost calculation, selected-credit-source consumption, and usage recording.
- User/admin APIs for plans, subscriptions, transactions, tickets, usage, and KPI summaries.

No separately marked roadmap or planned-feature source was found. Features not evidenced in the current controllers/services are not described as implemented. See [known issues](docs/KNOWN_ISSUES.md) for verified caveats.

## Technology

| Technology  | Version/evidence                                                   |
| ----------- | ------------------------------------------------------------------ |
| Java        | 17 (`pom.xml`)                                                     |
| Spring Boot | 4.1.1 (parent in `pom.xml`)                                        |
| MongoDB     | Spring Data MongoDB; local default URI in `application.properties` |
| JWT         | JJWT 0.12.6                                                        |
| Build       | Maven Wrapper (`mvnw`, `mvnw.cmd`)                                 |

## Prerequisites

- JDK 17.
- MongoDB reachable at the configured URI (default: local MongoDB on port 27017).
- An OmniRoute-compatible service reachable at the configured base URL and an API key for model listing/generation.
- Maven Wrapper scripts are included; a separate Maven installation is not required.

## Configure and run

The checked-in defaults target local development. Set the required secrets in the process environment; do not commit credentials.

PowerShell:

```powershell
$env:JWT_SECRET = '<a sufficiently long random secret>'
$env:OMNIROUTE_API_KEY = '<OmniRoute API key>'
.\mvnw.cmd spring-boot:run
```

POSIX shell:

```sh
export JWT_SECRET='<a sufficiently long random secret>'
export OMNIROUTE_API_KEY='<OmniRoute API key>'
./mvnw spring-boot:run
```

**MongoDB configuration requires review:** the checked-in `spring.data.mongodb.uri` declares `mongodb://localhost:27017/aiplatform`, but Spring Boot 4.1.1 binds connection configuration under `spring.mongodb.uri`. Do not assume the declared database is effective, or rename the property on an existing installation without confirming current data location and environment overrides. See [configuration and deployment](docs/CONFIGURATION_AND_DEPLOYMENT.md). The OmniRoute base URL defaults to `http://localhost:20128`.

Build a package:

```sh
./mvnw package
```

On Windows use `mvnw.cmd package`. Run tests with `./mvnw test` or `mvnw.cmd test`. Test results are not implied by these instructions; see [testing](docs/TESTING.md) for the repository test inventory and the verification performed for this documentation update.

## API

The API base path is `/api`. Register and log in at `/api/auth/register` and `/api/auth/login`; most other requests require `Authorization: Bearer <JWT>`. Browser CORS defaults to `http://localhost:5173`; deployment origins can be explicitly configured with `cors.allowed-origins`. No Swagger/OpenAPI configuration was found in this checkout. A project Postman collection exists, but its location has not yet been supplied. The full verified route reference is [docs/API_PROTOCOL.md](docs/API_PROTOCOL.md).

## Repository layout

- `src/main/java/dev/ramtin/aiplatform/` — Spring application, REST controllers, DTOs, services, Mongo repositories, security, OmniRoute integration, and business modules.
- `src/main/resources/application.properties` — application and integration defaults.
- `src/test/java/dev/ramtin/aiplatform/` — unit and Spring-oriented tests by feature.
- `docs/` — API and technical documentation.
- `mvnw`, `mvnw.cmd`, `.mvn/wrapper/` — Maven Wrapper.

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [REST API protocol](docs/API_PROTOCOL.md)
- [Data model](docs/DATA_MODEL.md)
- [Business flows](docs/BUSINESS_FLOWS.md)
- [Configuration and deployment](docs/CONFIGURATION_AND_DEPLOYMENT.md)
- [Testing](docs/TESTING.md)
- [Known issues and limitations](docs/KNOWN_ISSUES.md)

`HELP.md` is the generated Spring Boot reference sheet; it is not the project setup guide and includes a stale package-name note.

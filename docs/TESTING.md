# Testing and Verification

## Test stack and commands

The project uses the Spring Boot Maven parent and test starter dependencies for MongoDB, Security, Validation, and MVC. Run from the repository root:

```sh
./mvnw test
```

On Windows:

```powershell
.\mvnw.cmd test
```

To build the application and run tests as part of the package lifecycle, use `./mvnw package` or `mvnw.cmd package`.

## Existing test organization

Test sources are under `src/test/java/dev/ramtin/aiplatform/`. The 20 discovered Java test classes cover:

| Feature | Test classes |
|---|---|
| Application context | `AiPlatformApplicationTests` |
| AI and gateway | `AIServiceImplTest`, `OmniRouteClientImplTest` |
| Authentication and security | `AuthServiceImplTest`, `CustomUserDetailsServiceTest`, `JwtAuthenticationFilterTest`, `JwtTokenProviderTest`, `CustomAuthenticationEntryPointTest`, `CustomAccessDeniedHandlerTest`, `SecurityConfigTest` |
| Conversations and messages | `ConversationServiceImplTest`, `MessageServiceImplTest` |
| Pricing | `CostCalculatorTest`, `ModelPricingServiceImplTest` |
| Plans, subscriptions, transactions | `PlanServiceImplTest`, `SubscriptionServiceImplTest`, `TransactionServiceImplTest` |
| Balance/credit | `UserBalanceServiceImplTest` |
| Users | `UserServiceImplTest` |
| Tickets | `TicketServiceImplTest` |

This table records the original test inventory, not a line-coverage assertion. A project Postman collection exists; its location is pending verification. No collection/environment file was located in this checkout.

## Added regression and investigation tests

| Test class | Scope |
|---|---|
| `ConversationControllerValidationTest` | Real Bean Validation/advice in standalone MockMvc; invalid titles, existing boundaries, optional update fields and default-model selection. |
| `CorsConfigurationTest` | Property binding and actual preflights for default/custom/empty allowlists; rejected wildcard, origin, method and header inputs. |
| `OmniRoutePropertiesTest` | Default/boundary/custom timeout binding and invalid values. |
| `OmniRouteClientTimeoutTest` | Production transport against loopback HTTP: success, delayed headers, stalled body and HTTP errors for both gateway endpoints; no retries. |
| `MessageControllerContractTest` | Accepted but ignored legacy model field and existing flat page metadata. |
| `ModelCatalogControllerContractTest` | Nested direct-page shape, default size 20 and existing resolver cap 2000. |
| `MonetaryCasStorageGateTest` | Opt-in storage investigation, **not production service concurrency coverage**. Decimal128 mapping, CAS races/status writes, and incompatibility with seeded string-backed amounts. |

`SecurityConfigTest` additionally checks both role-update aliases for anonymous, USER and ADMIN callers, with a mocked user service so no actual role is changed.

The storage investigation is disabled by default. To run it explicitly against local MongoDB:

```powershell
.\mvnw.cmd '-Dtest=MonetaryCasStorageGateTest' '-Dstorage.cas.mongo=true' test
```

It uses newly created `storage_cas_fixture_<UUID>` databases on `localhost:27017`, without loading application configuration or reading existing records. Each fixture drops only its own database after successful creation. It requires permission to create/drop disposable databases. A green result includes the expected finding that default mapped decimal CAS does not match seeded string values; it does not make the unchanged balance/subscription services concurrency-safe.

## Verification performed

Latest execution on **2026-10-21**, Windows/JDK 17:

```powershell
.\mvnw.cmd '-Dstorage.cas.mongo=true' test
```

**BUILD SUCCESS — 175 tests, 0 failures, 0 errors, 0 skipped**, including all seven opt-in storage fixtures. Creation and cleanup were observed for all seven generated databases. Required test credentials were supplied through process environment, not committed. Loopback HTTP tests made no live OmniRoute requests. Relative Markdown links resolved across all eight documents and `git diff --check` passed (Git issued an LF/CRLF advisory).

The ordinary suite skips the seven storage cases unless explicitly enabled. Existing Spring context/security tests still initialize local MongoDB; connection alone does not establish the effective application database. Existing unused-import/deprecation/type-safety warnings remain outside this patch's scope.

The original pre-change documentation baseline was `mvnw.cmd test`: 113 tests, no failures/errors/skips. Neither result establishes production behavior or monetary concurrency safety.

## Coverage gaps visible from the test inventory

- New standalone MVC tests exercise selected controller contracts and validation; security tests exercise selected authenticated routes. This is not exhaustive end-to-end endpoint coverage.
- No repository-specific test class is apparent for custom `MongoTemplate` search and aggregation implementations.
- File-name inventory alone cannot establish test assertions or completeness; inspect test methods before relying on a specific scenario.

For authentication prerequisites on protected routes, see [API protocol](API_PROTOCOL.md). For configuration needed to run the application, see [configuration and deployment](CONFIGURATION_AND_DEPLOYMENT.md).

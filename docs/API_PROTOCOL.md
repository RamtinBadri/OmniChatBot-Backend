# REST API Protocol

This reference reflects controller mappings, request/response DTOs, security rules and service behavior in the current source. It supersedes the previous aspirational contract where that document disagreed with the implementation. Unless stated otherwise, the API base path is `/api`, JSON bodies use `Content-Type: application/json`, and protected requests use `Authorization: Bearer <JWT>`.

## Authentication, authorization and errors

- Public: `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/plans`.
- `GET /api/users/me` requires an authenticated user.
- `/api/admin/**` is restricted to `ADMIN`.
- `GET /api/users`, `GET /api/users/{userId}`, `DELETE /api/users/{userId}`, and `PUT /api/users/{userId}/role` require `ADMIN`.
- Other routes require authentication. User-scoped services verify ownership for conversations, tickets, usage, transactions, balance and subscriptions where applicable.
- CORS permits only configured `cors.allowed-origins`, defaulting to `http://localhost:5173` (see [configuration](CONFIGURATION_AND_DEPLOYMENT.md)).

Common application error body:

```json
{
  "status": 404,
  "message": "Resource not found",
  "timestamp": "2026-01-01T12:00:00"
}
```

`ErrorResponse` fields are `status`, `message`, `timestamp`. Explicit mappings include 400 validation/type mismatch/illegal argument/credit source missing; 401 bad credentials or security entry-point failure; 403 access denied; 404 missing resource; 409 conflicts/duplicate keys; 422 `InsufficientCreditException` or `ModelPricingNotFoundException`; 502 AI provider failure; otherwise 500. Balance insufficiency uses 422, but subscription insufficiency/expiry currently uses `IllegalStateException` and therefore 500. Missing active model-specific pricing uses configured fallback rates rather than the missing-pricing exception. Security filter errors use separate entry-point/denied handlers. Exact messages can vary by exception.

## Pagination and querying

Spring `Pageable` endpoints accept `page`, `size`, and repeated/combined `sort` conventions, for example `?page=0&size=20&sort=createdAt,desc`. The default page is 0, default size is 20, and the resolver caps requested size at 2000. The user ticket list's `@PageableDefault` overrides size to 10 and sort to `createdAt,DESC`. Other lists are unsorted unless configured/requested. Existing direct page JSON is retained, not replaced by a new envelope. For Mongo-backed listings, pageable constraints are applied to repository queries or `MongoTemplate` queries after a separate filtered count. `GET /api/models` instead fetches the complete OmniRoute model list and slices it in memory, retaining provider order and ignoring requested sort. Query filters listed below are only those exposed by controllers.

## Authentication and users

| Method and path | Access | Request | Success |
|---|---|---|---|
| `POST /api/auth/register` | Public | `RegisterRequest`: `username`, `email`, `password`, `firstName`, `lastName`; all nonblank; email format validated. | 201, no body. Creates active USER, hashes password and creates balance. |
| `POST /api/auth/login` | Public | `LoginRequest`: nonblank `username`, `password`. | 200, JWT string; token expiry is one hour. |
| `GET /api/users/me` | Authenticated | — | 200 `UserDTO`. |
| `GET /api/users` | ADMIN | Optional `search`, `role`, `status`; `Pageable`. | 200 `Page<UserDTO>`. Search covers email, username, first and last names (case-insensitive regex). |
| `GET /api/users/{userId}` | ADMIN | Path `userId`. | 200 `UserDTO`. |
| `PUT /api/users/{userId}` | Authenticated; self only | `UpdateUserRequest`: valid nonblank `email`, `firstName`, `lastName`. | 200 `UserDTO`. |
| `PUT /api/users/{userId}/credit-source` | Authenticated; self only | `UpdateCreditSourceRequest`: `creditSource` enum; null clears the selection. | 204. |
| `DELETE /api/users/{userId}` | ADMIN | Path `userId`. | 204. |
| `PUT /api/users/{userId}/role` | ADMIN | `UpdateUserRoleRequest`: non-null `role` enum. | 204. |
| `PATCH /api/admin/users/{userId}/role` | ADMIN | Role request body with non-null `role`. | 204. A second role-change route also exists at the user namespace above. |

## Models and conversations

| Method and path | Access | Request/query | Success |
|---|---|---|---|
| `GET /api/models` | Authenticated | `Pageable`. | 200 `ModelCatalogDTO`, containing `defaultModel` and a paginated available-model page from OmniRoute. |
| `POST /api/conversations` | Authenticated | `CreateConversationRequest`: required nonblank `title`; optional `model`. | 201 `ConversationDTO`; blank/absent model resolves to catalog default. The configured default is accepted without gateway membership verification; other unavailable models are rejected. |
| `GET /api/conversations` | Authenticated, own records | Optional `search`, `status`; `Pageable`. | 200 `Page<ConversationDTO>`. Search is case-insensitive title matching. |
| `GET /api/conversations/{conversationId}` | Authenticated, owner only | Path ID. | 200 `ConversationDTO`. |
| `PUT /api/conversations/{conversationId}` | Authenticated, owner only | Optional `title` (max 200 if supplied) and `model` in `UpdateConversationRequest`. | 200 `ConversationDTO`; blank title or unavailable model rejected. |
| `PATCH /api/conversations/{conversationId}/archive` | Authenticated, owner only | Path ID. | 204; status set to `ARCHIVED`. |
| `DELETE /api/conversations/{conversationId}` | Authenticated, owner only | Path ID. | 204; messages are deleted before conversation. |

## Messages and AI generation

| Method and path | Access | Request/query | Success |
|---|---|---|---|
| `POST /api/conversations/{conversationId}/messages` | Authenticated; conversation ownership checked | `SendMessageRequest`: required nonblank `content`; DTO also has optional `model`. | 201 `MessageExchangeDTO` (user and assistant messages). The controller currently passes content only; generation uses conversation's stored model, so request `model` is not applied. |
| `GET /api/conversations/{conversationId}/messages` | Authenticated; conversation ownership checked | `Pageable`. | 200 `Page<MessageDTO>`. |
| `GET /api/conversations/{conversationId}/messages/{messageId}` | Authenticated; owner and conversation membership checked | Path IDs. | 200 `MessageDTO`. |

AI requests themselves do not have a standalone client-facing controller route. A message triggers `OmniRoute` generation; provider request uses `/v1/chat/completions`, and catalog retrieval uses `/v1/models`. AI request status and usage/cost are described in [business flows](BUSINESS_FLOWS.md).

## Tickets

| Method and path | Access | Request/query | Success |
|---|---|---|---|
| `POST /api/tickets` | Authenticated; current user assigned | `CreateTicketRequest`: nonblank `subject`, `description`. | 201 `TicketDTO`. |
| `GET /api/tickets` | Authenticated; own tickets | Optional `search`, `status`; pageable defaults size 10, sort `createdAt,DESC`. | 200 `Page<TicketDTO>`. Search examines subject and description. |
| `GET /api/tickets/{ticketId}` | Authenticated; own ticket only | Path ID. | 200 `TicketDTO`. |
| `GET /api/admin/tickets` | ADMIN | Optional `search`, `status`, `userId`; `Pageable`. | 200 `Page<TicketDTO>`. |
| `GET /api/admin/tickets/{ticketId}` | ADMIN | Path ID. | 200 `TicketDTO`. |
| `POST /api/admin/tickets/{ticketId}/replies` | ADMIN | `ReplyToTicketRequest`: nonblank `message`. | 201, no body; records history. |
| `PATCH /api/admin/tickets/{ticketId}/status` | ADMIN | `UpdateTicketStatusRequest`: non-null status enum. | 204; records history. |
| `GET /api/admin/tickets/{ticketId}/history` | ADMIN | `Pageable`. | 200 `Page<TicketHistoryDTO>`. |

## Usage, transactions, plans, subscriptions and balance

| Method and path | Access | Request/query | Success |
|---|---|---|---|
| `GET /api/usage` | Authenticated; current user | `Pageable`. | 200 `Page<UsageRecord>`. |
| `GET /api/usage/summary` | Authenticated; current user | — | 200 `UsageSummary`. |
| `GET /api/transactions` | Authenticated; current user | `Pageable`. | 200 `Page<Transaction>`. |
| `GET /api/plans` | Public | `Pageable`. | 200 `Page<Plan>` (not a plain unpaged list). |
| `POST /api/subscriptions` | Authenticated; current user assigned | `CreateSubscriptionRequest`: nonblank `planId`. | 200 `Subscription`. |
| `GET /api/subscriptions/current` | Authenticated; current user | — | 200 `Subscription`; absence may yield 404. |
| `GET /api/balance` | Authenticated; current user | — | 200 `UserBalance`. |
| `POST /api/balance/purchase` | Authenticated; current user | `PurchaseCreditRequest`: `amount` >= 0.01, nonblank `currency`, optional `reference`. | 200 `Transaction`; no external payment gateway is shown in this operation. |

## Administrative resources

| Method and path | Access | Request/query | Success |
|---|---|---|---|
| `GET /api/admin/usage` | ADMIN | Optional `userId`, `model`; `Pageable`. | 200 `Page<UsageRecord>`. |
| `GET /api/admin/transactions` | ADMIN | Optional `userId`, `type`, `status`; `Pageable`. | 200 `Page<Transaction>`. |
| `GET /api/admin/transactions/{transactionId}` | ADMIN | Path ID. | 200 `Transaction`. |
| `PUT /api/admin/transactions/{transactionId}/status` | ADMIN | Required query `status` (`TransactionStatus`). | 200 updated `Transaction`. |
| `POST /api/admin/plans` | ADMIN | JSON `Plan` entity. | 201 `Plan`. |
| `PUT /api/admin/plans/{planId}` | ADMIN | JSON `Plan` entity. | 200 `Plan`. |
| `GET /api/admin/plans/{planId}` | ADMIN | Path ID. | 200 `Plan`. |
| `DELETE /api/admin/plans/{planId}` | ADMIN | Path ID. | 204; deactivates the plan (`active=false`), rather than physically deleting it. |
| `GET /api/admin/plans/{planId}/subscriptions` | ADMIN | `Pageable`. | 200 `Page<Subscription>`. |
| `GET /api/admin/subscriptions` | ADMIN | Optional `userId`, `planId`, `status`; `Pageable`. | 200 `Page<Subscription>`. |
| `GET /api/admin/subscriptions/{subscriptionId}` | ADMIN | Path ID. | 200 `Subscription`. |
| `DELETE /api/admin/subscriptions/{subscriptionId}` | ADMIN | Path ID. | 204. |
| `GET /api/admin/kpi/summary` | ADMIN | — | 200 `KpiSummaryDTO`. |
| `GET /api/admin/kpi/usage` | ADMIN | Required `from`, `to` ISO `LocalDate` query parameters. | 200 `List<KpiUsageDTO>`; date range includes `from` and `to` calendar days. |

## Documentation boundaries

No Swagger/OpenAPI configuration was found in this checkout. A project Postman collection exists; its location is pending verification. There is no standalone AI request endpoint. The previous protocol's general claims about all endpoints supporting search/filter/sort or pagination were not universal: only controller-exposed parameters are listed here. For model fields and persistence details see [data model](DATA_MODEL.md); for configuration see [configuration and deployment](CONFIGURATION_AND_DEPLOYMENT.md).

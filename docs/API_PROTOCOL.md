# AI Platform — API Protocol

## 1. Overview

This document defines the API contract between the AI Platform backend and its clients.

The backend is responsible for:

* Authentication and authorization
* User management
* Conversation and message management
* AI request processing
* Token usage tracking
* Cost calculation
* Plans and subscriptions
* Credit and pay-as-you-go operations
* Transaction management
* Ticket management
* Usage and billing information
* Administrative management
* KPI and reporting

Clients must communicate with external AI providers through the backend. Clients must not access external AI providers directly.

---

# 2. Base URL

All API endpoints are relative to:

```text
/api
```

Example:

```http
POST /api/auth/login
```

---

# 3. Content Type

Requests and responses containing a body use JSON.

```http
Content-Type: application/json
```

---

# 4. Authentication

Authenticated endpoints use JWT-based authentication.

The token must be provided through the `Authorization` header:

```http
Authorization: Bearer <JWT>
```

Authentication is handled by the backend.

The backend extracts the authenticated user's identity from the JWT and must not rely on a client-provided `userId` for user-owned resources.

Public endpoints are explicitly marked as **Public**.

---

# 5. Authorization

The API uses role-based authorization.

The main roles are:

```text
USER
ADMIN
```

Administrative endpoints are located under:

```text
/api/admin/**
```

and require the `ADMIN` role.

Users may only access resources that belong to them.

Examples:

* A user may access only their own conversations.
* A user may access only messages belonging to their own conversations.
* A user may access only their own tickets.
* A user may access only their own usage records.
* A user may access only their own transactions.
* A user may access only their own subscription and balance.

Administrative users may access resources through the corresponding Admin APIs.

---

# 6. Common Response Convention

## 6.1 Successful Responses

Successful requests return an appropriate HTTP status code and, when applicable, a JSON response body.

Example:

```json
{
  "id": "64f123",
  "email": "user@example.com",
  "firstName": "Ramtin",
  "lastName": "Example",
  "userRole": "USER",
  "status": "ACTIVE"
}
```

---

## 6.2 Error Responses

API errors use the following structure:

```json
{
  "status": 404,
  "message": "User not found",
  "timestamp": "2026-08-30T12:00:00"
}
```

Fields:

| Field       | Description                      |
| ----------- | -------------------------------- |
| `status`    | HTTP status code                 |
| `message`   | Description of the error         |
| `timestamp` | Time at which the error occurred |

---

# 7. Pagination, Sorting, Search and Filtering

Collection endpoints that may return a large number of records support pagination.

Common query parameters are:

| Parameter | Description                 |
| --------- | --------------------------- |
| `page`    | Page number                 |
| `size`    | Number of records per page  |
| `sort`    | Sorting field and direction |

Example:

```http
GET /api/conversations?page=0&size=20&sort=updatedAt,desc
```

The backend must not retrieve an unbounded number of MongoDB documents for a single request.

Where supported, collection endpoints may also provide search and filtering parameters.

Examples:

```http
GET /api/users?status=ACTIVE&role=USER
```

```http
GET /api/conversations?status=ACTIVE&search=spring
```

```http
GET /api/tickets?status=OPEN&search=account
```

```http
GET /api/admin/usage?userId=user123&model=gpt-model
```

Pagination and filtering are performed at the database/repository level.

---

# 8. Authentication API

## 8.1 Register

### Endpoint

```http
POST /api/auth/register
```

### Authentication

Public.

### Request Body

```json
{
  "username": "ramtin",
  "email": "user@example.com",
  "password": "password123",
  "firstName": "Ramtin",
  "lastName": "Example"
}
```

### Behavior

A newly registered user receives:

```text
role = USER
status = ACTIVE
```

The client must not assign its own role.

### Response

**201 Created**

Returns the created user information according to the authentication API response contract.

### Possible Errors

* `400 Bad Request`
* `409 Conflict`

---

## 8.2 Login

### Endpoint

```http
POST /api/auth/login
```

### Authentication

Public.

### Request Body

```json
{
  "username": "ramtin",
  "password": "password123"
}
```

### Response

**200 OK**

Returns a JWT.

Example:

```text
eyJhbGciOi...
```

The token must subsequently be sent using:

```http
Authorization: Bearer <JWT>
```

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`

---

# 9. User API

## 9.1 Get Current User

### Endpoint

```http
GET /api/users/me
```

### Authentication

Required.

### Authorization

Any authenticated user.

The backend identifies the user from the JWT.

### Response

**200 OK**

Returns the authenticated user's `UserDTO`.

### Possible Errors

* `401 Unauthorized`

---

## 9.2 Get Users

### Endpoint

```http
GET /api/users
```

### Authentication

Required.

### Required Role

`ADMIN`

### Query Parameters

| Parameter | Description                 |
| --------- | --------------------------- |
| `page`    | Page number                 |
| `size`    | Number of users per page    |
| `sort`    | Sorting field and direction |
| `role`    | Filter by user role         |
| `status`  | Filter by user status       |
| `search`  | Search users                |

### Example

```http
GET /api/users?page=0&size=20&sort=createdAt,desc&status=ACTIVE
```

### Response

**200 OK**

Returns a paginated list of users.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`

---

## 9.3 Get User

### Endpoint

```http
GET /api/users/{id}
```

### Authentication

Required.

### Required Role

`ADMIN`

### Response

**200 OK**

Returns a `UserDTO`.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 9.4 Update User

### Endpoint

```http
PUT /api/users/{id}
```

### Authentication

Required.

### Authorization

A user may update only their own profile.

The user ID must correspond to the authenticated user's ID.

Role and account status cannot be modified through this endpoint.

### Request Body

```json
{
  "email": "new@example.com",
  "firstName": "Ramtin",
  "lastName": "NewName"
}
```

### Response

**200 OK**

Returns the updated `UserDTO`.

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`
* `409 Conflict`

---

## 9.5 Update User Role

### Endpoint

```http
PATCH /api/admin/users/{userId}/role
```

### Authentication

Required.

### Required Role

`ADMIN`

### Request Body

```json
{
  "role": "ADMIN"
}
```

### Response

**204 No Content**

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 9.6 Delete User

### Endpoint

```http
DELETE /api/users/{id}
```

### Authentication

Required.

### Required Role

`ADMIN`

### Response

**204 No Content**

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

# 10. Conversation API

## 10.1 Create Conversation

### Endpoint

```http
POST /api/conversations
```

### Authentication

Required.

### Request Body

```json
{
  "title": "Learning Spring Boot",
  "model": "default"
}
```

The `userId` must not be provided by the client.

The backend obtains the authenticated user's ID from the JWT.

### Response

**201 Created**

Returns a `ConversationDTO`.

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`

---

## 10.2 Get User Conversations

### Endpoint

```http
GET /api/conversations
```

### Authentication

Required.

### Authorization

Only conversations belonging to the authenticated user are returned.

### Query Parameters

| Parameter | Description                      |
| --------- | -------------------------------- |
| `page`    | Page number                      |
| `size`    | Number of conversations per page |
| `sort`    | Sorting field and direction      |
| `status`  | Filter by conversation status    |
| `search`  | Search conversation titles       |

### Example

```http
GET /api/conversations?page=0&size=20&sort=updatedAt,desc&status=ACTIVE
```

### Response

**200 OK**

Returns a paginated list of `ConversationDTO`.

### Possible Errors

* `401 Unauthorized`

---

## 10.3 Get Conversation

### Endpoint

```http
GET /api/conversations/{conversationId}
```

### Authentication

Required.

### Authorization

The authenticated user must own the conversation.

### Response

**200 OK**

Returns a `ConversationDTO`.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 10.4 Update Conversation

### Endpoint

```http
PUT /api/conversations/{conversationId}
```

### Authentication

Required.

### Authorization

The authenticated user must own the conversation.

### Request Body

```json
{
  "title": "Spring Boot Learning"
}
```

### Response

**200 OK**

Returns the updated `ConversationDTO`.

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 10.5 Archive Conversation

### Endpoint

```http
PATCH /api/conversations/{conversationId}/archive
```

### Authentication

Required.

### Authorization

The authenticated user must own the conversation.

### Response

**204 No Content**

The conversation status becomes:

```text
ARCHIVED
```

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

# 11. Message API

Messages are child resources of conversations.

```text
/api/conversations/{conversationId}/messages
```

---

## 11.1 Get Conversation Messages

### Endpoint

```http
GET /api/conversations/{conversationId}/messages
```

### Authentication

Required.

### Authorization

The authenticated user must own the specified conversation.

### Query Parameters

| Parameter | Description                 |
| --------- | --------------------------- |
| `page`    | Page number                 |
| `size`    | Number of messages per page |
| `sort`    | Sorting field and direction |

### Example

```http
GET /api/conversations/conv123/messages?page=0&size=50&sort=createdAt,asc
```

### Response

**200 OK**

Returns a paginated list of `MessageDTO`.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 11.2 Get Message

### Endpoint

```http
GET /api/conversations/{conversationId}/messages/{messageId}
```

### Authentication

Required.

### Authorization

The authenticated user must own the conversation.

The requested message must also belong to the specified conversation.

### Response

**200 OK**

Returns a `MessageDTO`.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 11.3 Send Message

### Endpoint

```http
POST /api/conversations/{conversationId}/messages
```

### Authentication

Required.

### Authorization

The authenticated user must own the specified conversation.

### Request Body

```json
{
  "content": "Explain dependency injection."
}
```

The authenticated user's ID is obtained from the JWT.

### Behavior

The endpoint:

1. Validates conversation ownership.
2. Stores the user message.
3. Sends the conversation messages to the AI service.
4. Processes the AI response.
5. Records the AI request and usage information.
6. Stores the assistant message.
7. Returns the message exchange.

The backend communicates with the configured AI provider through OmniRoute.

### Response

**201 Created**

Returns the created user and assistant message exchange according to the current `MessageExchangeDTO` contract.

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`
* `422 Unprocessable Entity`
* `502 Bad Gateway`
* `500 Internal Server Error`

---

# 12. AI Request Processing

AI requests are represented separately from messages.

An `AIRequest` stores information about an individual AI generation request.

Important information includes:

```text
userId
conversationId
requestedModel
actualModel
status
requestTime
responseTime
```

AI request statuses include:

```text
PENDING
PROCESSING
SUCCESS
FAILED
TIMEOUT
RATE_LIMITED
PROVIDER_ERROR
```

The backend is responsible for calculating and recording the cost of an AI request.

The client must not calculate or override the charged cost.

---

# 13. Usage API

## 13.1 Get Usage History

### Endpoint

```http
GET /api/usage
```

### Authentication

Required.

### Authorization

Only usage records belonging to the authenticated user are returned.

### Query Parameters

| Parameter | Description                 |
| --------- | --------------------------- |
| `page`    | Page number                 |
| `size`    | Number of records per page  |
| `sort`    | Sorting field and direction |

### Response

**200 OK**

Returns a paginated list of `UsageRecord`.

A usage record contains information such as:

```json
{
  "id": "usage123",
  "aiRequestId": "request123",
  "userId": "user456",
  "conversationId": "conversation123",
  "model": "model-name",
  "inputTokens": 100,
  "outputTokens": 200,
  "totalTokens": 300,
  "providerCost": 0.002,
  "chargedAmount": 0.002,
  "timestamp": "2026-08-30T12:00:00"
}
```

### Possible Errors

* `401 Unauthorized`

---

## 13.2 Get Usage Summary

### Endpoint

```http
GET /api/usage/summary
```

### Authentication

Required.

### Authorization

The summary is calculated only for the authenticated user.

### Response

**200 OK**

Returns the user's usage summary, including the available aggregate usage information.

### Possible Errors

* `401 Unauthorized`

---

# 14. Transaction API

## 14.1 Get Transaction History

### Endpoint

```http
GET /api/transactions
```

### Authentication

Required.

### Authorization

Only transactions belonging to the authenticated user are returned.

### Query Parameters

| Parameter | Description                     |
| --------- | ------------------------------- |
| `page`    | Page number                     |
| `size`    | Number of transactions per page |
| `sort`    | Sorting field and direction     |

### Response

**200 OK**

Returns a paginated list of the user's transactions.

### Possible Errors

* `401 Unauthorized`

---

# 15. Plan API

Plans define the available subscription options.

A plan contains information such as:

```text
name
price
duration
creditLimit
active
```

---

## 15.1 Get Plans

### Endpoint

```http
GET /api/plans
```

### Authentication

Public.

### Response

**200 OK**

Returns the available plans.

Only plans intended to be publicly available should be returned to clients.

---

# 16. Subscription API

## 16.1 Create Subscription

### Endpoint

```http
POST /api/subscriptions
```

### Authentication

Required.

### Request Body

```json
{
  "planId": "plan123"
}
```

The user ID must not be provided by the client.

### Behavior

The backend:

1. Validates the requested plan.
2. Verifies that the plan is active.
3. Creates the subscription.
4. Sets the subscription validity period.
5. Sets the subscription credit according to the plan.
6. Creates the corresponding subscription purchase transaction.

### Response

**200 OK**

Returns the created `Subscription`.

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`
* `404 Not Found`

---

## 16.2 Get Current Subscription

### Endpoint

```http
GET /api/subscriptions/current
```

### Authentication

Required.

### Authorization

Only the authenticated user's active subscription is returned.

### Response

**200 OK**

Returns the current active `Subscription`.

### Possible Errors

* `401 Unauthorized`
* `404 Not Found`

---

# 17. Balance and Credit API

## 17.1 Get User Balance

### Endpoint

```http
GET /api/balance
```

### Authentication

Required.

### Authorization

Only the authenticated user's balance is returned.

### Response

**200 OK**

Returns the user's `UserBalance`.

Example:

```json
{
  "id": "balance123",
  "userId": "user456",
  "balance": 25.00,
  "currency": "USD",
  "createdAt": "2026-08-30T12:00:00",
  "updatedAt": "2026-08-30T12:00:00"
}
```

### Possible Errors

* `401 Unauthorized`
* `404 Not Found`

---

## 17.2 Purchase Credit

### Endpoint

```http
POST /api/balance/purchase
```

### Authentication

Required.

### Request Body

```json
{
  "amount": 20.00,
  "currency": "USD",
  "reference": "payment-reference"
}
```

The user ID must not be provided by the client.

### Behavior

The backend:

1. Creates a credit purchase transaction.
2. Marks the transaction as successful after the purchase operation succeeds.
3. Increases the authenticated user's balance.

### Response

**200 OK**

Returns the created `Transaction`.

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`
* `422 Unprocessable Entity`

---

# 18. Admin User API

Administrative user management is performed through:

```text
/api/admin/users
```

All endpoints require the `ADMIN` role.

---

## 18.1 Update User Role

### Endpoint

```http
PATCH /api/admin/users/{userId}/role
```

### Authentication

Required.

### Required Role

`ADMIN`

### Request Body

```json
{
  "role": "ADMIN"
}
```

### Response

**204 No Content**

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

# 19. Admin Ticket API

All endpoints require the `ADMIN` role.

## 19.1 Get All Tickets

### Endpoint

```http
GET /api/admin/tickets
```

### Query Parameters

| Parameter | Description                 |
| --------- | --------------------------- |
| `page`    | Page number                 |
| `size`    | Number of tickets per page  |
| `sort`    | Sorting field and direction |
| `search`  | Search tickets              |
| `status`  | Filter by ticket status     |
| `userId`  | Filter by ticket owner      |

### Response

**200 OK**

Returns a paginated list of `TicketDTO`.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`

---

## 19.2 Get Ticket

### Endpoint

```http
GET /api/admin/tickets/{ticketId}
```

### Response

**200 OK**

Returns the requested `TicketDTO`.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 19.3 Reply to Ticket

### Endpoint

```http
POST /api/admin/tickets/{ticketId}/replies
```

### Request Body

```json
{
  "message": "We have identified the issue."
}
```

The administrator identity is obtained from the JWT.

### Response

**201 Created**

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 19.4 Update Ticket Status

### Endpoint

```http
PATCH /api/admin/tickets/{ticketId}/status
```

### Request Body

```json
{
  "status": "IN_PROGRESS"
}
```

Supported statuses:

```text
OPEN
IN_PROGRESS
RESOLVED
CLOSED
```

### Response

**204 No Content**

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 19.5 Get Ticket History

### Endpoint

```http
GET /api/admin/tickets/{ticketId}/history
```

### Query Parameters

| Parameter | Description                 |
| --------- | --------------------------- |
| `page`    | Page number                 |
| `size`    | Number of history records   |
| `sort`    | Sorting field and direction |

### Response

**200 OK**

Returns a paginated list of `TicketHistoryDTO`.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

# 20. Admin Usage API

## 20.1 Get Usage Records

### Endpoint

```http
GET /api/admin/usage
```

### Authentication

Required.

### Required Role

`ADMIN`

### Query Parameters

| Parameter | Description                 |
| --------- | --------------------------- |
| `userId`  | Filter by user              |
| `model`   | Filter by model             |
| `page`    | Page number                 |
| `size`    | Number of records per page  |
| `sort`    | Sorting field and direction |

### Example

```http
GET /api/admin/usage?userId=user123&model=model-name&page=0&size=20
```

### Response

**200 OK**

Returns a paginated list of `UsageRecord`.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`

---

# 21. Admin Transaction API

## 21.1 Get Transactions

### Endpoint

```http
GET /api/admin/transactions
```

### Authentication

Required.

### Required Role

`ADMIN`

### Query Parameters

| Parameter | Description                     |
| --------- | ------------------------------- |
| `userId`  | Filter by user                  |
| `type`    | Filter by transaction type      |
| `status`  | Filter by transaction status    |
| `page`    | Page number                     |
| `size`    | Number of transactions per page |
| `sort`    | Sorting field and direction     |

### Response

**200 OK**

Returns a paginated list of transactions.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`

---

## 21.2 Get Transaction

### Endpoint

```http
GET /api/admin/transactions/{transactionId}
```

### Authentication

Required.

### Required Role

`ADMIN`

### Response

**200 OK**

Returns the requested transaction.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 21.3 Update Transaction Status

### Endpoint

```http
PUT /api/admin/transactions/{transactionId}/status
```

### Authentication

Required.

### Required Role

`ADMIN`

### Query Parameters

| Parameter | Description            |
| --------- | ---------------------- |
| `status`  | New transaction status |

### Example

```http
PUT /api/admin/transactions/transaction123/status?status=SUCCESS
```

### Response

**200 OK**

Returns the updated transaction.

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

# 22. Admin Plan API

All endpoints require the `ADMIN` role.

## 22.1 Create Plan

### Endpoint

```http
POST /api/admin/plans
```

### Request Body

The request contains the plan information, including its price, duration, credit limit and active state.

### Response

**201 Created**

Returns the created `Plan`.

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`
* `403 Forbidden`

---

## 22.2 Update Plan

### Endpoint

```http
PUT /api/admin/plans/{planId}
```

### Request Body

Contains the updated plan information.

### Response

**200 OK**

Returns the updated `Plan`.

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 22.3 Get Plan

### Endpoint

```http
GET /api/admin/plans/{planId}
```

### Response

**200 OK**

Returns the requested `Plan`.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 22.4 Deactivate Plan

### Endpoint

```http
DELETE /api/admin/plans/{planId}
```

### Authentication

Required.

### Required Role

`ADMIN`

The operation deactivates the plan rather than necessarily deleting its historical data.

### Response

**204 No Content**

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 22.5 Get Plan Subscriptions

### Endpoint

```http
GET /api/admin/plans/{planId}/subscriptions
```

### Authentication

Required.

### Required Role

`ADMIN`

### Query Parameters

| Parameter | Description                      |
| --------- | -------------------------------- |
| `page`    | Page number                      |
| `size`    | Number of subscriptions per page |
| `sort`    | Sorting field and direction      |

### Response

**200 OK**

Returns a paginated list of subscriptions associated with the specified plan.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

# 23. Admin Subscription API

All endpoints require the `ADMIN` role.

## 23.1 Get Subscriptions

### Endpoint

```http
GET /api/admin/subscriptions
```

### Query Parameters

| Parameter | Description                      |
| --------- | -------------------------------- |
| `userId`  | Filter by user                   |
| `planId`  | Filter by plan                   |
| `status`  | Filter by subscription status    |
| `page`    | Page number                      |
| `size`    | Number of subscriptions per page |
| `sort`    | Sorting field and direction      |

### Example

```http
GET /api/admin/subscriptions?status=ACTIVE&page=0&size=20
```

### Response

**200 OK**

Returns a paginated list of subscriptions.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`

---

## 23.2 Get Subscription

### Endpoint

```http
GET /api/admin/subscriptions/{subscriptionId}
```

### Response

**200 OK**

Returns the requested subscription.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 23.3 Cancel Subscription

### Endpoint

```http
DELETE /api/admin/subscriptions/{subscriptionId}
```

### Response

**204 No Content**

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

# 24. KPI and Reporting API

The KPI module provides administrative information about system usage and business activity.

The KPI APIs are intended for administrative use and require the `ADMIN` role.

The available KPI categories include:

```text
Total users
Active users
Subscribed users
AI request count
Successful AI requests
Failed AI requests
Total tokens
AI service costs
Subscription/payment revenue
Usage statistics over time
```

KPI endpoints should return aggregated information rather than raw records.

Where a KPI supports a time range, the requested time range is provided through the corresponding query parameters defined by the KPI API.

---

# 25. Ticket History

Ticket history records important actions performed on tickets.

At minimum, the following actions are recorded:

```text
STATUS_CHANGE
REPLY
```

A history record contains information such as:

```text
ticketId
userId
type
status
message
createdAt
```

For a status change:

```text
status = new status
```

For a reply:

```text
message = administrator response
```

Ticket history is append-only. Existing history records should not be modified when a new action occurs.

---

# 26. Credit and Usage Rules

The backend is responsible for enforcing credit and usage limits.

Clients must not directly modify:

```text
remainingCredit
balance
providerCost
chargedAmount
inputTokens
outputTokens
totalTokens
```

These values are calculated or updated by the backend.

For an AI request, the backend:

1. Receives the request.
2. Sends the request to the configured AI provider.
3. Receives token usage information.
4. Determines the actual model used.
5. Retrieves the applicable model pricing.
6. Calculates the provider cost.
7. Charges the selected credit source.
8. Stores the usage record.

If the selected credit source is unavailable or insufficient, the request may fail with an appropriate error.

---

# 27. AI Provider Errors

Communication with the external AI provider is performed through OmniRoute.

Provider failures are represented internally by:

```text
PROVIDER_ERROR
```

The API may return:

```text
502 Bad Gateway
```

for an AI provider communication failure.

The client must not access OmniRoute or the external AI provider directly.

---

# 28. HTTP Status Codes

| Status Code                 | Meaning                                                |
| --------------------------- | ------------------------------------------------------ |
| `200 OK`                    | Request completed successfully                         |
| `201 Created`               | Resource successfully created                          |
| `204 No Content`            | Request succeeded without a response body              |
| `400 Bad Request`           | Invalid request                                        |
| `401 Unauthorized`          | Authentication is missing or invalid                   |
| `403 Forbidden`             | Authenticated user does not have permission            |
| `404 Not Found`             | Requested resource does not exist                      |
| `409 Conflict`              | Request conflicts with existing data                   |
| `422 Unprocessable Entity`  | Request cannot be processed because of a business rule |
| `500 Internal Server Error` | Unexpected server-side error                           |
| `502 Bad Gateway`           | External AI provider request failed                    |

---

# 29. Common Error Cases

## 29.1 Resource Not Found

```text
404 Not Found
```

Example:

```json
{
  "status": 404,
  "message": "Conversation not found with id: conv123",
  "timestamp": "2026-08-30T12:00:00"
}
```

---

## 29.2 Validation Error

```text
400 Bad Request
```

Used when request data fails validation.

---

## 29.3 Unauthorized Request

```text
401 Unauthorized
```

Returned when authentication is required but the request does not contain a valid authentication context.

Example:

```json
{
  "status": 401,
  "message": "Authentication required",
  "timestamp": "2026-08-30T12:00:00"
}
```

---

## 29.4 Access Denied

```text
403 Forbidden
```

Returned when an authenticated user does not have permission to access the requested resource.

Example:

```json
{
  "status": 403,
  "message": "Access denied",
  "timestamp": "2026-08-30T12:00:00"
}
```

---

## 29.5 Duplicate Resource

```text
409 Conflict
```

Used when an operation conflicts with existing data, such as creating a duplicate username.

---

## 29.6 Business Rule Violation

```text
422 Unprocessable Entity
```

Examples include:

```text
Insufficient credit
Model pricing unavailable
```

---

## 29.7 AI Provider Failure

```text
502 Bad Gateway
```

Used when the backend cannot successfully communicate with the configured AI provider.

---

# 30. API Versioning

The initial API does not include an explicit version in the URL.

Examples:

```text
/api/users
/api/conversations
/api/tickets
```

If versioning becomes necessary in a future release, a version may be introduced:

```text
/api/v1/users
```

---

# 31. Security Rules

The following security rules apply throughout the API:

1. JWT authentication is required for protected endpoints.
2. Administrative endpoints require the `ADMIN` role.
3. User-owned resources are always resolved using the authenticated user's identity.
4. Clients must not provide another user's ID to access that user's resources.
5. User roles cannot be changed through ordinary user endpoints.
6. Sensitive authentication credentials must not be returned in API responses.
7. External AI providers must not be accessed directly by clients.
8. Credit, balance, token usage and cost values are controlled by the backend.
9. Administrative operations are separated under `/api/admin/**`.

---

# 32. API Design Rules

The following rules apply to the API:

* Collection endpoints should use pagination.
* Search and filtering should be performed at the database level where applicable.
* Sorting is supported through `Pageable` where applicable.
* Large MongoDB collections must not be loaded completely into application memory.
* User ownership must be validated before accessing user-owned resources.
* Administrative operations must remain under the Admin API namespace.
* Error responses must follow the common `ErrorResponse` structure.
* The backend is the trusted source for AI usage and cost information.
* The API contract must remain synchronized with the actual controller and DTO implementations.
* Changes to API behavior should be reflected in this document.

---

# 33. API Summary

## Public APIs

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/plans
```

## User APIs

```text
GET    /api/users/me
GET    /api/users
GET    /api/users/{id}
PUT    /api/users/{id}
DELETE /api/users/{id}

POST   /api/conversations
GET    /api/conversations
GET    /api/conversations/{conversationId}
PUT    /api/conversations/{conversationId}
PATCH  /api/conversations/{conversationId}/archive

GET    /api/conversations/{conversationId}/messages
GET    /api/conversations/{conversationId}/messages/{messageId}
POST   /api/conversations/{conversationId}/messages

POST   /api/tickets
GET    /api/tickets
GET    /api/tickets/{ticketId}

GET    /api/usage
GET    /api/usage/summary

GET    /api/transactions

POST   /api/subscriptions
GET    /api/subscriptions/current

GET    /api/balance
POST   /api/balance/purchase
```

## Admin APIs

```text
PATCH  /api/admin/users/{userId}/role

GET    /api/admin/tickets
GET    /api/admin/tickets/{ticketId}
POST   /api/admin/tickets/{ticketId}/replies
PATCH  /api/admin/tickets/{ticketId}/status
GET    /api/admin/tickets/{ticketId}/history

GET    /api/admin/usage

GET    /api/admin/transactions
GET    /api/admin/transactions/{transactionId}
PUT    /api/admin/transactions/{transactionId}/status

POST   /api/admin/plans
PUT    /api/admin/plans/{planId}
GET    /api/admin/plans/{planId}
DELETE /api/admin/plans/{planId}
GET    /api/admin/plans/{planId}/subscriptions

GET    /api/admin/subscriptions
GET    /api/admin/subscriptions/{subscriptionId}
DELETE /api/admin/subscriptions/{subscriptionId}
```

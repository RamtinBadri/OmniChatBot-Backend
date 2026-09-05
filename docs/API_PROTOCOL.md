# AI Platform — API Protocol

## 1. Overview

This document defines the API contract between the AI Platform backend and its clients.

The backend is responsible for authentication, user management, conversation management, message storage, ticket management, and communication with external AI services.

Clients must communicate with AI services through the backend and must not access external AI providers directly.

---

## 2. Base URL

All API endpoints are relative to the following base path:

```text
/api
```

Example:

```http
POST /api/auth/login
```

---

## 3. Content Type

Requests and responses containing a body use JSON.

```http
Content-Type: application/json
```

---

## 4. Authentication

Authenticated endpoints use JWT-based authentication.

The token must be provided through the `Authorization` header:

```http
Authorization: Bearer <JWT>
```

Endpoints that do not require authentication are explicitly marked as **Public**.

---

# 5. Response Convention

## 5.1 Successful Responses

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

## 5.2 Error Responses

All API errors follow a consistent response structure.

Example:

```json
{
  "status": 404,
  "message": "User not found",
  "timestamp": "2026-08-30T12:00:00"
}
```

---

# 6. Authentication API

## 6.1 Register

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

### Response

**201 Created**

The user is created with:

```text
role = USER
status = ACTIVE
```

### Possible Errors

* `400 Bad Request`
* `409 Conflict`

---

## 6.2 Login

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

Returns the JWT:

```text
eyJhbGciOi...
```

The token must subsequently be provided through the `Authorization` header:

```http
Authorization: Bearer <JWT>
```

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`

---

# 7. User API

## 7.1 Get Current User

### Endpoint

```http
GET /api/users/me
```

### Authentication

Required.

### Authorization

Any authenticated user.

The backend identifies the user from the JWT. The user ID must not be provided by the client.

### Response

**200 OK**

Returns the authenticated user's `UserDTO`.

Example:

```json
{
  "id": "64f123",
  "email": "user@example.com",
  "firstName": "Ramtin",
  "lastName": "Example",
  "userRole": "USER",
  "status": "ACTIVE",
  "createdAt": "2026-08-30T12:00:00",
  "updatedAt": "2026-08-30T12:00:00"
}
```

### Possible Errors

* `401 Unauthorized`

---

## 7.2 Get Users

### Endpoint

```http
GET /api/users
```

### Authentication

Required.

### Required Role

`ADMIN`

### Query Parameters

| Parameter | Description                 | Example          |
| --------- | --------------------------- | ---------------- |
| `page`    | Page number                 | `0`              |
| `size`    | Number of records per page  | `20`             |
| `sort`    | Sorting field and direction | `createdAt,desc` |
| `role`    | Filter by user role         | `USER`           |
| `status`  | Filter by user status       | `ACTIVE`         |
| `search`  | Search users                | `ramtin`         |

### Example

```http
GET /api/users?page=0&size=20&sort=createdAt,desc&status=ACTIVE
```

### Response

**200 OK**

```json
{
  "content": [
    {
      "id": "64f123",
      "email": "user@example.com",
      "firstName": "Ramtin",
      "lastName": "Example",
      "userRole": "USER",
      "status": "ACTIVE",
      "createdAt": "2026-08-30T12:00:00",
      "updatedAt": "2026-08-30T12:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`

---

## 7.3 Get User

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

## 7.4 Update User

### Endpoint

```http
PUT /api/users/{id}
```

### Authentication

Required.

### Authorization

An authenticated `USER` may only update their own profile.

The `{id}` in the URL must match the authenticated user's ID from the JWT.

The endpoint updates profile information only. The user cannot modify their own role or status through this endpoint.

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

## 7.5 Update User Role

### Endpoint

```http
PATCH /api/admin/users/{id}/role
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

The request may contain only the user's new role.

### Response

**204 No Content**

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 7.6 Delete User

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

# 8. Conversation API

## 8.1 Create Conversation

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

The backend identifies the authenticated user from the JWT.

### Response

**201 Created**

```json
{
  "id": "conv123",
  "userId": "user456",
  "title": "Learning Spring Boot",
  "model": "default",
  "status": "ACTIVE",
  "createdAt": "2026-08-30T12:00:00",
  "updatedAt": "2026-08-30T12:00:00"
}
```

---

## 8.2 Get User Conversations

### Endpoint

```http
GET /api/conversations
```

### Authentication

Required.

Only conversations belonging to the authenticated user are returned.

### Query Parameters

| Parameter | Description                   |
| --------- | ----------------------------- |
| `page`    | Page number                   |
| `size`    | Number of records per page    |
| `sort`    | Sorting field and direction   |
| `status`  | Filter by conversation status |
| `search`  | Search conversations          |

### Example

```http
GET /api/conversations?page=0&size=20&sort=updatedAt,desc
```

### Response

**200 OK**

Returns a paginated list of `ConversationDTO`.

---

## 8.3 Get Conversation

### Endpoint

```http
GET /api/conversations/{id}
```

### Authentication

Required.

Users may only access conversations that belong to them.

### Response

**200 OK**

Returns a `ConversationDTO`.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 8.4 Update Conversation

### Endpoint

```http
PUT /api/conversations/{id}
```

### Authentication

Required.

Users may only update conversations that belong to them.

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

## 8.5 Archive Conversation

### Endpoint

```http
PATCH /api/conversations/{id}/archive
```

### Authentication

Required.

Users may only archive conversations that belong to them.

### Response

**204 No Content**

The conversation status is changed to:

```text
ARCHIVED
```

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

# 9. Message API

Messages are treated as child resources of a conversation.

```text
/api/conversations/{conversationId}/messages
```

---

## 9.1 Get Conversation Messages

### Endpoint

```http
GET /api/conversations/{conversationId}/messages
```

### Authentication

Required.

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

```json
{
  "content": [
    {
      "id": "msg123",
      "conversationId": "conv123",
      "role": "USER",
      "content": "Explain dependency injection.",
      "createdAt": "2026-08-30T12:00:00"
    },
    {
      "id": "msg124",
      "conversationId": "conv123",
      "role": "ASSISTANT",
      "content": "Dependency injection is...",
      "createdAt": "2026-08-30T12:00:02"
    }
  ],
  "page": 0,
  "size": 50,
  "totalElements": 2,
  "totalPages": 1
}
```

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 9.2 Get Message

### Endpoint

```http
GET /api/conversations/{conversationId}/messages/{messageId}
```

### Authentication

Required.

The authenticated user must own the specified conversation.

The requested message must also belong to the specified conversation.

### Response

**200 OK**

Returns a `MessageDTO`.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 9.3 Send Message

### Endpoint

```http
POST /api/conversations/{conversationId}/messages
```

### Authentication

Required.

The authenticated user must own the specified conversation.

### Request Body

```json
{
  "content": "Explain dependency injection.",
  "model": "default"
}
```

### Phase 1 Behavior

In Phase 1, the endpoint stores the message in MongoDB.

### Phase 2 Behavior

In Phase 2, this endpoint becomes the entry point for AI requests.

Expected flow:

```text
Frontend
    ↓
MessageController
    ↓
MessageService
    ↓
AIService
    ↓
OmniRoute
    ↓
LLM
    ↓
AI Response
    ↓
Save Message
    ↓
Frontend
```

### Response

**201 Created**

Returns the created `MessageDTO`.

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

# 10. Ticket API

The ticketing system allows users to submit support requests and allows administrators to manage those requests.

A ticket belongs to the user who created it.

```text
User
  ↓
Ticket
  ↓
Ticket History
```

Users may only access their own tickets.

Administrators may access and manage all tickets.

---

## 10.1 Create Ticket

### Endpoint

```http
POST /api/tickets
```

### Authentication

Required.

### Authorization

Any authenticated user.

The `userId` must not be provided by the client.

The backend identifies the ticket owner from the authenticated user's JWT.

### Request Body

```json
{
  "subject": "Cannot access my account",
  "description": "I cannot log in after resetting my password."
}
```

### Response

**201 Created**

Example:

```json
{
  "id": "ticket123",
  "userId": "user456",
  "subject": "Cannot access my account",
  "description": "I cannot log in after resetting my password.",
  "status": "OPEN",
  "createdAt": "2026-08-30T12:00:00",
  "updatedAt": "2026-08-30T12:00:00"
}
```

New tickets are initially created with:

```text
status = OPEN
```

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`

---

## 10.2 Get User Tickets

### Endpoint

```http
GET /api/tickets
```

### Authentication

Required.

### Authorization

Any authenticated user.

Only tickets belonging to the authenticated user are returned.

### Query Parameters

| Parameter | Description                 |
| --------- | --------------------------- |
| `page`    | Page number                 |
| `size`    | Number of tickets per page  |
| `sort`    | Sorting field and direction |
| `status`  | Filter by ticket status     |
| `search`  | Search tickets              |

### Example

```http
GET /api/tickets?page=0&size=20&sort=updatedAt,desc&status=OPEN
```

### Response

**200 OK**

Returns a paginated list of `TicketDTO`.

### Possible Errors

* `401 Unauthorized`

---

## 10.3 Get Ticket

### Endpoint

```http
GET /api/tickets/{ticketId}
```

### Authentication

Required.

### Authorization

A user may only access a ticket that belongs to them.

Administrators may access any ticket.

### Response

**200 OK**

Returns a `TicketDTO`.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

# 11. Admin Ticket API

Administrative ticket operations are separated from user ticket operations under:

```text
/api/admin/tickets
```

All endpoints in this section require the `ADMIN` role.

---

## 11.1 Get All Tickets

### Endpoint

```http
GET /api/admin/tickets
```

### Authentication

Required.

### Required Role

`ADMIN`

### Query Parameters

| Parameter | Description                 |
| --------- | --------------------------- |
| `page`    | Page number                 |
| `size`    | Number of tickets per page  |
| `sort`    | Sorting field and direction |
| `status`  | Filter by ticket status     |
| `search`  | Search tickets              |
| `userId`  | Filter by ticket owner      |

### Example

```http
GET /api/admin/tickets?page=0&size=20&sort=updatedAt,desc&status=OPEN
```

### Response

**200 OK**

Returns a paginated list of `TicketDTO`.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`

---

## 11.2 Get Ticket

### Endpoint

```http
GET /api/admin/tickets/{ticketId}
```

### Authentication

Required.

### Required Role

`ADMIN`

### Response

**200 OK**

Returns the requested `TicketDTO`.

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 11.3 Reply to Ticket

### Endpoint

```http
POST /api/admin/tickets/{ticketId}/replies
```

### Authentication

Required.

### Required Role

`ADMIN`

### Request Body

```json
{
  "message": "We have identified the issue and are working on a fix."
}
```

The administrator's identity is obtained from the authenticated JWT and must not be provided by the client.

The reply is stored as part of the ticket history.

### Response

**201 Created**

Returns the created ticket history/reply record.

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 11.4 Update Ticket Status

### Endpoint

```http
PATCH /api/admin/tickets/{ticketId}/status
```

### Authentication

Required.

### Required Role

`ADMIN`

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

Every status change must be recorded in the ticket history.

### Response

**204 No Content**

### Possible Errors

* `400 Bad Request`
* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

## 11.5 Get Ticket History

### Endpoint

```http
GET /api/admin/tickets/{ticketId}/history
```

### Authentication

Required.

### Required Role

`ADMIN`

### Query Parameters

| Parameter | Description                 |
| --------- | --------------------------- |
| `page`    | Page number                 |
| `size`    | Number of history records   |
| `sort`    | Sorting field and direction |

### Example

```http
GET /api/admin/tickets/ticket123/history?page=0&size=20&sort=createdAt,asc
```

### Response

**200 OK**

Returns a paginated list of ticket history records.

Example:

```json
{
  "content": [
    {
      "id": "history123",
      "ticketId": "ticket123",
      "userId": "admin456",
      "type": "STATUS_CHANGE",
      "status": "IN_PROGRESS",
      "message": null,
      "createdAt": "2026-08-30T13:00:00"
    },
    {
      "id": "history124",
      "ticketId": "ticket123",
      "userId": "admin456",
      "type": "REPLY",
      "status": null,
      "message": "We are working on the issue.",
      "createdAt": "2026-08-30T13:05:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 2,
  "totalPages": 1
}
```

### Possible Errors

* `401 Unauthorized`
* `403 Forbidden`
* `404 Not Found`

---

# 12. Ticket History

Ticket history records important actions performed on a ticket.

At minimum, the following actions must be recorded:

```text
STATUS_CHANGE
REPLY
```

Each history record contains information about:

```text
ticketId
userId
type
status
message
createdAt
```

The `userId` identifies the user who performed the action.

For status changes, `status` contains the new status.

For replies, `message` contains the administrator's response.

Ticket history is append-only. Existing history records should not be modified when a new action occurs.

---

# 13. HTTP Status Codes

| Status Code                 | Meaning                                     |
| --------------------------- | ------------------------------------------- |
| `200 OK`                    | Request completed successfully              |
| `201 Created`               | Resource successfully created               |
| `204 No Content`            | Request succeeded without a response body   |
| `400 Bad Request`           | Invalid request                             |
| `401 Unauthorized`          | Authentication is missing or invalid        |
| `403 Forbidden`             | Authenticated user does not have permission |
| `404 Not Found`             | Requested resource does not exist           |
| `409 Conflict`              | Request conflicts with existing data        |
| `500 Internal Server Error` | Unexpected server-side error                |

---

# 14. Pagination

All potentially large collection endpoints must support pagination.

Standard parameters:

```text
page
size
sort
```

Example:

```http
GET /api/conversations?page=0&size=20&sort=updatedAt,desc
```

Default values:

```text
page = 0
size = 20
```

The backend must not retrieve an unbounded number of documents from MongoDB for a single request.

---

# 15. Search and Filtering

Collection endpoints should support search and filtering where applicable.

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
GET /api/admin/tickets?status=OPEN&userId=user456
```

Filtering and search implementations must be designed with MongoDB query performance and appropriate indexes in mind.

---

# 16. API Versioning

The initial version of the API does not include an explicit version in the URL.

Examples:

```text
/api/users
/api/conversations
/api/tickets
```

If API versioning becomes necessary, a version can be introduced in a future release:

```text
/api/v1/users
```

---

# 17. Future Phase 2 APIs

The following resources are planned for Phase 2:

```text
/api/ai
/api/usage
/api/plans
/api/subscriptions
/api/payments
/api/transactions
```

# Messaging Service

A backend service for one-to-one messaging built with **Spring Boot** and **MongoDB**. Supports sending messages, fetching paginated conversation history, and listing a user's conversations — with authorization enforced so a user can only read conversations they're part of.

---

## Tech Stack

- Java 17+
- Spring Boot (Web, Data MongoDB, Validation)
- MongoDB
- JUnit 5 + Mockito (unit tests)
- Maven

---

## Prerequisites

- JDK 17 or higher
- Maven (or use the included `./mvnw` wrapper — no local Maven install needed)
- MongoDB running locally, or via Docker

---

## 1. Start MongoDB

**Option A — Docker (recommended, no local install needed):**
```bash
docker run -d -p 27017:27017 --name messaging-mongo mongo:7
```

**Option B — local install:**
```bash
# macOS with Homebrew
brew services start mongodb-community

# or run manually
mongod --dbpath /path/to/your/data/directory
```

MongoDB should be reachable at `mongodb://localhost:27017`.

---

## 2. Configure the application

The active profile is `qa` by default (`spring.profiles.active=qa`). It connects to:
```
${MONGODB_URI:mongodb://localhost:27017/messaging_service_qa}
```
This means it uses the `MONGODB_URI` environment variable if set, and **falls back to a local MongoDB instance** (`messaging_service_qa` database) if not set — so it runs out of the box with just Docker/local Mongo running, no environment variables required.

To point at a different Mongo instance, set the environment variable before running:
```bash
export MONGODB_URI=mongodb://your-host:27017/your_db_name
```

Other profiles (`preprod`, `prod`) require `MONGODB_URI` to be set explicitly with no fallback — see the corresponding `application-<profile>.properties` files. These exist to demonstrate environment-based configuration; they are not deployed anywhere as part of this exercise.

---

## 3. Run the application

```bash
./mvnw spring-boot:run
```

The app starts on:
```
http://localhost:8085/messaging-service
```

Note the context path (`/messaging-service`) — every endpoint below is prefixed with it.

To run with a specific profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=preprod
```

---

## 4. Run the tests

```bash
./mvnw test
```

This runs the unit test suite covering:
- Sending a message (including auto-creating vs. reusing a conversation)
- Paginated conversation history (correct ordering, `hasMore`/cursor behavior)
- Authorization-denied read (user not part of a conversation)
- Listing a user's conversations

---

## API Reference

All endpoints are prefixed with the context path: `http://localhost:8085/messaging-service`

### 1. Send a message

```
POST /messaging-service/messages
Content-Type: application/json
```

**Request body:**
```json
{
  "senderId": "alice",
  "recipientId": "bob",
  "content": "hey, are you free tomorrow?"
}
```

**Example:**
```bash
curl -X POST http://localhost:8085/messaging-service/messages \
  -H "Content-Type: application/json" \
  -d '{
    "senderId": "alice",
    "recipientId": "bob",
    "content": "hey, are you free tomorrow?"
  }'
```

**Response (201 Created):**
```json
{
  "id": "msg123",
  "conversationId": "conv1",
  "senderId": "alice",
  "recipientId": "bob",
  "content": "hey, are you free tomorrow?",
  "createdAt": "2026-07-27T10:00:00Z",
  "status": "SENT"
}
```

A conversation between the two users is created automatically on their first message; subsequent messages reuse the same conversation.

---

### 2. Fetch conversation history (paginated)

```
GET /messaging-service/messages/conversations/{conversationId}?requestingUserId={userId}&limit={limit}&cursor={cursor}
```

| Param | Required | Description |
|---|---|---|
| `requestingUserId` | yes | user making the request — must be a participant in the conversation |
| `limit` | no (default 20) | max messages per page |
| `cursor` | no | opaque token from a previous response's `nextCursor`, used to fetch the next page |

**Example — first page:**
```bash
curl "http://localhost:8085/messaging-service/messages/conversations/conv1?requestingUserId=alice&limit=10"
```

**Response:**
```json
{
  "items": [
    { "id": "msg2", "conversationId": "conv1", "senderId": "bob", "recipientId": "alice", "content": "yes, free after 3pm", "createdAt": "2026-07-27T10:05:00Z", "status": "SENT" },
    { "id": "msg1", "conversationId": "conv1", "senderId": "alice", "recipientId": "bob", "content": "hey, are you free tomorrow?", "createdAt": "2026-07-27T10:00:00Z", "status": "SENT" }
  ],
  "nextCursor": "MjAyNi0wNy0yN1QxMDowMDowMFo=",
  "hasMore": true
}
```

**Example — next page (pass `nextCursor` back as `cursor`):**
```bash
curl "http://localhost:8085/messaging-service/messages/conversations/conv1?requestingUserId=alice&limit=10&cursor=MjAyNi0wNy0yN1QxMDowMDowMFo="
```

**Unauthorized example** (requesting user is not part of the conversation):
```bash
curl "http://localhost:8085/messaging-service/messages/conversations/conv1?requestingUserId=some_stranger"
```
Returns an error response with the appropriate status code and a JSON error body.

Pagination uses an **opaque cursor** (base64-encoded timestamp) rather than page numbers, so results stay stable even if new messages arrive between page loads — no duplicates or skipped messages.

---

### 3. List a user's conversations

```
GET /messaging-service/conversations/{userId}/conversations
```

**Example:**
```bash
curl http://localhost:8085/messaging-service/conversations/alice/conversations
```

**Response:**
```json
[
  {
    "id": "conv1",
    "participantIds": ["alice", "bob"],
    "lastMessagePreview": "yes, free after 3pm",
    "lastMessageAt": "2026-07-27T10:05:00Z"
  }
]
```

Conversations are sorted by most recent activity first.

---

## Data Model

**Message** — one document per message sent.
- `conversationId`, `senderId`, `recipientId`, `content`, `createdAt`, `status`

**Conversation** — one document per pair of users; created automatically on first message.
- `participantIds`, `lastMessagePreview`, `lastMessageAt`, `createdAt`

`lastMessagePreview`/`lastMessageAt` are denormalized onto `Conversation` so listing a user's conversations doesn't require scanning the entire `messages` collection.

---

## Design Notes

- **Cursor-based pagination** (not offset/page-number) keeps conversation history stable as new messages arrive.
- **Authorization** is enforced at the service layer: fetching history checks that `requestingUserId` is a participant in the conversation before returning any data.
- **Conversations are auto-created** on the first message between two users — no separate "start conversation" endpoint needed.
- `requestingUserId` is passed as a request param rather than derived from an auth token, since full authentication is out of scope for this exercise.
- Health check available at `GET /messaging-service/actuator/health` (via Spring Boot Actuator).

See the accompanying write-up for a full discussion of trade-offs and what's out of scope.
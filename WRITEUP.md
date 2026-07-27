# Write-up

## 1. What I asked the AI to do, and what I decided myself

I made the core design decisions myself. The AI helped me with the repetitive/boilerplate parts and some technical details I asked it to explain or generate.

Decisions I made myself:
- The overall design: a `Conversation` should be created automatically the first time two users message each other, instead of needing a separate "create conversation" API.
- Storing the last message preview directly on the `Conversation` document, so listing a user's conversations doesn't need to scan every message.
- Using cursor-based pagination (not page numbers) for conversation history, so it stays correct even when new messages arrive while someone is scrolling.
- Where authorization should be checked (at the service layer, before returning any conversation history).
- The package structure, API endpoint naming, and overall project layout — I asked the AI to fix the endpoint names multiple times until they read the way I wanted.
- Writing my own custom exception class (`M2MException`) instead of using the generic one the AI first suggested.

What the AI wrote for me (based on my design decisions):
- Boilerplate code: DTOs, model classes, repository interfaces, controller/service scaffolding.
- Unit tests (JUnit + Mockito) for the service classes, once I told it what scenarios to cover (send, pagination, unauthorized access).
- MongoDB queries — including fixing a query that failed at runtime (explained below).
- The README content, formatted around the actual endpoints and config I had.

## 2. Where I changed or corrected the AI's output

- The AI's first version of the "find conversation between two users" query used two `Containing` conditions in one Spring Data method name. This failed at runtime with a MongoDB error, because you can't repeat the same field twice in one query. I asked it to fix this, and we changed it to a `$all` query instead.
- I replaced the AI's first generic exception class with my own `M2MException` (with a `code` field), and asked it to rebuild the exception handler around my class instead of its own.
- I changed the endpoint URL naming a few times until it matched how I wanted the API to read (for example, deciding `/messages/conversation/{id}` instead of nesting everything under `/conversations/...`).
- I corrected the README when it didn't match my real `application.properties` file (wrong port, wrong context path, and a "local" profile that doesn't actually exist in my setup).

## 3. Biggest trade-offs I made

- **No real authentication.** Instead of a login/token system, the API takes `senderId`/`requestingUserId` directly as request values. This let me focus on the core messaging logic (send, fetch, list) instead of building auth from scratch. In a real product, this would come from a logged-in session instead.
- **Denormalizing the last message onto Conversation.** This means every time a message is sent, I write to two places (the message itself, and the conversation's preview fields) instead of one. I chose this because it makes listing a user's conversations fast and simple, which matters more since users check their conversation list often.
- **No pagination on the "list conversations" API.** Since one user usually has a small number of conversations (unlike messages, which can be huge), I kept this endpoint simple and returned the full list. If this were a large-scale product, I'd paginate this too.

## 4. What's missing, or what I'd do with more time

- Add real authentication (e.g., JWT tokens) instead of passing user IDs directly in requests.
- Add integration tests that run against a real (in-memory or Dockerized) MongoDB, not just mocked unit tests.
- Add rate limiting so one user can't spam messages.
- Add read receipts / message status updates (the `status` field exists but isn't fully used yet).
- Add proper indexes on MongoDB fields I query often (`conversationId`, `participantIds`) to keep it fast as data grows.
- Add pagination to the "list conversations" endpoint for very high-usage accounts.
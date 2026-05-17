# Task Manager API

A personal task manager REST API built with Java 17 and Spring Boot. Includes a browser-based UI and an AI-powered endpoint that converts plain-English descriptions into structured tasks using Claude.

## Requirements

- Java 17+
- Maven 3.8+ (or use the included `./mvnw` wrapper)
- An Anthropic API key — get one at [console.anthropic.com](https://console.anthropic.com)

## Setup

**1. Clone the repository**

```bash
git clone <repo-url>
cd taskmanager
```

**2. Add your API key**

Create a `.env` file in the project root:

```bash
echo "ANTHROPIC_API_KEY=sk-ant-api03-..." > .env
```

> The `.env` file is gitignored. Never commit a real key.

**3. Run**

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8081`. The UI is available at the same address.

That's it — no database installation required. The app uses an H2 in-memory database that is created on startup and dropped on shutdown.

---

## UI

Open `http://localhost:8081` in a browser. The interface lets you:

- View and filter all tasks by status
- Create tasks manually using the form
- Use the AI suggest panel to generate a structured task from a plain-English description, then review or instantly create it
- Update task status inline directly on each task card
- Delete tasks

---

## AI-Powered Endpoint

`POST /tasks/suggest` accepts a plain-English description and returns a structured task object inferred by Claude. Nothing is persisted — the result is returned for review and can be submitted separately to `POST /tasks` to create it.

### Request

```bash
curl -X POST http://localhost:8081/tasks/suggest \
  -H "Content-Type: application/json" \
  -d '{"description": "finish the quarterly report before Friday"}'
```

### Response

```json
{
  "title": "Finish quarterly report",
  "description": "Complete and submit the Q3 quarterly report before the end of the week.",
  "dueDate": "2026-05-23",
  "priority": "HIGH",
  "status": "TODO"
}
```

Claude infers `title`, `description`, `dueDate` (resolved relative to today), `priority` (`LOW` / `MEDIUM` / `HIGH`), and `status` (always `TODO` for a new suggestion).

### Error responses

| Status | Cause |
|--------|-------|
| `400`  | `description` field is blank |
| `502`  | Claude returned an unparseable response, or the Anthropic API returned an error |
| `500`  | Unexpected server error |

---

## Endpoints

### Tasks

| Method   | Path              | Status codes      | Description                        |
|----------|-------------------|-------------------|------------------------------------|
| `GET`    | `/tasks`          | `200`             | List all tasks, sorted by due date ascending (nulls last) |
| `GET`    | `/tasks/{id}`     | `200`, `404`      | Get a single task by ID            |
| `POST`   | `/tasks`          | `201`, `400`      | Create a task                      |
| `PUT`    | `/tasks/{id}`     | `200`, `400`, `404` | Replace a task (all fields required) |
| `DELETE` | `/tasks/{id}`     | `204`, `404`      | Delete a task                      |
| `POST`   | `/tasks/suggest`  | `200`, `400`, `502` | Generate a task suggestion from plain English (not persisted) |

### Task fields

| Field         | Type     | Required | Values                          |
|---------------|----------|----------|---------------------------------|
| `title`       | `string` | Yes      | Non-blank, max 80 chars         |
| `description` | `string` | No       | Free text, nullable             |
| `dueDate`     | `string` | No       | ISO-8601 date (`YYYY-MM-DD`), nullable |
| `priority`    | `enum`   | Yes      | `LOW`, `MEDIUM`, `HIGH`         |
| `status`      | `enum`   | Yes      | `TODO`, `IN_PROGRESS`, `DONE`   |

### Example: create a task

```bash
curl -X POST http://localhost:8081/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Review pull requests",
    "description": "Go through open PRs before the team standup",
    "dueDate": "2026-05-20",
    "priority": "HIGH",
    "status": "TODO"
  }'
```

### Example: update a task

```bash
curl -X PUT http://localhost:8081/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Review pull requests",
    "description": "Go through open PRs before the team standup",
    "dueDate": "2026-05-20",
    "priority": "HIGH",
    "status": "IN_PROGRESS"
  }'
```

### Error response shape

All errors return a consistent JSON body:

```json
{
  "timestamp": "2026-05-16T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 99"
}
```

---

## Running the tests

```bash
./mvnw test
```

The test suite does not require an Anthropic API key — the Claude integration is mocked in all tests.

---

## H2 Console

An in-browser SQL console is available at `http://localhost:8081/h2-console` while the app is running.

| Field    | Value                                      |
|----------|--------------------------------------------|
| JDBC URL | `jdbc:h2:mem:taskdb`                       |
| Username | `sa`                                       |
| Password | *(leave blank)*                            |

---

## Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Runtime     | Java 17                             |
| Framework   | Spring Boot 3.2.5                   |
| Persistence | Spring Data JPA, H2 (in-memory)     |
| Validation  | Jakarta Bean Validation             |
| AI          | Anthropic Java SDK 2.32.0 (Claude Sonnet 4.6) |
| Tests       | JUnit 5, Mockito, MockMvc           |
| UI          | Vanilla HTML / CSS / JS             |
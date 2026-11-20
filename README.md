# GitHub Repository Searcher (Backend Assignment)

Spring Boot REST service that searches GitHub repositories through the GitHub REST API,
stores the results in PostgreSQL, and exposes an endpoint for querying the stored data
with optional filters. No UI is included.

## Stack

- Java 17, Spring Boot 3.3 (Web, Data JPA, Validation)
- PostgreSQL 16 (Docker Compose provided)
- Maven
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, MockMvc, H2 (tests)

## Run locally

```bash
# 1. Start PostgreSQL
docker compose up -d

# 2. Run the application
mvn spring-boot:run

# 3. Run the tests (H2 in-memory, no database required)
mvn test
```

The service starts on `http://localhost:8080`.

Optional environment variables:

| Variable | Default | Description |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/githubsearch` | JDBC URL |
| `DB_USER` | `githubsearch` | Database user |
| `DB_PASSWORD` | `githubsearch` | Database password |
| `GITHUB_TOKEN` | *(empty)* | GitHub token for higher API rate limits |
| `GITHUB_API_URL` | `https://api.github.com` | GitHub API base URL |

Swagger UI: <http://localhost:8080/swagger-ui.html>
OpenAPI JSON: <http://localhost:8080/v3/api-docs>

No Docker? Use the in-memory H2 demo profile:

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Health check: <http://localhost:8080/actuator/health>

## API

### 1. Search GitHub and persist results

`POST /api/github/search`

```json
{
  "query": "spring boot",
  "language": "Java",
  "sort": "stars"
}
```

- `query` — required, non-blank. Supports partial names/keywords.
- `language` — optional (sent as GitHub `language:` qualifier).
- `sort` — optional: `stars` (default), `forks`, `updated`.

Response `200 OK`:

```json
{
  "message": "Repositories fetched and saved successfully",
  "repositories": [
    {
      "id": 123456,
      "name": "spring-boot-example",
      "description": "An example repository for Spring Boot",
      "owner": "user123",
      "language": "Java",
      "stars": 450,
      "forks": 120,
      "lastUpdated": "2024-01-01T12:00:00Z",
      "htmlUrl": "https://github.com/user123/spring-boot-example",
      "openIssues": 5,
      "watchers": 8
    }
  ]
}
```

If GitHub has no matches, the response is `200 OK` with
`"message": "No repositories found for the given criteria."` and an empty list.

### 2. Retrieve stored results

`GET /api/github/repositories?language=Java&minStars=100&sort=stars`

| Parameter | Type | Description |
| --- | --- | --- |
| `language` | string, optional | Case-insensitive language filter |
| `minStars` | long, optional | Minimum star count (`>= 0`) |
| `sort` | string, optional | `stars` (default), `forks`, `updated`; descending |

Response `200 OK`:

```json
{
  "repositories": [ { "...": "same repository shape as above" } ]
}
```

### Errors

All errors return a consistent body:

```json
{
  "timestamp": "2024-01-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "validationErrors": { "query": "query must not be blank" }
}
```

| Status | Situation |
| --- | --- |
| `400` | Invalid/missing body fields, invalid `sort`, negative `minStars`, malformed JSON |
| `429` | GitHub rate limit exceeded (includes `Retry-After` when known) |
| `502` | GitHub API unreachable, times out, or returns an error status |
| `500` | Unexpected server error |

## Design notes

- **Layers:** controller → service → client/repository, with a dedicated mapper and DTOs.
- **Upsert:** GitHub repository id is the database primary key and `saveAll` performs
  select-then-upsert, so repeated searches update existing rows instead of duplicating them.
- **Filtering:** JPA `Specification`s for optional filters; sort keys are whitelisted via
  `SortOption`, so arbitrary column names cannot be injected through request parameters.
- **Resilience:** GitHub errors, secondary/primary rate limits, empty results and invalid
  responses are handled explicitly; timeouts and a configurable result page size are supported.
- **Testing:** controller slice tests, mocked-client service tests, mocked HTTP tests for the
  GitHub client, and real JPA filter/upsert tests against H2.

A ready-to-import Postman collection is included in `postman_collection.json`.

## Submission assets

- `DEPLOYMENT.md` — step-by-step Render deployment (Blueprint + Docker + free PostgreSQL)
- `render.yaml` / `Dockerfile` — ready-to-use Render configuration
- `docs/swagger-ui.html` + `docs/openapi.json` — static Swagger documentation
- `docs/slides.html` — demo slide deck for screen recording
- `SUBMISSION.md` — live URL placeholders and a timestamped Loom recording script


# Submission Checklist

| Item | Link / Value |
| --- | --- |
| Live deployed URL | https://github-repository-searcher-xnbz.onrender.com |
| Swagger UI (live) | `https://github-repository-searcher-xnbz.onrender.com/swagger-ui.html` |
| OpenAPI JSON (live) | `https://github-repository-searcher-xnbz.onrender.com/v3/api-docs` |
| Static Swagger UI (repo) | `docs/swagger-ui.html` (open it in a browser; loads `docs/openapi.json`) |
| Static OpenAPI spec | `docs/openapi.json` |
| Loom video | _TODO — fill after recording_ |
| Demo slide deck | `docs/slides.html` — open in a browser and screen-record while narrating |
| Deployment guide | `DEPLOYMENT.md` — Render Blueprint steps |

---

## Loom recording script (~4 minutes)

Use this as a voice-over while showing the screen. Start a new Loom recording with
**Screen + Camera** (camera optional) and follow the timeline.

### 0:00–0:25 — Intro
> "Hi, this is my GitHub Repository Searcher backend assignment. It's a Spring Boot REST
> application that searches GitHub through their REST API, stores the results in PostgreSQL,
> and exposes an endpoint to query the stored repositories with filters. Everything is API-only —
> there is no UI."

Show the repository folder and `README.md` briefly.

### 0:25–0:55 — Run the app
Show the terminal:

```bash
docker compose up -d     # starts PostgreSQL
mvn spring-boot:run
```

Alternatively show the no-Docker demo:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

> "The app starts on port 8080. The demo profile uses in-memory H2 so it can be shown without Docker."

### 0:55–1:40 — Swagger documentation
Open `https://github-repository-searcher-xnbz.onrender.com/swagger-ui.html` (or `http://localhost:8080/swagger-ui.html`).

> "Here is the generated Swagger documentation. Two endpoints: POST /api/github/search and
> GET /api/github/repositories. I'll execute each one."

### 1:40–2:40 — POST /api/github/search
In Swagger, click **Try it out** on the POST endpoint and paste:

```json
{ "query": "spring boot", "language": "Java", "sort": "stars" }
```

Click **Execute**.

> "The service calls api.github.com/search/repositories, maps each item, and upserts it using the
> GitHub repository ID as the primary key — so running this again updates the rows instead of
> creating duplicates. The response contains the saved repositories plus a success message."

If GitHub is rate-limited without a token, mention:

> "Without a token GitHub allows 60 requests per hour; setting GITHUB_TOKEN raises that limit.
> Rate-limit responses are translated into HTTP 429 with a Retry-After header."

### 2:40–3:25 — GET /api/github/repositories
Try it out with:

- `language` = `Java`
- `minStars` = `100`
- `sort` = `stars`

Click **Execute**.

> "This queries the local database, not GitHub. Filters are applied with JPA Specifications and the
> sort key is whitelisted through an enum — stars, forks, or updated — descending."

### 3:25–3:50 — Validation and errors
Show one invalid call, e.g. POST with empty `query`, and point out the 400 response with field errors.

> "Validation errors return a consistent error body. GitHub API failures become 502, and rate limits
> become 429."

### 3:50–4:10 — Tests and architecture
Show the test run / structure:

```bash
mvn test
```

> "There are 22 passing tests covering controllers, the GitHub HTTP client including rate limits and
> errors, service logic, and real JPA filter/upsert tests against H2. The code is layered into
> controller, service, client/repository, mapper and DTOs."

### 4:10–4:25 — Outro
> "That completes the demo. Links to the live app, Swagger docs and this repository are in the
> submission description. Thank you."

---

## Before recording

1. Confirm the live URL responds: `https://github-repository-searcher-xnbz.onrender.com/swagger-ui.html`.
2. Set `GITHUB_TOKEN` on the deployed app if possible, so the POST demo won't hit rate limits.
3. Run the POST once before recording so the GET has data to show.
4. Hide notifications, close unrelated tabs, and use a 1080p recording.

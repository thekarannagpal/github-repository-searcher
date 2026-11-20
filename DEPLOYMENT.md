# Deploy to Render

The repository includes a Render Blueprint (`render.yaml`) and a `Dockerfile`, so the whole
stack (web service + PostgreSQL) can be created in a few clicks. You need a free Render account
and the code pushed to a GitHub repository.

## 1. Push the code to GitHub

```bash
cd C:\Users\Karan\github-repository-searcher
git init
git add .
git commit -m "GitHub Repository Searcher backend"
git remote add origin https://github.com/<your-username>/<your-repo>.git
git branch -M main
git push -u origin main
```

## 2. Deploy with the Blueprint

1. Sign up at <https://render.com> using GitHub (free tier).
2. In the Render Dashboard click **New + → Blueprint**.
3. Connect the GitHub repository you just created.
4. Render detects `render.yaml`. Name the blueprint and click **Apply**.
5. When prompted for `GITHUB_TOKEN`, you can leave it empty and add it later in the service's
   **Environment** tab. A token is recommended (GitHub allows only 60 unauthenticated requests/hour).
6. Wait for the first build (~5–10 minutes). Render will create:
   - `github-repository-searcher` — Docker web service
   - `github-search-db` — PostgreSQL database

Your live URL will be shown in the dashboard, typically:

```
https://github-repository-searcher.onrender.com
```

## 3. Verify the deployment

```text
GET  https://github-repository-searcher.onrender.com/actuator/health   -> {"status":"UP"}
GET  https://github-repository-searcher.onrender.com/swagger-ui.html   -> Swagger UI
GET  https://github-repository-searcher.onrender.com/v3/api-docs       -> OpenAPI JSON
POST https://github-repository-searcher.onrender.com/api/github/search
```

Example POST body:

```json
{ "query": "spring boot", "language": "Java", "sort": "stars" }
```

Then:

```text
GET https://github-repository-searcher.onrender.com/api/github/repositories?language=Java&minStars=100&sort=stars
```

## 4. Fill in the submission

Copy the live URL into `SUBMISSION.md` and your submission form:

- Live URL: `https://github-repository-searcher.onrender.com`
- Swagger: `https://github-repository-searcher.onrender.com/swagger-ui.html`
- Loom: link to your recording

## Notes

- Free web services **spin down when idle**; the first request after inactivity can take ~50 seconds.
  Warm it up before recording the Loom video.
- Render's free PostgreSQL plan may have a limited lifetime; check the current Render terms and
  upgrade to a paid plan if you need the instance to stay indefinitely.
- The app reads `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` and `DB_PASSWORD` from the Blueprint's
  database wiring, and `PORT` is supplied by Render automatically.
- `GITHUB_TOKEN` is optional but raises the GitHub API rate limit from 60 to 5,000 requests/hour.

## Manual setup (alternative)

If you prefer not to use a Blueprint:

1. **New + → PostgreSQL**: create a database, copy its host, port, database, user and password.
2. **New + → Web Service → Deploy an existing image or Docker repository**: point it at your
   GitHub repo; Render will use the `Dockerfile`.
3. Add the five `DB_*` environment variables plus `GITHUB_TOKEN`.
4. Set **Health Check Path** to `/actuator/health`.

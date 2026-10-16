# Track The Hill

A nonpartisan website for exploring how members of the U.S. Congress vote, built on primary-source government and academic data.

It covers all 536 members of the 119th Congress: voting records, bills, cosponsorships, and committee assignments in one searchable place.

Add screenshots to docs/screenshots/ and uncomment:
[Home](docs/screenshots/home.png)
[Member profile](docs/screenshots/member-profile.png)

## Features

- Browse the House and Senate, search by name, or pick a state
- Member profiles with committees, attendance, partisan voting score, ideology score, sponsored and cosponsored bills, and voting history
- Voting history filters by issue area and vote type (final passage, amendment, procedural)
- Bill pages with official CRS summaries, cosponsors, every vote on the bill, and an enactment status badge
- Full Yea/Nay breakdown for any roll call, linked to each member

## Tech stack

- **Backend:** Java 21, Spring Boot, Spring Data JPA, WebClient
- **Database:** PostgreSQL, Flyway migrations
- **Frontend:** React, Vite, React Router, Tailwind CSS

## Data sources

- [Congress.gov API](https://api.congress.gov): members, bills, cosponsors, bill summaries, House votes
- [Voteview](https://voteview.com) (UCLA): Senate votes, DW-NOMINATE scores
- [unitedstates/congress-legislators](https://github.com/unitedstates/congress-legislators): committee assignments

## Engineering notes

- **Senate votes come from a different source.** Congress.gov's vote API covers only the House, and Senate.gov blocks automated access. Senate votes are streamed from Voteview and filtered to the target Congress.
- **Chamber-prefixed vote IDs.** House and Senate roll call numbers are independent, so IDs like `H-119-1-23` and `S-119-1-2` keep one chamber from overwriting the other.
- **Idempotent, retry-safe syncs.** Every sync upserts, and external calls retry with backoff.
- **Curated bill set.** Of 17,000+ bills introduced, the site tracks those showing real legislative progress, and the bills page limits to bills with a recorded vote. This is disclosed in the UI.

## Running locally

Requires JDK 21, PostgreSQL, Node 20+, and a free [Congress.gov API key](https://api.congress.gov/sign-up/).

```bash
createdb trackthehill_dev
export CONGRESS_API_KEY="..."
export ADMIN_API_KEY="any-long-random-string"   # protects /api/sync/* endpoints

./mvnw spring-boot:run                 # backend on :8080, Flyway creates the schema
cd frontend && npm install && npm run dev   # frontend on :5173
```

Load data by calling the sync endpoints with the header `X-Admin-Key: $ADMIN_API_KEY` (members, then bills, then votes). The full load makes thousands of rate-limited API calls and takes a while.

## Status

The ingestion pipeline, REST API, and all frontend pages are working. Still in progress: error and 404 states, a committee browse page, automated tests, and cloud deployment.

*Independent project, not affiliated with Congress or any government agency.*
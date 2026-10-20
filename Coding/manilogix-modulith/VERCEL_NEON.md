# Vercel backend + Neon PostgreSQL

Use the tested `postgres` profile for a dedicated ManiLogix Neon project. The default MySQL profile and the MySQL legacy exporter/importer remain available, but do not point this PostgreSQL deployment at the Finance or Stayforge databases.

Set the Vercel project's Git repository to `rehman4113/Mani-Logix-B2B` with root directory `Coding/manilogix-modulith`. Vercel detects `Dockerfile.vercel`, builds the Java 21 HTTP container Function, and provides the runtime port through `PORT`.

Configure encrypted runtime variables:
- `SPRING_PROFILES_ACTIVE=postgres`
- `DB_URL=jdbc:postgresql://<direct-Neon-host>/neondb?sslmode=require`
- `DB_USERNAME=<Neon role>`
- `DB_PASSWORD=<Neon role password>`
- `JWT_SECRET=<unique random secret of at least 32 UTF-8 bytes>`

Use the direct Neon endpoint because Flyway runs at application startup, and this profile sets PostgreSQL `search_path` for the private `manilogix` schema. The JDBC Hikari pool is capped at three connections per instance. A future migration to pooled runtime connections should first move schema migrations into a separate direct-connection task and avoid session-scoped `SET search_path` on the transaction pooler.

On an empty project, Flyway creates schema `manilogix` and the PostgreSQL V1–V3 migrations, and Hibernate validates the tables. For an empty development demo, temporary `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` create the initial admin; remove both from Vercel after first startup. Do not bootstrap before importing historical data into an empty destination.

Verify `GET /actuator/health` returns `{"status":"UP"}` and signup/login read and write through the live API. Existing MySQL/MongoDB snapshots are **not imported** by creating a new Neon database. The existing `migration/import_to_mysql.py` is MySQL-only; adapting and testing a PostgreSQL importer is separate work.

# Vercel backend + Supabase PostgreSQL

The default application profile continues to use MySQL. To deploy this isolated PostgreSQL target, set the Vercel project root to `Coding/manilogix-modulith`; Vercel detects `Dockerfile.vercel` and builds a container-image HTTP Function. The app listens on `PORT`.

Create a **dedicated** Supabase PostgreSQL project for ManiLogix. Do not reuse the Stayforge or Personal Finance databases. The `manilogix` schema is created by Flyway on initial startup, separate from the Data API exposed `public` schema. Flyway selects the PostgreSQL V1–V3 SQL migrations when the `postgres` profile is active. These are equivalent fresh-schema migrations; do not switch an already populated MySQL database's JDBC URL to PostgreSQL.

Set encrypted runtime environment variables:
- `SPRING_PROFILES_ACTIVE=postgres`
- `DB_URL=jdbc:postgresql://<Supavisor-host>:5432/postgres?sslmode=require` (session pooler, region close to Vercel; direct connectivity if supported)
- `DB_USERNAME=postgres.<project-ref>`
- `DB_PASSWORD=<database password>`
- `JWT_SECRET=<unique random secret at least 32 UTF-8 bytes>`
- For the **first fresh deployment only**, `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` (at least 12 characters). Remove these after startup. Do not bootstrap when preparing to import historical data into an empty target.

Configure Vercel's Function region near the Supabase project. Check `GET /actuator/health` returns `{"status":"UP"}` and verify signup/login and a database-backed API. Health returns unavailable on database failure. Container image Functions scale to zero; startup migrations must complete before live API traffic.

The previous MySQL/MongoDB exporter and `migration/import_to_mysql.py` remain MySQL-only. They **cannot** populate this PostgreSQL target. Historical data migration needs a separate, tested PostgreSQL importer; no historical data is claimed migrated by a clean deployment.

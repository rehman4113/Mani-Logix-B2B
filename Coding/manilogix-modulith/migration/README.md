# Legacy MySQL and MongoDB → ManiLogix MySQL

This imports the original **auth MySQL**, **supplier MySQL**, **order MySQL**, **inventory MongoDB** and **notification MongoDB** into a new, empty Modulith MySQL database. It never writes to the source databases. Keep all source services paused or read only while taking the snapshot.

## Safety and mapping

- Users preserve numeric IDs, email and BCrypt password hashes; the old role join must resolve to **exactly one** ADMIN, MANAGER or SUPPLIER role per user. Resolve multiple roles manually before importing.
- Suppliers preserve numeric IDs, contact details and created date. Each must match a SUPPLIER user by email. If the email differs, supply `--owner-map owners.json` with a JSON object such as `{"7":2}` mapping old supplier ID to old user ID.
- Every inventory Mongo document moves to `inventory_items`, retaining its old Mongo ID in `legacy_mongo_id`, category, quantity and reorder level. Old inventory has **no price**. Imported products are inactive with `needs_price_review=1`; the owner or staff sets a positive price via `PUT /api/inventory/{id}` to make them orderable. Never invent a price or reserve stock twice.
- Original orders lack quantities and price snapshots. Their complete rows and item collection rows move to `legacy_records` as `ORDER`, requiring manual reconciliation before conversion into operational purchase orders. Original notification documents also move to `legacy_records` as `NOTIFICATION`. They are retained in MySQL but are not shown in the new notification API until reconciled. This prevents fabricated balances or wrong recipients.
- Import requires **empty** destination business tables and uses a single transaction. A SHA-256 recorded in `legacy_migration_runs` makes rerunning the same snapshot a no-op; a different snapshot is rejected.

## Runbook

1. Back up the three MySQL databases and both MongoDB databases. Restrict access to the backup and migration host.
2. Create a **new empty** MySQL 8 database for the Modulith, set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and start the Modulith once to apply Flyway V1–V3. Do **not** set bootstrap admin variables before import because the destination business tables must be empty. Stop the application.
3. Install Python requirements locally in an isolated environment: `python -m pip install -r Coding/manilogix-modulith/migration/requirements.txt`.
4. Set `LEGACY_AUTH_MYSQL_URL`, `LEGACY_SUPPLIER_MYSQL_URL`, `LEGACY_ORDER_MYSQL_URL` to URL-encoded `mysql://user:password@host:port/database` connections with read-only credentials. Set `LEGACY_INVENTORY_MONGO_URI`, `LEGACY_INVENTORY_MONGO_DB`, `LEGACY_NOTIFICATION_MONGO_URI`, `LEGACY_NOTIFICATION_MONGO_DB` to read-only Mongo connections and database names.
5. Run `python Coding/manilogix-modulith/migration/export_legacy.py --output /secure/path/manilogix-snapshot.json`. The file is created with owner-only permissions and includes password hashes and personal data; never commit it.
6. Run `python Coding/manilogix-modulith/migration/import_to_mysql.py --snapshot /secure/path/manilogix-snapshot.json` for an **offline preflight**. Fix any ambiguous supplier or user role mapping at the source or through the owner map. Never change the frozen snapshot without exporting a fresh one.
7. Set `TARGET_MYSQL_URL` to the empty Modulith database (with INSERT permission). Repeat the same command with `--apply` after a successful preflight. The script checks destination emptiness and commits or rolls back the whole import.
8. Verify `legacy_migration_runs.summary_json`, row counts in `app_users`, `suppliers`, `inventory_items`, and `legacy_records`. Check `SELECT id, legacy_mongo_id, quantity, needs_price_review FROM inventory_items`. Reconcile all archived orders and notifications before any production cutover; confirm user ownership and stock counts against the original backups.
9. Start the Modulith and test existing user logins. Review and price imported stock. Keep the old systems read only until reconciliation is signed off. Remove the sensitive snapshot from the migration host when retention rules allow.

**Important:** The original databases in this repository point to `localhost`; their current contents are not available to this project. The importer and test fixture are verified, but a real data transfer requires the actual source databases and a reviewed snapshot.

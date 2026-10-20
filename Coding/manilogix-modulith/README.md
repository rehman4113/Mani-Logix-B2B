# ManiLogix Modulith

One Spring Boot 3.5 application, five Spring Modulith modules: `auth`, `supplier`, `inventory`, `ordering`, `notification`. The previous independent services remain in `Coding/*-service` for reference; this application is the replacement runtime. No Eureka, gateway, Kafka or MongoDB is needed for this runtime.

## Local startup

Create an empty MySQL 8 database named `manilogix`, then set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` (at least 32 UTF-8 bytes). For the first run on an **empty** database, set `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` (at least 12 characters). Remove the bootstrap variables after first startup. Java 21 is required.

```sh
cd Coding/manilogix-modulith
mvn test
mvn spring-boot:run
```

Flyway creates the schema. Hibernate validates it. Flyway creates the Spring Modulith JDBC event publication table; the order notification listener persists after commit. The module structure test rejects cycles and references into another module's internals.

## APIs

- `POST /api/auth/signup` registers a CUSTOMER. `POST /api/auth/login` returns a bearer token. Public signup cannot create privileged users.
- `POST /api/auth/users` provisions supplier and manager users as admin. `POST /api/suppliers` is admin only; supplier owners can view and edit their own profile. An existing supplier account's numeric user ID is required.
- `GET /api/inventory?page=0&size=20` browses active stock. `POST /api/inventory` creates stock for an active supplier; owner, manager, or admin can manage it. `GET /api/inventory/{id}` reads one listed item.
- `POST /api/orders` takes `{"items":[{"itemId":1,"quantity":2}],"deliveryAddress":"..."}` for a CUSTOMER. The authenticated user becomes the customer. Items must belong to one supplier; stock is reserved under row locks in the same database transaction. `PATCH /api/orders/{id}/status?status=CONFIRMED` advances PENDING → CONFIRMED → SHIPPED → DELIVERED. Cancellation from PENDING/CONFIRMED returns stock once.
- `GET /api/notifications` returns the caller's notifications; only admin can list all.

## Migration from the old services

Use the [migration runbook](migration/README.md) for a read-only snapshot from the legacy MySQL and MongoDB databases, offline preflight, and transactional import into an empty Flyway V1–V3 MySQL schema. Historical orders and notifications are preserved for review; imported inventory stays inactive until priced.

## Current scope

This establishes a consistent medium project baseline: role-safe registration, owner-aware supplier and stock access, atomic stock reservation, guarded order transitions, and persisted notification events. Before production, add operational features such as onboarding supplier accounts, product search/pagination, payment and invoicing, returns, an inventory movement ledger, and API integration tests against MySQL. Existing clients must be updated from the old service routes and payloads.


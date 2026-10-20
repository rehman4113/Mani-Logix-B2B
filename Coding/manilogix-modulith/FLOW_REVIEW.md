# ManiLogix flow review

## Findings in the original microservices

| Area | Observed issue | Modulith baseline |
| --- | --- | --- |
| Identity | Public signup trusts a requested role; users endpoint returns JPA users including password hashes. | Public signup creates CUSTOMER only; admin provisioning has role checks; auth returns narrow DTOs. |
| Supplier | Supplier profile updates authorize SUPPLIER role without checking ownership of the requested supplier ID. | Profile and inventory changes check the authenticated user's ownership. |
| Inventory | Supplier lookup uses `findById(supplierId)` on the inventory repository; stock can be overwritten by updates; no quantity validation. | Stock uses one MySQL database, positive quantity checks, row locks for reserve/restock, and scoped supplier reads. |
| Ordering | Order creation trusts customer/supplier IDs from the request and stores arbitrary item IDs without validating stock. The SUPPLIER role creates orders and changes status without ownership checks. | Customer ID comes from JWT; stock is reserved transactionally; one supplier per order; allowed status transitions and cancellation restock are enforced. |
| Notifications | Kafka consumer is commented out; notification config names the order service and points at an order MySQL DB while the repository is MongoDB. Routes have no application security. | Event listener persists an order notification after commit; routes require authentication and limit personal reads. |
| Deployment | Multiple Spring Boot and Cloud versions, Eureka, gateway, MySQL and MongoDB are needed for a small to medium product. | One Java 21 process, one MySQL 8 database, Flyway and Spring Modulith module checks. |

## Implemented order flow

1. Admin creates a supplier account and an owned supplier profile; a customer can register directly.
2. An authorized supplier or staff user adds inventory. Customer can browse active items.
3. Customer places an order for items belonging to one active supplier.
4. The order transaction locks inventory rows in item ID order, reserves stock, records price snapshots and publishes an order event. Failure rolls back all reserved quantities.
5. The supplier owner or staff advances PENDING → CONFIRMED → SHIPPED → DELIVERED; the customer may cancel before shipping. Cancellation restocks once.
6. A persistent Spring Modulith event publication invokes the notification listener after the order commits.

## Remaining work before real business use

- **Data migration:** Import old MySQL users/suppliers/orders and MongoDB stock/notifications with an audited ID mapping and reconciliation. Nothing is automatically copied.
- **B2B lifecycle:** Add purchase order approval, supplier acceptance, partial fulfillment, returns, pricing/currency/tax, invoice and payment records. The current order is a simple single supplier purchase.
- **Inventory accounting:** Record immutable stock movements with reasons and actor IDs. Restrict manual quantity replacement once orders exist; use adjustments rather than overwriting stock.
- **Operational hardening:** Paginate order and notification lists; cap request payloads; add login rate limits, refresh/revocation rules, pagination filters, monitoring and backup exercises.
- **Testing:** The MySQL integration test verifies signup, role restriction, stock reservation and cancellation. Add concurrency, failure retry, supplier ownership, and migration reconciliation tests before cutover.
- **Notifications:** The event publication is durable, but add operator recovery/republication procedures and more notification types. No email, SMS or WebSocket delivery is implemented.

## Cutover

Do not replace a deployed service in place. Back up its databases, import into an empty Modulith database, compare record counts and balances, update consumers to the new API, run end to end tests, then switch traffic. Keep the original services read only until reconciled.

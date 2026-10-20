CREATE TABLE app_users (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 email VARCHAR(190) NOT NULL UNIQUE,
 password_hash VARCHAR(255) NOT NULL,
 role VARCHAR(20) NOT NULL
);
CREATE TABLE suppliers (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(255) NOT NULL,
 email VARCHAR(255) NOT NULL,
 owner_user_id BIGINT NOT NULL,
 active BIT NOT NULL DEFAULT 1,
 created_at DATETIME(6) NOT NULL,
 INDEX idx_supplier_owner (owner_user_id)
);
CREATE TABLE inventory_items (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 supplier_id BIGINT NOT NULL,
 name VARCHAR(255) NOT NULL,
 quantity INT NOT NULL,
 reorder_level INT NOT NULL,
 unit_price DECIMAL(15,2) NOT NULL,
 active BIT NOT NULL DEFAULT 1,
 INDEX idx_inventory_supplier (supplier_id),
 CONSTRAINT ck_inventory_quantity CHECK (quantity >= 0)
);
CREATE TABLE purchase_orders (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 customer_id BIGINT NOT NULL,
 supplier_id BIGINT NOT NULL,
 delivery_address VARCHAR(255) NOT NULL,
 status VARCHAR(24) NOT NULL,
 created_at DATETIME(6) NOT NULL,
 total DECIMAL(15,2) NOT NULL,
 INDEX idx_order_customer (customer_id),
 INDEX idx_order_supplier (supplier_id)
);
CREATE TABLE order_lines (
 order_id BIGINT NOT NULL,
 item_id BIGINT NOT NULL,
 quantity INT NOT NULL,
 unit_price DECIMAL(15,2) NOT NULL,
 CONSTRAINT fk_order_line_order FOREIGN KEY (order_id) REFERENCES purchase_orders(id)
);
CREATE TABLE notifications (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT NOT NULL,
 order_id BIGINT NOT NULL,
 kind VARCHAR(255) NOT NULL,
 message VARCHAR(255) NOT NULL,
 created_at DATETIME(6) NOT NULL,
 CONSTRAINT uk_notification_order_kind UNIQUE (order_id, kind),
 INDEX idx_notification_user_created (user_id, created_at)
);


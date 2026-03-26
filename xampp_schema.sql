CREATE DATABASE IF NOT EXISTS distributed_drinks_business;
USE distributed_drinks_business;

DROP TABLE IF EXISTS stock_alerts;
DROP TABLE IF EXISTS stock_levels;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS drinks;
DROP TABLE IF EXISTS branches;

CREATE TABLE branches (
    branch_id INT AUTO_INCREMENT PRIMARY KEY,
    branch_code VARCHAR(20) NOT NULL UNIQUE,
    branch_name VARCHAR(100) NOT NULL,
    is_headquarter TINYINT(1) NOT NULL DEFAULT 0
);

CREATE TABLE drinks (
    drink_id INT AUTO_INCREMENT PRIMARY KEY,
    drink_code VARCHAR(20) NOT NULL UNIQUE,
    drink_name VARCHAR(100) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL
);

CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(120) NOT NULL,
    phone_number VARCHAR(30) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    branch_id INT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_orders_branch FOREIGN KEY (branch_id) REFERENCES branches(branch_id)
);

CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    drink_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_drink FOREIGN KEY (drink_id) REFERENCES drinks(drink_id)
);

CREATE TABLE stock_levels (
    stock_id INT AUTO_INCREMENT PRIMARY KEY,
    branch_id INT NOT NULL,
    drink_id INT NOT NULL,
    quantity_available INT NOT NULL DEFAULT 40,
    minimum_threshold INT NOT NULL DEFAULT 10,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_stock UNIQUE (branch_id, drink_id),
    CONSTRAINT fk_stock_branch FOREIGN KEY (branch_id) REFERENCES branches(branch_id),
    CONSTRAINT fk_stock_drink FOREIGN KEY (drink_id) REFERENCES drinks(drink_id)
);

CREATE TABLE stock_alerts (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    branch_id INT NOT NULL,
    drink_id INT NOT NULL,
    quantity_remaining INT NOT NULL,
    minimum_threshold INT NOT NULL,
    alert_message VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alert_branch FOREIGN KEY (branch_id) REFERENCES branches(branch_id),
    CONSTRAINT fk_alert_drink FOREIGN KEY (drink_id) REFERENCES drinks(drink_id)
);

INSERT INTO branches (branch_code, branch_name, is_headquarter) VALUES
('NAIROBI', 'Nairobi Headquarter', 1),
('NAKURU', 'Nakuru Branch', 0),
('MOMBASA', 'Mombasa Branch', 0),
('KISUMU', 'Kisumu Branch', 0);

INSERT INTO drinks (drink_code, drink_name, unit_price) VALUES
('COKE', 'Coca Cola', 120.00),
('FANTA', 'Fanta', 110.00),
('SPRITE', 'Sprite', 115.00),
('WATER', 'Mineral Water', 80.00),
('JUICE', 'Mango Juice', 150.00);

INSERT INTO stock_levels (branch_id, drink_id, quantity_available, minimum_threshold)
SELECT b.branch_id, d.drink_id, 40, 10
FROM branches b
CROSS JOIN drinks d;

INSERT INTO customers (customer_name, phone_number) VALUES
('Alice', '0700000001'),
('Brian', '0700000002'),
('Carol', '0700000003');

INSERT INTO orders (customer_id, branch_id, total_amount) VALUES
(1, 2, 760.00),
(2, 3, 930.00),
(3, 4, 3565.00);

INSERT INTO order_items (order_id, drink_id, quantity, unit_price, line_total) VALUES
(1, 1, 5, 120.00, 600.00),
(1, 4, 2, 80.00, 160.00),
(2, 5, 4, 150.00, 600.00),
(2, 2, 3, 110.00, 330.00),
(3, 3, 31, 115.00, 3565.00);

UPDATE stock_levels SET quantity_available = 35 WHERE branch_id = 2 AND drink_id = 1;
UPDATE stock_levels SET quantity_available = 38 WHERE branch_id = 2 AND drink_id = 4;
UPDATE stock_levels SET quantity_available = 36 WHERE branch_id = 3 AND drink_id = 5;
UPDATE stock_levels SET quantity_available = 37 WHERE branch_id = 3 AND drink_id = 2;
UPDATE stock_levels SET quantity_available = 9 WHERE branch_id = 4 AND drink_id = 3;

INSERT INTO stock_alerts (branch_id, drink_id, quantity_remaining, minimum_threshold, alert_message) VALUES
(4, 3, 9, 10, 'Low stock at Kisumu Branch for Sprite. Remaining bottles: 9, threshold: 10');

CREATE OR REPLACE VIEW vw_orders_with_customer_branch AS
SELECT
    o.order_id,
    c.customer_name,
    b.branch_name,
    o.order_date,
    o.total_amount
FROM orders o
JOIN customers c ON o.customer_id = c.customer_id
JOIN branches b ON o.branch_id = b.branch_id;

CREATE OR REPLACE VIEW vw_sales_per_branch AS
SELECT
    b.branch_name,
    COALESCE(SUM(o.total_amount), 0.00) AS total_sales
FROM branches b
LEFT JOIN orders o ON b.branch_id = o.branch_id
GROUP BY b.branch_id, b.branch_name;

CREATE OR REPLACE VIEW vw_total_business_sales AS
SELECT COALESCE(SUM(total_amount), 0.00) AS grand_total
FROM orders;

CREATE OR REPLACE VIEW vw_low_stock AS
SELECT
    b.branch_name,
    d.drink_name,
    s.quantity_available,
    s.minimum_threshold
FROM stock_levels s
JOIN branches b ON s.branch_id = b.branch_id
JOIN drinks d ON s.drink_id = d.drink_id
WHERE s.quantity_available <= s.minimum_threshold;

-- REPORT QUERY 1: Customers who made orders and branches used
SELECT * FROM vw_orders_with_customer_branch ORDER BY order_id;

-- REPORT QUERY 2: Sales amount at each branch and headquarter
SELECT * FROM vw_sales_per_branch ORDER BY branch_name;

-- REPORT QUERY 3: Final total amount made by the business
SELECT * FROM vw_total_business_sales;

-- REPORT QUERY 4: Low stock signal report
SELECT * FROM vw_low_stock ORDER BY branch_name, drink_name;

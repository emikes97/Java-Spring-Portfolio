-- 1) Products in a specific category (by category name)
SELECT p.product_id, p.product_name, p.product_price, p.product_available_stock
FROM products p
JOIN product_category pc  ON pc.product_id = p.product_id
JOIN categories c           ON c.category_id = pc.category_id
WHERE c.category_name = 'Electronics'
ORDER BY p.product_name;

-- 2) Count of products per category
SELECT c.category_id, c.category_name, COUNT(*) AS product_count
FROM categories c
JOIN product_category pc ON pc.category_id = c.category_id
GROUP BY c.category_id, c.category_name
ORDER BY product_count DESC, c.category_name;

-- 3) Total stock (units) per category
SELECT c.category_name,
       SUM(p.product_available_stock) AS total_units_in_stock
FROM categories c
JOIN product_category pc ON pc.category_id = c.category_id
JOIN products p            ON p.product_id = pc.product_id
GROUP BY c.category_name
ORDER BY total_units_in_stock DESC;

-- 4) Inventory value per category (price * stock)
SELECT c.category_name,
       SUM(p.product_price * p.product_available_stock)::NUMERIC(14,2) AS inventory_value
FROM categories c
JOIN product_category pc ON pc.category_id = c.category_id
JOIN products p            ON p.product_id = pc.product_id
GROUP BY c.category_name
ORDER BY inventory_value DESC;

-- 5) Products belonging to multiple categories (useful showcase)
SELECT p.product_id, p.product_name, COUNT(pc.category_id) AS category_count
FROM products p
JOIN product_category pc ON pc.product_id = p.product_id
GROUP BY p.product_id, p.product_name
HAVING COUNT(pc.category_id) > 1
ORDER BY category_count DESC, p.product_name;


-- 6) Low-stock products (threshold param)
SELECT product_id, product_name, product_available_stock
FROM products
WHERE product_available_stock <= 10
ORDER BY product_available_stock, product_name;

-- 7) Out-of-stock products
SELECT product_id, product_name
FROM products
WHERE product_available_stock = 0
ORDER BY product_name;

-- 8) Recent orders with item counts and totals (nice “dashboard” row)
SELECT o.order_id,
       o.customer_id,
       o.order_status,
       o.order_created_at,
       COUNT(oi.order_item_id)                           AS items,
       SUM(oi.quantity * oi.price_at)::NUMERIC(12,2)   AS order_total
FROM orders o
JOIN order_item oi ON oi.order_id = o.order_id
GROUP BY o.order_id, o.customer_id, o.order_status, o.order_created_at
ORDER BY o.order_created_at DESC
LIMIT 25;
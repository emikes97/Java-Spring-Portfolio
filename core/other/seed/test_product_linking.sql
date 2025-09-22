BEGIN;

-- Electronics
WITH cat AS (SELECT category_id FROM categories WHERE category_name = 'Electronics'),
names(name) AS (
  VALUES
    ($$4K TV 55"$$),($$4K TV 65"$$),($$Soundbar 2.1$$),($$Bluetooth Speaker$$),
    ($$Microwave 20L$$),($$Air Fryer 5L$$),($$Robot Vacuum$$),($$Smart Bulb (4-Pack)$$),
    ($$Smart Plug (2-Pack)$$),($$Espresso Machine$$)
)
INSERT INTO product_category (product_id, category_id)
SELECT p.product_id, c.category_id
FROM products p JOIN names n ON p.product_name = n.name CROSS JOIN cat c
ON CONFLICT DO NOTHING;

-- PC
WITH cat AS (SELECT category_id FROM categories WHERE category_name = 'PC'),
names(name) AS (
  VALUES
    ($$Gaming Laptop 15"$$),($$Ultrabook 14"$$),($$Desktop Tower i5$$),($$Mechanical Keyboard$$),
    ($$Gaming Mouse$$),($$27" Monitor 144Hz$$),($$NVMe SSD 1TB$$),($$DDR5 32GB Kit$$),
    ($$ATX PSU 750W$$),($$USB-C Dock$$)
)
INSERT INTO product_category (product_id, category_id)
SELECT p.product_id, c.category_id
FROM products p JOIN names n ON p.product_name = n.name CROSS JOIN cat c
ON CONFLICT DO NOTHING;

-- Housing
WITH cat AS (SELECT category_id FROM categories WHERE category_name = 'Housing'),
names(name) AS (
  VALUES
    ($$Dining Table 6-Seat$$),($$Office Chair$$),($$Bookshelf 5-Tier$$),($$Floor Lamp$$),
    ($$Wall Clock$$),($$Rug 160x230$$),($$Cutlery Set 24pcs$$),($$Coffee Table$$),
    ($$Wardrobe 2-Door$$),($$Shoe Rack$$)
)
INSERT INTO product_category (product_id, category_id)
SELECT p.product_id, c.category_id
FROM products p JOIN names n ON p.product_name = n.name CROSS JOIN cat c
ON CONFLICT DO NOTHING;

-- Pets
WITH cat AS (SELECT category_id FROM categories WHERE category_name = 'Pets'),
names(name) AS (
  VALUES
    ($$Dry Dog Food 10kg$$),($$Cat Litter 10L$$),($$Dog Toy Rope$$),($$Cat Tower$$),
    ($$Pet Bed M$$),($$Nylon Leash$$),($$Cat Treats Salmon$$),($$Aquarium Filter$$),
    ($$Bird Feeder$$),($$Grooming Brush$$)
)
INSERT INTO product_category (product_id, category_id)
SELECT p.product_id, c.category_id
FROM products p JOIN names n ON p.product_name = n.name CROSS JOIN cat c
ON CONFLICT DO NOTHING;

-- Clothes
WITH cat AS (SELECT category_id FROM categories WHERE category_name = 'Clothes'),
names(name) AS (
  VALUES
    ($$Mens T-Shirt$$),($$Womens Hoodie$$),($$Jeans Slim Fit$$),($$Sneakers$$),
    ($$Socks 5-Pack$$),($$Waterproof Jacket$$),($$Casual Dress$$),($$Leather Belt$$),
    ($$Baseball Cap$$),($$Pajamas Set$$)
)
INSERT INTO product_category (product_id, category_id)
SELECT p.product_id, c.category_id
FROM products p JOIN names n ON p.product_name = n.name CROSS JOIN cat c
ON CONFLICT DO NOTHING;

-- Babies
WITH cat AS (SELECT category_id FROM categories WHERE category_name = 'Babies'),
names(name) AS (
  VALUES
    ($$Diapers Size 3 (70)$$),($$Baby Wipes (12x)$$),($$Baby Stroller$$),($$Baby Monitor$$),
    ($$Bottle Warmer$$),($$Pacifier (2-Pack)$$),($$Baby Shampoo$$),($$High Chair$$),
    ($$Baby Carrier$$),($$Play Mat$$)
)
INSERT INTO product_category (product_id, category_id)
SELECT p.product_id, c.category_id
FROM products p JOIN names n ON p.product_name = n.name CROSS JOIN cat c
ON CONFLICT DO NOTHING;

-- Games
WITH cat AS (SELECT category_id FROM categories WHERE category_name = 'Games'),
names(name) AS (
  VALUES
    ($$RPG (PC Digital)$$),($$Shooter (PC Digital)$$),($$Strategy (PC Digital)$$),($$Indie Platformer$$),
    ($$Racing Sim$$),($$Sports 2025$$),($$Puzzle Deluxe$$),($$MMO Time Card 60d$$),
    ($$DLC Expansion$$),($$Controller Charger$$)
)
INSERT INTO product_category (product_id, category_id)
SELECT p.product_id, c.category_id
FROM products p JOIN names n ON p.product_name = n.name CROSS JOIN cat c
ON CONFLICT DO NOTHING;

-- Consoles
WITH cat AS (SELECT category_id FROM categories WHERE category_name = 'Consoles'),
names(name) AS (
  VALUES
    ($$PlayStation 5$$),($$Xbox Series S$$),($$Nintendo Switch OLED$$),($$Joy-Con Pair$$),
    ($$DualSense Controller$$),($$Xbox Wireless Controller$$),($$microSD 1TB$$),
    ($$Charging Station$$),($$Console Headset$$),($$Retro Mini Console$$)
)
INSERT INTO product_category (product_id, category_id)
SELECT p.product_id, c.category_id
FROM products p JOIN names n ON p.product_name = n.name CROSS JOIN cat c
ON CONFLICT DO NOTHING;

-- books
WITH cat AS (SELECT category_id FROM categories WHERE category_name = 'books'),
names(name) AS (
  VALUES
    ($$Novel Bestseller$$),($$Sci-Fi Classic$$),($$Fantasy Epic$$),($$History Spotlight$$),
    ($$Self-Help Guide$$),($$Manga Vol. 1$$),($$Graphic Novel$$),($$Cookbook$$),
    ($$Children's Storybook$$),($$Mystery Thriller$$)
)
INSERT INTO product_category (product_id, category_id)
SELECT p.product_id, c.category_id
FROM products p JOIN names n ON p.product_name = n.name CROSS JOIN cat c
ON CONFLICT DO NOTHING;

-- DIY & Tools
WITH cat AS (SELECT category_id FROM categories WHERE category_name = 'DIY & Tools'),
names(name) AS (
  VALUES
    ($$Cordless Drill 18V$$),($$Impact Driver$$),($$Jigsaw$$),($$Circular Saw$$),
    ($$Tool Set 100pcs$$),($$Fiberglass Hammer$$),($$Screwdriver Set$$),($$Tape Measure 5m$$),
    ($$Glue Gun$$),($$Safety Glasses$$)
)
INSERT INTO product_category (product_id, category_id)
SELECT p.product_id, c.category_id
FROM products p JOIN names n ON p.product_name = n.name CROSS JOIN cat c
ON CONFLICT DO NOTHING;

COMMIT;
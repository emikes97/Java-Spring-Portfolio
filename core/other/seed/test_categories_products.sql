BEGIN;

INSERT INTO categories (category_name, category_description)
VALUES
  ('Electronics', 'TVs, audio, small appliances, and smart gadgets for every room—reliable brands at sharp prices.'),
  ('PC', 'Desktops, laptops, components, and peripherals—from entry rigs to pro workstations.'),
  ('Housing', 'Furniture & decor: tables, chairs, storage, and lighting to style your space.'),
  ('Pets', 'Food, treats, toys, and accessories to keep tails wagging and purrs going.'),
  ('Clothes', 'Everyday essentials and seasonal trends for men, women, and kids.'),
  ('Kids', 'Diapers, wipes, strollers, and nursery must-haves—gentle, safe, and parent-approved.'),
  ('Games', 'PC titles, digital codes, and accessories—indies to AAA. Play more.'),
  ('Consoles', 'PlayStation, Xbox, Nintendo, and retro classics—bundles, controllers, and more.'),
  ('books', 'Novels, non-fiction, comics, and manga—bestsellers to hidden gems.'),
  ('DIY & Tools', 'Power tools, hand tools, and hardware—build, repair, and create with confidence.')
ON CONFLICT (category_name) DO NOTHING;

COMMIT;

BEGIN;

-- (Optional) wipe products before reseeding
-- TRUNCATE TABLE products RESTART IDENTITY CASCADE;

-- ========== Electronics ==========
INSERT INTO products (product_name, product_description, product_details, product_available_stock, product_price)
VALUES
  ($$4K TV 55"$$,           $$Crisp 4K HDR picture with smart apps and voice control.$$ , $${"brand":"Auron","warranty":"2y","panel":"VA","hdr":"HDR10+","os":"SmartTV"}$$::jsonb, 25, 499.00),
  ($$4K TV 65"$$,           $$Large-screen 4K with Dolby Vision and slim bezels.$$      , $${"brand":"Auron","warranty":"2y","panel":"IPS","hdr":"Dolby Vision","os":"SmartTV"}$$::jsonb, 18, 799.00),
  ($$Soundbar 2.1$$,        $$Wireless subwoofer and HDMI ARC for cinematic sound.$$    , $${"brand":"SonicOne","channels":"2.1","inputs":["HDMI ARC","BT","Optical"]}$$::jsonb, 40, 159.00),
  ($$Bluetooth Speaker$$,   $$Portable IPX7 speaker with big sound and long battery.$$  , $${"brand":"SonicOne","battery":"18h","waterproof":"IPX7"}$$::jsonb, 60, 49.90),
  ($$Microwave 20L$$,       $$Compact microwave with quick-start presets.$$             , $${"brand":"HomePro","capacity":"20L","power":"700W"}$$::jsonb, 30, 79.90),
  ($$Air Fryer 5L$$,        $$Crispy results with little to no oil. Family size.$$      , $${"brand":"HomePro","capacity":"5L","programs":8}$$::jsonb, 22, 119.00),
  ($$Robot Vacuum$$,        $$Auto-docking robot vacuum with app scheduling.$$          , $${"brand":"Cleanix","suction":"2000Pa","mapping":true}$$::jsonb, 15, 249.00),
  ($$Smart Bulb (4-Pack)$$, $$Warm-to-cool smart bulbs with app & voice control.$$      , $${"brand":"Lumo","fitting":"E27","range":"2700-6500K"}$$::jsonb, 100, 29.90),
  ($$Smart Plug (2-Pack)$$, $$Monitor energy and automate your devices.$$               , $${"brand":"Lumo","max_load":"16A","voice":"Yes"}$$::jsonb, 85, 19.90),
  ($$Espresso Machine$$,    $$15-bar pump espresso with steam wand.$$                   , $${"brand":"Brewista","pressure":"15 bar","tank":"1.5L"}$$::jsonb, 12, 169.00);

-- ========== PC ==========
INSERT INTO products (product_name, product_description, product_details, product_available_stock, product_price)
VALUES
  ($$Gaming Laptop 15"$$, $$RTX graphics and high-refresh display for smooth play.$$ , $${"brand":"NovaTech","cpu":"i7","gpu":"RTX 4060","ram":"16GB","ssd":"1TB"}$$::jsonb, 10, 1299.00),
  ($$Ultrabook 14"$$,     $$Slim metal body with all-day battery life.$$             , $${"brand":"NovaTech","cpu":"i5","ram":"16GB","ssd":"512GB","weight":"1.2kg"}$$::jsonb, 14, 999.00),
  ($$Desktop Tower i5$$,  $$Quiet productivity build with easy upgrades.$$           , $${"brand":"BuildCore","cpu":"i5","ram":"16GB","ssd":"512GB"}$$::jsonb, 12, 799.00),
  ($$Mechanical Keyboard$$, $$Hot-swap switches and per-key backlight.$$             , $${"brand":"KeyWorks","layout":"ANSI","switch":"Tactile"}$$::jsonb, 50, 89.90),
  ($$Gaming Mouse$$,        $$Lightweight FPS mouse with adjustable DPI.$$           , $${"brand":"KeyWorks","dpi":"26K","buttons":6}$$::jsonb, 70, 39.90),
  ($$27" Monitor 144Hz$$,   $$Fast IPS panel with FreeSync/G-Sync compatible.$$      , $${"brand":"ViewMax","size":"27","refresh":"144Hz","panel":"IPS"}$$::jsonb, 20, 229.00),
  ($$NVMe SSD 1TB$$,        $$PCIe 4.0 speed for blazing load times.$$               , $${"brand":"FlashPro","seq_read":"7000MB/s","seq_write":"6000MB/s"}$$::jsonb, 80, 109.00),
  ($$DDR5 32GB Kit$$,       $$High-speed 2x16GB kit for modern builds.$$             , $${"brand":"MemX","speed":"6000MHz","cl":"36"}$$::jsonb, 60, 119.00),
  ($$ATX PSU 750W$$,        $$80+ Gold, fully modular cabling.$$                     , $${"brand":"PowerGrid","watt":"750W","rating":"80+ Gold"}$$::jsonb, 35, 109.00),
  ($$USB-C Dock$$,          $$Expand ports: HDMI, USB-A, LAN, PD charging.$$         , $${"brand":"NovaTech","ports":["HDMI","RJ45","USB-A","USB-C PD"]}$$::jsonb, 55, 69.90);

-- ========== Housing ==========
INSERT INTO products (product_name, product_description, product_details, product_available_stock, product_price)
VALUES
  ($$Dining Table 6-Seat$$, $$Sturdy wood table with easy-clean finish.$$ , $${"brand":"HomeCraft","material":"MDF/wood","size":"180x90cm"}$$::jsonb, 8, 249.00),
  ($$Office Chair$$,        $$Ergonomic mesh back with lumbar support.$$  , $${"brand":"HomeCraft","max_load":"120kg","adjust":"tilt/height"}$$::jsonb, 25, 129.00),
  ($$Bookshelf 5-Tier$$,    $$Slim profile storage for books and decor.$$ , $${"brand":"HomeCraft","material":"steel/wood","size":"170x60cm"}$$::jsonb, 20, 89.90),
  ($$Floor Lamp$$,          $$Warm ambient light with fabric shade.$$     , $${"brand":"HomeCraft","bulb":"E27","height":"160cm"}$$::jsonb, 30, 39.90),
  ($$Wall Clock$$,          $$Minimal metal frame with silent sweep.$$    , $${"brand":"HomeCraft","diameter":"30cm"}$$::jsonb, 40, 24.90),
  ($$Rug 160x230$$,         $$Soft weave rug that ties the room together.$$ , $${"brand":"HomeCraft","material":"polyester"}$$::jsonb, 18, 99.00),
  ($$Cutlery Set 24pcs$$,   $$Everyday stainless steel set for 6.$$       , $${"brand":"HomeCraft","steel":"18/10"}$$::jsonb, 50, 29.90),
  ($$Coffee Table$$,        $$Modern low table with shelf storage.$$       , $${"brand":"HomeCraft","size":"100x60x45cm"}$$::jsonb, 15, 79.90),
  ($$Wardrobe 2-Door$$,     $$Compact wardrobe with hanging rail.$$        , $${"brand":"HomeCraft","size":"180x80x50cm"}$$::jsonb, 10, 199.00),
  ($$Shoe Rack$$,           $$Stackable rack keeps entry neat.$$           , $${"brand":"HomeCraft","tiers":3}$$::jsonb, 35, 22.90);

-- ========== Pets ==========
INSERT INTO products (product_name, product_description, product_details, product_available_stock, product_price)
VALUES
  ($$Dry Dog Food 10kg$$, $$Balanced nutrition for active dogs.$$ , $${"brand":"PetJoy","flavor":"chicken","size":"10kg"}$$::jsonb, 28, 34.90),
  ($$Cat Litter 10L$$,    $$Low-dust, clumping litter with odor control.$$ , $${"brand":"PetJoy","type":"clumping","size":"10L"}$$::jsonb, 40, 12.90),
  ($$Dog Toy Rope$$,      $$Durable chew and tug fun.$$           , $${"brand":"PetJoy","size":"M"}$$::jsonb, 100, 6.90),
  ($$Cat Tower$$,         $$Multi-level scratching and lounging.$$ , $${"brand":"PetJoy","height":"140cm"}$$::jsonb, 12, 69.90),
  ($$Pet Bed M$$,         $$Cozy washable bed for small/medium pets.$$ , $${"brand":"PetJoy","size":"M"}$$::jsonb, 25, 19.90),
  ($$Nylon Leash$$,       $$Comfort grip, reflective stitching.$$ , $${"brand":"PetJoy","length":"1.5m"}$$::jsonb, 45, 9.90),
  ($$Cat Treats Salmon$$, $$Soft bites rich in omega-3.$$         , $${"brand":"PetJoy","weight":"150g"}$$::jsonb, 60, 3.90),
  ($$Aquarium Filter$$,   $$Quiet internal filter for clear water.$$ , $${"brand":"AquaLife","tank":"60L"}$$::jsonb, 20, 24.90),
  ($$Bird Feeder$$,       $$Weather-resistant feeder for gardens.$$ , $${"brand":"PetJoy","capacity":"1L"}$$::jsonb, 22, 14.90),
  ($$Grooming Brush$$,    $$Dual-sided brush for detangle & shine.$$ , $${"brand":"PetJoy","type":"dual"}$$::jsonb, 35, 8.90);

-- ========== Clothes ==========
INSERT INTO products (product_name, product_description, product_details, product_available_stock, product_price)
VALUES
  ($$Mens T-Shirt$$,      $$Soft cotton crew neck, everyday fit.$$ , $${"brand":"Wearly","material":"100% cotton","sizes":["S","M","L","XL"]}$$::jsonb, 80, 9.90),
  ($$Womens Hoodie$$,     $$Cozy fleece hoodie with kangaroo pocket.$$ , $${"brand":"Wearly","material":"cotton blend"}$$::jsonb, 45, 24.90),
  ($$Jeans Slim Fit$$,     $$Stretch denim that moves with you.$$    , $${"brand":"Wearly","material":"98% cotton","fit":"slim"}$$::jsonb, 50, 29.90),
  ($$Sneakers$$,           $$Breathable everyday sneakers.$$         , $${"brand":"Wearly","sizes":"38-45"}$$::jsonb, 40, 39.90),
  ($$Socks 5-Pack$$,       $$Cushioned ankle socks.$$                , $${"brand":"Wearly","count":5}$$::jsonb, 120, 7.90),
  ($$Waterproof Jacket$$,  $$Light rain jacket packs small.$$        , $${"brand":"Wearly","rating":"5k"}$$::jsonb, 25, 49.90),
  ($$Casual Dress$$,       $$Easy summer dress with flowy cut.$$     , $${"brand":"Wearly","material":"viscose"}$$::jsonb, 30, 24.90),
  ($$Leather Belt$$,       $$Genuine leather with metal buckle.$$    , $${"brand":"Wearly","width":"3.5cm"}$$::jsonb, 35, 14.90),
  ($$Baseball Cap$$,       $$Adjustable cap with curved brim.$$      , $${"brand":"Wearly"}$$::jsonb, 60, 9.90),
  ($$Pajamas Set$$,        $$Soft two-piece lounge set.$$            , $${"brand":"Wearly","material":"cotton blend"}$$::jsonb, 40, 19.90);

-- ========== Babies ==========
INSERT INTO products (product_name, product_description, product_details, product_available_stock, product_price)
VALUES
  ($$Diapers Size 3 (70)$$, $$Soft, breathable diapers with leak guards.$$ , $${"brand":"BabyCare","count":70,"size":"3"}$$::jsonb, 35, 12.90),
  ($$Baby Wipes (12x)$$,    $$Fragrance-free wipes for sensitive skin.$$   , $${"brand":"BabyCare","packs":12}$$::jsonb, 28, 14.90),
  ($$Baby Stroller$$,       $$Lightweight stroller with reclining seat.$$  , $${"brand":"BabyCare","weight":"6.5kg"}$$::jsonb, 10, 139.00),
  ($$Baby Monitor$$,        $$Audio/video monitor with night vision.$$     , $${"brand":"BabyCare","range":"250m"}$$::jsonb, 12, 79.90),
  ($$Bottle Warmer$$,       $$Gentle, even warming with auto-off.$$        , $${"brand":"BabyCare","modes":3}$$::jsonb, 18, 29.90),
  ($$Pacifier (2-Pack)$$,   $$Orthodontic design for comfort.$$            , $${"brand":"BabyCare","count":2}$$::jsonb, 60, 4.90),
  ($$Baby Shampoo$$,        $$Tear-free formula, dermatologically tested.$$ , $${"brand":"BabyCare","volume":"500ml"}$$::jsonb, 40, 5.90),
  ($$High Chair$$,          $$Stable high chair with easy-clean tray.$$    , $${"brand":"BabyCare","max_load":"15kg"}$$::jsonb, 8, 69.90),
  ($$Baby Carrier$$,        $$Ergonomic baby carrier for 0-24m.$$          , $${"brand":"BabyCare","positions":3}$$::jsonb, 9, 49.90),
  ($$Play Mat$$,            $$Cushioned, easy-fold play mat.$$             , $${"brand":"BabyCare","size":"180x120cm"}$$::jsonb, 15, 24.90);

-- ========== Games ==========
INSERT INTO products (product_name, product_description, product_details, product_available_stock, product_price)
VALUES
  ($$RPG (PC Digital)$$,       $$Epic role-playing adventure—digital code.$$ , $${"brand":"GameHub","platform":"PC","delivery":"digital"}$$::jsonb, 100, 49.99),
  ($$Shooter (PC Digital)$$,   $$Fast-paced FPS—digital code.$$              , $${"brand":"GameHub","platform":"PC","delivery":"digital"}$$::jsonb, 100, 59.99),
  ($$Strategy (PC Digital)$$,  $$Plan, build, conquer—digital code.$$        , $${"brand":"GameHub","platform":"PC"}$$::jsonb, 90, 39.99),
  ($$Indie Platformer$$,       $$Charming pixel adventure—digital code.$$    , $${"brand":"GameHub","platform":"PC"}$$::jsonb, 100, 14.99),
  ($$Racing Sim$$,             $$Realistic driving with wheel support.$$     , $${"brand":"GameHub","platform":"PC"}$$::jsonb, 80, 29.99),
  ($$Sports 2025$$,            $$Season update with online leagues.$$         , $${"brand":"GameHub","platform":"PC"}$$::jsonb, 80, 49.99),
  ($$Puzzle Deluxe$$,          $$Brain-teasing levels to relax.$$             , $${"brand":"GameHub","platform":"PC"}$$::jsonb, 120, 9.99),
  ($$MMO Time Card 60d$$,      $$Pre-paid time card for your MMO.$$           , $${"brand":"GameHub","platform":"PC"}$$::jsonb, 70, 19.99),
  ($$DLC Expansion$$,          $$New campaign and gear pack.$$                , $${"brand":"GameHub","platform":"PC"}$$::jsonb, 90, 24.99),
  ($$Controller Charger$$,     $$Dual charging dock for controllers.$$        , $${"brand":"GameHub","compat":"multi"}$$::jsonb, 60, 19.99);

-- ========== Consoles ==========
INSERT INTO products (product_name, product_description, product_details, product_available_stock, product_price)
VALUES
  ($$PlayStation 5$$,         $$Ultra-fast SSD and next-gen visuals.$$ , $${"brand":"Sony","storage":"1TB"}$$::jsonb, 8, 549.00),
  ($$Xbox Series S$$,         $$Compact next-gen console, digital only.$$ , $${"brand":"Microsoft","storage":"512GB"}$$::jsonb, 12, 299.00),
  ($$Nintendo Switch OLED$$,  $$Brighter OLED portable play.$$         , $${"brand":"Nintendo","storage":"64GB"}$$::jsonb, 10, 349.00),
  ($$Joy-Con Pair$$,          $$Wireless Joy-Con controllers.$$        , $${"brand":"Nintendo"}$$::jsonb, 25, 69.00),
  ($$DualSense Controller$$,  $$PS5 controller with haptics.$$         , $${"brand":"Sony"}$$::jsonb, 20, 69.00),
  ($$Xbox Wireless Controller$$, $$Refined grip and share button.$$    , $${"brand":"Microsoft"}$$::jsonb, 22, 64.00),
  ($$microSD 1TB$$,           $$Expand portable storage.$$             , $${"brand":"MemX","speed":"A2 V30"}$$::jsonb, 30, 139.00),
  ($$Charging Station$$,      $$Charge two controllers at once.$$      , $${"brand":"PowerDock"}$$::jsonb, 35, 24.90),
  ($$Console Headset$$,       $$Clear chat and comfy earcups.$$        , $${"brand":"SoundCore","conn":"3.5mm"}$$::jsonb, 28, 39.90),
  ($$Retro Mini Console$$,    $$Preloaded classics in mini form.$$     , $${"brand":"RetroBox"}$$::jsonb, 14, 59.90);

-- ========== books ==========
INSERT INTO products (product_name, product_description, product_details, product_available_stock, product_price)
VALUES
  ($$Novel Bestseller$$,       $$A gripping tale that you won't put down.$$ , $${"brand":"PaperTrail","format":"paperback"}$$::jsonb, 40, 12.99),
  ($$Sci-Fi Classic$$,         $$Timeless sci-fi that defined a genre.$$    , $${"brand":"PaperTrail","format":"paperback"}$$::jsonb, 35, 10.99),
  ($$Fantasy Epic$$,           $$Vast worldbuilding and unforgettable heroes.$$ , $${"brand":"PaperTrail","format":"paperback"}$$::jsonb, 30, 14.99),
  ($$History Spotlight$$,      $$Deep dive into pivotal events.$$           , $${"brand":"PaperTrail","format":"paperback"}$$::jsonb, 25, 13.99),
  ($$Self-Help Guide$$,        $$Practical steps for daily improvements.$$  , $${"brand":"PaperTrail","format":"paperback"}$$::jsonb, 45, 11.99),
  ($$Manga Vol. 1$$,           $$Beloved series begins here.$$              , $${"brand":"PaperTrail","format":"tankobon"}$$::jsonb, 50, 8.99),
  ($$Graphic Novel$$,          $$Bold art meets powerful story.$$           , $${"brand":"PaperTrail","format":"trade"}$$::jsonb, 28, 15.99),
  ($$Cookbook$$,               $$Tasty recipes, easy techniques.$$          , $${"brand":"PaperTrail","format":"hardcover"}$$::jsonb, 20, 19.99),
  ($$Children's Storybook$$,   $$Whimsical bedtime adventure.$$             , $${"brand":"PaperTrail","format":"hardcover"}$$::jsonb, 25, 9.99),
  ($$Mystery Thriller$$,       $$A twisty page-turner until the end.$$      , $${"brand":"PaperTrail","format":"paperback"}$$::jsonb, 32, 12.49);

-- ========== DIY & Tools ==========
INSERT INTO products (product_name, product_description, product_details, product_available_stock, product_price)
VALUES
  ($$Cordless Drill 18V$$,  $$Versatile drill with 2 batteries and case.$$ , $${"brand":"ToolForge","voltage":"18V","batteries":2}$$::jsonb, 22, 89.00),
  ($$Impact Driver$$,       $$High torque for heavy screws and bolts.$$    , $${"brand":"ToolForge","voltage":"18V"}$$::jsonb, 18, 79.00),
  ($$Jigsaw$$,              $$Smooth curves with variable speed.$$         , $${"brand":"ToolForge","blade":"T-shank"}$$::jsonb, 16, 59.00),
  ($$Circular Saw$$,        $$Clean rip cuts with safety guard.$$          , $${"brand":"ToolForge","blade":"185mm"}$$::jsonb, 14, 79.00),
  ($$Tool Set 100pcs$$,     $$All the basics in a durable case.$$          , $${"brand":"ToolForge","pieces":100}$$::jsonb, 25, 49.00),
  ($$Fiberglass Hammer$$,   $$Balanced 16oz hammer, anti-shock.$$          , $${"brand":"ToolForge","weight":"16oz"}$$::jsonb, 40, 14.90),
  ($$Screwdriver Set$$,     $$Precision and standard bits.$$               , $${"brand":"ToolForge","pieces":32}$$::jsonb, 35, 19.90),
  ($$Tape Measure 5m$$,     $$Auto-lock tape with belt clip.$$             , $${"brand":"ToolForge","length":"5m"}$$::jsonb, 60, 6.90),
  ($$Glue Gun$$,            $$Fast-heat glue gun for crafts/repairs.$$     , $${"brand":"ToolForge","sticks":"7mm"}$$::jsonb, 30, 9.90),
  ($$Safety Glasses$$,      $$Clear polycarbonate, anti-fog.$$             , $${"brand":"ToolForge","standard":"EN166"}$$::jsonb, 55, 4.90);

COMMIT;

-- Quick check (optional)
-- SELECT category_id, category_name FROM categories ORDER BY category_id;
-- TRUNCATE TABLE categories RESTART IDENTITY CASCADE;

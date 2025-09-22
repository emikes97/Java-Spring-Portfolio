BEGIN;
INSERT INTO customers
  (customer_id, phone_number, email, username, password_hash, name, surname)
VALUES
  ('1dfbf8d3-419d-5beb-8c07-0c28fabee34b', '+306900000001', 'load+01@example.com', 'user01', crypt('1234!', gen_salt('bf', 10)), 'Name01', 'Surname01'),
  ('359e7038-7f63-5f0c-97a5-c024defa2c3d', '+306900000002', 'load+02@example.com', 'user02', crypt('1234!', gen_salt('bf', 10)), 'Name02', 'Surname02'),
  ('d6277657-f105-50e9-b59f-81b370626aa6', '+306900000003', 'load+03@example.com', 'user03', crypt('1234!', gen_salt('bf', 10)), 'Name03', 'Surname03'),
  ('8bb5dbc9-3fb1-5d7f-8c9d-2279c5761467', '+306900000004', 'load+04@example.com', 'user04', crypt('1234!', gen_salt('bf', 10)), 'Name04', 'Surname04'),
  ('00251527-2f24-56c8-ae55-4540d7873bd7', '+306900000005', 'load+05@example.com', 'user05', crypt('1234!', gen_salt('bf', 10)), 'Name05', 'Surname05'),
  ('ac259b3c-bf38-5b30-a201-eb35a46064fd', '+306900000006', 'load+06@example.com', 'user06', crypt('1234!', gen_salt('bf', 10)), 'Name06', 'Surname06'),
  ('1bd28b79-182d-56ca-b6b3-743b1b610404', '+306900000007', 'load+07@example.com', 'user07', crypt('1234!', gen_salt('bf', 10)), 'Name07', 'Surname07'),
  ('b38b13e3-f599-59d6-9efc-bc3c7084ae41', '+306900000008', 'load+08@example.com', 'user08', crypt('1234!', gen_salt('bf', 10)), 'Name08', 'Surname08'),
  ('9892810f-9448-516d-b184-60163d511d6c', '+306900000009', 'load+09@example.com', 'user09', crypt('1234!', gen_salt('bf', 10)), 'Name09', 'Surname09'),
  ('ef93248f-9e98-5475-92a0-8f6e2082f978', '+306900000010', 'load+10@example.com', 'user10', crypt('1234!', gen_salt('bf', 10)), 'Name10', 'Surname10'),
  ('a84ffe3f-10c3-53f4-8eb7-947f78900ed0', '+306900000011', 'load+11@example.com', 'user11', crypt('1234!', gen_salt('bf', 10)), 'Name11', 'Surname11'),
  ('d144cb9a-01e7-55de-90ca-eaa7c09aab68', '+306900000012', 'load+12@example.com', 'user12', crypt('1234!', gen_salt('bf', 10)), 'Name12', 'Surname12'),
  ('a61ea36e-f736-586e-9672-c11847c8aa75', '+306900000013', 'load+13@example.com', 'user13', crypt('1234!', gen_salt('bf', 10)), 'Name13', 'Surname13'),
  ('fc0216cd-45cf-51bc-9a88-440bf499bd87', '+306900000014', 'load+14@example.com', 'user14', crypt('1234!', gen_salt('bf', 10)), 'Name14', 'Surname14'),
  ('a1075f31-73d0-5d88-a985-16ecebe77c74', '+306900000015', 'load+15@example.com', 'user15', crypt('1234!', gen_salt('bf', 10)), 'Name15', 'Surname15'),
  ('a65463f5-6488-5ad5-aefc-2940e9d913a0', '+306900000016', 'load+16@example.com', 'user16', crypt('1234!', gen_salt('bf', 10)), 'Name16', 'Surname16'),
  ('8d154520-e2b9-5530-ba50-14489207c4cf', '+306900000017', 'load+17@example.com', 'user17', crypt('1234!', gen_salt('bf', 10)), 'Name17', 'Surname17'),
  ('73bab0df-1d56-557e-840c-9dfb9ef7ecac', '+306900000018', 'load+18@example.com', 'user18', crypt('1234!', gen_salt('bf', 10)), 'Name18', 'Surname18'),
  ('6a27c30a-fbb7-5907-a410-37299006b3d9', '+306900000019', 'load+19@example.com', 'user19', crypt('1234!', gen_salt('bf', 10)), 'Name19', 'Surname19'),
  ('c0f91d4c-2fe8-5b6e-bd0c-917e673aabab', '+306900000020', 'load+20@example.com', 'user20', crypt('1234!', gen_salt('bf', 10)), 'Name20', 'Surname20')
ON CONFLICT DO NOTHING;
COMMIT;

BEGIN;

-- Carts for the 20 customers
INSERT INTO cart (cart_id, customer_id)
VALUES
  ('11111111-1111-4111-8111-000000000001','1dfbf8d3-419d-5beb-8c07-0c28fabee34b'),
  ('11111111-1111-4111-8111-000000000002','359e7038-7f63-5f0c-97a5-c024defa2c3d'),
  ('11111111-1111-4111-8111-000000000003','d6277657-f105-50e9-b59f-81b370626aa6'),
  ('11111111-1111-4111-8111-000000000004','8bb5dbc9-3fb1-5d7f-8c9d-2279c5761467'),
  ('11111111-1111-4111-8111-000000000005','00251527-2f24-56c8-ae55-4540d7873bd7'),
  ('11111111-1111-4111-8111-000000000006','ac259b3c-bf38-5b30-a201-eb35a46064fd'),
  ('11111111-1111-4111-8111-000000000007','1bd28b79-182d-56ca-b6b3-743b1b610404'),
  ('11111111-1111-4111-8111-000000000008','b38b13e3-f599-59d6-9efc-bc3c7084ae41'),
  ('11111111-1111-4111-8111-000000000009','9892810f-9448-516d-b184-60163d511d6c'),
  ('11111111-1111-4111-8111-000000000010','ef93248f-9e98-5475-92a0-8f6e2082f978'),
  ('11111111-1111-4111-8111-000000000011','a84ffe3f-10c3-53f4-8eb7-947f78900ed0'),
  ('11111111-1111-4111-8111-000000000012','d144cb9a-01e7-55de-90ca-eaa7c09aab68'),
  ('11111111-1111-4111-8111-000000000013','a61ea36e-f736-586e-9672-c11847c8aa75'),
  ('11111111-1111-4111-8111-000000000014','fc0216cd-45cf-51bc-9a88-440bf499bd87'),
  ('11111111-1111-4111-8111-000000000015','a1075f31-73d0-5d88-a985-16ecebe77c74'),
  ('11111111-1111-4111-8111-000000000016','a65463f5-6488-5ad5-aefc-2940e9d913a0'),
  ('11111111-1111-4111-8111-000000000017','8d154520-e2b9-5530-ba50-14489207c4cf'),
  ('11111111-1111-4111-8111-000000000018','73bab0df-1d56-557e-840c-9dfb9ef7ecac'),
  ('11111111-1111-4111-8111-000000000019','6a27c30a-fbb7-5907-a410-37299006b3d9'),
  ('11111111-1111-4111-8111-000000000020','c0f91d4c-2fe8-5b6e-bd0c-917e673aabab')
ON CONFLICT (customer_id) DO NOTHING;

-- Wishlists for the same 20 customers
INSERT INTO wishlist (wishlist_id, customer_id)
VALUES
  ('22222222-2222-4222-8222-000000000001','1dfbf8d3-419d-5beb-8c07-0c28fabee34b'),
  ('22222222-2222-4222-8222-000000000002','359e7038-7f63-5f0c-97a5-c024defa2c3d'),
  ('22222222-2222-4222-8222-000000000003','d6277657-f105-50e9-b59f-81b370626aa6'),
  ('22222222-2222-4222-8222-000000000004','8bb5dbc9-3fb1-5d7f-8c9d-2279c5761467'),
  ('22222222-2222-4222-8222-000000000005','00251527-2f24-56c8-ae55-4540d7873bd7'),
  ('22222222-2222-4222-8222-000000000006','ac259b3c-bf38-5b30-a201-eb35a46064fd'),
  ('22222222-2222-4222-8222-000000000007','1bd28b79-182d-56ca-b6b3-743b1b610404'),
  ('22222222-2222-4222-8222-000000000008','b38b13e3-f599-59d6-9efc-bc3c7084ae41'),
  ('22222222-2222-4222-8222-000000000009','9892810f-9448-516d-b184-60163d511d6c'),
  ('22222222-2222-4222-8222-000000000010','ef93248f-9e98-5475-92a0-8f6e2082f978'),
  ('22222222-2222-4222-8222-000000000011','a84ffe3f-10c3-53f4-8eb7-947f78900ed0'),
  ('22222222-2222-4222-8222-000000000012','d144cb9a-01e7-55de-90ca-eaa7c09aab68'),
  ('22222222-2222-4222-8222-000000000013','a61ea36e-f736-586e-9672-c11847c8aa75'),
  ('22222222-2222-4222-8222-000000000014','fc0216cd-45cf-51bc-9a88-440bf499bd87'),
  ('22222222-2222-4222-8222-000000000015','a1075f31-73d0-5d88-a985-16ecebe77c74'),
  ('22222222-2222-4222-8222-000000000016','a65463f5-6488-5ad5-aefc-2940e9d913a0'),
  ('22222222-2222-4222-8222-000000000017','8d154520-e2b9-5530-ba50-14489207c4cf'),
  ('22222222-2222-4222-8222-000000000018','73bab0df-1d56-557e-840c-9dfb9ef7ecac'),
  ('22222222-2222-4222-8222-000000000019','6a27c30a-fbb7-5907-a410-37299006b3d9'),
  ('22222222-2222-4222-8222-000000000020','c0f91d4c-2fe8-5b6e-bd0c-917e673aabab')
ON CONFLICT (customer_id) DO NOTHING;

COMMIT;


---Test case---
SELECT
  (SELECT count(*) FROM cart)      AS carts,
  (SELECT count(*) FROM wishlist)  AS wishlists,
  (SELECT count(*) FROM customers) AS customers;

-- Ensure 1:1 mapping and no duplicates
SELECT customer_id, count(*) FROM cart GROUP BY customer_id HAVING count(*) > 1;
SELECT customer_id, count(*) FROM wishlist GROUP BY customer_id HAVING count(*) > 1;


-- Should all return true
SELECT email, password_hash = crypt('1234!', password_hash) AS ok
FROM customers
WHERE email LIKE 'load+%';
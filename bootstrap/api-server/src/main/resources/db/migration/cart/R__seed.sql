-- Cart seed data (dev/staging)
TRUNCATE TABLE cart_items CASCADE;
TRUNCATE TABLE carts CASCADE;

INSERT INTO carts (id, member_id, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 5, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 6, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

SELECT setval('carts_id_seq', 100, false);

INSERT INTO cart_items (id, cart_id, wishlist_item_id, amount, wishlist_item_status)
VALUES (1, 2, 9, 539100.00, 'IN_PROGRESS');

SELECT setval('cart_items_id_seq', 100, false);

-- =============================================================================
-- V1.2.1: Seed - Wallets, Carts
-- =============================================================================

INSERT INTO wallet (id, member_id, balance, version, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 100000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, 118000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, 1000000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, 0.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 5, 300000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 6, 62000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

SELECT setval('wallet_id_seq', (SELECT MAX(id) FROM wallet));

INSERT INTO cart (id, member_id, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 5, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 6, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

SELECT setval('cart_id_seq', (SELECT MAX(id) FROM cart));

-- Wallet seed data (dev/staging)
TRUNCATE TABLE wallet_histories CASCADE;
TRUNCATE TABLE wallets CASCADE;

INSERT INTO wallets (id, member_id, balance, version, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 100000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, 50000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, 1000000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, 0.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 5, 250000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 6, 180000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

SELECT setval('wallets_id_seq', 100, false);

INSERT INTO wallet_histories (id, wallet_id, transaction_type, amount, balance_after,
                            reference_type, reference_id, occurred_at,
                            created_at, updated_at, created_by, updated_by)
VALUES (1, 2, 'CHARGE', 500000.00, 500000.00, 'CHARGE', 'CHG-20260205-001', '2026-02-04 10:00:00',
        '2026-02-04 10:00:00', '2026-02-04 10:00:00', 'SYSTEM', 'SYSTEM'),
       (2, 2, 'ORDER_DEDUCT', 359000.00, 141000.00, 'PAYMENT', 'ORD-20260205-A1B2C3D4E5F6-20260205170000',
        '2026-02-05 17:05:00',
        '2026-02-05 17:05:00', '2026-02-05 17:05:00', 'SYSTEM', 'SYSTEM'),
       (3, 2, 'ORDER_DEDUCT', 23000.00, 118000.00, 'PAYMENT', 'ORD-20260205-B2C3D4E5F6G7-20260205171000',
        '2026-02-05 17:15:00',
        '2026-02-05 17:15:00', '2026-02-05 17:15:00', 'SYSTEM', 'SYSTEM'),
       (4, 5, 'CHARGE', 300000.00, 300000.00, 'CHARGE', 'CHG-20260207-001', '2026-02-07 09:00:00',
        '2026-02-07 09:00:00', '2026-02-07 09:00:00', 'SYSTEM', 'SYSTEM'),
       (5, 6, 'CHARGE', 500000.00, 500000.00, 'CHARGE', 'CHG-20260208-001', '2026-02-08 10:00:00',
        '2026-02-08 10:00:00', '2026-02-08 10:00:00', 'SYSTEM', 'SYSTEM');

SELECT setval('wallet_histories_id_seq', 100, false);

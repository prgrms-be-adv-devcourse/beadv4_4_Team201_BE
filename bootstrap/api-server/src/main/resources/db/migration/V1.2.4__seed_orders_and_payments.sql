-- =============================================================================
-- V1.2.4: Seed - Orders, Order Items, Payments, Payment History, Wallet History
-- =============================================================================

-- Orders
INSERT INTO order_v2 (id, buyer_id, order_number, total_amount, quantity, payment_method, status,
                      payment_key, last_transaction_key, paid_at, confirmed_at, cancelled_at,
                      created_at, updated_at)
VALUES
    (1, 2, 'ORD-20260205-A1B2C3D4E5F6-20260205170000', 359000.00, 1, 'CARD', 'PAID',
     'toss_pk_20260205_0001', 'toss_tx_20260205_0001', '2026-02-05 17:05:00', NULL, NULL,
     '2026-02-05 17:00:00', '2026-02-05 17:05:00'),

    (2, 2, 'ORD-20260205-B2C3D4E5F6G7-20260205171000', 23000.00, 1, 'CARD', 'CONFIRMED',
     'toss_pk_20260205_0002', 'toss_tx_20260205_0002', '2026-02-05 17:15:00', '2026-02-06 10:00:00', NULL,
     '2026-02-05 17:10:00', '2026-02-06 10:00:00'),

    (3, 5, 'ORD-20260207-C3D4E5F6G7H8-20260207120000', 415000.00, 1, 'CARD', 'CREATED',
     NULL, NULL, NULL, NULL, NULL,
     '2026-02-07 12:00:00', '2026-02-07 12:00:00'),

    (4, 6, 'ORD-20260208-D4E5F6G7H8I9-20260208140000', 448000.00, 2, 'KAKAO_PAY', 'PAID',
     'toss_pk_20260208_0004', 'toss_tx_20260208_0004', '2026-02-08 14:05:00', NULL, NULL,
     '2026-02-08 14:00:00', '2026-02-08 14:05:00'),

    (5, 5, 'ORD-20260208-E5F6G7H8I9J0-20260208150000', 89000.00, 1, 'CARD', 'CANCELED',
     'toss_pk_20260208_0005', 'toss_tx_20260208_0005', '2026-02-08 15:05:00', NULL, '2026-02-08 16:00:00',
     '2026-02-08 15:00:00', '2026-02-08 16:00:00');

SELECT setval('order_v2_id_seq', (SELECT MAX(id) FROM order_v2));

-- Order Items
INSERT INTO order_item_v2 (id, order_id, target_id, target_type, order_item_type, seller_id, receiver_id,
                           price, amount, status, cancelled_at, created_at, updated_at)
VALUES
    (1, 1, 1, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 2,
     359000.00, 359000.00, 'PAID', NULL, '2026-02-05 17:00:00', '2026-02-05 17:05:00'),

    (2, 2, 2, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 2,
     23000.00, 23000.00, 'PAID', NULL, '2026-02-05 17:10:00', '2026-02-06 10:00:00'),

    (3, 3, 3, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 5,
     415000.00, 415000.00, 'CREATED', NULL, '2026-02-07 12:00:00', '2026-02-07 12:00:00'),

    (4, 4, 2, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 6,
     23000.00, 23000.00, 'PAID', NULL, '2026-02-08 14:00:00', '2026-02-08 14:05:00'),
    (5, 4, 3, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 6,
     415000.00, 415000.00, 'PAID', NULL, '2026-02-08 14:00:00', '2026-02-08 14:05:00'),

    (6, 5, 5, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 5,
     89000.00, 89000.00, 'CANCELLED', '2026-02-08 16:00:00', '2026-02-08 15:00:00', '2026-02-08 16:00:00');

SELECT setval('order_item_v2_id_seq', (SELECT MAX(id) FROM order_item_v2));

-- Payments
INSERT INTO payment (id, idempotency_key, type, method, order_id, member_id,
                     origin_amount, paid_amount, order_items_json, status,
                     payment_key, last_transaction_key, approve_code, paid_at,
                     created_at, updated_at, created_by, updated_by)
VALUES
    (1, 'idem-20260205-0001', 'FUNDING', 'CARD',
     'ORD-20260205-A1B2C3D4E5F6-20260205170000', 2,
     359000.00, 359000.00, '[{"targetId":1,"amount":359000}]', 'PAID',
     'toss_pk_20260205_0001', 'toss_tx_20260205_0001', 'approve_001', '2026-02-05 17:05:00',
     '2026-02-05 17:00:00', '2026-02-05 17:05:00', 'SYSTEM', 'SYSTEM'),

    (2, 'idem-20260205-0002', 'FUNDING', 'CARD',
     'ORD-20260205-B2C3D4E5F6G7-20260205171000', 2,
     23000.00, 23000.00, '[{"targetId":2,"amount":23000}]', 'PAID',
     'toss_pk_20260205_0002', 'toss_tx_20260205_0002', 'approve_002', '2026-02-05 17:15:00',
     '2026-02-05 17:10:00', '2026-02-05 17:15:00', 'SYSTEM', 'SYSTEM'),

    (3, 'idem-20260208-0004', 'FUNDING', 'KAKAO_PAY',
     'ORD-20260208-D4E5F6G7H8I9-20260208140000', 6,
     448000.00, 448000.00, '[{"targetId":2,"amount":23000},{"targetId":3,"amount":415000}]', 'PAID',
     'toss_pk_20260208_0004', 'toss_tx_20260208_0004', 'approve_004', '2026-02-08 14:05:00',
     '2026-02-08 14:00:00', '2026-02-08 14:05:00', 'SYSTEM', 'SYSTEM');

SELECT setval('payment_id_seq', (SELECT MAX(id) FROM payment));

-- Payment History
INSERT INTO payment_history (id, payment_id, idempotency_key, event_type, occurred_at, metadata,
                             created_at, updated_at, created_by, updated_by)
VALUES
    (1, 1, 'idem-20260205-0001-created', 'CREATED', '2026-02-05 17:00:00', NULL,
     '2026-02-05 17:00:00', '2026-02-05 17:00:00', 'SYSTEM', 'SYSTEM'),
    (2, 1, 'idem-20260205-0001-paid', 'PAID', '2026-02-05 17:05:00', NULL,
     '2026-02-05 17:05:00', '2026-02-05 17:05:00', 'SYSTEM', 'SYSTEM'),
    (3, 2, 'idem-20260205-0002-created', 'CREATED', '2026-02-05 17:10:00', NULL,
     '2026-02-05 17:10:00', '2026-02-05 17:10:00', 'SYSTEM', 'SYSTEM'),
    (4, 2, 'idem-20260205-0002-paid', 'PAID', '2026-02-05 17:15:00', NULL,
     '2026-02-05 17:15:00', '2026-02-05 17:15:00', 'SYSTEM', 'SYSTEM'),
    (5, 3, 'idem-20260208-0004-paid', 'PAID', '2026-02-08 14:05:00', NULL,
     '2026-02-08 14:05:00', '2026-02-08 14:05:00', 'SYSTEM', 'SYSTEM');

SELECT setval('payment_history_id_seq', (SELECT MAX(id) FROM payment_history));

-- Wallet History
INSERT INTO wallet_history (id, wallet_id, transaction_type, amount, balance_after,
                            reference_type, reference_id, occurred_at,
                            created_at, updated_at, created_by, updated_by)
VALUES
    (1, 2, 'CHARGE', 500000.00, 500000.00, 'CHARGE', 'CHG-20260205-001', '2026-02-04 10:00:00',
     '2026-02-04 10:00:00', '2026-02-04 10:00:00', 'SYSTEM', 'SYSTEM'),
    (2, 2, 'PAYMENT', 359000.00, 141000.00, 'PAYMENT', 'ORD-20260205-A1B2C3D4E5F6-20260205170000', '2026-02-05 17:05:00',
     '2026-02-05 17:05:00', '2026-02-05 17:05:00', 'SYSTEM', 'SYSTEM'),
    (3, 2, 'PAYMENT', 23000.00, 118000.00, 'PAYMENT', 'ORD-20260205-B2C3D4E5F6G7-20260205171000', '2026-02-05 17:15:00',
     '2026-02-05 17:15:00', '2026-02-05 17:15:00', 'SYSTEM', 'SYSTEM'),
    (4, 5, 'CHARGE', 300000.00, 300000.00, 'CHARGE', 'CHG-20260207-001', '2026-02-07 09:00:00',
     '2026-02-07 09:00:00', '2026-02-07 09:00:00', 'SYSTEM', 'SYSTEM'),
    (5, 6, 'CHARGE', 500000.00, 500000.00, 'CHARGE', 'CHG-20260208-001', '2026-02-08 10:00:00',
     '2026-02-08 10:00:00', '2026-02-08 10:00:00', 'SYSTEM', 'SYSTEM');

SELECT setval('wallet_history_id_seq', (SELECT MAX(id) FROM wallet_history));

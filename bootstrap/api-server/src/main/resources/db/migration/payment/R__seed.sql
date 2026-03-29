-- Payment seed data (dev/staging)
TRUNCATE TABLE payment_histories CASCADE;
TRUNCATE TABLE payment_cancels CASCADE;
TRUNCATE TABLE payments CASCADE;

INSERT INTO payments (id, type, method, order_id, order_number, member_id,
                     origin_amount, paid_amount, refunded_amount, wallet_deducted_amount, order_items_json, status,
                     payment_key, last_transaction_key, approve_code, paid_at,
                     created_at, updated_at, created_by, updated_by)
VALUES (1, 'FUNDING', 'CARD',
        1, 'ORD-20260205-A1B2C3D4E5F6-20260205170000', 2,
        359000.00, 359000.00, 0.00, 0.00, '[{"targetId":1, "amount":{"amount":359000}, "sellerId":3}]', 'PAID',
        'toss_pk_20260205_0001', 'toss_tx_20260205_0001', 'approve_001', '2026-02-05 17:05:00',
        '2026-02-05 17:00:00', '2026-02-05 17:05:00', 'SYSTEM', 'SYSTEM'),

       (2, 'FUNDING', 'CARD',
        2, 'ORD-20260205-B2C3D4E5F6G7-20260205171000', 2,
        23000.00, 23000.00, 0.00, 0.00, '[{"targetId":2, "amount":{"amount":23000}, "sellerId":3}]', 'PAID',
        'toss_pk_20260205_0002', 'toss_tx_20260205_0002', 'approve_002', '2026-02-05 17:15:00',
        '2026-02-05 17:10:00', '2026-02-05 17:15:00', 'SYSTEM', 'SYSTEM'),

       (3, 'FUNDING', 'KAKAO_PAY',
        3, 'ORD-20260208-D4E5F6G7H8I9-20260208140000', 6,
        448000.00, 448000.00, 0.00, 0.00,
        '[{"targetId":2, "amount":{"amount":23000}, "sellerId":3}, {"targetId":3, "amount":{"amount":415000}, "sellerId":3}]',
        'PAID',
        'toss_pk_20260208_0004', 'toss_tx_20260208_0004', 'approve_004', '2026-02-08 14:05:00',
        '2026-02-08 14:00:00', '2026-02-08 14:05:00', 'SYSTEM', 'SYSTEM');

SELECT setval('payments_id_seq', 100, false);

INSERT INTO payment_histories (id, payment_id, history_key, event_type, occurred_at, metadata,
                             created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 'idem-20260205-0001-created', 'CREATED', '2026-02-05 17:00:00', NULL,
        '2026-02-05 17:00:00', '2026-02-05 17:00:00', 'SYSTEM', 'SYSTEM'),
       (2, 1, 'idem-20260205-0001-paid', 'PAID', '2026-02-05 17:05:00', NULL,
        '2026-02-05 17:05:00', '2026-02-05 17:05:00', 'SYSTEM', 'SYSTEM'),
       (3, 2, 'idem-20260205-0002-created', 'CREATED', '2026-02-05 17:10:00', NULL,
        '2026-02-05 17:10:00', '2026-02-05 17:10:00', 'SYSTEM', 'SYSTEM'),
       (4, 2, 'idem-20260205-0002-paid', 'PAID', '2026-02-05 17:15:00', NULL,
        '2026-02-05 17:15:00', '2026-02-05 17:15:00', 'SYSTEM', 'SYSTEM'),
       (5, 3, 'idem-20260208-0004-paid', 'PAID', '2026-02-08 14:05:00', NULL,
        '2026-02-08 14:05:00', '2026-02-08 14:05:00', 'SYSTEM', 'SYSTEM');

SELECT setval('payment_histories_id_seq', 100, false);

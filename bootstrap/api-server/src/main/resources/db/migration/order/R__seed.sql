-- Order seed data (dev/staging)
TRUNCATE TABLE order_items CASCADE;
TRUNCATE TABLE orders CASCADE;
TRUNCATE TABLE core_member_replicas CASCADE;

INSERT INTO core_member_replicas (id, nickname)
VALUES (1, '멍청한돼지0009'),
       (2, '나른한고양이0013'),
       (3, '멍청한고양이2013'),
       (4, '관리자'),
       (5, '졸린토끼0042'),
       (6, '배고픈강아지0007');

INSERT INTO orders (id, buyer_id, order_number, total_amount, quantity, payment_method, status,
                    payment_id, origin_transaction_key, paid_at, confirmed_at, cancelled_at,
                    created_at, updated_at)
VALUES (1, 2, 'ORD-20260205-A1B2C3D4E5F6-20260205170000', 359000.00, 1, 'CARD', 'PAID',
        1, 'toss_tx_20260205_0001', '2026-02-05 17:05:00', NULL, NULL,
        '2026-02-05 17:00:00', '2026-02-05 17:05:00'),

       (2, 2, 'ORD-20260205-B2C3D4E5F6G7-20260205171000', 23000.00, 1, 'CARD', 'CONFIRMED',
        2, 'toss_tx_20260205_0002', '2026-02-05 17:15:00', '2026-02-06 10:00:00', NULL,
        '2026-02-05 17:10:00', '2026-02-06 10:00:00'),

       (3, 5, 'ORD-20260207-C3D4E5F6G7H8-20260207120000', 415000.00, 1, 'CARD', 'CREATED',
        NULL, NULL, NULL, NULL, NULL,
        '2026-02-07 12:00:00', '2026-02-07 12:00:00'),

       (4, 6, 'ORD-20260208-D4E5F6G7H8I9-20260208140000', 448000.00, 2, 'KAKAO_PAY', 'PAID',
        NULL, NULL, NULL, NULL, NULL,
        '2026-02-08 14:00:00', '2026-02-08 14:05:00'),

       (5, 5, 'ORD-20260208-E5F6G7H8I9J0-20260208150000', 89000.00, 1, 'CARD', 'CANCELED',
        NULL, NULL, NULL, NULL, '2026-02-08 16:00:00',
        '2026-02-08 15:00:00', '2026-02-08 16:00:00');

SELECT setval('orders_id_seq', 100, false);

INSERT INTO order_items (id, order_id, target_id, target_type, order_item_type, seller_id, receiver_id,
                         price, amount, status, cancelled_at, created_at, updated_at)
VALUES (1, 1, 1, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 2,
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
        89000.00, 89000.00, 'CANCELED', '2026-02-08 16:00:00', '2026-02-08 15:00:00', '2026-02-08 16:00:00');

SELECT setval('order_items_id_seq', 100, false);

-- Loadtest: core_member_replicas for BUYER + SELLER accounts
DO $$
DECLARE
  _id bigint;
BEGIN
  IF current_schema() = 'loadtest' THEN
    -- Givers (1001-1050)
    FOR _id IN 1001..1050 LOOP
      INSERT INTO core_member_replicas (id, nickname)
      VALUES (_id, 'loadtester' || LPAD((_id - 1000)::text, 3, '0'))
      ON CONFLICT (id) DO NOTHING;
    END LOOP;

    -- Receivers (1051-1060)
    FOR _id IN 1051..1060 LOOP
      INSERT INTO core_member_replicas (id, nickname)
      VALUES (_id, 'loadtester' || LPAD((_id - 1000)::text, 3, '0'))
      ON CONFLICT (id) DO NOTHING;
    END LOOP;

    -- Sellers (1101-1110)
    FOR _id IN 1101..1110 LOOP
      INSERT INTO core_member_replicas (id, nickname)
      VALUES (_id, 'seller' || LPAD((_id - 1100)::text, 3, '0'))
      ON CONFLICT (id) DO NOTHING;
    END LOOP;
  END IF;
END
$$;

-- -----------------------------------------------------------------------------
-- 1. MEMBERS - 총 10 명
-- SELLER: 1 명
-- BUYER: 9 명
-- -----------------------------------------------------------------------------
INSERT INTO members (id, email, nickname, birthday, role, address, phone_num, name, status, auth_sub,
                     created_at, updated_at, created_by, updated_by)
VALUES (1, 'qa-seller-giftify@team201.dev', '멍청한돼지0009', '1975-11-08', 'SELLER', '서울시 종로구',
        '010-1234-5678', '김주영', 'ACTIVE', 'auth0|6981838d48f8397cae06ddb0', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (2, 'qa-buyer2-giftify@team201.dev', '졸린토끼0042', '1998-07-22', 'BUYER', '서울시 마포구',
    '010-3333-4444', '이수현', 'ACTIVE', 'auth0|698184500000000000000001', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (3, 'qa-seller-03-giftify@team201.dev', '용감한여우0017', '1990-03-15', 'BUYER', '서울시 강남구',
    '010-1001-0003', '박민준', 'ACTIVE', 'auth0|6981838d48f8397cae06dd03', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (4, 'qa-seller-04-giftify@team201.dev', '빠른독수리0031', '1985-09-27', 'BUYER', '서울시 송파구',
    '010-1001-0004', '최지은', 'ACTIVE', 'auth0|6981838d48f8397cae06dd04', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (5, 'qa-seller-05-giftify@team201.dev', '조용한고양이0055', '1992-12-03', 'BUYER', '서울시 용산구',
    '010-1001-0005', '정해린', 'ACTIVE', 'auth0|6981838d48f8397cae06dd05', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (6, 'qa-seller-06-giftify@team201.dev', '날쌘토끼0088', '1980-06-19', 'BUYER', '서울시 서초구',
    '010-1001-0006', '한동훈', 'ACTIVE', 'auth0|6981838d48f8397cae06dd06', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (7, 'qa-seller-07-giftify@team201.dev', '배고픈곰0023', '1995-01-30', 'BUYER', '서울시 광진구',
    '010-1001-0007', '오세진', 'ACTIVE', 'auth0|6981838d48f8397cae06dd07', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (8, 'qa-seller-08-giftify@team201.dev', '신비한고래0066', '1988-08-11', 'BUYER', '서울시 동작구',
    '010-1001-0008', '윤아름', 'ACTIVE', 'auth0|6981838d48f8397cae06dd08', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (9, 'qa-seller-09-giftify@team201.dev', '느린거북이0044', '1993-04-25', 'BUYER', '서울시 은평구',
    '010-1001-0009', '임태양', 'ACTIVE', 'auth0|6981838d48f8397cae06dd09', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (10, 'qa-seller-10-giftify@team201.dev', '영리한여우0077', '1982-10-07', 'BUYER', '서울시 노원구',
    '010-1001-0010', '강서연', 'ACTIVE', 'auth0|6981838d48f8397cae06dd10', NOW(), NOW(), 'SYSTEM', 'SYSTEM');


-- -----------------------------------------------------------------------------
-- 2. PRODUCT (상품) — 총 1 개
-- seller_id: 1 번 (SELLER)
-- stock: 50 개
-- -----------------------------------------------------------------------------
INSERT INTO products (id, seller_id, name, description, price, stock, status, image_key, category,
                      created_at, updated_at, created_by, updated_by)
VALUES (1, 1, '소니 WH-1000XM5', '압도적인 정적, 최고의 노이즈 캔슬링 헤드폰', 449000, 50, 'ACTIVE',
        'products/11/sony-xm5.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

-- -----------------------------------------------------------------------------
-- 3. WISHLIST (위시리스트) — 총 1 개
-- member_id: 2 번
-- -----------------------------------------------------------------------------
INSERT INTO wishlists (id, member_id, visibility, created_at, updated_at, created_by, updated_by)
VALUES (1, 2, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

-- -----------------------------------------------------------------------------
-- 4. WISHLIST_ITEM (위시리스트 아이템) — 총 1 개
-- wishlist_id: 1 번
-- product_id: 1 번
-- wishlist_item_status: COMPLETED (펀딩 수령)
-- -----------------------------------------------------------------------------
INSERT INTO wishlist_items (id, wishlist_id, product_id, wishlist_item_status, added_at, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 1, 'COMPLETED', '2026-02-27 04:38:07.137213', '2026-02-27 04:38:07.137213', '2026-02-27 05:02:52.542501',
        NULL, NULL);

-- -----------------------------------------------------------------------------
-- 5. FUNDING (펀딩) — 총 1 개
-- wishlist_item_id: 1
-- product_id: 1
-- status: ACCEPTED (펀딩 수령)
-- -----------------------------------------------------------------------------
INSERT INTO fundings (
    id, version, wishlist_item_id, product_id, product_name, image_key, receiver_id,
    target_amount, current_amount, status,
    deadline, achieved_at, closed_at,
    created_at, updated_at, created_by, updated_by
)
VALUES
    (1, 1, 1, 1, '소니 WH-1000XM5', 'products/11/sony-xm5.jpg', 2,
     449000, 449000, 'ACCEPTED','2999-03-14 04:46:27.808092','2026-02-27 04:46:27.853178',
     '2026-02-27 04:46:27.809499','2026-02-27 04:46:27.809499','2026-02-27 04:46:27.809499',NULL, NULL);

-- ──────────────────────────────────────────
-- 6. ORDER (주문) - 총 8 개
-- 회원 3~10번, 각 1개 (총 8개)
-- ──────────────────────────────────────────
INSERT INTO orders (id, buyer_id, order_number, total_amount, quantity, payment_method, status,
                    payment_id, origin_transaction_key, paid_at, confirmed_at, cancelled_at,
                    created_at, updated_at)
VALUES
    (1,  3, 'ORD-20260227-000000000001', 56125.00, 1, 'CARD', 'CONFIRMED', 1,  'toss_tx_20260227_0001', '2026-02-27 04:46:27', NULL, NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27'),
    (2,  4, 'ORD-20260227-000000000002', 56125.00, 1, 'CARD', 'CONFIRMED', 2,  'toss_tx_20260227_0002', '2026-02-27 04:46:27', NULL, NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27'),
    (3,  5, 'ORD-20260227-000000000003', 56125.00, 1, 'CARD', 'CONFIRMED', 3,  'toss_tx_20260227_0003', '2026-02-27 04:46:27', NULL, NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27'),
    (4,  6, 'ORD-20260227-000000000004', 56125.00, 1, 'CARD', 'CONFIRMED', 4,  'toss_tx_20260227_0004', '2026-02-27 04:46:27', NULL, NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27'),
    (5,  7, 'ORD-20260227-000000000005', 56125.00, 1, 'CARD', 'CONFIRMED', 5,  'toss_tx_20260227_0005', '2026-02-27 04:46:27', NULL, NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27'),
    (6,  8, 'ORD-20260227-000000000006', 56125.00, 1, 'CARD', 'CONFIRMED', 6,  'toss_tx_20260227_0006', '2026-02-27 04:46:27', NULL, NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27'),
    (7,  9, 'ORD-20260227-000000000007', 56125.00, 1, 'CARD', 'CONFIRMED', 7,  'toss_tx_20260227_0007', '2026-02-27 04:46:27', NULL, NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27'),
    (8, 10, 'ORD-20260227-000000000008', 56125.00, 1, 'CARD', 'CONFIRMED', 8,  'toss_tx_20260227_0008', '2026-02-27 04:46:27', NULL, NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27');

-- ──────────────────────────────────────────
-- 7. ORDER_ITEM (주문 아이템) - 총 8 개
-- 주문당 1개 (펀딩1)
-- seller_id: 1번 (SELLER)
-- receiver_id: 2번 (펀딩 receiver)
-- target_type: FUNDING
-- ──────────────────────────────────────────
INSERT INTO order_items (id, order_id, target_id, target_type, order_item_type, seller_id, receiver_id,
                         price, amount, status, cancelled_at, created_at, updated_at)
VALUES
    -- 회원 3번 주문 (order_id=1)
    (1,  1, 1, 'FUNDING', 'FUNDING_GIFT', 1, 2, 449000.00, 56125.00, 'CONFIRMED', NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27'),
    -- 회원 4번 주문 (order_id=2)
    (2,  2, 1, 'FUNDING', 'FUNDING_GIFT', 1, 2, 449000.00, 56125.00, 'CONFIRMED', NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27'),
    -- 회원 5번 주문 (order_id=3)
    (3,  3, 1, 'FUNDING', 'FUNDING_GIFT', 1, 2, 449000.00, 56125.00, 'CONFIRMED', NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27'),
    -- 회원 6번 주문 (order_id=4)
    (4,  4, 1, 'FUNDING', 'FUNDING_GIFT', 1, 2, 449000.00, 56125.00, 'CONFIRMED', NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27'),
    -- 회원 7번 주문 (order_id=5)
    (5,  5, 1, 'FUNDING', 'FUNDING_GIFT', 1, 2, 449000.00, 56125.00, 'CONFIRMED', NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27'),
    -- 회원 8번 주문 (order_id=6)
    (6, 6, 1, 'FUNDING', 'FUNDING_GIFT', 1, 2, 449000.00, 56125.00, 'CONFIRMED', NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27'),
    -- 회원 9번 주문 (order_id=7)
    (7, 7, 1, 'FUNDING', 'FUNDING_GIFT', 1, 2, 449000.00, 56125.00, 'CONFIRMED', NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27'),
    -- 회원 10번 주문 (order_id=8)
    (8, 8, 1, 'FUNDING', 'FUNDING_GIFT', 1, 2, 449000.00, 56125.00, 'CONFIRMED', NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27');




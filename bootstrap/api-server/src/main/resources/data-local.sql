-- =============================================================================
-- Giftify Local Development Seed Data (H2)
-- =============================================================================
-- 이 파일은 local 프로파일에서 서버 시작 시 자동 실행됩니다.
-- H2 + ddl-auto:create 환경에서는 매번 테이블이 새로 생성되므로
-- ON CONFLICT 구문이 필요 없습니다.
--
-- Flyway 시드 데이터(V1.2.0~V1.2.4)와 1:1 동기화 상태입니다.
-- PostgreSQL setval() → H2 ALTER TABLE ... RESTART WITH 100 으로 변환.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. MEMBERS (회원) — V1.2.0
-- -----------------------------------------------------------------------------
INSERT INTO members (id, email, nickname, birthday, role, address, phone_num, name, status, auth_sub,
                     created_at, updated_at, created_by, updated_by)
VALUES (1, 'qa-seller-giftify@team201.dev', '멍청한돼지0009', '1975-11-08', 'SELLER', '서울시 종로구',
        '010-1234-5678', '김주영', 'ACTIVE', 'auth0|6981838d48f8397cae06ddb0', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 'qa-buyer-giftify@team201.dev', '나른한고양이0013', '2003-02-14', 'BUYER', '서울시 송파구',
        '010-5678-1234', '김영주', 'ACTIVE', 'auth0|698183a503a368a7b14ca6ab', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 'qa-seller-giftify@naver.com', '멍청한고양이2013', '2003-02-14', 'SELLER',
        '서울시 송파구', '010-0002-9871', '김영주', 'ACTIVE', 'auth0|6981842c839dce07958f5a37', NOW(), NOW(), 'SYSTEM',
        'SYSTEM'),
       (4, 'admin-giftify@team201.dev', '관리자', '1999-01-01', 'ADMIN', '서울시 강남구',
        '010-0000-0000', 'TEAM201', 'ACTIVE', 'auth0|6981843a226ff0ca1e6a5ae8', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 'qa-buyer2-giftify@team201.dev', '졸린토끼0042', '1998-07-22', 'BUYER', '서울시 마포구',
        '010-3333-4444', '이수현', 'ACTIVE', 'auth0|698184500000000000000001', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 'qa-buyer3-giftify@team201.dev', '배고픈강아지0007', '2001-12-25', 'BUYER', '서울시 서초구',
        '010-5555-6666', '박지민', 'ACTIVE', 'auth0|698184500000000000000002', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE members
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 2. MEMBER_REPLICA (회원 레플리카 - catalog 모듈) — V1.2.0
-- -----------------------------------------------------------------------------
INSERT INTO member_replica (id, nickname)
VALUES (1, '멍청한돼지0009'),
       (2, '나른한고양이0013'),
       (3, '멍청한고양이2013'),
       (4, '관리자'),
       (5, '졸린토끼0042'),
       (6, '배고픈강아지0007');

-- -----------------------------------------------------------------------------
-- 3. CORE_MEMBER_REPLICA (회원 레플리카 - core 모듈) — V1.0.0
-- -----------------------------------------------------------------------------
INSERT INTO core_member_replica (id, nickname)
VALUES (1, '멍청한돼지0009'),
       (2, '나른한고양이0013'),
       (3, '멍청한고양이2013'),
       (4, '관리자'),
       (5, '졸린토끼0042'),
       (6, '배고픈강아지0007');

-- -----------------------------------------------------------------------------
-- 4. WALLET (지갑) — V1.2.1
-- -----------------------------------------------------------------------------
INSERT INTO wallet (id, member_id, balance, version, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 100000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, 50000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, 1000000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, 0.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 5, 250000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 6, 180000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE wallet
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 5. CART (장바구니) — V1.2.1
-- -----------------------------------------------------------------------------
INSERT INTO cart (id, member_id, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 5, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 6, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE cart
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 6. PRODUCT (상품) — V1.2.2
-- -----------------------------------------------------------------------------
INSERT INTO product (id, seller_id, name, description, price, stock, status, image_key, category,
                     created_at, updated_at, created_by, updated_by)
VALUES
    (1, 3, '에어팟 프로 2세대', '애플 정품 노이즈 캔슬링 이어폰', 359000, 50, 'ACTIVE',
     'products/1/airpods-pro-2.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (2, 3, '스타벅스 텀블러', '리유저블 콜드컵 710ml 그린', 23000, 50, 'ACTIVE',
     'products/2/starbucks-tumbler.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (3, 3, '닌텐도 스위치 OLED', '화이트 에디션 새상품', 415000, 50, 'ACTIVE',
     'products/3/nintendo-switch-oled.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (4, 3, '다이슨 에어랩', '컴플리트 롱 니켈/코퍼', 699000, 50, 'ACTIVE',
     'products/4/dyson-airwrap.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (5, 3, '레고 스타워즈', '밀레니엄 팔콘 75375', 89000, 50, 'ACTIVE',
     'products/5/lego-starwars.jpg', 'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (6, 3, '캠핑 감성 랜턴', '충전식 LED 무드등 빈티지 스타일', 32000, 50, 'DRAFT',
     'products/6/camping-lantern.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (7, 3, '고양이 자동 급식기', '6L 대용량 스마트 펫 피더', 78000, 50, 'DRAFT',
     'products/7/pet-feeder.jpg', 'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (8, 3, '마샬 스피커', '액톤 III 블루투스 스피커 블랙', 489000, 50, 'DRAFT',
     'products/8/marshall-speaker.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (9, 3, '무지 아로마 디퓨저', '초음파 가습기 겸용 500ml', 45000, 50, 'DRAFT',
     'products/9/aroma-diffuser.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (10, 3, '몽블랑 볼펜', '마이스터스튁 클래식 블랙', 520000, 50, 'DRAFT',
     'products/10/montblanc-pen.jpg', 'FASHION', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE product
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 7. WISHLIST (위시리스트) — V1.2.2
-- -----------------------------------------------------------------------------
INSERT INTO wishlist (id, member_id, visibility, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, 'PRIVATE', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 5, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 6, 'FRIENDS_ONLY', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE wishlist
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 8. WISHLIST_ITEM (위시리스트 아이템) — V1.2.2
-- -----------------------------------------------------------------------------
INSERT INTO wishlist_item (id, wishlist_id, product_id, wishlist_item_status, added_at, created_at, updated_at,
                           created_by, updated_by)
VALUES (1, 1, 1, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 1, 2, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 2, 3, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 2, 4, 'IN_PROGRESS', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 2, 5, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 3, 1, 'COMPLETED', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (7, 3, 3, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (8, 5, 4, 'IN_PROGRESS', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (9, 5, 1, 'COMPLETED', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (10, 6, 5, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE wishlist_item
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 9. CART_ITEM (장바구니 아이템) — V1.2.2
-- -----------------------------------------------------------------------------
INSERT INTO cart_item (id, cart_id, target_type, target_id, amount, wishlist_item_status)
VALUES (1, 2, 'GENERAL_PRODUCT', 1, 359000.00, NULL),
       (2, 5, 'GENERAL_PRODUCT', 4, 699000.00, NULL),
       (3, 5, 'GENERAL_PRODUCT', 2, 23000.00, NULL),
       (4, 6, 'GENERAL_PRODUCT', 3, 415000.00, NULL),
       (5, 6, 'GENERAL_PRODUCT', 5, 89000.00, NULL);

ALTER TABLE cart_item
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 10. FUNDING (펀딩) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO funding (id, version, wishlist_item_id, product_id, receiver_id, target_amount, current_amount,
                     status, deadline, achieved_at, closed_at,
                     created_at, updated_at, created_by, updated_by)
VALUES
    (1, 0,8, 4, 5, 699000, 15000, 'IN_PROGRESS',
     '2026-03-01 23:59:59', NULL, NULL,
     NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

    (2, 0,9, 1, 5, 359000, 359000, 'ACHIEVED',
     '2026-02-28 23:59:59', '2026-02-07 14:30:00', NULL,
     NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

    (3, 0,4, 3, 2, 415000, 120000, 'EXPIRED',
     '2026-02-01 23:59:59', NULL, '2026-02-01 23:59:59',
     NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

    (4, 0,2, 3, 2, 415000, 415000, 'ACHIEVED',
     '2026-02-15 23:59:59', '2026-02-13 23:59:59', NULL,
     NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE funding
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 11. FUNDING_PARTICIPANT_MEMBER (펀딩 참여자) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO funding_participant_member (id, funding_id, participant_id, nick_name, amount,
                                        created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 2, '나른한고양이0013', 10000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 1, 6, '배고픈강아지0007', 555000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 2, 2, '나른한고양이0013', 1200000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 2, 6,  '배고픈강아지0007', 159000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 3, 2,  '나른한고양이0013', 1159000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 4, 2,  '나른한고양이0013', 1159000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (7, 4, 6,  '배고픈강아지0007', 10000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (8, 4, 1,  '멍청한돼지0009', 50000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (9, 4, 2,  '참가자4', 1000, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE funding_participant_member
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 12. ORDER_V2 (주문) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO order_v2 (id, buyer_id, order_number, total_amount, quantity, payment_method, status,
                      payment_id, origin_transaction_key, paid_at, confirmed_at, cancelled_at,
                      created_at, updated_at)
VALUES
    (1, 2, 'ORD-20260205-A1B2C3D4E5F6-20260205170000', 359000.00, 1, 'CARD', 'PAID',
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

ALTER TABLE order_v2
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 13. ORDER_ITEM_V2 (주문 아이템) — V1.2.4
-- -----------------------------------------------------------------------------
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
     89000.00, 89000.00, 'CANCELED', '2026-02-08 16:00:00', '2026-02-08 15:00:00', '2026-02-08 16:00:00');

ALTER TABLE order_item_v2
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 14. PAYMENT (결제) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO payment (id, type, method, order_id, order_number, member_id,
                     origin_amount, paid_amount, refunded_amount, order_items_json, status,
                     payment_key, last_transaction_key, approve_code, paid_at,
                     created_at, updated_at, created_by, updated_by)
VALUES
    (1, 'FUNDING', 'CARD',
     1, 'ORD-20260205-A1B2C3D4E5F6-20260205170000', 2,
     359000.00, 359000.00, 0.00, '[{"targetId":1,"amount":{"amount":359000},"sellerId":3}]', 'PAID',
     'toss_pk_20260205_0001', 'toss_tx_20260205_0001', 'approve_001', '2026-02-05 17:05:00',
     '2026-02-05 17:00:00', '2026-02-05 17:05:00', 'SYSTEM', 'SYSTEM'),

    (2, 'FUNDING', 'CARD',
     2, 'ORD-20260205-B2C3D4E5F6G7-20260205171000', 2,
     23000.00, 23000.00, 0.00, '[{"targetId":2,"amount":{"amount":23000},"sellerId":3}]', 'PAID',
     'toss_pk_20260205_0002', 'toss_tx_20260205_0002', 'approve_002', '2026-02-05 17:15:00',
     '2026-02-05 17:10:00', '2026-02-05 17:15:00', 'SYSTEM', 'SYSTEM'),

    (3, 'FUNDING', 'KAKAO_PAY',
     3, 'ORD-20260208-D4E5F6G7H8I9-20260208140000', 6,
     448000.00, 448000.00, 0.00, '[{"targetId":2,"amount":{"amount":23000},"sellerId":3},{"targetId":3,"amount":{"amount":415000},"sellerId":3}]', 'PAID',
     'toss_pk_20260208_0004', 'toss_tx_20260208_0004', 'approve_004', '2026-02-08 14:05:00',
     '2026-02-08 14:00:00', '2026-02-08 14:05:00', 'SYSTEM', 'SYSTEM');

ALTER TABLE payment
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 15. PAYMENT_HISTORY (결제 이력) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO payment_history (id, payment_id, history_key, event_type, occurred_at, metadata,
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

ALTER TABLE payment_history
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 16. WALLET_HISTORY (지갑 이력) — V1.2.4
-- -----------------------------------------------------------------------------
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

ALTER TABLE wallet_history
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 17. FRIENDSHIPS(소셜) -
-- -----------------------------------------------------------------------------
    INSERT INTO friendships (id, requester_id, receiver_id, status, accepted_at,
                             created_at, updated_at, created_by, updated_by)
    VALUES (1, 2, 3, 'ACCEPTED', '2026-02-07 12:00:00', '2026-02-07 12:00:00', '2026-02-07 12:00:00','SYSTEM','SYSTEM');

ALTER TABLE friendships
    ALTER COLUMN id RESTART WITH 100;

-- =============================================================================
-- Seed Data Summary (Flyway V1.2.0~V1.2.4 동기화)
-- =============================================================================
-- Members: 6명 (SELLER 2, BUYER 3, ADMIN 1)
--   - ID 1: qa-seller-giftify@team201.dev (SELLER, 멍청한돼지0009)
--   - ID 2: qa-buyer-giftify@team201.dev (BUYER, 나른한고양이0013)
--   - ID 3: qa-seller-giftify@naver.com (SELLER, 멍청한고양이2013)
--   - ID 4: admin-giftify@team201.dev (ADMIN, 관리자)
--   - ID 5: qa-buyer2-giftify@team201.dev (BUYER, 졸린토끼0042)
--   - ID 6: qa-buyer3-giftify@team201.dev (BUYER, 배고픈강아지0007)
-- Wallets: 6개 (각 회원당 1개)
-- Carts: 6개 (각 회원당 1개)
-- Products: 10개 (ACTIVE 5, DRAFT 5) - seller_id=3
-- Wishlists: 6개 (PUBLIC 4, PRIVATE 1, FRIENDS_ONLY 1)
-- WishlistItems: 10개 (PENDING 6, IN_PROGRESS 2, COMPLETED 2)
-- CartItems: 5개 (buyer2: 2, buyer3: 2, buyer1: 1)
-- Fundings: 4개 (IN_PROGRESS 1, ACHIEVED 2, EXPIRED 1)
-- FundingParticipants: 9명
-- Orders: 5개 (PAID 2, CONFIRMED 1, CREATED 1, CANCELED 1)
-- OrderItems: 6개
-- Payments: 3개 (모두 PAID)
-- PaymentHistory: 5건
-- WalletHistory: 5건
-- =============================================================================

-- =============================================================================
-- Giftify Local Development Seed Data (H2)
-- =============================================================================
-- 이 파일은 local 프로파일에서 서버 시작 시 자동 실행됩니다.
-- H2 + ddl-auto:create 환경에서는 매번 테이블이 새로 생성되므로
-- ON CONFLICT 구문이 필요 없습니다.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. MEMBERS (회원)
-- -----------------------------------------------------------------------------
INSERT INTO members (id, email, password, nickname, birthday, role, address, phone_num, name, status, auth_sub,
                     created_at, updated_at, created_by, updated_by)
VALUES (1, 'qa-seller-giftify@team201.dev', '1Q@W3r4R1q2w3e4r', '멍청한돼지0009', '1975-11-08', 'SELLER', '서울시 종로구',
        '010-1234-5678', '김주영', 'ACTIVE', 'auth0|6981838d48f8397cae06ddb0', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 'qa-buyer-giftify@team201.dev', '1Q@W3r4R1q2w3e4r', '나른한고양이0013', '2003-02-14', 'BUYER', '서울시 송파구',
        '010-5678-1234', '김영주', 'ACTIVE', 'auth0|698183a503a368a7b14ca6ab', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 'qa-seller-giftify@naver.com', '5@675995dC43786719eb76Cd455d3b368', '멍청한고양이2013', '2003-02-14', 'SELLER',
        '서울시 송파구', '010-0002-9871', '김영주', 'ACTIVE', 'auth0|6981842c839dce07958f5a37', NOW(), NOW(), 'SYSTEM',
        'SYSTEM'),
       (4, 'admin-giftify@team201.dev', '@675995dC43786719eb76Cd455d3b368', '관리자', '1999-01-01', 'ADMIN', '서울시 강남구',
        '010-0000-0000', 'TEAM201', 'ACTIVE', 'auth0|6981843a226ff0ca1e6a5ae8', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

-- ID 시퀀스 조정 (H2)
ALTER TABLE members
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 2. MEMBER_REPLICA (회원 레플리카 - catalog 모듈)
-- -----------------------------------------------------------------------------
INSERT INTO member_replica (id, nickname)
VALUES (1, '멍청한돼지0009'),
       (2, '나른한고양이0013'),
       (3, '멍청한고양이2013'),
       (4, '관리자');

-- -----------------------------------------------------------------------------
-- 3. FUNDING_MEMBER (펀딩 회원 - core 모듈)
-- -----------------------------------------------------------------------------
INSERT INTO funding_member (id, auth_sub, nickname)
VALUES (1, 'auth0|6981838d48f8397cae06ddb0', '멍청한돼지0009'),
       (2, 'auth0|698183a503a368a7b14ca6ab', '나른한고양이0013'),
       (3, 'auth0|6981842c839dce07958f5a37', '멍청한고양이2013'),
       (4, 'auth0|6981843a226ff0ca1e6a5ae8', '관리자');

-- -----------------------------------------------------------------------------
-- 4. WALLET (지갑)
-- -----------------------------------------------------------------------------
INSERT INTO wallet (id, member_id, balance, version, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 100000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, 50000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, 1000000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, 0.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE wallet
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 5. CART (장바구니)
-- -----------------------------------------------------------------------------
INSERT INTO cart (id, member_id, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE cart
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 6. WISHLIST (위시리스트)
-- -----------------------------------------------------------------------------
INSERT INTO wishlist (id, member_id, visibility, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 'PRIVATE', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, 'PRIVATE', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, 'PRIVATE', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, 'PRIVATE', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE wishlist
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 7. PRODUCT (상품)
-- -----------------------------------------------------------------------------
INSERT INTO product (id, seller_id, name, description, price, stock, status, created_at, updated_at, created_by,
                     updated_by)
VALUES
    -- ACTIVE 상품 (판매중)
    (1, 3, '에어팟 프로 2세대', '애플 정품 노이즈 캔슬링 이어폰', 359000, 50, 'ACTIVE', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (2, 3, '스타벅스 텀블러', '리유저블 콜드컵 710ml 그린', 23000, 50, 'ACTIVE', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (3, 3, '닌텐도 스위치 OLED', '화이트 에디션 새상품', 415000, 50, 'ACTIVE', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (4, 3, '다이슨 에어랩', '컴플리트 롱 니켈/코퍼', 699000, 50, 'ACTIVE', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (5, 3, '레고 스타워즈', '밀레니엄 팔콘 75375', 89000, 50, 'ACTIVE', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    -- DRAFT 상품 (승인 대기)
    (6, 3, '캠핑 감성 랜턴', '충전식 LED 무드등 빈티지 스타일', 32000, 50, 'DRAFT', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (7, 3, '고양이 자동 급식기', '6L 대용량 스마트 펫 피더', 78000, 50, 'DRAFT', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (8, 3, '마샬 스피커', '액톤 III 블루투스 스피커 블랙', 489000, 50, 'DRAFT', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (9, 3, '무지 아로마 디퓨저', '초음파 가습기 겸용 500ml', 45000, 50, 'DRAFT', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (10, 3, '몽블랑 볼펜', '마이스터스튁 클래식 블랙', 520000, 50, 'DRAFT', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE product
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 8. WISHLIST_ITEM (위시리스트 아이템) - Optional
-- -----------------------------------------------------------------------------
INSERT INTO wishlist_item (id, wishlist_id, product_id, wishlist_item_status, created_at, updated_at, created_by,
                           updated_by)
VALUES (1, 1, 1, 'PENDING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 1, 2, 'PENDING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 2, 3, 'PENDING', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE wishlist_item
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 9. ORDER_V2 (주문)
-- -----------------------------------------------------------------------------
INSERT INTO order_v2 (id, buyer_id, order_number, total_amount, quantity, payment_method, status,
                      created_at, updated_at)
VALUES
    -- PAID 주문: 구매자(2)가 에어팟(1) 구매
    (1, 2, 'ORD-20260201-0001', 359000.00, 1, 'WALLET', 'PAID', NOW(), NOW()),
    -- CONFIRMED 주문: 구매자(2)가 스타벅스 텀블러(2) 구매 후 확정
    (2, 2, 'ORD-20260201-0002', 23000.00, 1, 'WALLET', 'CONFIRMED', NOW(), NOW()),
    -- CREATED 주문: 구매자(2)가 닌텐도 스위치(3) 주문 생성 (결제 전)
    (3, 2, 'ORD-20260202-0001', 415000.00, 1, 'WALLET', 'CREATED', NOW(), NOW());

ALTER TABLE order_v2
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 10. ORDER_ITEM_V2 (주문 아이템)
-- -----------------------------------------------------------------------------
INSERT INTO order_item_v2 (id, order_id, target_id, target_type, order_item_type, seller_id, receiver_id,
                           price, amount, status, created_at, updated_at)
VALUES
    -- 주문1의 아이템: 에어팟 프로
    (1, 1, 1, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 2, 359000.00, 359000.00, 1, NOW(), NOW()),
    -- 주문2의 아이템: 스타벅스 텀블러
    (2, 2, 2, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 2, 23000.00, 23000.00, 2, NOW(), NOW()),
    -- 주문3의 아이템: 닌텐도 스위치
    (3, 3, 3, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 2, 415000.00, 415000.00, 0, NOW(), NOW());

ALTER TABLE order_item_v2
    ALTER COLUMN id RESTART WITH 100;

-- =============================================================================
-- Seed Data Summary
-- =============================================================================
-- Members: 4명 (SELLER 2, BUYER 1, ADMIN 1)
--   - ID 1: qa-seller-giftify@team201.dev (SELLER, 멍청한돼지0009)
--   - ID 2: qa-buyer-giftify@team201.dev (BUYER, 나른한고양이0013)
--   - ID 3: qa-seller-giftify@naver.com (SELLER, 멍청한고양이2013)
--   - ID 4: admin-giftify@team201.dev (ADMIN, 관리자)
-- Wallets: 4개 (각 회원당 1개, 테스트용 잔액 포함)
-- Carts: 4개 (각 회원당 1개)
-- Wishlists: 4개 (각 회원당 1개)
-- Products: 10개 (ACTIVE 5, DRAFT 5) - seller_id=3
-- WishlistItems: 3개 (테스트용)
-- Orders: 3개 (PAID 1, CONFIRMED 1, CREATED 1) - buyer_id=2
-- OrderItems: 3개 (주문당 1개)
-- =============================================================================

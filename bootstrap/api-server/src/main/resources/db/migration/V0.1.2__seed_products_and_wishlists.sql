-- =============================================================================
-- V1.2.2: Seed - Products, Wishlists, Wishlist Items, Cart Items
-- =============================================================================

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

SELECT setval('product_id_seq', (SELECT MAX(id) FROM product));

INSERT INTO wishlist (id, member_id, visibility, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, 'PRIVATE', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 5, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 6, 'FRIENDS_ONLY', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

SELECT setval('wishlist_id_seq', (SELECT MAX(id) FROM wishlist));

INSERT INTO wishlist_item (id, wishlist_id, product_id, wishlist_item_status, added_at,
                           created_at, updated_at, created_by, updated_by)
VALUES
    (1, 1, 1, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (2, 1, 2, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (3, 2, 3, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (4, 2, 4, 'IN_PROGRESS', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (5, 2, 5, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (6, 3, 1, 'COMPLETED', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (7, 3, 3, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (8, 5, 4, 'IN_PROGRESS', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (9, 5, 1, 'COMPLETED', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (10, 6, 5, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM');

SELECT setval('wishlist_item_id_seq', (SELECT MAX(id) FROM wishlist_item));

INSERT INTO cart_item (id, cart_id, target_type, target_id, amount, wishlist_item_status)
VALUES
    (1, 2, 'GENERAL_PRODUCT', 1, 359000.00, NULL),
    (2, 5, 'GENERAL_PRODUCT', 4, 699000.00, NULL),
    (3, 5, 'GENERAL_PRODUCT', 2, 23000.00, NULL),
    (4, 6, 'GENERAL_PRODUCT', 3, 415000.00, NULL),
    (5, 6, 'GENERAL_PRODUCT', 5, 89000.00, NULL);

SELECT setval('cart_item_id_seq', (SELECT MAX(id) FROM cart_item));

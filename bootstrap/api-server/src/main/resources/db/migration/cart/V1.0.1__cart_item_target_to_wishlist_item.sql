-- 1. 새 컬럼 추가
ALTER TABLE cart_items
    ADD COLUMN IF NOT EXISTS wishlist_item_id BIGINT;

-- 2. 기존 데이터 마이그레이션
UPDATE cart_items
SET wishlist_item_id = target_id;

-- 3. NOT NULL 제약 (데이터 이관 후)
ALTER TABLE cart_items
    ALTER COLUMN wishlist_item_id SET NOT NULL;

-- 4. 기존 컬럼 제거
ALTER TABLE cart_items
DROP COLUMN target_type,
DROP COLUMN target_id;

-- 5. ★핵심: 부모 데이터 보장 후 자식 데이터 삽입
INSERT INTO carts (id, member_id, created_at, updated_at, created_by, updated_by)
VALUES (2, 2, NOW(), NOW(), 'SYSTEM', 'SYSTEM')
    ON CONFLICT (id) DO NOTHING; -- id 2번이 없으면 넣고, 있으면 통과

INSERT INTO cart_items (id, cart_id, wishlist_item_id, amount, wishlist_item_status)
VALUES (1, 2, 9, 539100.00, 'IN_PROGRESS')
    ON CONFLICT (id) DO NOTHING;
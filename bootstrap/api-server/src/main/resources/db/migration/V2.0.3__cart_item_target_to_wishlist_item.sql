-- 1. 새 컬럼 추가
ALTER TABLE cart_item
    ADD COLUMN wishlist_item_id BIGINT;

-- 2. 기존 데이터 마이그레이션
UPDATE cart_item
SET wishlist_item_id = target_id
WHERE target_type = 'FUNDING';

-- 3. NOT NULL 제약 (데이터 이관 후)
ALTER TABLE cart_item
    ALTER COLUMN wishlist_item_id SET NOT NULL;

-- 4. 기존 컬럼 제거
ALTER TABLE cart_item
DROP COLUMN target_type,
DROP COLUMN target_id;

-- 5. 테스트 데이터 삽입
INSERT INTO cart_item (id, cart_id, wishlist_item_id, amount, wishlist_item_status)
VALUES (1, 2, 9, 539100.00, 'IN_PROGRESS')
    ON CONFLICT DO NOTHING;
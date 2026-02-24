-- 1. 기존 유니크 제약 조건 삭제
ALTER TABLE settlement_item
    DROP CONSTRAINT IF EXISTS uk_target_id_type;

-- 2. 기존 필드 제거 (orderNumber, orderedAt, paidAt)
ALTER TABLE settlement_item
    DROP COLUMN IF EXISTS order_number,
    DROP COLUMN IF EXISTS ordered_at,
    DROP COLUMN IF EXISTS paid_at;

-- 3. 신규 필드 추가 및 수정
ALTER TABLE settlement_item
    ADD COLUMN IF NOT EXISTS target_type VARCHAR(50) NOT NULL,
    ADD COLUMN IF NOT EXISTS payment_id BIGINT NOT NULL;

-- 4. 새로운 유니크 제약 조건 추가 (order_item_id + type)
-- 정산 아이템은 '주문 아이템' 하나당 '유형별'로 유일해야 함
ALTER TABLE settlement_item
    ADD CONSTRAINT uk_order_item_id_type
        UNIQUE (order_item_id, type);

-- 5. 인덱스 최적화 (조회용)
CREATE INDEX IF NOT EXISTS idx_settlement_item_payment_id ON settlement_item(payment_id);
CREATE INDEX IF NOT EXISTS idx_settlement_item_order_item_id ON settlement_item(order_item_id);
CREATE INDEX IF NOT EXISTS idx_settlement_item_order_item_id ON settlement_item(seller_id);
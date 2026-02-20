-- order_item_v2: orderId, status 인덱스 생성
CREATE INDEX idx_order_items_order_id_status
    ON order_item_v2 (order_id, status);

-- order_item_v2: 주문 아이템에 개별 취소 일자 추가
ALTER TABLE order_item_v2
    ADD COLUMN cancel_requested_at TIMESTAMP WITH TIME ZONE;

-- order_v2: 주문 전체 취소 일자 추가
ALTER TABLE order_v2
    ADD COLUMN cancel_requested_at TIMESTAMP WITH TIME ZONE;
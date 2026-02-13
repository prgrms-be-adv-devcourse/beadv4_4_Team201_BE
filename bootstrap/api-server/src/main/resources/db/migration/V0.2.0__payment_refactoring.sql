-- V0.2.0__payment_refactoring.sql
-- Payment 도메인 리팩터링 (Task 1 + Task 5)

-- ==========================================
-- Task 1: idempotencyKey 제거, orderId UNIQUE
-- ==========================================

-- payment 테이블: idempotency_key 제거
ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_idempotency_key_key;
ALTER TABLE payment DROP COLUMN IF EXISTS idempotency_key;

-- payment 테이블: order_id에 UNIQUE 제약 추가
ALTER TABLE payment ADD CONSTRAINT uk_payment_order_id UNIQUE (order_id);

-- payment_history 테이블: idempotency_key → history_key 이름 변경
ALTER TABLE payment_history DROP CONSTRAINT IF EXISTS payment_history_idempotency_key_key;
ALTER TABLE payment_history RENAME COLUMN idempotency_key TO history_key;
ALTER TABLE payment_history ADD CONSTRAINT uk_payment_history_history_key UNIQUE (history_key);

-- ==========================================
-- Task 5: 부분 환불 지원 (refunded_amount)
-- ==========================================

ALTER TABLE payment ADD COLUMN refunded_amount NUMERIC(19,2) NOT NULL DEFAULT 0;

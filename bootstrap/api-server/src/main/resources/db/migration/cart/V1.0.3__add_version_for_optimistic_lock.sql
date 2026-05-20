-- carts, cart_items 에 낙관적 락(@Version) 컬럼 추가
-- 사유: 동일 memberId 동시 cart_add 요청 시 PG row-level exclusive lock 직렬화로 p95 2.18s 발생.
-- JPA @Version 적용으로 lock-free 경합 + 충돌 시 retry 정책 (CartService @Retryable) 으로 전환.
-- 기존 row 는 default 0 으로 채워지며, 신규 row 는 Hibernate 가 1 부터 증가.

ALTER TABLE carts ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE cart_items ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

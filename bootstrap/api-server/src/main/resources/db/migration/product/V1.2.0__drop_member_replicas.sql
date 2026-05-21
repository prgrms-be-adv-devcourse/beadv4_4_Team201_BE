-- 공유 member_replicas 테이블을 BC 별 read-model 로 마이그레이션 후 폐기
-- product / cart / wishlist 의 V1.1.0 에서 view 테이블이 이미 생성된 상태를 전제로 함

INSERT INTO product_member_views (id, nickname)
SELECT id, nickname FROM member_replicas
ON CONFLICT (id) DO NOTHING;

INSERT INTO cart_member_views (id, nickname)
SELECT id, nickname FROM member_replicas
ON CONFLICT (id) DO NOTHING;

INSERT INTO wishlist_member_views (id, nickname)
SELECT id, nickname FROM member_replicas
ON CONFLICT (id) DO NOTHING;

DROP TABLE IF EXISTS member_replicas;

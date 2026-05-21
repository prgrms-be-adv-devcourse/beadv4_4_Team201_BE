-- bc/core 의 사적 core_member_replicas 테이블을 funding read-model 로 마이그레이션 후 폐기
-- funding/V1.1.0 에서 funding_member_views 가 이미 생성된 상태를 전제로 함

INSERT INTO funding_member_views (id, nickname)
SELECT id, nickname FROM core_member_replicas
ON CONFLICT (id) DO NOTHING;

DROP TABLE IF EXISTS core_member_replicas;

-- =============================================================================
-- V0.5.1_: core_member_replica, Funding Participants
-- =============================================================================

-- core_member_replica 테이블 생성
CREATE TABLE IF NOT EXISTS core_member_replica (
    id bigint NOT NULL,
    nickname character varying(100) NOT NULL
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'core_member_replica_pkey') THEN
        ALTER TABLE core_member_replica ADD CONSTRAINT core_member_replica_pkey PRIMARY KEY (id);
    END IF;
END $$;

-- core_member_replica 데이터 추가 (중복 방지)
INSERT INTO core_member_replica (id, nickname)
VALUES (1, '멍청한돼지0009'),
       (2, '나른한고양이0013'),
       (3, '멍청한고양이2013'),
       (4, '관리자'),
       (5, '졸린토끼0042'),
       (6, '배고픈강아지0007')
ON CONFLICT (id) DO NOTHING;

-- funding 테이블에 version 컬럼 추가 (존재하지 않을 경우에만)
ALTER TABLE funding ADD COLUMN IF NOT EXISTS version bigint NOT NULL DEFAULT 0;

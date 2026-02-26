-- settlement_item 테이블에 BaseJpaEntity audit 컬럼 추가
ALTER TABLE settlement_item
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

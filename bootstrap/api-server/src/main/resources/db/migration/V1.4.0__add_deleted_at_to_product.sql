-- product 테이블에 soft delete 컬럼 추가
ALTER TABLE product
    ADD COLUMN deleted_at timestamp(6);

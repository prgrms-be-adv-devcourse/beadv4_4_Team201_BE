-- Spring Modulith 1.3 -> 2.0: event_publication 테이블 스키마 변경
-- 추가 컬럼: completion_attempts, last_resubmission_date, status

ALTER TABLE event_publication
    ADD COLUMN IF NOT EXISTS completion_attempts INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_resubmission_date TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS status VARCHAR(255) DEFAULT 'PUBLISHED';

ALTER TABLE event_publication_archive
    ADD COLUMN IF NOT EXISTS completion_attempts INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_resubmission_date TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS status VARCHAR(255) DEFAULT 'COMPLETED';

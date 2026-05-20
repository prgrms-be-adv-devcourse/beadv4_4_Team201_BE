-- 동일 (funding_id, participant_id) 조합의 동시 중복 INSERT 차단.
-- 어플리케이션 레벨 존재 여부 확인 - INSERT 사이 race condition 보완.
ALTER TABLE funding_participant_members
    ADD CONSTRAINT uk_funding_participant_member UNIQUE (funding_id, participant_id);

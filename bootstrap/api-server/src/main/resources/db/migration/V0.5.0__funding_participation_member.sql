-- V0.5.0__funding_participant_member.sql

-- funding_participant_member 테이블: nick_name 추가
ALTER TABLE funding_participant_member ADD COLUMN nick_name VARCHAR(255);
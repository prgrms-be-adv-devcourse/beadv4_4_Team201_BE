-- payment: Optimistic Locking을 위한 version 컬럼 추가
alter table payment add column version bigint not null default 0;

-- wallet_history: 멱등성 확인 쿼리 성능을 위한 복합 인덱스
create index idx_wallet_history_ref on wallet_history(reference_id, reference_type);

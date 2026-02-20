-- order_v2: last_transaction_key 컬럼명 변경
ALTER TABLE order_v2 RENAME COLUMN last_transaction_key TO origin_transaction_key;

-- order_item_v2: originTransactionKey 컬럼 추가
ALTER TABLE order_item_v2 ADD COLUMN origin_transaction_key character varying(255);
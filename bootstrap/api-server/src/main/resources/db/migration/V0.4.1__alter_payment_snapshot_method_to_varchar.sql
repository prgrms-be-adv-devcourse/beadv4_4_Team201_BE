-- payment_snapshot.method 컬럼 타입 변경: smallint(ORDINAL) → varchar(STRING)

ALTER TABLE payment_snapshot DROP CONSTRAINT IF EXISTS payment_snapshot_method_check;

ALTER TABLE payment_snapshot
ALTER
COLUMN method TYPE varchar(255)
    USING CASE method
        WHEN 0 THEN 'CARD'
        WHEN 1 THEN 'KAKAO_PAY'
        WHEN 2 THEN 'NAVER_PAY'
        WHEN 3 THEN 'TOSS_PAY'
        WHEN 4 THEN 'ACCOUNT_TRANSFER'
        WHEN 5 THEN 'VIRTUAL_ACCOUNT'
        WHEN 6 THEN 'BANK_TRANSFER'
        WHEN 7 THEN 'DEPOSIT'
        WHEN 8 THEN 'POINT'
END;

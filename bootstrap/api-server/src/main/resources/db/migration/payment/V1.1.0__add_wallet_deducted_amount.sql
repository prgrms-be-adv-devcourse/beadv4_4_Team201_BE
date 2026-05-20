ALTER TABLE payments
    ADD COLUMN wallet_deducted_amount numeric(19,2) NOT NULL DEFAULT 0;

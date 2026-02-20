ALTER TABLE payment RENAME COLUMN order_id TO order_number;

ALTER TABLE payment DROP CONSTRAINT IF EXISTS uk_payment_order_id;
ALTER TABLE payment ADD CONSTRAINT uk_payment_order_number UNIQUE (order_number);

ALTER TABLE payment ADD COLUMN order_id BIGINT;

CREATE INDEX idx_payment_order_id ON payment(order_id);

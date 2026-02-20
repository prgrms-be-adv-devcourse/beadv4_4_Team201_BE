-- product_stock_history.change_type check constraint 변경

ALTER TABLE product_stock_history DROP CONSTRAINT product_stock_history_change_type_check;

ALTER TABLE product_stock_history
    ADD CONSTRAINT product_stock_history_change_type_check
        CHECK (change_type IN ('MANUAL_SELLER', 'MANUAL_SYSTEM', 'ORDER_COMPLETED', 'ORDER_REFUNDED'));

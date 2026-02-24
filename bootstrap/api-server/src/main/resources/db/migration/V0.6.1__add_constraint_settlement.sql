ALTER TABLE settlement_item
    ADD CONSTRAINT uk_order_id_order_item_id_type
        UNIQUE (order_id, order_item_id, type);
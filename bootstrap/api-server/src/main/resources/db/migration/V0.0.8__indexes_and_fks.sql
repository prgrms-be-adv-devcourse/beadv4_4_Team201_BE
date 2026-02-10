-- Cross-module indexes and foreign keys

CREATE INDEX idx_funding_wishlist_status ON funding USING btree (wishlist_item_id, status);
CREATE INDEX idx_settlement_order_id ON settlement_item USING btree (order_id);
CREATE INDEX idx_settlement_status_created_retry ON settlement_item USING btree (status, created_at, retry_count);

ALTER TABLE ONLY cart_item ADD CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart(id);
ALTER TABLE ONLY funding_participant_member ADD CONSTRAINT fk_funding_participant_funding FOREIGN KEY (funding_id) REFERENCES funding(id);
ALTER TABLE ONLY settlement_queue ADD CONSTRAINT fk_settlement_queue_item FOREIGN KEY (settlement_item_id) REFERENCES settlement_item(id);
ALTER TABLE ONLY order_item_v2 ADD CONSTRAINT fk_order_item_v2_order FOREIGN KEY (order_id) REFERENCES order_v2(id);

-- Indexes and Foreign Keys

create index idx_funding_wishlist_status on funding (wishlist_item_id, status);
create index idx_order_items_order_id_status on order_items (order_id, status);
create index idx_settlement_status_created_retry on settlement_item (status, created_at, retry_count);
create index idx_settlement_order_id on settlement_item (order_id);

alter table if exists cart_item
    add constraint fk_cart_item_cart foreign key (cart_id) references cart;

alter table if exists funding_participant_member
    add constraint fk_funding_participant_funding foreign key (funding_id) references funding;

alter table if exists order_items
    add constraint fk_order_items_orders foreign key (order_id) references orders;

alter table if exists settlement_queue
    add constraint fk_settlement_queue_item foreign key (settlement_item_id) references settlement_item;

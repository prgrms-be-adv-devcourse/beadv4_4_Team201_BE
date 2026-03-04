-- V1.0.1 Fix member auth_sub to match Auth0 dev tenant
-- Fixes google-oauth2|* placeholder values -> real auth0|* IDs
-- Removes members 5, 6 (not registered in Auth0)

---------------------------------------------------
-- 1. Update auth_sub for existing members (1-4)
---------------------------------------------------
UPDATE members SET auth_sub = 'auth0|6981838d48f8397cae06ddb0' WHERE id = 1;
UPDATE members SET auth_sub = 'auth0|698183a503a368a7b14ca6ab' WHERE id = 2;
UPDATE members SET auth_sub = 'auth0|6981842c839dce07958f5a37' WHERE id = 3;
UPDATE members SET auth_sub = 'auth0|6981843a226ff0ca1e6a5ae8' WHERE id = 4;

---------------------------------------------------
-- 2. Remove members 5, 6 and all dependent data
---------------------------------------------------

-- payment_history (via payment)
DELETE FROM payment_history WHERE payment_id IN (
    SELECT id FROM payment WHERE member_id IN (5, 6)
);

-- payment
DELETE FROM payment WHERE member_id IN (5, 6);

-- order_items (via orders)
DELETE FROM order_items WHERE order_id IN (
    SELECT id FROM orders WHERE buyer_id IN (5, 6)
);

-- order_items where receiver is member 5 or 6
DELETE FROM order_items WHERE receiver_id IN (5, 6);

-- orders
DELETE FROM orders WHERE buyer_id IN (5, 6);

-- funding_participant_member (via funding)
DELETE FROM funding_participant_member WHERE funding_id IN (
    SELECT id FROM funding WHERE receiver_id IN (5, 6)
);

-- funding_participant_member where participant is member 5 or 6
DELETE FROM funding_participant_member WHERE participant_id IN (5, 6);

-- funding
DELETE FROM funding WHERE receiver_id IN (5, 6);

-- wishlist_item (via wishlist)
DELETE FROM wishlist_item WHERE wishlist_id IN (
    SELECT id FROM wishlist WHERE member_id IN (5, 6)
);

-- wishlist
DELETE FROM wishlist WHERE member_id IN (5, 6);

-- notifications
DELETE FROM notifications WHERE recipient_id IN (5, 6);

-- friendships
DELETE FROM friendships WHERE requester_id IN (5, 6) OR receiver_id IN (5, 6);

-- product_stock_history (no direct member FK, but check seller_id)
-- Members 5, 6 are BUYER role, no products owned

-- cart
DELETE FROM cart WHERE member_id IN (5, 6);

-- wallet
DELETE FROM wallet WHERE member_id IN (5, 6);

-- member replicas
DELETE FROM member_replica WHERE id IN (5, 6);
DELETE FROM core_member_replica WHERE id IN (5, 6);

-- members
DELETE FROM members WHERE id IN (5, 6);

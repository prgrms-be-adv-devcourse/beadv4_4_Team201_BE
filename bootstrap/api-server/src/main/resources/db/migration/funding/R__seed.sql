-- Funding seed data (dev/staging)
TRUNCATE TABLE funding_participant_members CASCADE;
TRUNCATE TABLE fundings CASCADE;

INSERT INTO fundings (
    id, version, wishlist_item_id, product_id, product_name, image_key, receiver_id,
    target_amount, current_amount, status,
    deadline, achieved_at, closed_at,
    created_at, updated_at, created_by, updated_by
)
VALUES
    (2, 1, 11, 38, '킨토 데이오프 텀블러', 'products/38/kinto-tumbler.jpg', 2,
     45000, 45000, 'ACHIEVED', '2026-03-14 04:46:27', '2026-02-27 04:46:27',
     '2026-02-27 04:46:27', '2026-02-27 04:46:27', '2026-02-27 04:46:27', NULL, NULL),

    (3, 2, 1, 17, '벨킨 3-in-1 맥세이프 충전기', 'products/17/belkin-3in1.jpg', 2,
     179000, 179000, 'ACCEPTED', '2026-03-14 04:46:27', '2026-02-27 04:46:27',
     '2026-02-27 04:46:27', '2026-02-27 04:46:27', '2026-02-27 04:46:27', NULL, NULL),

    (4, 1, 9, 13, '애플워치 시리즈 9', 'products/13/apple-watch.jpg', 2,
     599000, 59900, 'IN_PROGRESS', '2026-03-14 04:46:27', NULL,
     NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27', NULL, NULL),

    (5, 1, 24, 74, '모나미 153 ID 볼펜', 'products/74/monami-153id.jpg', 3,
     25000, 25000, 'ACHIEVED', '2026-03-14 04:46:27', '2026-02-27 04:46:27',
     NULL, '2026-02-27 04:46:27', '2026-02-27 04:46:27', NULL, NULL);

SELECT setval('fundings_id_seq', 100, false);

INSERT INTO funding_participant_members (id, funding_id, participant_id, nick_name, amount,
                                        created_at, updated_at, created_by, updated_by)
VALUES
    (2, 2, 3, '멍청한고양이2013', 45000, '2026-02-27 04:46:27', '2026-02-27 04:46:27', NULL, NULL),
    (3, 3, 3, '멍청한고양이2013', 179000, '2026-02-27 04:46:27', '2026-02-27 04:46:27', NULL, NULL),
    (4, 4, 3, '멍청한고양이2013', 59900, '2026-02-27 04:46:27', '2026-02-27 04:46:27', NULL, NULL),
    (5, 5, 2, '나른한고양이0013', 25000, '2026-02-27 04:46:27', '2026-02-27 04:46:27', NULL, NULL);

SELECT setval('funding_participant_members_id_seq', 100, false);

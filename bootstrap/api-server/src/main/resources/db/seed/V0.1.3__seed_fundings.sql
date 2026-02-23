-- =============================================================================
-- V1.2.3: Seed - Fundings, Funding Participants
-- =============================================================================

INSERT INTO funding (id, wishlist_item_id, product_id, receiver_id, target_amount, current_amount,
                     status, deadline, achieved_at, closed_at,
                     created_at, updated_at, created_by, updated_by)
VALUES
    -- 펀딩1: 진행중 (다이슨 에어랩, 목표 699000, 현재 15000)
    (1, 8, 4, 5, 699000, 15000, 'IN_PROGRESS',
     '2026-03-01 23:59:59', NULL, NULL,
     NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

    -- 펀딩2: 달성 완료 (에어팟 프로, 목표 359000, 달성)
    (2, 9, 1, 5, 359000, 359000, 'ACHIEVED',
     '2026-02-28 23:59:59', '2026-02-07 14:30:00', NULL,
     NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

    -- 펀딩3: 만료 (닌텐도, 목표 415000, 미달성)
    (3, 4, 3, 2, 415000, 120000, 'EXPIRED',
     '2026-02-01 23:59:59', NULL, '2026-02-01 23:59:59',
     NOW(), NOW(), 'SYSTEM', 'SYSTEM');

SELECT setval('funding_id_seq', (SELECT MAX(id) FROM funding));

INSERT INTO funding_participant_member (id, funding_id, participant_id, amount,
                                        created_at, updated_at, created_by, updated_by)
VALUES
    (1, 1, 2, 10000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (2, 1, 6, 5000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (3, 2, 2, 200000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (4, 2, 6, 159000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (5, 3, 5, 70000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (6, 3, 6, 50000, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

SELECT setval('funding_participant_member_id_seq', (SELECT MAX(id) FROM funding_participant_member));

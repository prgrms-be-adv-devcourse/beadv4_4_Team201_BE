-- Friendship seed data (dev/staging)
TRUNCATE TABLE friendships CASCADE;

INSERT INTO friendships (id, requester_id, receiver_id, status, accepted_at,
                         created_at, updated_at, created_by, updated_by)
VALUES (1, 2, 3, 'ACCEPTED', '2026-02-07 12:00:00', '2026-02-07 12:00:00', '2026-02-07 12:00:00', 'SYSTEM', 'SYSTEM'),
       (2, 2, 5, 'ACCEPTED', '2026-02-07 12:00:00', '2026-02-07 12:00:00', '2026-02-07 12:00:00', 'SYSTEM', 'SYSTEM'),
       (3, 5, 6, 'ACCEPTED', '2026-02-08 14:00:00', '2026-02-08 14:00:00', '2026-02-08 14:00:00', 'SYSTEM', 'SYSTEM');

SELECT setval('friendships_id_seq', 100, false);

-- Friendship seed data (dev/staging)
TRUNCATE TABLE friendships CASCADE;

INSERT INTO friendships (id, requester_id, receiver_id, status, accepted_at,
                         created_at, updated_at, created_by, updated_by)
VALUES (1, 2, 3, 'ACCEPTED', '2026-02-07 12:00:00', '2026-02-07 12:00:00', '2026-02-07 12:00:00', 'SYSTEM', 'SYSTEM'),
       (2, 2, 5, 'ACCEPTED', '2026-02-07 12:00:00', '2026-02-07 12:00:00', '2026-02-07 12:00:00', 'SYSTEM', 'SYSTEM'),
       (3, 5, 6, 'ACCEPTED', '2026-02-08 14:00:00', '2026-02-08 14:00:00', '2026-02-08 14:00:00', 'SYSTEM', 'SYSTEM');

SELECT setval('friendships_id_seq', 100, false);

-- Loadtest friendships (only in loadtest schema)
-- Givers(1001-1050) × Receivers(1051-1060) = 500 ACCEPTED friendships
DO $$
DECLARE
  _giver_id  bigint;
  _recv_id   bigint;
  _seq       bigint := 1001;
BEGIN
  IF current_schema() = 'loadtest' THEN
    FOR _giver_id IN 1001..1050 LOOP
      FOR _recv_id IN 1051..1060 LOOP
        INSERT INTO friendships (id, requester_id, receiver_id, status, accepted_at,
                                 created_at, updated_at, created_by, updated_by)
        VALUES (_seq, _giver_id, _recv_id, 'ACCEPTED',
                '2026-03-01 00:00:00', '2026-03-01 00:00:00', '2026-03-01 00:00:00',
                'SYSTEM', 'SYSTEM');
        _seq := _seq + 1;
      END LOOP;
    END LOOP;

    PERFORM setval('friendships_id_seq', 1600, false);
  END IF;
END
$$;

-- Giftify DEV DB Reset Script
-- Usage: psql -U giftify -d giftify_db -f reset-dev.sql
--
-- g7app: Flyway managed (business tables)
-- public: Framework auto-created (event_publication, BATCH_*)

CREATE SCHEMA IF NOT EXISTS public;
DROP SCHEMA IF EXISTS g7app CASCADE;

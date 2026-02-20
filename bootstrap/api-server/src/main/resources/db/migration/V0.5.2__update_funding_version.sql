-- =============================================================================
-- V0.5.2: Update funding version to be not-null
-- =============================================================================

-- 1. Set existing NULL versions to 0
UPDATE funding SET version = 0 WHERE version IS NULL;

-- 2. Alter column to be NOT NULL and set default value
ALTER TABLE funding ALTER COLUMN version SET NOT NULL;
ALTER TABLE funding ALTER COLUMN version SET DEFAULT 0;

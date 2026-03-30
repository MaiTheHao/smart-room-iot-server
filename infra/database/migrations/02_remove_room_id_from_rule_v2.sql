-- Migration: Remove room_id from rule_v2
-- Version: 2026-03-31
-- Description: Drop room_id foreign key, index, and column from rule_v2 table.
USE smart_room_iot;

SET FOREIGN_KEY_CHECKS = 0;

-- Drop foreign key constraint
ALTER TABLE rule_v2 DROP FOREIGN KEY fk_rule_v2_room;

-- Drop index
ALTER TABLE rule_v2 DROP INDEX idx_rule_v2_room;

-- Drop column
ALTER TABLE rule_v2 DROP COLUMN room_id;

SET FOREIGN_KEY_CHECKS = 1;

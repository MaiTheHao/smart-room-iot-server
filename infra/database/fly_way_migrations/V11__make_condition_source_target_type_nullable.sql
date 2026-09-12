-- =========================================================================
-- V11: Make condition.source_target_type nullable
-- -------------------------------------------------------------------------
-- source_target_type only applies to DEVICE/SENSOR sources. SYSTEM/ROOM
-- conditions have no target type, so the column must allow NULL.
-- =========================================================================

ALTER TABLE `condition` MODIFY COLUMN `source_target_type` varchar(50) NULL;

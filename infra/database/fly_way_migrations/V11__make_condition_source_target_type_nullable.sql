-- =========================================================================
-- V11: Make condition.source_target_type nullable & Remove motion_detector.code
-- -------------------------------------------------------------------------
-- 1. source_target_type only applies to DEVICE/SENSOR sources. SYSTEM/ROOM
--    conditions have no target type, so the column must allow NULL.
-- 2. motion_detector does not use 'code' field (identified by natural_id
--    like other IoT sensors). Drop the redundant column and unique constraint.
-- =========================================================================

ALTER TABLE `condition` MODIFY COLUMN `source_target_type` varchar(50) NULL;

-- Drop redundant 'code' column and constraint from motion_detector
ALTER TABLE `motion_detector` DROP INDEX IF EXISTS `uq_motion_detector_room_code`;
ALTER TABLE `motion_detector` DROP COLUMN IF EXISTS `code`;

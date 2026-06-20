-- =============================================================================
-- Migration: Modify Rule Action Alert columns from JSON to TEXT
-- Date       : 2026-06-20
-- Description: Changes columns `recipient_groups` and `channels` in `rule_action_alert`
--              table to TEXT type to resolve cross-db compatibility issues.
-- =============================================================================

ALTER TABLE `rule_action_alert`
    MODIFY COLUMN `recipient_groups` TEXT DEFAULT NULL COMMENT 'e.g. ["G_ADMIN","G_MAINTENANCE"]',
    MODIFY COLUMN `channels` TEXT DEFAULT NULL COMMENT 'e.g. ["PUSH","EMAIL","SMS"]';

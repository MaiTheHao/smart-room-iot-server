-- Migration 005: Standardize energy_metrics table
-- Compatibility: MySQL 8.x, MariaDB 11.x
-- Description: Rename 'category' to 'target_category' and update index.

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Drop old index
ALTER TABLE `energy_metrics` DROP INDEX `idx_energy_metrics_target`;

-- 2. Rename column
-- MySQL 8.0+ and MariaDB 10.5.2+ support RENAME COLUMN
-- This preserves column definition (type, nullability, comment, etc.)
ALTER TABLE `energy_metrics` RENAME COLUMN `category` TO `target_category`;

-- 3. Create new index on the new column name
ALTER TABLE `energy_metrics` ADD INDEX `idx_energy_metrics_target` (`target_category`, `target_id`, `timestamp`);

SET FOREIGN_KEY_CHECKS = 1;

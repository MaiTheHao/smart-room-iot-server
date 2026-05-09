-- ============================================================
-- Migration: 007_remove_generated_unix_minute.sql
-- Date      : 2026-05-09
-- Author    : antigravity
-- Scope     : Convert unix_minute from generated/trigger-based to regular column for energy_metrics only
-- ============================================================

-- 1. energy_metrics
ALTER TABLE `energy_metrics` MODIFY COLUMN `unix_minute` INT;

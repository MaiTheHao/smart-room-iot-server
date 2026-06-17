-- =============================================================================
-- Migration: Add `duration` column to `fan` table
-- Date       : 2026-06-17
-- Description: Fan entity now stores IR gap duration (µs) alongside specificType
--              so the gateway client can send it as part of every control request,
--              consistent with the AC device model.
-- =============================================================================

ALTER TABLE `fan`
    ADD COLUMN `duration` INT NULL DEFAULT NULL;

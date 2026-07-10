-- =============================================================================
-- Migration: Create Device Status Metrics Table
-- Date       : 2026-07-10
-- Description: Create the `device_status_metrics` table to store status snapshots.
-- =============================================================================

CREATE TABLE `device_status_metrics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_category` varchar(50) NOT NULL,
  `target_id` bigint NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  `status_data` text DEFAULT NULL,
  `unix_minute` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_device_status_metrics_target` (`target_category`, `target_id`, `timestamp`),
  KEY `idx_device_status_metrics_timestamp` (`timestamp`),
  KEY `idx_dsm_unix_minute` (`unix_minute`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

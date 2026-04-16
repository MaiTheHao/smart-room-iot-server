-- ============================================================
-- Migration: 001_add_energy_metrics_table.sql
-- Date      : 2026-04-16
-- Author    : system
-- Scope     : Add energy_metrics table for PZEM-004T 6-metric
--             time-series data (LIGHT, FAN, AC, ROOM categories)
-- ============================================================

CREATE TABLE IF NOT EXISTS `energy_metrics` (
  `id`           bigint        NOT NULL AUTO_INCREMENT,
  `category`     varchar(50)   NOT NULL COMMENT 'Enum: LIGHT, FAN, AC, ROOM',
  `target_id`    bigint        NOT NULL COMMENT 'lightId | fanId | acId | roomId depending on category',
  `timestamp`    datetime(6)   NOT NULL,
  `voltage`      double        DEFAULT NULL COMMENT 'V',
  `current`      double        DEFAULT NULL COMMENT 'A',
  `power`        double        DEFAULT NULL COMMENT 'W - instantaneous',
  `energy`       double        DEFAULT NULL COMMENT 'Wh - accumulated, reset daily at 00:00',
  `frequency`    double        DEFAULT NULL COMMENT 'Hz',
  `power_factor` double        DEFAULT NULL COMMENT '0.0 - 1.0',
  PRIMARY KEY (`id`),
  KEY `idx_energy_metrics_target`    (`category`, `target_id`, `timestamp`),
  KEY `idx_energy_metrics_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================================
-- V8: Complete and validate Motion Detector, Motion Lan, and Motion Metrics schema
-- =========================================================================

-- 1. Table: motion_detector
CREATE TABLE IF NOT EXISTS `motion_detector` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL DEFAULT 0,
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `natural_id` varchar(256) NOT NULL,
  `specific_type` varchar(256) DEFAULT NULL,
  `code` varchar(256) NULL,
  `current_motion` BOOLEAN DEFAULT NULL,
  `last_event_at` datetime(6) DEFAULT NULL,
  `hardware_config_id` bigint DEFAULT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_motion_detector_natural_id` (`natural_id`),
  UNIQUE KEY `uq_motion_detector_room_code` (`room_id`, `code`),
  UNIQUE KEY `idx_motion_detector_hardware_config_id` (`hardware_config_id`),
  KEY `idx_motion_detector_room_id` (`room_id`),
  KEY `idx_motion_detector_natural_id` (`natural_id`),
  CONSTRAINT `fk_motion_detector_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_motion_detector_hardware_config` FOREIGN KEY (`hardware_config_id`) REFERENCES `hardware_config` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Ensure all BaseIoTEntity and BaseAuditEntity columns exist if table was partially created previously
ALTER TABLE `motion_detector` ADD COLUMN IF NOT EXISTS `v` bigint NOT NULL DEFAULT 0;
ALTER TABLE `motion_detector` ADD COLUMN IF NOT EXISTS `is_active` BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE `motion_detector` ADD COLUMN IF NOT EXISTS `specific_type` varchar(256) DEFAULT NULL;
ALTER TABLE `motion_detector` ADD COLUMN IF NOT EXISTS `code` varchar(256) NULL;
ALTER TABLE `motion_detector` ADD COLUMN IF NOT EXISTS `hardware_config_id` bigint DEFAULT NULL;
ALTER TABLE `motion_detector` ADD UNIQUE INDEX IF NOT EXISTS `idx_motion_detector_hardware_config_id` (`hardware_config_id`);
ALTER TABLE `motion_detector` MODIFY COLUMN `created_by` varchar(256) DEFAULT NULL;
ALTER TABLE `motion_detector` MODIFY COLUMN `updated_by` varchar(256) DEFAULT NULL;

-- 2. Table: motion_detector_lan
CREATE TABLE IF NOT EXISTS `motion_detector_lan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL DEFAULT 0,
  `description` text,
  `lang_code` varchar(10) NOT NULL,
  `name` varchar(256) NOT NULL,
  `owner_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_mdl_owner_lang` (`owner_id`, `lang_code`),
  KEY `idx_mdl_owner_id` (`owner_id`),
  CONSTRAINT `fk_mdl_owner` FOREIGN KEY (`owner_id`) REFERENCES `motion_detector` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE `motion_detector_lan` ADD COLUMN IF NOT EXISTS `v` bigint NOT NULL DEFAULT 0;
ALTER TABLE `motion_detector_lan` MODIFY COLUMN `created_by` varchar(256) DEFAULT NULL;
ALTER TABLE `motion_detector_lan` MODIFY COLUMN `updated_by` varchar(256) DEFAULT NULL;

-- 3. Table: motion_metrics
CREATE TABLE IF NOT EXISTS `motion_metrics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_category` varchar(50) NOT NULL,
  `target_id` bigint NOT NULL,
  `motion_detected` BOOLEAN NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  `unix_minute` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_motion_metrics_target` (`target_category`, `target_id`, `timestamp`),
  KEY `idx_motion_metrics_timestamp` (`timestamp`),
  KEY `idx_motion_metrics_unix_minute` (`unix_minute`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Table: room_event
CREATE TABLE IF NOT EXISTS `room_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_room_event_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `room_event` (`code`, `description`)
VALUES ('MOTION_DETECTED', 'Motion detected event in room')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- 5. Table: room_event_config
CREATE TABLE IF NOT EXISTS `room_event_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL DEFAULT 0,
  `room_id` bigint NOT NULL,
  `room_event_id` bigint NOT NULL,
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `cooldown_seconds` int NOT NULL DEFAULT 0,
  `last_triggered_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_rec_room_event` (`room_id`, `room_event_id`),
  KEY `idx_rec_room_id` (`room_id`),
  CONSTRAINT `fk_rec_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rec_room_event` FOREIGN KEY (`room_event_id`) REFERENCES `room_event` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE `room_event_config` ADD COLUMN IF NOT EXISTS `v` bigint NOT NULL DEFAULT 0;
ALTER TABLE `room_event_config` MODIFY COLUMN `created_by` varchar(256) DEFAULT NULL;
ALTER TABLE `room_event_config` MODIFY COLUMN `updated_by` varchar(256) DEFAULT NULL;

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;
SET GLOBAL time_zone = '+00:00';
SET time_zone = '+00:00';

-- ============================================
-- Step 1: Create new condition table
-- ============================================
CREATE TABLE `condition` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL DEFAULT 0,
  
  -- Owner fields
  `owner_category` varchar(50) NOT NULL,
  `owner_id` varchar(256) NOT NULL,
  
  -- Source fields
  `source_category` varchar(50) NOT NULL,
  `source_target_id` varchar(256) DEFAULT NULL,
  `source_target_type` varchar(50) DEFAULT NULL,
  
  -- Evaluation fields
  `property` varchar(100) NOT NULL,
  `operator` varchar(10) NOT NULL,
  `value` varchar(256) NOT NULL,
  `extra_params` text DEFAULT NULL,
  
  -- Order and logic
  `sort_order` int NOT NULL DEFAULT 0,
  `next_logic` varchar(10) DEFAULT NULL,
  
  PRIMARY KEY (`id`),
  KEY `idx_condition_owner` (`owner_category`, `owner_id`),
  KEY `idx_condition_source` (`source_category`, `source_target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Step 2: Create new action table
-- ============================================
CREATE TABLE `action` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL DEFAULT 0,
  
  -- Owner fields
  `owner_category` varchar(50) NOT NULL,
  `owner_id` varchar(256) NOT NULL,
  
  -- Target & Action Settings
  `target_category` varchar(50) NOT NULL,
  `target_id` varchar(256) NOT NULL,
  `params` text DEFAULT NULL,
  `execution_order` int NOT NULL DEFAULT 0,
  
  PRIMARY KEY (`id`),
  KEY `idx_action_owner` (`owner_category`, `owner_id`),
  KEY `idx_action_target` (`target_category`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Step 3: Migrate data from rule_condition to condition
-- ============================================
INSERT INTO `condition` (
  `created_at`,
  `created_by`,
  `updated_at`,
  `updated_by`,
  `v`,
  `owner_category`,
  `owner_id`,
  `source_category`,
  `source_target_id`,
  `source_target_type`,
  `property`,
  `operator`,
  `value`,
  `extra_params`,
  `sort_order`,
  `next_logic`
)
SELECT 
  rc.`created_at`,
  rc.`created_by`,
  rc.`updated_at`,
  rc.`updated_by`,
  rc.`v`,
  'RULE' AS `owner_category`,
  CAST(rc.`rule_id` AS CHAR) AS `owner_id`,
  rc.`data_source` AS `source_category`,
  COALESCE(
    JSON_UNQUOTE(JSON_EXTRACT(rc.`resource_param`, '$.roomId')),
    JSON_UNQUOTE(JSON_EXTRACT(rc.`resource_param`, '$.deviceId')),
    JSON_UNQUOTE(JSON_EXTRACT(rc.`resource_param`, '$.sensorId'))
  ) AS `source_target_id`,
  JSON_UNQUOTE(JSON_EXTRACT(rc.`resource_param`, '$.category')) AS `source_target_type`,
  JSON_UNQUOTE(JSON_EXTRACT(rc.`resource_param`, '$.property')) AS `property`,
  rc.`operator`,
  rc.`value_param` AS `value`,
  NULL AS `extra_params`,
  rc.`sort_order`,
  rc.`next_logic`
FROM `rule_condition` rc
WHERE rc.`rule_id` IS NOT NULL
  AND rc.`data_source` IS NOT NULL
  AND rc.`operator` IS NOT NULL
  AND rc.`value_param` IS NOT NULL;

-- ============================================
-- Step 4: Migrate data from rule_action to action
-- ============================================
INSERT INTO `action` (
  `created_at`,
  `created_by`,
  `updated_at`,
  `updated_by`,
  `v`,
  `owner_category`,
  `owner_id`,
  `target_category`,
  `target_id`,
  `params`,
  `execution_order`
)
SELECT 
  ra.`created_at`,
  ra.`created_by`,
  ra.`updated_at`,
  ra.`updated_by`,
  ra.`v`,
  'RULE' AS `owner_category`,
  CAST(ra.`rule_id` AS CHAR) AS `owner_id`,
  ra.`target_device_category` AS `target_category`,
  CAST(ra.`target_device_id` AS CHAR) AS `target_id`,
  ra.`action_params` AS `params`,
  ra.`execution_order`
FROM `rule_action` ra
WHERE ra.`rule_id` IS NOT NULL
  AND ra.`target_device_id` IS NOT NULL
  AND ra.`target_device_category` IS NOT NULL;

-- ============================================
-- Step 5: Drop old tables
-- ============================================
DROP TABLE IF EXISTS `rule_condition`;
DROP TABLE IF EXISTS `rule_action`;

SET FOREIGN_KEY_CHECKS = 1;
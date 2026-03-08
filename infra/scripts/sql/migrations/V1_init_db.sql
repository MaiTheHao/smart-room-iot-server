USE smart_room_iot;

SET
  FOREIGN_KEY_CHECKS = 0;

SET NAMES utf8;

SET
  TIME_ZONE = '+00:00';

CREATE TABLE
  `air_condition` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `is_active` bit(1) NOT NULL,
    `natural_id` varchar(256) NOT NULL,
    `fan_speed` int DEFAULT NULL,
    `mode` varchar(256) DEFAULT NULL,
    `power` varchar(256) DEFAULT NULL,
    `swing` varchar(256) DEFAULT NULL,
    `temperature` int DEFAULT NULL,
    `device_control_id` bigint DEFAULT NULL,
    `room_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_air_condition_natural_id` (`natural_id`),
    UNIQUE KEY `idx_air_condition_device_control_id` (`device_control_id`),
    KEY `idx_air_condition_room_id` (`room_id`),
    CONSTRAINT `fk_air_condition_device_control` FOREIGN KEY (`device_control_id`) REFERENCES `device_control` (`id`),
    CONSTRAINT `fk_air_condition_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 2 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `air_condition_lan` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `description` text,
    `lang_code` varchar(10) NOT NULL,
    `name` varchar(256) NOT NULL,
    `owner_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_air_condition_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
    CONSTRAINT `fk_air_condition_lan_air_condition` FOREIGN KEY (`owner_id`) REFERENCES `air_condition` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 2 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `fan` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `is_active` bit(1) NOT NULL,
    `natural_id` varchar(256) NOT NULL,
    `power` varchar(256) DEFAULT NULL,
    `type` varchar(256) NOT NULL,
    `speed` int DEFAULT NULL,
    `mode` varchar(256) DEFAULT NULL,
    `light` varchar(256) DEFAULT NULL,
    `swing` varchar(256) DEFAULT NULL,
    `device_control_id` bigint DEFAULT NULL,
    `room_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_fan_natural_id` (`natural_id`),
    UNIQUE KEY `idx_fan_device_control_id` (`device_control_id`),
    KEY `idx_fan_room_id` (`room_id`),
    CONSTRAINT `fk_fan_device_control` FOREIGN KEY (`device_control_id`) REFERENCES `device_control` (`id`),
    CONSTRAINT `fk_fan_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `fan_lan` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `description` text,
    `lang_code` varchar(10) NOT NULL,
    `name` varchar(256) NOT NULL,
    `owner_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_fan_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
    CONSTRAINT `fk_fan_lan_fan` FOREIGN KEY (`owner_id`) REFERENCES `fan` (`id`)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `automation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `cron_expression` varchar(256) NOT NULL,
    `description` varchar(256) DEFAULT NULL,
    `is_active` bit(1) DEFAULT NULL,
    `name` varchar(256) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_auto_status` (`is_active`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 20 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `automation_action` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `action_type` varchar(256) NOT NULL,
    `execution_order` int DEFAULT NULL,
    `parameter_value` varchar(256) DEFAULT NULL,
    `target_id` bigint NOT NULL,
    `target_type` varchar(256) DEFAULT NULL,
    `automation_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_automation_action_automation_id` (`automation_id`),
    CONSTRAINT `fk_automation_action_automation` FOREIGN KEY (`automation_id`) REFERENCES `automation` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 9 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `client` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `avatar_url` varchar(256) DEFAULT NULL,
    `client_type` varchar(256) DEFAULT NULL,
    `ip_address` varchar(45) DEFAULT NULL,
    `last_login_at` datetime(6) DEFAULT NULL,
    `mac_address` varchar(100) DEFAULT NULL,
    `password_hash` varchar(256) DEFAULT NULL,
    `username` varchar(100) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_username` (`username`),
    UNIQUE KEY `idx_client_type_ip_address` (`client_type`, `ip_address`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 9 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `client_group` (
    `client_id` bigint NOT NULL,
    `group_id` bigint NOT NULL,
    PRIMARY KEY (`client_id`, `group_id`),
    KEY `idx_client_group_group_id` (`group_id`),
    CONSTRAINT `fk_client_group_sys_group` FOREIGN KEY (`group_id`) REFERENCES `sys_group` (`id`),
    CONSTRAINT `fk_client_group_client` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `device_control` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `api_endpoint` varchar(256) DEFAULT NULL,
    `ble_mac_address` varchar(100) DEFAULT NULL,
    `device_control_type` varchar(256) NOT NULL,
    `gpio_pin` int NOT NULL,
    `client_id` bigint NOT NULL,
    `room_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_device_control_client_gpio_ble` (`client_id`, `gpio_pin`, `ble_mac_address`),
    KEY `idx_device_control_room_id` (`room_id`),
    CONSTRAINT `fk_device_control_client` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`),
    CONSTRAINT `fk_device_control_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 14 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `floor` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `code` varchar(256) NOT NULL,
    `level` int DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_floor_code` (`code`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 4 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `floor_lan` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `description` text,
    `lang_code` varchar(10) NOT NULL,
    `name` varchar(256) NOT NULL,
    `owner_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_floor_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
    CONSTRAINT `fk_floor_lan_floor` FOREIGN KEY (`owner_id`) REFERENCES `floor` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 7 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `language` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `code` varchar(10) NOT NULL,
    `description` varchar(256) DEFAULT NULL,
    `name` varchar(100) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_language_code` (`code`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 3 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `light` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `is_active` bit(1) NOT NULL,
    `natural_id` varchar(256) NOT NULL,
    `power` varchar(256) DEFAULT NULL,
    `level` int DEFAULT NULL,
    `device_control_id` bigint DEFAULT NULL,
    `room_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_light_natural_id` (`natural_id`),
    UNIQUE KEY `idx_light_device_control_id` (`device_control_id`),
    KEY `idx_light_room_id` (`room_id`),
    CONSTRAINT `fk_light_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`),
    CONSTRAINT `fk_light_device_control` FOREIGN KEY (`device_control_id`) REFERENCES `device_control` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 8 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `light_lan` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `description` text,
    `lang_code` varchar(10) NOT NULL,
    `name` varchar(256) NOT NULL,
    `owner_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_light_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
    CONSTRAINT `fk_light_lan_light` FOREIGN KEY (`owner_id`) REFERENCES `light` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 8 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `persistent_logins` (`series` varchar(64) NOT NULL, `last_used` datetime(6) NOT NULL, `token` varchar(64) NOT NULL, `username` varchar(64) NOT NULL, PRIMARY KEY (`series`)) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `power_consumption` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `is_active` bit(1) NOT NULL,
    `natural_id` varchar(256) NOT NULL,
    `current_watt` double DEFAULT NULL,
    `device_control_id` bigint DEFAULT NULL,
    `room_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_power_consumption_natural_id` (`natural_id`),
    UNIQUE KEY `idx_power_consumption_device_control_id` (`device_control_id`),
    KEY `idx_power_consumption_room_id` (`room_id`),
    CONSTRAINT `fk_power_consumption_device_control` FOREIGN KEY (`device_control_id`) REFERENCES `device_control` (`id`),
    CONSTRAINT `fk_power_consumption_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 4 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `power_consumption_lan` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `description` text,
    `lang_code` varchar(10) NOT NULL,
    `name` varchar(256) NOT NULL,
    `owner_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_power_consumption_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
    CONSTRAINT `fk_power_consumption_lan_power_consumption` FOREIGN KEY (`owner_id`) REFERENCES `power_consumption` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 4 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `power_consumption_value` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `timestamp` datetime(6) NOT NULL,
    `watt` double DEFAULT NULL,
    `sensor_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_sensor_timestamp` (`sensor_id`, `timestamp`),
    KEY `idx_timestamp` (`timestamp`),
    CONSTRAINT `fk_power_consumption_value_power_consumption` FOREIGN KEY (`sensor_id`) REFERENCES `power_consumption` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 593 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `QRTZ_BLOB_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `BLOB_DATA` blob,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `QRTZ_CALENDARS` (`SCHED_NAME` varchar(120) NOT NULL, `CALENDAR_NAME` varchar(200) NOT NULL, `CALENDAR` blob NOT NULL, PRIMARY KEY (`SCHED_NAME`, `CALENDAR_NAME`)) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `QRTZ_CRON_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `CRON_EXPRESSION` varchar(120) NOT NULL,
    `TIME_ZONE_ID` varchar(80) DEFAULT NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `QRTZ_FIRED_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `ENTRY_ID` varchar(95) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `INSTANCE_NAME` varchar(200) NOT NULL,
    `FIRED_TIME` bigint NOT NULL,
    `SCHED_TIME` bigint NOT NULL,
    `PRIORITY` int NOT NULL,
    `STATE` varchar(16) NOT NULL,
    `JOB_NAME` varchar(200) DEFAULT NULL,
    `JOB_GROUP` varchar(200) DEFAULT NULL,
    `IS_NONCONCURRENT` tinyint(1) DEFAULT NULL,
    `REQUESTS_RECOVERY` tinyint(1) DEFAULT NULL,
    PRIMARY KEY (`SCHED_NAME`, `ENTRY_ID`)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `QRTZ_JOB_DETAILS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `JOB_NAME` varchar(200) NOT NULL,
    `JOB_GROUP` varchar(200) NOT NULL,
    `DESCRIPTION` varchar(250) DEFAULT NULL,
    `JOB_CLASS_NAME` varchar(250) NOT NULL,
    `IS_DURABLE` tinyint(1) NOT NULL,
    `IS_NONCONCURRENT` tinyint(1) NOT NULL,
    `IS_UPDATE_DATA` tinyint(1) NOT NULL,
    `REQUESTS_RECOVERY` tinyint(1) NOT NULL,
    `JOB_DATA` blob,
    PRIMARY KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `QRTZ_LOCKS` (`SCHED_NAME` varchar(120) NOT NULL, `LOCK_NAME` varchar(40) NOT NULL, PRIMARY KEY (`SCHED_NAME`, `LOCK_NAME`)) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `QRTZ_PAUSED_TRIGGER_GRPS` (`SCHED_NAME` varchar(120) NOT NULL, `TRIGGER_GROUP` varchar(200) NOT NULL, PRIMARY KEY (`SCHED_NAME`, `TRIGGER_GROUP`)) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `QRTZ_SCHEDULER_STATE` (`SCHED_NAME` varchar(120) NOT NULL, `INSTANCE_NAME` varchar(200) NOT NULL, `LAST_CHECKIN_TIME` bigint NOT NULL, `CHECKIN_INTERVAL` bigint NOT NULL, PRIMARY KEY (`SCHED_NAME`, `INSTANCE_NAME`)) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `QRTZ_SIMPLE_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `REPEAT_COUNT` bigint NOT NULL,
    `REPEAT_INTERVAL` bigint NOT NULL,
    `TIMES_TRIGGERED` bigint NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `QRTZ_SIMPROP_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `STR_PROP_1` varchar(512) DEFAULT NULL,
    `STR_PROP_2` varchar(512) DEFAULT NULL,
    `STR_PROP_3` varchar(512) DEFAULT NULL,
    `INT_PROP_1` int DEFAULT NULL,
    `INT_PROP_2` int DEFAULT NULL,
    `LONG_PROP_1` bigint DEFAULT NULL,
    `LONG_PROP_2` bigint DEFAULT NULL,
    `DEC_PROP_1` decimal(13, 4) DEFAULT NULL,
    `DEC_PROP_2` decimal(13, 4) DEFAULT NULL,
    `BOOL_PROP_1` tinyint(1) DEFAULT NULL,
    `BOOL_PROP_2` tinyint(1) DEFAULT NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `QRTZ_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `JOB_NAME` varchar(200) NOT NULL,
    `JOB_GROUP` varchar(200) NOT NULL,
    `DESCRIPTION` varchar(250) DEFAULT NULL,
    `NEXT_FIRE_TIME` bigint DEFAULT NULL,
    `PREV_FIRE_TIME` bigint DEFAULT NULL,
    `PRIORITY` int DEFAULT NULL,
    `TRIGGER_STATE` varchar(16) NOT NULL,
    `TRIGGER_TYPE` varchar(8) NOT NULL,
    `START_TIME` bigint NOT NULL,
    `END_TIME` bigint DEFAULT NULL,
    `CALENDAR_NAME` varchar(200) DEFAULT NULL,
    `MISFIRE_INSTR` smallint DEFAULT NULL,
    `JOB_DATA` blob,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    KEY `SCHED_NAME` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`),
    CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `QRTZ_JOB_DETAILS` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `room` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `code` varchar(256) NOT NULL,
    `floor_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_room_code` (`code`),
    KEY `idx_room_floor_id` (`floor_id`),
    CONSTRAINT `fk_room_floor` FOREIGN KEY (`floor_id`) REFERENCES `floor` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 4 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `room_lan` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `description` text,
    `lang_code` varchar(10) NOT NULL,
    `name` varchar(256) NOT NULL,
    `owner_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_room_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
    CONSTRAINT `fk_room_lan_room` FOREIGN KEY (`owner_id`) REFERENCES `room` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 7 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `rule` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `name` varchar(256) NOT NULL,
    `priority` int NOT NULL,
    `is_active` bit(1) NOT NULL DEFAULT(1),
    `room_id` bigint NOT NULL,
    `target_device_id` bigint NOT NULL,
    `target_device_category` varchar(256) NOT NULL COMMENT 'Enum: AIR_CONDITION, LIGHT',
    `action_params` text DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_rule_room` (`room_id`),
    KEY `idx_rule_status` (`is_active`),
    CONSTRAINT `fk_rule_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `rule_condition` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `rule_id` bigint NOT NULL,
    `sort_order` int NOT NULL,
    `data_source` varchar(256) NOT NULL COMMENT 'Enum: SYSTEM, ROOM, DEVICE, SENSOR',
    `resource_param` text DEFAULT NULL COMMENT 'JSON: { "deviceId": 1, "category": "FAN", "property": "level" }',
    `operator` varchar(5) NOT NULL,
    `value_param` varchar(256) NOT NULL,
    `next_logic` varchar(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_rule_condition_rule_id` (`rule_id`),
    CONSTRAINT `fk_rule_condition_rule` FOREIGN KEY (`rule_id`) REFERENCES `rule` (`id`)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `sys_function` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `function_code` varchar(256) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_sys_function_code` (`function_code`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 207 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `sys_function_lan` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `description` text,
    `lang_code` varchar(10) NOT NULL,
    `name` varchar(256) NOT NULL,
    `owner_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_sys_function_lan_owner_lang` (`owner_id`, `lang_code`),
    CONSTRAINT `fk_sys_function_lan_sys_function` FOREIGN KEY (`owner_id`) REFERENCES `sys_function` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 18 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `sys_group` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `group_code` varchar(100) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_sys_group_code` (`group_code`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 10 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `sys_group_lan` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `description` text,
    `lang_code` varchar(10) NOT NULL,
    `name` varchar(256) NOT NULL,
    `owner_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_sys_group_lan_owner_lang` (`owner_id`, `lang_code`),
    CONSTRAINT `fk_sys_group_lan_sys_group` FOREIGN KEY (`owner_id`) REFERENCES `sys_group` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 11 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `sys_role` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `function_id` bigint NOT NULL,
    `group_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_sys_role_group_function` (`group_id`, `function_id`),
    KEY `idx_sys_role_function_id` (`function_id`),
    CONSTRAINT `fk_sys_role_sys_group` FOREIGN KEY (`group_id`) REFERENCES `sys_group` (`id`),
    CONSTRAINT `fk_sys_role_sys_function` FOREIGN KEY (`function_id`) REFERENCES `sys_function` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 23 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `temperature` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `is_active` bit(1) NOT NULL,
    `natural_id` varchar(256) NOT NULL,
    `current_value` double DEFAULT NULL,
    `device_control_id` bigint DEFAULT NULL,
    `room_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_temperature_natural_id` (`natural_id`),
    UNIQUE KEY `idx_temperature_device_control_id` (`device_control_id`),
    KEY `idx_temperature_room_id` (`room_id`),
    CONSTRAINT `fk_temperature_device_control` FOREIGN KEY (`device_control_id`) REFERENCES `device_control` (`id`),
    CONSTRAINT `fk_temperature_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 4 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `temperature_lan` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `description` text,
    `lang_code` varchar(10) NOT NULL,
    `name` varchar(256) NOT NULL,
    `owner_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_temperature_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
    CONSTRAINT `fk_temperature_lan_temperature` FOREIGN KEY (`owner_id`) REFERENCES `temperature` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 4 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE
  `temperature_value` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `timestamp` datetime(6) NOT NULL,
    `temp_c` double DEFAULT NULL,
    `sensor_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_sensor_timestamp` (`sensor_id`, `timestamp`),
    KEY `idx_timestamp` (`timestamp`),
    CONSTRAINT `fk_temperature_value_temperature` FOREIGN KEY (`sensor_id`) REFERENCES `temperature` (`id`)
  ) ENGINE = InnoDB AUTO_INCREMENT = 593 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

SET
  FOREIGN_KEY_CHECKS = 1;
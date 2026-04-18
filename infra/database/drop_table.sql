USE smart_room_iot;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Module: System & Auth
-- ----------------------------
DROP TABLE IF EXISTS `client`;
DROP TABLE IF EXISTS `client_group`;
DROP TABLE IF EXISTS `sys_group`;
DROP TABLE IF EXISTS `sys_group_lan`;
DROP TABLE IF EXISTS `sys_function`;
DROP TABLE IF EXISTS `sys_function_lan`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `persistent_logins`;
DROP TABLE IF EXISTS `language`;

-- ----------------------------
-- Module: Core Domain
-- ----------------------------
DROP TABLE IF EXISTS `floor`;
DROP TABLE IF EXISTS `floor_lan`;
DROP TABLE IF EXISTS `room`;
DROP TABLE IF EXISTS `room_lan`;
DROP TABLE IF EXISTS `device_control`;

-- ----------------------------
-- Module: Devices & Sensors
-- ----------------------------
DROP TABLE IF EXISTS `air_condition`;
DROP TABLE IF EXISTS `air_condition_lan`;
DROP TABLE IF EXISTS `light`;
DROP TABLE IF EXISTS `light_lan`;
DROP TABLE IF EXISTS `temperature`;
DROP TABLE IF EXISTS `temperature_lan`;
DROP TABLE IF EXISTS `temperature_value`;
DROP TABLE IF EXISTS `power_consumption`;
DROP TABLE IF EXISTS `power_consumption_lan`;
DROP TABLE IF EXISTS `power_consumption_value`;
DROP TABLE IF EXISTS `energy_metrics`;
DROP TABLE IF EXISTS `fan`;
DROP TABLE IF EXISTS `fan_lan`;

-- ----------------------------
-- Module: Automation & Rules
-- ----------------------------
DROP TABLE IF EXISTS `automation`;
DROP TABLE IF EXISTS `automation_action`;
DROP TABLE IF EXISTS `rule`;
DROP TABLE IF EXISTS `rule_condition`;
DROP TABLE IF EXISTS `rule_v2`;
DROP TABLE IF EXISTS `rule_condition_v2`;
DROP TABLE IF EXISTS `rule_action_v2`;

-- ----------------------------
-- Module: Quartz Scheduler
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_BLOB_TRIGGERS`;
DROP TABLE IF EXISTS `QRTZ_CALENDARS`;
DROP TABLE IF EXISTS `QRTZ_CRON_TRIGGERS`;
DROP TABLE IF EXISTS `QRTZ_FIRED_TRIGGERS`;
DROP TABLE IF EXISTS `QRTZ_JOB_DETAILS`;
DROP TABLE IF EXISTS `QRTZ_LOCKS`;
DROP TABLE IF EXISTS `QRTZ_PAUSED_TRIGGER_GRPS`;
DROP TABLE IF EXISTS `QRTZ_SCHEDULER_STATE`;
DROP TABLE IF EXISTS `QRTZ_SIMPLE_TRIGGERS`;
DROP TABLE IF EXISTS `QRTZ_SIMPROP_TRIGGERS`;
DROP TABLE IF EXISTS `QRTZ_TRIGGERS`;

SET FOREIGN_KEY_CHECKS = 1;
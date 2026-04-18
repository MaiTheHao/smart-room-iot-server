USE smart_room_iot;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Module: System & Auth
-- ----------------------------
TRUNCATE TABLE `client`;
TRUNCATE TABLE `client_group`;
TRUNCATE TABLE `sys_group`;
TRUNCATE TABLE `sys_group_lan`;
TRUNCATE TABLE `sys_function`;
TRUNCATE TABLE `sys_function_lan`;
TRUNCATE TABLE `sys_role`;
TRUNCATE TABLE `persistent_logins`;
TRUNCATE TABLE `language`;

-- ----------------------------
-- Module: Core Domain
-- ----------------------------
TRUNCATE TABLE `floor`;
TRUNCATE TABLE `floor_lan`;
TRUNCATE TABLE `room`;
TRUNCATE TABLE `room_lan`;
TRUNCATE TABLE `device_control`;

-- ----------------------------
-- Module: Devices & Sensors
-- ----------------------------
TRUNCATE TABLE `air_condition`;
TRUNCATE TABLE `air_condition_lan`;
TRUNCATE TABLE `light`;
TRUNCATE TABLE `light_lan`;
TRUNCATE TABLE `fan`;
TRUNCATE TABLE `fan_lan`;
TRUNCATE TABLE `temperature`;
TRUNCATE TABLE `temperature_lan`;
TRUNCATE TABLE `temperature_value`;
TRUNCATE TABLE `power_consumption`;
TRUNCATE TABLE `power_consumption_lan`;
TRUNCATE TABLE `power_consumption_value`;
TRUNCATE TABLE `energy_metrics`;

-- ----------------------------
-- Module: Automation & Rules
-- ----------------------------
TRUNCATE TABLE `automation`;
TRUNCATE TABLE `automation_action`;
TRUNCATE TABLE `rule`;
TRUNCATE TABLE `rule_condition`;
TRUNCATE TABLE `rule_v2`;
TRUNCATE TABLE `rule_condition_v2`;
TRUNCATE TABLE `rule_action_v2`;

-- ----------------------------
-- Module: Quartz Scheduler
-- ----------------------------
TRUNCATE TABLE `QRTZ_BLOB_TRIGGERS`;
TRUNCATE TABLE `QRTZ_CALENDARS`;
TRUNCATE TABLE `QRTZ_CRON_TRIGGERS`;
TRUNCATE TABLE `QRTZ_FIRED_TRIGGERS`;
TRUNCATE TABLE `QRTZ_JOB_DETAILS`;
TRUNCATE TABLE `QRTZ_LOCKS`;
TRUNCATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS`;
TRUNCATE TABLE `QRTZ_SCHEDULER_STATE`;
TRUNCATE TABLE `QRTZ_SIMPLE_TRIGGERS`;
TRUNCATE TABLE `QRTZ_SIMPROP_TRIGGERS`;
TRUNCATE TABLE `QRTZ_TRIGGERS`;

SET FOREIGN_KEY_CHECKS = 1;
USE smart_room_iot;

SET
  FOREIGN_KEY_CHECKS = 0;

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

TRUNCATE TABLE `sys_client_function_cache`;

TRUNCATE TABLE `persistent_logins`;

TRUNCATE TABLE `language`;

-- ----------------------------
-- Module: Core Domain (Building & Devices)
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

TRUNCATE TABLE `temperature`;

TRUNCATE TABLE `temperature_lan`;

TRUNCATE TABLE `temperature_value`;

TRUNCATE TABLE `power_consumption`;

TRUNCATE TABLE `power_consumption_lan`;

TRUNCATE TABLE `power_consumption_value`;

-- ----------------------------
-- Module: Automation
-- ----------------------------
TRUNCATE TABLE `automation`;

TRUNCATE TABLE `automation_action`;

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

SET
  FOREIGN_KEY_CHECKS = 1;
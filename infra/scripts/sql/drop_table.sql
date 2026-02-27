USE smart_room_iot;

SET
  FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Xóa bảng Module System
-- ----------------------------
DROP TABLE IF EXISTS `client`;

DROP TABLE IF EXISTS `client_group`;

DROP TABLE IF EXISTS `sys_group`;

DROP TABLE IF EXISTS `sys_group_lan`;

DROP TABLE IF EXISTS `sys_function`;

DROP TABLE IF EXISTS `sys_function_lan`;

DROP TABLE IF EXISTS `sys_role`;

DROP TABLE IF EXISTS `sys_client_function_cache`;

DROP TABLE IF EXISTS `persistent_logins`;

DROP TABLE IF EXISTS `language`;

-- ----------------------------
-- Xóa bảng Module Core
-- ----------------------------
DROP TABLE IF EXISTS `floor`;

DROP TABLE IF EXISTS `floor_lan`;

DROP TABLE IF EXISTS `room`;

DROP TABLE IF EXISTS `room_lan`;

DROP TABLE IF EXISTS `device_control`;

-- ----------------------------
-- Xóa bảng Module Devices
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

DROP TABLE IF EXISTS `fan`;

DROP TABLE IF EXISTS `fan_lan`;

-- ----------------------------
-- Xóa bảng Module Automation
-- ----------------------------
DROP TABLE IF EXISTS `automation`;

DROP TABLE IF EXISTS `automation_action`;

-- ----------------------------
-- Xóa bảng Module Rule
-- ----------------------------
DROP TABLE IF EXISTS `rule`;

DROP TABLE IF EXISTS `rule_condition`;

-- ----------------------------
-- Xóa bảng Module Quartz
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

SET
  FOREIGN_KEY_CHECKS = 1;
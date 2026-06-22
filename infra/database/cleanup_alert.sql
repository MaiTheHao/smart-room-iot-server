USE smart_room_iot;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `alert_instance_group`;
DROP TABLE IF EXISTS `alert_instance_log`;
DROP TABLE IF EXISTS `alert_instance`;
DROP TABLE IF EXISTS `alert_config_group`;
DROP TABLE IF EXISTS `alert_config`;
DROP TABLE IF EXISTS `alert_recipient_group`;
DROP TABLE IF EXISTS `alert_recipient`;
DROP TABLE IF EXISTS `alert_incident_log`;
DROP TABLE IF EXISTS `rule_action_alert`;

SET FOREIGN_KEY_CHECKS = 1;

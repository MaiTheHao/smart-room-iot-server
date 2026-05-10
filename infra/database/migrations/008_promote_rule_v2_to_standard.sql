-- Migration 008: Promote Rule V2 to Standard Rule Engine (Fixed for MariaDB Compatibility)
-- Decommission Rule V1 and rebuild Rule V2 with standard names.

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Drop Rule V1 tables (Dọn dẹp bản cũ)
DROP TABLE IF EXISTS `rule_condition`;
DROP TABLE IF EXISTS `rule`;

-- 2. Rename Rule V2 tables to standard names (Đổi tên bảng)
-- Lưu ý: Nếu bạn đã chạy script trước đó và lệnh RENAME đã thành công, 
-- bạn có thể bỏ qua bước này nếu nó báo lỗi "Table already exists".
RENAME TABLE `rule_v2` TO `rule`;
RENAME TABLE `rule_condition_v2` TO `rule_condition`;
RENAME TABLE `rule_action_v2` TO `rule_action`;

-- 3. Update columns (Đổi tên cột khóa ngoại)
ALTER TABLE `rule_condition` CHANGE COLUMN `rule_v2_id` `rule_id` bigint NOT NULL;
ALTER TABLE `rule_action` CHANGE COLUMN `rule_v2_id` `rule_id` bigint NOT NULL;

-- 4. Rebuild Indexes (Xóa index cũ và tạo index mới với tên chuẩn)
ALTER TABLE `rule` DROP INDEX IF EXISTS `idx_rule_v2_status`;
ALTER TABLE `rule` ADD INDEX `idx_rule_status` (`is_active`);

ALTER TABLE `rule_condition` DROP INDEX IF EXISTS `idx_rule_condition_v2_rule_id`;
ALTER TABLE `rule_condition` ADD INDEX `idx_rule_condition_rule_id` (`rule_id`);

ALTER TABLE `rule_action` DROP INDEX IF EXISTS `idx_rule_action_v2_rule_id`;
ALTER TABLE `rule_action` ADD INDEX `idx_rule_action_rule_id` (`rule_id`);

ALTER TABLE `rule_action` DROP INDEX IF EXISTS `idx_rule_action_v2_target_device`;
ALTER TABLE `rule_action` ADD INDEX `idx_rule_action_target_device` (`target_device_id`);

-- 5. Rebuild Constraints (Xóa khóa ngoại cũ và tạo khóa ngoại mới với tên chuẩn)
-- Chúng ta dùng DROP và ADD thay vì RENAME để tương thích với MariaDB cũ.
ALTER TABLE `rule_condition` DROP FOREIGN KEY IF EXISTS `fk_rule_condition_v2_rule`;
ALTER TABLE `rule_condition` ADD CONSTRAINT `fk_rule_condition_rule` 
  FOREIGN KEY (`rule_id`) REFERENCES `rule` (`id`) ON DELETE CASCADE;

ALTER TABLE `rule_action` DROP FOREIGN KEY IF EXISTS `fk_rule_action_v2_rule`;
ALTER TABLE `rule_action` ADD CONSTRAINT `fk_rule_action_rule` 
  FOREIGN KEY (`rule_id`) REFERENCES `rule` (`id`) ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;

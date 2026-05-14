-- Migration: Update unix_minute type and calculate values from timestamp

SET time_zone = '+00:00';
SET FOREIGN_KEY_CHECKS = 0;

-- 2. Xử lý dữ liệu cho bảng temperature_value
UPDATE `temperature_value` 
SET `unix_minute` = UNIX_TIMESTAMP(`timestamp`) DIV 60 
WHERE `unix_minute` IS NULL OR `unix_minute` = 0;

ALTER TABLE `temperature_value` 
MODIFY COLUMN `unix_minute` BIGINT NOT NULL;


-- 3. Xử lý dữ liệu cho bảng power_consumption_value
UPDATE `power_consumption_value` 
SET `unix_minute` = UNIX_TIMESTAMP(`timestamp`) DIV 60 
WHERE `unix_minute` IS NULL OR `unix_minute` = 0;

ALTER TABLE `power_consumption_value` 
MODIFY COLUMN `unix_minute` BIGINT NOT NULL;


-- 4. Xử lý dữ liệu cho bảng energy_metrics
UPDATE `energy_metrics` 
SET `unix_minute` = UNIX_TIMESTAMP(`timestamp`) DIV 60 
WHERE `unix_minute` IS NULL OR `unix_minute` = 0;

ALTER TABLE `energy_metrics` 
MODIFY COLUMN `unix_minute` BIGINT NOT NULL;

-- 5. Hoàn tất và bật lại kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS = 1;
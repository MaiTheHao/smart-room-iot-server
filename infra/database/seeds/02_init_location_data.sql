/**
 * MỤC ĐÍCH & MỤC TIÊU CỦA FILE SEED:
 * 1) Khởi tạo cấu trúc hạ tầng vật lý (Physical Location) cơ bản cho hệ thống.
 * 2) Thiết lập các đơn vị không gian mẫu (Tầng, Phòng) và hỗ trợ đa ngôn ngữ tương ứng.
 * 3) Mở rộng hệ thống phân quyền (Functions/Roles) để kiểm soát truy cập cho các vị trí cụ thể.
 *
 * CHI TIẾT CÁC THÀNH PHẦN:
 * - Floor & Floor Lan: Khởi tạo dữ liệu tầng mẫu (Tầng Trệt - F00).
 * - Room & Room Lan: Khởi tạo dữ liệu phòng mẫu (Phòng Máy Chủ - R-Main-Server) thuộc tầng tương ứng.
 * - Sys Functions: Bổ sung các mã chức năng đặc thù để phân quyền truy cập theo từng tầng/phòng (F_ACCESS_FLOOR_*, F_ACCESS_ROOM_*).
 * - Sys Roles: Gán các quyền truy cập địa điểm mới khởi tạo cho nhóm quản trị (G_ADMIN).
 */
USE smart_room_iot;

SET
  FOREIGN_KEY_CHECKS = 0;

SET NAMES utf8;

SET
  TIME_ZONE = '+00:00';

-- ----------------------------
-- 1. Dữ liệu bảng floor (Tầng)
-- ----------------------------
INSERT INTO
  `floor` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `code`, `level`)
VALUES
  (1, NOW(), 'system', NOW(), 'system', 0, 'F00', 0);

-- ----------------------------
-- 2. Dữ liệu bảng floor_lan (Đa ngôn ngữ cho Tầng)
-- ----------------------------
INSERT INTO
  `floor_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (1, NULL, NULL, NULL, NULL, 0, 'Khu vực kỹ thuật và sảnh chính', 'vi', 'Tầng Trệt', 1),
  (2, NULL, NULL, NULL, NULL, 0, 'Technical area and main lobby', 'en', 'Ground Floor', 1);

-- ----------------------------
-- 3. Dữ liệu bảng room (Phòng)
-- ----------------------------
INSERT INTO
  `room` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `code`, `floor_id`)
VALUES
  (1, NOW(), 'system', NOW(), 'system', 0, 'R-Main-Server', 1);

-- ----------------------------
-- 4. Dữ liệu bảng room_lan (Đa ngôn ngữ cho Phòng)
-- ----------------------------
INSERT INTO
  `room_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (1, NULL, NULL, NULL, NULL, 0, 'Nơi đặt hệ thống quản trị', 'vi', 'Phòng Máy Chủ', 1),
  (2, NULL, NULL, NULL, NULL, 0, 'Central Server Room', 'en', 'Server Room', 1);

-- ----------------------------
-- 5. Dữ liệu bổ sung bảng sys_function (Các function đặc thù/mock)
-- ----------------------------
INSERT INTO
  `sys_function` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `function_code`)
VALUES
  (100, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_FLOOR_F00'),
  (200, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ROOM_R-Main-Server');

-- ----------------------------
-- 6. Dữ liệu bổ sung bảng sys_function_lan
-- ----------------------------
INSERT INTO
  `sys_function_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (25, NULL, NULL, NULL, NULL, 0, 'Quyền xem dữ liệu Tầng Trệt', 'vi', 'Truy cập Tầng Trệt', 100),
  (26, NULL, NULL, NULL, NULL, 0, 'Permission to view Ground Floor', 'en', 'Access Ground Floor', 100),
  (27, NULL, NULL, NULL, NULL, 0, 'Quyền hạn cao cấp điều khiển Server', 'vi', 'Truy Cập Server', 200),
  (28, NULL, NULL, NULL, NULL, 0, 'Control devices in Server Room', 'en', 'Server Access', 200);

-- ----------------------------
-- 7. Dữ liệu bổ sung bảng sys_role
-- ----------------------------
INSERT INTO
  `sys_role` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `function_id`, `group_id`)
VALUES
  (13, NOW(), 'system', NOW(), 'system', 0, 100, 1),
  (14, NOW(), 'system', NOW(), 'system', 0, 200, 1);

SET
  FOREIGN_KEY_CHECKS = 1;
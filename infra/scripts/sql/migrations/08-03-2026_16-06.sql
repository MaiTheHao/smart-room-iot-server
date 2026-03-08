-- Migration: Drop sys_client_function_cache table
-- Date: 08/03/2026 16:06
-- Description: Remove sys_client_function_cache table
SET
  FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `sys_client_function_cache`;

SET
  FOREIGN_KEY_CHECKS = 1;
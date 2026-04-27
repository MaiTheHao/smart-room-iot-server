-- Migration: Add access_token to client table
-- Created At: 2026-04-26

ALTER TABLE `client` ADD COLUMN `access_token` VARCHAR(1024) DEFAULT NULL;
ALTER TABLE `client` ADD COLUMN `gateway_password` VARCHAR(255) DEFAULT NULL;

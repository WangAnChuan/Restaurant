-- 添加 image_url 字段到 dish 表
ALTER TABLE `dish` ADD COLUMN `image_url` VARCHAR(500) AFTER `ingredients`;

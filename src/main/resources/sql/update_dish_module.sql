-- --------------------------------------------------------
-- Update Script for Dish Module (Dish Category & Attributes)
-- --------------------------------------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Create table `dish_category`
DROP TABLE IF EXISTS `dish_category`;
CREATE TABLE `dish_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` VARCHAR(50) NOT NULL COMMENT 'Category Name',
  `sort` INT DEFAULT 0 COMMENT 'Sort Order',
  `type` TINYINT DEFAULT 1 COMMENT '1:Dish 2:SetMeal',
  `status` TINYINT DEFAULT 1 COMMENT '1:Enable 0:Disable',
  `create_user` BIGINT COMMENT 'Creator ID',
  `update_user` BIGINT COMMENT 'Updater ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Dish Category Table';

-- 2. Alter table `dish` to add new fields
-- Add category_id
ALTER TABLE `dish` ADD COLUMN `category_id` BIGINT COMMENT 'Category ID' AFTER `image_url`;
-- Add code (mnemonic code)
ALTER TABLE `dish` ADD COLUMN `code` VARCHAR(50) COMMENT 'Mnemonic Code' AFTER `name`;
-- Add description
ALTER TABLE `dish` ADD COLUMN `description` VARCHAR(255) COMMENT 'Description' AFTER `image_url`;
-- Add sort
ALTER TABLE `dish` ADD COLUMN `sort` INT DEFAULT 0 COMMENT 'Sort Order' AFTER `status`;

-- 3. Initial Data for Dish Category
INSERT INTO `dish_category` (name, sort, type) VALUES ('Hot Dishes', 1, 1);
INSERT INTO `dish_category` (name, sort, type) VALUES ('Cold Dishes', 2, 1);
INSERT INTO `dish_category` (name, sort, type) VALUES ('Soup', 3, 1);
INSERT INTO `dish_category` (name, sort, type) VALUES ('Beverages', 4, 1);
INSERT INTO `dish_category` (name, sort, type) VALUES ('Main Course', 5, 1);

-- 4. Update existing dishes (if any) to a default category (e.g., Hot Dishes id=1)
-- This is optional but good for avoiding nulls if logic depends on it
UPDATE `dish` SET `category_id` = (SELECT id FROM `dish_category` LIMIT 1) WHERE `category_id` IS NULL;

SET FOREIGN_KEY_CHECKS = 1;

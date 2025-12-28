SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 2. Drop Tables if exist
DROP TABLE IF EXISTS `reservation`;
DROP TABLE IF EXISTS `restaurant_table`;
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_user`;
DROP TABLE IF EXISTS `account_record`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `purchase_list`;
DROP TABLE IF EXISTS `dish`;
DROP TABLE IF EXISTS `dish_category`;

-- 3. Validation Tables

-- Table: sys_user
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` VARCHAR(50) NOT NULL COMMENT 'Username',
  `password` VARCHAR(100) NOT NULL COMMENT 'Password',
  `real_name` VARCHAR(50) COMMENT 'Real Name',
  `avatar` VARCHAR(255) COMMENT 'Avatar',
  `status` TINYINT DEFAULT 1 COMMENT '1:Enable 0:Disable',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Table';

-- Table: sys_role
CREATE TABLE `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_name` VARCHAR(50) NOT NULL,
  `role_code` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Role Table';

-- Table: sys_user_role
CREATE TABLE `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User-Role Relation';

-- Table: category
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `type` TINYINT NOT NULL COMMENT '1:Income 2:Expense',
  `parent_id` BIGINT DEFAULT 0,
  `create_by` BIGINT COMMENT 'Creator ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Category Table';

-- Table: account_record
CREATE TABLE `account_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `type` TINYINT NOT NULL COMMENT '1:Income 2:Expense',
  `category_id` BIGINT NOT NULL,
  `category_name` VARCHAR(50) COMMENT 'Snapshot',
  `amount` DECIMAL(10,2) NOT NULL,
  `transaction_date` DATE NOT NULL,
  `payment_method` VARCHAR(50) COMMENT 'WeChat/Alipay/Cash',
  `remark` VARCHAR(255),
  `create_by` BIGINT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_date` (`transaction_date`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Account Record';

-- Table: purchase_list
CREATE TABLE `purchase_list` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `target_date` DATE NOT NULL,
  `item_name` VARCHAR(100) NOT NULL,
  `quantity` VARCHAR(50) NOT NULL,
  `status` TINYINT DEFAULT 0 COMMENT '0:Pending 1:Done',
  `remark` VARCHAR(255),
  `create_by` BIGINT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_target_date` (`target_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Purchase List';

-- Table: dish_category
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

-- Table: dish
CREATE TABLE `dish` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `code` VARCHAR(50) COMMENT 'Mnemonic Code',
  `price` DECIMAL(10,2) NOT NULL,
  `ingredients` VARCHAR(255),
  `image_url` VARCHAR(500),
  `category_id` BIGINT COMMENT 'Category ID',
  `description` VARCHAR(255) COMMENT 'Description',
  `status` TINYINT DEFAULT 1 COMMENT '1:OnShelf 0:OffShelf',
  `sort` INT DEFAULT 0 COMMENT 'Sort Order',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Dish Table';

-- Table: restaurant_table
CREATE TABLE `restaurant_table` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `table_number` VARCHAR(10) NOT NULL COMMENT 'Table Number',
  `table_type` VARCHAR(20) NOT NULL COMMENT 'ROUND_TABLE or SQUARE_TABLE',
  `capacity` INT NOT NULL COMMENT 'Seat Capacity',
  `status` TINYINT DEFAULT 1 COMMENT '1:Available 0:Unavailable',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_table_number` (`table_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Restaurant Table';

-- Table: reservation
CREATE TABLE `reservation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `table_id` BIGINT NOT NULL COMMENT 'Table ID',
  `customer_name` VARCHAR(50) NOT NULL COMMENT 'Customer Name',
  `customer_phone` VARCHAR(20) NOT NULL COMMENT 'Customer Phone',
  `reservation_date` DATE NOT NULL COMMENT 'Reservation Date',
  `reservation_time` VARCHAR(10) NOT NULL COMMENT 'Time Slot (e.g. 12:00)',
  `guest_count` INT NOT NULL COMMENT 'Number of Guests',
  `status` TINYINT DEFAULT 0 COMMENT '0:Pending 1:Confirmed 2:Completed 3:Cancelled',
  `remark` VARCHAR(255) COMMENT 'Remark',
  `create_by` BIGINT COMMENT 'Creator User ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_table_id` (`table_id`),
  KEY `idx_reservation_date` (`reservation_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Reservation';

-- 4. Initial Data
-- Password: 123456 (BCrypt encoded)
-- Roles
INSERT INTO `sys_role` (id, role_name, role_code) VALUES (1, 'Boss', 'BOSS');
INSERT INTO `sys_role` (id, role_name, role_code) VALUES (2, 'Purchaser', 'PURCHASER');
INSERT INTO `sys_role` (id, role_name, role_code) VALUES (3, 'Visitor', 'VISITOR');

-- Users
-- Boss: admin / 123456
INSERT INTO `sys_user` (id, username, password, real_name) VALUES (1, 'admin', '$2a$10$N.kmUOlE4qGmWeA7BcL6/.Sm3Q6gDKaHmqFBxsLutQ7bNZrCHQzGC', 'Boss Zhang');
-- Purchaser: buyer / 123456
INSERT INTO `sys_user` (id, username, password, real_name) VALUES (2, 'buyer', '$2a$10$N.kmUOlE4qGmWeA7BcL6/.Sm3Q6gDKaHmqFBxsLutQ7bNZrCHQzGC', 'Xiao Li');
-- Visitor: guest / 123456
INSERT INTO `sys_user` (id, username, password, real_name) VALUES (3, 'guest', '$2a$10$N.kmUOlE4qGmWeA7BcL6/.Sm3Q6gDKaHmqFBxsLutQ7bNZrCHQzGC', 'Guest');

-- User Roles
INSERT INTO `sys_user_role` (user_id, role_id) VALUES (1, 1);
INSERT INTO `sys_user_role` (user_id, role_id) VALUES (2, 2);
INSERT INTO `sys_user_role` (user_id, role_id) VALUES (3, 3);

-- Categories
INSERT INTO `category` (type, name) VALUES (1, 'WeChat'), (1, 'Alipay'), (1, 'Cash');
INSERT INTO `category` (type, name) VALUES (2, 'Ingredients - Meat'), (2, 'Ingredients - Veg'), (2, 'Seasoning'), (2, 'Utilities');

-- Dish Categories
INSERT INTO `dish_category` (name, sort, type) VALUES ('Hot Dishes', 1, 1);
INSERT INTO `dish_category` (name, sort, type) VALUES ('Cold Dishes', 2, 1);
INSERT INTO `dish_category` (name, sort, type) VALUES ('Soup', 3, 1);
INSERT INTO `dish_category` (name, sort, type) VALUES ('Beverages', 4, 1);
INSERT INTO `dish_category` (name, sort, type) VALUES ('Main Course', 5, 1);

-- Restaurant Tables: 2 Round Tables (10 seats) + 6 Square Tables (4 seats) = 8 tables
INSERT INTO `restaurant_table` (table_number, table_type, capacity, status) VALUES
('A1', 'ROUND_TABLE', 10, 1),
('A2', 'ROUND_TABLE', 10, 1),
('B1', 'SQUARE_TABLE', 4, 1),
('B2', 'SQUARE_TABLE', 4, 1),
('B3', 'SQUARE_TABLE', 4, 1),
('B4', 'SQUARE_TABLE', 4, 1),
('B5', 'SQUARE_TABLE', 4, 1),
('B6', 'SQUARE_TABLE', 4, 1);

SET FOREIGN_KEY_CHECKS = 1;

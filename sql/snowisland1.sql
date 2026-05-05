/*
Navicat MySQL Data Transfer

Source Server         : course_system
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : snowisland

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2026-05-02 22:22:42
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ammo
-- ----------------------------
DROP TABLE IF EXISTS `ammo`;
CREATE TABLE `ammo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `unit` varchar(20) NOT NULL,
  `remark` text,
  `weapon_id` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `weapon_id` (`weapon_id`),
  CONSTRAINT `ammo_ibfk_1` FOREIGN KEY (`weapon_id`) REFERENCES `weapon` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for item
-- ----------------------------
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `unit` varchar(20) NOT NULL,
  `remark` text,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for job
-- ----------------------------
DROP TABLE IF EXISTS `job`;
CREATE TABLE `job` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `skills` text NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for material
-- ----------------------------
DROP TABLE IF EXISTS `material`;
CREATE TABLE `material` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `unit` varchar(20) NOT NULL,
  `remark` text,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for player
-- ----------------------------
DROP TABLE IF EXISTS `player`;
CREATE TABLE `player` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `is_weak` tinyint(1) NOT NULL DEFAULT '0',
  `is_overworked` tinyint(1) NOT NULL DEFAULT '0',
  `is_injured` tinyint(1) NOT NULL DEFAULT '0',
  `job_id` int(11) NOT NULL,
  `skill_id` int(11) DEFAULT NULL,
  `faction` enum('统治者','反叛者','冒险者','杀戮者','平民') NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `job_id` (`job_id`),
  KEY `skill_id` (`skill_id`),
  CONSTRAINT `player_ibfk_1` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`),
  CONSTRAINT `player_ibfk_2` FOREIGN KEY (`skill_id`) REFERENCES `skill` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for player_items
-- ----------------------------
DROP TABLE IF EXISTS `player_items`;
CREATE TABLE `player_items` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL,
  `item_type` enum('item','weapon','ammo','material') NOT NULL,
  `item_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_player_type_item` (`player_id`,`item_type`,`item_id`),
  KEY `idx_player_type` (`player_id`,`item_type`),
  CONSTRAINT `player_items_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for skill
-- ----------------------------
DROP TABLE IF EXISTS `skill`;
CREATE TABLE `skill` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `function` text NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for trade
-- ----------------------------
DROP TABLE IF EXISTS `trade`;
CREATE TABLE `trade` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `from_player_id` int(11) NOT NULL COMMENT '发起方玩家ID',
  `to_player_id` int(11) NOT NULL COMMENT '接收方玩家ID',
  `status` enum('pending','accepted','rejected','cancelled','completed') NOT NULL DEFAULT 'pending' COMMENT '交易状态：pending-交易中, accepted-已接受, rejected-已拒绝, cancelled-交易中止, completed-交易成功',
  `remark` text COMMENT '交易备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_from_player` (`from_player_id`),
  KEY `idx_to_player` (`to_player_id`),
  KEY `idx_status` (`status`),
  KEY `idx_from_to_status` (`from_player_id`,`to_player_id`,`status`),
  CONSTRAINT `trade_ibfk_1` FOREIGN KEY (`from_player_id`) REFERENCES `player` (`id`),
  CONSTRAINT `trade_ibfk_2` FOREIGN KEY (`to_player_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COMMENT='交易主表';

-- ----------------------------
-- Table structure for trade_items
-- ----------------------------
DROP TABLE IF EXISTS `trade_items`;
CREATE TABLE `trade_items` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `trade_id` int(11) NOT NULL COMMENT '关联的交易ID',
  `item_type` enum('item','weapon','ammo','material') NOT NULL COMMENT '物品类型',
  `item_id` int(11) NOT NULL COMMENT '物品ID',
  `quantity` int(11) NOT NULL DEFAULT '1' COMMENT '物品数量',
  `direction` enum('give','take') NOT NULL COMMENT '物品方向：give-给予, take-索取',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `name` varchar(255) DEFAULT NULL,
  `unit` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_trade_id` (`trade_id`),
  KEY `idx_item` (`item_type`,`item_id`),
  CONSTRAINT `trade_items_ibfk_1` FOREIGN KEY (`trade_id`) REFERENCES `trade` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COMMENT='交易物品明细表';

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` enum('dm','player') NOT NULL,
  `player_id` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `player_id` (`player_id`),
  CONSTRAINT `user_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for weapon
-- ----------------------------
DROP TABLE IF EXISTS `weapon`;
CREATE TABLE `weapon` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `unit` varchar(20) NOT NULL,
  `remark` text,
  `threat_level` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4;

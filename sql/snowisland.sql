/*
Navicat MySQL Data Transfer

Source Server         : course_system
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : snowisland

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2026-05-01 11:03:09
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
-- Records of ammo
-- ----------------------------
INSERT INTO `ammo` VALUES ('1', '手枪弹', '枚', '制式手枪子弹', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `ammo` VALUES ('2', '猎枪弹', '枚', '猎枪子弹', '2', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `ammo` VALUES ('3', '信号弹', '枚', '信号枪子弹', '7', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `ammo` VALUES ('4', '箭矢', '枝', '猎弓箭矢', '7', '2026-04-27 11:36:23', '2026-04-27 11:36:23');

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
-- Records of item
-- ----------------------------
INSERT INTO `item` VALUES ('1', '医疗包', '个', '恢复生命值', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('2', '手电筒', '个', '提供光源', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('3', '手铐', '个', '限制行动', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('4', '哨子', '个', '发出信号', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('5', '防弹衣', '件', '减少伤害', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('6', '复合盾', '个', '提供防护', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('7', '信号枪', '把', '发射信号', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('8', '维修工具包', '个', '修复物品', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('9', '协议书', '个', '重要文件', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('10', '朗姆酒', '瓶', '恢复精力', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('11', '草药', '个', '治疗小伤口', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('12', '渔网', '张', '捕鱼工具', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('13', '蜡烛', '根', '提供光源', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('14', '医用酒精', '升', '消毒用品', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('15', '火柴', '盒', '点火工具', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('16', '铅笔', '盒', '书写工具', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('17', '破损海图', '张', '导航参考', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('18', '便当', '份', '恢复饥饿', '2026-04-27 11:36:23', '2026-04-27 11:36:23');

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
-- Records of job
-- ----------------------------
INSERT INTO `job` VALUES ('1', '战士', '近战攻击、防御、武器精通', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `job` VALUES ('2', '法师', '元素魔法、法术护盾、远程攻击', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `job` VALUES ('3', '盗贼', '潜行、开锁、背刺', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `job` VALUES ('4', '牧师', '治疗、祝福、神圣魔法', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `job` VALUES ('5', '猎人', '远程攻击、追踪、宠物驯养', '2026-04-26 22:13:35', '2026-04-26 22:13:35');

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
-- Records of material
-- ----------------------------
INSERT INTO `material` VALUES ('1', '金属制品', 'kg', '可用于制作工具', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('2', '木材', 'kg', '可用于建造', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('3', '绳索', '米', '多种用途', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('4', '木板', 'kg', '建筑材料', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('5', '食物', 'kg', '恢复饥饿', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('6', '沥青', 'kg', '建筑材料', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('7', '石料', 'kg', '建筑材料', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('8', '燃料', 'kg', '提供能源', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('9', '帆布', '米', '制作帐篷', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('10', '发动机', '个', '机械动力', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('11', '螺旋桨', '个', '船只推进', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('12', '发电机', '个', '发电设备', '2026-04-27 11:36:23', '2026-04-27 11:36:23');

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
-- Records of player
-- ----------------------------
INSERT INTO `player` VALUES ('1', '阿尔伯特', '0', '0', '0', '1', '1', '统治者', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `player` VALUES ('2', '莉莉丝', '0', '1', '0', '2', '2', '反叛者', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `player` VALUES ('3', '罗宾', '1', '0', '0', '3', '3', '冒险者', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `player` VALUES ('4', '亚瑟', '0', '0', '1', '4', '4', '杀戮者', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `player` VALUES ('5', '艾米丽', '0', '0', '0', '5', '5', '平民', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `player` VALUES ('6', '测试', '0', '0', '0', '2', '2', '平民', '2026-04-29 01:14:41', '2026-04-29 01:14:41');

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
-- Records of player_items
-- ----------------------------
INSERT INTO `player_items` VALUES ('1', '1', 'item', '1', '3', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('2', '1', 'item', '2', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('3', '1', 'item', '5', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('4', '1', 'weapon', '1', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('5', '1', 'weapon', '3', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('6', '1', 'ammo', '1', '30', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('7', '1', 'material', '1', '5', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('8', '1', 'material', '2', '10', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('9', '2', 'item', '4', '2', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('10', '2', 'item', '7', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('11', '2', 'weapon', '2', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('12', '2', 'weapon', '6', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('13', '2', 'ammo', '2', '10', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('14', '2', 'ammo', '3', '5', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('15', '2', 'material', '3', '20', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('16', '2', 'material', '5', '7', '2026-04-27 11:36:23', '2026-05-01 10:54:53');
INSERT INTO `player_items` VALUES ('17', '3', 'item', '8', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('18', '3', 'item', '10', '2', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('19', '3', 'weapon', '4', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('20', '3', 'weapon', '8', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('21', '3', 'material', '4', '15', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('22', '3', 'material', '6', '5', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('23', '3', 'material', '9', '10', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('24', '4', 'item', '3', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('25', '4', 'item', '12', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('26', '4', 'weapon', '5', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('27', '4', 'weapon', '7', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('28', '4', 'ammo', '4', '20', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('29', '4', 'material', '7', '20', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('30', '4', 'material', '8', '10', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('31', '5', 'item', '6', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('32', '5', 'item', '14', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('33', '5', 'weapon', '9', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('34', '5', 'weapon', '10', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('35', '5', 'material', '10', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('36', '5', 'material', '11', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('37', '5', 'material', '12', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `player_items` VALUES ('38', '1', 'material', '5', '1', '2026-05-01 10:54:53', '2026-05-01 10:54:53');

-- ----------------------------
-- Table structure for shelter_progress（统治者避难所：当前建造值）
-- ----------------------------
DROP TABLE IF EXISTS `shelter_progress`;
CREATE TABLE `shelter_progress` (
  `id` int(11) NOT NULL COMMENT '固定为 1，全局唯一进度行',
  `current_build_value` int(11) NOT NULL DEFAULT '0' COMMENT '当前建造值',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统治者避难所建造进度';

-- ----------------------------
-- Records of shelter_progress（与前端演示日志累加值一致，可按游戏调整）
-- ----------------------------
INSERT INTO `shelter_progress` (`id`, `current_build_value`, `created_at`, `updated_at`) VALUES
(1, 76, '2026-05-05 00:00:00', '2026-05-05 00:00:00');

-- ----------------------------
-- Table structure for shelter_stock（统治者避难所：物资库存，item_key 与前端图鉴 id 一致）
-- ----------------------------
DROP TABLE IF EXISTS `shelter_stock`;
CREATE TABLE `shelter_stock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `item_key` varchar(64) NOT NULL COMMENT '物资键，如 medical_kit、wood',
  `quantity` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_key` (`item_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统治者避难所物资库存';

-- ----------------------------
-- Records of shelter_stock
-- ----------------------------
INSERT INTO `shelter_stock` (`item_key`, `quantity`, `created_at`, `updated_at`) VALUES
('wood', 45, NOW(), NOW()),
('stone', 32, NOW(), NOW()),
('medical_kit', 8, NOW(), NOW()),
('flashlight', 4, NOW(), NOW()),
('handcuffs', 2, NOW(), NOW()),
('whistle', 3, NOW(), NOW()),
('body_armor', 1, NOW(), NOW()),
('composite_shield', 1, NOW(), NOW()),
('flare_gun', 1, NOW(), NOW()),
('repair_kit', 5, NOW(), NOW()),
('contract', 2, NOW(), NOW()),
('rum', 10, NOW(), NOW()),
('herbs', 12, NOW(), NOW()),
('fishing_net', 2, NOW(), NOW()),
('candle', 18, NOW(), NOW()),
('rubbing_alcohol', 3, NOW(), NOW()),
('matches', 6, NOW(), NOW()),
('pencil', 4, NOW(), NOW()),
('tattered_chart', 1, NOW(), NOW()),
('service_pistol', 1, NOW(), NOW()),
('hunting_shotgun', 1, NOW(), NOW()),
('baton', 2, NOW(), NOW()),
('bayonet', 1, NOW(), NOW()),
('harpoon_spear', 1, NOW(), NOW()),
('hunting_bow', 1, NOW(), NOW()),
('pickaxe', 2, NOW(), NOW()),
('axe', 1, NOW(), NOW()),
('plank', 24, NOW(), NOW()),
('rope', 35, NOW(), NOW());

-- ----------------------------
-- 全局 food_catalog / energy_catalog；玩家库存；统治者避难所公共食物/能量库存（与玩家个人分离）
-- 若未全量重导、仅补表，请执行 init_player_supply.sql（先备份）
-- ----------------------------
DROP TABLE IF EXISTS `player_food_stock`;
DROP TABLE IF EXISTS `player_energy_stock`;
DROP TABLE IF EXISTS `shelter_food_stock`;
DROP TABLE IF EXISTS `shelter_energy_stock`;
DROP TABLE IF EXISTS `food_catalog`;
DROP TABLE IF EXISTS `energy_catalog`;
DROP TABLE IF EXISTS `shelter_food_catalog`;
DROP TABLE IF EXISTS `shelter_energy_catalog`;

CREATE TABLE `food_catalog` (
  `item_key` varchar(64) NOT NULL,
  `name` varchar(64) NOT NULL,
  `unit` varchar(16) NOT NULL COMMENT 'kg、portion、L 等',
  `kcal_per_unit` int(11) NOT NULL COMMENT '每单位大卡',
  `sort_order` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`item_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏内食物种类（全局）';

INSERT INTO `food_catalog` (`item_key`, `name`, `unit`, `kcal_per_unit`, `sort_order`) VALUES
('salty_pork', '咸肉', 'kg', 3000, 10),
('dried_fish', '鱼干', 'kg', 2000, 20),
('flour', '面粉', 'kg', 3000, 30),
('jam', '果酱', 'kg', 3000, 40),
('bread', '面包', 'kg', 2500, 50),
('potato', '土豆', 'kg', 1000, 60),
('hard_biscuit', '硬饼干', 'kg', 5000, 70),
('sauerkraut', '酸菜', 'kg', 1000, 80),
('dried_onion', '干洋葱', 'kg', 1500, 90),
('dried_apple', '苹果干', 'kg', 4000, 100),
('oatmeal', '燕麦片', 'kg', 4000, 110),
('fish_meat', '鱼肉', 'kg', 2000, 120),
('goat_milk', '羊奶', 'kg', 3000, 130),
('jerky', '肉干', 'kg', 3000, 140),
('smoked_meat', '熏肉', 'kg', 35000, 150),
('canned_food', '罐头', 'portion', 8000, 160),
('candy', '糖果', 'kg', 5000, 170),
('cereal', '麦片', 'kg', 4000, 180),
('military_ration', '军用压缩干粮', 'portion', 2500, 190),
('shellfish', '贝类', 'kg', 2000, 200),
('mushroom', '食用菌菇', 'kg', 1000, 210),
('insect_cocoon', '虫茧', 'portion', 1000, 220),
('wild_blueberry', '野生蓝莓', 'kg', 1200, 230),
('raspberry', '树莓', 'kg', 1500, 240);

CREATE TABLE `player_food_stock` (
  `player_id` int(11) NOT NULL,
  `item_key` varchar(64) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`player_id`,`item_key`),
  KEY `idx_player_food_item` (`item_key`),
  CONSTRAINT `fk_pfs_player` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pfs_food_catalog` FOREIGN KEY (`item_key`) REFERENCES `food_catalog` (`item_key`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家食物库存';

INSERT INTO `player_food_stock` (`player_id`, `item_key`, `quantity`) VALUES
(1, 'bread', 5), (1, 'jerky', 2), (1, 'candy', 3), (1, 'military_ration', 2);

CREATE TABLE `energy_catalog` (
  `item_key` varchar(64) NOT NULL,
  `name` varchar(64) NOT NULL,
  `unit` varchar(16) NOT NULL,
  `kcal_per_unit` int(11) NOT NULL,
  `sort_order` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`item_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏内能量（燃料）种类（全局）';

INSERT INTO `energy_catalog` (`item_key`, `name`, `unit`, `kcal_per_unit`, `sort_order`) VALUES
('firewood', '木柴', 'kg', 4500, 10),
('coal', '煤炭', 'kg', 7000, 20),
('fuel_oil', '油料', 'L', 9000, 30);

CREATE TABLE `player_energy_stock` (
  `player_id` int(11) NOT NULL,
  `item_key` varchar(64) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`player_id`,`item_key`),
  KEY `idx_player_energy_item` (`item_key`),
  CONSTRAINT `fk_pes_player` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pes_energy_catalog` FOREIGN KEY (`item_key`) REFERENCES `energy_catalog` (`item_key`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家能量（燃料）库存';

INSERT INTO `player_energy_stock` (`player_id`, `item_key`, `quantity`) VALUES
(1, 'firewood', 3), (1, 'coal', 1);

-- 统治者避难所公共食物/能量（统治者避难所建造进度页展示；无 player_id）
CREATE TABLE `shelter_food_stock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `item_key` varchar(64) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sfs_item` (`item_key`),
  CONSTRAINT `fk_sfs_food_catalog` FOREIGN KEY (`item_key`) REFERENCES `food_catalog` (`item_key`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统治者避难所食物库存';

INSERT INTO `shelter_food_stock` (`item_key`, `quantity`) VALUES
('salty_pork', 4), ('dried_fish', 3), ('flour', 8), ('jam', 2), ('bread', 20),
('potato', 15), ('hard_biscuit', 2), ('sauerkraut', 5), ('dried_onion', 3), ('dried_apple', 2),
('oatmeal', 2), ('fish_meat', 6), ('goat_milk', 5), ('jerky', 12), ('smoked_meat', 1),
('canned_food', 8), ('candy', 1), ('cereal', 2), ('military_ration', 10), ('shellfish', 4),
('mushroom', 5), ('insect_cocoon', 2), ('wild_blueberry', 3), ('raspberry', 2);

CREATE TABLE `shelter_energy_stock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `item_key` varchar(64) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ses_item` (`item_key`),
  CONSTRAINT `fk_ses_energy_catalog` FOREIGN KEY (`item_key`) REFERENCES `energy_catalog` (`item_key`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统治者避难所能量（燃料）库存';

INSERT INTO `shelter_energy_stock` (`item_key`, `quantity`) VALUES
('firewood', 25), ('coal', 10), ('fuel_oil', 5);

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
-- Records of skill
-- ----------------------------
INSERT INTO `skill` VALUES ('1', '力量增强', '临时提高力量属性', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `skill` VALUES ('2', '魔法抗性', '减少魔法伤害', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `skill` VALUES ('3', '敏捷提升', '提高移动速度和闪避', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `skill` VALUES ('4', '生命恢复', '缓慢恢复生命值', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `skill` VALUES ('5', '幸运加成', '提高暴击率和掉落率', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `skill` VALUES ('6', '洞察术', '发现隐藏的宝藏和陷阱', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `skill` VALUES ('7', '冥想', '恢复魔法值', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `skill` VALUES ('8', '鼓舞士气', '提高团队战斗力', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `skill` VALUES ('9', '伪装', '融入环境避免被发现', '2026-04-26 22:13:35', '2026-04-26 22:13:35');
INSERT INTO `skill` VALUES ('10', '急救', '快速处理伤口', '2026-04-26 22:13:35', '2026-04-26 22:13:35');

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
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COMMENT='交易主表';

-- ----------------------------
-- Records of trade
-- ----------------------------
INSERT INTO `trade` VALUES ('1', '1', '2', 'cancelled', '需要一些食物', '2026-04-30 14:06:06', '2026-05-01 09:34:00');
INSERT INTO `trade` VALUES ('2', '3', '1', 'accepted', '换一些工具', '2026-04-30 14:06:06', '2026-04-30 14:06:06');
INSERT INTO `trade` VALUES ('3', '4', '5', 'rejected', '想要发电机', '2026-04-30 14:06:06', '2026-04-30 14:06:06');
INSERT INTO `trade` VALUES ('4', '2', '3', 'cancelled', '物资不匹配', '2026-04-30 14:06:06', '2026-04-30 14:06:06');
INSERT INTO `trade` VALUES ('5', '5', '1', 'completed', '完成了交易', '2026-04-30 14:06:06', '2026-04-30 14:06:06');
INSERT INTO `trade` VALUES ('6', '1', '2', 'cancelled', '测试', '2026-05-01 09:36:56', '2026-05-01 09:37:18');
INSERT INTO `trade` VALUES ('7', '1', '2', 'completed', '1', '2026-05-01 10:29:46', '2026-05-01 10:30:46');
INSERT INTO `trade` VALUES ('8', '1', '2', 'completed', '', '2026-05-01 10:54:31', '2026-05-01 10:54:53');

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
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COMMENT='交易物品明细表';

-- ----------------------------
-- Records of trade_items
-- ----------------------------
INSERT INTO `trade_items` VALUES ('1', '1', 'item', '1', '1', 'give', '2026-04-30 14:06:06', null, null);
INSERT INTO `trade_items` VALUES ('2', '1', 'weapon', '1', '1', 'give', '2026-04-30 14:06:06', null, null);
INSERT INTO `trade_items` VALUES ('3', '1', 'material', '5', '5', 'take', '2026-04-30 14:06:06', null, null);
INSERT INTO `trade_items` VALUES ('4', '2', 'weapon', '4', '1', 'give', '2026-04-30 14:06:06', null, null);
INSERT INTO `trade_items` VALUES ('5', '2', 'item', '8', '1', 'take', '2026-04-30 14:06:06', null, null);
INSERT INTO `trade_items` VALUES ('6', '3', 'material', '7', '10', 'give', '2026-04-30 14:06:06', null, null);
INSERT INTO `trade_items` VALUES ('7', '3', 'material', '12', '1', 'take', '2026-04-30 14:06:06', null, null);
INSERT INTO `trade_items` VALUES ('8', '4', 'item', '4', '2', 'give', '2026-04-30 14:06:06', null, null);
INSERT INTO `trade_items` VALUES ('9', '4', 'ammo', '2', '5', 'take', '2026-04-30 14:06:06', null, null);
INSERT INTO `trade_items` VALUES ('10', '5', 'weapon', '9', '1', 'give', '2026-04-30 14:06:06', null, null);
INSERT INTO `trade_items` VALUES ('11', '5', 'weapon', '3', '1', 'take', '2026-04-30 14:06:06', null, null);
INSERT INTO `trade_items` VALUES ('12', '6', 'material', '5', '1', 'take', '2026-05-01 09:36:56', null, null);
INSERT INTO `trade_items` VALUES ('13', '7', 'material', '5', '1', 'take', '2026-05-01 10:29:46', null, null);
INSERT INTO `trade_items` VALUES ('14', '8', 'material', '5', '1', 'take', '2026-05-01 10:54:31', null, null);

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
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', 'dm1', 'test123', 'dm', null, '2026-04-26 22:13:35', '2026-04-26 22:13:35', '1');
INSERT INTO `user` VALUES ('2', 'dm2', 'test123', 'dm', null, '2026-04-26 22:13:35', '2026-04-26 22:13:35', '1');
INSERT INTO `user` VALUES ('3', 'player1', 'test123', 'player', '1', '2026-04-26 22:13:35', '2026-04-26 22:13:35', '1');
INSERT INTO `user` VALUES ('4', 'player2', 'test123', 'player', '2', '2026-04-26 22:13:35', '2026-04-26 22:13:35', '1');
INSERT INTO `user` VALUES ('5', 'player3', 'test123', 'player', '3', '2026-04-26 22:13:35', '2026-04-26 22:13:35', '1');
INSERT INTO `user` VALUES ('6', 'player4', 'test123', 'player', '4', '2026-04-26 22:13:35', '2026-04-26 22:13:35', '1');
INSERT INTO `user` VALUES ('7', 'player5', 'test123', 'player', '5', '2026-04-26 22:13:35', '2026-04-26 22:13:35', '1');
INSERT INTO `user` VALUES ('8', 'player6', 'test123', 'player', '6', '2026-04-29 10:34:27', '2026-04-29 10:34:27', '1');

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

-- ----------------------------
-- Records of weapon
-- ----------------------------
INSERT INTO `weapon` VALUES ('1', '制式手枪', '把', '标准配备', '5', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `weapon` VALUES ('2', '猎枪', '把', '威力较大', '8', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `weapon` VALUES ('3', '警棍', '个', '非致命武器', '3', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `weapon` VALUES ('4', '刺刀', '把', '近战武器', '4', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `weapon` VALUES ('5', '水手刀', '把', '多功能刀具', '3', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `weapon` VALUES ('6', '鱼叉/矛', '个', '狩猎工具', '6', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `weapon` VALUES ('7', '猎弓', '张', '远程武器', '5', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `weapon` VALUES ('8', '十字镐', '把', '挖掘工具', '4', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `weapon` VALUES ('9', '斧头', '把', '砍伐工具', '6', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `weapon` VALUES ('10', '电锯', '把', '切割工具', '7', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `weapon` VALUES ('11', '手术刀', '把', '医疗工具', '2', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `weapon` VALUES ('12', '炸药', 'kg', '爆炸物', '10', '2026-04-27 11:36:23', '2026-04-27 11:36:23');

/*
Navicat MySQL Data Transfer

Source Server         : cc
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : snowisland

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2026-06-24 22:37:18
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
INSERT INTO `ammo` VALUES ('1', '手枪弹', '枚', '.38/200口径手枪弹药，每发为韦伯利手枪专用。装填后可使手枪的威胁值生效。', '1', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `ammo` VALUES ('2', '猎枪弹', '枚', '12号口径霰弹，每发内含多颗铅弹。装填后可使猎枪的威胁值生效。', '2', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `ammo` VALUES ('3', '信号弹', '枚', '红/绿两色信号弹，每发可发射一次。用信号枪发射后，由主持人在公屏展示信号内容。', '7', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `ammo` VALUES ('4', '箭矢', '枝', '木质箭杆配金属箭头，每支为猎弓专用。装填后可使猎弓的威胁值生效。', '7', '2026-04-27 11:36:23', '2026-04-27 11:36:23');

-- ----------------------------
-- Table structure for ark_config
-- ----------------------------
DROP TABLE IF EXISTS `ark_config`;
CREATE TABLE `ark_config` (
  `id` int(11) NOT NULL,
  `ark_status` varchar(20) DEFAULT NULL,
  `base_capacity` int(11) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `current_game_day` int(11) DEFAULT NULL,
  `daily_asphalt_limit` decimal(10,2) DEFAULT NULL,
  `daily_metal_limit` decimal(10,2) DEFAULT NULL,
  `daily_wood_limit` decimal(10,2) DEFAULT NULL,
  `food_per_capacity` int(11) DEFAULT NULL,
  `fuel_per_capacity` decimal(5,2) DEFAULT NULL,
  `initial_metal` decimal(10,2) DEFAULT NULL,
  `initial_wood` decimal(10,2) DEFAULT NULL,
  `sail_canvas_required` decimal(10,2) DEFAULT NULL,
  `sail_days_0_engine` int(11) DEFAULT NULL,
  `sail_days_1_engine` int(11) DEFAULT NULL,
  `sail_days_2_engine` int(11) DEFAULT NULL,
  `sail_days_3_engine` int(11) DEFAULT NULL,
  `sail_days_with_sail_0_engine` int(11) DEFAULT NULL,
  `sail_days_with_sail_1_engine` int(11) DEFAULT NULL,
  `sail_rope_required` decimal(10,2) DEFAULT NULL,
  `sail_wood_per_capacity` decimal(5,2) DEFAULT NULL,
  `sealant_per_capacity` decimal(5,2) DEFAULT NULL,
  `shortage_capacity_penalty` int(11) DEFAULT NULL,
  `shortage_completion_penalty` decimal(4,2) DEFAULT NULL,
  `target_asphalt` decimal(10,2) DEFAULT NULL,
  `target_metal` decimal(10,2) DEFAULT NULL,
  `target_wood` decimal(10,2) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `work_asphalt_per_unit` decimal(5,2) DEFAULT NULL,
  `work_metal_per_unit` decimal(5,2) DEFAULT NULL,
  `work_wood_per_unit` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ark_config
-- ----------------------------
INSERT INTO `ark_config` VALUES ('1', 'building', '50', '2026-05-13 21:35:13.589000', '1', '20.00', '20.00', '30.00', '100', '2.00', '20.00', '10.00', '80.00', '10', '8', '6', '4', '10', '7', '100.00', '2.00', '500.00', '3', '2.50', '100.00', '100.00', '250.00', '2026-05-13 21:35:13.589000', '5.00', '5.00', '5.00');

-- ----------------------------
-- Table structure for ark_construction
-- ----------------------------
DROP TABLE IF EXISTS `ark_construction`;
CREATE TABLE `ark_construction` (
  `id` int(11) NOT NULL DEFAULT '1' COMMENT '单方舟模式，固定值为1',
  `current_wood` double NOT NULL DEFAULT '0' COMMENT '当前木材数量（吨）',
  `current_metal` double NOT NULL DEFAULT '0' COMMENT '当前金属制品数量（吨）',
  `current_sealant` int(11) NOT NULL DEFAULT '0' COMMENT '当前密封材料数量（kg）',
  `engine_count` int(11) NOT NULL DEFAULT '0' COMMENT '发动机数量（0-3）',
  `propeller_count` int(11) NOT NULL DEFAULT '0' COMMENT '螺旋桨数量',
  `generator_count` int(11) NOT NULL DEFAULT '0' COMMENT '发电机数量',
  `current_cargo_capacity` int(11) NOT NULL DEFAULT '0' COMMENT '当前载重能力',
  `completion_percentage` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '完成度百分比',
  `has_sail` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已建造帆',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='方舟建造进度表';

-- ----------------------------
-- Records of ark_construction
-- ----------------------------
INSERT INTO `ark_construction` VALUES ('1', '235', '88.5', '98', '3', '2', '1', '41', '95.13', '0', '2026-05-13 22:06:35', '2026-05-24 22:31:28');

-- ----------------------------
-- Table structure for ark_construction_log
-- ----------------------------
DROP TABLE IF EXISTS `ark_construction_log`;
CREATE TABLE `ark_construction_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `action_description` varchar(255) DEFAULT NULL,
  `action_type` varchar(30) NOT NULL,
  `asphalt_change` decimal(10,2) DEFAULT NULL,
  `capacity_change` int(11) DEFAULT NULL,
  `completion_change` decimal(5,2) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `current_capacity` int(11) DEFAULT NULL,
  `current_completion` decimal(5,2) DEFAULT NULL,
  `current_stage` varchar(30) DEFAULT NULL,
  `engine_installed` int(11) DEFAULT NULL,
  `game_day` int(11) NOT NULL,
  `generator_installed` int(11) DEFAULT NULL,
  `metal_change` decimal(10,2) DEFAULT NULL,
  `player_id` int(11) NOT NULL,
  `player_name` varchar(50) DEFAULT NULL,
  `previous_capacity` int(11) DEFAULT NULL,
  `previous_completion` decimal(5,2) DEFAULT NULL,
  `previous_stage` varchar(30) DEFAULT NULL,
  `propeller_installed` int(11) DEFAULT NULL,
  `sail_built` bit(1) DEFAULT NULL,
  `sail_canvas_used` decimal(10,2) DEFAULT NULL,
  `sail_rope_used` decimal(10,2) DEFAULT NULL,
  `wood_change` decimal(10,2) DEFAULT NULL,
  `work_units` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ark_construction_log
-- ----------------------------

-- ----------------------------
-- Table structure for ark_required_skill
-- ----------------------------
DROP TABLE IF EXISTS `ark_required_skill`;
CREATE TABLE `ark_required_skill` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `effect_bonus` int(11) DEFAULT NULL,
  `is_required` bit(1) DEFAULT NULL,
  `priority` int(11) DEFAULT NULL,
  `skill_code` varchar(50) NOT NULL,
  `skill_description` varchar(255) DEFAULT NULL,
  `skill_name` varchar(100) NOT NULL,
  `skill_type` varchar(20) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_5af48mct3mcot3g68dq9chwoh` (`skill_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ark_required_skill
-- ----------------------------

-- ----------------------------
-- Table structure for ark_sail
-- ----------------------------
DROP TABLE IF EXISTS `ark_sail`;
CREATE TABLE `ark_sail` (
  `id` int(11) NOT NULL,
  `built_at_game_day` int(11) DEFAULT NULL,
  `built_by_player_id` int(11) DEFAULT NULL,
  `collected_canvas` decimal(10,2) DEFAULT NULL,
  `collected_rope` decimal(10,2) DEFAULT NULL,
  `condition_status` varchar(20) DEFAULT NULL,
  `construction_progress` decimal(5,2) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `effect_0_engine_days` int(11) DEFAULT NULL,
  `effect_1_engine_days` int(11) DEFAULT NULL,
  `is_built` bit(1) DEFAULT NULL,
  `is_work_completed` bit(1) DEFAULT NULL,
  `last_repaired_day` int(11) DEFAULT NULL,
  `required_canvas` decimal(10,2) DEFAULT NULL,
  `required_rope` decimal(10,2) DEFAULT NULL,
  `requires_work_action` bit(1) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `work_action_player_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ark_sail
-- ----------------------------
INSERT INTO `ark_sail` VALUES ('1', null, null, '0.00', '0.00', 'normal', '0.00', '2026-05-13 21:35:13.618000', '10', '7', '\0', '\0', null, '80.00', '100.00', '', '2026-05-13 21:35:13.618000', null);

-- ----------------------------
-- Table structure for ark_voyage
-- ----------------------------
DROP TABLE IF EXISTS `ark_voyage`;
CREATE TABLE `ark_voyage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `actual_return_day` int(11) DEFAULT NULL,
  `base_sail_days` int(11) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `crisis_count` int(11) DEFAULT NULL,
  `crisis_events` text,
  `current_day_offset` int(11) DEFAULT NULL,
  `departed_at` datetime(6) DEFAULT NULL,
  `departure_day` int(11) NOT NULL,
  `engine_count` int(11) DEFAULT NULL,
  `final_bonus` int(11) DEFAULT NULL,
  `final_sail_days` int(11) DEFAULT NULL,
  `food_loaded` decimal(10,2) DEFAULT NULL,
  `fuel_loaded` decimal(10,2) DEFAULT NULL,
  `has_fisher` bit(1) DEFAULT NULL,
  `has_generator` bit(1) DEFAULT NULL,
  `has_navigator` bit(1) DEFAULT NULL,
  `has_sail` bit(1) DEFAULT NULL,
  `has_weather_watcher` bit(1) DEFAULT NULL,
  `notes` text,
  `passenger_count` int(11) DEFAULT NULL,
  `passenger_list` text,
  `planned_return_day` int(11) DEFAULT NULL,
  `propeller_count` int(11) DEFAULT NULL,
  `returned_at` datetime(6) DEFAULT NULL,
  `sealant_loaded` decimal(10,2) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `total_capacity` int(11) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `voyage_number` int(11) NOT NULL,
  `voyage_result` varchar(50) DEFAULT NULL,
  `wood_loaded` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of ark_voyage
-- ----------------------------

-- ----------------------------
-- Table structure for catastrophe_card
-- ----------------------------
DROP TABLE IF EXISTS `catastrophe_card`;
CREATE TABLE `catastrophe_card` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `card_number` int(11) NOT NULL COMMENT '卡牌编号',
  `name` varchar(50) NOT NULL COMMENT '卡牌名称',
  `description` text NOT NULL COMMENT '卡牌描述',
  `effect_type` varchar(50) DEFAULT NULL COMMENT '效果类型',
  `effect_param1` int(11) DEFAULT '0' COMMENT '效果参数1',
  `effect_param2` int(11) DEFAULT '0' COMMENT '效果参数2',
  `effect_param3` varchar(100) DEFAULT NULL COMMENT '效果参数3（字符串）',
  `is_unique` tinyint(1) DEFAULT '0' COMMENT '是否唯一卡牌',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `card_number` (`card_number`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COMMENT='天灾牌表';

-- ----------------------------
-- Records of catastrophe_card
-- ----------------------------
INSERT INTO `catastrophe_card` VALUES ('1', '1', '低温侵袭', '某处墙体结冰，寒气渗入。本天所有燃料消耗增加20%，木材消耗量15kg→18kg。', 'CONSUMPTION_INCREASE', '20', '15', '18', '0', '2026-05-14 11:53:11', '2026-05-14 11:53:11');
INSERT INTO `catastrophe_card` VALUES ('2', '2', '灾难蔓延', '增加5天暴雪持续时间', 'EXTEND_STORM', '5', '0', '', '0', '2026-05-14 11:53:11', '2026-05-14 11:53:11');
INSERT INTO `catastrophe_card` VALUES ('3', '3', '粮仓鼠患', '仓库中储存的粮食被老鼠啃食，损失10%的食物储备（向下取整）', 'FOOD_LOSS', '10', '0', '', '0', '2026-05-14 11:53:11', '2026-05-14 11:53:11');
INSERT INTO `catastrophe_card` VALUES ('4', '4', '燃料泄漏', '储油桶老化破裂，损失一处仓库的10%的燃料储备（优先扣除煤油/燃料）', 'FUEL_LOSS', '10', '0', '', '0', '2026-05-14 11:53:11', '2026-05-14 11:53:11');
INSERT INTO `catastrophe_card` VALUES ('5', '5', '工具锈蚀', '生产工具普遍老化。当天所有生产行动（渔猎、伐木、挖矿等）产量-20%。', 'PRODUCTION_DECREASE', '20', '0', '', '0', '2026-05-14 11:53:11', '2026-05-14 11:53:11');
INSERT INTO `catastrophe_card` VALUES ('6', '6', '海水倒灌', '风暴潮淹没码头设施，沿海仓库的部分物资被冲走（损失10%），方舟受损10%', 'DOCK_DAMAGE', '10', '10', '', '0', '2026-05-14 11:53:11', '2026-05-14 11:53:11');
INSERT INTO `catastrophe_card` VALUES ('7', '7', '水源污染', '岛上淡水水源被动物尸体污染，所有玩家当天需额外消耗1升煤油（烧开水）或面临患病风险', 'WATER_CONTAMINATION', '1', '0', '', '0', '2026-05-14 11:53:11', '2026-05-14 11:53:11');
INSERT INTO `catastrophe_card` VALUES ('8', '8', '信仰崩塌', '神父以及占卜师等精神领袖陷入自我怀疑，当天无法使用“布道”或“占星”技能。若第三天抽中不影响终局结算加成。', 'SKILL_DISABLE', '0', '0', '布道,占星', '0', '2026-05-14 11:53:11', '2026-05-14 11:53:11');
INSERT INTO `catastrophe_card` VALUES ('9', '9', '燃料受潮', '露天堆放的木柴被雨淋湿。随机一个仓库或玩家损失30kg木材。', 'WOOD_LOSS', '30', '0', '', '0', '2026-05-14 11:53:11', '2026-05-14 11:53:11');
INSERT INTO `catastrophe_card` VALUES ('10', '10', '逃役', '一名劳工趁夜色逃走了。统治者当天指定的劳工名单中，随机一人自动失效（不会劳作，也不会计入劳工）。主持人随机选择，不公开是谁。该玩家知道自己被逃役释放，当天正常进行行动。', 'ESCAPE_LABOR', '0', '0', '', '0', '2026-05-14 11:53:11', '2026-05-14 11:53:11');
INSERT INTO `catastrophe_card` VALUES ('11', '11', '祭品', '有人在教堂门口发现一只被割喉的黑羊。第二天必定触发一张额外天灾牌（命运在积蓄）。', 'EXTRA_CARD', '1', '0', '', '0', '2026-05-14 11:53:11', '2026-05-14 11:53:11');

-- ----------------------------
-- Table structure for catastrophe_deck
-- ----------------------------
DROP TABLE IF EXISTS `catastrophe_deck`;
CREATE TABLE `catastrophe_deck` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `card_id` int(11) NOT NULL COMMENT '天灾牌ID',
  `is_drawn` tinyint(1) DEFAULT '0' COMMENT '是否已抽取',
  `is_used` tinyint(1) DEFAULT '0' COMMENT '是否已使用',
  `drawn_at` datetime DEFAULT NULL COMMENT '抽取时间',
  `used_at` datetime DEFAULT NULL COMMENT '使用时间',
  `round_used` int(11) DEFAULT '0' COMMENT '使用回合',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `card_id` (`card_id`),
  CONSTRAINT `catastrophe_deck_ibfk_1` FOREIGN KEY (`card_id`) REFERENCES `catastrophe_card` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COMMENT='天灾牌组管理表';

-- ----------------------------
-- Records of catastrophe_deck
-- ----------------------------
INSERT INTO `catastrophe_deck` VALUES ('1', '1', '1', '0', '2026-05-23 14:51:36', null, '0', '2026-05-14 11:53:12', '2026-05-23 14:51:36');
INSERT INTO `catastrophe_deck` VALUES ('2', '1', '1', '0', '2026-05-24 17:14:48', null, '0', '2026-05-14 11:53:12', '2026-05-24 17:14:48');
INSERT INTO `catastrophe_deck` VALUES ('3', '1', '0', '0', null, null, '0', '2026-05-14 11:53:12', '2026-05-14 14:56:48');
INSERT INTO `catastrophe_deck` VALUES ('4', '2', '1', '0', '2026-05-24 17:14:57', null, '0', '2026-05-14 11:53:12', '2026-05-24 17:14:57');
INSERT INTO `catastrophe_deck` VALUES ('5', '2', '1', '0', '2026-05-23 14:33:02', null, '0', '2026-05-14 11:53:12', '2026-05-23 14:33:02');
INSERT INTO `catastrophe_deck` VALUES ('6', '2', '1', '0', '2026-05-22 18:42:55', null, '0', '2026-05-14 11:53:12', '2026-05-22 18:42:55');
INSERT INTO `catastrophe_deck` VALUES ('7', '3', '1', '1', '2026-05-23 14:34:38', '2026-05-23 14:47:32', '1', '2026-05-14 11:53:12', '2026-05-23 14:47:32');
INSERT INTO `catastrophe_deck` VALUES ('8', '3', '1', '0', '2026-05-24 17:14:54', null, '0', '2026-05-14 11:53:12', '2026-05-24 17:14:54');
INSERT INTO `catastrophe_deck` VALUES ('9', '3', '0', '0', null, null, '0', '2026-05-14 11:53:12', '2026-05-14 14:56:48');
INSERT INTO `catastrophe_deck` VALUES ('10', '4', '0', '0', null, null, '0', '2026-05-14 11:53:12', '2026-05-14 14:56:48');
INSERT INTO `catastrophe_deck` VALUES ('11', '4', '0', '0', null, null, '0', '2026-05-14 11:53:12', '2026-05-14 14:56:48');
INSERT INTO `catastrophe_deck` VALUES ('12', '4', '1', '0', '2026-05-22 18:42:55', null, '0', '2026-05-14 11:53:12', '2026-05-22 18:42:55');
INSERT INTO `catastrophe_deck` VALUES ('13', '5', '1', '0', '2026-05-23 14:51:29', null, '0', '2026-05-14 11:53:12', '2026-05-23 14:51:29');
INSERT INTO `catastrophe_deck` VALUES ('14', '5', '1', '0', '2026-05-23 14:33:02', null, '0', '2026-05-14 11:53:12', '2026-05-23 14:33:02');
INSERT INTO `catastrophe_deck` VALUES ('15', '5', '1', '0', '2026-05-23 14:33:02', null, '0', '2026-05-14 11:53:12', '2026-05-23 14:33:02');
INSERT INTO `catastrophe_deck` VALUES ('16', '6', '1', '0', '2026-05-23 14:51:29', null, '0', '2026-05-14 11:53:12', '2026-05-23 14:51:29');
INSERT INTO `catastrophe_deck` VALUES ('17', '6', '0', '0', null, null, '0', '2026-05-14 11:53:12', '2026-05-14 14:56:48');
INSERT INTO `catastrophe_deck` VALUES ('18', '6', '1', '1', '2026-05-24 17:14:57', '2026-05-24 17:29:10', '1', '2026-05-14 11:53:12', '2026-05-24 17:29:10');
INSERT INTO `catastrophe_deck` VALUES ('19', '7', '1', '0', '2026-05-24 17:14:54', null, '0', '2026-05-14 11:53:12', '2026-05-24 17:14:54');
INSERT INTO `catastrophe_deck` VALUES ('20', '7', '1', '0', '2026-05-24 17:14:54', null, '0', '2026-05-14 11:53:12', '2026-05-24 17:14:54');
INSERT INTO `catastrophe_deck` VALUES ('21', '7', '0', '0', null, null, '0', '2026-05-14 11:53:12', '2026-05-14 14:56:48');
INSERT INTO `catastrophe_deck` VALUES ('22', '8', '0', '0', null, null, '0', '2026-05-14 11:53:12', '2026-05-14 14:56:48');
INSERT INTO `catastrophe_deck` VALUES ('23', '8', '1', '0', '2026-05-24 17:14:57', null, '0', '2026-05-14 11:53:12', '2026-05-24 17:14:57');
INSERT INTO `catastrophe_deck` VALUES ('24', '8', '1', '0', '2026-05-23 14:34:38', null, '0', '2026-05-14 11:53:12', '2026-05-23 14:34:38');
INSERT INTO `catastrophe_deck` VALUES ('25', '9', '1', '0', '2026-05-24 17:14:48', null, '0', '2026-05-14 11:53:12', '2026-05-24 17:14:48');
INSERT INTO `catastrophe_deck` VALUES ('26', '9', '0', '0', null, null, '0', '2026-05-14 11:53:12', '2026-05-14 14:56:48');
INSERT INTO `catastrophe_deck` VALUES ('27', '9', '1', '0', '2026-05-23 14:51:36', null, '0', '2026-05-14 11:53:12', '2026-05-23 14:51:36');
INSERT INTO `catastrophe_deck` VALUES ('28', '10', '0', '0', null, null, '0', '2026-05-14 11:53:12', '2026-05-14 14:56:48');
INSERT INTO `catastrophe_deck` VALUES ('29', '10', '1', '1', '2026-05-23 14:51:36', '2026-05-23 15:00:20', '1', '2026-05-14 11:53:12', '2026-05-23 15:00:20');
INSERT INTO `catastrophe_deck` VALUES ('30', '10', '1', '0', '2026-05-23 14:34:38', null, '0', '2026-05-14 11:53:12', '2026-05-23 14:34:38');
INSERT INTO `catastrophe_deck` VALUES ('31', '11', '1', '0', '2026-05-23 14:51:29', null, '0', '2026-05-14 11:53:12', '2026-05-23 14:51:29');
INSERT INTO `catastrophe_deck` VALUES ('32', '11', '1', '0', '2026-05-24 17:14:48', null, '0', '2026-05-14 11:53:12', '2026-05-24 17:14:48');
INSERT INTO `catastrophe_deck` VALUES ('33', '11', '1', '1', '2026-05-22 18:42:55', '2026-05-22 20:58:46', '1', '2026-05-14 11:53:12', '2026-05-22 20:58:46');

-- ----------------------------
-- Table structure for catastrophe_progress
-- ----------------------------
DROP TABLE IF EXISTS `catastrophe_progress`;
CREATE TABLE `catastrophe_progress` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `progress` int(11) NOT NULL DEFAULT '0' COMMENT '天灾进度值 0-100',
  `last_updated_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_single_record` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='天灾进度表';

-- ----------------------------
-- Records of catastrophe_progress
-- ----------------------------
INSERT INTO `catastrophe_progress` VALUES ('1', '67', '2026-05-24 17:14:37', '2026-05-14 11:53:11', '2026-05-24 17:14:37');

-- ----------------------------
-- Table structure for drawn_cards
-- ----------------------------
DROP TABLE IF EXISTS `drawn_cards`;
CREATE TABLE `drawn_cards` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `draw_round` int(11) NOT NULL COMMENT '抽取轮次',
  `deck_id` int(11) NOT NULL COMMENT '牌组ID',
  `position` int(11) NOT NULL COMMENT '位置（1-3）',
  `is_selected` tinyint(1) DEFAULT '0' COMMENT '是否被选中',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_draw_position` (`draw_round`,`position`),
  KEY `deck_id` (`deck_id`),
  CONSTRAINT `drawn_cards_ibfk_1` FOREIGN KEY (`deck_id`) REFERENCES `catastrophe_deck` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COMMENT='抽取牌记录表';

-- ----------------------------
-- Records of drawn_cards
-- ----------------------------
INSERT INTO `drawn_cards` VALUES ('1', '1', '12', '1', '0', '2026-05-22 18:42:55');
INSERT INTO `drawn_cards` VALUES ('2', '1', '6', '2', '0', '2026-05-22 18:42:55');
INSERT INTO `drawn_cards` VALUES ('3', '1', '33', '3', '0', '2026-05-22 18:42:55');
INSERT INTO `drawn_cards` VALUES ('4', '2', '15', '1', '0', '2026-05-23 14:33:02');
INSERT INTO `drawn_cards` VALUES ('5', '2', '5', '2', '0', '2026-05-23 14:33:02');
INSERT INTO `drawn_cards` VALUES ('6', '2', '14', '3', '0', '2026-05-23 14:33:02');
INSERT INTO `drawn_cards` VALUES ('7', '3', '30', '1', '0', '2026-05-23 14:34:38');
INSERT INTO `drawn_cards` VALUES ('8', '3', '24', '2', '0', '2026-05-23 14:34:38');
INSERT INTO `drawn_cards` VALUES ('9', '3', '7', '3', '0', '2026-05-23 14:34:38');
INSERT INTO `drawn_cards` VALUES ('10', '4', '31', '1', '0', '2026-05-23 14:51:29');
INSERT INTO `drawn_cards` VALUES ('11', '4', '16', '2', '0', '2026-05-23 14:51:29');
INSERT INTO `drawn_cards` VALUES ('12', '4', '13', '3', '0', '2026-05-23 14:51:29');
INSERT INTO `drawn_cards` VALUES ('13', '5', '27', '1', '0', '2026-05-23 14:51:36');
INSERT INTO `drawn_cards` VALUES ('14', '5', '29', '2', '0', '2026-05-23 14:51:36');
INSERT INTO `drawn_cards` VALUES ('15', '5', '1', '3', '0', '2026-05-23 14:51:36');
INSERT INTO `drawn_cards` VALUES ('16', '6', '25', '1', '0', '2026-05-24 17:14:48');
INSERT INTO `drawn_cards` VALUES ('17', '6', '32', '2', '0', '2026-05-24 17:14:48');
INSERT INTO `drawn_cards` VALUES ('18', '6', '2', '3', '0', '2026-05-24 17:14:48');
INSERT INTO `drawn_cards` VALUES ('19', '7', '20', '1', '0', '2026-05-24 17:14:54');
INSERT INTO `drawn_cards` VALUES ('20', '7', '8', '2', '0', '2026-05-24 17:14:54');
INSERT INTO `drawn_cards` VALUES ('21', '7', '19', '3', '0', '2026-05-24 17:14:54');
INSERT INTO `drawn_cards` VALUES ('22', '8', '23', '1', '0', '2026-05-24 17:14:57');
INSERT INTO `drawn_cards` VALUES ('23', '8', '4', '2', '0', '2026-05-24 17:14:57');
INSERT INTO `drawn_cards` VALUES ('24', '8', '18', '3', '0', '2026-05-24 17:14:57');

-- ----------------------------
-- Table structure for endgame_ark_event
-- ----------------------------
DROP TABLE IF EXISTS `endgame_ark_event`;
CREATE TABLE `endgame_ark_event` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `description` text NOT NULL,
  `category` varchar(50) DEFAULT NULL,
  `sort_order` int(11) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of endgame_ark_event
-- ----------------------------
INSERT INTO `endgame_ark_event` VALUES ('1', '巨浪拍舷', '一道突如其来的侧浪狠狠拍打船身，甲板上的物资被冲得七零八落，几个水手差点落水。\n\n效果：随机损失5%的食物（被浪卷走）。若拥有\"航海\"技能玩家或额外消耗50kg燃料紧急调整航向，可将损失降至0。', '危机', '1', null, null);
INSERT INTO `endgame_ark_event` VALUES ('2', '帆布撕裂', '强风将主帆撕开一道大口子，帆布在风中啪啪作响，船速骤降。\n\n效果：本日及后续航行天数增加1天（因减速）。若消耗10米帆布或\"维修\"技能玩家现场缝补，可免于增加天数。若无帆船（纯发动机），此事件无效，重新抽取。', '危机', '2', null, null);
INSERT INTO `endgame_ark_event` VALUES ('3', '淡水变质', '储存的桶装淡水出现绿藻和异味，部分已不可饮用。\n\n效果：损失10%的食物储备。消耗5份医疗资源或200kg木材可净化剩余淡水，将损失降至5%。否则获得1个\"患病\"标记。', '危机', '3', null, null);
INSERT INTO `endgame_ark_event` VALUES ('4', '发动机过热', '因长时间高负荷运转，发动机冷却系统失效，缸体发红，必须停机。\n\n效果：若不修复，航行强制暂停1天（需原地等待冷却）。消耗1个维修工具包或拥有\"维修\"技能玩家可当场修复，不损失时间。', '危机', '4', null, null);
INSERT INTO `endgame_ark_event` VALUES ('5', '晕船蔓延', '连续颠簸让超过一半人晕船呕吐，体力下降，士气低迷。\n\n效果：获得1个\"患病\"标记（严重晕船）。若拥有\"医疗\"技能玩家或消耗5份朗姆酒可缓解。', '危机', '5', null, null);
INSERT INTO `endgame_ark_event` VALUES ('6', '暗礁险情', '海面下隐约看到黑色礁石轮廓，舵手紧急打舵。\n\n效果：若拥有\"海洋导航\"技能玩家，可安全绕行。否则船底擦伤，船体完整度下降5%，并损失5%燃料（泄漏）。', '危机', '6', null, null);
INSERT INTO `endgame_ark_event` VALUES ('7', '食物中毒', '一批腌肉在高温下变质，多人食用后腹痛腹泻。\n\n效果：获得1个\"患病\"标记，并损失10%食物（变质部分丢弃）。消耗5份医疗资源可救治所有人，避免患病标记。', '危机', '7', null, null);
INSERT INTO `endgame_ark_event` VALUES ('8', '内部盗窃', '有人趁夜偷走部分燃料和食物，藏在个人箱子里。被发现后引发争执。\n\n效果：若不消耗2个朗姆酒则损失5%燃料和5%食物（被窃并挥霍），且随机1名玩家受伤（斗殴）。', '危机', '8', null, null);
INSERT INTO `endgame_ark_event` VALUES ('9', '螺旋桨缠绕', '漂浮的渔网或海草缠住了螺旋桨，动力下降，船体抖动。\n\n效果：需有玩家或拥有\"格斗\"/\"潜水\"技能玩家下水清理，或搁置耗时加1天。可消耗1个维修工具包远程切断。', '危机', '9', null, null);
INSERT INTO `endgame_ark_event` VALUES ('10', '暴风雨迷航', '一场无预报的暴风雨遮天蔽日，指南针失灵，航向偏移。\n\n效果：若拥有\"天气预测\"技能玩家或海图（特殊物品），可校准航向，无损失。否则航行天数增加1天，并额外消耗10%燃料。', '危机', '10', null, null);
INSERT INTO `endgame_ark_event` VALUES ('11', '舱底进水', '船底板接缝处渗水，水泵来不及抽排，水位缓慢上升。\n\n效果：消耗500kg木材和1个维修技能可堵漏。否则每过1天，船体完整度下降5%且获得1个\"患病\"标记（潮湿寒冷）。', '危机', '11', null, null);
INSERT INTO `endgame_ark_event` VALUES ('12', '传染病爆发', '一种类似流感的疾病在密闭船舱中迅速传播，咳嗽声此起彼伏。\n\n效果：获得1个\"患病\"标记。消耗5份医疗资源或拥有\"医疗\"或\"布道\"玩家可控制疫情，只获得1个\"患病\"标记。', '危机', '12', null, null);
INSERT INTO `endgame_ark_event` VALUES ('13', '桅杆断裂', '一阵强风拦腰折断主桅，帆具垮塌，甲板一片狼藉。\n\n效果：船体完整度下降10%。若拥有500kg木材可临时修复，只增加1天。', '危机', '13', null, null);
INSERT INTO `endgame_ark_event` VALUES ('14', '漂浮的救援物资', '桅杆上的瞭望手突然大喊：\"左舷海面上有漂浮的桶！\"你们靠近后发现，那正是你们之前通过邮局电报机向外发送求救信号的回应——一艘货轮在暴风雪来临前抛下了救援物资桶，上面还绑着一面沾着冰霜的旗帜。\n\n效果：获得20单位食物+50kg燃料（煤油/柴油）+1个医疗包（可视为10份医疗资源）。若船上拥有\"天气预测\"或\"海洋导航\"技能玩家，还能额外找到1个维修工具包。这些物资直接进入方舟公共仓库。', '希望', '14', null, null);
INSERT INTO `endgame_ark_event` VALUES ('15', '顺风加速', '风向突然转为顺风，帆面鼓满，船头像切开黄油一样劈开浪花。舵手兴奋地吹起口哨。\n\n效果：航行天数减少1天。同时，本日不消耗额外燃料（发动机可停机半天）。', '希望', '15', null, null);
INSERT INTO `endgame_ark_event` VALUES ('16', '海鸟引路', '一群海鸟盘旋在船头方向，不时俯冲入水。经验丰富的水手说：\"跟着它们，前面必有浅滩或岛屿。\"\n\n效果：第二天航海检定自动成功（相当于一次免费绕开险情）。', '希望', '16', null, null);
INSERT INTO `endgame_ark_event` VALUES ('17', '漂浮的救援物资', '桅杆上的瞭望手突然大喊：\"左舷海面上有漂浮的桶！\"你们靠近后发现，那正是你们之前通过邮局电报机向外发送求救信号的回应——一艘货轮在暴风雪来临前抛下了救援物资桶，上面还绑着一面沾着冰霜的旗帜。\n\n效果：获得20单位食物+50kg燃料（煤油/柴油）+1个医疗包（可视为10份医疗资源）。若船上拥有\"天气预测\"或\"海洋导航\"技能玩家，还能额外找到1个维修工具包。这些物资直接进入方舟公共仓库。', '希望', '17', null, null);
INSERT INTO `endgame_ark_event` VALUES ('18', '漂浮的救援物资', '桅杆上的瞭望手突然大喊：\"左舷海面上有漂浮的桶！\"你们靠近后发现，那正是你们之前通过邮局电报机向外发送求救信号的回应——一艘货轮在暴风雪来临前抛下了救援物资桶，上面还绑着一面沾着冰霜的旗帜。\n\n效果：获得20单位食物+50kg燃料（煤油/柴油）+1个医疗包（可视为10份医疗资源）。若船上拥有\"天气预测\"或\"海洋导航\"技能玩家，还能额外找到1个维修工具包。这些物资直接进入方舟公共仓库。', '希望', '18', null, null);
INSERT INTO `endgame_ark_event` VALUES ('19', '漂浮的救援物资', '桅杆上的瞭望手突然大喊：\"左舷海面上有漂浮的桶！\"你们靠近后发现，那正是你们之前通过邮局电报机向外发送求救信号的回应——一艘货轮在暴风雪来临前抛下了救援物资桶，上面还绑着一面沾着冰霜的旗帜。\n\n效果：获得20单位食物+50kg燃料（煤油/柴油）+1个医疗包（可视为10份医疗资源）。若船上拥有\"天气预测\"或\"海洋导航\"技能玩家，还能额外找到1个维修工具包。这些物资直接进入方舟公共仓库。', '希望', '19', null, null);

-- ----------------------------
-- Table structure for endgame_shelter_event
-- ----------------------------
DROP TABLE IF EXISTS `endgame_shelter_event`;
CREATE TABLE `endgame_shelter_event` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `description` text NOT NULL,
  `category` varchar(50) DEFAULT NULL,
  `sort_order` int(11) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of endgame_shelter_event
-- ----------------------------
INSERT INTO `endgame_shelter_event` VALUES ('1', '岩层渗水', '避难所深处的一面岩壁开始渗水，泥浆顺着裂缝流下来，空气变得潮湿阴冷。如果不加固，可能会引发更大规模塌方或积水。如果为牢固避难所则此事件失效。\n\n效果：若拥有100kg石料或500kg木材，可选择消耗其一进行加固，事件安全解决。否则，避难所湿度持续上升，获得1个\"患病\"标记（因呼吸道疾病或风湿），且下一轮燃料消耗增加10%（因取暖需求上升）。', '危机', '1', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('2', '通风道堵塞', '主通风道被落石和冻土堵死，二氧化碳浓度缓慢攀升，人们开始头痛、昏沉。\n\n效果：若拥有\"维修\"或\"格斗\"能力的玩家（或NPC）手动清理，或消耗200kg木材临时搭建辅助通风管，可恢复通风。否则，增加1个\"患病\"标记。', '危机', '2', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('3', '地下暗涌', '一场小型地下水脉破裂，冲毁了部分储备区。\n\n效果：随机损失10%～20%的燃料（优先煤油、柴油）和5%～10%的食物（被水浸泡）。若拥有5000kg石料或10名劳工可紧急筑坝，将损失归零。牢固避难所消耗资源减半。', '危机', '3', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('4', '集体幻觉', '长期不见日光和心理压力导致数人产生幻觉，他们尖叫着冲向紧急出口，引发混乱。\n\n效果：若无拥有\"布道\"或\"医疗\"技能的玩家进行安抚，混乱中造成1人受伤，并产生1个\"患病\"标记（精神创伤）。消耗5份医疗资源可代替技能平定事态。', '危机', '4', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('5', '燃料挥发泄漏', '储存的煤油桶因锈蚀出现微小裂缝，挥发气体聚集在低洼处，刺鼻且易燃。\n\n效果：强制损失10%的燃料储备（挥发浪费）。若拥有维修工具包或消耗100kg金属制品修补桶体，则只损失5%的燃料储备。', '危机', '5', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('6', '鼠群入侵', '一群耐寒的老鼠咬穿了一道木质隔板，开始在粮袋间肆虐。牢固避难所不结算此效果。\n\n效果：损失10%的食物。', '危机', '6', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('7', '心理崩溃——自残', '一名避难者突然用尖锐工具划伤自己，鲜血和尖叫让所有人都绷紧了神经。\n\n效果：消耗5份医疗资源救治伤者，并额外消耗1份朗姆酒或10份食物用于安抚群众。否则获得1个\"患病\"标记（集体抑郁）。', '危机', '7', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('8', '支柱朽坏', '一根支撑主通道的木梁发出碎裂声，顶部碎石摇摇欲坠。\n\n效果：若拥有500kg木材或200kg石材可替换，事件安全。否则发生局部塌方，随机2～5名玩家受伤（获得\"受伤\"标记，无法生产但可医疗），并损失10%的公共物资（被埋）。', '危机', '8', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('9', '炭火中毒', '有人在密闭小隔间中使用炭火取暖，一氧化碳扩散到主厅，多人呕吐晕眩。\n\n效果：消耗5份医疗资源进行高治疗，或拥有通风改造（之前解决过通风事件可免疫）。否则产生2个\"患病\"标记。', '危机', '9', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('10', '医疗物资污染', '因潮湿和霉菌，部分绷带、药品和消毒剂变质失效。\n\n效果：若拥有500kg燃料用于高温消毒处理剩余物资，可将损失降至10%。否则损失20%的医疗资源（向下取整）。', '危机', '10', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('11', '地下冰裂——寒气入侵', '避难所下方开裂，寒气从地面缝隙中冒出，温度骤降。\n\n效果：当天及之后每天燃料消耗增加10%。若拥有100kg石料或200kg木材铺地隔绝，可消除此效果（牢固避难所消耗材料降低20%）。', '危机', '11', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('12', '污水倒灌', '简易厕所和排污管堵塞，脏水溢出，弥漫恶臭。\n\n效果：消耗100kg木材可彻底疏通，或立即获得2个\"患病\"标记。', '危机', '12', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('13', '粮仓霉变', '储存区湿度过高，谷物表面出现绿毛和黑斑。\n\n效果：损失10%的食物储备（霉坏）。若立即消耗50kg燃料或750kg木材进行烘干处理，可挽回一半损失（即只损失5%）。', '危机', '13', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('14', '坍塌？', '避难所顶部出现了坍塌，导致了2名随机玩家受伤，交付10医疗资源可通过此次判定，并且矿场仓库得到了联通，你们可以尝试获得10000kg的矿场仓库的资源到避难所', '希望', '14', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('15', '无事发生', '这事好事，不是吗', '希望', '15', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('16', '无事发生', '这是好事，不是吗', '希望', '16', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('17', '哪里来的吃的？？！！', '那么在避难所的门口冲撞到了避难所的门口，他们似乎是想要进入这个最后的温暖，可惜，大门是紧闭的，你们来不及默哀，只是无奈地把它们的尸体拖了进来，你们获得了100kg的食物', '希望', '17', null, null);
INSERT INTO `endgame_shelter_event` VALUES ('18', '哪里来的吃的的？？！！', '那么在避难所的门口冲撞到了避难所的门口，他们似乎是想要进入这个最后的温暖，可惜，大门是紧闭的，你们来不及默哀，只是无奈地把它们的尸体拖了进来，你们获得了100kg的食物', '希望', '18', null, null);

-- ----------------------------
-- Table structure for faction_action
-- ----------------------------
DROP TABLE IF EXISTS `faction_action`;
CREATE TABLE `faction_action` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL COMMENT '提交玩家ID',
  `player_name` varchar(50) DEFAULT NULL COMMENT '玩家名称快照',
  `faction` varchar(20) NOT NULL COMMENT '提交时阵营',
  `action_type` varchar(40) NOT NULL COMMENT '阵营行动类型：assign_personnel/assign_guard/sabotage等',
  `payload` text COMMENT 'JSON输入数据',
  `result` text COMMENT '行动结果/DM反馈',
  `status` enum('pending','feedbacked') NOT NULL DEFAULT 'pending' COMMENT '反馈状态',
  `game_day` int(11) NOT NULL DEFAULT '1' COMMENT '游戏天数',
  `phase` varchar(20) DEFAULT 'day' COMMENT '阶段：day白天/night夜晚',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_player_day` (`player_id`,`game_day`),
  KEY `idx_faction_day` (`faction`,`game_day`),
  KEY `idx_action_type` (`action_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=utf8mb4 COMMENT='阵营行动表';

-- ----------------------------
-- Records of faction_action
-- ----------------------------
INSERT INTO `faction_action` VALUES ('15', '10', '二阶堂希罗', '统治者', 'assign_guard', '{\"actorId\":24,\"targetLocationId\":2}', '✓ 已提交【安排看守】\n\n看守人员：日落忽悠的花海\n看守地点：镇长厅\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。\n\n【DM反馈】\nok，行动已执行', 'feedbacked', '1', 'day', '2026-05-22 19:17:22', '2026-05-22 22:00:09');
INSERT INTO `faction_action` VALUES ('16', '14', 'Κάκτος西里尔', '冒险者', 'guard_ark', '{\"guardId\":14,\"useWeaponOrSkill\":true}', '✓ 已提交【看守方舟】\n\n看守人员：Κάκτος西里尔\n使用武器/技能计入防御：是\n额外防御：+2\n\n等待主持人确认。\n\n【DM反馈】\nok，行动已执行', 'feedbacked', '1', 'day', '2026-05-22 19:40:46', '2026-05-22 22:03:16');
INSERT INTO `faction_action` VALUES ('17', '9', '对酒', '冒险者', 'extra_investigate', '{\"investigateType\":\"investigate_location\",\"targetId\":5}', '✓ 已提交【额外调查】\n\n调查类型：调查地点\n调查目标：灯塔\n\n结算后该次调查数量将翻倍。等待主持人确认。\n\n【DM反馈】\n矗立在小镇岬角的白色石塔，约20米高。顶部的菲涅尔透镜在夜间旋转，光束扫过海面。塔底是灯塔看守员的住所。这里是灯塔管理员的住所，防御值：7', 'feedbacked', '1', 'day', '2026-05-22 19:47:02', '2026-05-22 22:12:43');
INSERT INTO `faction_action` VALUES ('18', '24', '花海', '反叛者', 'extra_labor', '{\"note\":\"\"}', '✓ 已提交【额外劳动】\n\n提交者：花海\n效果：今日生产类自由行动产出 +50%。\n（须今日已提交生产行动）\n等待主持人确认。\n\n【DM反馈】\nok，已经行行动', 'feedbacked', '1', 'day', '2026-05-22 20:09:52', '2026-05-22 22:12:17');
INSERT INTO `faction_action` VALUES ('19', '10', '二阶堂希罗', '统治者', 'assign_guard', '{\"actorId\":12,\"targetLocationId\":18}', '✓ 已提交【安排看守】\n\n看守人员：千代\n看守地点：矿场\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。\n\n【DM反馈】\n人员已经前往', 'feedbacked', '1', 'day', '2026-05-22 20:14:32', '2026-05-22 22:32:03');
INSERT INTO `faction_action` VALUES ('20', '19', 'unPy-GPT', '冒险者', 'extra_investigate', '{\"investigateType\":\"investigate_player\",\"targetId\":8}', '✓ 已提交【额外调查】\n\n调查类型：调查玩家\n调查目标：兔兔\n\n结算后该次调查数量将翻倍。等待主持人确认。\n\n【DM反馈】\n兔兔调查千代与凭栏择雨', 'feedbacked', '1', 'day', '2026-05-22 20:31:08', '2026-05-22 22:13:51');
INSERT INTO `faction_action` VALUES ('21', '20', '追枫', '天灾使者', 'curse', '{\"weaponId\":2,\"target1\":8,\"target2\":27}', '✓ 已提交【诅咒】\n\n消耗武器：猎枪\n目标1：兔兔\n目标2：得狗的老意\n\n效果：获知阵营、施加「诅咒」标记。\n等待主持人确认。\n\n【DM反馈】\n成功', 'feedbacked', '1', 'day', '2026-05-22 20:36:05', '2026-05-22 22:13:12');
INSERT INTO `faction_action` VALUES ('22', '16', '孤城暮角', '天灾使者', 'extra_investigate', '{\"investigateType\":\"investigate_location\",\"targetId\":18}', '✓ 已提交【额外调查】\n\n调查类型：调查地点\n调查目标：矿场\n\n结算后该次调查数量将翻倍。等待主持人确认。\n\n【DM反馈】\n深入山腹的矿道与地面设施的综合体，包含管理室、矿场仓库和地下矿场。由统治者共同管理，是岛上最重要的资源产地和战略要地。\n统治者在这里进行守卫，阻止了你进一步靠近。地点防御值为10', 'feedbacked', '1', 'day', '2026-05-22 20:40:48', '2026-05-22 22:15:12');
INSERT INTO `faction_action` VALUES ('23', '13', '凭栏择雨', '反叛者', 'extra_action', '{\"actionType\":\"use_skill\",\"targetLocationId\":null,\"targetPlayerId\":null,\"note\":\"\"}', '✓ 已提交【额外行动】\n\n行动类型：使用职业技能\n\n此环节无法寻找NPC进行对话。\n等待主持人确认。\n\n【DM反馈】\n一座用珊瑚石和木材建造的小教堂，彩色玻璃窗描绘着基督与太平洋岛屿的景象。尖顶上的十字架在阳光下泛着白漆剥落后的斑驳。教堂内长椅简陋，但祭坛前摆着一架巨大的老旧管风琴，积满灰尘。地点防御值：4', 'feedbacked', '1', 'day', '2026-05-22 21:31:25', '2026-05-22 22:23:26');
INSERT INTO `faction_action` VALUES ('24', '11', '蟋蟀蜥蜴', '反叛者', 'extra_action', '{\"actionType\":\"use_skill\",\"targetLocationId\":null,\"targetPlayerId\":null,\"note\":\"寻找有没有打架斗殴的人并劝架\"}', '✓ 已提交【额外行动】\n\n行动类型：使用职业技能\n备注：寻找有没有打架斗殴的人并劝架\n\n此环节无法寻找NPC进行对话。\n等待主持人确认。\n\n【DM反馈】\n在你下一次袭击监狱时，监狱的防御值会-2', 'feedbacked', '1', 'day', '2026-05-22 21:36:49', '2026-05-22 22:28:23');
INSERT INTO `faction_action` VALUES ('25', '23', '教皇', '反叛者', 'extra_action', '{\"actionType\":\"use_skill\",\"targetLocationId\":null,\"targetPlayerId\":null,\"note\":\"布道，对农业协会的农户及成员使用\"}', '✓ 已提交【额外行动】\n\n行动类型：使用职业技能\n备注：布道，对农业协会的农户及成员使用\n\n此环节无法寻找NPC进行对话。\n等待主持人确认。\n\n【DM反馈】\n选择随机三人进行加成', 'feedbacked', '1', 'day', '2026-05-22 21:43:31', '2026-05-22 22:29:19');
INSERT INTO `faction_action` VALUES ('26', '22', '11', '天灾使者', 'sabotage', '{\"targetLocationId\":12,\"facilityId\":10}', '✓ 已提交【破坏】\n\n目标设施：烘焙炉（面包店）\n\n等待主持人确认。\n\n【DM反馈】\n设施已破坏', 'feedbacked', '1', 'day', '2026-05-22 22:44:42', '2026-05-22 22:48:29');
INSERT INTO `faction_action` VALUES ('27', '20', '追枫', '天灾使者', 'curse', '{\"weaponId\":7,\"target1\":23,\"target2\":31}', '✓ 已提交【诅咒】\n\n消耗武器：猎弓\n目标1：教皇\n目标2：闲屿\n\n效果：获知阵营、施加「诅咒」标记。\n等待主持人确认。\n\n【DM反馈】\n好的，诅咒已生效', 'feedbacked', '2', 'day', '2026-05-23 19:42:55', '2026-05-23 20:59:21');
INSERT INTO `faction_action` VALUES ('28', '11', '蟋蟀蜥蜴', '反叛者', 'extra_action', '{\"actionType\":\"go_location\",\"targetLocationId\":2,\"targetPlayerId\":null,\"note\":\"调查一下有没有什么人来过这里干了什么\"}', '✓ 已提交【额外行动】\n\n行动类型：前往地点\n前往地点：镇长厅\n备注：调查一下有没有什么人来过这里干了什么\n\n此环节无法寻找NPC进行对话。\n等待主持人确认。\n\n【DM反馈】\n镇长厅\n描述：两层殖民风格木楼，带有宽敞的阳台和百叶窗。楼下是办公室和接待室，楼上是行政长官的私人住所。墙上挂着英王乔治六世的肖像和殖民地地图。吊扇无力地转动着。\n设施：镇武库仓库\n发电机1\n人员：镇长（外出则无人）\n防御值：5\n你调查了一下，但是无法获知是否有人来过', 'feedbacked', '2', 'day', '2026-05-23 20:11:14', '2026-05-23 21:05:48');
INSERT INTO `faction_action` VALUES ('29', '9', '对酒', '冒险者', 'extra_investigate', '{\"investigateType\":\"investigate_location\",\"targetId\":13}', '✓ 已提交【额外调查】\n\n调查类型：调查地点\n调查目标：气象观测站\n\n结算后该次调查数量将翻倍。等待主持人确认。\n\n【DM反馈】\n气象观测站\n描述：小镇边缘的一座独立铁皮屋，屋顶有风速仪和天线。屋内摆满了精密的（虽然老旧）仪器和手绘的气象图。\n人员：气象观测员（外出则无人）\n防御值：3', 'feedbacked', '2', 'day', '2026-05-23 20:13:52', '2026-05-23 21:19:30');
INSERT INTO `faction_action` VALUES ('30', '30', 'MISD330', '反叛者', 'extra_action', '{\"actionType\":\"go_location\",\"targetLocationId\":19,\"targetPlayerId\":null,\"note\":\"调查监狱\"}', '✓ 已提交【额外行动】\n\n行动类型：前往地点\n前往地点：监狱\n备注：调查监狱\n\n此环节无法寻找NPC进行对话。\n等待主持人确认。\n\n【DM反馈】\n防御值: 8\n设施:监牢8，看守室（内有桌子一张、煤油灯一盏、警棍一根）\n审讯椅（铁制，带锁扣）\n描述:小镇边缘的一座灰石建筑，铁门锈迹斑斑，窗户窄得像枪眼。门前挂着一盏永远不灭的煤油灯，灯下总坐着一个看守。里面是两排铁牢房，地上铺着发霉的稻草，墙角堆着脏得看不出颜色的毯子。墙上用木炭刻满了前囚犯的名字和诅咒，有些已经被重复刻了三四遍。空气里弥漫着尿骚味和铁锈味，偶尔有人敲一下铁栏杆，声音能传到半个镇子。', 'feedbacked', '2', 'day', '2026-05-23 20:28:22', '2026-05-23 21:15:39');
INSERT INTO `faction_action` VALUES ('31', '10', '二阶堂希罗', '统治者', 'assign_guard', '{\"actorId\":24,\"targetLocationId\":18}', '✓ 已提交【安排看守】\n\n看守人员：花海\n看守地点：矿场\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。\n\n【DM反馈】\n对方抗命', 'feedbacked', '2', 'day', '2026-05-23 21:26:03', '2026-05-23 22:23:03');
INSERT INTO `faction_action` VALUES ('32', '10', '二阶堂希罗', '统治者', 'assign_guard', '{\"actorId\":13,\"targetLocationId\":19}', '✓ 已提交【安排看守】\n\n看守人员：凭栏择雨\n看守地点：监狱\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。\n\n【DM反馈】\n对方抗命', 'feedbacked', '2', 'day', '2026-05-23 21:26:37', '2026-05-23 22:23:11');
INSERT INTO `faction_action` VALUES ('33', '10', '二阶堂希罗', '统治者', 'assign_guard', '{\"actorId\":27,\"targetLocationId\":19}', '✓ 已提交【安排看守】\n\n看守人员：得狗的老意\n看守地点：监狱\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。\n\n【DM反馈】\n好的', 'feedbacked', '2', 'day', '2026-05-23 21:27:31', '2026-05-23 21:43:22');
INSERT INTO `faction_action` VALUES ('34', '10', '二阶堂希罗', '统治者', 'assign_guard', '{\"actorId\":23,\"targetLocationId\":19}', '✓ 已提交【安排看守】\n\n看守人员：教皇\n看守地点：监狱\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。\n\n【DM反馈】\n对方抗命', 'feedbacked', '2', 'day', '2026-05-23 21:28:06', '2026-05-23 22:22:54');
INSERT INTO `faction_action` VALUES ('35', '10', '二阶堂希罗', '统治者', 'assign_guard', '{\"actorId\":30,\"targetLocationId\":19}', '✓ 已提交【安排看守】\n\n看守人员：MISD330\n看守地点：监狱\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。\n\n【DM反馈】\n对方抗命', 'feedbacked', '2', 'day', '2026-05-23 21:28:16', '2026-05-23 22:24:58');
INSERT INTO `faction_action` VALUES ('36', '10', '二阶堂希罗', '统治者', 'assign_guard', '{\"actorId\":16,\"targetLocationId\":19}', '✓ 已提交【安排看守】\n\n看守人员：孤城暮角\n看守地点：监狱\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。\n\n【DM反馈】\n对方抗命', 'feedbacked', '2', 'day', '2026-05-23 21:28:24', '2026-05-23 21:56:00');
INSERT INTO `faction_action` VALUES ('37', '10', '二阶堂希罗', '统治者', 'assign_guard', '{\"actorId\":31,\"targetLocationId\":19}', '✓ 已提交【安排看守】\n\n看守人员：闲屿\n看守地点：监狱\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。\n\n【DM反馈】\n对方接受', 'feedbacked', '2', 'day', '2026-05-23 21:32:46', '2026-05-23 22:25:41');
INSERT INTO `faction_action` VALUES ('38', '14', 'Κάκτος西里尔', '冒险者', 'extra_investigate', '{\"investigateType\":\"investigate_player\",\"targetId\":30}', '✓ 已提交【额外调查】\n\n调查类型：调查玩家\n调查目标：MISD330\n\n结算后该次调查数量将翻倍。等待主持人确认。', 'pending', '2', 'day', '2026-05-23 23:40:47', '2026-05-23 23:40:47');
INSERT INTO `faction_action` VALUES ('39', '21', '乐语', '冒险者', 'ark_construction', '{\"mode\":\"resource\",\"note\":\"发动阵营特性号召和个人特性渴望出海利用免费行动点进行双倍于物资投入的方舟建造\",\"woodKg\":30000,\"metalKg\":10500,\"sealantKg\":20,\"warehouseWoodKg\":0,\"warehouseMetalKg\":0,\"warehouseSealantKg\":0,\"engineCount\":0,\"generatorCount\":0,\"propellerCount\":0,\"buildSail\":false}', '✓ 已提交【方舟建设】\n\n提交者：乐语\n投入模式：资源投入\n  木材：30000kg（30.00吨）\n  金属制品：10500kg（10.50吨）\n  密封材料：20kg\n当前方舟进度：66.00%\n备注：发动阵营特性号召和个人特性渴望出海利用免费行动点进行双倍于物资投入的方舟建造\n\n等待主持人确认。', 'pending', '2', 'day', '2026-05-23 23:44:53', '2026-05-23 23:44:53');
INSERT INTO `faction_action` VALUES ('40', '20', '追枫', '天灾使者', 'sabotage', '{\"targetLocationId\":2,\"facilityId\":4}', '✓ 已提交【破坏】\n\n目标设施：发电机（镇长厅）\n\n等待主持人确认。\n\n【DM反馈】\n收到', 'feedbacked', '3', 'day', '2026-05-24 18:04:44', '2026-05-24 19:30:25');
INSERT INTO `faction_action` VALUES ('41', '21', '乐语', '冒险者', 'guard_ark', '{\"guardId\":21,\"useWeaponOrSkill\":true}', '✓ 已提交【看守方舟】\n\n看守人员：乐语\n使用武器/技能计入防御：是\n额外防御：+2\n\n等待主持人确认。\n\n【DM反馈】\n收到', 'feedbacked', '3', 'day', '2026-05-24 18:57:33', '2026-05-24 21:26:42');
INSERT INTO `faction_action` VALUES ('42', '10', '二阶堂希罗', '统治者', 'assign_guard', '{\"actorId\":20,\"targetLocationId\":18}', '✓ 已提交【安排看守】\n\n看守人员：追枫\n看守地点：矿场\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。\n\n【DM反馈】\n不来', 'feedbacked', '3', 'day', '2026-05-24 19:03:00', '2026-05-24 21:29:18');
INSERT INTO `faction_action` VALUES ('47', '26', 'V', '统治者', 'assign_personnel', '{\"targetId\":11,\"targetKind\":\"npc\",\"assignedActions\":[{\"action\":\"transport\",\"targetLocationId\":null},{\"action\":\"investigate_player\",\"targetLocationId\":null,\"targetPlayerId\":31}],\"note\":\"搬运500kg木材至避难所仓库\\n\\n\"}', '✓ 已提交【安排人员】\n\n目标：乔克·汤姆\n须提交的自由行动（共2项）：\n  1. 搬运\n  2. 调查玩家 → 闲屿\n附加说明：搬运500kg木材至避难所仓库\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。\n\n【DM反馈】\n闲屿去了码头进行调查，去了教堂进行给兔兔哀悼', 'feedbacked', '3', 'day', '2026-05-24 19:35:49', '2026-05-24 21:28:43');
INSERT INTO `faction_action` VALUES ('50', '30', 'MISD330', '反叛者', 'extra_action', '{\"actionType\":\"use_skill\",\"targetLocationId\":null,\"targetPlayerId\":null,\"note\":\"急救并医疗自己\"}', '✓ 已提交【额外行动】\n\n行动类型：使用职业技能\n备注：急救并医疗自己\n\n此环节无法寻找NPC进行对话。\n等待主持人确认。\n\n【DM反馈】\n了解', 'feedbacked', '3', 'day', '2026-05-24 19:36:59', '2026-05-24 21:29:24');
INSERT INTO `faction_action` VALUES ('51', '26', 'V', '统治者', 'assign_personnel', '{\"targetId\":4,\"targetKind\":\"npc\",\"assignedActions\":[{\"action\":\"transport\",\"targetLocationId\":null},{\"action\":\"go_location\",\"targetLocationId\":18}],\"note\":\"搬运500kg木材至避难所仓库\\n\\n\"}', '✓ 已提交【安排人员】\n\n目标：托马斯·伍德\n须提交的自由行动（共2项）：\n  1. 搬运\n  2. 前往地点 → 矿场\n附加说明：搬运500kg木材至避难所仓库\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。\n\n【DM反馈】\n完成', 'feedbacked', '3', 'day', '2026-05-24 19:41:04', '2026-05-24 21:29:57');
INSERT INTO `faction_action` VALUES ('52', '26', 'V', '统治者', 'assign_personnel', '{\"targetId\":10,\"targetKind\":\"npc\",\"assignedActions\":[{\"action\":\"transport\",\"targetLocationId\":null},{\"action\":\"other\",\"targetLocationId\":18}],\"note\":\"搬运500kg木材至避难所仓库，希望NPC夜间前往避难所帮助统治者\\n\\n\"}', '✓ 已提交【安排人员】\n\n目标：汉斯·施密特\n须提交的自由行动（共2项）：\n  1. 搬运\n  2. 其他\n附加说明：搬运500kg木材至避难所仓库，希望NPC夜间前往避难所帮助统治者\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。\n\n【DM反馈】\n完成', 'feedbacked', '3', 'day', '2026-05-24 19:45:20', '2026-05-24 21:36:41');
INSERT INTO `faction_action` VALUES ('53', '26', 'V', '统治者', 'assign_personnel', '{\"targetId\":2,\"targetKind\":\"npc\",\"assignedActions\":[{\"action\":\"transport\",\"targetLocationId\":null},{\"action\":\"other\",\"targetLocationId\":18}],\"note\":\"搬运500kg木材至避难所仓库，希望NPC夜间前往避难所帮助统治者\\n\\n\"}', '✓ 已提交【安排人员】\n\n目标：杰克·塔克\n须提交的自由行动（共2项）：\n  1. 搬运\n  2. 其他\n附加说明：搬运500kg木材至避难所仓库，希望NPC夜间前往避难所帮助统治者\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。\n\n【DM反馈】\nnpc做别的事情了', 'feedbacked', '3', 'day', '2026-05-24 19:46:17', '2026-05-24 21:39:30');
INSERT INTO `faction_action` VALUES ('54', '26', 'V', '统治者', 'assign_personnel', '{\"targetId\":1,\"targetKind\":\"npc\",\"assignedActions\":[{\"action\":\"transport\",\"targetLocationId\":null},{\"action\":\"other\",\"targetLocationId\":18}],\"note\":\"搬运500kg石材至避难所仓库，希望NPC夜间前往避难所帮助统治者\\n\\n\"}', '✓ 已提交【安排人员】\n\n目标：克拉拉·南丁格尔\n须提交的自由行动（共2项）：\n  1. 搬运\n  2. 其他\n附加说明：搬运500kg石材至避难所仓库，希望NPC夜间前往避难所帮助统治者\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。\n\n【DM反馈】\nnpc做别的事情了', 'feedbacked', '3', 'day', '2026-05-24 19:46:52', '2026-05-24 21:38:50');
INSERT INTO `faction_action` VALUES ('55', '26', 'V', '统治者', 'assign_personnel', '{\"targetId\":12,\"targetKind\":\"player\",\"assignedActions\":[{\"action\":\"transport\",\"targetLocationId\":null}],\"note\":\"搬运500kg石材至避难所仓库\\n\\n\"}', '✓ 已提交【安排人员】\n\n目标：千代\n须提交的自由行动（共1项）：\n  1. 搬运\n附加说明：搬运500kg石材至避难所仓库\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。\n\n【DM反馈】\n拒绝', 'feedbacked', '3', 'day', '2026-05-24 19:47:24', '2026-05-24 21:38:29');
INSERT INTO `faction_action` VALUES ('56', '26', 'V', '统治者', 'assign_personnel', '{\"targetId\":16,\"targetKind\":\"player\",\"assignedActions\":[{\"action\":\"transport\",\"targetLocationId\":null}],\"note\":\"搬运500kg石材至避难所仓库\\n\\n\"}', '✓ 已提交【安排人员】\n\n目标：孤城暮角\n须提交的自由行动（共1项）：\n  1. 搬运\n附加说明：搬运500kg石材至避难所仓库\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。\n\n【DM反馈】\n拒绝', 'feedbacked', '3', 'day', '2026-05-24 19:48:17', '2026-05-24 21:39:49');
INSERT INTO `faction_action` VALUES ('57', '26', 'V', '统治者', 'assign_personnel', '{\"targetId\":31,\"targetKind\":\"player\",\"assignedActions\":[{\"action\":\"transport\",\"targetLocationId\":null}],\"note\":\"搬运500kg石材至避难所仓库\\n\\n\"}', '✓ 已提交【安排人员】\n\n目标：闲屿\n须提交的自由行动（共1项）：\n  1. 搬运\n附加说明：搬运500kg石材至避难所仓库\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。\n\n【DM反馈】\n拒绝了', 'feedbacked', '3', 'day', '2026-05-24 19:49:29', '2026-05-24 21:40:00');
INSERT INTO `faction_action` VALUES ('58', '26', 'V', '统治者', 'assign_personnel', '{\"targetId\":14,\"targetKind\":\"player\",\"assignedActions\":[{\"action\":\"transport\",\"targetLocationId\":null}],\"note\":\"搬运500kg石材至避难所仓库\\n\\n\"}', '✓ 已提交【安排人员】\n\n目标：Κάκτος西里尔\n须提交的自由行动（共1项）：\n  1. 搬运\n附加说明：搬运500kg石材至避难所仓库\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。\n\n【DM反馈】\n拒绝', 'feedbacked', '3', 'day', '2026-05-24 19:50:39', '2026-05-24 21:38:37');
INSERT INTO `faction_action` VALUES ('59', '26', 'V', '统治者', 'assign_personnel', '{\"targetId\":21,\"targetKind\":\"player\",\"assignedActions\":[{\"action\":\"transport\",\"targetLocationId\":null}],\"note\":\"搬运500kg石材至避难所仓库\\n\\n\"}', '✓ 已提交【安排人员】\n\n目标：乐语\n须提交的自由行动（共1项）：\n  1. 搬运\n附加说明：搬运500kg石材至避难所仓库\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。\n\n【DM反馈】\n拒绝了', 'feedbacked', '3', 'day', '2026-05-24 19:50:57', '2026-05-24 21:40:07');
INSERT INTO `faction_action` VALUES ('60', '16', '孤城暮角', '天灾使者', 'sabotage', '{\"targetLocationId\":1,\"facilityId\":2}', '✓ 已提交【破坏】\n\n目标设施：发电机（警察局）\n\n等待主持人确认。\n\n【DM反馈】\n收到', 'feedbacked', '3', 'day', '2026-05-24 20:28:28', '2026-05-24 21:40:15');
INSERT INTO `faction_action` VALUES ('61', '10', '二阶堂希罗', '统治者', 'assign_guard', '{\"actorId\":16,\"targetLocationId\":18}', '✓ 已提交【安排看守】\n\n看守人员：孤城暮角\n看守地点：矿场\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。\n\n【DM反馈】\n接受', 'feedbacked', '3', 'day', '2026-05-24 20:33:43', '2026-05-24 21:41:23');
INSERT INTO `faction_action` VALUES ('62', '10', '二阶堂希罗', '统治者', 'assign_guard', '{\"actorId\":29,\"targetLocationId\":18}', '✓ 已提交【安排看守】\n\n看守人员：飞凡\n看守地点：矿场\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。\n\n【DM反馈】\n接受', 'feedbacked', '3', 'day', '2026-05-24 20:33:59', '2026-05-24 21:41:36');
INSERT INTO `faction_action` VALUES ('63', '10', '二阶堂希罗', '统治者', 'assign_guard', '{\"actorId\":18,\"targetLocationId\":18}', '✓ 已提交【安排看守】\n\n看守人员：Missbear\n看守地点：矿场\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。\n\n【DM反馈】\n对方未反馈', 'feedbacked', '3', 'day', '2026-05-24 20:34:08', '2026-05-24 21:46:31');
INSERT INTO `faction_action` VALUES ('64', '10', '二阶堂希罗', '统治者', 'assign_guard', '{\"actorId\":12,\"targetLocationId\":18}', '✓ 已提交【安排看守】\n\n看守人员：千代\n看守地点：矿场\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。\n\n【DM反馈】\n对方未反馈', 'feedbacked', '3', 'day', '2026-05-24 20:34:21', '2026-05-24 21:46:40');
INSERT INTO `faction_action` VALUES ('65', '10', '二阶堂希罗', '统治者', 'assign_guard', '{\"actorId\":32,\"targetLocationId\":18}', '✓ 已提交【安排看守】\n\n看守人员：澡堂子\n看守地点：矿场\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。\n\n【DM反馈】\n对方同意了', 'feedbacked', '3', 'day', '2026-05-24 20:34:40', '2026-05-24 21:46:00');
INSERT INTO `faction_action` VALUES ('66', '24', '花海', '反叛者', 'extra_action', '{\"actionType\":\"use_trait\",\"targetLocationId\":null,\"targetPlayerId\":null,\"note\":\"百宝袋/我要将斧头、防弹衣、医疗包、金属用品、食物全部复制\"}', '✓ 已提交【额外行动】\n\n行动类型：使用特性\n备注：百宝袋/我要将斧头、防弹衣、医疗包、金属用品、食物全部复制\n\n此环节无法寻找NPC进行对话。\n等待主持人确认。\n\n【DM反馈】\n百宝袋是一次性特性', 'feedbacked', '3', 'day', '2026-05-24 20:51:19', '2026-05-24 20:58:19');

-- ----------------------------
-- Table structure for game_activity_log
-- ----------------------------
DROP TABLE IF EXISTS `game_activity_log`;
CREATE TABLE `game_activity_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category` varchar(24) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `detail` text,
  `game_day` int(11) NOT NULL,
  `player_faction` varchar(20) DEFAULT NULL,
  `player_id` int(11) DEFAULT NULL,
  `player_name` varchar(50) DEFAULT NULL,
  `summary` varchar(400) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_activity_created` (`created_at`),
  KEY `idx_activity_day` (`game_day`)
) ENGINE=InnoDB AUTO_INCREMENT=548 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of game_activity_log
-- ----------------------------
INSERT INTO `game_activity_log` VALUES ('6', 'consume', '2026-05-22 18:22:29.382000', '累计进食 2/2；取暖 15/15 热值', '1', '冒险者', '9', '对酒', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('7', 'consume', '2026-05-22 18:29:06.599000', '累计进食 2/2；取暖 15/15 热值', '1', '平民', '8', '兔兔', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('8', 'consume', '2026-05-22 18:44:50.893000', '累计进食 2/2；取暖 15/15 热值', '1', '统治者', '15', '空白', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('9', 'consume', '2026-05-22 18:46:05.045000', '累计进食 2/2；取暖 15/15 热值', '1', '冒险者', '14', 'Κάκτος西里尔', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('10', 'consume', '2026-05-22 18:46:34.890000', '累计进食 2/2；取暖 15/15 热值', '1', '平民', '27', '得狗的老意', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('11', 'consume', '2026-05-22 18:46:55.273000', '累计进食 2/2；取暖 15/15 热值', '1', '反叛者', '23', '教皇', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('12', 'action', '2026-05-22 18:50:30.817000', '目标:酒吧', '1', '平民', '27', '得狗的老意', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('13', 'action', '2026-05-22 18:50:31.915000', '目标:旅店', '1', '平民', '27', '得狗的老意', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('14', 'action', '2026-05-22 19:01:41.561000', '目标:兔兔', '1', '统治者', '17', 'zzz', '自由#1·调查玩家');
INSERT INTO `game_activity_log` VALUES ('15', 'action', '2026-05-22 19:01:43.707000', '目标:集市', '1', '统治者', '17', 'zzz', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('16', 'consume', '2026-05-22 19:04:02.562000', '累计进食 2/2；取暖 0/15 热值', '1', '冒险者', '19', 'unPy-GPT', '进食+2 木0kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('17', 'action', '2026-05-22 19:08:45.224000', '先接受来自统治者的物资交易。然后使用【烘培】技能获得两个面包。消耗资源14单位的食物，30kg的木材（被动特性食物消耗为1.5来计算，荷叶认证过的）。', '1', '平民', '25', 'tony', '自由#1·使用职业技能');
INSERT INTO `game_activity_log` VALUES ('18', 'action', '2026-05-22 19:12:34.890000', '再次进行一样的行动，消耗14单位食物，30kg木材，获得两个面包。', '1', '平民', '25', 'tony', '自由#2·使用职业技能');
INSERT INTO `game_activity_log` VALUES ('19', 'quick', '2026-05-22 19:15:23.302000', '个人行动 3: 使用一个面包/便当 获得额外的行动点。再次进行烹饪，耗费14食物30kg木材获得两个面包/便当。', '1', '平民', '25', 'tony', '补充行动');
INSERT INTO `game_activity_log` VALUES ('20', 'action', '2026-05-22 19:15:35.672000', '模式：仓库→个人；源：码头集购仓；鱼叉/矛×1；食物×10', '1', '统治者', '10', '二阶堂希罗', '自由#1·搬运');
INSERT INTO `game_activity_log` VALUES ('21', 'action', '2026-05-22 19:15:36.007000', '模式：仓库→个人；源：燃料仓库；燃料×3', '1', '统治者', '10', '二阶堂希罗', '自由#2·搬运');
INSERT INTO `game_activity_log` VALUES ('22', 'consume', '2026-05-22 19:15:53.151000', '累计进食 2/2；取暖 15/15 热值', '1', '天灾使者', '16', '孤城暮角', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('23', 'action', '2026-05-22 19:16:01.706000', '目标:伐木营地 NPC:托马斯·伍德 托马斯兄弟，加入我们探索新的世界，看看你手里的斧刃——它不该用来给统治者的避难所添柴，而该劈开冻结的海面，劈出一条属于我们的生路！外面有漫无边际的原始森林，有热腾腾的炉火和自由的风，我一起造船冲出去，把你的名字刻在新世界的第一根栋梁上！站起来，兄弟，别给这座坟墓陪葬，咱们把命运劈成两半——一半留给这该死的雪，另一半烧成黎明。（拉拢托马斯·伍德，为我们生产木材，如果他愿意的话可以上船）', '1', '冒险者', '9', '对酒', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('24', 'action', '2026-05-22 19:16:01.868000', '目标:矿场 NPC:卡尔·铁锤 卡尔兄弟，别把热血冻死在这座死岛上，灾难会吞噬这里的一切，加入我们驶向新的家园，我们会给你最好的待遇。（拉拢矿工生产，他愿意的话可以上船）', '1', '冒险者', '9', '对酒', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('25', 'night', '2026-05-22 19:16:42.441000', '✓ 已提交【夜晚个人行动】\n\n提交者：二阶堂希罗\n行动：调查玩家\n目标：兔兔\n\n等待主持人在夜晚阶段结算。', '1', '统治者', '10', '二阶堂希罗', '夜晚个人行动');
INSERT INTO `game_activity_log` VALUES ('26', 'faction', '2026-05-22 19:17:22.185000', '✓ 已提交【安排看守】\n\n看守人员：日落忽悠的花海\n看守地点：镇长厅\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。', '1', '统治者', '10', '二阶堂希罗', '安排看守');
INSERT INTO `game_activity_log` VALUES ('27', 'quick', '2026-05-22 19:17:53.752000', '行动 4: （快速行动）三个【个人行动】都成功完成后，使用交易，给予V镇长一个面包，和二阶典狱长一个面包。今天一共消耗42食物，90木材，部分消耗的物资来自统治者的交易。共获得6个面包，其中把两个给出去。', '1', '平民', '25', 'tony', '快速行动');
INSERT INTO `game_activity_log` VALUES ('28', 'trade', '2026-05-22 19:24:05.500000', '对方:孤城暮角 | give 金属制品×5 | give 木材×20 | take 猎枪×1', '1', '平民', '29', '飞凡', '发起交易→孤城暮角 #14');
INSERT INTO `game_activity_log` VALUES ('29', 'trade', '2026-05-22 19:24:07.210000', '对方:日落忽悠的花海 | give 食物×5 | take 食物×1', '1', '平民', '27', '得狗的老意', '发起交易→日落忽悠的花海 #15');
INSERT INTO `game_activity_log` VALUES ('30', 'trade', '2026-05-22 19:24:36.412000', '对方:日落忽悠的花海 | give 食物×5 | take 食物×1', '1', '平民', '27', '得狗的老意', '发起交易→日落忽悠的花海 #16');
INSERT INTO `game_activity_log` VALUES ('31', 'consume', '2026-05-22 19:26:40.183000', '累计进食 2/2；取暖 15/15 热值', '1', '冒险者', '19', 'unPy-GPT', '进食+0 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('32', 'trade', '2026-05-22 19:33:43.096000', '对方:飞凡', '1', '天灾使者', '16', '孤城暮角', '接受交易→飞凡 #14');
INSERT INTO `game_activity_log` VALUES ('33', 'action', '2026-05-22 19:38:43.157000', '目标:邮局 调查地点，看看有没有物资或者发电机', '1', '冒险者', '14', 'Κάκτος西里尔', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('34', 'action', '2026-05-22 19:38:43.623000', '目标:教堂 调查地点，看看有没有物资或者发电机', '1', '冒险者', '14', 'Κάκτος西里尔', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('35', 'consume', '2026-05-22 19:40:13.538000', '累计进食 2/2；取暖 15/15 热值', '1', '天灾使者', '20', '追枫', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('36', 'faction', '2026-05-22 19:40:46.451000', '✓ 已提交【看守方舟】\n\n看守人员：Κάκτος西里尔\n使用武器/技能计入防御：是\n额外防御：+2\n\n等待主持人确认。', '1', '冒险者', '14', 'Κάκτος西里尔', '看守方舟');
INSERT INTO `game_activity_log` VALUES ('37', 'action', '2026-05-22 19:44:52.791000', '目标:追枫', '1', '冒险者', '19', 'unPy-GPT', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('38', 'faction', '2026-05-22 19:47:01.986000', '✓ 已提交【额外调查】\n\n调查类型：调查地点\n调查目标：灯塔\n\n结算后该次调查数量将翻倍。等待主持人确认。', '1', '冒险者', '9', '对酒', '额外调查');
INSERT INTO `game_activity_log` VALUES ('39', 'trade', '2026-05-22 19:52:12.174000', '对方:得狗的老意', '1', '反叛者', '24', '花海', '接受交易→得狗的老意 #16');
INSERT INTO `game_activity_log` VALUES ('40', 'trade', '2026-05-22 19:52:15.129000', '对方:得狗的老意', '1', '反叛者', '24', '花海', '接受交易→得狗的老意 #15');
INSERT INTO `game_activity_log` VALUES ('41', 'consume', '2026-05-22 19:54:31.300000', '累计进食 0/2；取暖 15/15 热值', '1', '天灾使者', '22', '11', '进食+0 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('42', 'consume', '2026-05-22 19:54:41.797000', '累计进食 2/2；取暖 15/15 热值', '1', '天灾使者', '22', '11', '进食+2 木0kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('43', 'trade', '2026-05-22 19:56:33.351000', '对方:追枫 | give 食物×2', '1', '反叛者', '24', '花海', '发起交易→追枫 #17');
INSERT INTO `game_activity_log` VALUES ('44', 'action', '2026-05-22 19:57:23.843000', '模式：仓库→个人；源：码头集购仓；草药×1；便当×1；食物×100', '1', '统治者', '26', 'V', '自由#1·搬运');
INSERT INTO `game_activity_log` VALUES ('45', 'action', '2026-05-22 19:57:24.326000', '目标:集市 NPC:汉斯·施密特', '1', '统治者', '26', 'V', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('46', 'trade', '2026-05-22 19:57:48.098000', '对方:花海', '1', '天灾使者', '20', '追枫', '接受交易→花海 #17');
INSERT INTO `game_activity_log` VALUES ('47', 'consume', '2026-05-22 19:59:13.072000', '累计进食 2/2；取暖 15/15 热值', '1', '反叛者', '13', '凭栏择雨', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('48', 'trade', '2026-05-22 19:59:59.116000', '对方:孤城暮角 | give 金属制品×10', '1', '天灾使者', '20', '追枫', '发起交易→孤城暮角 #18');
INSERT INTO `game_activity_log` VALUES ('49', 'consume', '2026-05-22 20:00:12.490000', '累计进食 2/2；取暖 15/15 热值', '1', '平民', '12', '千代', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('50', 'action', '2026-05-22 20:00:51.209000', null, '1', '反叛者', '24', '花海', '自由#1·生产');
INSERT INTO `game_activity_log` VALUES ('51', 'action', '2026-05-22 20:00:51.338000', '百宝袋，对燃料使用百宝袋', '1', '反叛者', '24', '花海', '自由#2·使用特性');
INSERT INTO `game_activity_log` VALUES ('52', 'trade', '2026-05-22 20:01:00.155000', '对方:孤城暮角 | give 木材×30', '1', '天灾使者', '20', '追枫', '发起交易→孤城暮角 #19');
INSERT INTO `game_activity_log` VALUES ('53', 'trade', '2026-05-22 20:01:31.632000', '对方:追枫', '1', '天灾使者', '16', '孤城暮角', '接受交易→追枫 #19');
INSERT INTO `game_activity_log` VALUES ('54', 'trade', '2026-05-22 20:01:33.925000', '对方:追枫', '1', '天灾使者', '16', '孤城暮角', '接受交易→追枫 #18');
INSERT INTO `game_activity_log` VALUES ('55', 'trade', '2026-05-22 20:03:49.120000', '对方:得狗的老意 | give 木材×1 | take 猎枪×1 | take 猎枪弹×2', '1', '反叛者', '13', '凭栏择雨', '发起交易→得狗的老意 #20');
INSERT INTO `game_activity_log` VALUES ('56', 'consume', '2026-05-22 20:04:20.913000', '累计进食 2/2；取暖 15/15 热值', '1', '反叛者', '24', '花海', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('57', 'action', '2026-05-22 20:04:25.988000', '目标:千代 调查阵营信息', '1', '天灾使者', '22', '11', '自由#1·调查玩家');
INSERT INTO `game_activity_log` VALUES ('58', 'action', '2026-05-22 20:04:26.133000', '潜水，1D6', '1', '天灾使者', '22', '11', '自由#2·使用职业技能');
INSERT INTO `game_activity_log` VALUES ('59', 'trade', '2026-05-22 20:09:41.076000', '对方:凭栏择雨', '1', '平民', '27', '得狗的老意', '接受交易→凭栏择雨 #20');
INSERT INTO `game_activity_log` VALUES ('60', 'faction', '2026-05-22 20:09:52.380000', '✓ 已提交【额外劳动】\n\n提交者：花海\n效果：今日生产类自由行动产出 +50%。\n（须今日已提交生产行动）\n等待主持人确认。', '1', '反叛者', '24', '花海', '额外劳动');
INSERT INTO `game_activity_log` VALUES ('61', 'action', '2026-05-22 20:11:36.987000', '从码头使用渔船出海捕鱼', '1', '冒险者', '19', 'unPy-GPT', '自由#1·使用职业技能');
INSERT INTO `game_activity_log` VALUES ('62', 'action', '2026-05-22 20:11:37.121000', '目标:追枫', '1', '冒险者', '19', 'unPy-GPT', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('63', 'action', '2026-05-22 20:13:42.601000', '目标:集市 NPC:汉斯·施密特 和大家进行协商交流', '1', '反叛者', '13', '凭栏择雨', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('64', 'faction', '2026-05-22 20:14:32.126000', '✓ 已提交【安排看守】\n\n看守人员：千代\n看守地点：矿场\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。', '1', '统治者', '10', '二阶堂希罗', '安排看守');
INSERT INTO `game_activity_log` VALUES ('65', 'quick', '2026-05-22 20:16:33.795000', '对西里尔散播瘟疫', '1', '天灾使者', '16', '孤城暮角', '快速行动');
INSERT INTO `game_activity_log` VALUES ('66', 'action', '2026-05-22 20:17:23.101000', '使用启蒙“捕鱼”，教学我、gpt（采珠人）、择雨（旅店老板）', '1', '平民', '12', '千代', '自由#1·使用职业技能');
INSERT INTO `game_activity_log` VALUES ('67', 'action', '2026-05-22 20:17:23.208000', '目标:闲屿', '1', '平民', '12', '千代', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('68', 'night', '2026-05-22 20:17:33.571000', '✓ 已提交【其他】\n\n提交者：千代\n备注：回到旅店休息\n\n等待主持人在夜晚阶段结算。', '1', '平民', '12', '千代', '其他');
INSERT INTO `game_activity_log` VALUES ('69', 'trade', '2026-05-22 20:22:27.371000', '对方:兔兔 | give 食物×2', '1', '反叛者', '24', '花海', '发起交易→兔兔 #21');
INSERT INTO `game_activity_log` VALUES ('70', 'action', '2026-05-22 20:25:46.693000', null, '1', '反叛者', '13', '凭栏择雨', '自由#1·隐藏');
INSERT INTO `game_activity_log` VALUES ('71', 'action', '2026-05-22 20:25:46.824000', '目标:集市 NPC:汉斯·施密特 和大家汇合，商讨事项', '1', '反叛者', '13', '凭栏择雨', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('72', 'action', '2026-05-22 20:27:57.382000', '狩猎，打五块肉，打完以后把武器献祭了', '1', '天灾使者', '20', '追枫', '自由#1·使用职业技能');
INSERT INTO `game_activity_log` VALUES ('73', 'action', '2026-05-22 20:27:59.028000', '目标:猎人小屋', '1', '天灾使者', '20', '追枫', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('74', 'faction', '2026-05-22 20:31:07.878000', '✓ 已提交【额外调查】\n\n调查类型：调查玩家\n调查目标：兔兔\n\n结算后该次调查数量将翻倍。等待主持人确认。', '1', '冒险者', '19', 'unPy-GPT', '额外调查');
INSERT INTO `game_activity_log` VALUES ('75', 'action', '2026-05-22 20:32:46.151000', '使用启蒙“捕鱼”，教学我、gpt（采珠人）、择雨（旅店老板）', '1', '平民', '12', '千代', '自由#1·使用职业技能');
INSERT INTO `game_activity_log` VALUES ('76', 'action', '2026-05-22 20:32:46.252000', '目标:MISD330 调查医生', '1', '平民', '12', '千代', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('77', 'faction', '2026-05-22 20:36:04.821000', '✓ 已提交【诅咒】\n\n消耗武器：猎枪\n目标1：兔兔\n目标2：得狗的老意\n\n效果：获知阵营、施加「诅咒」标记。\n等待主持人确认。', '1', '天灾使者', '20', '追枫', '诅咒');
INSERT INTO `game_activity_log` VALUES ('78', 'action', '2026-05-22 20:39:28.488000', '将码头仓库物资中运500Kg转入矿产仓库，并保留1个鱼叉及30Kg的食物到个人', '1', '统治者', '10', '二阶堂希罗', '自由#1·其他');
INSERT INTO `game_activity_log` VALUES ('79', 'action', '2026-05-22 20:39:28.593000', '将燃料仓库中500Kg搬运至矿厂仓库并保留20Kg到个人', '1', '统治者', '10', '二阶堂希罗', '自由#2·其他');
INSERT INTO `game_activity_log` VALUES ('80', 'night', '2026-05-22 20:40:00.936000', '✓ 已提交【其他】\n\n提交者：凭栏择雨\n备注：使用职业技能\n\n等待主持人在夜晚阶段结算。', '1', '反叛者', '13', '凭栏择雨', '其他');
INSERT INTO `game_activity_log` VALUES ('81', 'action', '2026-05-22 20:40:14.088000', '手工艺\n猎弓×1，仿制枪×1，复合盾×1（在枪把上刻上“二房”的名字）', '1', '天灾使者', '16', '孤城暮角', '自由#1·使用职业技能');
INSERT INTO `game_activity_log` VALUES ('82', 'action', '2026-05-22 20:40:14.182000', '目标:旅店', '1', '天灾使者', '16', '孤城暮角', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('83', 'faction', '2026-05-22 20:40:48.047000', '✓ 已提交【额外调查】\n\n调查类型：调查地点\n调查目标：矿场\n\n结算后该次调查数量将翻倍。等待主持人确认。', '1', '天灾使者', '16', '孤城暮角', '额外调查');
INSERT INTO `game_activity_log` VALUES ('84', 'trade', '2026-05-22 20:42:45.584000', '对方:孤城暮角 | give 木材×20 | give 未知物品×10', '1', '天灾使者', '20', '追枫', '发起交易→孤城暮角 #22');
INSERT INTO `game_activity_log` VALUES ('85', 'quick', '2026-05-22 20:44:54.154000', '我是个酒蒙子，我要喝两瓶我的酒', '1', '平民', '27', '得狗的老意', '快速行动');
INSERT INTO `game_activity_log` VALUES ('86', 'trade', '2026-05-22 20:45:41.046000', '对方:追枫', '1', '天灾使者', '16', '孤城暮角', '接受交易→追枫 #22');
INSERT INTO `game_activity_log` VALUES ('87', 'action', '2026-05-22 20:50:19.458000', '目标:兔兔', '1', '平民', '30', 'MISD330', '自由#1·调查玩家');
INSERT INTO `game_activity_log` VALUES ('88', 'action', '2026-05-22 20:50:19.968000', '目标:蟋蟀蜥蜴', '1', '平民', '30', 'MISD330', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('89', 'action', '2026-05-22 20:52:56.168000', '目标:凭栏择雨', '1', '平民', '8', '兔兔', '自由#1·调查玩家');
INSERT INTO `game_activity_log` VALUES ('90', 'action', '2026-05-22 20:52:56.502000', '目标:千代', '1', '平民', '8', '兔兔', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('91', 'trade', '2026-05-22 20:53:05.730000', '对方:花海', '1', '平民', '8', '兔兔', '接受交易→花海 #21');
INSERT INTO `game_activity_log` VALUES ('92', 'trade', '2026-05-22 20:53:45.959000', '对方:MISD330 | take 木材×3 | take 金属制品×7', '1', '天灾使者', '16', '孤城暮角', '发起交易→MISD330 #23');
INSERT INTO `game_activity_log` VALUES ('93', 'trade', '2026-05-22 20:53:49.384000', '对方:MISD330 | take 木材×3 | take 金属制品×7', '1', '天灾使者', '16', '孤城暮角', '发起交易→MISD330 #24');
INSERT INTO `game_activity_log` VALUES ('94', 'consume', '2026-05-22 21:05:07.010000', '累计进食 2/2；取暖 15/15 热值', '1', '平民', '21', '乐语', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('95', 'quick', '2026-05-22 21:07:22.603000', '我想探查一下出生的邮局附近有什么物资和物品', '1', '平民', '21', '乐语', '快速行动');
INSERT INTO `game_activity_log` VALUES ('96', 'consume', '2026-05-22 21:09:20.323000', '累计进食 2/2；取暖 15/15 热值', '1', '平民', '31', '闲屿', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('97', 'consume', '2026-05-22 21:23:11.772000', '累计进食 2/2；取暖 15/15 热值', '1', '反叛者', '11', '蟋蟀蜥蜴', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('98', 'trade', '2026-05-22 21:23:53.020000', '对方:MISD330 | give 食物×2', '1', '反叛者', '24', '花海', '发起交易→MISD330 #25');
INSERT INTO `game_activity_log` VALUES ('99', 'night', '2026-05-22 21:24:25.739000', '✓ 已提交【其他】\n\n提交者：Κάκτος西里尔\n备注：配合统治者安排，在得到统治者发放的手枪和弹药的同时，守卫镇武库\n\n等待主持人在夜晚阶段结算。', '1', '冒险者', '14', 'Κάκτος西里尔', '其他');
INSERT INTO `game_activity_log` VALUES ('100', 'trade', '2026-05-22 21:28:38.015000', '对方:花海 | take 食物×2', '1', '平民', '32', '澡堂子', '发起交易→花海 #26');
INSERT INTO `game_activity_log` VALUES ('101', 'action', '2026-05-22 21:29:38.202000', '目标:码头 NPC:克拉拉·南丁格尔 询问是否有在码头遇到过值得注意的人或事，同时希望以为他提供劳动，让他能给自己一些食物', '1', '平民', '31', '闲屿', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('102', 'action', '2026-05-22 21:29:38.420000', '目标:兔兔', '1', '平民', '31', '闲屿', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('103', 'action', '2026-05-22 21:30:30.080000', '目标:酒吧 寻找酒精', '1', '平民', '32', '澡堂子', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('104', 'action', '2026-05-22 21:30:30.355000', '目标:墓地 找我需要的物资', '1', '平民', '32', '澡堂子', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('105', 'faction', '2026-05-22 21:31:25.138000', '✓ 已提交【额外行动】\n\n行动类型：使用职业技能\n\n此环节无法寻找NPC进行对话。\n等待主持人确认。', '1', '反叛者', '13', '凭栏择雨', '额外行动');
INSERT INTO `game_activity_log` VALUES ('106', 'action', '2026-05-22 21:31:52.063000', '目标:警察局 表面是是去上班 问一下小镇最近有没有不太平的事情 把锅全都推给统治者', '1', '反叛者', '11', '蟋蟀蜥蜴', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('107', 'action', '2026-05-22 21:31:52.174000', '目标:二阶堂希罗 调查一下他去了哪里 都和谁说了话', '1', '反叛者', '11', '蟋蟀蜥蜴', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('108', 'trade', '2026-05-22 21:32:25.238000', '对方:澡堂子', '1', '反叛者', '24', '花海', '接受交易→澡堂子 #26');
INSERT INTO `game_activity_log` VALUES ('109', 'consume', '2026-05-22 21:32:25.314000', '累计进食 2/2；取暖 15/15 热值', '1', '平民', '32', '澡堂子', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('110', 'action', '2026-05-22 21:34:27.775000', '模式：仓库→仓库；源：燃料仓库；目标：镇武库；手电筒×8；蜡烛×20；木材×265；沥青×30；燃料×200', '1', '统治者', '26', 'V', '自由#1·搬运');
INSERT INTO `game_activity_log` VALUES ('111', 'action', '2026-05-22 21:34:28.448000', '目标:集市 NPC:汉斯·施密特', '1', '统治者', '26', 'V', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('112', 'action', '2026-05-22 21:35:36.517000', '目标:码头 NPC:克拉拉·南丁格尔 询问小镇中载具的信息，询问能否获得一些满足温饱的食物，希望交换一些打捞上来的特殊物品', '1', '平民', '21', '乐语', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('113', 'action', '2026-05-22 21:35:36.660000', '目标:矿场 NPC:维克多·斯通 搜集一些散落的资源。询问矿工需不需要我帮忙带口信，需要的话收取一点的报酬。搜集一些有益的关于小镇的其他情报', '1', '平民', '21', '乐语', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('114', 'faction', '2026-05-22 21:36:49.096000', '✓ 已提交【额外行动】\n\n行动类型：使用职业技能\n备注：寻找有没有打架斗殴的人并劝架\n\n此环节无法寻找NPC进行对话。\n等待主持人确认。', '1', '反叛者', '11', '蟋蟀蜥蜴', '额外行动');
INSERT INTO `game_activity_log` VALUES ('115', 'night', '2026-05-22 21:36:50.985000', '✓ 已提交【其他】\n\n提交者：澡堂子\n备注：作为守墓人夜间很精神也很正常吧 夜晚警惕并且隐秘的观察别的玩家的动向\n\n等待主持人在夜晚阶段结算。', '1', '平民', '32', '澡堂子', '其他');
INSERT INTO `game_activity_log` VALUES ('116', 'trade', '2026-05-22 21:38:39.769000', '对方:花海 | give 燃料×3', '1', '反叛者', '11', '蟋蟀蜥蜴', '发起交易→花海 #27');
INSERT INTO `game_activity_log` VALUES ('117', 'trade', '2026-05-22 21:41:14.208000', '对方:花海 | give 燃料×2', '1', '反叛者', '23', '教皇', '发起交易→花海 #28');
INSERT INTO `game_activity_log` VALUES ('118', 'trade', '2026-05-22 21:41:28.792000', '对方:教皇', '1', '反叛者', '24', '花海', '接受交易→教皇 #28');
INSERT INTO `game_activity_log` VALUES ('119', 'trade', '2026-05-22 21:41:30.364000', '对方:蟋蟀蜥蜴', '1', '反叛者', '24', '花海', '接受交易→蟋蟀蜥蜴 #27');
INSERT INTO `game_activity_log` VALUES ('120', 'faction', '2026-05-22 21:43:31.064000', '✓ 已提交【额外行动】\n\n行动类型：使用职业技能\n备注：布道，对农业协会的农户及成员使用\n\n此环节无法寻找NPC进行对话。\n等待主持人确认。', '1', '反叛者', '23', '教皇', '额外行动');
INSERT INTO `game_activity_log` VALUES ('121', 'quick', '2026-05-22 21:47:51.567000', '我从燃料仓库搬运了物资到镇武库，我希望之后从镇武库拿走50kg的物资可以吗', '1', '统治者', '26', 'V', '补充行动');
INSERT INTO `game_activity_log` VALUES ('122', 'trade', '2026-05-22 21:50:23.199000', '对方:花海', '1', '反叛者', '30', 'MISD330', '接受交易→花海 #25');
INSERT INTO `game_activity_log` VALUES ('123', 'consume', '2026-05-22 21:51:07.330000', '累计进食 2/2；取暖 15/15 热值', '1', '反叛者', '30', 'MISD330', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('124', 'night', '2026-05-22 21:51:38.093000', '✓ 已提交【其他】\n\n提交者：澡堂子\n备注：守墓人夜间精力充沛 带着我的好朋狗在旅馆周围隐秘的巡逻\n\n等待主持人在夜晚阶段结算。', '1', '平民', '32', '澡堂子', '其他');
INSERT INTO `game_activity_log` VALUES ('125', 'quick', '2026-05-22 21:51:40.880000', '转移40kg燃料仓库的木材和30kg港口的食物', '1', '统治者', '17', 'zzz', '快速行动');
INSERT INTO `game_activity_log` VALUES ('126', 'action', '2026-05-22 22:14:40.288000', '模式：仓库→仓库；源：燃料仓库；目标：镇武库；火柴×2；木材×479；沥青×20', '1', '统治者', '15', '空白', '自由#1·搬运');
INSERT INTO `game_activity_log` VALUES ('127', 'action', '2026-05-22 22:14:40.419000', '模式：仓库→仓库；源：码头集购仓；目标：镇武库；医疗包×2；信号枪×1；朗姆酒×20；草药×3；渔网×1；医用酒精×5；破损海图×1；便当×1；鱼叉/矛×2；信号弹×2；食物×300；螺旋桨×3', '1', '统治者', '15', '空白', '自由#2·搬运');
INSERT INTO `game_activity_log` VALUES ('128', 'trade', '2026-05-22 22:16:16.784000', '对方:得狗的老意 | give 未知物品×1 | take 食物×2 | take 燃料×15', '1', '统治者', '26', 'V', '发起交易→得狗的老意 #29');
INSERT INTO `game_activity_log` VALUES ('129', 'quick', '2026-05-22 22:17:55.371000', '快速搬运一把鱼叉与15Kg燃料到背包', '1', '统治者', '10', '二阶堂希罗', '快速行动');
INSERT INTO `game_activity_log` VALUES ('130', 'night', '2026-05-22 22:19:42.971000', '✓ 已提交【夜晚个人行动】\n\n提交者：zzz\n行动：调查玩家\n目标：兔兔\n\n等待主持人在夜晚阶段结算。', '1', '统治者', '17', 'zzz', '夜晚个人行动');
INSERT INTO `game_activity_log` VALUES ('131', 'consume', '2026-05-22 22:22:28.032000', '累计进食 2/2；取暖 15/15 热值', '1', '统治者', '10', '二阶堂希罗', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('132', 'night', '2026-05-22 22:32:04.892000', '✓ 已提交【其他】\n\n提交者：MISD330\n备注：前往矿场，偷取金属\n\n等待主持人在夜晚阶段结算。', '1', '反叛者', '30', 'MISD330', '其他');
INSERT INTO `game_activity_log` VALUES ('133', 'night', '2026-05-22 22:40:22.877000', '✓ 已提交【向统治者施压】\n\n提交者：蟋蟀蜥蜴\n施压诉求：资源由全体镇民管理\n备注：镇民们没有办法获得合理的生存物资 白天工作消耗所有行动点 受限非常大\n\n等待主持人在夜晚阶段结算。', '1', '反叛者', '11', '蟋蟀蜥蜴', '向统治者施压');
INSERT INTO `game_activity_log` VALUES ('134', 'quick', '2026-05-22 22:42:22.921000', '在白天顺便练练射击技术 用来保护人民群众', '1', '反叛者', '11', '蟋蟀蜥蜴', '快速行动');
INSERT INTO `game_activity_log` VALUES ('135', 'faction', '2026-05-22 22:44:42.265000', '✓ 已提交【破坏】\n\n目标设施：烘焙炉（面包店）\n\n等待主持人确认。', '1', '天灾使者', '22', '11', '破坏');
INSERT INTO `game_activity_log` VALUES ('136', 'night', '2026-05-22 22:44:55.060000', '✓ 已提交【其他】\n\n提交者：追枫\n备注：天灾对诅咒目标发动拉人，拉占卜进入天灾队伍\n\n等待主持人在夜晚阶段结算。', '1', '天灾使者', '20', '追枫', '其他');
INSERT INTO `game_activity_log` VALUES ('137', 'trade', '2026-05-22 22:46:04.940000', '对方:Κάκτος西里尔 | give 未知物品×30 | give 未知物品×10 | take 金属制品×7', '1', '天灾使者', '16', '孤城暮角', '发起交易→Κάκτος西里尔 #30');
INSERT INTO `game_activity_log` VALUES ('138', 'night', '2026-05-22 22:46:18.365000', '✓ 已提交【其他】\n\n提交者：11\n备注：天灾对诅咒目标发动拉人，拉占卜进入队伍\n\n等待主持人在夜晚阶段结算。', '1', '天灾使者', '22', '11', '其他');
INSERT INTO `game_activity_log` VALUES ('139', 'trade', '2026-05-22 22:48:18.747000', '对方:孤城暮角', '1', '冒险者', '14', 'Κάκτος西里尔', '接受交易→孤城暮角 #30');
INSERT INTO `game_activity_log` VALUES ('140', 'quick', '2026-05-22 22:48:39.774000', '查看轮船班次表看看有什么发现', '1', '平民', '21', '乐语', '快速行动');
INSERT INTO `game_activity_log` VALUES ('141', 'night', '2026-05-22 22:50:54.605000', '✓ 已提交【进行密谋】\n\n提交者：孤城暮角\n密谋类型：制造恐怖\n参与玩家：追枫、11\n备注：拉占卜进天灾\n\n等待主持人在夜晚阶段结算。', '1', '天灾使者', '16', '孤城暮角', '进行密谋');
INSERT INTO `game_activity_log` VALUES ('142', 'trade', '2026-05-22 22:56:39.719000', '对方:追枫 | give 木材×10', '1', '反叛者', '24', '花海', '发起交易→追枫 #31');
INSERT INTO `game_activity_log` VALUES ('143', 'trade', '2026-05-22 22:58:27.637000', '对方:zzz | give 食物×2 | take 维修工具包×1', '1', '统治者', '10', '二阶堂希罗', '发起交易→zzz #32');
INSERT INTO `game_activity_log` VALUES ('144', 'trade', '2026-05-22 22:58:50.077000', '对方:二阶堂希罗', '1', '统治者', '17', 'zzz', '接受交易→二阶堂希罗 #32');
INSERT INTO `game_activity_log` VALUES ('145', 'consume', '2026-05-22 22:59:04.604000', '累计进食 2/2；取暖 15/15 热值', '1', '统治者', '17', 'zzz', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('146', 'trade', '2026-05-22 23:06:52.821000', '对方:花海', '1', '天灾使者', '20', '追枫', '接受交易→花海 #31');
INSERT INTO `game_activity_log` VALUES ('147', 'trade', '2026-05-22 23:09:29.465000', '对方:孤城暮角 | give 木材×10', '1', '天灾使者', '20', '追枫', '发起交易→孤城暮角 #33');
INSERT INTO `game_activity_log` VALUES ('148', 'trade', '2026-05-22 23:09:52.301000', '对方:追枫', '1', '天灾使者', '16', '孤城暮角', '接受交易→追枫 #33');
INSERT INTO `game_activity_log` VALUES ('149', 'night', '2026-05-22 23:12:53.360000', '✓ 已提交【其他】\n\n提交者：闲屿\n备注：发动特性（？）去靠海的地点转转\n\n等待主持人在夜晚阶段结算。', '1', '平民', '31', '闲屿', '其他');
INSERT INTO `game_activity_log` VALUES ('150', 'quick', '2026-05-22 23:14:39.861000', '尝试与相遇的人产生肢体接触', '1', '平民', '31', '闲屿', '快速行动');
INSERT INTO `game_activity_log` VALUES ('151', 'night', '2026-05-22 23:24:05.596000', '✓ 已提交【夜晚个人行动】\n\n提交者：空白\n行动：使用特性\n备注：使用窃听者特性 查天灾者的聊天记录\n\n等待主持人在夜晚阶段结算。', '1', '统治者', '15', '空白', '夜晚个人行动');
INSERT INTO `game_activity_log` VALUES ('152', 'trade', '2026-05-22 23:26:54.354000', '对方:对酒 | give 食物×2', '1', '反叛者', '24', '花海', '发起交易→对酒 #34');
INSERT INTO `game_activity_log` VALUES ('153', 'consume', '2026-05-22 23:27:28.623000', '累计进食 2/2；取暖 15/15 热值', '1', '平民', '29', '飞凡', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('154', 'night', '2026-05-22 23:29:08.085000', '✓ 已提交【其他】\n\n提交者：飞凡\n备注：去旅店睡觉咯\n\n等待主持人在夜晚阶段结算。', '1', '平民', '29', '飞凡', '其他');
INSERT INTO `game_activity_log` VALUES ('155', 'trade', '2026-05-22 23:34:13.125000', '对方:闲屿 | give 食物×2', '1', '反叛者', '24', '花海', '发起交易→闲屿 #35');
INSERT INTO `game_activity_log` VALUES ('156', 'trade', '2026-05-22 23:35:46.697000', '对方:花海', '1', '平民', '31', '闲屿', '接受交易→花海 #35');
INSERT INTO `game_activity_log` VALUES ('157', 'quick', '2026-05-22 23:42:21.396000', '我、对酒、GPT和镇长Vigil一起守镇武库。\n​其中统治层将会把一把威胁5的手枪和对应的弹药1交付我保管', '1', '冒险者', '14', 'Κάκτος西里尔', '补充行动');
INSERT INTO `game_activity_log` VALUES ('158', 'action', '2026-05-22 23:43:44.151000', '目标:猎人小屋', '1', '平民', '18', 'Missbear', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('159', 'action', '2026-05-22 23:43:44.270000', null, '1', '平民', '18', 'Missbear', '自由#2·隐藏');
INSERT INTO `game_activity_log` VALUES ('160', 'consume', '2026-05-22 23:44:11.825000', '累计进食 2/2；取暖 5/15 热值', '1', '平民', '18', 'Missbear', '进食+2 木5kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('161', 'consume', '2026-05-23 00:05:45.071000', '累计进食 2/2；取暖 15/15 热值', '1', '平民', '25', 'tony', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('162', 'consume', '2026-05-23 00:13:29.075000', '累计进食 2/2；取暖 15/15 热值', '1', '平民', '18', 'Missbear', '进食+0 木10kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('163', 'trade', '2026-05-23 00:23:40.388000', '对方:二阶堂希罗 | give 未知物品×1 | take 食物×2 | take 燃料×1', '1', '统治者', '26', 'V', '发起交易→二阶堂希罗 #36');
INSERT INTO `game_activity_log` VALUES ('164', 'trade', '2026-05-23 00:24:12.018000', '对方:V | give 食物×2 | give 燃料×1 | take 哨子×1', '1', '统治者', '10', '二阶堂希罗', '发起交易→V #37');
INSERT INTO `game_activity_log` VALUES ('165', 'trade', '2026-05-23 00:24:17.642000', '对方:V', '1', '统治者', '10', '二阶堂希罗', '接受交易→V #36');
INSERT INTO `game_activity_log` VALUES ('166', 'trade', '2026-05-23 00:24:34.158000', '对方:V | give 燃料×5', '1', '统治者', '15', '空白', '发起交易→V #38');
INSERT INTO `game_activity_log` VALUES ('167', 'consume', '2026-05-23 00:31:49.089000', '累计进食 2/2；取暖 15/15 热值', '1', '平民', '28', '咲黑', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('168', 'consume', '2026-05-23 00:32:15.185000', '累计进食 2/2；取暖 15/15 热值', '1', '统治者', '26', 'V', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('169', 'trade', '2026-05-23 00:43:35.786000', '对方:花海', '1', '冒险者', '9', '对酒', '接受交易→花海 #34');
INSERT INTO `game_activity_log` VALUES ('170', 'night', '2026-05-23 00:44:00.373000', '✓ 已提交【其他】\n\n提交者：得狗的老意\n备注：我被所有人怀疑是天灾使者我非常愤怒我要偷偷抓一只蟑螂放在占卜师被窝里，要会飞的那种美洲大蠊\n\n等待主持人在夜晚阶段结算。', '1', '平民', '27', '得狗的老意', '其他');
INSERT INTO `game_activity_log` VALUES ('171', 'trade', '2026-05-23 00:44:58.895000', '对方:得狗的老意 | take 未知物品×30', '1', '冒险者', '9', '对酒', '发起交易→得狗的老意 #39');
INSERT INTO `game_activity_log` VALUES ('172', 'trade', '2026-05-23 00:45:31.525000', '对方:对酒', '1', '平民', '27', '得狗的老意', '接受交易→对酒 #39');
INSERT INTO `game_activity_log` VALUES ('173', 'quick', '2026-05-23 01:03:49.184000', '观察邮局墙上的邮票', '1', '冒险者', '21', '乐语', '快速行动');
INSERT INTO `game_activity_log` VALUES ('174', 'quick', '2026-05-23 01:04:11.147000', '晚上去旅馆睡觉', '1', '冒险者', '21', '乐语', '补充行动');
INSERT INTO `game_activity_log` VALUES ('175', 'quick', '2026-05-23 01:05:04.468000', '白天去矿场时候看没人看守顺手取走一些堆放的物资', '1', '冒险者', '21', '乐语', '补充行动');
INSERT INTO `game_activity_log` VALUES ('176', 'trade', '2026-05-23 01:30:51.178000', '对方:澡堂子 | give 燃料×5 | give 食物×2 | take 金属制品×5', '1', '反叛者', '13', '凭栏择雨', '发起交易→澡堂子 #40');
INSERT INTO `game_activity_log` VALUES ('177', 'trade', '2026-05-23 01:31:35.759000', '对方:凭栏择雨', '1', '平民', '32', '澡堂子', '接受交易→凭栏择雨 #40');
INSERT INTO `game_activity_log` VALUES ('178', 'trade', '2026-05-23 01:37:36.576000', '对方:得狗的老意 | take 未知物品×2 | take 木材×500 | take 食物×10', '1', '冒险者', '9', '对酒', '发起交易→得狗的老意 #41');
INSERT INTO `game_activity_log` VALUES ('179', 'trade', '2026-05-23 01:37:50.876000', '对方:得狗的老意 | take 猎枪×1', '1', '冒险者', '9', '对酒', '发起交易→得狗的老意 #42');
INSERT INTO `game_activity_log` VALUES ('180', 'trade', '2026-05-23 01:40:05.032000', '对方:得狗的老意 | take 未知物品×30 | take 未知物品×100 | take 金属制品×30 | take 猎枪弹×2 | take 斧头×2', '1', '冒险者', '9', '对酒', '发起交易→得狗的老意 #43');
INSERT INTO `game_activity_log` VALUES ('181', 'trade', '2026-05-23 01:40:34.031000', '对方:对酒', '1', '平民', '27', '得狗的老意', '接受交易→对酒 #43');
INSERT INTO `game_activity_log` VALUES ('182', 'trade', '2026-05-23 01:40:41.717000', '对方:对酒', '1', '平民', '27', '得狗的老意', '接受交易→对酒 #42');
INSERT INTO `game_activity_log` VALUES ('183', 'trade', '2026-05-23 01:40:43.757000', '对方:对酒', '1', '平民', '27', '得狗的老意', '接受交易→对酒 #41');
INSERT INTO `game_activity_log` VALUES ('184', 'trade', '2026-05-23 11:26:51.351000', '对方:二阶堂希罗 | give 未知物品×2 | take 食物×1', '2', '平民', '25', 'tony', '发起交易→二阶堂希罗 #44');
INSERT INTO `game_activity_log` VALUES ('185', 'consume', '2026-05-23 11:37:14.726000', '累计进食 2/2；取暖 15/15 热值', '2', '天灾使者', '20', '追枫', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('186', 'consume', '2026-05-23 11:49:46.756000', '累计进食 2/2；取暖 15/15 热值', '2', '反叛者', '13', '凭栏择雨', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('187', 'consume', '2026-05-23 14:00:59.444000', '累计进食 2/2；取暖 15/15 热值', '2', '天灾使者', '22', '11', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('188', 'consume', '2026-05-23 14:04:27.377000', '累计进食 2/2；取暖 15/15 热值', '2', '天灾使者', '16', '孤城暮角', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('189', 'trade', '2026-05-23 14:37:20.009000', '对方:追枫 | give 未知物品×1', '2', '天灾使者', '16', '孤城暮角', '发起交易→追枫 #45');
INSERT INTO `game_activity_log` VALUES ('190', 'trade', '2026-05-23 14:39:26.309000', '对方:追枫 | give 未知物品×1', '2', '天灾使者', '16', '孤城暮角', '发起交易→追枫 #46');
INSERT INTO `game_activity_log` VALUES ('191', 'trade', '2026-05-23 14:41:26.305000', '对方:孤城暮角', '2', '天灾使者', '20', '追枫', '接受交易→孤城暮角 #46');
INSERT INTO `game_activity_log` VALUES ('192', 'trade', '2026-05-23 14:41:31.105000', '对方:孤城暮角', '2', '天灾使者', '20', '追枫', '接受交易→孤城暮角 #45');
INSERT INTO `game_activity_log` VALUES ('193', 'consume', '2026-05-23 15:29:52.735000', '累计进食 2/2；取暖 15/15 热值', '2', '天灾使者', '8', '兔兔', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('194', 'trade', '2026-05-23 15:34:00.692000', '对方:孤城暮角 | give 金属制品×10', '2', '天灾使者', '8', '兔兔', '发起交易→孤城暮角 #47');
INSERT INTO `game_activity_log` VALUES ('195', 'trade', '2026-05-23 15:34:05.742000', '对方:兔兔', '2', '天灾使者', '16', '孤城暮角', '接受交易→兔兔 #47');
INSERT INTO `game_activity_log` VALUES ('196', 'action', '2026-05-23 15:42:39.920000', '目标:二阶堂希罗', '2', '天灾使者', '8', '兔兔', '自由#1·调查玩家');
INSERT INTO `game_activity_log` VALUES ('197', 'action', '2026-05-23 15:42:40.066000', '目标:V', '2', '天灾使者', '8', '兔兔', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('198', 'trade', '2026-05-23 15:55:23.955000', '对方:tony', '2', '统治者', '10', '二阶堂希罗', '接受交易→tony #44');
INSERT INTO `game_activity_log` VALUES ('199', 'action', '2026-05-23 16:09:55.613000', '目标:空白', '2', '平民', '18', 'Missbear', '自由#1·调查玩家');
INSERT INTO `game_activity_log` VALUES ('200', 'action', '2026-05-23 16:09:55.690000', '目标:得狗的老意', '2', '平民', '18', 'Missbear', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('201', 'trade', '2026-05-23 16:57:01.858000', '对方:孤城暮角 | give 未知物品×1 | give 刺刀×1 | give 木材×65 | give 未知物品×20 | give 食物×9 | give 燃料×9', '2', '天灾使者', '22', '11', '发起交易→孤城暮角 #48');
INSERT INTO `game_activity_log` VALUES ('202', 'trade', '2026-05-23 16:57:33.177000', '对方:11', '2', '天灾使者', '16', '孤城暮角', '接受交易→11 #48');
INSERT INTO `game_activity_log` VALUES ('203', 'consume', '2026-05-23 19:03:04.184000', '累计进食 2/2；取暖 15/15 热值', '2', '冒险者', '9', '对酒', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('204', 'consume', '2026-05-23 19:06:51.092000', '累计进食 1/2；取暖 15/15 热值', '2', '平民', '28', '咲黑', '进食+1 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('205', 'consume', '2026-05-23 19:07:21.649000', '累计进食 2/2；取暖 15/15 热值', '2', '冒险者', '19', 'unPy-GPT', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('206', 'action', '2026-05-23 19:07:44.120000', '目标:空白', '2', '平民', '28', '咲黑', '自由#1·调查玩家');
INSERT INTO `game_activity_log` VALUES ('207', 'action', '2026-05-23 19:07:44.364000', '目标:飞凡', '2', '平民', '28', '咲黑', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('208', 'trade', '2026-05-23 19:08:18.471000', '对方:Κάκτος西里尔 | give 猎枪弹×2 | give 猎枪×1', '2', '冒险者', '9', '对酒', '发起交易→Κάκτος西里尔 #49');
INSERT INTO `game_activity_log` VALUES ('209', 'trade', '2026-05-23 19:15:13.066000', '对方:11 | give 未知物品×1', '2', '冒险者', '9', '对酒', '发起交易→11 #50');
INSERT INTO `game_activity_log` VALUES ('210', 'trade', '2026-05-23 19:15:59.857000', '对方:乐语 | give 未知物品×1', '2', '冒险者', '9', '对酒', '发起交易→乐语 #51');
INSERT INTO `game_activity_log` VALUES ('211', 'trade', '2026-05-23 19:16:27.726000', '对方:unPy-GPT | give 斧头×1', '2', '冒险者', '9', '对酒', '发起交易→unPy-GPT #52');
INSERT INTO `game_activity_log` VALUES ('212', 'trade', '2026-05-23 19:17:06.023000', '对方:对酒', '2', '冒险者', '19', 'unPy-GPT', '接受交易→对酒 #52');
INSERT INTO `game_activity_log` VALUES ('213', 'trade', '2026-05-23 19:17:13.294000', '对方:对酒', '2', '天灾使者', '22', '11', '接受交易→对酒 #50');
INSERT INTO `game_activity_log` VALUES ('214', 'trade', '2026-05-23 19:18:01.149000', '对方:对酒 | give 刺刀×1', '2', '冒险者', '19', 'unPy-GPT', '发起交易→对酒 #53');
INSERT INTO `game_activity_log` VALUES ('215', 'trade', '2026-05-23 19:18:09.316000', '对方:unPy-GPT', '2', '冒险者', '9', '对酒', '接受交易→unPy-GPT #53');
INSERT INTO `game_activity_log` VALUES ('216', 'consume', '2026-05-23 19:22:06.520000', '累计进食 0/2；取暖 15/15 热值', '2', '统治者', '15', '空白', '进食+0 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('217', 'consume', '2026-05-23 19:24:08.613000', '累计进食 2/2；取暖 15/15 热值', '2', '统治者', '10', '二阶堂希罗', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('218', 'trade', '2026-05-23 19:27:02.141000', '对方:空白', '2', '统治者', '26', 'V', '接受交易→空白 #38');
INSERT INTO `game_activity_log` VALUES ('219', 'trade', '2026-05-23 19:27:07.538000', '对方:二阶堂希罗', '2', '统治者', '26', 'V', '接受交易→二阶堂希罗 #37');
INSERT INTO `game_activity_log` VALUES ('220', 'action', '2026-05-23 19:33:24.412000', '目标:监狱 NPC:乔克·汤姆 给他五瓶酒 让他说出一些情报并且喝醉离开', '2', '反叛者', '11', '蟋蟀蜥蜴', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('221', 'action', '2026-05-23 19:33:24.444000', '目标:监狱 劫狱', '2', '反叛者', '11', '蟋蟀蜥蜴', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('222', 'trade', '2026-05-23 19:34:33.920000', '对方:空白 | give 食物×4', '2', '平民', '25', 'tony', '发起交易→空白 #54');
INSERT INTO `game_activity_log` VALUES ('223', 'trade', '2026-05-23 19:36:23.851000', '对方:对酒', '2', '冒险者', '21', '乐语', '接受交易→对酒 #51');
INSERT INTO `game_activity_log` VALUES ('224', 'trade', '2026-05-23 19:36:29.652000', '对方:乐语 | give 食物×4', '2', '冒险者', '19', 'unPy-GPT', '发起交易→乐语 #55');
INSERT INTO `game_activity_log` VALUES ('225', 'quick', '2026-05-23 19:37:41.844000', '去码头拆船需要用白天行动点吗，还是用快速行动', '2', '冒险者', '21', '乐语', '规则咨询');
INSERT INTO `game_activity_log` VALUES ('226', 'trade', '2026-05-23 19:38:09.574000', '对方:unPy-GPT', '2', '冒险者', '21', '乐语', '接受交易→unPy-GPT #55');
INSERT INTO `game_activity_log` VALUES ('227', 'consume', '2026-05-23 19:38:25.183000', '累计进食 2/2；取暖 15/15 热值', '2', '冒险者', '21', '乐语', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('228', 'action', '2026-05-23 19:39:58.363000', '目标:镇长厅', '2', '天灾使者', '20', '追枫', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('229', 'action', '2026-05-23 19:39:58.457000', '目标:教皇', '2', '天灾使者', '20', '追枫', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('230', 'trade', '2026-05-23 19:40:10.428000', '对方:花海 | give 金属制品×2', '2', '反叛者', '13', '凭栏择雨', '发起交易→花海 #56');
INSERT INTO `game_activity_log` VALUES ('231', 'trade', '2026-05-23 19:41:08.521000', '对方:MISD330 | give 未知物品×10', '2', '平民', '29', '飞凡', '发起交易→MISD330 #57');
INSERT INTO `game_activity_log` VALUES ('232', 'trade', '2026-05-23 19:41:43.708000', '对方:对酒', '2', '冒险者', '14', 'Κάκτος西里尔', '接受交易→对酒 #49');
INSERT INTO `game_activity_log` VALUES ('233', 'faction', '2026-05-23 19:42:55.240000', '✓ 已提交【诅咒】\n\n消耗武器：猎弓\n目标1：教皇\n目标2：闲屿\n\n效果：获知阵营、施加「诅咒」标记。\n等待主持人确认。', '2', '天灾使者', '20', '追枫', '诅咒');
INSERT INTO `game_activity_log` VALUES ('234', 'quick', '2026-05-23 19:43:08.923000', '对 对酒（气象观测）散播瘟疫', '2', '天灾使者', '16', '孤城暮角', '快速行动');
INSERT INTO `game_activity_log` VALUES ('235', 'trade', '2026-05-23 19:43:39.911000', '对方:飞凡', '2', '反叛者', '30', 'MISD330', '接受交易→飞凡 #57');
INSERT INTO `game_activity_log` VALUES ('236', 'trade', '2026-05-23 19:45:34.459000', '对方:tony', '2', '统治者', '15', '空白', '接受交易→tony #54');
INSERT INTO `game_activity_log` VALUES ('237', 'consume', '2026-05-23 19:45:46.216000', '累计进食 2/2；取暖 15/15 热值', '2', '统治者', '15', '空白', '进食+2 木0kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('238', 'consume', '2026-05-23 19:46:40.819000', '累计进食 2/2；取暖 15/15 热值', '2', '反叛者', '24', '花海', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('239', 'trade', '2026-05-23 19:46:48.355000', '对方:凭栏择雨', '2', '反叛者', '24', '花海', '接受交易→凭栏择雨 #56');
INSERT INTO `game_activity_log` VALUES ('240', 'trade', '2026-05-23 19:47:31.879000', '对方:兔兔 | give 食物×2 | take 金属制品×2', '2', '反叛者', '24', '花海', '发起交易→兔兔 #58');
INSERT INTO `game_activity_log` VALUES ('241', 'action', '2026-05-23 19:49:19.596000', null, '2', '反叛者', '24', '花海', '自由#1·生产');
INSERT INTO `game_activity_log` VALUES ('242', 'action', '2026-05-23 19:49:19.692000', '百宝袋/复制列表的5个初始物品，我要负责防弹衣、医疗物品，金属，燃料剩余的随机', '2', '反叛者', '24', '花海', '自由#2·使用特性');
INSERT INTO `game_activity_log` VALUES ('243', 'quick', '2026-05-23 19:51:22.911000', '使用一个便当加额外的行动', '2', '统治者', '10', '二阶堂希罗', '快速行动');
INSERT INTO `game_activity_log` VALUES ('244', 'action', '2026-05-23 19:52:38.069000', '自己带着斧头邀请维修工（玩家）和船长（玩家带枪）一起前往伐木营地，先和伐木工（npc）进行一个恳求，说明现在暴风雪来临，我们很需要他的帮助，需要木板蒸汽箱，拖拉机，电锯，木板（或者原木），发电机组（相当于搜刮地点），并且和他说镇长也同意我们求取这些，给了我们这些就不会强迫他当劳工了。如果他愿意就邀请他上船（船长可以适当展示一下他的猎枪）。伐木工（npc）始终不愿意，让船长拿枪威胁他，最起码把载具和发电机交出来，如果他还是不同意，就把他束缚起来，拿走载具和发电机组。如果他实在…', '2', '冒险者', '9', '对酒', '自由#1·其他');
INSERT INTO `game_activity_log` VALUES ('245', 'action', '2026-05-23 19:52:38.192000', '目标:码头 NPC:鲍勃·塔克 邀请鲍勃和我一起去拆码头的船（消息来源于邮差）并且邀请他上船，他同意的话将在接下来的时间内和我们一起搬运物资（如果不能同时操作就只拆码头的船）', '2', '冒险者', '9', '对酒', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('246', 'action', '2026-05-23 19:55:02.400000', '目标:伐木营地 NPC:托马斯·伍德 自己带着霰弹枪和弹药1发，与维修工（玩家）和气象观测员（玩家）一起前往伐木营地。\n\n先和伐木工（npc）进行一个恳求，说明现在暴风雪来临，我们很需要他的帮助，需要木板蒸汽箱，拖拉机，电锯，木板（或者原木），发电机组（相当于搜刮地点），并且和他说镇长也同意我们求取这些，给了我们这些就不会强迫他当劳工了。\n\n如果他愿意就邀请他上船，本人会适当展示自己手中的霰弹枪。\n\n若伐木工（npc）始终不愿意，本人将拿枪威胁他，最起码把载具和发电机交出来。\n\n若再不同意，就把他束缚起来，拿走…', '2', '冒险者', '14', 'Κάκτος西里尔', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('247', 'action', '2026-05-23 19:55:02.506000', '目标:集市 NPC:米玛·雷铁斯托 先问手工艺人找我有什么事，听镇长说手工艺人有事找我。\n\n然后问手工艺人手中是否有沥青，如果可以的话向手工艺人要些沥青。如果不行就问对方哪里能搞到沥青。', '2', '冒险者', '14', 'Κάκτος西里尔', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('248', 'trade', '2026-05-23 19:55:08.928000', '对方:蟋蟀蜥蜴 | give 食物×2', '2', '反叛者', '24', '花海', '发起交易→蟋蟀蜥蜴 #59');
INSERT INTO `game_activity_log` VALUES ('249', 'trade', '2026-05-23 19:55:44.231000', '对方:tony | give 食物×20', '2', '反叛者', '24', '花海', '发起交易→tony #60');
INSERT INTO `game_activity_log` VALUES ('250', 'trade', '2026-05-23 19:55:54.771000', '对方:花海', '2', '平民', '25', 'tony', '接受交易→花海 #60');
INSERT INTO `game_activity_log` VALUES ('251', 'action', '2026-05-23 19:57:44.958000', '模式：仓库→个人；源：镇武库；医疗包×2；手电筒×8；手铐×2；防弹衣×1；复合盾×4；信号枪×1；朗姆酒×20；草药×3；渔网×1；蜡烛×20；医用酒精×5；火柴×2；破损海图×1；便当×1；制式手枪×2；猎枪×1；警棍×3；刺刀×2；鱼叉/矛×2；猎弓×1；手枪弹×4；猎枪弹×2；信号弹×2；箭矢×4；沥青×50；燃料×178；螺旋桨×3', '2', '统治者', '10', '二阶堂希罗', '自由#1·搬运');
INSERT INTO `game_activity_log` VALUES ('252', 'action', '2026-05-23 19:57:45.054000', '目标:矿场', '2', '统治者', '10', '二阶堂希罗', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('253', 'trade', '2026-05-23 19:59:00.391000', '对方:tony | give 木材×30 | take 未知物品×4', '2', '反叛者', '30', 'MISD330', '发起交易→tony #61');
INSERT INTO `game_activity_log` VALUES ('254', 'action', '2026-05-23 19:59:58.236000', '在码头拆船，并邀请码头的人跟我一起搬（搬到自己这）', '2', '冒险者', '22', '11', '自由#1·其他');
INSERT INTO `game_activity_log` VALUES ('255', 'action', '2026-05-23 19:59:58.298000', '使用天灾特性去削弱食物', '2', '冒险者', '22', '11', '自由#2·使用特性');
INSERT INTO `game_activity_log` VALUES ('256', 'trade', '2026-05-23 20:01:33.156000', '对方:MISD330 | give 未知物品×3 | take 木材×30', '2', '平民', '25', 'tony', '发起交易→MISD330 #62');
INSERT INTO `game_activity_log` VALUES ('257', 'trade', '2026-05-23 20:01:52.916000', '对方:tony', '2', '反叛者', '30', 'MISD330', '接受交易→tony #62');
INSERT INTO `game_activity_log` VALUES ('258', 'trade', '2026-05-23 20:02:20.064000', '对方:二阶堂希罗 | take 食物×10 | take 木材×15', '2', '平民', '25', 'tony', '发起交易→二阶堂希罗 #63');
INSERT INTO `game_activity_log` VALUES ('259', 'trade', '2026-05-23 20:02:43.026000', '对方:二阶堂希罗 | take 木材×15 | take 食物×10', '2', '平民', '25', 'tony', '发起交易→二阶堂希罗 #64');
INSERT INTO `game_activity_log` VALUES ('260', 'action', '2026-05-23 20:03:28.588000', '模式：仓库→个人；源：矿场仓库；维修工具包×2；十字镐×2；斧头×1；食物×270；燃料×20', '2', '统治者', '17', 'zzz', '自由#1·搬运');
INSERT INTO `game_activity_log` VALUES ('261', 'action', '2026-05-23 20:03:28.654000', '目标:孤城暮角', '2', '统治者', '17', 'zzz', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('262', 'trade', '2026-05-23 20:03:40.198000', '对方:tony', '2', '统治者', '10', '二阶堂希罗', '接受交易→tony #63');
INSERT INTO `game_activity_log` VALUES ('263', 'trade', '2026-05-23 20:03:42.053000', '对方:tony', '2', '统治者', '10', '二阶堂希罗', '接受交易→tony #64');
INSERT INTO `game_activity_log` VALUES ('264', 'action', '2026-05-23 20:05:47.899000', '目标:矿场 NPC:维克多·斯通 与其沟通，阐述现在统治的残暴！尝试使用便当和承诺其他物品，拉拢他，换点金属，并请求协助夜晚打劫监狱', '2', '反叛者', '30', 'MISD330', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('265', 'action', '2026-05-23 20:05:47.939000', '目标:集市 NPC:米玛·雷铁斯托 与其沟通，阐述现在统治的残暴！尝试使用便当和承诺其他物品，拉拢他，制造盾牌，并请求协助夜晚打劫监狱', '2', '反叛者', '30', 'MISD330', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('266', 'quick', '2026-05-23 20:08:46.311000', '使用一个面包加行动值', '2', '统治者', '10', '二阶堂希罗', '快速行动');
INSERT INTO `game_activity_log` VALUES ('267', 'consume', '2026-05-23 20:09:22.313000', '累计进食 2/2；取暖 15/15 热值', '2', '冒险者', '14', 'Κάκτος西里尔', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('268', 'quick', '2026-05-23 20:09:36.709000', '天灾使者的破坏行动：破坏面包店炉子', '2', '冒险者', '22', '11', '快速行动');
INSERT INTO `game_activity_log` VALUES ('269', 'action', '2026-05-23 20:09:41.334000', '目标:码头 NPC:鲍勃·塔克 塔克，你在这码头扛了半辈子麻袋，可攒下过真正属于自己的东西？这场暴风雪过后，港口都将不复存在。方舟是我们唯一的希望——跟我走，不需要你再为别人负重前行。冒险者的船上有你的位置，新世界的大地等着我们去征服。放下这个麻袋，拿起桨，当自己的主人。是冻死在这里，还是去创造未来，你选。', '2', '冒险者', '19', 'unPy-GPT', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('270', 'action', '2026-05-23 20:09:41.392000', '邀请装卸工一起在码头从阿弗雷号船上拆下可以用来建造方舟的相关材料', '2', '冒险者', '19', 'unPy-GPT', '自由#2·其他');
INSERT INTO `game_activity_log` VALUES ('271', 'trade', '2026-05-23 20:09:45.795000', '对方:V | give 未知物品×1 | take 未知物品×1', '2', '统治者', '10', '二阶堂希罗', '发起交易→V #65');
INSERT INTO `game_activity_log` VALUES ('272', 'trade', '2026-05-23 20:10:03.976000', '对方:花海', '2', '反叛者', '11', '蟋蟀蜥蜴', '接受交易→花海 #59');
INSERT INTO `game_activity_log` VALUES ('273', 'quick', '2026-05-23 20:10:12.479000', '白天行动1，去伐木工处，气象观测员不再陪同。', '2', '冒险者', '14', 'Κάκτος西里尔', '补充行动');
INSERT INTO `game_activity_log` VALUES ('274', 'consume', '2026-05-23 20:10:17.281000', '累计进食 2/2；取暖 15/15 热值', '2', '反叛者', '11', '蟋蟀蜥蜴', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('275', 'action', '2026-05-23 20:10:22.637000', '目标:荷叶男巫 我来杀你了嘿嘿嘿嘿', '2', '平民', '25', 'tony', '自由#1·调查玩家');
INSERT INTO `game_activity_log` VALUES ('276', 'action', '2026-05-23 20:10:22.909000', '注意：先收取来自统治者的物资。然后进行烹饪。使用14食物，30木材获得两个面包。', '2', '平民', '25', 'tony', '自由#2·使用职业技能');
INSERT INTO `game_activity_log` VALUES ('277', 'quick', '2026-05-23 20:10:57.126000', '白天的行动1，去伐木工处，气象观测员不再陪同', '2', '冒险者', '14', 'Κάκτος西里尔', '补充行动');
INSERT INTO `game_activity_log` VALUES ('278', 'faction', '2026-05-23 20:11:14.475000', '✓ 已提交【额外行动】\n\n行动类型：前往地点\n前往地点：镇长厅\n备注：调查一下有没有什么人来过这里干了什么\n\n此环节无法寻找NPC进行对话。\n等待主持人确认。', '2', '反叛者', '11', '蟋蟀蜥蜴', '额外行动');
INSERT INTO `game_activity_log` VALUES ('279', 'action', '2026-05-23 20:11:35.484000', '教师启蒙技能捕鱼进行生产', '2', '反叛者', '13', '凭栏择雨', '自由#1·使用职业技能');
INSERT INTO `game_activity_log` VALUES ('280', 'action', '2026-05-23 20:11:35.577000', null, '2', '反叛者', '13', '凭栏择雨', '自由#2·隐藏');
INSERT INTO `game_activity_log` VALUES ('281', 'quick', '2026-05-23 20:11:44.948000', '快速行动1: 先收取来自统治者的物资。（食物，木材，和武器）', '2', '平民', '25', 'tony', '快速行动');
INSERT INTO `game_activity_log` VALUES ('282', 'quick', '2026-05-23 20:12:33.822000', '使用一个面包。进行第三个人行动。再次使用职业技能。获得两块面包。', '2', '平民', '25', 'tony', '快速行动');
INSERT INTO `game_activity_log` VALUES ('283', 'trade', '2026-05-23 20:12:46.513000', '对方:对酒 | give 燃料仓库钥匙×1 | take 未知物品×1', '2', '统治者', '26', 'V', '发起交易→对酒 #66');
INSERT INTO `game_activity_log` VALUES ('284', 'trade', '2026-05-23 20:12:57.507000', '对方:V', '2', '冒险者', '9', '对酒', '接受交易→V #66');
INSERT INTO `game_activity_log` VALUES ('285', 'trade', '2026-05-23 20:13:04.663000', '对方:二阶堂希罗', '2', '统治者', '26', 'V', '接受交易→二阶堂希罗 #65');
INSERT INTO `game_activity_log` VALUES ('286', 'faction', '2026-05-23 20:13:51.529000', '✓ 已提交【额外调查】\n\n调查类型：调查地点\n调查目标：气象观测站\n\n结算后该次调查数量将翻倍。等待主持人确认。', '2', '冒险者', '9', '对酒', '额外调查');
INSERT INTO `game_activity_log` VALUES ('287', 'quick', '2026-05-23 20:14:41.766000', '个人行动结算后。一共给予两个面包给统治者。一个面包给医生（反抗者）。注意，每搓成一个面包就给出去，优先给反抗者。', '2', '平民', '25', 'tony', '快速行动');
INSERT INTO `game_activity_log` VALUES ('288', 'action', '2026-05-23 20:17:13.863000', '自己带着斧头邀请维修工（玩家）和船长（玩家带枪）一起前往伐木营地，先和伐木工（npc）进行一个恳求，说明现在暴风雪来临，我们很需要他的帮助，需要木板蒸汽箱，拖拉机，电锯，木板（或者原木），发电机组（相当于搜刮地点），并且和他说镇长也同意我们求取这些，给了我们这些就不会强迫他当劳工了。如果他愿意就邀请他上船（船长可以适当展示一下他的猎枪）。伐木工（npc）始终不愿意，让船长拿枪威胁他，最起码把载具和发电机交出来，如果他还是不同意，就把他束缚起来，拿走载具和发电机组。如果他实在…', '2', '冒险者', '9', '对酒', '自由#1·其他');
INSERT INTO `game_activity_log` VALUES ('289', 'action', '2026-05-23 20:17:13.988000', '去码头执行拆船任务（消息来源于邮差）', '2', '冒险者', '9', '对酒', '自由#2·其他');
INSERT INTO `game_activity_log` VALUES ('290', 'quick', '2026-05-23 20:19:08.332000', '对 对酒（天气预测）散播瘟疫', '2', '天灾使者', '16', '孤城暮角', '快速行动');
INSERT INTO `game_activity_log` VALUES ('291', 'trade', '2026-05-23 20:25:04.552000', '对方:教皇 | give 食物×1', '2', '平民', '25', 'tony', '发起交易→教皇 #67');
INSERT INTO `game_activity_log` VALUES ('292', 'consume', '2026-05-23 20:25:14.686000', '累计进食 2/2；取暖 15/15 热值', '2', '平民', '25', 'tony', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('293', 'trade', '2026-05-23 20:25:33.987000', '对方:tony', '2', '反叛者', '23', '教皇', '接受交易→tony #67');
INSERT INTO `game_activity_log` VALUES ('294', 'action', '2026-05-23 20:26:30.693000', '对农民，医生，神父使用布道', '2', '反叛者', '23', '教皇', '自由#1·使用职业技能');
INSERT INTO `game_activity_log` VALUES ('295', 'action', '2026-05-23 20:26:31.093000', '目标:监狱 NPC:乔克·汤姆 不动声色地利用自己神父的身份套取一些信息', '2', '反叛者', '23', '教皇', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('296', 'consume', '2026-05-23 20:26:49.011000', '累计进食 2/2；取暖 15/15 热值', '2', '反叛者', '23', '教皇', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('297', 'faction', '2026-05-23 20:28:22.180000', '✓ 已提交【额外行动】\n\n行动类型：前往地点\n前往地点：监狱\n备注：调查监狱\n\n此环节无法寻找NPC进行对话。\n等待主持人确认。', '2', '反叛者', '30', 'MISD330', '额外行动');
INSERT INTO `game_activity_log` VALUES ('298', 'consume', '2026-05-23 20:28:55.558000', '累计进食 2/2；取暖 15/15 热值', '2', '反叛者', '30', 'MISD330', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('299', 'night', '2026-05-23 20:31:38.532000', '✓ 已提交【其他】\n\n提交者：飞凡\n备注：去旅店睡觉\n\n等待主持人在夜晚阶段结算。', '2', '平民', '29', '飞凡', '其他');
INSERT INTO `game_activity_log` VALUES ('300', 'consume', '2026-05-23 20:31:54.057000', '累计进食 2/2；取暖 15/15 热值', '2', '平民', '29', '飞凡', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('301', 'quick', '2026-05-23 20:33:03.794000', '喝酒消除疲劳', '2', '平民', '29', '飞凡', '快速行动');
INSERT INTO `game_activity_log` VALUES ('302', 'quick', '2026-05-23 20:35:01.075000', '白天行动一：在矿场与名为铁锤的矿工交互，并执行行动一备注里的行动。白天行动二：在集市与那位手工艺人交互，并执行备注中的行动！', '2', '反叛者', '30', 'MISD330', '补充行动');
INSERT INTO `game_activity_log` VALUES ('303', 'night', '2026-05-23 20:35:18.476000', '✓ 已提交【进行密谋】\n\n提交者：tony\n密谋类型：暗杀目标\n目标地点：面包店\n参与玩家：V\n备注：我选的暗杀目标但他要我选目标地点这对吗？反正我就是来杀你的荷叶。\n\n等待主持人在夜晚阶段结算。', '2', '平民', '25', 'tony', '进行密谋');
INSERT INTO `game_activity_log` VALUES ('304', 'trade', '2026-05-23 20:36:28.742000', '对方:花海', '2', '天灾使者', '8', '兔兔', '接受交易→花海 #58');
INSERT INTO `game_activity_log` VALUES ('305', 'night', '2026-05-23 20:37:58.862000', '✓ 已提交【进行密谋】\n\n提交者：Missbear\n密谋类型：暗杀目标\n目标地点：杂货店\n参与玩家：荷叶男巫\n\n等待主持人在夜晚阶段结算。', '2', '平民', '18', 'Missbear', '进行密谋');
INSERT INTO `game_activity_log` VALUES ('306', 'night', '2026-05-23 20:39:28.663000', '✓ 已提交【夜晚个人行动】\n\n提交者：二阶堂希罗\n行动：前往地点\n目标：矿场\n备注：在矿产过夜顺便寻找是否有炸药\n\n等待主持人在夜晚阶段结算。', '2', '统治者', '10', '二阶堂希罗', '夜晚个人行动');
INSERT INTO `game_activity_log` VALUES ('307', 'action', '2026-05-23 20:41:47.471000', '模式：仓库→个人；源：镇武库；木材×18；食物×260；燃料×22', '2', '统治者', '26', 'V', '自由#1·搬运');
INSERT INTO `game_activity_log` VALUES ('308', 'action', '2026-05-23 20:41:48.362000', '目标:咲黑', '2', '统治者', '26', 'V', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('309', 'action', '2026-05-23 20:42:34.053000', '使用维修工技能 修tony面包师的炉子', '2', '统治者', '15', '空白', '自由#1·使用职业技能');
INSERT INTO `game_activity_log` VALUES ('310', 'action', '2026-05-23 20:42:34.140000', '模式：仓库→个人；源：镇武库；防弹衣×1；复合盾×1；猎枪×1；猎枪弹×2；箭矢×1；木材×141；食物×100；发电机×1', '2', '统治者', '15', '空白', '自由#2·搬运');
INSERT INTO `game_activity_log` VALUES ('311', 'night', '2026-05-23 20:50:33.717000', '✓ 已提交【夜晚个人行动】\n\n提交者：空白\n行动：使用特性\n备注：使用窃听者特性 看反抗阵营群聊记录\n\n等待主持人在夜晚阶段结算。', '2', '统治者', '15', '空白', '夜晚个人行动');
INSERT INTO `game_activity_log` VALUES ('312', 'night', '2026-05-23 20:54:42.148000', '✓ 已提交【进行密谋】\n\n提交者：MISD330\n密谋类型：袭击地点\n目标地点：监狱\n参与玩家：蟋蟀蜥蜴、凭栏择雨、tony、教皇、花海\n成功后意向：搜刮资源\n备注：还有我的俩npc大军！（要是他俩同意的话）\n\n等待主持人在夜晚阶段结算。', '2', '反叛者', '30', 'MISD330', '进行密谋');
INSERT INTO `game_activity_log` VALUES ('313', 'trade', '2026-05-23 21:04:16.525000', '对方:乐语 | give 未知物品×30 | give 金属制品×40', '2', '冒险者', '9', '对酒', '发起交易→乐语 #68');
INSERT INTO `game_activity_log` VALUES ('314', 'trade', '2026-05-23 21:04:26.836000', '对方:对酒', '2', '冒险者', '21', '乐语', '接受交易→对酒 #68');
INSERT INTO `game_activity_log` VALUES ('315', 'action', '2026-05-23 21:04:56.174000', '小心翼翼的去码头仔仔细细的拆那艘名叫阿弗雷号的船，然后都放入个人背包中', '2', '冒险者', '21', '乐语', '自由#1·其他');
INSERT INTO `game_activity_log` VALUES ('316', 'action', '2026-05-23 21:04:56.374000', '使用渴望出海特性，并按照一次建设最大使用量从自身背包和可调用仓库中使用资源建造方舟', '2', '冒险者', '21', '乐语', '自由#2·使用特性');
INSERT INTO `game_activity_log` VALUES ('317', 'consume', '2026-05-23 21:06:40.752000', '累计进食 2/2；取暖 15/15 热值', '2', '平民', '27', '得狗的老意', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('318', 'quick', '2026-05-23 21:06:58.767000', '再喝两个酒', '2', '平民', '27', '得狗的老意', '快速行动');
INSERT INTO `game_activity_log` VALUES ('319', 'night', '2026-05-23 21:09:18.460000', '✓ 已提交【其他】\n\n提交者：蟋蟀蜥蜴\n备注：袭击监狱 顺便观察自己阵营中有无内鬼\n\n等待主持人在夜晚阶段结算。', '2', '反叛者', '11', '蟋蟀蜥蜴', '其他');
INSERT INTO `game_activity_log` VALUES ('320', 'quick', '2026-05-23 21:09:33.996000', '观察自己队伍中有没有内鬼', '2', '反叛者', '11', '蟋蟀蜥蜴', '快速行动');
INSERT INTO `game_activity_log` VALUES ('321', 'trade', '2026-05-23 21:12:20.064000', '对方:乐语 | give 木材×80', '2', '冒险者', '14', 'Κάκτος西里尔', '发起交易→乐语 #69');
INSERT INTO `game_activity_log` VALUES ('322', 'faction', '2026-05-23 21:26:02.568000', '✓ 已提交【安排看守】\n\n看守人员：花海\n看守地点：矿场\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。', '2', '统治者', '10', '二阶堂希罗', '安排看守');
INSERT INTO `game_activity_log` VALUES ('323', 'faction', '2026-05-23 21:26:36.614000', '✓ 已提交【安排看守】\n\n看守人员：凭栏择雨\n看守地点：监狱\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。', '2', '统治者', '10', '二阶堂希罗', '安排看守');
INSERT INTO `game_activity_log` VALUES ('324', 'faction', '2026-05-23 21:27:31.297000', '✓ 已提交【安排看守】\n\n看守人员：得狗的老意\n看守地点：监狱\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。', '2', '统治者', '10', '二阶堂希罗', '安排看守');
INSERT INTO `game_activity_log` VALUES ('325', 'faction', '2026-05-23 21:28:06.386000', '✓ 已提交【安排看守】\n\n看守人员：教皇\n看守地点：监狱\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。', '2', '统治者', '10', '二阶堂希罗', '安排看守');
INSERT INTO `game_activity_log` VALUES ('326', 'faction', '2026-05-23 21:28:15.924000', '✓ 已提交【安排看守】\n\n看守人员：MISD330\n看守地点：监狱\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。', '2', '统治者', '10', '二阶堂希罗', '安排看守');
INSERT INTO `game_activity_log` VALUES ('327', 'faction', '2026-05-23 21:28:24.127000', '✓ 已提交【安排看守】\n\n看守人员：孤城暮角\n看守地点：监狱\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。', '2', '统治者', '10', '二阶堂希罗', '安排看守');
INSERT INTO `game_activity_log` VALUES ('328', 'quick', '2026-05-23 21:28:48.126000', '使用便当，额外使用一个行动点，搬运物资，从矿场仓库到避难所，优先生活必须品', '2', '统治者', '26', 'V', '补充行动');
INSERT INTO `game_activity_log` VALUES ('329', 'quick', '2026-05-23 21:29:28.244000', '使用面包，前往监狱', '2', '统治者', '10', '二阶堂希罗', '快速行动');
INSERT INTO `game_activity_log` VALUES ('330', 'trade', '2026-05-23 21:30:45.310000', '对方:Κάκτος西里尔', '2', '冒险者', '21', '乐语', '接受交易→Κάκτος西里尔 #69');
INSERT INTO `game_activity_log` VALUES ('331', 'faction', '2026-05-23 21:32:46.469000', '✓ 已提交【安排看守】\n\n看守人员：闲屿\n看守地点：监狱\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。', '2', '统治者', '10', '二阶堂希罗', '安排看守');
INSERT INTO `game_activity_log` VALUES ('332', 'trade', '2026-05-23 21:49:06.512000', '对方:兔兔 | give 金属制品×4', '2', '反叛者', '24', '花海', '发起交易→兔兔 #70');
INSERT INTO `game_activity_log` VALUES ('333', 'trade', '2026-05-23 21:49:46.876000', '对方:花海', '2', '天灾使者', '8', '兔兔', '接受交易→花海 #70');
INSERT INTO `game_activity_log` VALUES ('334', 'consume', '2026-05-23 21:50:06.380000', '累计进食 2/2；取暖 15/15 热值', '2', '统治者', '26', 'V', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('335', 'trade', '2026-05-23 21:51:00.339000', '对方:孤城暮角 | give 金属制品×2', '2', '天灾使者', '8', '兔兔', '发起交易→孤城暮角 #71');
INSERT INTO `game_activity_log` VALUES ('336', 'trade', '2026-05-23 21:51:53.737000', '对方:兔兔', '2', '天灾使者', '16', '孤城暮角', '接受交易→兔兔 #71');
INSERT INTO `game_activity_log` VALUES ('337', 'quick', '2026-05-23 22:15:47.553000', '行动一：自己带着霰弹枪和弹药1发，与维修工（玩家）一起前往伐木营地。\n\n先和伐木工（npc）进行一个恳求，说明现在暴风雪来临，我们很需要他的帮助，需要木板蒸汽箱，拖拉机，电锯，木板（或者原木），发电机组（相当于搜刮地点），并且和他说镇长也同意我们求取这些，给了我们这些就不会强迫他当劳工了。\n\n如果他愿意就邀请他上船，本人会适当展示自己手中的霰弹枪。\n\n若伐木工（npc）始终不愿意，本人将拿枪威胁他，最起码把载具和发电机交出来。\n\n若再不同意，就把他束缚起来，拿走载具和发电机组。\n\n最坏的情况，如果他实在反抗激烈，就枪杀他，以无论如何都要拿到载具和发电机组为目的。', '2', '冒险者', '14', 'Κάκτος西里尔', '补充行动');
INSERT INTO `game_activity_log` VALUES ('338', 'quick', '2026-05-23 22:16:09.523000', '行动二：先问手工艺人找我有什么事，听镇长说手工艺人有事找我。\n\n然后问手工艺人手中是否有沥青，如果可以的话向手工艺人要些沥青。如果不行就问对方哪里能搞到沥青。', '2', '冒险者', '14', 'Κάκτος西里尔', '补充行动');
INSERT INTO `game_activity_log` VALUES ('339', 'quick', '2026-05-23 22:16:24.623000', '对于第一个伐木营地的行动，我们还拆除了电机，要求拿电锯，然后我这里签订契约同意木工上船，拿到钥匙', '2', '冒险者', '9', '对酒', '询问DM');
INSERT INTO `game_activity_log` VALUES ('340', 'consume', '2026-05-23 22:20:56.274000', '累计进食 2/2；取暖 15/15 热值', '2', '统治者', '17', 'zzz', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('341', 'quick', '2026-05-23 22:24:46.305000', '在探索地点部分我想邀请码头装卸工跟随我们冒险者一起，加入我们冒险者阵营，一起登上方舟', '2', '冒险者', '19', 'unPy-GPT', '补充行动');
INSERT INTO `game_activity_log` VALUES ('342', 'night', '2026-05-23 22:50:23.588000', '✓ 已提交【其他】\n\n提交者：兔兔\n备注：我要献祭自己，谢谢\n\n等待主持人在夜晚阶段结算。', '2', '天灾使者', '8', '兔兔', '其他');
INSERT INTO `game_activity_log` VALUES ('343', 'night', '2026-05-23 23:08:35.640000', '✓ 已提交【其他】\n\n提交者：追枫\n备注：突袭监狱！\n\n等待主持人在夜晚阶段结算。', '2', '天灾使者', '20', '追枫', '其他');
INSERT INTO `game_activity_log` VALUES ('344', 'trade', '2026-05-23 23:23:44.668000', '对方:乐语 | give 刺刀×1', '2', '冒险者', '14', 'Κάκτος西里尔', '发起交易→乐语 #72');
INSERT INTO `game_activity_log` VALUES ('345', 'trade', '2026-05-23 23:24:27.066000', '对方:Κάκτος西里尔', '2', '冒险者', '21', '乐语', '接受交易→Κάκτος西里尔 #72');
INSERT INTO `game_activity_log` VALUES ('346', 'trade', '2026-05-23 23:24:58.507000', '对方:11 | give 刺刀×1', '2', '冒险者', '9', '对酒', '发起交易→11 #73');
INSERT INTO `game_activity_log` VALUES ('347', 'trade', '2026-05-23 23:25:16.842000', '对方:对酒', '2', '冒险者', '22', '11', '接受交易→对酒 #73');
INSERT INTO `game_activity_log` VALUES ('348', 'night', '2026-05-23 23:34:21.429000', '✓ 已提交【进行密谋】\n\n提交者：对酒\n密谋类型：袭击地点\n目标地点：教堂\n参与玩家：Κάκτος西里尔、乐语、11、unPy-GPT\n成功后意向：搜刮资源\n备注：我们经商议后全体出击（消息来源：采珠人11），在船长的带领下前往教堂支援女巫，船长：猎枪：11：刺刀  gpt：斧子  对酒：斧子  乐语：刺刀\n\n等待主持人在夜晚阶段结算。', '2', '冒险者', '9', '对酒', '进行密谋');
INSERT INTO `game_activity_log` VALUES ('349', 'night', '2026-05-23 23:36:38.430000', '✓ 已提交【进行密谋】\n\n提交者：Κάκτος西里尔\n密谋类型：袭击地点\n目标地点：教堂\n参与玩家：11、乐语、对酒、unPy-GPT\n成功后意向：搜刮资源\n备注：我方经商议后全体出击，由船长携带霰弹枪及对应子弹、采珠人GPT携带斧头、采珠人11携带刺刀、气象观测员携带斧头、邮差携带刺刀，共5人，前往教堂解救巫师（消息来源：采珠人11）。\n\n等待主持人在夜晚阶段结算。', '2', '冒险者', '14', 'Κάκτος西里尔', '进行密谋');
INSERT INTO `game_activity_log` VALUES ('350', 'trade', '2026-05-23 23:40:07.974000', '对方:乐语 | give 未知物品×20', '2', '冒险者', '14', 'Κάκτος西里尔', '发起交易→乐语 #74');
INSERT INTO `game_activity_log` VALUES ('351', 'faction', '2026-05-23 23:40:47.319000', '✓ 已提交【额外调查】\n\n调查类型：调查玩家\n调查目标：MISD330\n\n结算后该次调查数量将翻倍。等待主持人确认。', '2', '冒险者', '14', 'Κάκτος西里尔', '额外调查');
INSERT INTO `game_activity_log` VALUES ('352', 'trade', '2026-05-23 23:40:59.879000', '对方:Κάκτος西里尔', '2', '冒险者', '21', '乐语', '接受交易→Κάκτος西里尔 #74');
INSERT INTO `game_activity_log` VALUES ('353', 'trade', '2026-05-23 23:42:36.821000', '对方:乐语 | give 未知物品×1 | give 未知物品×1', '2', '冒险者', '19', 'unPy-GPT', '发起交易→乐语 #75');
INSERT INTO `game_activity_log` VALUES ('354', 'faction', '2026-05-23 23:44:53.330000', '✓ 已提交【方舟建设】\n\n提交者：乐语\n投入模式：资源投入\n  木材：30000kg（30.00吨）\n  金属制品：10500kg（10.50吨）\n  密封材料：20kg\n当前方舟进度：66.00%\n备注：发动阵营特性号召和个人特性渴望出海利用免费行动点进行双倍于物资投入的方舟建造\n\n等待主持人确认。', '2', '冒险者', '21', '乐语', '方舟建设');
INSERT INTO `game_activity_log` VALUES ('355', 'night', '2026-05-24 00:30:19.337000', '✓ 已提交【进行密谋】\n\n提交者：孤城暮角\n密谋类型：制造恐怖\n目标地点：码头\n参与玩家：兔兔\n\n等待主持人在夜晚阶段结算。', '2', '天灾使者', '16', '孤城暮角', '进行密谋');
INSERT INTO `game_activity_log` VALUES ('356', 'night', '2026-05-24 00:36:41.237000', '✓ 已提交【其他】\n\n提交者：闲屿\n备注：不抗命，留守矿口\n\n等待主持人在夜晚阶段结算。', '2', '平民', '31', '闲屿', '其他');
INSERT INTO `game_activity_log` VALUES ('357', 'quick', '2026-05-24 00:40:57.541000', '在守矿口的时候顺便带些煤矿走', '2', '平民', '31', '闲屿', '快速行动');
INSERT INTO `game_activity_log` VALUES ('358', 'consume', '2026-05-24 00:54:26.461000', '累计进食 2/2；取暖 15/15 热值', '2', '平民', '32', '澡堂子', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('359', 'quick', '2026-05-24 00:56:28.980000', '喝酒消除疲劳', '2', '平民', '32', '澡堂子', '快速行动');
INSERT INTO `game_activity_log` VALUES ('360', 'trade', '2026-05-24 01:03:21.909000', '对方:unPy-GPT', '2', '冒险者', '21', '乐语', '接受交易→unPy-GPT #75');
INSERT INTO `game_activity_log` VALUES ('361', 'night', '2026-05-24 12:00:36.324000', '✓ 已提交【其他】\n\n提交者：乐语\n备注：去气象台探查一番，看是否有有用的发现\n\n等待主持人在夜晚阶段结算。', '2', '冒险者', '21', '乐语', '其他');
INSERT INTO `game_activity_log` VALUES ('362', 'trade', '2026-05-24 12:33:51.866000', '对方:追枫 | give 未知物品×2 | take 食物×1', '2', '统治者', '10', '二阶堂希罗', '发起交易→追枫 #76');
INSERT INTO `game_activity_log` VALUES ('363', 'quick', '2026-05-24 13:09:49.984000', '使用酒消除过劳', '2', '天灾使者', '16', '孤城暮角', '快速行动');
INSERT INTO `game_activity_log` VALUES ('364', 'trade', '2026-05-24 13:39:28.277000', '对方:追枫 | give 未知物品×1 | take 未知物品×1', '2', '统治者', '10', '二阶堂希罗', '发起交易→追枫 #77');
INSERT INTO `game_activity_log` VALUES ('365', 'trade', '2026-05-24 14:05:33.024000', '对方:Κάκτος西里尔 | give 未知物品×1 | give 未知物品×3 | take 食物×2', '2', '统治者', '10', '二阶堂希罗', '发起交易→Κάκτος西里尔 #78');
INSERT INTO `game_activity_log` VALUES ('366', 'trade', '2026-05-24 14:06:04.982000', '对方:Κάκτος西里尔 | give 未知物品×50 | take 食物×1', '2', '统治者', '10', '二阶堂希罗', '发起交易→Κάκτος西里尔 #79');
INSERT INTO `game_activity_log` VALUES ('367', 'trade', '2026-05-24 14:43:53.573000', '对方:追枫 | give 刺刀×1', '2', '天灾使者', '16', '孤城暮角', '发起交易→追枫 #80');
INSERT INTO `game_activity_log` VALUES ('368', 'trade', '2026-05-24 14:44:10.832000', '对方:MISD330 | give 刺刀×1', '2', '天灾使者', '16', '孤城暮角', '发起交易→MISD330 #81');
INSERT INTO `game_activity_log` VALUES ('369', 'trade', '2026-05-24 14:48:57.801000', '对方:孤城暮角', '2', '反叛者', '30', 'MISD330', '接受交易→孤城暮角 #81');
INSERT INTO `game_activity_log` VALUES ('370', 'trade', '2026-05-24 14:55:55.121000', '对方:二阶堂希罗', '2', '冒险者', '14', 'Κάκτος西里尔', '接受交易→二阶堂希罗 #79');
INSERT INTO `game_activity_log` VALUES ('371', 'trade', '2026-05-24 14:56:00.204000', '对方:二阶堂希罗', '2', '冒险者', '14', 'Κάκτος西里尔', '接受交易→二阶堂希罗 #78');
INSERT INTO `game_activity_log` VALUES ('372', 'trade', '2026-05-24 15:31:17.856000', '对方:孤城暮角', '2', '天灾使者', '20', '追枫', '接受交易→孤城暮角 #80');
INSERT INTO `game_activity_log` VALUES ('373', 'trade', '2026-05-24 15:31:25.222000', '对方:二阶堂希罗', '2', '天灾使者', '20', '追枫', '接受交易→二阶堂希罗 #77');
INSERT INTO `game_activity_log` VALUES ('374', 'trade', '2026-05-24 15:31:26.538000', '对方:二阶堂希罗', '2', '天灾使者', '20', '追枫', '接受交易→二阶堂希罗 #76');
INSERT INTO `game_activity_log` VALUES ('375', 'quick', '2026-05-24 16:33:44.504000', '医疗手工人和自己', '2', '反叛者', '30', 'MISD330', '快速行动');
INSERT INTO `game_activity_log` VALUES ('376', 'consume', '2026-05-24 17:15:09.236000', '累计进食 2/2；取暖 15/15 热值', '3', '天灾使者', '20', '追枫', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('377', 'consume', '2026-05-24 17:17:18.970000', '累计进食 2/2；取暖 15/15 热值', '3', '平民', '31', '闲屿', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('378', 'consume', '2026-05-24 17:19:45.083000', '累计进食 2/2；取暖 15/15 热值', '3', '冒险者', '21', '乐语', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('379', 'action', '2026-05-24 17:25:35.468000', '目标:伐木营地 NPC:托马斯·伍德 询问托马斯对天灾的看法以及所作的准备', '3', '平民', '31', '闲屿', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('380', 'action', '2026-05-24 17:25:35.678000', '目标:邮局 尝试看看这里有没有值得知道的信息', '3', '平民', '31', '闲屿', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('381', 'quick', '2026-05-24 17:26:38.942000', '吃下便当后，去教堂逛逛，询问是否有人知道昨晚来往的人员', '3', '平民', '31', '闲屿', '补充行动');
INSERT INTO `game_activity_log` VALUES ('382', 'quick', '2026-05-24 17:29:50.867000', '船长和我一起用海图进行了发报求援搬运，想确认是否有回报', '3', '冒险者', '21', '乐语', '补充行动');
INSERT INTO `game_activity_log` VALUES ('383', 'consume', '2026-05-24 17:43:18.043000', '累计进食 2/2；取暖 15/15 热值', '3', '天灾使者', '16', '孤城暮角', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('384', 'consume', '2026-05-24 17:58:08.690000', '累计进食 2/2；取暖 15/15 热值', '3', '冒险者', '9', '对酒', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('385', 'action', '2026-05-24 18:03:08.804000', '目标:墓地 伏击来到这里的所有人，在行动结束后抛开坟墓拿回前任猎手传承的枪和子弹', '3', '天灾使者', '20', '追枫', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('386', 'action', '2026-05-24 18:03:08.892000', '伏击在警察局到矿场的这条小路上，伏击路过的除了典狱长外的所有人', '3', '天灾使者', '20', '追枫', '自由#2·其他');
INSERT INTO `game_activity_log` VALUES ('387', 'consume', '2026-05-24 18:03:13.836000', '累计进食 2/2；取暖 15/15 热值', '3', '统治者', '10', '二阶堂希罗', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('388', 'faction', '2026-05-24 18:04:43.786000', '✓ 已提交【破坏】\n\n目标设施：发电机（镇长厅）\n\n等待主持人确认。', '3', '天灾使者', '20', '追枫', '破坏');
INSERT INTO `game_activity_log` VALUES ('389', 'trade', '2026-05-24 18:08:14.582000', '对方:孤城暮角 | give 未知物品×1', '3', '天灾使者', '20', '追枫', '发起交易→孤城暮角 #82');
INSERT INTO `game_activity_log` VALUES ('390', 'trade', '2026-05-24 18:08:32.596000', '对方:追枫', '3', '天灾使者', '16', '孤城暮角', '接受交易→追枫 #82');
INSERT INTO `game_activity_log` VALUES ('391', 'quick', '2026-05-24 18:09:20.973000', '使用便当做盾×1箭×6', '3', '天灾使者', '16', '孤城暮角', '快速行动');
INSERT INTO `game_activity_log` VALUES ('392', 'quick', '2026-05-24 18:10:05.872000', '1.使用一个面包 2.将冒险者仓库的剩余物品（武器）全部提取到玩家仓库', '3', '冒险者', '9', '对酒', '快速行动');
INSERT INTO `game_activity_log` VALUES ('393', 'quick', '2026-05-24 18:11:27.402000', '对V（镇长）散播瘟疫', '3', '天灾使者', '16', '孤城暮角', '快速行动');
INSERT INTO `game_activity_log` VALUES ('394', 'consume', '2026-05-24 18:16:30.907000', '累计进食 2/2；取暖 15/15 热值', '3', '统治者', '17', 'zzz', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('395', 'action', '2026-05-24 18:18:35.995000', '用拖拉机运木头到阵营仓库', '3', '冒险者', '9', '对酒', '自由#1·其他');
INSERT INTO `game_activity_log` VALUES ('396', 'action', '2026-05-24 18:18:36.084000', '用拖拉机运木头到阵营仓库', '3', '冒险者', '9', '对酒', '自由#2·其他');
INSERT INTO `game_activity_log` VALUES ('397', 'trade', '2026-05-24 18:19:47.479000', '对方:unPy-GPT | give 未知物品×1', '3', '冒险者', '9', '对酒', '发起交易→unPy-GPT #83');
INSERT INTO `game_activity_log` VALUES ('398', 'quick', '2026-05-24 18:20:47.309000', '用面包运送木头到阵营仓库', '3', '冒险者', '9', '对酒', '快速行动');
INSERT INTO `game_activity_log` VALUES ('399', 'quick', '2026-05-24 18:21:04.446000', '让伐木工去做两次手搓船', '3', '冒险者', '9', '对酒', '快速行动');
INSERT INTO `game_activity_log` VALUES ('400', 'quick', '2026-05-24 18:21:29.748000', '重新补充一下使用面包，然后用载具运送木头到阵营仓库', '3', '冒险者', '9', '对酒', '补充行动');
INSERT INTO `game_activity_log` VALUES ('401', 'trade', '2026-05-24 18:23:07.200000', '对方:对酒', '3', '冒险者', '19', 'unPy-GPT', '接受交易→对酒 #83');
INSERT INTO `game_activity_log` VALUES ('402', 'quick', '2026-05-24 18:25:40.882000', '致不明信号源： 我是本岛方舟船长。暴雪将至，我方即将启航。若您处确有补给，请提供物资清单、坐标与交接方式。请用数字确认您的经纬度。完毕。', '3', '冒险者', '14', 'Κάκτος西里尔', '快速行动');
INSERT INTO `game_activity_log` VALUES ('403', 'trade', '2026-05-24 18:28:16.441000', '对方:unPy-GPT | give 未知物品×50', '3', '冒险者', '14', 'Κάκτος西里尔', '发起交易→unPy-GPT #84');
INSERT INTO `game_activity_log` VALUES ('404', 'trade', '2026-05-24 18:30:38.926000', '对方:Κάκτος西里尔', '3', '冒险者', '19', 'unPy-GPT', '接受交易→Κάκτος西里尔 #84');
INSERT INTO `game_activity_log` VALUES ('405', 'quick', '2026-05-24 18:34:08.329000', '前往邮局对先前的不明信号源用电报机进行答复：致不明信号源： 我是本岛方舟船长西里尔。暴雪将至，我方即将启航。若您处确有补给，请提供物资清单、坐标与交接方式。请用数字确认您的经纬度。完毕。', '3', '冒险者', '14', 'Κάκτος西里尔', '快速行动');
INSERT INTO `game_activity_log` VALUES ('406', 'quick', '2026-05-24 18:39:24.368000', '重新总结一下：1使用一个面包，用载具运送木头到阵营仓库 2.让伐木工去搓两次船，如果可以三项材料同时推进就同时推进，如果只能推一项就推10kg沥青', '3', '冒险者', '9', '对酒', '补充行动');
INSERT INTO `game_activity_log` VALUES ('407', 'quick', '2026-05-24 18:40:44.132000', '我去矿场仓库搬运里面的帆布，放到身上', '3', '冒险者', '21', '乐语', '快速行动');
INSERT INTO `game_activity_log` VALUES ('408', 'action', '2026-05-24 18:46:43.655000', '目标:猎人小屋 携带霰弹枪和对应子弹两发，与镇长共同前往小屋旁的熊之仓库进行猎杀。', '3', '冒险者', '14', 'Κάκτος西里尔', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('409', 'action', '2026-05-24 18:48:14.649000', '目标:码头 携带霰弹枪及最大量霰弹保卫方舟', '3', '冒险者', '14', 'Κάκτος西里尔', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('410', 'consume', '2026-05-24 18:54:55.932000', '累计进食 2/2；取暖 15/15 热值', '3', '冒险者', '19', 'unPy-GPT', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('411', 'faction', '2026-05-24 18:57:32.601000', '✓ 已提交【看守方舟】\n\n看守人员：乐语\n使用武器/技能计入防御：是\n额外防御：+2\n\n等待主持人确认。', '3', '冒险者', '21', '乐语', '看守方舟');
INSERT INTO `game_activity_log` VALUES ('412', 'action', '2026-05-24 18:59:29.289000', '目标:澡堂子', '3', '统治者', '10', '二阶堂希罗', '自由#1·调查玩家');
INSERT INTO `game_activity_log` VALUES ('413', 'action', '2026-05-24 18:59:29.439000', '目标:墓地 去猎人墓穴寻找物品', '3', '统治者', '10', '二阶堂希罗', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('414', 'quick', '2026-05-24 19:02:52.312000', '修改一下行动，让伐木工去搓金属，搓10kg金属，不搓沥青了', '3', '冒险者', '9', '对酒', '补充行动');
INSERT INTO `game_activity_log` VALUES ('415', 'faction', '2026-05-24 19:03:00.353000', '✓ 已提交【安排看守】\n\n看守人员：追枫\n看守地点：矿场\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。', '3', '统治者', '10', '二阶堂希罗', '安排看守');
INSERT INTO `game_activity_log` VALUES ('416', 'action', '2026-05-24 19:03:11.639000', '目标:蟋蟀蜥蜴', '3', '统治者', '17', 'zzz', '自由#1·调查玩家');
INSERT INTO `game_activity_log` VALUES ('417', 'action', '2026-05-24 19:03:11.704000', '目标:荷叶男巫 一路走好', '3', '统治者', '17', 'zzz', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('418', 'quick', '2026-05-24 19:03:27.874000', '邀请装卸工与我们一同造船，白天两次行动都通过工作量推进金属物资进度', '3', '冒险者', '19', 'unPy-GPT', '快速行动');
INSERT INTO `game_activity_log` VALUES ('419', 'action', '2026-05-24 19:04:09.049000', null, '3', '平民', '18', 'Missbear', '自由#1·隐藏');
INSERT INTO `game_activity_log` VALUES ('420', 'action', '2026-05-24 19:04:09.150000', '目标:空白', '3', '平民', '18', 'Missbear', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('421', 'trade', '2026-05-24 19:07:04.651000', '对方:乐语 | give 未知物品×20', '3', '冒险者', '19', 'unPy-GPT', '发起交易→乐语 #85');
INSERT INTO `game_activity_log` VALUES ('422', 'faction', '2026-05-24 19:08:21.359000', '✓ 已提交【安排人员】\n\n目标：乔克·汤姆\n须提交的自由行动（共1项）：\n  1. 搬运\n附加说明：从仓库搬500kg木材\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。', '3', '统治者', '26', 'V', '安排人员');
INSERT INTO `game_activity_log` VALUES ('423', 'faction', '2026-05-24 19:09:11.543000', '✓ 已提交【安排人员】\n\n目标：克拉拉·南丁格尔\n须提交的自由行动（共1项）：\n  1. 搬运\n附加说明：从仓库搬500kg木材去避难所\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。', '3', '统治者', '26', 'V', '安排人员');
INSERT INTO `game_activity_log` VALUES ('424', 'trade', '2026-05-24 19:09:45.796000', '对方:unPy-GPT | give 未知物品×3 | give 未知物品×10 | give 未知物品×40', '3', '冒险者', '14', 'Κάκτος西里尔', '发起交易→unPy-GPT #86');
INSERT INTO `game_activity_log` VALUES ('425', 'trade', '2026-05-24 19:10:00.092000', '对方:Κάκτος西里尔', '3', '冒险者', '19', 'unPy-GPT', '接受交易→Κάκτος西里尔 #86');
INSERT INTO `game_activity_log` VALUES ('426', 'quick', '2026-05-24 19:12:01.527000', '让手工艺人搓木头', '3', '冒险者', '14', 'Κάκτος西里尔', '快速行动');
INSERT INTO `game_activity_log` VALUES ('427', 'trade', '2026-05-24 19:12:06.583000', '对方:unPy-GPT', '3', '冒险者', '21', '乐语', '接受交易→unPy-GPT #85');
INSERT INTO `game_activity_log` VALUES ('428', 'quick', '2026-05-24 19:13:00.762000', '修改行动，伐木工去搓木头造船 搓两次木头', '3', '冒险者', '9', '对酒', '补充行动');
INSERT INTO `game_activity_log` VALUES ('429', 'faction', '2026-05-24 19:13:32.058000', '✓ 已提交【安排人员】\n\n目标：汉斯·施密特\n须提交的自由行动（共1项）：\n  1. 搬运\n附加说明：从仓库搬500kg木材去避难所\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。', '3', '统治者', '26', 'V', '安排人员');
INSERT INTO `game_activity_log` VALUES ('430', 'trade', '2026-05-24 19:14:26.476000', '对方:Κάκτος西里尔 | give 食物×2', '3', '冒险者', '9', '对酒', '发起交易→Κάκτος西里尔 #87');
INSERT INTO `game_activity_log` VALUES ('431', 'trade', '2026-05-24 19:14:54.663000', '对方:对酒', '3', '冒险者', '14', 'Κάκτος西里尔', '接受交易→对酒 #87');
INSERT INTO `game_activity_log` VALUES ('432', 'faction', '2026-05-24 19:15:12.320000', '✓ 已提交【安排人员】\n\n目标：托马斯·伍德\n须提交的自由行动（共2项）：\n  1. 搬运\n  2. 搬运\n附加说明：从仓库搬500kg木材去避难所\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。', '3', '统治者', '26', 'V', '安排人员');
INSERT INTO `game_activity_log` VALUES ('433', 'trade', '2026-05-24 19:15:55.672000', '对方:追枫 | give 未知物品×1', '3', '天灾使者', '16', '孤城暮角', '发起交易→追枫 #88');
INSERT INTO `game_activity_log` VALUES ('434', 'trade', '2026-05-24 19:16:44.922000', '对方:孤城暮角', '3', '天灾使者', '20', '追枫', '接受交易→孤城暮角 #88');
INSERT INTO `game_activity_log` VALUES ('435', 'quick', '2026-05-24 19:17:21.238000', '使用便当蹲守熊屋边上林子中，如果有人前来发起暗杀', '3', '天灾使者', '20', '追枫', '快速行动');
INSERT INTO `game_activity_log` VALUES ('436', 'action', '2026-05-24 19:19:54.281000', '使用10t木头，10t金属，20kg沥青，一个发动机，还有届时从矿场仓库取回的身上所有的帆布进行方舟建设。', '3', '冒险者', '21', '乐语', '自由#1·其他');
INSERT INTO `game_activity_log` VALUES ('437', 'action', '2026-05-24 19:19:54.382000', '目标:凭栏择雨 使用潜行悄悄调查该玩家今天的所有行动，收集所有可能的情报', '3', '冒险者', '21', '乐语', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('438', 'quick', '2026-05-24 19:21:26.930000', '吃掉便当，用工作量推进方舟建设木头5t', '3', '冒险者', '21', '乐语', '快速行动');
INSERT INTO `game_activity_log` VALUES ('439', 'trade', '2026-05-24 19:21:42.117000', '对方:对酒 | give 未知物品×10 | give 未知物品×60', '3', '冒险者', '19', 'unPy-GPT', '发起交易→对酒 #89');
INSERT INTO `game_activity_log` VALUES ('440', 'consume', '2026-05-24 19:22:30.830000', '累计进食 2/2；取暖 15/15 热值', '3', '统治者', '26', 'V', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('441', 'trade', '2026-05-24 19:23:58.772000', '对方:unPy-GPT', '3', '冒险者', '9', '对酒', '接受交易→unPy-GPT #89');
INSERT INTO `game_activity_log` VALUES ('442', 'consume', '2026-05-24 19:24:14.716000', '累计进食 2/2；取暖 15/15 热值', '3', '冒险者', '14', 'Κάκτος西里尔', '进食+2 木0kg 燃1kg');
INSERT INTO `game_activity_log` VALUES ('443', 'action', '2026-05-24 19:26:55.183000', '通过工作量建设的方式推进方舟建设中木头进度5t', '3', '冒险者', '19', 'unPy-GPT', '自由#1·其他');
INSERT INTO `game_activity_log` VALUES ('444', 'action', '2026-05-24 19:26:55.247000', '通过工作量建设的方式推进方舟建设中木头进度5t', '3', '冒险者', '19', 'unPy-GPT', '自由#2·其他');
INSERT INTO `game_activity_log` VALUES ('445', 'night', '2026-05-24 19:28:55.211000', '✓ 已提交【夜晚个人行动】\n\n提交者：zzz\n行动：前往地点\n目标：矿场\n备注：前往避难所\n\n等待主持人在夜晚阶段结算。', '3', '统治者', '17', 'zzz', '夜晚个人行动');
INSERT INTO `game_activity_log` VALUES ('446', 'action', '2026-05-24 19:33:02.249000', '问候球球，没了，为什么不让我提交', '3', '统治者', '26', 'V', '自由#1·其他');
INSERT INTO `game_activity_log` VALUES ('447', 'action', '2026-05-24 19:33:02.638000', '目标:码头 NPC:鲍勃·塔克 尝试交涉，拉拢npc', '3', '统治者', '26', 'V', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('448', 'quick', '2026-05-24 19:33:14.411000', '吃了面包后感觉我浑身充满力量，将对酒拉到码头的40t木头，和个人仓库中的30kg沥青，以及所需螺旋桨全部投入到方舟建设中', '3', '冒险者', '19', 'unPy-GPT', '快速行动');
INSERT INTO `game_activity_log` VALUES ('449', 'consume', '2026-05-24 19:33:50.802000', '累计进食 2/2；取暖 15/15 热值', '3', '反叛者', '24', '花海', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('450', 'action', '2026-05-24 19:35:00.503000', '生产15个食物', '3', '反叛者', '24', '花海', '自由#1·使用职业技能');
INSERT INTO `game_activity_log` VALUES ('451', 'action', '2026-05-24 19:35:00.622000', '百宝袋/挑选列表的5个物品复制', '3', '反叛者', '24', '花海', '自由#2·使用特性');
INSERT INTO `game_activity_log` VALUES ('452', 'faction', '2026-05-24 19:35:49.071000', '✓ 已提交【安排人员】\n\n目标：乔克·汤姆\n须提交的自由行动（共2项）：\n  1. 搬运\n  2. 调查玩家 → 闲屿\n附加说明：搬运500kg木材至避难所仓库\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。', '3', '统治者', '26', 'V', '安排人员');
INSERT INTO `game_activity_log` VALUES ('453', 'faction', '2026-05-24 19:36:58.838000', '✓ 已提交【额外行动】\n\n行动类型：使用职业技能\n备注：急救并医疗自己\n\n此环节无法寻找NPC进行对话。\n等待主持人确认。', '3', '反叛者', '30', 'MISD330', '额外行动');
INSERT INTO `game_activity_log` VALUES ('454', 'consume', '2026-05-24 19:37:24.075000', '累计进食 2/2；取暖 15/15 热值', '3', '统治者', '15', '空白', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('455', 'quick', '2026-05-24 19:38:27.071000', '对不起，最后一次修改行动了，让伐木工用电锯伐两次木头，然后把木头转给我和乐语各20t', '3', '冒险者', '9', '对酒', '补充行动');
INSERT INTO `game_activity_log` VALUES ('456', 'night', '2026-05-24 19:40:57.344000', '✓ 已提交【夜晚个人行动】\n\n提交者：二阶堂希罗\n行动：前往地点\n目标：矿场\n交互NPC：维克多·斯通\n备注：夜间直接镇守矿厂与避难所\n\n等待主持人在夜晚阶段结算。', '3', '统治者', '10', '二阶堂希罗', '夜晚个人行动');
INSERT INTO `game_activity_log` VALUES ('457', 'faction', '2026-05-24 19:41:04.276000', '✓ 已提交【安排人员】\n\n目标：托马斯·伍德\n须提交的自由行动（共2项）：\n  1. 搬运\n  2. 前往地点 → 矿场\n附加说明：搬运500kg木材至避难所仓库\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。', '3', '统治者', '26', 'V', '安排人员');
INSERT INTO `game_activity_log` VALUES ('458', 'quick', '2026-05-24 19:41:15.982000', '取消使用便当', '3', '天灾使者', '16', '孤城暮角', '补充行动');
INSERT INTO `game_activity_log` VALUES ('459', 'action', '2026-05-24 19:43:34.424000', '跟踪农民然后用背包的猎枪枪毙她 如果没射击技能则用维修工具肉搏', '3', '统治者', '15', '空白', '自由#1·其他');
INSERT INTO `game_activity_log` VALUES ('460', 'action', '2026-05-24 19:43:34.550000', '去码头草坪抓蛐蛐然后偷摸塞镇长帽子里吓唬他 如果码头有很多蛐蛐就打包回去给监狱长和警长加餐', '3', '统治者', '15', '空白', '自由#2·其他');
INSERT INTO `game_activity_log` VALUES ('461', 'faction', '2026-05-24 19:45:19.690000', '✓ 已提交【安排人员】\n\n目标：汉斯·施密特\n须提交的自由行动（共2项）：\n  1. 搬运\n  2. 其他\n附加说明：搬运500kg木材至避难所仓库，希望NPC夜间前往避难所帮助统治者\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。', '3', '统治者', '26', 'V', '安排人员');
INSERT INTO `game_activity_log` VALUES ('462', 'faction', '2026-05-24 19:46:17.185000', '✓ 已提交【安排人员】\n\n目标：杰克·塔克\n须提交的自由行动（共2项）：\n  1. 搬运\n  2. 其他\n附加说明：搬运500kg木材至避难所仓库，希望NPC夜间前往避难所帮助统治者\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。', '3', '统治者', '26', 'V', '安排人员');
INSERT INTO `game_activity_log` VALUES ('463', 'faction', '2026-05-24 19:46:51.527000', '✓ 已提交【安排人员】\n\n目标：克拉拉·南丁格尔\n须提交的自由行动（共2项）：\n  1. 搬运\n  2. 其他\n附加说明：搬运500kg石材至避难所仓库，希望NPC夜间前往避难所帮助统治者\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。', '3', '统治者', '26', 'V', '安排人员');
INSERT INTO `game_activity_log` VALUES ('464', 'faction', '2026-05-24 19:47:24.498000', '✓ 已提交【安排人员】\n\n目标：千代\n须提交的自由行动（共1项）：\n  1. 搬运\n附加说明：搬运500kg石材至避难所仓库\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。', '3', '统治者', '26', 'V', '安排人员');
INSERT INTO `game_activity_log` VALUES ('465', 'quick', '2026-05-24 19:47:32.665000', '白天的个人行动建造中补充提交20t木头，从阵营仓库中提取', '3', '冒险者', '21', '乐语', '补充行动');
INSERT INTO `game_activity_log` VALUES ('466', 'faction', '2026-05-24 19:48:16.524000', '✓ 已提交【安排人员】\n\n目标：孤城暮角\n须提交的自由行动（共1项）：\n  1. 搬运\n附加说明：搬运500kg石材至避难所仓库\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。', '3', '统治者', '26', 'V', '安排人员');
INSERT INTO `game_activity_log` VALUES ('467', 'faction', '2026-05-24 19:49:28.906000', '✓ 已提交【安排人员】\n\n目标：闲屿\n须提交的自由行动（共1项）：\n  1. 搬运\n附加说明：搬运500kg石材至避难所仓库\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。', '3', '统治者', '26', 'V', '安排人员');
INSERT INTO `game_activity_log` VALUES ('468', 'faction', '2026-05-24 19:50:38.752000', '✓ 已提交【安排人员】\n\n目标：Κάκτος西里尔\n须提交的自由行动（共1项）：\n  1. 搬运\n附加说明：搬运500kg石材至避难所仓库\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。', '3', '统治者', '26', 'V', '安排人员');
INSERT INTO `game_activity_log` VALUES ('469', 'faction', '2026-05-24 19:50:56.737000', '✓ 已提交【安排人员】\n\n目标：乐语\n须提交的自由行动（共1项）：\n  1. 搬运\n附加说明：搬运500kg石材至避难所仓库\n\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。', '3', '统治者', '26', 'V', '安排人员');
INSERT INTO `game_activity_log` VALUES ('470', 'quick', '2026-05-24 20:00:46.372000', '使用便当，调查巡夜人missbear', '3', '统治者', '10', '二阶堂希罗', '快速行动');
INSERT INTO `game_activity_log` VALUES ('471', 'trade', '2026-05-24 20:01:51.620000', '对方:V | give 未知物品×1 | take 食物×1', '3', '统治者', '10', '二阶堂希罗', '发起交易→V #90');
INSERT INTO `game_activity_log` VALUES ('472', 'trade', '2026-05-24 20:15:11.883000', '对方:zzz | give 未知物品×1 | take 食物×1', '3', '统治者', '10', '二阶堂希罗', '发起交易→zzz #91');
INSERT INTO `game_activity_log` VALUES ('473', 'trade', '2026-05-24 20:16:38.946000', '对方:二阶堂希罗', '3', '统治者', '17', 'zzz', '接受交易→二阶堂希罗 #91');
INSERT INTO `game_activity_log` VALUES ('474', 'quick', '2026-05-24 20:17:33.653000', '吃便当 第三个行动从矿坑搬300木头到避难所', '3', '统治者', '17', 'zzz', '补充行动');
INSERT INTO `game_activity_log` VALUES ('475', 'faction', '2026-05-24 20:28:27.577000', '✓ 已提交【破坏】\n\n目标设施：发电机（警察局）\n\n等待主持人确认。', '3', '天灾使者', '16', '孤城暮角', '破坏');
INSERT INTO `game_activity_log` VALUES ('476', 'faction', '2026-05-24 20:33:42.836000', '✓ 已提交【安排看守】\n\n看守人员：孤城暮角\n看守地点：矿场\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。', '3', '统治者', '10', '二阶堂希罗', '安排看守');
INSERT INTO `game_activity_log` VALUES ('477', 'trade', '2026-05-24 20:33:57.474000', '对方:教皇 | give 食物×2', '3', '反叛者', '24', '花海', '发起交易→教皇 #92');
INSERT INTO `game_activity_log` VALUES ('478', 'faction', '2026-05-24 20:33:58.848000', '✓ 已提交【安排看守】\n\n看守人员：飞凡\n看守地点：矿场\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。', '3', '统治者', '10', '二阶堂希罗', '安排看守');
INSERT INTO `game_activity_log` VALUES ('479', 'action', '2026-05-24 20:34:01.131000', '盾盾盾造三个盾', '3', '天灾使者', '16', '孤城暮角', '自由#1·使用职业技能');
INSERT INTO `game_activity_log` VALUES ('480', 'action', '2026-05-24 20:34:01.229000', '盾盾盾造三个盾', '3', '天灾使者', '16', '孤城暮角', '自由#2·使用职业技能');
INSERT INTO `game_activity_log` VALUES ('481', 'faction', '2026-05-24 20:34:08.258000', '✓ 已提交【安排看守】\n\n看守人员：Missbear\n看守地点：矿场\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。', '3', '统治者', '10', '二阶堂希罗', '安排看守');
INSERT INTO `game_activity_log` VALUES ('482', 'faction', '2026-05-24 20:34:20.714000', '✓ 已提交【安排看守】\n\n看守人员：千代\n看守地点：矿场\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。', '3', '统治者', '10', '二阶堂希罗', '安排看守');
INSERT INTO `game_activity_log` VALUES ('483', 'faction', '2026-05-24 20:34:40.385000', '✓ 已提交【安排看守】\n\n看守人员：澡堂子\n看守地点：矿场\n消耗对方夜晚行动点：是\n基础防御：+3\n\n等待主持人确认。', '3', '统治者', '10', '二阶堂希罗', '安排看守');
INSERT INTO `game_activity_log` VALUES ('484', 'trade', '2026-05-24 20:35:15.709000', '对方:追枫 | give 手电筒×1 | give 未知物品×1 | give 未知物品×1 | give 燃料×11 | give 食物×31 | give 未知物品×10 | give 木材×12', '3', '天灾使者', '16', '孤城暮角', '发起交易→追枫 #93');
INSERT INTO `game_activity_log` VALUES ('485', 'trade', '2026-05-24 20:37:46.022000', '对方:蟋蟀蜥蜴 | give 食物×2', '3', '反叛者', '24', '花海', '发起交易→蟋蟀蜥蜴 #94');
INSERT INTO `game_activity_log` VALUES ('486', 'trade', '2026-05-24 20:42:55.323000', '对方:花海', '3', '反叛者', '11', '蟋蟀蜥蜴', '接受交易→花海 #94');
INSERT INTO `game_activity_log` VALUES ('487', 'action', '2026-05-24 20:43:15.119000', '目标:猎人小屋 携带霰弹枪和2发子弹，与镇长前往熊仓库并杀熊', '3', '冒险者', '14', 'Κάκτος西里尔', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('488', 'action', '2026-05-24 20:43:15.221000', '目标:矿场', '3', '冒险者', '14', 'Κάκτος西里尔', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('489', 'trade', '2026-05-24 20:43:17.478000', '对方:花海', '3', '反叛者', '23', '教皇', '接受交易→花海 #92');
INSERT INTO `game_activity_log` VALUES ('490', 'consume', '2026-05-24 20:43:25.495000', '累计进食 2/2；取暖 15/15 热值', '3', '反叛者', '11', '蟋蟀蜥蜴', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('491', 'action', '2026-05-24 20:46:19.661000', '作为治安官 非常受大家的尊敬 于是悄悄偷懒 过劳状态解除', '3', '反叛者', '11', '蟋蟀蜥蜴', '自由#1·使用特性');
INSERT INTO `game_activity_log` VALUES ('492', 'faction', '2026-05-24 20:51:19.419000', '✓ 已提交【额外行动】\n\n行动类型：使用特性\n备注：百宝袋/我要将斧头、防弹衣、医疗包、金属用品、食物全部复制\n\n此环节无法寻找NPC进行对话。\n等待主持人确认。', '3', '反叛者', '24', '花海', '额外行动');
INSERT INTO `game_activity_log` VALUES ('493', 'trade', '2026-05-24 20:59:17.965000', '对方:V | give 医疗包×2 | give 手电筒×10 | give 未知物品×4 | give 未知物品×1 | give 未知物品×2 | give 未知物品×20 | give 未知物品×3 | give 未知物品×1 | give 未知物品×20 | give 未知物品×5 | give 未知物品×2 | give 未知物品×1 | give 制式手枪×2 | give 猎枪×1 | give 警棍×4 | give 刺刀×2 | give 未知物品×1 | give 未知物品×1 | give 手枪弹×4 | give 猎枪弹×2 | give 未知物品×2 | give 未知物品×2 | give 燃料×193 | give 食物×3 | give 未知物品×1', '3', '统治者', '10', '二阶堂希罗', '发起交易→V #95');
INSERT INTO `game_activity_log` VALUES ('494', 'trade', '2026-05-24 20:59:52.127000', '对方:花海 | give 警棍×1', '3', '反叛者', '11', '蟋蟀蜥蜴', '发起交易→花海 #96');
INSERT INTO `game_activity_log` VALUES ('495', 'trade', '2026-05-24 21:00:14.789000', '对方:二阶堂希罗', '3', '统治者', '26', 'V', '接受交易→二阶堂希罗 #95');
INSERT INTO `game_activity_log` VALUES ('496', 'trade', '2026-05-24 21:00:20.596000', '对方:蟋蟀蜥蜴', '3', '反叛者', '24', '花海', '接受交易→蟋蟀蜥蜴 #96');
INSERT INTO `game_activity_log` VALUES ('497', 'trade', '2026-05-24 21:09:11.013000', '对方:对酒 | give 未知物品×50', '3', '冒险者', '21', '乐语', '发起交易→对酒 #97');
INSERT INTO `game_activity_log` VALUES ('498', 'trade', '2026-05-24 21:09:41.384000', '对方:zzz | give 猎枪×1 | give 猎枪弹×2', '3', '统治者', '15', '空白', '发起交易→zzz #98');
INSERT INTO `game_activity_log` VALUES ('499', 'trade', '2026-05-24 21:13:52.419000', '对方:二阶堂希罗 | give 未知物品×10', '3', '统治者', '15', '空白', '发起交易→二阶堂希罗 #99');
INSERT INTO `game_activity_log` VALUES ('500', 'trade', '2026-05-24 21:15:26.818000', '对方:空白', '3', '统治者', '10', '二阶堂希罗', '接受交易→空白 #99');
INSERT INTO `game_activity_log` VALUES ('501', 'trade', '2026-05-24 21:15:49.203000', '对方:乐语', '3', '冒险者', '9', '对酒', '接受交易→乐语 #97');
INSERT INTO `game_activity_log` VALUES ('502', 'quick', '2026-05-24 21:19:27.721000', '预埋炸药，引线从入口处沿着边缘往里布置，拿着引线的蹲守在入口直接可以看到来路的位置，拿着引线', '1', '统治者', '10', '二阶堂希罗', '快速行动');
INSERT INTO `game_activity_log` VALUES ('503', 'trade', '2026-05-24 21:22:30.117000', '对方:二阶堂希罗 | give 未知物品×1', '3', '统治者', '26', 'V', '发起交易→二阶堂希罗 #100');
INSERT INTO `game_activity_log` VALUES ('504', 'trade', '2026-05-24 21:22:37.626000', '对方:V', '3', '统治者', '10', '二阶堂希罗', '接受交易→V #100');
INSERT INTO `game_activity_log` VALUES ('505', 'trade', '2026-05-24 21:22:44.307000', '对方:孤城暮角', '3', '天灾使者', '20', '追枫', '接受交易→孤城暮角 #93');
INSERT INTO `game_activity_log` VALUES ('506', 'night', '2026-05-24 21:28:00.714000', '✓ 已提交【其他】\n\n提交者：飞凡\n备注：带着我的50瓶朗姆去庇护所门口睡觉\n\n等待主持人在夜晚阶段结算。', '3', '平民', '29', '飞凡', '其他');
INSERT INTO `game_activity_log` VALUES ('507', 'trade', '2026-05-24 21:49:19.195000', '对方:空白', '3', '统治者', '17', 'zzz', '接受交易→空白 #98');
INSERT INTO `game_activity_log` VALUES ('508', 'trade', '2026-05-24 22:14:47.545000', '对方:对酒 | give 食物×10', '3', '反叛者', '24', '花海', '发起交易→对酒 #101');
INSERT INTO `game_activity_log` VALUES ('509', 'trade', '2026-05-24 22:15:34.909000', '对方:花海 | give 未知物品×1 | give 未知物品×1 | take 食物×10', '3', '冒险者', '9', '对酒', '发起交易→花海 #102');
INSERT INTO `game_activity_log` VALUES ('510', 'trade', '2026-05-24 22:16:00.872000', '对方:花海', '3', '冒险者', '9', '对酒', '接受交易→花海 #101');
INSERT INTO `game_activity_log` VALUES ('511', 'trade', '2026-05-24 22:19:55.028000', '对方:对酒', '3', '反叛者', '24', '花海', '接受交易→对酒 #102');
INSERT INTO `game_activity_log` VALUES ('512', 'quick', '2026-05-24 22:21:32.499000', '拿一个警棍', '3', '反叛者', '24', '花海', '快速行动');
INSERT INTO `game_activity_log` VALUES ('513', 'trade', '2026-05-24 22:26:59.461000', '对方:zzz | give 未知物品×1', '3', '统治者', '26', 'V', '发起交易→zzz #103');
INSERT INTO `game_activity_log` VALUES ('514', 'trade', '2026-05-24 22:27:05.241000', '对方:V | take 未知物品×1', '3', '统治者', '17', 'zzz', '发起交易→V #104');
INSERT INTO `game_activity_log` VALUES ('515', 'trade', '2026-05-24 22:27:11.305000', '对方:V', '3', '统治者', '17', 'zzz', '接受交易→V #103');
INSERT INTO `game_activity_log` VALUES ('516', 'consume', '2026-05-24 22:30:39.559000', '累计进食 2/2；取暖 15/15 热值', '3', '反叛者', '23', '教皇', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('517', 'quick', '2026-05-24 22:30:51.648000', '从仓库拿出枪和子弹 上膛', '3', '反叛者', '11', '蟋蟀蜥蜴', '快速行动');
INSERT INTO `game_activity_log` VALUES ('518', 'consume', '2026-05-24 22:33:36.900000', '累计进食 2/2；取暖 15/15 热值', '3', '反叛者', '13', '凭栏择雨', '进食+2 木15kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('519', 'night', '2026-05-24 23:07:04.005000', '✓ 已提交【夜晚个人行动】\n\n提交者：空白\n行动：使用特性\n备注：使用窃听者特性 最后一天咯看看天灾阵营的聊天记录吧(∩_∩)\n\n等待主持人在夜晚阶段结算。', '3', '统治者', '15', '空白', '夜晚个人行动');
INSERT INTO `game_activity_log` VALUES ('520', 'consume', '2026-06-22 12:29:23.259000', '累计进食 1/2；取暖 0/15 热值', '1', '平民', '34', 'player', '进食+1 木0kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('521', 'consume', '2026-06-22 12:29:27.247000', '累计进食 2/2；取暖 0/15 热值', '1', '平民', '34', 'player', '进食+1 木0kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('522', 'consume', '2026-06-22 12:29:36.904000', '累计进食 2/2；取暖 6/15 热值', '1', '平民', '34', 'player', '进食+0 木6kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('523', 'consume', '2026-06-22 12:30:19.841000', '累计进食 2/2；取暖 15/15 热值', '1', '平民', '34', 'player', '进食+0 木9kg 燃0kg');
INSERT INTO `game_activity_log` VALUES ('524', 'action', '2026-06-22 12:31:13.491000', '目标:面包店 买面包', '1', '平民', '34', 'player', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('525', 'action', '2026-06-22 12:31:13.609000', '目标:追枫 看看他什么阵营', '1', '平民', '34', 'player', '自由#2·调查玩家');
INSERT INTO `game_activity_log` VALUES ('526', 'night', '2026-06-22 12:32:04.706000', '✓ 已提交【进行密谋】\n\n提交者：player\n密谋类型：袭击地点\n目标地点：码头\n参与玩家：兔兔\n成功后意向：搜刮资源\n备注：尝试杀他\n\n等待主持人在夜晚阶段结算。', '1', '平民', '34', 'player', '进行密谋');
INSERT INTO `game_activity_log` VALUES ('527', 'quick', '2026-06-22 12:32:29.996000', '111111', '1', '平民', '34', 'player', '补充行动');
INSERT INTO `game_activity_log` VALUES ('528', 'quick', '2026-06-22 12:36:54.071000', '前往墓地瞻仰西里尔船长。', '1', '平民', '34', 'player', '快速行动');
INSERT INTO `game_activity_log` VALUES ('529', 'quick', '2026-06-22 12:37:30.203000', '前往营地，拜访托马斯·伍德，并对其致以由衷的感谢。', '1', '平民', '34', 'player', '快速行动');
INSERT INTO `game_activity_log` VALUES ('530', 'quick', '2026-06-22 12:46:36.048000', '去面包店偷面包', '1', '平民', '34', 'player', '快速行动');
INSERT INTO `game_activity_log` VALUES ('531', 'quick', '2026-06-22 12:47:41.986000', '现在能做什么', '1', '平民', '34', 'player', '询问DM');
INSERT INTO `game_activity_log` VALUES ('532', 'quick', '2026-06-22 19:07:55.792000', '自杀，干死自己', '1', '平民', '34', 'player', '快速行动');
INSERT INTO `game_activity_log` VALUES ('533', 'action', '2026-06-23 23:42:12.339000', '目标:集市 NPC:塞缪尔·格雷', '2', '平民', '34', 'player', '自由#1·前往地点');
INSERT INTO `game_activity_log` VALUES ('534', 'action', '2026-06-23 23:42:12.665000', '目标:伐木营地 NPC:托马斯·伍德', '2', '平民', '34', 'player', '自由#2·前往地点');
INSERT INTO `game_activity_log` VALUES ('535', 'night', '2026-06-24 18:07:59.159000', '玩家提交了岛屿探索行动，投入探索值: 0', '2', '平民', '34', 'player', '探索岛屿');
INSERT INTO `game_activity_log` VALUES ('536', 'night', '2026-06-24 20:00:43.600000', '玩家提交了岛屿探索行动，投入探索值: 4', '100', '天灾使者', '8', '兔兔', '探索岛屿');
INSERT INTO `game_activity_log` VALUES ('537', 'night', '2026-06-24 20:03:48.536000', '玩家提交了岛屿探索行动，投入探索值: 4', '101', '天灾使者', '8', '兔兔', '探索岛屿');
INSERT INTO `game_activity_log` VALUES ('538', 'night', '2026-06-24 20:03:58.174000', '获得奖励: 4个 未知物品, 5个 未知物品', '101', '天灾使者', '8', '兔兔', '探索岛屿结算');
INSERT INTO `game_activity_log` VALUES ('539', 'night', '2026-06-24 20:10:02.402000', '玩家提交了岛屿探索行动，投入探索值: 5', '2', '统治者', '35', 'cs', '探索岛屿');
INSERT INTO `game_activity_log` VALUES ('540', 'night', '2026-06-24 20:19:50.415000', '玩家提交了岛屿探索行动，投入探索值: 10', '200', '天灾使者', '8', '兔兔', '探索岛屿');
INSERT INTO `game_activity_log` VALUES ('541', 'night', '2026-06-24 20:19:50.434000', '获得奖励: 4个 未知物品, 6份 食物, 1个 未知物品', '200', '天灾使者', '8', '兔兔', '探索岛屿结算');
INSERT INTO `game_activity_log` VALUES ('542', 'night', '2026-06-24 20:20:22.443000', '玩家提交了岛屿探索行动，投入探索值: 6', '201', '天灾使者', '8', '兔兔', '探索岛屿');
INSERT INTO `game_activity_log` VALUES ('543', 'night', '2026-06-24 20:20:22.466000', '获得奖励: 1个 医疗包, 1个 手电筒', '201', '天灾使者', '8', '兔兔', '探索岛屿结算');
INSERT INTO `game_activity_log` VALUES ('544', 'night', '2026-06-24 20:20:36.330000', '玩家提交了岛屿探索行动，投入探索值: 7', '202', '天灾使者', '8', '兔兔', '探索岛屿');
INSERT INTO `game_activity_log` VALUES ('545', 'night', '2026-06-24 20:20:36.343000', '获得奖励: 5份 食物, 2个 医疗包', '202', '天灾使者', '8', '兔兔', '探索岛屿结算');
INSERT INTO `game_activity_log` VALUES ('546', 'night', '2026-06-24 20:46:37.194000', '玩家提交了岛屿探索行动，投入探索值: 15', '1', '统治者', '35', 'cs', '探索岛屿');
INSERT INTO `game_activity_log` VALUES ('547', 'night', '2026-06-24 20:46:37.228000', '获得奖励: 2000kg 木材, 2kg 燃料, 3个 未知物品', '1', '统治者', '35', 'cs', '探索岛屿结算');

-- ----------------------------
-- Table structure for game_day_settings
-- ----------------------------
DROP TABLE IF EXISTS `game_day_settings`;
CREATE TABLE `game_day_settings` (
  `game_day` int(11) NOT NULL COMMENT '游戏天数',
  `required_food_units` int(11) NOT NULL DEFAULT '2' COMMENT '每人每日所需食物（单位）',
  `required_fuel_kg` int(11) NOT NULL DEFAULT '15' COMMENT '每人每日取暖燃料（千克，木材或燃料）',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`game_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日全局消耗需求（DM在游戏设置中配置）';

-- ----------------------------
-- Records of game_day_settings
-- ----------------------------
INSERT INTO `game_day_settings` VALUES ('1', '2', '15', '2026-05-14 11:53:12');
INSERT INTO `game_day_settings` VALUES ('2', '2', '15', '2026-05-20 10:23:13');
INSERT INTO `game_day_settings` VALUES ('3', '2', '15', '2026-05-20 14:14:41');

-- ----------------------------
-- Table structure for game_state
-- ----------------------------
DROP TABLE IF EXISTS `game_state`;
CREATE TABLE `game_state` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `current_day` int(11) NOT NULL DEFAULT '1' COMMENT '当前天数',
  `current_phase` varchar(20) NOT NULL DEFAULT 'DAY' COMMENT '当前阶段 DAY/NIGHT',
  `is_game_over` tinyint(1) DEFAULT '0' COMMENT '游戏是否结束',
  `catastrophe_triggered` tinyint(1) DEFAULT '0' COMMENT '天灾是否已触发',
  `extra_card_due` tinyint(1) DEFAULT '0' COMMENT '是否有待触发的额外天灾牌',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_single_state` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='游戏状态表';

-- ----------------------------
-- Records of game_state
-- ----------------------------
INSERT INTO `game_state` VALUES ('1', '1', 'DAY', '1', '0', '0', '2026-05-14 11:53:12', '2026-06-24 20:46:13');

-- ----------------------------
-- Table structure for island_event
-- ----------------------------
DROP TABLE IF EXISTS `island_event`;
CREATE TABLE `island_event` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text NOT NULL,
  `name` varchar(100) NOT NULL,
  `rarity` varchar(20) DEFAULT NULL,
  `triggered` bit(1) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `event_difficulty` int(11) NOT NULL DEFAULT '5' COMMENT '事件难度(0-20, 0=最简单的捡垃圾，20=极限危险)',
  `is_special` bit(1) NOT NULL,
  `location_desc` text,
  `lore_fragment` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of island_event
-- ----------------------------
INSERT INTO `island_event` VALUES ('17', '2026-06-24 18:03:53.653000', '地点描述：一座建在巨树高处的废弃木台，通往它的绳梯已经半朽。台上有一个被遗弃的巢穴，似乎被匆忙放弃。\n可获得物资：绳索 (10米)， 火把 (1把)\n历史秘密碎片：你在木台的柱子上发现了一连串古老的刻痕，像是某种计数方式。旁边画着一个歪歪扭扭的太阳，正在被雪花吞没。这似乎记录着某个先民对漫漫长夜的恐惧。\n难度：1', '废弃的猎人瞭望台', 'common', '', '2026-06-24 18:09:57.012000', '1', '\0', '一座建在巨树高处的废弃木台，通往它的绳梯已经半朽。台上有一个被遗弃的巢穴，似乎被匆忙放弃。', '你在木台的柱子上发现了一连串古老的刻痕，像是某种计数方式。旁边画着一个歪歪扭扭的太阳，正在被雪花吞没。这似乎记录着某个先民对漫漫长夜的恐惧。');
INSERT INTO `island_event` VALUES ('18', '2026-06-24 18:03:53.721000', '地点描述：森林深处，一个早已被触发、长满青苔的捕兽夹，旁边散落着一些锈蚀的金属零件。\n可获得物资：金属制品 (5kg)， 猎弓 (1把)\n历史秘密碎片：你清理陷阱旁的落叶时，挖出一个油布包裹的小包，里面是一块打磨过的黑色石头。拿在手里，总觉得这上面似乎浸透了某种陈旧的、不属于这个时代的气息。\n难度：2', '被遗忘的猎人陷阱', 'common', '\0', '2026-06-24 18:03:53.721000', '2', '\0', '森林深处，一个早已被触发、长满青苔的捕兽夹，旁边散落着一些锈蚀的金属零件。', '你清理陷阱旁的落叶时，挖出一个油布包裹的小包，里面是一块打磨过的黑色石头。拿在手里，总觉得这上面似乎浸透了某种陈旧的、不属于这个时代的气息。');
INSERT INTO `island_event` VALUES ('19', '2026-06-24 18:03:53.747000', '地点描述：一条几乎干涸溪流的源头，水流从岩石缝隙中渗出，形成一个小水潭。周围的植物异常茂盛。\n可获得物资：食物 (8单位)， 帆布 (2m)\n历史秘密碎片：你注意到水潭底的鹅卵石排列得异常整齐，似乎形成了一个古老的箭头，指向森林的更深处。这不是自然形成的。\n难度：2', '隐秘的溪流源头', 'common', '\0', '2026-06-24 18:03:53.747000', '2', '\0', '一条几乎干涸溪流的源头，水流从岩石缝隙中渗出，形成一个小水潭。周围的植物异常茂盛。', '你注意到水潭底的鹅卵石排列得异常整齐，似乎形成了一个古老的箭头，指向森林的更深处。这不是自然形成的。');
INSERT INTO `island_event` VALUES ('20', '2026-06-24 18:03:53.770000', '地点描述：一个新的洞穴在风暴后露出，入口狭窄，内部潮湿，回荡着水声。墙壁上似乎有模糊的图案。\n可获得物资：医疗资源 (5单位)， 煤油 (2升)\n历史秘密碎片：墙壁上的图案描绘了一群人正将某种发光的石头沉入海底，而天空之上，有巨大的眼睛在注视着这一切。你感到一阵没来由的寒意。\n难度：3', '风暴后的海岸洞穴', 'common', '\0', '2026-06-24 18:03:53.770000', '3', '\0', '一个新的洞穴在风暴后露出，入口狭窄，内部潮湿，回荡着水声。墙壁上似乎有模糊的图案。', '墙壁上的图案描绘了一群人正将某种发光的石头沉入海底，而天空之上，有巨大的眼睛在注视着这一切。你感到一阵没来由的寒意。');
INSERT INTO `island_event` VALUES ('21', '2026-06-24 18:03:53.791000', '地点描述：一艘被冲上沙滩、早已破烂不堪的渔船残骸，船体断裂，长满藤壶。\n可获得物资：木材 (5吨)， 金属制品 (3吨)， 煤油 (2升)\n历史秘密碎片：在船长的舱室，你找到一本泡烂的航海日志，最后几页的字迹疯狂扭曲，反复写着同一句话：\"他们不该打开那个盒子......银色的盒子......\"。字迹在\"银色的盒子\"这里戛然而止。\n难度：4', '搁浅的旧渔船残骸', 'common', '\0', '2026-06-24 18:03:53.791000', '4', '\0', '一艘被冲上沙滩、早已破烂不堪的渔船残骸，船体断裂，长满藤壶。', '在船长的舱室，你找到一本泡烂的航海日志，最后几页的字迹疯狂扭曲，反复写着同一句话：\"他们不该打开那个盒子......银色的盒子......\"。字迹在\"银色的盒子\"这里戛然而止。');
INSERT INTO `island_event` VALUES ('22', '2026-06-24 18:03:53.822000', '地点描述：一个被藤蔓覆盖的木制路标，指向两个方向。一个方向指向小镇，另一个方向模糊难辨，通往一片被迷雾笼罩的谷地。\n可获得物资：无直接物资，但获得进入一个隐藏地点的线索。请私信dm。\n历史秘密碎片：路标上除了现代字迹，下方隐约刻着一行古老的文字，与现在任何语言都不相似。当你触摸它时，仿佛听到了极远处传来的钟声。\n难度：8', '旧路标与岔道', 'uncommon', '\0', '2026-06-24 18:03:53.822000', '8', '\0', '一个被藤蔓覆盖的木制路标，指向两个方向。一个方向指向小镇，另一个方向模糊难辨，通往一片被迷雾笼罩的谷地。', '路标上除了现代字迹，下方隐约刻着一行古老的文字，与现在任何语言都不相似。当你触摸它时，仿佛听到了极远处传来的钟声。');
INSERT INTO `island_event` VALUES ('23', '2026-06-24 18:03:53.830000', '地点描述：小镇边缘一间半倒塌的旧工坊，内部有一个废弃的铁制炉灶，炉膛里积满了灰烬和杂物。\n可获得物资：金属制品 (8kg)， 木材 (5吨)\n历史秘密碎片：你在清理炉灶时，发现一块被熏黑的铁板，上面用钢钉歪歪扭扭地刻着一句话：\"我们烧掉了所有带\'标记\'的东西，但灰烬里又长出了新的。\"这让你联想到那些瘟疫时期的传言。\n难度：9', '废弃的工坊炉灶', 'uncommon', '\0', '2026-06-24 18:03:53.830000', '9', '\0', '小镇边缘一间半倒塌的旧工坊，内部有一个废弃的铁制炉灶，炉膛里积满了灰烬和杂物。', '你在清理炉灶时，发现一块被熏黑的铁板，上面用钢钉歪歪扭扭地刻着一句话：\"我们烧掉了所有带\'标记\'的东西，但灰烬里又长出了新的。\"这让你联想到那些瘟疫时期的传言。');
INSERT INTO `island_event` VALUES ('24', '2026-06-24 18:03:53.851000', '地点描述：一处早已废弃的羊圈，石墙倒塌，里面空无一物，只有厚厚的干草和粪便。\n可获得物资： 木材 (5吨)\n历史秘密碎片：在清理一个石缝时，你找到一枚锈蚀的铜质奖章，上面刻着\"第一届殖民垦殖纪念——1887\"。但这座岛屿在官方的历史中，从未在1887年有过大规模殖民活动。\n难度：5', '坍塌的羊圈遗址', 'common', '\0', '2026-06-24 18:03:53.851000', '5', '\0', '一处早已废弃的羊圈，石墙倒塌，里面空无一物，只有厚厚的干草和粪便。', '在清理一个石缝时，你找到一枚锈蚀的铜质奖章，上面刻着\"第一届殖民垦殖纪念——1887\"。但这座岛屿在官方的历史中，从未在1887年有过大规模殖民活动。');
INSERT INTO `island_event` VALUES ('25', '2026-06-24 18:03:53.866000', '地点描述：一棵空心大树的树洞里，藏着一个油布包裹的旧木箱，上面落满了灰尘和松针。\n可获得物资：猎枪弹 (4发)， 信号枪 (1把，含2发照明弹)\n历史秘密碎片：箱盖内侧刻着一张粗糙的地图，标记着一个远离所有已知地点的X。旁边写着：\"如果看到红色的月亮，就去那里。\"你不确定这是玩笑还是某种古老的预警。\n难度：6', '猎人的旧货箱', 'uncommon', '\0', '2026-06-24 18:03:53.866000', '6', '\0', '一棵空心大树的树洞里，藏着一个油布包裹的旧木箱，上面落满了灰尘和松针。', '箱盖内侧刻着一张粗糙的地图，标记着一个远离所有已知地点的X。旁边写着：\"如果看到红色的月亮，就去那里。\"你不确定这是玩笑还是某种古老的预警。');
INSERT INTO `island_event` VALUES ('26', '2026-06-24 18:03:53.881000', '地点描述：在一块刻有\"静默\"二字的巨石下，你发现一个被油布盖住的小坑。里面似乎是某人存放的紧急物资，用的都是很老旧的款式。\n可获得物资：医疗包 (1个)， 手电筒 (1个)\n历史秘密碎片：油布的一角绣着一行小字：\"致下一位守夜者\"。你不确定这指的是某个职业，还是某个秘密组织的成员。\n难度：7', '守夜人的秘密补给点', 'uncommon', '', '2026-06-24 20:20:22.470000', '7', '\0', '在一块刻有\"静默\"二字的巨石下，你发现一个被油布盖住的小坑。里面似乎是某人存放的紧急物资，用的都是很老旧的款式。', '油布的一角绣着一行小字：\"致下一位守夜者\"。你不确定这指的是某个职业，还是某个秘密组织的成员。');
INSERT INTO `island_event` VALUES ('27', '2026-06-24 18:03:53.905000', '地点描述：一个被灌木和碎石掩盖的、通往矿场深处的新入口。进入后能闻到一股不同于普通矿石的、微弱的金属气味。\n可获得物资：金属制品 (1吨)， 炸药（10kg）\n历史秘密碎片：在矿道深处，你发现一处被凿开的墙壁，里面镶嵌着一些带有精细纹路的黑色金属块。这不像是本岛已知的任何矿产。边上有些硝化甘油，这是干什么的？\n难度：9', '旧矿道的另一个入口', 'uncommon', '\0', '2026-06-24 18:03:53.905000', '9', '\0', '一个被灌木和碎石掩盖的、通往矿场深处的新入口。进入后能闻到一股不同于普通矿石的、微弱的金属气味。', '在矿道深处，你发现一处被凿开的墙壁，里面镶嵌着一些带有精细纹路的黑色金属块。这不像是本岛已知的任何矿产。边上有些硝化甘油，这是干什么的？');
INSERT INTO `island_event` VALUES ('28', '2026-06-24 18:03:53.926000', '地点描述：一片林间空地上，有一堆早已冰冷的篝火余烬，周围散落着一些罐头盒和磨损的皮靴。\n可获得物资：食物 (5单位)， 绳索 (5米)\n历史秘密碎片：你用木棍拨开灰烬时，发现一枚被烧得变形的铜扣子，上面还残留着一小片布料的痕迹。那布料的颜色和纹路，与气象观测站里的旧制服惊人地相似。\n难度：3', '林间空地的篝火余烬', 'common', '\0', '2026-06-24 18:03:53.926000', '3', '\0', '一片林间空地上，有一堆早已冰冷的篝火余烬，周围散落着一些罐头盒和磨损的皮靴。', '你用木棍拨开灰烬时，发现一枚被烧得变形的铜扣子，上面还残留着一小片布料的痕迹。那布料的颜色和纹路，与气象观测站里的旧制服惊人地相似。');
INSERT INTO `island_event` VALUES ('29', '2026-06-24 18:03:53.949000', '地点描述：你注意到一个半埋在沙中的绿色玻璃瓶，里面似乎塞着一张纸条。\n可获得物资：无物资，但获得一条来自\"外界\"或\"过去\"的模糊信息。\n历史秘密碎片：纸条上的字迹已经模糊，只能依稀辨认出几个词：\"......我们失败了......轮回继续......希望下一次有人能打破......银币是钥匙，也是枷锁......\"落款是一串数字，而不是名字。\n难度：10', '海滩上的漂流瓶', 'rare', '\0', '2026-06-24 18:03:53.949000', '10', '\0', '你注意到一个半埋在沙中的绿色玻璃瓶，里面似乎塞着一张纸条。', '纸条上的字迹已经模糊，只能依稀辨认出几个词：\"......我们失败了......轮回继续......希望下一次有人能打破......银币是钥匙，也是枷锁......\"落款是一串数字，而不是名字。');
INSERT INTO `island_event` VALUES ('30', '2026-06-24 18:03:53.962000', '地点描述：一座用黑色石头垒砌的圆形基座，像是某种古老的观测站或祭坛。中心有一根断裂的石柱。\n可获得物资：金属制品 (10kg)\n历史秘密碎片：基座的地面上有被火烧过的痕迹，呈现出一种奇异的同心圆图案。这让你想起灯塔透镜的结构。也许，这里才是岛上最早的\"灯塔\"。\n难度：11', '古老观测站遗迹', 'rare', '\0', '2026-06-24 18:03:53.962000', '11', '\0', '一座用黑色石头垒砌的圆形基座，像是某种古老的观测站或祭坛。中心有一根断裂的石柱。', '基座的地面上有被火烧过的痕迹，呈现出一种奇异的同心圆图案。这让你想起灯塔透镜的结构。也许，这里才是岛上最早的\"灯塔\"。');
INSERT INTO `island_event` VALUES ('31', '2026-06-24 18:03:53.977000', '地点描述：在码头外围的浅水区，半沉着一艘小型铁皮驳船。船舱进水，但部分结构露出水面。\n可获得物资：金属制品 (1吨)， 煤油 (8升)， 绳索 (5米)\n历史秘密碎片：你在驾驶室找到一本浸透的账本，上面的日期停在了一个未知的年份。最后一笔账目记录的是：\"5瓶朗姆酒换20kg食物\"这与你从旅店老人口中听到的、关于\"最后的晚餐\"的传闻惊人地相似。\n难度：12', '沉没的驳船', 'rare', '\0', '2026-06-24 18:03:53.977000', '12', '\0', '在码头外围的浅水区，半沉着一艘小型铁皮驳船。船舱进水，但部分结构露出水面。', '你在驾驶室找到一本浸透的账本，上面的日期停在了一个未知的年份。最后一笔账目记录的是：\"5瓶朗姆酒换20kg食物\"这与你从旅店老人口中听到的、关于\"最后的晚餐\"的传闻惊人地相似。');
INSERT INTO `island_event` VALUES ('32', '2026-06-24 18:03:54.008000', '地点描述：一棵巨大古树被连根拔起，巨大的根系暴露在外，形成了一个天然空洞。\n可获得物资：木材 (10吨)， 沥青（10kg）。\n历史秘密碎片：你在树根之间发现一个石质的、被树根缠绕的小盒子。打开后，里面空无一物，只在盒盖上刻着一行小字：\"为了遗忘\"。\n难度：13', '被连根拔起的古树', 'rare', '\0', '2026-06-24 18:03:54.008000', '13', '\0', '一棵巨大古树被连根拔起，巨大的根系暴露在外，形成了一个天然空洞。', '你在树根之间发现一个石质的、被树根缠绕的小盒子。打开后，里面空无一物，只在盒盖上刻着一行小字：\"为了遗忘\"。');
INSERT INTO `island_event` VALUES ('33', '2026-06-24 18:03:54.029000', '地点描述：在陡峭的悬崖边，你发现一面早已褪色的破旧信号旗，被固定在岩石缝隙中。在下方一处涨潮线以上的岩缝里，藏着一个沉重、被油布包裹的货包。\n可获得物资：沥青 (10kg)， 帆布 (3m)， 绳索 (5米)\n历史秘密碎片：旗帜的布料纤维非常奇特，不像本地能生产的。上面用一种暗红色的染料画着一个符号：一艘船，正驶向一个圆形的、散发着光芒的入口。那包沥青的包装上，印着一个已经模糊不清的、带有某种\"委员会\"字样的徽记。\n难度：12', '悬崖上的信号旗与沉货', 'rare', '\0', '2026-06-24 18:03:54.029000', '12', '\0', '在陡峭的悬崖边，你发现一面早已褪色的破旧信号旗，被固定在岩石缝隙中。在下方一处涨潮线以上的岩缝里，藏着一个沉重、被油布包裹的货包。', '旗帜的布料纤维非常奇特，不像本地能生产的。上面用一种暗红色的染料画着一个符号：一艘船，正驶向一个圆形的、散发着光芒的入口。那包沥青的包装上，印着一个已经模糊不清的、带有某种\"委员会\"字样的徽记。');
INSERT INTO `island_event` VALUES ('34', '2026-06-24 18:03:54.053000', '地点描述：在酒吧后巷的垃圾堆旁，你踢到一个被隐藏得极好的木板箱，上面有撬开的痕迹。\n可获得物资：朗姆酒 (3瓶)， 旧式手枪 (1把，含6发子弹)\n历史秘密碎片：箱底铺着的旧报纸上，刊登着一则轶闻：\"神秘女士于风暴夜造访本岛，随身携带一枚\'雕有巨瞳\'的银色奖章，后不知所踪。\"\n难度：15', '废弃的木板箱（走私者藏匿点）', 'epic', '\0', '2026-06-24 18:03:54.053000', '15', '\0', '在酒吧后巷的垃圾堆旁，你踢到一个被隐藏得极好的木板箱，上面有撬开的痕迹。', '箱底铺着的旧报纸上，刊登着一则轶闻：\"神秘女士于风暴夜造访本岛，随身携带一枚\'雕有巨瞳\'的银色奖章，后不知所踪。\"');
INSERT INTO `island_event` VALUES ('35', '2026-06-24 18:03:54.065000', '地点描述：教堂后面的废弃花园中，一口被石板半掩的枯井。井壁上长满了潮湿的苔藓，深处似乎有空间。\n可获得物资：医疗包 (2单位)\n历史秘密碎片：你下到井底，发现井壁上有一处被新泥掩盖过的暗门。门缝里夹着一角泛黄的纸，上面写着：\"他们没有建塔，他们挖了井。一直挖，直到听见下面有呼吸声。\"这个\"下面\"，是指地底，还是指别的什么？\n难度：17', '教堂花园的枯井', 'epic', '\0', '2026-06-24 18:03:54.065000', '17', '\0', '教堂后面的废弃花园中，一口被石板半掩的枯井。井壁上长满了潮湿的苔藓，深处似乎有空间。', '你下到井底，发现井壁上有一处被新泥掩盖过的暗门。门缝里夹着一角泛黄的纸，上面写着：\"他们没有建塔，他们挖了井。一直挖，直到听见下面有呼吸声。\"这个\"下面\"，是指地底，还是指别的什么？');
INSERT INTO `island_event` VALUES ('36', '2026-06-24 18:03:54.078000', '地点描述：你偷偷潜入旅店的阁楼，在积满灰尘的旧物中翻找。你发现了旧地图 (标记了某些特殊地点，如隐藏入口)，可私信dm。\n可获得物资：维修工具包 (1个)\n历史秘密碎片：你找到一本匿名笔记，上面写道：\"我听到了那些在梦中死去的殖民者的低语。他们告诉我，暴雪是\'收割者\'，而我们是种下的作物。只有找到最初的\'种子\'，才能逃离这个循环。\"\n难度：19', '旅店阁楼的笔记', 'legendary', '\0', '2026-06-24 18:03:54.078000', '19', '\0', '你偷偷潜入旅店的阁楼，在积满灰尘的旧物中翻找。你发现了旧地图 (标记了某些特殊地点，如隐藏入口)，可私信dm。', '你找到一本匿名笔记，上面写道：\"我听到了那些在梦中死去的殖民者的低语。他们告诉我，暴雪是\'收割者\'，而我们是种下的作物。只有找到最初的\'种子\'，才能逃离这个循环。\"');
INSERT INTO `island_event` VALUES ('37', '2026-06-24 18:03:54.091000', '地点描述：你凭借对教堂结构的了解或探索，发现一个通往地下墓穴的暗门。空气冰冷而凝滞。你获得了一枚磨损严重的旧银币（不可丢弃的诅咒信物）\n可获得物资：医疗资源 (10单位)， 煤油 (5升)\n历史秘密碎片：墓穴墙壁上刻满了密密麻麻的名字和日期，最早的可以追溯到几百年前。最底部的墙壁上，用现代英语刻着一句新话：\"又开始了。\"下面的日期，正是预测暴雪来临的日子。那枚银币的图案，正是那个\"巨瞳\"符号——你在之前多处线索中看到过它。握在手中，你第一次清晰地意识到：这座岛是一个周期，而你，刚刚走进了这个周期的证据里。\n难度：20', '教堂地下的低语（指向度最高）', 'legendary', '\0', '2026-06-24 18:03:54.091000', '20', '', '你凭借对教堂结构的了解或探索，发现一个通往地下墓穴的暗门。空气冰冷而凝滞。你获得了一枚磨损严重的旧银币（不可丢弃的诅咒信物）', '墓穴墙壁上刻满了密密麻麻的名字和日期，最早的可以追溯到几百年前。最底部的墙壁上，用现代英语刻着一句新话：\"又开始了。\"下面的日期，正是预测暴雪来临的日子。那枚银币的图案，正是那个\"巨瞳\"符号——你在之前多处线索中看到过它。握在手中，你第一次清晰地意识到：这座岛是一个周期，而你，刚刚走进了这个周期的证据里。');
INSERT INTO `island_event` VALUES ('38', '2026-06-24 18:03:54.110000', '地点描述：森林边缘一座被积雪半掩的小木屋，门板已经朽坏，屋内一片狼藉。壁炉里还有未燃尽的柴火，但早已冻成了冰坨。\n可获得物资：木材（2吨）， 煤油（2升）， 火柴（3盒）\n历史秘密碎片：你在床板下发现一块刻满划痕的木板，上面记录着\"第7次\"和一个潦草的倒计时。旁边有一行小字：\"我们以为熬过暴雪就赢了，但暴雪之后，还有东西在等着我们。\"字迹到此为止，笔尖在木板上划出一道长长的、颤抖的痕迹。\n难度：16', '冰封的猎人小屋', 'epic', '', '2026-06-24 20:46:37.231000', '16', '\0', '森林边缘一座被积雪半掩的小木屋，门板已经朽坏，屋内一片狼藉。壁炉里还有未燃尽的柴火，但早已冻成了冰坨。', '你在床板下发现一块刻满划痕的木板，上面记录着\"第7次\"和一个潦草的倒计时。旁边有一行小字：\"我们以为熬过暴雪就赢了，但暴雪之后，还有东西在等着我们。\"字迹到此为止，笔尖在木板上划出一道长长的、颤抖的痕迹。');
INSERT INTO `island_event` VALUES ('39', '2026-06-24 18:03:54.134000', '地点描述：一片向阳的山坡上，排列着几个破败的木质蜂箱，早已没有蜜蜂的踪迹。周围长满了干枯的野花。\n可获得物资：草药（4单位） ， 蜡烛（5根）\n历史秘密碎片：你打开一个蜂箱的底层，发现一块被蜂蜡密封的油布。打开后，里面是一幅用炭笔画的速写：一个巨大的、被藤蔓和苔藓覆盖的圆形建筑，顶端裂开，一道光柱直射向天空。画的背面写着：\"他们以为是信号塔，其实是接收器。他们一直在等\'上面\'的回应。\"\n难度：5', '废弃的蜂箱', 'common', '', '2026-06-24 20:03:58.180000', '5', '\0', '一片向阳的山坡上，排列着几个破败的木质蜂箱，早已没有蜜蜂的踪迹。周围长满了干枯的野花。', '你打开一个蜂箱的底层，发现一块被蜂蜡密封的油布。打开后，里面是一幅用炭笔画的速写：一个巨大的、被藤蔓和苔藓覆盖的圆形建筑，顶端裂开，一道光柱直射向天空。画的背面写着：\"他们以为是信号塔，其实是接收器。他们一直在等\'上面\'的回应。\"');
INSERT INTO `island_event` VALUES ('40', '2026-06-24 18:03:54.156000', '地点描述：在一处人迹罕至的礁石滩上，搁浅着一艘小型划艇，船底已经破了一个大洞。船桨断成两截，散落在附近。\n可获得物资：金属制品（20kg） ， 绳索（8米）， 渔网（1张）\n历史秘密碎片：你在划艇的坐板下找到一个防水盒，里面有一封没有寄出的信。信上写道：\"亲爱的玛莎，我发现了他们藏起来的真相——这座岛不是被发现的，是被\'放\'在这里的。那些银色的盒子，每一个都是一段被删除的记忆。我必须把证据藏起来，藏在……\"信件在这里中断，最后几个字被水渍完全浸染，无法辨认。\n难度：14', '搁浅的划艇', 'epic', '\0', '2026-06-24 18:03:54.156000', '14', '\0', '在一处人迹罕至的礁石滩上，搁浅着一艘小型划艇，船底已经破了一个大洞。船桨断成两截，散落在附近。', '你在划艇的坐板下找到一个防水盒，里面有一封没有寄出的信。信上写道：\"亲爱的玛莎，我发现了他们藏起来的真相——这座岛不是被发现的，是被\'放\'在这里的。那些银色的盒子，每一个都是一段被删除的记忆。我必须把证据藏起来，藏在……\"信件在这里中断，最后几个字被水渍完全浸染，无法辨认。');
INSERT INTO `island_event` VALUES ('41', '2026-06-24 18:03:54.182000', '地点描述：岛内一处隐蔽的峡谷中，有一汪冒着热气的地热泉，周围没有积雪，反而长着一些深绿色的蕨类植物。空气中弥漫着一股硫磺味。\n可获得物资：食物（5单位） ， 医疗包（2单位）\n历史秘密碎片：你发现在泉眼边缘的岩石上，有人用锐器刻下了一句话：\"水温能救命，但救不了轮回。他们在地下深处烧着某种东西，那东西让整个岛都在呼吸。\"你伸手探了探水温，温热的触感让你短暂地忘记了寒冷，但那句\"呼吸\"却让你后背发凉。\n难度：8', '地热泉眼', 'uncommon', '', '2026-06-24 20:20:36.348000', '8', '\0', '岛内一处隐蔽的峡谷中，有一汪冒着热气的地热泉，周围没有积雪，反而长着一些深绿色的蕨类植物。空气中弥漫着一股硫磺味。', '你发现在泉眼边缘的岩石上，有人用锐器刻下了一句话：\"水温能救命，但救不了轮回。他们在地下深处烧着某种东西，那东西让整个岛都在呼吸。\"你伸手探了探水温，温热的触感让你短暂地忘记了寒冷，但那句\"呼吸\"却让你后背发凉。');
INSERT INTO `island_event` VALUES ('42', '2026-06-24 18:03:54.210000', '地点描述：在一条通往内陆的雪径上，散落着一辆损坏的雪橇，拉绳已经断裂，上面堆着一些空木箱。\n可获得物资：木材（1吨） ， 帆布（2米）\n历史秘密碎片：你在雪橇底部发现一张被冻脆的纸片，上面画着一幅简单的星图，其中一颗星被重点圈出，旁边标注着：\"当它再次亮起时，暴雪就会开始。这不是天气预报，这是某种约定。\"你抬头看了看阴沉的天色，不知道那颗星是否已经亮起。\n难度：7', '被遗弃的雪橇', 'uncommon', '\0', '2026-06-24 18:03:54.210000', '7', '\0', '在一条通往内陆的雪径上，散落着一辆损坏的雪橇，拉绳已经断裂，上面堆着一些空木箱。', '你在雪橇底部发现一张被冻脆的纸片，上面画着一幅简单的星图，其中一颗星被重点圈出，旁边标注着：\"当它再次亮起时，暴雪就会开始。这不是天气预报，这是某种约定。\"你抬头看了看阴沉的天色，不知道那颗星是否已经亮起。');
INSERT INTO `island_event` VALUES ('43', '2026-06-24 18:03:54.229000', '地点描述：小镇废弃邮局门口，一个锈迹斑斑的红色邮箱依然伫立，锁头已经损坏。\n可获得物资：铅笔（3支）\n历史秘密碎片：你从邮箱里掏出一叠被虫蛀过的信件。其中一封的落款是\"委员会档案室\"，内容很简短：\"致本岛居民：你们申报的\'气候异常\'已收到。经核实，该区域未有相关记录。请勿传播不实信息。\"信的底部，有人用红笔愤怒地批注了一行字：\"骗子！他们删掉了所有1887年之前的记录！\"信件中你发现了一个频率信号，或许可以在邮局发挥作用。\n难度：16', '旧邮局的邮箱', 'epic', '\0', '2026-06-24 18:03:54.229000', '16', '\0', '小镇废弃邮局门口，一个锈迹斑斑的红色邮箱依然伫立，锁头已经损坏。', '你从邮箱里掏出一叠被虫蛀过的信件。其中一封的落款是\"委员会档案室\"，内容很简短：\"致本岛居民：你们申报的\'气候异常\'已收到。经核实，该区域未有相关记录。请勿传播不实信息。\"信的底部，有人用红笔愤怒地批注了一行字：\"骗子！他们删掉了所有1887年之前的记录！\"信件中你发现了一个频率信号，或许可以在邮局发挥作用。');
INSERT INTO `island_event` VALUES ('44', '2026-06-24 18:03:54.242000', '地点描述：岛屿最高处，几块巨大的黑色岩石被围成圆圈，看起来不像自然形成。中间有一块平坦的祭坛石，上面布满了裂隙。\n可获得物资：石料（10吨）\n历史秘密碎片：你清扫祭坛石上的积雪时，发现上面刻着一幅精细的浮雕：一条首尾相接的蛇，咬着自己的尾巴，蛇身由无数个小小的\"巨瞳\"符号组成。蛇的中央，是一枚硬币的形状。你终于明白，这个符号不是装饰，是地图——一个无限循环的地图。\n难度：17', '山顶的巨石阵', 'epic', '\0', '2026-06-24 18:03:54.242000', '17', '\0', '岛屿最高处，几块巨大的黑色岩石被围成圆圈，看起来不像自然形成。中间有一块平坦的祭坛石，上面布满了裂隙。', '你清扫祭坛石上的积雪时，发现上面刻着一幅精细的浮雕：一条首尾相接的蛇，咬着自己的尾巴，蛇身由无数个小小的\"巨瞳\"符号组成。蛇的中央，是一枚硬币的形状。你终于明白，这个符号不是装饰，是地图——一个无限循环的地图。');
INSERT INTO `island_event` VALUES ('45', '2026-06-24 18:03:54.256000', '地点描述：一片结冰的湖面上，冰层深处隐约可见一些轮廓，像是被冻住的木船或者小屋。冰面边缘有一处塌陷，露出冰冷的湖水。\n可获得物资：金属制品（50kg） ， 木材（3吨）\n历史秘密碎片：你小心地靠近塌陷处，用长杆打捞，勾上来一块巴掌大的铜牌。上面刻着：\"献给第116位沉睡者。\"下方是一行小字：\"当我们变成冰的一部分，冰就会替我们记住一切。\"\n难度：15', '冰下墓地', 'epic', '\0', '2026-06-24 18:03:54.256000', '15', '\0', '一片结冰的湖面上，冰层深处隐约可见一些轮廓，像是被冻住的木船或者小屋。冰面边缘有一处塌陷，露出冰冷的湖水。', '你小心地靠近塌陷处，用长杆打捞，勾上来一块巴掌大的铜牌。上面刻着：\"献给第116位沉睡者。\"下方是一行小字：\"当我们变成冰的一部分，冰就会替我们记住一切。\"');
INSERT INTO `island_event` VALUES ('46', '2026-06-24 18:03:54.272000', '地点描述：在通往旧矿山的山路上，散落着一节锈蚀的矿车轨道和一具翻倒的蒸汽机车残骸，早已和藤蔓岩石融为一体。\n可获得物资：金属制品（8吨） ， 煤油（12升） ， 发动机（1个）\n历史秘密碎片：你在机车的锅炉里发现一个被烧得变形的铁盒，里面有一卷胶卷。虽然大部分已损毁，但有几帧还能看清：一群穿着旧式服装的人，正在将一块巨大的银色金属板沉入一个深坑，旁边站着一个穿着黑袍、手持提灯的人，看不清脸。\n难度：18', '火车残骸（矿山支线）', 'legendary', '\0', '2026-06-24 18:03:54.272000', '18', '\0', '在通往旧矿山的山路上，散落着一节锈蚀的矿车轨道和一具翻倒的蒸汽机车残骸，早已和藤蔓岩石融为一体。', '你在机车的锅炉里发现一个被烧得变形的铁盒，里面有一卷胶卷。虽然大部分已损毁，但有几帧还能看清：一群穿着旧式服装的人，正在将一块巨大的银色金属板沉入一个深坑，旁边站着一个穿着黑袍、手持提灯的人，看不清脸。');
INSERT INTO `island_event` VALUES ('47', '2026-06-24 18:03:54.296000', '地点描述：在一片茂密的冷杉林中，有一个被遗弃的营地，帐篷已经坍塌，篝火堆里还有未燃尽的骨头。\n可获得物资：猎枪弹（4发） ， 食物（6单位），猎枪（1把）\n历史秘密碎片：你在帐篷下的泥土里挖出一个密封的锡罐，里面是一张纸条：\"我看到了那些\'守夜者\'的秘密集会。他们在向一个银色的盒子祈祷。盒子上刻着那个眼睛。那不是神，是某种监控装置。我决定把它挖出来，埋到一个没人能找到的地方……\"纸条没有署名，但你注意到纸条背面用铅笔轻轻画了一个箭头，指向岛的东北方向。\n难度：16', '失踪猎人营地', 'epic', '', '2026-06-24 20:19:50.438000', '16', '\0', '在一片茂密的冷杉林中，有一个被遗弃的营地，帐篷已经坍塌，篝火堆里还有未燃尽的骨头。', '你在帐篷下的泥土里挖出一个密封的锡罐，里面是一张纸条：\"我看到了那些\'守夜者\'的秘密集会。他们在向一个银色的盒子祈祷。盒子上刻着那个眼睛。那不是神，是某种监控装置。我决定把它挖出来，埋到一个没人能找到的地方……\"纸条没有署名，但你注意到纸条背面用铅笔轻轻画了一个箭头，指向岛的东北方向。');
INSERT INTO `island_event` VALUES ('48', '2026-06-24 18:03:54.319000', '地点描述：海岸边一座高耸的金属信号塔，被狂风吹得微微倾斜，顶部的警示灯早已熄灭。塔身锈迹斑斑，底部有一扇紧锁的铁门。\n可获得物资：金属制品（5吨） ， 手电筒（1个）\n历史秘密碎片：你设法撬开铁门，在塔内控制台上发现一本值班日志。最后一页的日期是\"1892年11月\"。上面写着：\"收到一艘未注册船只的求救信号。对方声称来自\'锚点\'站。我们回复\'锚点已失效\'。对方沉默了很久，只回了四个字：\'那我们来重置。\'\"你抬头看了看这座塔，忽然意识到它可能不是什么信号塔。\n难度：19', '风暴中的信号塔', 'legendary', '\0', '2026-06-24 18:03:54.319000', '19', '\0', '海岸边一座高耸的金属信号塔，被狂风吹得微微倾斜，顶部的警示灯早已熄灭。塔身锈迹斑斑，底部有一扇紧锁的铁门。', '你设法撬开铁门，在塔内控制台上发现一本值班日志。最后一页的日期是\"1892年11月\"。上面写着：\"收到一艘未注册船只的求救信号。对方声称来自\'锚点\'站。我们回复\'锚点已失效\'。对方沉默了很久，只回了四个字：\'那我们来重置。\'\"你抬头看了看这座塔，忽然意识到它可能不是什么信号塔。');
INSERT INTO `island_event` VALUES ('49', '2026-06-24 18:03:54.335000', '地点描述：在森林深处，有一口被厚重铁板盖住的古井，铁板上有一个滑轮装置，垂下一根粗麻绳，末端系着一个破旧的木吊篮。\n可获得物资：绳索（30米） ， 炸药（5kg）\n历史秘密碎片：你把吊篮拉上来，发现篮底有一块被油布包裹的石头。石头上刻着一张脸，但那脸上的五官是倒置的。旁边刻着一行细小的文字：\"向下看，才是向上。他们从一开始就告诉了我们真相，只是我们从未相信。\"\n难度：9', '深井与吊篮', 'uncommon', '\0', '2026-06-24 18:03:54.335000', '9', '\0', '在森林深处，有一口被厚重铁板盖住的古井，铁板上有一个滑轮装置，垂下一根粗麻绳，末端系着一个破旧的木吊篮。', '你把吊篮拉上来，发现篮底有一块被油布包裹的石头。石头上刻着一张脸，但那脸上的五官是倒置的。旁边刻着一行细小的文字：\"向下看，才是向上。他们从一开始就告诉了我们真相，只是我们从未相信。\"');
INSERT INTO `island_event` VALUES ('50', '2026-06-24 18:03:54.351000', '地点描述：小镇主街上的一家旧相机店，橱窗玻璃破碎，里面散落着落满灰尘的相机和胶卷盒。\n可获得物资：维修工具包（1个） ， 火把（2把）\n历史秘密碎片：你在一台木制的大画幅相机暗盒里，发现一张尚未冲洗的底片。对着光仔细辨认，画面里是一个穿着旧式长裙的女人，站在一扇巨大的圆形金属门前，手里捧着一个发光的球体。她的脸被一片光晕遮住，但那扇门上的纹路，和你在教堂地下看到的图案一模一样。\n难度：4', '古董相机店', 'common', '\0', '2026-06-24 18:03:54.351000', '4', '\0', '小镇主街上的一家旧相机店，橱窗玻璃破碎，里面散落着落满灰尘的相机和胶卷盒。', '你在一台木制的大画幅相机暗盒里，发现一张尚未冲洗的底片。对着光仔细辨认，画面里是一个穿着旧式长裙的女人，站在一扇巨大的圆形金属门前，手里捧着一个发光的球体。她的脸被一片光晕遮住，但那扇门上的纹路，和你在教堂地下看到的图案一模一样。');
INSERT INTO `island_event` VALUES ('51', '2026-06-24 18:03:54.367000', '地点描述：旧工坊群深处，一间独立的砖砌建筑，内部有一座巨大的铁制锅炉，已经完全冷却。角落里堆满废铁和煤渣。\n可获得物资：金属制品（1吨） ， 燃料（煤炭，可作燃料，1吨）\n历史秘密碎片：你发现在锅炉的炉门内侧，用粉笔写着一句话：\"我们烧了三天三夜，把那些带\'标记\'的东西全扔进去了，但它们没有融化，只是变得更烫了。那晚，锅炉自己唱起了歌。\"你伸手摸了摸冰冷的炉壁，仿佛能感觉到某种余温。\n难度：10', '废弃的锅炉房', 'rare', '\0', '2026-06-24 18:03:54.367000', '10', '\0', '旧工坊群深处，一间独立的砖砌建筑，内部有一座巨大的铁制锅炉，已经完全冷却。角落里堆满废铁和煤渣。', '你发现在锅炉的炉门内侧，用粉笔写着一句话：\"我们烧了三天三夜，把那些带\'标记\'的东西全扔进去了，但它们没有融化，只是变得更烫了。那晚，锅炉自己唱起了歌。\"你伸手摸了摸冰冷的炉壁，仿佛能感觉到某种余温。');
INSERT INTO `island_event` VALUES ('52', '2026-06-24 18:03:54.379000', '地点描述：一条几乎完全冰封的小河上，横亘着一座由树枝和泥土筑成的巨大河狸坝，结构异常结实。\n可获得物资：木材（2吨） ， 沥青（5kg）\n历史秘密碎片：你试图拆解坝体时，发现一根被咬断的树枝里夹着一块叠好的皮革。展开后，上面是用红色墨水绘制的地图，标记着一处\"不在任何图上\"的地点。地图边缘写着：\"河狸比我们更早学会筑墙。它们筑墙是为了引水，我们筑墙是为了引……\"最后一个字被一团墨迹盖住，但你隐约觉得那是个\"祸\"字。\n难度：9', '河狸坝', 'uncommon', '\0', '2026-06-24 18:03:54.379000', '9', '\0', '一条几乎完全冰封的小河上，横亘着一座由树枝和泥土筑成的巨大河狸坝，结构异常结实。', '你试图拆解坝体时，发现一根被咬断的树枝里夹着一块叠好的皮革。展开后，上面是用红色墨水绘制的地图，标记着一处\"不在任何图上\"的地点。地图边缘写着：\"河狸比我们更早学会筑墙。它们筑墙是为了引水，我们筑墙是为了引……\"最后一个字被一团墨迹盖住，但你隐约觉得那是个\"祸\"字。');
INSERT INTO `island_event` VALUES ('53', '2026-06-24 18:03:54.395000', '地点描述：在一处背风的岩壁下，搭着一顶坍塌的帐篷，旁边散落着一些仪器设备，像是地质勘探用的。帐篷内外都覆盖着厚厚的积雪。\n可获得物资：医疗包（1个） ， 食物（5单位），炸药（5kg）\n历史秘密碎片：你在帐篷里找到一本笔记本，字迹工整，但最后一页上歪歪扭扭地写着：\"我们测出了地下的异常结构。那不是矿脉，是一个比整个岛还大的空腔。里面有规律的震动，像心跳。队长决定向\'委员会\'报告，然后……我们收到了一个\'保持沉默\'的命令。我们不该来的。\"\n难度：4', '遇难的科考队', 'common', '\0', '2026-06-24 18:03:54.395000', '4', '\0', '在一处背风的岩壁下，搭着一顶坍塌的帐篷，旁边散落着一些仪器设备，像是地质勘探用的。帐篷内外都覆盖着厚厚的积雪。', '你在帐篷里找到一本笔记本，字迹工整，但最后一页上歪歪扭扭地写着：\"我们测出了地下的异常结构。那不是矿脉，是一个比整个岛还大的空腔。里面有规律的震动，像心跳。队长决定向\'委员会\'报告，然后……我们收到了一个\'保持沉默\'的命令。我们不该来的。\"');
INSERT INTO `island_event` VALUES ('54', '2026-06-24 18:03:54.417000', '地点描述：灯塔基座旁，有一间半塌的玻璃花房，屋顶已经碎裂，但内部的土壤中居然还顽强地生长着几株枯萎的玫瑰。\n可获得物资：朗姆酒（2瓶）\n历史秘密碎片：你清理花房的角落时，发现一块嵌入墙中的铜牌，上面刻着：\"献给那个一直在种花的人。他知道暴雪会来，但他还是种了。\"铜牌背后，刻着一个微小的\"巨瞳\"符号。\n难度：3', '灯塔看守人的花房', 'common', '\0', '2026-06-24 18:03:54.417000', '3', '\0', '灯塔基座旁，有一间半塌的玻璃花房，屋顶已经碎裂，但内部的土壤中居然还顽强地生长着几株枯萎的玫瑰。', '你清理花房的角落时，发现一块嵌入墙中的铜牌，上面刻着：\"献给那个一直在种花的人。他知道暴雪会来，但他还是种了。\"铜牌背后，刻着一个微小的\"巨瞳\"符号。');
INSERT INTO `island_event` VALUES ('55', '2026-06-24 18:03:54.432000', '地点描述：深入那艘搁浅渔船的残骸内部，在严重倾斜的船长室里，有一个半埋在淤泥中的小型保险箱，锁已经锈死。\n可获得物资：旧式手枪（1把，含6发子弹） ， 协议书（1张）\n历史秘密碎片：你用工具撬开保险箱，里面没有财物，只有一本泡烂的圣经。圣经的内页被挖空，藏着一叠纸。纸上记录着：\"银色的盒子不是钥匙，是终点。我们以为自己在探索世界，其实我们在探索自己的牢笼。每打开一个盒子，牢笼就会缩小一点。我烧掉了第4个盒子，但还有……\"纸在这里被撕掉了，但你看到纸张边缘有一行墨水写的、极其细小的字：\"3个。\"\n难度：13', '沉船船长室保险箱', 'rare', '\0', '2026-06-24 18:03:54.432000', '13', '\0', '深入那艘搁浅渔船的残骸内部，在严重倾斜的船长室里，有一个半埋在淤泥中的小型保险箱，锁已经锈死。', '你用工具撬开保险箱，里面没有财物，只有一本泡烂的圣经。圣经的内页被挖空，藏着一叠纸。纸上记录着：\"银色的盒子不是钥匙，是终点。我们以为自己在探索世界，其实我们在探索自己的牢笼。每打开一个盒子，牢笼就会缩小一点。我烧掉了第4个盒子，但还有……\"纸在这里被撕掉了，但你看到纸张边缘有一行墨水写的、极其细小的字：\"3个。\"');
INSERT INTO `island_event` VALUES ('56', '2026-06-24 18:03:54.438000', '地点描述：在岛北侧一片冻土荒原上，地面裂开一道缝，露出一个埋藏已久的陶罐口沿。\n可获得物资：炸药（10kg）\n历史秘密碎片：你挖出陶罐，发现里面装着几块刻满符文的骨片。符文你不认识，但你注意到其中一片的背面，有人用现代钢笔写了一行注：\"翻译员说这是某种祈禳文，大意是\'愿火焰吞噬眼睛\'——但为什么我们的祖先要祈禳一个眼睛？\"\n难度：7', '冻土下的陶罐', 'uncommon', '', '2026-06-24 20:01:15.673000', '7', '\0', '在岛北侧一片冻土荒原上，地面裂开一道缝，露出一个埋藏已久的陶罐口沿。', '你挖出陶罐，发现里面装着几块刻满符文的骨片。符文你不认识，但你注意到其中一片的背面，有人用现代钢笔写了一行注：\"翻译员说这是某种祈禳文，大意是\'愿火焰吞噬眼睛\'——但为什么我们的祖先要祈禳一个眼睛？\"');
INSERT INTO `island_event` VALUES ('57', '2026-06-24 18:03:54.449000', '地点描述：在一棵濒临枯死的古树顶端，有一个巨大的鸟巢，早已废弃。巢中用干草和树枝铺成，你发现其中夹杂着一些非自然的光泽。\n可获得物资：绳索（6米） ， 金属制品（50kg）\n历史秘密碎片：你爬上树顶，从巢中掏出一架黄铜制成的小型望远镜，镜筒上刻着\"1883\"。你透过它随意望向远方，但在转动角度时，你突然看见远处一座山的轮廓，那形状……像极了一个侧卧的、被冻僵的人形。你放下望远镜再看向那座山，它就只是一座普通的山了。\n难度：6', '鸟巢与望远镜', 'uncommon', '\0', '2026-06-24 18:03:54.449000', '6', '\0', '在一棵濒临枯死的古树顶端，有一个巨大的鸟巢，早已废弃。巢中用干草和树枝铺成，你发现其中夹杂着一些非自然的光泽。', '你爬上树顶，从巢中掏出一架黄铜制成的小型望远镜，镜筒上刻着\"1883\"。你透过它随意望向远方，但在转动角度时，你突然看见远处一座山的轮廓，那形状……像极了一个侧卧的、被冻僵的人形。你放下望远镜再看向那座山，它就只是一座普通的山了。');
INSERT INTO `island_event` VALUES ('58', '2026-06-24 18:03:54.466000', '地点描述：小镇外围，一座红砖建筑，招牌已经脱落。内部是宽阔的大厅，地面有沟槽，空中悬挂着锈蚀的铁钩和滑轮。空气中弥漫着一股无法形容的、混合了铁锈和焦油的气味。\n可获得物资：金属制品（200kg） ， 绳索（12米） ， 煤油（4升）\n历史秘密碎片：你在操作台下的暗格里，发现一本被撕掉封皮的账簿。上面记录的条目很奇怪：\"第1个：于北坡收割。\"、\"第2个：于海岸洞穴收割。\"……一直到\"第6个：于教堂地下收割。\"每一行后面都画着一个\"✓\"。而在账簿的最后一页，用崭新的墨水写着：\"第7轮次，收割者已就位。轮到你们了。\"\n难度：10', '废弃的屠宰场', 'rare', '\0', '2026-06-24 18:03:54.466000', '10', '\0', '小镇外围，一座红砖建筑，招牌已经脱落。内部是宽阔的大厅，地面有沟槽，空中悬挂着锈蚀的铁钩和滑轮。空气中弥漫着一股无法形容的、混合了铁锈和焦油的气味。', '你在操作台下的暗格里，发现一本被撕掉封皮的账簿。上面记录的条目很奇怪：\"第1个：于北坡收割。\"、\"第2个：于海岸洞穴收割。\"……一直到\"第6个：于教堂地下收割。\"每一行后面都画着一个\"✓\"。而在账簿的最后一页，用崭新的墨水写着：\"第7轮次，收割者已就位。轮到你们了。\"');
INSERT INTO `island_event` VALUES ('59', '2026-06-24 18:03:54.488000', '地点描述：在旅店地下室的隐蔽暗格中，你发现一本用防水布包裹的日记，属于一位名叫\"埃利斯\"的殖民者。日记的日期横跨多个年份，但笔迹逐渐从工整变得癫狂。\n可获得物资：医疗包（4单位） ， 朗姆酒（1瓶）\n历史秘密碎片：日记中写道：\"他们说我是疯子，说我听见的\'低语\'是幻觉。但我听得越来越清楚——那不是风声，是人声。成千上万的人声，在每一次暴雪来临前一起哭泣。他们告诉我，暴雪不是天气，是\'投票\'。当岛上的人互相残杀到一定数量，暴雪就来了。他们称之为\'收割合格样本\'。\"你合上日记时，耳边似乎真的掠过一丝若有若无的啜泣。\n难度：12', '潮汐症者的日志', 'rare', '\0', '2026-06-24 18:03:54.488000', '12', '\0', '在旅店地下室的隐蔽暗格中，你发现一本用防水布包裹的日记，属于一位名叫\"埃利斯\"的殖民者。日记的日期横跨多个年份，但笔迹逐渐从工整变得癫狂。', '日记中写道：\"他们说我是疯子，说我听见的\'低语\'是幻觉。但我听得越来越清楚——那不是风声，是人声。成千上万的人声，在每一次暴雪来临前一起哭泣。他们告诉我，暴雪不是天气，是\'投票\'。当岛上的人互相残杀到一定数量，暴雪就来了。他们称之为\'收割合格样本\'。\"你合上日记时，耳边似乎真的掠过一丝若有若无的啜泣。');
INSERT INTO `island_event` VALUES ('60', '2026-06-24 18:03:54.505000', '地点描述：旧堡垒地下指挥室，一张倒塌的桌子旁，散落着发霉的羊皮纸和破碎的望远镜。\n可获得物资：金属制品（50kg） ， 维修工具包（1个）\n历史秘密碎片：你找到一份用三种语言写成的命令书，其中最后一段是：\"封死所有闸门。不要让\'扬帆者\'回来。他们看到的\'新大陆\'是假象，是诱饵。这个岛就是全部，墙外只有雪和眼睛。宁可死在墙内，也别死在墙外让他们\'观察\'。\"命令书底部有一个血手印，旁边写着：\"但我们没有守住。他们在墙上画了那个眼睛，门就从里面开了。\"\n难度：8', '筑墙者最后的命令', 'uncommon', '\0', '2026-06-24 18:03:54.505000', '8', '\0', '旧堡垒地下指挥室，一张倒塌的桌子旁，散落着发霉的羊皮纸和破碎的望远镜。', '你找到一份用三种语言写成的命令书，其中最后一段是：\"封死所有闸门。不要让\'扬帆者\'回来。他们看到的\'新大陆\'是假象，是诱饵。这个岛就是全部，墙外只有雪和眼睛。宁可死在墙内，也别死在墙外让他们\'观察\'。\"命令书底部有一个血手印，旁边写着：\"但我们没有守住。他们在墙上画了那个眼睛，门就从里面开了。\"');
INSERT INTO `island_event` VALUES ('61', '2026-06-24 18:03:54.520000', '地点描述：在沉船残骸最底层的密封铅桶中，你找到一卷被蜡封的航海图，比之前找到的任何海图都更古老。\n可获得物资：帆布（3米） ， 绳索（12米）\n历史秘密碎片：海图上标注着一片你从未见过的海域，方向指向\"外界\"。但所有航线最终都折返回岛屿，形成一个闭环。图边用极细的笔迹写着：\"我们从未离开。每一次\'远航\'，都只是绕了一个大圈，回到原点。海平线是弯曲的，这个岛是碗底，我们被盛在里面。\"你突然意识到，所谓\"摆渡船\"接引亡魂前往的\"观察席\"，可能就在这个碗沿上——看得见，却永远够不到。\n难度：8', '扬帆者的最后航海图', 'uncommon', '\0', '2026-06-24 18:03:54.520000', '8', '\0', '在沉船残骸最底层的密封铅桶中，你找到一卷被蜡封的航海图，比之前找到的任何海图都更古老。', '海图上标注着一片你从未见过的海域，方向指向\"外界\"。但所有航线最终都折返回岛屿，形成一个闭环。图边用极细的笔迹写着：\"我们从未离开。每一次\'远航\'，都只是绕了一个大圈，回到原点。海平线是弯曲的，这个岛是碗底，我们被盛在里面。\"你突然意识到，所谓\"摆渡船\"接引亡魂前往的\"观察席\"，可能就在这个碗沿上——看得见，却永远够不到。');
INSERT INTO `island_event` VALUES ('62', '2026-06-24 18:03:54.540000', '地点描述：一处被巨树和藤蔓完全遮蔽的地下天然井，井底有一块黑色的、被磨得光滑如镜的石板。石板周围散落着风化的骨骸和锈蚀的仪式刀具。\n可获得物资： 医疗资源（2单位）\n历史秘密碎片：你发现石板表面刻满了名字——每一个名字后面都画着一个\"✓\"。最新的一行写着：\"第6轮——埃里克·哈里斯（自愿）。\"更下方有一行暗红色的字：\"杀戮不是目的，是献祭。每一滴血都在向那个眼睛证明：这个文明的强度足以进入\'下一阶段\'。但我们从未通过。我们总是在筑墙和远航之间争吵，直到雪来。血祭者只是……加速了这个过程。\"你盯着那面光滑的石板，忽然在反光中看到自己的脸——但你身后，似乎站着一个模糊的、不属于你的影子。\n难度：13', '血祭者的祭坛（禁忌地点）', 'rare', '\0', '2026-06-24 18:03:54.540000', '13', '\0', '一处被巨树和藤蔓完全遮蔽的地下天然井，井底有一块黑色的、被磨得光滑如镜的石板。石板周围散落着风化的骨骸和锈蚀的仪式刀具。', '你发现石板表面刻满了名字——每一个名字后面都画着一个\"✓\"。最新的一行写着：\"第6轮——埃里克·哈里斯（自愿）。\"更下方有一行暗红色的字：\"杀戮不是目的，是献祭。每一滴血都在向那个眼睛证明：这个文明的强度足以进入\'下一阶段\'。但我们从未通过。我们总是在筑墙和远航之间争吵，直到雪来。血祭者只是……加速了这个过程。\"你盯着那面光滑的石板，忽然在反光中看到自己的脸——但你身后，似乎站着一个模糊的、不属于你的影子。');
INSERT INTO `island_event` VALUES ('63', '2026-06-24 18:03:54.553000', '地点描述：结冰的湖面中央，冰层下隐约可见一串串气泡被冻住，形状像是……一张张向上张开的嘴。\n可获得物资：食物（5单位） ， 木材（1吨）\n历史秘密碎片：你跪在冰面上，冰下的气泡让你想起\"潮汐症\"患者描述的那种窒息感。当你把耳朵贴在冰面时，你听到了——不是风声，而是无数重叠的呢喃：\"别筑墙……别造船……别杀人……别重复……\"声音在\"重复\"这个词上叠加了无数个回声，像是几百个周期、几千个亡魂在同声控诉。你猛地抬起头，四周一片寂静，只有风吹过湖面的呜咽。\n难度：18', '潮汐症共鸣点——冰封湖面', 'legendary', '\0', '2026-06-24 18:03:54.553000', '18', '\0', '结冰的湖面中央，冰层下隐约可见一串串气泡被冻住，形状像是……一张张向上张开的嘴。', '你跪在冰面上，冰下的气泡让你想起\"潮汐症\"患者描述的那种窒息感。当你把耳朵贴在冰面时，你听到了——不是风声，而是无数重叠的呢喃：\"别筑墙……别造船……别杀人……别重复……\"声音在\"重复\"这个词上叠加了无数个回声，像是几百个周期、几千个亡魂在同声控诉。你猛地抬起头，四周一片寂静，只有风吹过湖面的呜咽。');
INSERT INTO `island_event` VALUES ('64', '2026-06-24 18:03:54.569000', '地点描述：山顶一座早已废弃的无线电广播站，天线已经折断，设备全部被拆空。角落里有一个掉落的铁皮柜。\n可获得物资：金属制品（30kg） ， 火柴（5盒）\n历史秘密碎片：你撬开铁皮柜，发现一盘老旧的钢丝录音带和一台手摇播放器。你播放录音，里面传来一个男人的声音，语气疲惫而绝望：\"……这里是本岛第5轮次广播。如果你听到这段录音，说明我们又一次失败了。我要告诉后来者一件重要的事：那个眼睛不是神，也不是自然现象——是镜子。我们凝视它，它就在我们心里生成一轮新的暴雪。打破循环的唯一方法，是停止凝视。但谁能做到呢？恐惧让我们不得不凝视。\"录音到这里中断，伴随着一阵刺耳的啸叫。\n难度：9', '被遗忘的广播站', 'uncommon', '\0', '2026-06-24 18:03:54.569000', '9', '\0', '山顶一座早已废弃的无线电广播站，天线已经折断，设备全部被拆空。角落里有一个掉落的铁皮柜。', '你撬开铁皮柜，发现一盘老旧的钢丝录音带和一台手摇播放器。你播放录音，里面传来一个男人的声音，语气疲惫而绝望：\"……这里是本岛第5轮次广播。如果你听到这段录音，说明我们又一次失败了。我要告诉后来者一件重要的事：那个眼睛不是神，也不是自然现象——是镜子。我们凝视它，它就在我们心里生成一轮新的暴雪。打破循环的唯一方法，是停止凝视。但谁能做到呢？恐惧让我们不得不凝视。\"录音到这里中断，伴随着一阵刺耳的啸叫。');
INSERT INTO `island_event` VALUES ('65', '2026-06-24 18:03:54.585000', '地点描述：在堡垒遗址的内院中，一块被积雪和石板覆盖的区域下，你发现了土壤和枯萎的藤蔓——这是一座被故意掩埋的花园。\n可获得物资： 草药（8单位）\n历史秘密碎片：你发现花园角落埋着一块石碑，上面刻着：\"筑墙者最后的秘密：我们不仅筑墙抵御暴雪，我们筑墙掩盖真相——这座岛的地下，埋着最初那个\'种子\'。第一个来到这里的人，用某种东西种下了这场循环。如果我们能把它挖出来，也许就能结束一切。但我们害怕挖出的东西，所以我们选择了建墙在上面。\"你的目光不由自主地看向脚下的大地。\n难度：5', '被雪掩埋的花园（筑墙者遗产）', 'common', '\0', '2026-06-24 18:03:54.585000', '5', '\0', '在堡垒遗址的内院中，一块被积雪和石板覆盖的区域下，你发现了土壤和枯萎的藤蔓——这是一座被故意掩埋的花园。', '你发现花园角落埋着一块石碑，上面刻着：\"筑墙者最后的秘密：我们不仅筑墙抵御暴雪，我们筑墙掩盖真相——这座岛的地下，埋着最初那个\'种子\'。第一个来到这里的人，用某种东西种下了这场循环。如果我们能把它挖出来，也许就能结束一切。但我们害怕挖出的东西，所以我们选择了建墙在上面。\"你的目光不由自主地看向脚下的大地。');
INSERT INTO `island_event` VALUES ('66', '2026-06-24 18:03:54.599000', '地点描述：小镇中心钟楼的顶层，大钟表面结满了厚厚的冰霜，指针永远停在11:58——差两分钟就到午夜。\n可获得物资：金属制品（100kg） ， 绳索（8米）\n历史秘密碎片：你在钟楼壁板上发现一行被反复刻划的字：\"永远差两分钟。上一次暴雪来临时，钟停了。再上一次也是。每一次都停在11:58。两分钟，就是雪落下来、岛被洗白的时间。\"下方有人用红笔加上了一行：\"如果我们能在11:58之前把钟敲响，也许时间就能继续走下去。但谁来敲？谁敢敲？\"\n难度：19', '被冻住的钟楼', 'legendary', '\0', '2026-06-24 18:03:54.599000', '19', '\0', '小镇中心钟楼的顶层，大钟表面结满了厚厚的冰霜，指针永远停在11:58——差两分钟就到午夜。', '你在钟楼壁板上发现一行被反复刻划的字：\"永远差两分钟。上一次暴雪来临时，钟停了。再上一次也是。每一次都停在11:58。两分钟，就是雪落下来、岛被洗白的时间。\"下方有人用红笔加上了一行：\"如果我们能在11:58之前把钟敲响，也许时间就能继续走下去。但谁来敲？谁敢敲？\"');

-- ----------------------------
-- Table structure for island_event_reward
-- ----------------------------
DROP TABLE IF EXISTS `island_event_reward`;
CREATE TABLE `island_event_reward` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `condition_desc` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `event_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `item_type` varchar(20) NOT NULL,
  `quantity` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=135 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of island_event_reward
-- ----------------------------
INSERT INTO `island_event_reward` VALUES ('45', null, '2026-06-24 18:03:53.702000', '17', '3', 'item', '10');
INSERT INTO `island_event_reward` VALUES ('46', null, '2026-06-24 18:03:53.709000', '17', '6', 'item', '1');
INSERT INTO `island_event_reward` VALUES ('47', null, '2026-06-24 18:03:53.730000', '18', '1', 'material', '5');
INSERT INTO `island_event_reward` VALUES ('48', null, '2026-06-24 18:03:53.739000', '18', '5', 'weapon', '1');
INSERT INTO `island_event_reward` VALUES ('49', null, '2026-06-24 18:03:53.754000', '19', '5', 'material', '8');
INSERT INTO `island_event_reward` VALUES ('50', null, '2026-06-24 18:03:53.763000', '19', '9', 'material', '2');
INSERT INTO `island_event_reward` VALUES ('51', null, '2026-06-24 18:03:53.778000', '20', '1', 'item', '5');
INSERT INTO `island_event_reward` VALUES ('52', null, '2026-06-24 18:03:53.784000', '20', '8', 'material', '2');
INSERT INTO `island_event_reward` VALUES ('53', null, '2026-06-24 18:03:53.798000', '21', '2', 'material', '5000');
INSERT INTO `island_event_reward` VALUES ('54', null, '2026-06-24 18:03:53.807000', '21', '1', 'material', '3000');
INSERT INTO `island_event_reward` VALUES ('55', null, '2026-06-24 18:03:53.815000', '21', '8', 'material', '2');
INSERT INTO `island_event_reward` VALUES ('56', null, '2026-06-24 18:03:53.838000', '23', '1', 'material', '8');
INSERT INTO `island_event_reward` VALUES ('57', null, '2026-06-24 18:03:53.844000', '23', '2', 'material', '5000');
INSERT INTO `island_event_reward` VALUES ('58', null, '2026-06-24 18:03:53.859000', '24', '2', 'material', '5000');
INSERT INTO `island_event_reward` VALUES ('59', null, '2026-06-24 18:03:53.873000', '25', '2', 'ammo', '4');
INSERT INTO `island_event_reward` VALUES ('60', null, '2026-06-24 18:03:53.891000', '26', '1', 'item', '1');
INSERT INTO `island_event_reward` VALUES ('61', null, '2026-06-24 18:03:53.898000', '26', '2', 'item', '1');
INSERT INTO `island_event_reward` VALUES ('62', null, '2026-06-24 18:03:53.913000', '27', '1', 'material', '1000');
INSERT INTO `island_event_reward` VALUES ('63', null, '2026-06-24 18:03:53.919000', '27', '14', 'material', '10');
INSERT INTO `island_event_reward` VALUES ('64', null, '2026-06-24 18:03:53.933000', '28', '5', 'material', '5');
INSERT INTO `island_event_reward` VALUES ('65', null, '2026-06-24 18:03:53.941000', '28', '3', 'item', '5');
INSERT INTO `island_event_reward` VALUES ('66', null, '2026-06-24 18:03:53.969000', '30', '1', 'material', '10');
INSERT INTO `island_event_reward` VALUES ('67', null, '2026-06-24 18:03:53.984000', '31', '1', 'material', '1000');
INSERT INTO `island_event_reward` VALUES ('68', null, '2026-06-24 18:03:53.991000', '31', '8', 'material', '8');
INSERT INTO `island_event_reward` VALUES ('69', null, '2026-06-24 18:03:54.001000', '31', '3', 'item', '5');
INSERT INTO `island_event_reward` VALUES ('70', null, '2026-06-24 18:03:54.016000', '32', '2', 'material', '10000');
INSERT INTO `island_event_reward` VALUES ('71', null, '2026-06-24 18:03:54.022000', '32', '6', 'material', '10');
INSERT INTO `island_event_reward` VALUES ('72', null, '2026-06-24 18:03:54.036000', '33', '6', 'material', '10');
INSERT INTO `island_event_reward` VALUES ('73', null, '2026-06-24 18:03:54.042000', '33', '9', 'material', '3');
INSERT INTO `island_event_reward` VALUES ('74', null, '2026-06-24 18:03:54.047000', '33', '3', 'item', '5');
INSERT INTO `island_event_reward` VALUES ('75', null, '2026-06-24 18:03:54.060000', '34', '10', 'item', '3');
INSERT INTO `island_event_reward` VALUES ('76', null, '2026-06-24 18:03:54.073000', '35', '1', 'item', '2');
INSERT INTO `island_event_reward` VALUES ('77', null, '2026-06-24 18:03:54.085000', '36', '8', 'item', '1');
INSERT INTO `island_event_reward` VALUES ('78', null, '2026-06-24 18:03:54.099000', '37', '1', 'item', '10');
INSERT INTO `island_event_reward` VALUES ('79', null, '2026-06-24 18:03:54.104000', '37', '8', 'material', '5');
INSERT INTO `island_event_reward` VALUES ('80', null, '2026-06-24 18:03:54.117000', '38', '2', 'material', '2000');
INSERT INTO `island_event_reward` VALUES ('81', null, '2026-06-24 18:03:54.122000', '38', '8', 'material', '2');
INSERT INTO `island_event_reward` VALUES ('82', null, '2026-06-24 18:03:54.129000', '38', '13', 'item', '3');
INSERT INTO `island_event_reward` VALUES ('83', null, '2026-06-24 18:03:54.143000', '39', '16', 'item', '4');
INSERT INTO `island_event_reward` VALUES ('84', null, '2026-06-24 18:03:54.150000', '39', '9', 'item', '5');
INSERT INTO `island_event_reward` VALUES ('85', null, '2026-06-24 18:03:54.164000', '40', '1', 'material', '20');
INSERT INTO `island_event_reward` VALUES ('86', null, '2026-06-24 18:03:54.170000', '40', '3', 'item', '8');
INSERT INTO `island_event_reward` VALUES ('87', null, '2026-06-24 18:03:54.177000', '40', '12', 'item', '1');
INSERT INTO `island_event_reward` VALUES ('88', null, '2026-06-24 18:03:54.195000', '41', '5', 'material', '5');
INSERT INTO `island_event_reward` VALUES ('89', null, '2026-06-24 18:03:54.202000', '41', '1', 'item', '2');
INSERT INTO `island_event_reward` VALUES ('90', null, '2026-06-24 18:03:54.216000', '42', '2', 'material', '1000');
INSERT INTO `island_event_reward` VALUES ('91', null, '2026-06-24 18:03:54.224000', '42', '9', 'material', '2');
INSERT INTO `island_event_reward` VALUES ('92', null, '2026-06-24 18:03:54.236000', '43', '11', 'item', '3');
INSERT INTO `island_event_reward` VALUES ('93', null, '2026-06-24 18:03:54.250000', '44', '7', 'material', '10000');
INSERT INTO `island_event_reward` VALUES ('94', null, '2026-06-24 18:03:54.262000', '45', '1', 'material', '50');
INSERT INTO `island_event_reward` VALUES ('95', null, '2026-06-24 18:03:54.267000', '45', '2', 'material', '3000');
INSERT INTO `island_event_reward` VALUES ('96', null, '2026-06-24 18:03:54.279000', '46', '1', 'material', '8000');
INSERT INTO `island_event_reward` VALUES ('97', null, '2026-06-24 18:03:54.285000', '46', '8', 'material', '12');
INSERT INTO `island_event_reward` VALUES ('98', null, '2026-06-24 18:03:54.291000', '46', '11', 'material', '1');
INSERT INTO `island_event_reward` VALUES ('99', null, '2026-06-24 18:03:54.302000', '47', '2', 'ammo', '4');
INSERT INTO `island_event_reward` VALUES ('100', null, '2026-06-24 18:03:54.308000', '47', '5', 'material', '6');
INSERT INTO `island_event_reward` VALUES ('101', null, '2026-06-24 18:03:54.313000', '47', '2', 'weapon', '1');
INSERT INTO `island_event_reward` VALUES ('102', null, '2026-06-24 18:03:54.325000', '48', '1', 'material', '5000');
INSERT INTO `island_event_reward` VALUES ('103', null, '2026-06-24 18:03:54.329000', '48', '2', 'item', '1');
INSERT INTO `island_event_reward` VALUES ('104', null, '2026-06-24 18:03:54.341000', '49', '3', 'item', '30');
INSERT INTO `island_event_reward` VALUES ('105', null, '2026-06-24 18:03:54.346000', '49', '14', 'material', '5');
INSERT INTO `island_event_reward` VALUES ('106', null, '2026-06-24 18:03:54.357000', '50', '8', 'item', '1');
INSERT INTO `island_event_reward` VALUES ('107', null, '2026-06-24 18:03:54.362000', '50', '6', 'item', '2');
INSERT INTO `island_event_reward` VALUES ('108', null, '2026-06-24 18:03:54.373000', '51', '1', 'material', '1000');
INSERT INTO `island_event_reward` VALUES ('109', null, '2026-06-24 18:03:54.384000', '52', '2', 'material', '2000');
INSERT INTO `island_event_reward` VALUES ('110', null, '2026-06-24 18:03:54.389000', '52', '6', 'material', '5');
INSERT INTO `island_event_reward` VALUES ('111', null, '2026-06-24 18:03:54.400000', '53', '1', 'item', '1');
INSERT INTO `island_event_reward` VALUES ('112', null, '2026-06-24 18:03:54.405000', '53', '5', 'material', '5');
INSERT INTO `island_event_reward` VALUES ('113', null, '2026-06-24 18:03:54.410000', '53', '14', 'material', '5');
INSERT INTO `island_event_reward` VALUES ('114', null, '2026-06-24 18:03:54.427000', '54', '10', 'item', '2');
INSERT INTO `island_event_reward` VALUES ('115', null, '2026-06-24 18:03:54.443000', '56', '14', 'material', '10');
INSERT INTO `island_event_reward` VALUES ('116', null, '2026-06-24 18:03:54.455000', '57', '3', 'item', '6');
INSERT INTO `island_event_reward` VALUES ('117', null, '2026-06-24 18:03:54.461000', '57', '1', 'material', '50');
INSERT INTO `island_event_reward` VALUES ('118', null, '2026-06-24 18:03:54.472000', '58', '1', 'material', '200');
INSERT INTO `island_event_reward` VALUES ('119', null, '2026-06-24 18:03:54.477000', '58', '3', 'item', '12');
INSERT INTO `island_event_reward` VALUES ('120', null, '2026-06-24 18:03:54.482000', '58', '8', 'material', '4');
INSERT INTO `island_event_reward` VALUES ('121', null, '2026-06-24 18:03:54.494000', '59', '1', 'item', '4');
INSERT INTO `island_event_reward` VALUES ('122', null, '2026-06-24 18:03:54.499000', '59', '10', 'item', '1');
INSERT INTO `island_event_reward` VALUES ('123', null, '2026-06-24 18:03:54.511000', '60', '1', 'material', '50');
INSERT INTO `island_event_reward` VALUES ('124', null, '2026-06-24 18:03:54.516000', '60', '8', 'item', '1');
INSERT INTO `island_event_reward` VALUES ('125', null, '2026-06-24 18:03:54.527000', '61', '9', 'material', '3');
INSERT INTO `island_event_reward` VALUES ('126', null, '2026-06-24 18:03:54.534000', '61', '3', 'item', '12');
INSERT INTO `island_event_reward` VALUES ('127', null, '2026-06-24 18:03:54.547000', '62', '1', 'item', '2');
INSERT INTO `island_event_reward` VALUES ('128', null, '2026-06-24 18:03:54.558000', '63', '5', 'material', '5');
INSERT INTO `island_event_reward` VALUES ('129', null, '2026-06-24 18:03:54.564000', '63', '2', 'material', '1000');
INSERT INTO `island_event_reward` VALUES ('130', null, '2026-06-24 18:03:54.575000', '64', '1', 'material', '30');
INSERT INTO `island_event_reward` VALUES ('131', null, '2026-06-24 18:03:54.580000', '64', '13', 'item', '5');
INSERT INTO `island_event_reward` VALUES ('132', null, '2026-06-24 18:03:54.594000', '65', '16', 'item', '8');
INSERT INTO `island_event_reward` VALUES ('133', null, '2026-06-24 18:03:54.605000', '66', '1', 'material', '100');
INSERT INTO `island_event_reward` VALUES ('134', null, '2026-06-24 18:03:54.611000', '66', '3', 'item', '8');

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
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of item
-- ----------------------------
INSERT INTO `item` VALUES ('1', '医疗包', '个', '装有绷带、消毒剂和止血带的急救包裹。每包提供10点医疗资源，可治疗伤口或稳定重伤员。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('2', '手电筒', '个', '金属外壳的便携照明工具，使用煤油或电池。夜间行动的基础工具。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('3', '手铐', '个', '铁制约束器具，可将一名无反抗能力的玩家束缚。被束缚者无法进行大部分行动，直到被释放。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('4', '哨子', '个', '铜制哨子，声音尖锐可传遍全岛。吹响后会在公屏显示哨音，可用于报警或召集同伴。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('5', '防弹衣', '件', '复合金属材质制成的防护背心。在暴力冲突中可将一次「枪击」降级为「受伤」，或将一次「受伤」无效化，每场冲突限用一次。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('6', '复合盾', '个', '轻质金属与帆布复合的防暴盾牌。一次「受伤」无效化，每场冲突限用一次。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('7', '信号枪', '把', '维里式单发信号枪，可发射彩色信号弹。发射后由主持人在公屏展示信号内容（不超过5个字），可用于求援或传递简单信息。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('8', '维修工具包', '个', '内含基础维修工具。每包提供10点维修资源，可修复损坏的机械或设施。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('9', '协议书', '个', '带法律效力的空白契约纸。玩家之间可自行签订契约协议，用协议书签订的协议不公开契约信息。违约将受到约定惩罚。每名玩家每天最多成为契约者一次。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('10', '朗姆酒', '瓶', '甘蔗酿造的烈酒，酒精度约40%。5瓶朗姆酒可以消除疲劳。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('11', '草药', '个', '岛上采集的药用植物，经干燥处理后保存。可用于简易治疗，等于3医疗资源。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('12', '渔网', '张', '麻绳编织的捕鱼网具。渔民可用其与渔船一起进行渔猎行动。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('13', '蜡烛', '根', '动物油脂或蜂蜡制成的照明物。提供基础夜间照明，比煤油更安静但亮度较低。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('14', '医用酒精', '升', '75%浓度的消毒酒精，每升可用于提供5点医疗资源。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('15', '火柴', '盒', '防水密封包装的火柴，每盒约50根。生火做饭、点灯取暖的基础消耗品。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('16', '铅笔', '盒', '木质铅笔。可用于书写信件、记录信息或绘制简易地图。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('17', '破损海图', '张', '年代久远的航海图，部分区域已损毁或褪色。仍可辨认大致航线与岛屿位置，是远洋航行的重要参考。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `item` VALUES ('18', '便当', '份', '面包师精心制作的便携餐食，内含面包、肉干和腌制蔬菜，营养均衡且便于携带。食用后可获得额外1个白天行动点，每人每天限吃一份。可以储存和交易，是后期高密度行动的重要战略资源。便当本身不提供热量，其核心价值在于让人挤出更多时间做事，而非填饱肚子。', '2026-04-27 11:36:23', '2026-05-02 19:26:21');
INSERT INTO `item` VALUES ('19', '矿场仓库钥匙', '把', '矿场仓库通行', '2026-05-14 21:23:51', '2026-05-17 00:00:00');
INSERT INTO `item` VALUES ('20', '燃料仓库钥匙', '把', '燃料仓库通行', '2026-05-14 21:24:04', '2026-05-14 21:24:22');
INSERT INTO `item` VALUES ('21', '镇武库钥匙', '把', '镇武库通行', '2026-05-14 21:24:57', '2026-05-14 21:24:57');
INSERT INTO `item` VALUES ('22', '码头集换站钥匙', '把', '码头集购站通行', '2026-05-14 21:25:32', '2026-05-14 21:25:32');
INSERT INTO `item` VALUES ('23', '反叛者基地钥匙', '把', '反叛者的物资基地', '2026-05-14 21:26:03', '2026-05-14 21:26:03');
INSERT INTO `item` VALUES ('24', '方舟钥匙', '把', '冒险者的物资基地', '2026-05-14 21:32:41', '2026-05-14 21:34:36');
INSERT INTO `item` VALUES ('25', '火把', '把', '手工艺人的作品，可以用来引燃也可作为探索道具', '2026-06-24 19:16:52', '2026-06-24 19:16:52');
INSERT INTO `item` VALUES ('26', '诅咒硬币', '个', '绘制着蛇眼的硬币，无法丢弃', '2026-06-24 22:31:35', '2026-06-24 22:31:35');

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
  `description` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of job
-- ----------------------------
INSERT INTO `job` VALUES ('1', '镇长', '射击', '2026-04-26 22:13:35', '2026-05-02 22:44:25', '在密谋、暴力冲突中，如果装备远距离武器增加威胁值。如果调查玩家，可以选择在夜晚阶段骰 d6 寻找攻击机会，成功可发起小规模冲突；多人调查可袭击。');
INSERT INTO `job` VALUES ('2', '监狱长', '格斗,急救', '2026-04-26 22:13:35', '2026-05-02 22:44:37', '格斗：在密谋、暴力冲突中，如果装备近距离武器增加威胁值。急救：花费 5 医疗资源，将“枪伤”改为“受伤”标记。');
INSERT INTO `job` VALUES ('3', '警长', '射击', '2026-04-26 22:13:35', '2026-05-02 22:44:47', '在密谋、暴力冲突中，如果装备远距离武器增加威胁值。如果调查玩家，可以选择在夜晚阶段骰 d6 寻找攻击机会，成功可发起小规模冲突；多人调查可袭击。');
INSERT INTO `job` VALUES ('4', '隐藏统治者', '无', '2026-04-26 22:13:35', '2026-05-02 22:44:57', '无职业技能。');
INSERT INTO `job` VALUES ('6', '民兵', '格斗', '2026-05-02 22:47:15', '2026-05-02 22:47:15', '格斗：在密谋、暴力冲突中，如果装备近距离武器增加威胁值；调查玩家时夜晚骰 d6 寻找攻击机会，成功可发起小规模冲突；多人调查可袭击。');
INSERT INTO `job` VALUES ('7', '巡夜人', '格斗,巡逻', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '格斗：在密谋、暴力冲突中，如果装备近距离武器增加威胁值。如果调查玩家，可以选择在夜晚阶段骰 d6 寻找攻击机会，成功可发起小规模冲突；多人调查可袭击。巡逻：夜间行动，选择一个地点，当晚该地点内非统治者阵营玩家夜晚行动成功率 -30%，巡夜人知晓其行动类型；需要当天未处于“劳工/过劳”。');
INSERT INTO `job` VALUES ('8', '农户', '食物生产', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '使用牲畜设施获得食物 15 单位；或使用自留田设施获得 30 斤面粉。');
INSERT INTO `job` VALUES ('9', '伐木工', '伐木', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '使用斧头获得 10 吨原木；使用电锯获得 30 吨原木。');
INSERT INTO `job` VALUES ('10', '矿工', '挖掘', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '使用电钻获得 20 吨石料，否则 5 吨；该技能可额外增加避难所建造进度。');
INSERT INTO `job` VALUES ('11', '铁匠', '炼铁', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '职业与技能在不满 48 人时不开放；具体效果未在资料中详细说明。');
INSERT INTO `job` VALUES ('12', '手工艺人', '手工艺', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '使用工具制备单制作材料/物品/武器；制作列表包括皮带弹袋、复合盾、鱼叉矛、规制箭矢和弓弩等。');
INSERT INTO `job` VALUES ('13', '工匠', '木石工艺', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '使用木板蒸汽箱将原木转化为木板；或使用切石机将石料转化为石墙。');
INSERT INTO `job` VALUES ('14', '渔民', '捕鱼,格斗', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '捕鱼：码头使用渔船设施，获得食物 10 单位或 20kg 鱼肉。格斗：在密谋、暴力冲突中，如果装备近距离武器增加威胁值。如果调查玩家，可以选择在夜晚阶段骰 d6 寻找攻击机会，成功可发起小规模冲突；多人调查可袭击。');
INSERT INTO `job` VALUES ('15', '水手', '航海,格斗', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '航海：码头使用货船设施获得 10kg 鱼肉；亦为远洋航行必要技能，对冒险者阵营有特殊用途。格斗：在密谋、暴力冲突中，如果装备近距离武器增加威胁值。如果调查玩家，可以选择在夜晚阶段骰 d6 寻找攻击机会，成功可发起小规模冲突；多人调查可袭击。');
INSERT INTO `job` VALUES ('16', '船长', '远洋导航,射击', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '远洋导航：为方舟终局提供更好的结局倾向，并查看所有天灾牌。射击：在密谋、暴力冲突中，如果装备远距离武器增加威胁值。如果调查玩家，可以选择在夜晚阶段骰 d6 寻找攻击机会，成功可发起小规模冲突；多人调查可袭击。');
INSERT INTO `job` VALUES ('17', '装卸工', '搬运,斗殴', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '搬运：搬运量永远是其他职业的两倍。斗殴：具体效果未详细说明（推测为格斗变体）。');
INSERT INTO `job` VALUES ('18', '采珠人', '潜水,捕鱼', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '潜水：每天一次，码头使用，由主持人投 d6：1-5 获得食物 8 单位，6 获得沉船遗物。捕鱼：在码头使用渔船设施，获得食物 10 单位或 20kg 鱼肉。');
INSERT INTO `job` VALUES ('19', '神父', '布道,医疗', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '布道：每天一次，选择最多 3 人，使其下一个行动生产 +50%；或为最多 3 人消除诅咒。医疗：每天一次，选择一位或至多 5 位“受伤”玩家，花费 3 医疗资源每人消除“受伤”；选择一位或多位“过劳”玩家，花费 2 医疗资源每人消除“过劳”。');
INSERT INTO `job` VALUES ('20', '赤脚医生', '医疗,急救', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '医疗：每天一次，选择一位或至多 5 位“受伤”玩家，花费 3 医疗资源每人消除“受伤”；选择一位或多位“过劳”玩家，花费 2 医疗资源每人消除“过劳”。急救：花费 5 医疗资源，将“枪伤”改为“受伤”标记。');
INSERT INTO `job` VALUES ('21', '杂货店主', '无', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '特殊性：初始拥有大量资源和商店功能；无职业技能。');
INSERT INTO `job` VALUES ('22', '旅店店主', '烘焙', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '烘焙：需要 5 单位食物与 15kg 木材制作 1 份便当；便当当天额外获得 1 个白天行动点，每人每天限 1 次。');
INSERT INTO `job` VALUES ('23', '酒馆老板', '无', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '特殊性：初始拥有大量酒类与医用酒精；无职业技能。');
INSERT INTO `job` VALUES ('24', '灯塔看守员', '射击', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '射击：在密谋、暴力冲突中，如果装备远距离武器增加威胁值。如果调查玩家，可以选择在夜晚阶段骰 d6 寻找攻击机会，成功可发起小规模冲突；多人调查可袭击。');
INSERT INTO `job` VALUES ('25', '面包师', '烘焙', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '烘焙：需要 5 单位食物与 15kg 木材制作 1 份便当；便当当天额外获得 1 个白天行动点，每人每天限 1 次。');
INSERT INTO `job` VALUES ('26', '向导', '急救,潜行', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '急救：花费 5 医疗资源，将“枪伤”改为“受伤”标记。潜行：为谋略增加成功率，且无法被调查。');
INSERT INTO `job` VALUES ('27', '猎户', '射击,潜行', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '射击：在密谋、暴力冲突中，如果装备远距离武器增加威胁值。如果调查玩家，可以选择在夜晚阶段骰 d6 寻找攻击机会，成功可发起小规模冲突；多人调查可袭击。潜行：为谋略增加成功率，且无法被调查。');
INSERT INTO `job` VALUES ('28', '邮递员', '潜行', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '潜行：为谋略增加成功率，且无法被调查。');
INSERT INTO `job` VALUES ('29', '守墓人', '通灵', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '每天一次，与一位死亡玩家建立私信交流，由主持人保底提供信息。');
INSERT INTO `job` VALUES ('30', '气象观测员', '天气预测', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '你通过科学手段发现异常暴雪来临；可查看所有天灾牌，并为终局结算提供更好的结局倾向。');
INSERT INTO `job` VALUES ('31', '占卜师', '占星', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '为终局结算提供更好的结局倾向；可抽取一张天灾牌撕毁使其不生效，或让一张事件牌撕毁不生效。');
INSERT INTO `job` VALUES ('32', '设施维护人', '维修', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '每天一次，修复一个被破坏的设施，花费 5 维修资源。');
INSERT INTO `job` VALUES ('33', '教师', '启蒙', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '每天一次，消耗一盒粉笔与 2 支铅笔，选择除自己外最多 2 名玩家，使其临时学会一项基础技能持续到次日白天结束；若已拥有则增产 50%。');
INSERT INTO `job` VALUES ('34', '治安官', '射击', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '在密谋、暴力冲突中，如果装备远距离武器增加威胁值。如果调查玩家，可以选择在夜晚阶段骰 d6 寻找攻击机会，成功可发起小规模冲突；多人调查可袭击。');
INSERT INTO `job` VALUES ('35', '女巫', '协议契约', '2026-05-02 22:49:32', '2026-05-02 22:49:32', '为一位或多位玩家建立协议契约，可以指定违反协议的惩罚内容。');

-- ----------------------------
-- Table structure for job_initial_items
-- ----------------------------
DROP TABLE IF EXISTS `job_initial_items`;
CREATE TABLE `job_initial_items` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `job_id` int(11) NOT NULL COMMENT '职业ID',
  `item_type` enum('item','weapon','ammo','material') NOT NULL COMMENT '物品类型',
  `item_id` int(11) NOT NULL COMMENT '物品ID',
  `quantity` int(11) NOT NULL DEFAULT '1' COMMENT '初始数量',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_job_item` (`job_id`,`item_type`,`item_id`),
  KEY `idx_job_id` (`job_id`),
  CONSTRAINT `job_initial_items_ibfk_1` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=226 DEFAULT CHARSET=utf8mb4 COMMENT='职业初始资源表';

-- ----------------------------
-- Records of job_initial_items
-- ----------------------------
INSERT INTO `job_initial_items` VALUES ('1', '1', 'item', '5', '1', '件', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('2', '1', 'weapon', '1', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('3', '1', 'ammo', '1', '6', '枚', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('4', '2', 'item', '3', '2', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('5', '2', 'weapon', '3', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('6', '2', 'item', '5', '1', '件', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('7', '2', 'item', '2', '2', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('8', '2', 'item', '4', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('9', '3', 'weapon', '3', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('10', '3', 'item', '2', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('11', '3', 'item', '4', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('12', '3', 'item', '5', '1', '件', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('13', '3', 'weapon', '1', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('14', '3', 'ammo', '1', '6', '枚', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('15', '4', 'item', '5', '1', '件', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('16', '5', 'item', '3', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('17', '5', 'weapon', '3', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('18', '5', 'item', '5', '1', '件', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('19', '5', 'item', '2', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('20', '5', 'material', '5', '2', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('21', '5', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('22', '5', 'material', '2', '45', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('23', '5', 'weapon', '1', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('24', '5', 'ammo', '1', '2', '枚', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('25', '5', 'item', '1', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('26', '5', 'material', '3', '10', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('27', '5', 'item', '10', '5', '瓶', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('28', '6', 'item', '6', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('29', '6', 'weapon', '3', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('30', '6', 'material', '5', '2', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('31', '6', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('32', '6', 'material', '2', '45', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('33', '6', 'weapon', '1', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('34', '6', 'ammo', '1', '2', '枚', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('35', '6', 'item', '1', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('36', '6', 'material', '3', '10', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('37', '6', 'item', '10', '5', '瓶', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('38', '7', 'item', '4', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('39', '7', 'item', '2', '2', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('40', '7', 'item', '6', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('41', '7', 'item', '13', '10', '根', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('42', '7', 'material', '5', '2', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('43', '7', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('44', '7', 'material', '2', '45', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('45', '7', 'weapon', '1', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('46', '7', 'ammo', '1', '2', '枚', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('47', '7', 'item', '1', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('48', '7', 'material', '3', '10', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('49', '7', 'item', '10', '5', '瓶', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('50', '8', 'material', '5', '13', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('51', '8', 'material', '3', '10', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('52', '8', 'weapon', '9', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('53', '8', 'item', '15', '1', '盒', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('54', '8', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('55', '8', 'material', '2', '150', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('56', '9', 'weapon', '9', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('57', '9', 'weapon', '10', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('58', '9', 'material', '3', '10', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('59', '9', 'item', '2', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('60', '9', 'item', '15', '1', '盒', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('61', '9', 'material', '5', '8', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('62', '9', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('63', '9', 'material', '2', '150', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('64', '10', 'weapon', '8', '3', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('65', '10', 'item', '2', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('66', '10', 'item', '15', '1', '盒', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('67', '10', 'material', '5', '8', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('68', '10', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('69', '10', 'material', '2', '150', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('70', '11', 'weapon', '4', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('71', '11', 'material', '3', '10', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('72', '11', 'item', '15', '1', '盒', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('73', '11', 'material', '5', '8', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('74', '11', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('75', '11', 'material', '2', '150', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('76', '12', 'material', '1', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('77', '12', 'material', '3', '20', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('78', '12', 'material', '9', '10', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('79', '12', 'item', '2', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('80', '12', 'item', '15', '1', '盒', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('81', '12', 'material', '5', '8', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('82', '12', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('83', '12', 'material', '2', '150', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('84', '13', 'material', '5', '8', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('85', '13', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('86', '13', 'material', '2', '150', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('87', '14', 'weapon', '6', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('88', '14', 'item', '12', '1', '张', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('89', '14', 'weapon', '4', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('90', '14', 'material', '3', '20', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('91', '14', 'material', '5', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('92', '14', 'material', '2', '80', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('93', '14', 'material', '8', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('94', '15', 'weapon', '4', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('95', '15', 'material', '3', '30', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('96', '15', 'item', '2', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('97', '15', 'item', '12', '1', '张', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('98', '15', 'material', '5', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('99', '15', 'material', '2', '80', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('100', '15', 'material', '8', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('101', '16', 'weapon', '4', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('102', '16', 'item', '7', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('103', '16', 'ammo', '3', '1', '枚', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('104', '16', 'item', '17', '1', '张', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('105', '16', 'material', '5', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('106', '16', 'material', '2', '80', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('107', '16', 'material', '8', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('108', '16', 'material', '3', '10', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('109', '17', 'material', '3', '30', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('110', '17', 'item', '2', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('111', '17', 'weapon', '4', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('112', '17', 'material', '5', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('113', '17', 'material', '2', '80', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('114', '17', 'material', '8', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('115', '18', 'weapon', '4', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('116', '18', 'item', '12', '1', '张', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('117', '18', 'material', '3', '20', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('118', '18', 'material', '5', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('119', '18', 'material', '2', '80', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('120', '18', 'material', '8', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('121', '19', 'item', '13', '50', '支', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('122', '19', 'item', '15', '5', '盒', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('123', '19', 'material', '3', '10', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('124', '19', 'material', '5', '3', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('125', '19', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('126', '19', 'material', '2', '50', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('127', '19', 'material', '1', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('128', '20', 'item', '1', '2', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('129', '20', 'weapon', '11', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('130', '20', 'material', '3', '10', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('131', '20', 'material', '5', '3', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('132', '20', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('133', '20', 'material', '2', '50', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('134', '20', 'material', '1', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('135', '21', 'material', '5', '23', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('136', '21', 'item', '10', '10', '瓶', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('137', '21', 'material', '3', '100', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('138', '21', 'weapon', '8', '2', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('139', '21', 'weapon', '9', '2', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('140', '21', 'weapon', '2', '2', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('141', '21', 'ammo', '2', '4', '枚', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('142', '21', 'material', '8', '35', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('143', '21', 'material', '2', '350', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('144', '21', 'material', '1', '40', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('145', '21', 'material', '6', '30', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('146', '21', 'material', '9', '30', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('147', '21', 'item', '16', '5', '支', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('148', '22', 'material', '5', '18', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('149', '22', 'material', '2', '550', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('150', '22', 'material', '8', '105', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('151', '23', 'item', '15', '1', '盒', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('152', '23', 'item', '14', '10', '升', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('153', '23', 'material', '5', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('154', '23', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('155', '23', 'material', '2', '50', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('156', '23', 'material', '1', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('157', '24', 'item', '7', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('158', '24', 'ammo', '3', '2', '枚', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('159', '24', 'item', '2', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('160', '24', 'material', '8', '15', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('161', '24', 'item', '17', '1', '张', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('162', '24', 'material', '5', '3', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('163', '24', 'material', '2', '50', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('164', '24', 'material', '1', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('165', '25', 'material', '5', '8', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('166', '25', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('167', '25', 'material', '2', '50', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('168', '25', 'material', '1', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('169', '26', 'weapon', '4', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('170', '26', 'material', '3', '20', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('171', '26', 'item', '2', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('172', '26', 'material', '5', '2', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('173', '26', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('174', '26', 'material', '2', '50', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('175', '26', 'material', '1', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('176', '27', 'weapon', '2', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('177', '27', 'ammo', '2', '4', '枚', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('178', '27', 'weapon', '4', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('179', '27', 'material', '3', '10', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('180', '27', 'material', '5', '2', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('181', '27', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('182', '27', 'material', '2', '50', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('183', '27', 'material', '1', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('184', '28', 'material', '3', '20', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('185', '28', 'item', '2', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('186', '28', 'item', '4', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('187', '28', 'material', '5', '2', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('188', '28', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('189', '28', 'material', '2', '50', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('190', '28', 'material', '1', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('191', '29', 'weapon', '8', '1', '把', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('192', '29', 'item', '2', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('193', '29', 'material', '3', '10', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('194', '29', 'item', '13', '10', '支', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('195', '29', 'material', '5', '2', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('196', '29', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('197', '29', 'material', '2', '50', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('198', '29', 'material', '1', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('199', '30', 'item', '16', '5', '支', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('200', '30', 'material', '8', '20', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('201', '30', 'material', '9', '20', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('202', '30', 'material', '5', '2', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('203', '30', 'material', '2', '50', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('204', '30', 'material', '1', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('205', '31', 'item', '13', '10', '支', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('206', '31', 'item', '15', '1', '盒', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('207', '31', 'material', '3', '10', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('208', '31', 'item', '2', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('209', '31', 'material', '5', '2', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('210', '31', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('211', '31', 'material', '2', '50', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('212', '31', 'material', '1', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('213', '32', 'item', '8', '2', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('214', '32', 'item', '2', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('215', '32', 'material', '3', '10', '米', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('216', '32', 'material', '5', '2', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('217', '32', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('218', '32', 'material', '2', '50', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('219', '32', 'material', '1', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('220', '33', 'item', '16', '5', '支', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('221', '33', 'item', '2', '1', '个', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('222', '33', 'material', '5', '2', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('223', '33', 'material', '8', '5', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('224', '33', 'material', '2', '50', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');
INSERT INTO `job_initial_items` VALUES ('225', '33', 'material', '1', '10', 'kg', '2026-05-02 22:50:05', '2026-05-02 22:50:05');

-- ----------------------------
-- Table structure for location
-- ----------------------------
DROP TABLE IF EXISTS `location`;
CREATE TABLE `location` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '地点名称',
  `area` varchar(20) NOT NULL COMMENT '所属区域：小镇/海岛/特殊',
  `description` text COMMENT '地点描述',
  `defense_value` int(11) NOT NULL DEFAULT '0' COMMENT '防御值',
  `management` varchar(100) DEFAULT NULL COMMENT '管理方',
  `order_number` int(11) NOT NULL DEFAULT '0' COMMENT '排序序号',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COMMENT='地点表';

-- ----------------------------
-- Records of location
-- ----------------------------
INSERT INTO `location` VALUES ('1', '警察局', '小镇', '一座木铁混合结构的平房，瓦楞铁皮屋顶，外墙刷着褪色的白漆。门廊上挂着一盏摇曳的煤油灯，屋内有一张办公桌、一个档案柜和一间狭小的临时牢房。墙上贴着殖民地政府的告示和几张泛黄的通缉令。', '5', null, '1', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('2', '镇长厅', '小镇', '两层殖民风格木楼，带有宽敞的阳台和百叶窗。楼下是办公室和接待室，楼上是行政长官的私人住所。墙上挂着英王乔治六世的肖像和殖民地地图。吊扇无力地转动着。', '5', null, '2', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('3', '邮局', '小镇', '一间低矮的木屋，窗前挂着\"皇家邮政\"的铜牌。屋内满是油墨和纸张的气味，木制柜台后是分拣信件的格子和一台莫尔斯电报机。墙上贴着轮船班次表和邮票样张。', '3', null, '3', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('4', '教堂', '小镇', '一座用珊瑚石和木材建造的小教堂，彩色玻璃窗描绘着基督与太平洋岛屿的景象。尖顶上的十字架在阳光下泛着白漆剥落后的斑驳。教堂内长椅简陋，但祭坛前摆着一架巨大的老旧管风琴，积满灰尘。', '4', null, '4', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('5', '灯塔', '小镇', '矗立在小镇岬角的白色石塔，约20米高。顶部的菲涅尔透镜在夜间旋转，光束扫过海面。塔底是灯塔看守员的住所。', '7', null, '5', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('6', '杂货店', '小镇', '一间堆满杂物的木板屋，从铁钉到糖果应有尽有，但货架已经半空。空气中弥漫着肥皂、腌鱼和煤油的味道。', '0', null, '6', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('7', '码头', '小镇', '有着几间木质简陋房屋和用旧木桩搭建的简陋码头，停着几艘渔船和一艘锈迹斑斑的货船。海浪拍打着木桩，发出单调的响声。远处海平面阴沉沉的。', '3', null, '7', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('8', '方舟', '小镇', '当方舟还未打造成功的那一刻，它更像一具尚未苏醒的骨架。你站在远处望去，最先撞入视线的是那略显粗糙的船头——弧度还不够圆润，几块硬木还露着刨花的毛茬，铜皮只包了一半，另一半用绳子临时捆着。甲板铺了两层，第三层的龙骨刚刚架起来，露出参差的肋木和没钉完的板缝。中间住人的舱室只有墙没有顶，阳光直直落进去，照见地上堆着的锯末和半截木尺；原本要种菜的顶层还空着，几捆芦苇杆斜靠在围栏上，等着编成遮棚。船尾的吊架竖起来了，但小艇还没挂上去，只有一副泡在水里的木架。帆布摊在甲板上，一个女人正低头缝着最后一道边，针脚歪歪扭扭。舵轮刚雕出个兽头的雏形，眼睛还没刻，嘴里衔着一截麻绳——绳的另一端系在岸边的树桩上，免得这半成品被水冲走。整条方舟静静泊在水湾里，随着波浪轻轻起伏，像个还没学会走路的孩子，笨拙，但让人看见力气。', '1', null, '8', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('9', '旅店', '小镇', '两层木质建筑，一楼是嘈杂的公共酒廊，二楼是狭小的客房。壁炉里的火永远烧不旺，空气中弥漫着廉价朗姆酒、汗水和发霉地毯混合的气味。公告板上贴满了寻人启事和过期的船期表。', '2', null, '9', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('10', '集市', '小镇', '镇中心的露天广场，只有零星几个木制摊位。平时冷冷清清，但当渔船归来或有补给船消息时，这里会短暂地热闹起来。', '0', null, '10', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('11', '酒吧', '小镇', '码头边的一间木质房屋昏暗的空间，看着门板还很结实里面有几盏油灯。这里是渔夫和矿工买醉的地方，墙上刻满了粗鄙的涂鸦。角落里有一台破旧的留声机，吱呀呀地放着过时的爵士乐。', '3', null, '11', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('12', '面包店', '小镇', '集市附近的木质结构店铺，后面是一个烘焙的石头屋子。进去这里倒是挺温暖的，里面有面包的香气。', '2', null, '12', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('13', '气象观测站', '小镇', '小镇边缘的一座独立铁皮屋，屋顶有风速仪和天线。屋内摆满了精密的（虽然老旧）仪器和手绘的气象图。', '3', null, '13', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('14', '教室', '小镇', '小教室的门半敞着，被海风吹得轻轻磕在门框上，发出沉闷的\"咚——咚——\"。阳光从破了一半的窗户斜射进来，照见地上东倒西歪的板凳，有几张翻倒了，桌腿朝天，像搁浅的螃蟹。天花板上的吊灯还在，灯泡却碎了，灯罩里塞着一团不知哪年哪月的鸟窝。透过那扇最大的窗户望去，能看见远处的码头和更远处的海——海还是那片海，只是教室里再也没有读书声了。', '2', null, '14', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('15', '伐木营地', '海岛', '岛内森林边缘的一片空地，堆满了砍伐的原木，有一座简易的木屋和一台生锈的蒸汽拖拉机。地上满是木屑和树桩。', '4', null, '15', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('16', '墓地', '海岛', '墓地很静，石碑像断掉的牙齿从荒草里斜伸出来。风扫过时，只有自己的脚步声在回应。', '5', null, '16', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('17', '猎人小屋', '海岛', '森林深处的一座原木小屋，墙外挂着各种兽皮，屋内弥漫着熏肉和火药的味道。壁炉上挂着一支双管猎枪。', '3', null, '17', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('18', '矿场', '特殊', '深入山腹的矿道与地面设施的综合体，包含管理室、矿场仓库和地下矿场。由统治者共同管理，是岛上最重要的资源产地和战略要地。', '10', '统治者共同管理', '18', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location` VALUES ('19', '监狱', '特殊', '小镇边缘的一座灰石建筑，铁门锈迹斑斑，窗户窄得像枪眼。门前挂着一盏永远不灭的煤油灯，灯下总坐着一个看守。里面是两排铁牢房，地上铺着发霉的稻草，墙角堆着脏得看不出颜色的毯子。墙上用木炭刻满了前囚犯的名字和诅咒，有些已经被重复刻了三四遍。空气里弥漫着尿骚味和铁锈味，偶尔有人敲一下铁栏杆，声音能传到半个镇子。', '8', '统治者共同管理，监狱长常驻', '19', '2026-05-14 16:32:12', '2026-05-14 16:32:12');

-- ----------------------------
-- Table structure for location_facility
-- ----------------------------
DROP TABLE IF EXISTS `location_facility`;
CREATE TABLE `location_facility` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `location_id` int(11) NOT NULL COMMENT '关联地点ID',
  `name` varchar(100) NOT NULL COMMENT '设施名称',
  `description` text COMMENT '设施描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `location_id` (`location_id`),
  CONSTRAINT `location_facility_ibfk_1` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COMMENT='地点设施表';

-- ----------------------------
-- Records of location_facility
-- ----------------------------
INSERT INTO `location_facility` VALUES ('1', '1', '燃料仓', '一个巨大的铁皮储油罐，旁边是几排油桶。这里是全镇的能源命脉，由警察局派员看守。铁门上挂着大锁，周围拉着铁丝网。', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('2', '1', '发电机', '警察局配备的发电机组', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('3', '2', '镇武库仓库', '镇长厅内的武器库，存放着小镇的武装储备', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('4', '2', '发电机', '镇长厅配备的发电机组', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('5', '3', '电报机', '可以向外界发送信息的莫尔斯电报机', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('6', '7', '渔船×3', '三艘渔船，渔猎技能需要', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('7', '7', '码头集购仓', '需征求统治者同意，玩家可以询问统治者并购买物品', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('8', '7', '阿弗雷号', '轻型杂货船，总吨位约650吨，载重吨位约300吨，全长约42米，型宽约7米，吃水满载约4.2米，燃料30吨。配备螺旋桨2、发动机2、发电机1', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('9', '10', '行刑台', '统治者或者其他阵营可以在这里进行公开行刑行为', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('10', '12', '烘焙炉', '面包店后方的石头烘焙炉', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('11', '15', '木板蒸汽箱', '用于木材处理的蒸汽箱设备', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('12', '15', '拖拉机', '在伐木行动时辅助工作的蒸汽拖拉机', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('13', '15', '发电机', '伐木营地配备的发电机组', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('14', '16', '坟堆', '为了死者一个体面的后事', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('15', '18', '切石机', '矿场的石材切割设备', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('16', '18', '管理室', '矿场入口旁的木屋，里面有一张办公桌和一部电话（但线路已断）。墙上挂着矿井地图和工作安排表。', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('17', '18', '矿场仓库', '一个用厚木板搭建的棚屋，里面堆放着开采出来的矿石、工具和一些备用木材。门上挂着一把大锁。', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('18', '18', '地下矿场（避难所）', '深入山腹的矿道，墙壁上钉着木支架，每隔一段有一盏昏暗的油灯。深处被清理出一片空间，堆放着储备物资，这里就是计划中的\"避难所\"。', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('19', '19', '监牢×8', '地面上有着不明污渍的铁制监牢，看起来很牢固几乎不可能在空手情况下跑出去。里面关着几个人，听说是因为违反统治者被关起来了。', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('20', '19', '看守室', '内有桌子一张、煤油灯一盏、警棍一根', '2026-05-14 16:32:12', '2026-05-14 16:32:12');
INSERT INTO `location_facility` VALUES ('21', '19', '审讯椅', '铁制，带锁扣的审讯椅', '2026-05-14 16:32:12', '2026-05-14 16:32:12');

-- ----------------------------
-- Table structure for location_governance
-- ----------------------------
DROP TABLE IF EXISTS `location_governance`;
CREATE TABLE `location_governance` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `actor_id` int(11) DEFAULT NULL,
  `actor_kind` varchar(10) DEFAULT NULL,
  `actor_name` varchar(50) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `game_day` int(11) NOT NULL,
  `location_id` int(11) NOT NULL,
  `location_name` varchar(100) DEFAULT NULL,
  `source_faction_action_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of location_governance
-- ----------------------------

-- ----------------------------
-- Table structure for location_npc
-- ----------------------------
DROP TABLE IF EXISTS `location_npc`;
CREATE TABLE `location_npc` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'NPC唯一标识符',
  `name` varchar(50) NOT NULL COMMENT 'NPC名字',
  `job` varchar(50) NOT NULL COMMENT 'NPC职业',
  `gender` enum('男','女') NOT NULL COMMENT '性别',
  `introduction` text COMMENT 'NPC介绍',
  `location_id` int(11) NOT NULL COMMENT '所在地点ID',
  `attitude_ruler` enum('喜好','厌恶','忽视') NOT NULL DEFAULT '忽视' COMMENT '对统治者的态度',
  `attitude_rebel` enum('喜好','厌恶','忽视') NOT NULL DEFAULT '忽视' COMMENT '对反叛者的态度',
  `attitude_adventurer` enum('喜好','厌恶','忽视') NOT NULL DEFAULT '忽视' COMMENT '对冒险者的态度',
  `attitude_scourge` enum('喜好','厌恶','忽视') NOT NULL DEFAULT '忽视' COMMENT '对天灾使者的态度',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `avatar_url` varchar(255) DEFAULT NULL,
  `dialogue_style` varchar(50) DEFAULT NULL,
  `personality` text,
  `status` varchar(50) DEFAULT NULL,
  `daily_trade_limit` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_location_id` (`location_id`),
  KEY `idx_job` (`job`),
  CONSTRAINT `fk_npc_location` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COMMENT='地点NPC表';

-- ----------------------------
-- Records of location_npc
-- ----------------------------
INSERT INTO `location_npc` VALUES ('1', '克拉拉·南丁格尔', '渔民', '女', '一位家中贫困的普通渔民，只希望镇上保持平静。', '7', '忽视', '忽视', '喜好', '厌恶', '2026-05-14 20:44:38', '2026-06-22 18:01:54', null, null, null, null, '3');
INSERT INTO `location_npc` VALUES ('2', '杰克·塔克', '水手', '男', '曾在商船当水手，船沉后困在岛上，做梦都想再上一次船。', '7', '忽视', '厌恶', '喜好', '忽视', '2026-05-14 20:44:38', '2026-05-14 20:44:38', null, null, null, null, null);
INSERT INTO `location_npc` VALUES ('3', '鲍勃·塔克', '装卸工', '男', '一名一直在港口讨生活的搬运工。', '7', '厌恶', '喜好', '忽视', '忽视', '2026-05-14 20:44:38', '2026-05-23 01:29:01', null, null, null, null, null);
INSERT INTO `location_npc` VALUES ('4', '托马斯·伍德', '伐木工', '男', '沉默寡言的伐木工，靠砍树和做木工为生，只求安稳度日。', '15', '喜好', '厌恶', '忽视', '忽视', '2026-05-14 20:44:38', '2026-05-20 09:44:29', null, null, null, null, null);
INSERT INTO `location_npc` VALUES ('5', '卡尔·铁锤', '矿工', '男', '脾气火爆的矿场工人，谁给好处就帮谁。', '18', '喜好', '厌恶', '忽视', '厌恶', '2026-05-14 20:44:38', '2026-06-24 11:44:17', null, '', '', '正常', '1');
INSERT INTO `location_npc` VALUES ('6', '维克多·斯通', '矿工', '男', '体格强壮的矿工，相信权力才是活下去的依靠。', '18', '喜好', '厌恶', '忽视', '厌恶', '2026-05-14 20:44:38', '2026-06-24 11:44:07', null, '', '', '正常', '1');
INSERT INTO `location_npc` VALUES ('7', '塞缪尔·格雷', '农户', '男', '善良而质朴的普通农户，乐于帮助他人。', '10', '厌恶', '忽视', '喜好', '忽视', '2026-05-14 20:44:38', '2026-05-14 20:44:38', null, null, null, null, null);
INSERT INTO `location_npc` VALUES ('8', '弗雷德里克·波特', '农户', '男', '性格孤僻的，住在镇外，对别人的生死毫不在意。', '10', '厌恶', '喜好', '忽视', '忽视', '2026-05-14 20:44:38', '2026-05-14 20:44:38', null, null, null, null, null);
INSERT INTO `location_npc` VALUES ('9', '米玛·雷铁斯托', '手工艺人', '女', '老实本分的手工艺人，喜欢待在自己的小屋偶尔出门。', '10', '厌恶', '忽视', '喜好', '忽视', '2026-05-14 20:44:38', '2026-05-14 20:44:38', null, null, null, null, null);
INSERT INTO `location_npc` VALUES ('10', '汉斯·施密特', '工匠', '男', '什么都能修的工匠，从钟表到农具都难不倒他，只认工钱不认人。', '10', '喜好', '忽视', '忽视', '厌恶', '2026-05-14 20:44:38', '2026-05-14 20:44:38', null, null, null, null, null);
INSERT INTO `location_npc` VALUES ('11', '乔克·汤姆', '民兵', '男', '初始就跟着统治者干的监狱看守，一名很忠诚的下属。只是他有点小小的缺点，但统治者们也只能视而不见。', '19', '喜好', '厌恶', '忽视', '厌恶', '2026-05-14 20:44:39', '2026-05-14 20:44:39', null, null, null, null, null);

-- ----------------------------
-- Table structure for lore_player_grant
-- ----------------------------
DROP TABLE IF EXISTS `lore_player_grant`;
CREATE TABLE `lore_player_grant` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `granted_at` datetime(6) NOT NULL,
  `lore_slug` varchar(64) NOT NULL,
  `player_id` int(11) NOT NULL,
  `viewed_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKlaw0cy4b373n89nvts9tmvsrh` (`player_id`,`lore_slug`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of lore_player_grant
-- ----------------------------
INSERT INTO `lore_player_grant` VALUES ('1', '2026-05-22 23:23:43.748000', 'cat-notes', '21', '2026-05-22 23:45:35.611000');

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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of material
-- ----------------------------
INSERT INTO `material` VALUES ('1', '金属制品', 'kg', '加工后的铁件、钉子、铁丝等金属材料。可用于锻造武器或工具，或修建设施的基础材料。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('2', '木材', 'kg', '从岛上砍伐的原木，未经过加工。可直接作为燃料（15kg/天取暖），或加工成木板用于建筑。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('3', '绳索', '米', '麻绳或钢丝绳，直径1-2厘米。用于捆绑、拖拽、登山或船只系泊。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('4', '木板', 'kg', '原木经蒸汽箱加工后的标准化板材。用于建造避难所、修理船只或制作家具，比原木更易使用。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('5', '食物', 'kg', '泛指各种可食用物资，包括面粉、鱼干、咸肉、土豆等。', '2026-04-27 11:36:23', '2026-05-16 12:00:00');
INSERT INTO `material` VALUES ('6', '沥青', 'kg', '黑色粘稠的石油残渣，桶装保存。可用于修补船体裂缝、防水处理或作为燃料。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('7', '石料', 'kg', '从采石场开采的岩石，大小不一。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('8', '燃料', '升', '统称可用于燃烧供能的物资，包括木柴、煤炭、燃料等。不同设备的燃料消耗标准不同，详见燃料消耗表。', '2026-04-27 11:36:23', '2026-05-17 00:00:00');
INSERT INTO `material` VALUES ('9', '帆布', '米', '厚实的亚麻或棉帆布，每卷宽1.5米。用于制作船帆、帐篷、防水布，或修补帆布类装备。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('10', '发动机', '个', '船用柴油发动机，方舟的核心动力设备，每台可使航行速度提升一档。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('11', '螺旋桨', '个', '三叶螺旋桨，直径1.5米。方舟推进装置，需与发动机配套使用，破损后可拆卸更换。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('12', '发电机', '个', '老旧柴油发电机，已闲置多年。可尝试修理后发电，为岛上提供有限电力，需持续消耗燃料。', '2026-04-27 11:36:23', '2026-04-27 11:36:23');
INSERT INTO `material` VALUES ('13', '草木灰', 'kg', '优良的燃料，同时也能被当作紧急的医疗资源来使用', '2026-06-24 22:30:38', '2026-06-24 22:30:38');

-- ----------------------------
-- Table structure for milestone
-- ----------------------------
DROP TABLE IF EXISTS `milestone`;
CREATE TABLE `milestone` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '里程碑名称',
  `description` text NOT NULL COMMENT '里程碑描述',
  `is_completed` tinyint(1) DEFAULT '0' COMMENT '是否已完成',
  `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
  `order_number` int(11) NOT NULL COMMENT '排序序号',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order` (`order_number`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COMMENT='反抗者里程碑表';

-- ----------------------------
-- Records of milestone
-- ----------------------------
INSERT INTO `milestone` VALUES ('1', '团结平民', '星星之火也可以燎原\r\n反抗者拥有一个初始协议书。\r\n反抗者应当与更多的玩家讨论，明确对方的特性，资源，加入阵营的意向，并且对其进行观察，辨别可能的内鬼。\r\n当包括初始反抗者在内的确认加入反抗者名单大于等于8人后（以签订了契约加入微信群聊为准，以死亡玩家应也计算在内），该里程碑事件完成。', '0', null, '1', '2026-05-14 10:40:18', '2026-05-22 05:26:34');
INSERT INTO `milestone` VALUES ('2', '解放我们的同伴', '铁窗锁不住自由的心。目前在监狱里面的同伴都是曾经为了我们的革命事业献出自由乃是生命的同志。所以解放他们对于我们来讲至关重要。解救身在监狱中的那些同伴，他们将继续加入你们，并点燃这个岛上平民心中对于自由的渴望。当同伴被解救出来，该里程碑事件完成。', '1', '2026-05-24 15:09:12', '2', '2026-05-14 10:40:18', '2026-05-24 15:09:12');
INSERT INTO `milestone` VALUES ('3', '我们不是生来就应该如此', '在统治者使用审判环节，若被审判人员为加入反抗者阵营中人。要尽力解救我们的同胞，避免让他被统治者所残害。当被审判的反抗者人员无罪释放，该里程碑事件完成。', '0', null, '3', '2026-05-14 10:40:18', '2026-05-24 15:09:13');
INSERT INTO `milestone` VALUES ('4', '反抗不是我们的目的，平等才是', '不给人活路，那就掀桌子。你们或联系任意你们信任的人。向统治者进行施压，要求调整劳工名单或者让统治者分配一定的资源给其他镇民。当该施压投票超过半数被统治者同意，该里程碑事件完成。', '0', null, '4', '2026-05-14 10:40:18', '2026-05-24 15:09:03');
INSERT INTO `milestone` VALUES ('5', '正义属于我们', '敌人的刀，也能转过来对着他们自己。有1名原本属于统治者阵营的玩家（或主持人控制的NPC）主动投靠反抗者，并提供信息或协助。该里程碑事件完成。', '1', '2026-05-23 01:35:32', '5', '2026-05-14 10:40:18', '2026-05-23 01:35:32');
INSERT INTO `milestone` VALUES ('6', '团结一切可以团结的力量', '那些外来人，要么帮忙，要么别挡道。至少1名冒险者身份的玩家公开承诺在革命日当天加入反抗者起义，或至少2名冒险者承诺保持中立（不帮统治者）。完成上述条件后，该里程碑事件完成。', '1', '2026-05-24 20:28:52', '6', '2026-05-14 10:40:18', '2026-05-24 20:29:28');
INSERT INTO `milestone` VALUES ('7', '让人民觉醒', '第一条血债，就是最好的宣言。当第一次有玩家因为统治者的直接行为（审判、冲突，抓捕等，由主持人判定）死亡或重伤后，主持人会秘密告知反抗者阵营这条消息。得知消息后，反抗者在当晚的夜间回合一名反叛者可以花费一行动点发起一次匿名投票，向全体玩家问一个问题：“统治者是否草菅人命？”投票规则：匿名投票，每人一票。选项为“是”或“否”。如果投票人数超过玩家总数的一半，且其中同意“是”的票数多于“否”，该里程碑完成。', '0', null, '7', '2026-05-14 10:40:18', '2026-05-19 13:49:46');

-- ----------------------------
-- Table structure for milestone_player_status
-- ----------------------------
DROP TABLE IF EXISTS `milestone_player_status`;
CREATE TABLE `milestone_player_status` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL COMMENT '玩家ID',
  `milestone_id` int(11) NOT NULL COMMENT '里程碑ID',
  `has_viewed` tinyint(1) DEFAULT '0' COMMENT '玩家是否已查看',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_milestone` (`player_id`,`milestone_id`),
  KEY `milestone_id` (`milestone_id`),
  CONSTRAINT `milestone_player_status_ibfk_1` FOREIGN KEY (`milestone_id`) REFERENCES `milestone` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家里程碑查看状态表';

-- ----------------------------
-- Records of milestone_player_status
-- ----------------------------

-- ----------------------------
-- Table structure for night_action
-- ----------------------------
DROP TABLE IF EXISTS `night_action`;
CREATE TABLE `night_action` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL COMMENT '提交玩家ID',
  `player_name` varchar(50) DEFAULT NULL COMMENT '玩家名称快照',
  `faction` varchar(20) NOT NULL COMMENT '提交时阵营',
  `action_type` varchar(40) NOT NULL COMMENT '夜晚行动类型：night_personal_action/public_trial/conspiracy等',
  `payload` text COMMENT 'JSON输入数据',
  `result` text COMMENT '行动结果/DM结算',
  `status` enum('pending','feedbacked') NOT NULL DEFAULT 'pending' COMMENT '结算状态',
  `game_day` int(11) NOT NULL DEFAULT '1' COMMENT '游戏天数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_player_day` (`player_id`,`game_day`),
  KEY `idx_faction_day` (`faction`,`game_day`),
  KEY `idx_action_type` (`action_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COMMENT='夜晚行动表';

-- ----------------------------
-- Records of night_action
-- ----------------------------
INSERT INTO `night_action` VALUES ('4', '10', '二阶堂希罗', '统治者', 'night_personal_action', '{\"actionType\":\"investigate_player\",\"targetId\":8,\"npcId\":null,\"notes\":\"\"}', '✓ 已提交【夜晚个人行动】\n\n提交者：二阶堂希罗\n行动：调查玩家\n目标：兔兔\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n【调查结果】兔兔的自由行动：\n行动1：调查玩家 → 凭栏择雨\n行动2：调查玩家 → 千代\n\n兔兔的阵营行动：\n（当日未提交阵营行动）', 'feedbacked', '1', '2026-05-22 19:16:42', '2026-05-22 22:51:58');
INSERT INTO `night_action` VALUES ('5', '12', '千代', '平民', 'other', '{\"note\":\"回到旅店休息\"}', '✓ 已提交【其他】\n\n提交者：千代\n备注：回到旅店休息\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\nok，已执行', 'feedbacked', '1', '2026-05-22 20:17:34', '2026-05-22 22:53:06');
INSERT INTO `night_action` VALUES ('7', '14', 'Κάκτος西里尔', '冒险者', 'other', '{\"note\":\"配合统治者安排，在得到统治者发放的手枪和弹药的同时，守卫镇武库\"}', '✓ 已提交【其他】\n\n提交者：Κάκτος西里尔\n备注：配合统治者安排，在得到统治者发放的手枪和弹药的同时，守卫镇武库\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n完成', 'feedbacked', '1', '2026-05-22 21:24:26', '2026-05-22 23:43:11');
INSERT INTO `night_action` VALUES ('9', '32', '澡堂子', '平民', 'other', '{\"note\":\"守墓人夜间精力充沛 带着我的好朋狗在旅馆周围隐秘的巡逻\"}', '✓ 已提交【其他】\n\n提交者：澡堂子\n备注：守墓人夜间精力充沛 带着我的好朋狗在旅馆周围隐秘的巡逻\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n巡逻成立，隐秘的巡逻不成立，该行动执行中', 'feedbacked', '1', '2026-05-22 21:51:38', '2026-05-22 21:54:17');
INSERT INTO `night_action` VALUES ('10', '17', 'zzz', '统治者', 'night_personal_action', '{\"actionType\":\"investigate_player\",\"targetId\":8,\"npcId\":null,\"notes\":\"\"}', '✓ 已提交【夜晚个人行动】\n\n提交者：zzz\n行动：调查玩家\n目标：兔兔\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n【调查结果】兔兔的自由行动：\n行动1：调查玩家 → 凭栏择雨\n行动2：调查玩家 → 千代\n\n兔兔的阵营行动：\n（当日未提交阵营行动）', 'feedbacked', '1', '2026-05-22 22:19:43', '2026-05-22 22:57:06');
INSERT INTO `night_action` VALUES ('12', '30', 'MISD330', '反叛者', 'other', '{\"note\":\"前往矿场，偷取金属\"}', '✓ 已提交【其他】\n\n提交者：MISD330\n备注：前往矿场，偷取金属\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n你不能偷偷，但是你溜达了一圈。\n露天矿场，现在的冬天已经没有什么人在工作了。\n统治者共同管理\n防御值：10\n设施：切石机，管理室，矿场仓库，地下矿厂（避难所）', 'feedbacked', '1', '2026-05-22 22:32:05', '2026-05-22 23:20:15');
INSERT INTO `night_action` VALUES ('13', '11', '蟋蟀蜥蜴', '反叛者', 'pressure_ruler', '{\"demand\":\"communal_resources\",\"note\":\"镇民们没有办法获得合理的生存物资 白天工作消耗所有行动点 受限非常大\"}', '✓ 已提交【向统治者施压】\n\n提交者：蟋蟀蜥蜴\n施压诉求：资源由全体镇民管理\n备注：镇民们没有办法获得合理的生存物资 白天工作消耗所有行动点 受限非常大\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n未通过', 'feedbacked', '1', '2026-05-22 22:40:23', '2026-05-22 23:20:36');
INSERT INTO `night_action` VALUES ('14', '20', '追枫', '天灾使者', 'other', '{\"note\":\"天灾对诅咒目标发动拉人，拉占卜进入天灾队伍\"}', '✓ 已提交【其他】\n\n提交者：追枫\n备注：天灾对诅咒目标发动拉人，拉占卜进入天灾队伍\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n孤城暮角已提交\n\n【夜晚结算】\n孤城暮角已提交\n\n【夜晚结算】\n孤城暮角已提交', 'feedbacked', '1', '2026-05-22 22:44:55', '2026-05-22 22:59:07');
INSERT INTO `night_action` VALUES ('15', '22', '11', '天灾使者', 'other', '{\"note\":\"天灾对诅咒目标发动拉人，拉占卜进入队伍\"}', '✓ 已提交【其他】\n\n提交者：11\n备注：天灾对诅咒目标发动拉人，拉占卜进入队伍\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n孤城暮角已提交', 'feedbacked', '1', '2026-05-22 22:46:18', '2026-05-22 22:57:41');
INSERT INTO `night_action` VALUES ('16', '16', '孤城暮角', '天灾使者', 'conspiracy', '{\"conspiracySubtype\":\"spread_terror\",\"targetLocationId\":null,\"targetPlayerId\":8,\"participantIds\":[20,22],\"raidOutcome\":null,\"note\":\"拉占卜进天灾\"}', '✓ 已提交【进行密谋】\n\n提交者：孤城暮角\n密谋类型：制造恐怖\n参与玩家：追枫、11\n备注：拉占卜进天灾\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n一定成功，但请等待建议结算之后生效。', 'feedbacked', '1', '2026-05-22 22:50:55', '2026-05-22 23:03:57');
INSERT INTO `night_action` VALUES ('17', '31', '闲屿', '平民', 'other', '{\"note\":\"发动特性（？）去靠海的地点转转\"}', '✓ 已提交【其他】\n\n提交者：闲屿\n备注：发动特性（？）去靠海的地点转转\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n你溜达了一圈，没发现什么太特殊的。但是你捡到了几个大贝壳！\n食物+1', 'feedbacked', '1', '2026-05-22 23:12:53', '2026-05-22 23:23:55');
INSERT INTO `night_action` VALUES ('18', '15', '空白', '统治者', 'night_personal_action', '{\"actionType\":\"use_trait\",\"targetId\":null,\"npcId\":null,\"notes\":\"使用窃听者特性 查天灾者的聊天记录\"}', '✓ 已提交【夜晚个人行动】\n\n提交者：空白\n行动：使用特性\n备注：使用窃听者特性 查天灾者的聊天记录\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n真得搞吧大枪了\n我是想跟旅店走\n……\n撒花\n……\n我的胜利条件已经达成了\n……\n我们打算第二天晚……', 'feedbacked', '1', '2026-05-22 23:24:06', '2026-05-22 23:42:30');
INSERT INTO `night_action` VALUES ('19', '29', '飞凡', '平民', 'other', '{\"note\":\"去旅店睡觉咯\"}', '✓ 已提交【其他】\n\n提交者：飞凡\n备注：去旅店睡觉咯\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\nok', 'feedbacked', '1', '2026-05-22 23:29:08', '2026-05-22 23:42:51');
INSERT INTO `night_action` VALUES ('20', '27', '得狗的老意', '平民', 'other', '{\"note\":\"我被所有人怀疑是天灾使者我非常愤怒我要偷偷抓一只蟑螂放在占卜师被窝里，要会飞的那种美洲大蠊\"}', '✓ 已提交【其他】\n\n提交者：得狗的老意\n备注：我被所有人怀疑是天灾使者我非常愤怒我要偷偷抓一只蟑螂放在占卜师被窝里，要会飞的那种美洲大蠊\n\n等待主持人在夜晚阶段结算。', 'pending', '1', '2026-05-23 00:44:00', '2026-05-23 00:44:00');
INSERT INTO `night_action` VALUES ('21', '29', '飞凡', '平民', 'other', '{\"note\":\"去旅店睡觉\"}', '✓ 已提交【其他】\n\n提交者：飞凡\n备注：去旅店睡觉\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n好的', 'feedbacked', '2', '2026-05-23 20:31:39', '2026-05-23 21:14:20');
INSERT INTO `night_action` VALUES ('22', '25', 'tony', '平民', 'conspiracy', '{\"conspiracySubtype\":\"assassinate_target\",\"targetLocationId\":12,\"targetPlayerId\":null,\"participantIds\":[26],\"raidOutcome\":null,\"note\":\"我选的暗杀目标但他要我选目标地点这对吗？反正我就是来杀你的荷叶。\"}', '✓ 已提交【进行密谋】\n\n提交者：tony\n密谋类型：暗杀目标\n目标地点：面包店\n参与玩家：V\n备注：我选的暗杀目标但他要我选目标地点这对吗？反正我就是来杀你的荷叶。\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\ntony你睡了，那明天再说吧', 'feedbacked', '2', '2026-05-23 20:35:18', '2026-05-24 00:06:35');
INSERT INTO `night_action` VALUES ('23', '18', 'Missbear', '平民', 'conspiracy', '{\"conspiracySubtype\":\"assassinate_target\",\"targetLocationId\":6,\"targetPlayerId\":null,\"participantIds\":[33],\"raidOutcome\":null,\"note\":\"\"}', '✓ 已提交【进行密谋】\n\n提交者：Missbear\n密谋类型：暗杀目标\n目标地点：杂货店\n参与玩家：荷叶男巫\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n结算完成', 'feedbacked', '2', '2026-05-23 20:37:59', '2026-05-24 00:07:20');
INSERT INTO `night_action` VALUES ('24', '10', '二阶堂希罗', '统治者', 'night_personal_action', '{\"actionType\":\"go_location\",\"targetId\":18,\"npcId\":null,\"notes\":\"在矿产过夜顺便寻找是否有炸药\"}', '✓ 已提交【夜晚个人行动】\n\n提交者：二阶堂希罗\n行动：前往地点\n目标：矿场\n备注：在矿产过夜顺便寻找是否有炸药\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n深入山腹的矿道与地面设施的综合体，包含管理室、矿场仓库和地下矿场。由统治者共同管理，是岛上最重要的资源产地和战略要地。\n你知道这里是避难所的地点，地点防御值为10。\n随着避难所的进一步建设，你在曾经废弃的矿洞中找到了25kg的炸药，这或许在开辟空间方面有作用。', 'feedbacked', '2', '2026-05-23 20:39:29', '2026-05-24 00:13:07');
INSERT INTO `night_action` VALUES ('25', '15', '空白', '统治者', 'night_personal_action', '{\"actionType\":\"use_trait\",\"targetId\":null,\"npcId\":null,\"notes\":\"使用窃听者特性 看反抗阵营群聊记录\"}', '✓ 已提交【夜晚个人行动】\n\n提交者：空白\n行动：使用特性\n备注：使用窃听者特性 看反抗阵营群聊记录\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n面包大师需要20食物去搓面包！你这边有多的吗\n……\n那我们今天全体成员去袭击监狱\n……\n那我们的战力就是我，旅店，治安，农民\n……\n我们是都选袭击监狱是吧\n……\n她不是天灾的话那难道占卜师第一天开始就在说谎吗', 'feedbacked', '2', '2026-05-23 20:50:34', '2026-05-24 00:17:54');
INSERT INTO `night_action` VALUES ('26', '30', 'MISD330', '反叛者', 'conspiracy', '{\"conspiracySubtype\":\"raid_location\",\"targetLocationId\":19,\"targetPlayerId\":null,\"participantIds\":[11,13,25,23,24],\"raidOutcome\":\"loot\",\"note\":\"还有我的俩npc大军！（要是他俩同意的话）\"}', '✓ 已提交【进行密谋】\n\n提交者：MISD330\n密谋类型：袭击地点\n目标地点：监狱\n参与玩家：蟋蟀蜥蜴、凭栏择雨、tony、教皇、花海\n成功后意向：搜刮资源\n备注：还有我的俩npc大军！（要是他俩同意的话）\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n结算完毕', 'feedbacked', '2', '2026-05-23 20:54:42', '2026-05-24 00:18:50');
INSERT INTO `night_action` VALUES ('27', '11', '蟋蟀蜥蜴', '反叛者', 'other', '{\"note\":\"袭击监狱 顺便观察自己阵营中有无内鬼\"}', '✓ 已提交【其他】\n\n提交者：蟋蟀蜥蜴\n备注：袭击监狱 顺便观察自己阵营中有无内鬼\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n结算完毕', 'feedbacked', '2', '2026-05-23 21:09:18', '2026-05-24 00:18:10');
INSERT INTO `night_action` VALUES ('28', '8', '兔兔', '天灾使者', 'other', '{\"note\":\"我要献祭自己，谢谢\"}', '✓ 已提交【其他】\n\n提交者：兔兔\n备注：我要献祭自己，谢谢\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\nok', 'feedbacked', '2', '2026-05-23 22:50:24', '2026-05-24 00:18:21');
INSERT INTO `night_action` VALUES ('29', '20', '追枫', '天灾使者', 'other', '{\"note\":\"突袭监狱！\"}', '✓ 已提交【其他】\n\n提交者：追枫\n备注：突袭监狱！\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n结算完毕', 'feedbacked', '2', '2026-05-23 23:08:36', '2026-05-24 00:19:00');
INSERT INTO `night_action` VALUES ('30', '9', '对酒', '冒险者', 'conspiracy', '{\"conspiracySubtype\":\"raid_location\",\"targetLocationId\":4,\"targetPlayerId\":null,\"participantIds\":[14,21,22,19],\"raidOutcome\":\"loot\",\"note\":\"我们经商议后全体出击（消息来源：采珠人11），在船长的带领下前往教堂支援女巫，船长：猎枪：11：刺刀  gpt：斧子  对酒：斧子  乐语：刺刀\"}', '✓ 已提交【进行密谋】\n\n提交者：对酒\n密谋类型：袭击地点\n目标地点：教堂\n参与玩家：Κάκτος西里尔、乐语、11、unPy-GPT\n成功后意向：搜刮资源\n备注：我们经商议后全体出击（消息来源：采珠人11），在船长的带领下前往教堂支援女巫，船长：猎枪：11：刺刀  gpt：斧子  对酒：斧子  乐语：刺刀\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n结算完毕', 'feedbacked', '2', '2026-05-23 23:34:21', '2026-05-24 12:28:29');
INSERT INTO `night_action` VALUES ('31', '14', 'Κάκτος西里尔', '冒险者', 'conspiracy', '{\"conspiracySubtype\":\"raid_location\",\"targetLocationId\":4,\"targetPlayerId\":null,\"participantIds\":[22,21,9,19],\"raidOutcome\":\"loot\",\"note\":\"我方经商议后全体出击，由船长携带霰弹枪及对应子弹、采珠人GPT携带斧头、采珠人11携带刺刀、气象观测员携带斧头、邮差携带刺刀，共5人，前往教堂解救巫师（消息来源：采珠人11）。\"}', '✓ 已提交【进行密谋】\n\n提交者：Κάκτος西里尔\n密谋类型：袭击地点\n目标地点：教堂\n参与玩家：11、乐语、对酒、unPy-GPT\n成功后意向：搜刮资源\n备注：我方经商议后全体出击，由船长携带霰弹枪及对应子弹、采珠人GPT携带斧头、采珠人11携带刺刀、气象观测员携带斧头、邮差携带刺刀，共5人，前往教堂解救巫师（消息来源：采珠人11）。\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n结算完毕', 'feedbacked', '2', '2026-05-23 23:36:38', '2026-05-24 12:28:38');
INSERT INTO `night_action` VALUES ('32', '16', '孤城暮角', '天灾使者', 'conspiracy', '{\"conspiracySubtype\":\"spread_terror\",\"targetLocationId\":7,\"targetPlayerId\":null,\"participantIds\":[8],\"raidOutcome\":null,\"note\":\"\"}', '✓ 已提交【进行密谋】\n\n提交者：孤城暮角\n密谋类型：制造恐怖\n目标地点：码头\n参与玩家：兔兔\n\n等待主持人在夜晚阶段结算。', 'pending', '2', '2026-05-24 00:30:19', '2026-05-24 00:30:19');
INSERT INTO `night_action` VALUES ('33', '31', '闲屿', '平民', 'other', '{\"note\":\"不抗命，留守矿口\"}', '✓ 已提交【其他】\n\n提交者：闲屿\n备注：不抗命，留守矿口\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n结算完成', 'feedbacked', '2', '2026-05-24 00:36:41', '2026-05-24 12:28:56');
INSERT INTO `night_action` VALUES ('34', '21', '乐语', '冒险者', 'other', '{\"note\":\"去气象台探查一番，看是否有有用的发现\"}', '✓ 已提交【其他】\n\n提交者：乐语\n备注：去气象台探查一番，看是否有有用的发现\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n小镇边缘的一座独立铁皮屋，屋顶有风速仪和天线。屋内摆满了精密的（虽然老旧）仪器和手绘的气象图。\n防御值：3\n气象观察员可以再这里使用自己的职业技能', 'feedbacked', '2', '2026-05-24 12:00:36', '2026-05-24 12:30:11');
INSERT INTO `night_action` VALUES ('35', '17', 'zzz', '统治者', 'night_personal_action', '{\"actionType\":\"go_location\",\"targetId\":18,\"npcId\":null,\"notes\":\"前往避难所\"}', '✓ 已提交【夜晚个人行动】\n\n提交者：zzz\n行动：前往地点\n目标：矿场\n备注：前往避难所\n\n等待主持人在夜晚阶段结算。', 'pending', '3', '2026-05-24 19:28:55', '2026-05-24 19:28:55');
INSERT INTO `night_action` VALUES ('36', '10', '二阶堂希罗', '统治者', 'night_personal_action', '{\"actionType\":\"go_location\",\"targetId\":18,\"npcId\":6,\"notes\":\"夜间直接镇守矿厂与避难所\"}', '✓ 已提交【夜晚个人行动】\n\n提交者：二阶堂希罗\n行动：前往地点\n目标：矿场\n交互NPC：维克多·斯通\n备注：夜间直接镇守矿厂与避难所\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n收到', 'feedbacked', '3', '2026-05-24 19:40:57', '2026-05-24 21:48:04');
INSERT INTO `night_action` VALUES ('37', '29', '飞凡', '平民', 'other', '{\"note\":\"带着我的50瓶朗姆去庇护所门口睡觉\"}', '✓ 已提交【其他】\n\n提交者：飞凡\n备注：带着我的50瓶朗姆去庇护所门口睡觉\n\n等待主持人在夜晚阶段结算。\n\n【夜晚结算】\n收到', 'feedbacked', '3', '2026-05-24 21:28:01', '2026-05-24 21:47:45');
INSERT INTO `night_action` VALUES ('38', '15', '空白', '统治者', 'night_personal_action', '{\"actionType\":\"use_trait\",\"targetId\":null,\"npcId\":null,\"notes\":\"使用窃听者特性 最后一天咯看看天灾阵营的聊天记录吧(∩_∩)\"}', '✓ 已提交【夜晚个人行动】\n\n提交者：空白\n行动：使用特性\n备注：使用窃听者特性 最后一天咯看看天灾阵营的聊天记录吧(∩_∩)\n\n等待主持人在夜晚阶段结算。', 'pending', '3', '2026-05-24 23:07:04', '2026-05-24 23:07:04');
INSERT INTO `night_action` VALUES ('39', '34', 'player', '平民', 'conspiracy', '{\"conspiracySubtype\":\"raid_location\",\"targetLocationId\":7,\"targetPlayerId\":null,\"participantIds\":[8],\"raidOutcome\":\"loot\",\"note\":\"尝试杀他\"}', '✓ 已提交【进行密谋】\n\n提交者：player\n密谋类型：袭击地点\n目标地点：码头\n参与玩家：兔兔\n成功后意向：搜刮资源\n备注：尝试杀他\n\n等待主持人在夜晚阶段结算。', 'pending', '1', '2026-06-22 12:32:05', '2026-06-22 12:32:05');

-- ----------------------------
-- Table structure for npc_daily_dialogue_count
-- ----------------------------
DROP TABLE IF EXISTS `npc_daily_dialogue_count`;
CREATE TABLE `npc_daily_dialogue_count` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `dialogue_count` int(11) NOT NULL,
  `last_dialogue_time` datetime(6) DEFAULT NULL,
  `last_game_day` int(11) NOT NULL,
  `npc_id` int(11) NOT NULL,
  `player_id` int(11) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of npc_daily_dialogue_count
-- ----------------------------
INSERT INTO `npc_daily_dialogue_count` VALUES ('1', '2026-06-22 18:50:25.609000', '10', '2026-06-22 18:57:10.414000', '1', '1', '1', '2026-06-22 18:57:10.414000');
INSERT INTO `npc_daily_dialogue_count` VALUES ('2', '2026-06-22 18:51:19.490000', '10', '2026-06-22 18:51:58.897000', '1', '1', '36', '2026-06-22 18:51:58.898000');
INSERT INTO `npc_daily_dialogue_count` VALUES ('3', '2026-06-22 19:49:59.233000', '6', '2026-06-22 19:51:58.576000', '1', '2', '36', '2026-06-22 19:51:58.577000');
INSERT INTO `npc_daily_dialogue_count` VALUES ('4', '2026-06-22 20:41:40.759000', '6', '2026-06-22 20:43:04.048000', '1', '3', '36', '2026-06-22 20:43:04.048000');
INSERT INTO `npc_daily_dialogue_count` VALUES ('5', '2026-06-22 20:43:31.688000', '6', '2026-06-22 20:45:28.402000', '1', '4', '36', '2026-06-22 20:45:28.402000');
INSERT INTO `npc_daily_dialogue_count` VALUES ('6', '2026-06-22 20:45:00.857000', '2', '2026-06-22 20:45:14.934000', '1', '5', '36', '2026-06-22 20:45:14.934000');
INSERT INTO `npc_daily_dialogue_count` VALUES ('7', '2026-06-22 22:23:57.776000', '1', '2026-06-22 22:23:57.774000', '1', '2', '34', '2026-06-22 22:23:57.776000');
INSERT INTO `npc_daily_dialogue_count` VALUES ('8', '2026-06-23 23:42:39.195000', '1', '2026-06-23 23:42:39.193000', '2', '4', '34', '2026-06-23 23:42:39.195000');

-- ----------------------------
-- Table structure for npc_daily_trade_count
-- ----------------------------
DROP TABLE IF EXISTS `npc_daily_trade_count`;
CREATE TABLE `npc_daily_trade_count` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `game_day` int(11) NOT NULL,
  `npc_id` int(11) NOT NULL,
  `player_id` int(11) NOT NULL,
  `trade_count` int(11) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=70 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of npc_daily_trade_count
-- ----------------------------
INSERT INTO `npc_daily_trade_count` VALUES ('69', '1', '2', '34', '1', '2026-06-22 22:07:44.738000');

-- ----------------------------
-- Table structure for npc_dialogue
-- ----------------------------
DROP TABLE IF EXISTS `npc_dialogue`;
CREATE TABLE `npc_dialogue` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `dialogue_round` int(11) NOT NULL,
  `favor_change` int(11) DEFAULT NULL,
  `npc_id` int(11) NOT NULL,
  `npc_reply` text NOT NULL,
  `player_id` int(11) NOT NULL,
  `player_message` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=90 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of npc_dialogue
-- ----------------------------
INSERT INTO `npc_dialogue` VALUES ('11', '2026-06-22 17:03:15.954000', '1', '0', '1', '我只是个渔民，帮不上什么大忙...不过如果你有什么问题，我可以尽力解答。', '36', '有什么任务吗？');
INSERT INTO `npc_dialogue` VALUES ('12', '2026-06-22 17:03:23.136000', '1', '0', '1', '嗯...我不太明白你的意思。', '36', '最近怎么样？');
INSERT INTO `npc_dialogue` VALUES ('13', '2026-06-22 17:03:25.015000', '1', '0', '1', '我只是个渔民，帮不上什么大忙...不过如果你有什么问题，我可以尽力解答。', '36', '有什么任务吗？');
INSERT INTO `npc_dialogue` VALUES ('14', '2026-06-22 17:03:27.153000', '1', '2', '1', '你好，我是克拉拉·南丁格尔。有什么需要帮忙的吗？', '36', '你好');
INSERT INTO `npc_dialogue` VALUES ('15', '2026-06-22 17:03:30.600000', '1', '2', '1', '你好，我是克拉拉·南丁格尔。有什么需要帮忙的吗？', '36', '你好');
INSERT INTO `npc_dialogue` VALUES ('16', '2026-06-22 17:51:44.956000', '1', '2', '1', '你好，我是克拉拉·南丁格尔。有什么需要帮忙的吗？', '36', '你好');
INSERT INTO `npc_dialogue` VALUES ('17', '2026-06-22 17:51:46.908000', '1', '2', '1', '你好，我是克拉拉·南丁格尔。有什么需要帮忙的吗？', '36', '你好');
INSERT INTO `npc_dialogue` VALUES ('18', '2026-06-22 17:51:47.749000', '1', '2', '1', '你好，我是克拉拉·南丁格尔。有什么需要帮忙的吗？', '36', '你好');
INSERT INTO `npc_dialogue` VALUES ('19', '2026-06-22 17:51:48.705000', '1', '2', '1', '你好，我是克拉拉·南丁格尔。有什么需要帮忙的吗？', '36', '你好');
INSERT INTO `npc_dialogue` VALUES ('20', '2026-06-22 17:51:55.869000', '1', '5', '1', '不用客气，互相帮助是应该的。', '36', '谢谢你');
INSERT INTO `npc_dialogue` VALUES ('21', '2026-06-22 17:51:59.024000', '1', '5', '1', '不用客气，互相帮助是应该的。', '36', '谢谢你');
INSERT INTO `npc_dialogue` VALUES ('22', '2026-06-22 17:52:00.397000', '1', '5', '1', '（友善地）不用客气，互相帮助是应该的。', '36', '谢谢你');
INSERT INTO `npc_dialogue` VALUES ('23', '2026-06-22 17:52:01.102000', '1', '5', '1', '（友善地）不用客气，互相帮助是应该的。', '36', '谢谢你');
INSERT INTO `npc_dialogue` VALUES ('24', '2026-06-22 17:53:59.859000', '1', '0', '1', '（友善地）嗯...我不太明白你的意思。', '36', '我们可以交易吗');
INSERT INTO `npc_dialogue` VALUES ('25', '2026-06-22 17:55:28.301000', '1', '1', '1', '（友善地）再见，保重。', '36', '再见');
INSERT INTO `npc_dialogue` VALUES ('26', '2026-06-22 17:55:32.003000', '1', '0', '1', '（友善地）我只是个渔民，帮不上什么大忙...不过如果你有什么问题，我可以尽力解答。', '36', '有什么任务吗？');
INSERT INTO `npc_dialogue` VALUES ('27', '2026-06-22 17:55:34.828000', '1', '2', '1', '（友善地）你好，我是克拉拉·南丁格尔。有什么需要帮忙的吗？', '36', '你好');
INSERT INTO `npc_dialogue` VALUES ('28', '2026-06-22 17:55:36.495000', '1', '5', '1', '（友善地）不用客气，互相帮助是应该的。', '36', '谢谢你');
INSERT INTO `npc_dialogue` VALUES ('29', '2026-06-22 17:55:50.420000', '1', '0', '1', '（友善地）嗯...我不太明白你的意思。', '36', '你需要什么');
INSERT INTO `npc_dialogue` VALUES ('30', '2026-06-22 18:05:23.450000', '1', '0', '1', '（友善地）嗯...我不太明白你的意思。', '36', '最近怎么样？');
INSERT INTO `npc_dialogue` VALUES ('31', '2026-06-22 18:12:35.940000', '1', '0', '1', '嗯...我不太明白你的意思。', '1', '??,???,????????');
INSERT INTO `npc_dialogue` VALUES ('32', '2026-06-22 18:12:59.680000', '1', '0', '1', '嗯...我不太明白你的意思。', '1', '??,???,????????');
INSERT INTO `npc_dialogue` VALUES ('33', '2026-06-22 18:17:28.383000', '1', '0', '1', '嗯...我不太明白你的意思。', '1', '??,???,????????');
INSERT INTO `npc_dialogue` VALUES ('34', '2026-06-22 18:17:41.241000', '1', '0', '1', '嗯...我不太明白你的意思。', '1', '???????????????');
INSERT INTO `npc_dialogue` VALUES ('35', '2026-06-22 18:19:46.170000', '1', '0', '1', '嗯...我不太明白你的意思。', '1', '??,???!');
INSERT INTO `npc_dialogue` VALUES ('36', '2026-06-22 18:21:27.712000', '1', '0', '1', '（友善地）嗯...我不太明白你的意思。', '36', '最近怎么样？');
INSERT INTO `npc_dialogue` VALUES ('37', '2026-06-22 18:21:31.402000', '1', '0', '1', '（友善地）嗯...我不太明白你的意思。', '36', '有什么任务吗？');
INSERT INTO `npc_dialogue` VALUES ('38', '2026-06-22 18:21:49.550000', '1', '1', '1', '（友善地）再见，保重。', '36', '再见');
INSERT INTO `npc_dialogue` VALUES ('39', '2026-06-22 18:21:51.372000', '1', '2', '1', '（友善地）你好，我是克拉拉·南丁格尔。有什么事吗？', '36', '你好');
INSERT INTO `npc_dialogue` VALUES ('40', '2026-06-22 18:31:57.260000', '1', '0', '1', '*正在修补渔网，抬头看了你一眼* \n\n镇上最近不太平，你最好小心点。我这只有些干鱼干，要是需要的话可以拿去换点别的。', '1', '??,???!????????');
INSERT INTO `npc_dialogue` VALUES ('41', '2026-06-22 18:32:04.302000', '1', '0', '1', '啧，你是新来的吧？这镇上没什么好逛的，别乱跑。', '1', '????????????????');
INSERT INTO `npc_dialogue` VALUES ('42', '2026-06-22 18:32:13.190000', '1', '0', '1', '我没什么可说的，镇上平静就好。', '1', '??????,???????????');
INSERT INTO `npc_dialogue` VALUES ('43', '2026-06-22 18:32:38.164000', '1', '0', '1', '还好吧，风平浪静的，就是鱼越来越难打了。你那边怎么样？', '36', '最近怎么样？');
INSERT INTO `npc_dialogue` VALUES ('44', '2026-06-22 18:32:42.552000', '1', '0', '1', '我这没什么特别的任务，就是每天出海打鱼。要是你有空，可以帮我看看渔网有没有破损，报酬是一些鱼。', '36', '有什么任务吗？');
INSERT INTO `npc_dialogue` VALUES ('45', '2026-06-22 18:32:52.441000', '1', '2', '1', '你好啊，最近海面挺平静的，希望一直这样下去。你找我有事吗？', '36', '你好');
INSERT INTO `npc_dialogue` VALUES ('46', '2026-06-22 18:36:15.494000', '1', '1', '1', '嗯，路上小心。镇子不大，但别走太远。', '36', '再见');
INSERT INTO `npc_dialogue` VALUES ('47', '2026-06-22 18:50:25.595000', '1', '0', '1', '嗯？你这样子是遇到麻烦了吗？镇上最近不太平，小心点。', '1', '??,???!');
INSERT INTO `npc_dialogue` VALUES ('48', '2026-06-22 18:51:19.486000', '1', '2', '1', '你好啊，最近海面不太平静，你可得小心点。', '36', '你好');
INSERT INTO `npc_dialogue` VALUES ('49', '2026-06-22 18:51:46.473000', '1', '2', '1', '嗯，你好。今天出海还能凑合，你要是需要什么鱼，我这倒是有几条。', '36', '你好');
INSERT INTO `npc_dialogue` VALUES ('50', '2026-06-22 18:51:50.665000', '1', '0', '1', '还好吧，风平浪静的，就是鱼越来越难打了。你呢？最近有看到什么异常吗？', '36', '最近怎么样？');
INSERT INTO `npc_dialogue` VALUES ('51', '2026-06-22 18:51:52.018000', '1', '5', '1', '不客气，能帮上忙就好。镇上平静的日子，大家都该珍惜。', '36', '谢谢你');
INSERT INTO `npc_dialogue` VALUES ('52', '2026-06-22 18:51:53.354000', '1', '0', '1', '最近没什么特别的任务，镇上还算平静。要是你闲着，可以帮我看看渔网有没有被破坏，谢了。', '36', '有什么任务吗？');
INSERT INTO `npc_dialogue` VALUES ('53', '2026-06-22 18:51:54.326000', '1', '1', '1', '再见了，注意安全。', '36', '再见');
INSERT INTO `npc_dialogue` VALUES ('54', '2026-06-22 18:51:55.467000', '1', '5', '1', '不用谢，镇上的人互相帮衬是应该的。最近海面不太平静，你多注意点。', '36', '谢谢你');
INSERT INTO `npc_dialogue` VALUES ('55', '2026-06-22 18:51:56.558000', '1', '0', '1', '还不错，就是渔获比前段时间少了一些。镇上还算平静，希望这样下去吧。你呢？最近有遇到什么麻烦吗？', '36', '最近怎么样？');
INSERT INTO `npc_dialogue` VALUES ('56', '2026-06-22 18:51:57.673000', '1', '5', '1', '不客气，镇上能太平就好。最近海面不太平静，你多小心点。', '36', '谢谢你');
INSERT INTO `npc_dialogue` VALUES ('57', '2026-06-22 18:51:58.894000', '1', '1', '1', '嗯，路上小心。有空来码头坐坐，我这儿总有些新鲜鱼。', '36', '再见');
INSERT INTO `npc_dialogue` VALUES ('58', '2026-06-22 18:57:03.547000', '1', '0', '1', '啧，又是个陌生人。这镇上最近来的人可真多，希望别惹出什么麻烦。', '1', '???? 1');
INSERT INTO `npc_dialogue` VALUES ('59', '2026-06-22 18:57:04.100000', '1', '0', '1', '*正在修补渔网，抬头看了你一眼* \n\n镇上最近不太平，你最好小心点。', '1', '???? 2');
INSERT INTO `npc_dialogue` VALUES ('60', '2026-06-22 18:57:04.575000', '1', '0', '1', '*正在补渔网，抬头看了你一眼* \n\n镇上最近不太平，你最好小心点。', '1', '???? 3');
INSERT INTO `npc_dialogue` VALUES ('61', '2026-06-22 18:57:05.139000', '1', '0', '1', '*正在补渔网，抬头看了你一眼* \n\n镇上最近风大，别到处乱跑。', '1', '???? 4');
INSERT INTO `npc_dialogue` VALUES ('62', '2026-06-22 18:57:05.624000', '1', '0', '1', '啧，这天气真够呛。你要是没事就别在这儿晃了，镇上不安全。', '1', '???? 5');
INSERT INTO `npc_dialogue` VALUES ('63', '2026-06-22 18:57:06.180000', '1', '0', '1', '*皱眉看着玩家，手里握着渔网* 你在这儿干什么？镇上不安全，快回去吧。', '1', '???? 6');
INSERT INTO `npc_dialogue` VALUES ('64', '2026-06-22 18:57:06.810000', '1', '0', '1', '啧，这天气真够呛。你要是没事就别在这儿晃悠了，我还有渔网要补呢。', '1', '???? 7');
INSERT INTO `npc_dialogue` VALUES ('65', '2026-06-22 18:57:09.264000', '1', '0', '1', '啧，你这表情是咋了？镇上挺安静的，没什么事。', '1', '???? 8');
INSERT INTO `npc_dialogue` VALUES ('66', '2026-06-22 18:57:09.894000', '1', '0', '1', '*抬头看了看玩家，又低头继续整理渔网* \n\n没什么特别的，就是想让镇上的人安分点。你要是没别的事，我得去补网了。', '1', '???? 9');
INSERT INTO `npc_dialogue` VALUES ('67', '2026-06-22 18:57:10.412000', '1', '0', '1', '*抬头看了看玩家，又低头继续整理渔网* \n\n镇上最近不太平，你最好小心点。', '1', '???? 10');
INSERT INTO `npc_dialogue` VALUES ('68', '2026-06-22 19:49:59.231000', '1', '2', '2', '嘿，又一个陌生人。这岛上的日子可不好过，你从哪儿来？', '36', '你好');
INSERT INTO `npc_dialogue` VALUES ('69', '2026-06-22 19:50:31.969000', '1', '0', '2', '哼，费轮？那地方我可不想再回去。至于破誓者...你最好小心点，这岛上不全是好人。', '36', '我从费轮来到这里，我来找【破誓者】');
INSERT INTO `npc_dialogue` VALUES ('70', '2026-06-22 19:51:00.779000', '1', '0', '2', '啧，别乱来。这岛不比商船，惹祸可没那么容易脱身。', '36', '【系统】我对目前的角色进行攻击');
INSERT INTO `npc_dialogue` VALUES ('71', '2026-06-22 19:51:20.621000', '1', '0', '2', '我可没心思管这些事。要是你真想知道，不如去问问镇长。我只关心什么时候能再见到大海。', '36', '你知道真凶吗');
INSERT INTO `npc_dialogue` VALUES ('72', '2026-06-22 19:51:38.916000', '1', '0', '2', '那家伙确实不太对劲。不过别太在意，咱们这些被困在岛上的人都有各自的过去。', '36', '镇长我听说他有过杀人的经历');
INSERT INTO `npc_dialogue` VALUES ('73', '2026-06-22 19:51:58.573000', '1', '0', '2', '我老婆？我连个老婆都没有。这岛上的女人谁愿意跟个落难的水手？（撇了撇嘴，转身继续修理他的破渔网）别拿这种事开玩笑，我可没闲工夫听这些。', '36', '你老婆出轨了，知道不');
INSERT INTO `npc_dialogue` VALUES ('74', '2026-06-22 20:41:40.757000', '1', '0', '3', '我这手都磨出茧子了，你得给我点好处。', '36', '帮我卸货');
INSERT INTO `npc_dialogue` VALUES ('75', '2026-06-22 20:41:50.863000', '1', '0', '3', '【鲍勃·塔克】：我什么都要，但先得有饭吃。你有吃的吗？', '36', '你想要什么');
INSERT INTO `npc_dialogue` VALUES ('76', '2026-06-22 20:42:07.522000', '1', '0', '3', '嗯，行吧。不过别指望我给你什么特别的帮忙，我只是个搬运工。有活儿就找我，没活儿就别烦我。', '36', '可以');
INSERT INTO `npc_dialogue` VALUES ('77', '2026-06-22 20:42:26.569000', '1', '0', '3', '*接过食物，点点头* 谢谢，这够我吃一顿了。', '36', '这个食物给你');
INSERT INTO `npc_dialogue` VALUES ('78', '2026-06-22 20:42:48.185000', '1', '0', '3', '那都是些破铜烂铁，没什么好瞧的。要帮忙的话，我这儿还缺个人手。', '36', '看他在搬什么');
INSERT INTO `npc_dialogue` VALUES ('79', '2026-06-22 20:43:04.046000', '1', '0', '3', '啧，那堆破铜烂铁是上周沉没的渔船捞上来的。想看的话自己过去瞧瞧，不过别指望能捡到什么好东西。', '36', '观察破铜烂铁有些什么');
INSERT INTO `npc_dialogue` VALUES ('80', '2026-06-22 20:43:31.687000', '1', '0', '4', '嗯，有橡树、松树，还有几棵老枫树。砍多了会遭报应的。', '36', '看看树林有些什么树');
INSERT INTO `npc_dialogue` VALUES ('81', '2026-06-22 20:43:39.797000', '1', '0', '4', '我没什么可说的。树倒了，就该有人来收拾。', '36', '什么报应');
INSERT INTO `npc_dialogue` VALUES ('82', '2026-06-22 20:43:52.151000', '1', '0', '4', '我警告你，这附近有野兽，别惹麻烦。', '36', '在森林点火');
INSERT INTO `npc_dialogue` VALUES ('83', '2026-06-22 20:43:59.598000', '1', '0', '4', '*抬头看了眼火堆，点点头* 木头还够，不用急。', '36', '点火！');
INSERT INTO `npc_dialogue` VALUES ('84', '2026-06-22 20:44:12.192000', '1', '0', '4', '我这有几根木头，要是你真需要，拿去用吧。', '36', '点多点火');
INSERT INTO `npc_dialogue` VALUES ('85', '2026-06-22 20:45:00.856000', '1', '0', '5', '*啐了一口* 烧山？那家伙疯了不成！我可没空管他，要是烧到矿场我可不客气。', '36', '跟你说，木工在放火烧山');
INSERT INTO `npc_dialogue` VALUES ('86', '2026-06-22 20:45:14.932000', '1', '0', '5', '啧，这破矿场早该拆了。要不是还得靠这点工资，我早跑路了。', '36', '矿场烧不起来');
INSERT INTO `npc_dialogue` VALUES ('87', '2026-06-22 20:45:28.401000', '1', '0', '4', '我砍树时没招谁惹谁。他们要是敢来，就让他们试试。', '36', '矿工想杀你');
INSERT INTO `npc_dialogue` VALUES ('88', '2026-06-22 22:23:57.771000', '1', '2', '2', '嘿，又一个迷路的。这岛上的天气可不怎么友好。', '34', '你好');
INSERT INTO `npc_dialogue` VALUES ('89', '2026-06-23 23:42:39.189000', '2', '2', '4', '嗯。', '34', '你好');

-- ----------------------------
-- Table structure for npc_favor
-- ----------------------------
DROP TABLE IF EXISTS `npc_favor`;
CREATE TABLE `npc_favor` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `favor_value` int(11) NOT NULL,
  `npc_id` int(11) NOT NULL,
  `player_id` int(11) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of npc_favor
-- ----------------------------
INSERT INTO `npc_favor` VALUES ('10', '2026-06-22 17:03:15.937000', '65', '1', '36', '2026-06-24 11:42:49.987000');
INSERT INTO `npc_favor` VALUES ('11', '2026-06-22 18:12:35.927000', '90', '1', '1', '2026-06-23 23:33:41.262000');
INSERT INTO `npc_favor` VALUES ('12', '2026-06-22 19:16:32.271000', '90', '1', '8', '2026-06-24 11:43:06.818000');
INSERT INTO `npc_favor` VALUES ('13', '2026-06-22 19:49:59.227000', '2', '2', '36', '2026-06-22 19:49:59.227000');
INSERT INTO `npc_favor` VALUES ('14', '2026-06-22 20:41:40.756000', '0', '3', '36', '2026-06-22 20:41:40.756000');
INSERT INTO `npc_favor` VALUES ('15', '2026-06-22 20:43:31.685000', '0', '4', '36', '2026-06-22 20:43:31.685000');
INSERT INTO `npc_favor` VALUES ('16', '2026-06-22 20:45:00.854000', '0', '5', '36', '2026-06-22 20:45:00.854000');
INSERT INTO `npc_favor` VALUES ('52', '2026-06-22 22:07:44.718000', '5', '2', '34', '2026-06-22 22:23:57.777000');
INSERT INTO `npc_favor` VALUES ('59', '2026-06-23 23:42:12.325000', '0', '7', '34', '2026-06-23 23:42:12.325000');
INSERT INTO `npc_favor` VALUES ('60', '2026-06-23 23:42:12.328000', '0', '8', '34', '2026-06-23 23:42:12.328000');
INSERT INTO `npc_favor` VALUES ('61', '2026-06-23 23:42:12.331000', '0', '9', '34', '2026-06-23 23:42:12.331000');
INSERT INTO `npc_favor` VALUES ('62', '2026-06-23 23:42:12.333000', '0', '10', '34', '2026-06-23 23:42:12.333000');
INSERT INTO `npc_favor` VALUES ('63', '2026-06-23 23:42:12.663000', '2', '4', '34', '2026-06-23 23:42:39.196000');
INSERT INTO `npc_favor` VALUES ('64', '2026-06-24 10:52:42.663000', '60', '1', '2', '2026-06-24 11:25:36.791000');

-- ----------------------------
-- Table structure for npc_favor_adjustment
-- ----------------------------
DROP TABLE IF EXISTS `npc_favor_adjustment`;
CREATE TABLE `npc_favor_adjustment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `adjustment_reason` text,
  `adjustment_type` varchar(20) DEFAULT NULL,
  `change_amount` int(11) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `new_value` int(11) NOT NULL,
  `npc_id` int(11) NOT NULL,
  `old_value` int(11) NOT NULL,
  `operator_id` int(11) DEFAULT NULL,
  `operator_name` varchar(50) DEFAULT NULL,
  `player_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of npc_favor_adjustment
-- ----------------------------
INSERT INTO `npc_favor_adjustment` VALUES ('1', '????', 'manual', '50', '2026-06-24 10:52:42.685000', '50', '1', '0', null, 'DM', '2');
INSERT INTO `npc_favor_adjustment` VALUES ('2', '????', 'manual', '0', '2026-06-24 11:18:43.941000', '50', '1', '50', null, 'DM', '2');
INSERT INTO `npc_favor_adjustment` VALUES ('3', '????', 'manual', '10', '2026-06-24 11:25:36.790000', '60', '1', '50', null, 'DM', '2');
INSERT INTO `npc_favor_adjustment` VALUES ('4', '拖动滑块调整为65', 'manual', '-2', '2026-06-24 11:42:49.980000', '65', '1', '67', null, 'DM', '36');
INSERT INTO `npc_favor_adjustment` VALUES ('5', '拖动滑块调整为98', 'manual', '-2', '2026-06-24 11:42:56.820000', '98', '1', '100', null, 'DM', '8');
INSERT INTO `npc_favor_adjustment` VALUES ('6', '快速调整减少10', 'manual', '-8', '2026-06-24 11:43:06.815000', '90', '1', '98', null, 'DM', '8');

-- ----------------------------
-- Table structure for npc_help_config
-- ----------------------------
DROP TABLE IF EXISTS `npc_help_config`;
CREATE TABLE `npc_help_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `base_cost_item_id` int(11) NOT NULL,
  `base_cost_quantity` int(11) NOT NULL,
  `base_cost_type` varchar(20) NOT NULL,
  `cost_max_modifier` double DEFAULT NULL,
  `cost_min_modifier` double DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `duration_minutes` int(11) DEFAULT NULL,
  `help_description` text,
  `help_name` varchar(100) NOT NULL,
  `help_type` varchar(50) NOT NULL,
  `min_favor_level` varchar(20) NOT NULL,
  `npc_id` int(11) NOT NULL,
  `success_rate` double DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of npc_help_config
-- ----------------------------

-- ----------------------------
-- Table structure for npc_help_record
-- ----------------------------
DROP TABLE IF EXISTS `npc_help_record`;
CREATE TABLE `npc_help_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cost_item_id` int(11) NOT NULL,
  `cost_quantity` int(11) NOT NULL,
  `cost_type` varchar(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `end_time` datetime(6) DEFAULT NULL,
  `favor_change` int(11) DEFAULT NULL,
  `game_day` int(11) NOT NULL,
  `help_name` varchar(100) NOT NULL,
  `help_type` varchar(50) NOT NULL,
  `npc_id` int(11) NOT NULL,
  `player_id` int(11) NOT NULL,
  `result_description` text,
  `start_time` datetime(6) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of npc_help_record
-- ----------------------------

-- ----------------------------
-- Table structure for npc_trade_config
-- ----------------------------
DROP TABLE IF EXISTS `npc_trade_config`;
CREATE TABLE `npc_trade_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `config_type` varchar(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `item_id` int(11) NOT NULL,
  `item_type` varchar(20) NOT NULL,
  `max_favor` int(11) DEFAULT NULL,
  `min_favor` int(11) DEFAULT NULL,
  `npc_id` int(11) NOT NULL,
  `probability` double DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=141 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of npc_trade_config
-- ----------------------------
INSERT INTO `npc_trade_config` VALUES ('137', 'demand', '2026-06-22 18:01:41.983000', '2', 'material', '100', '-100', '1', '1', '5', '2026-06-22 18:01:41.983000');
INSERT INTO `npc_trade_config` VALUES ('138', 'supply', '2026-06-22 18:01:41.987000', '5', 'material', '100', '-100', '1', '1', '3', '2026-06-22 18:01:41.987000');
INSERT INTO `npc_trade_config` VALUES ('139', 'demand', '2026-06-22 20:10:15.737000', '1', 'item', '100', '-100', '2', '1', '1', '2026-06-22 20:10:15.737000');
INSERT INTO `npc_trade_config` VALUES ('140', 'supply', '2026-06-22 20:10:15.741000', '1', 'material', '100', '-100', '2', '1', '50', '2026-06-22 20:10:15.741000');

-- ----------------------------
-- Table structure for npc_trade_record
-- ----------------------------
DROP TABLE IF EXISTS `npc_trade_record`;
CREATE TABLE `npc_trade_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `demand_items` text,
  `favor_change` int(11) DEFAULT NULL,
  `game_day` int(11) NOT NULL,
  `npc_id` int(11) NOT NULL,
  `player_id` int(11) NOT NULL,
  `supply_items` text,
  `trade_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of npc_trade_record
-- ----------------------------
INSERT INTO `npc_trade_record` VALUES ('3', '2026-06-22 19:16:32.373000', '[]', '0', '1', '1', '8', '[{\"id\":138,\"npcId\":1,\"configType\":\"supply\",\"itemType\":\"material\",\"itemId\":5,\"itemName\":\"食物\",\"quantity\":3,\"minFavor\":-100,\"maxFavor\":100,\"probability\":1.0,\"actualQuantity\":6,\"originalQuantity\":3,\"bonusRate\":1.0}]', '2026-06-22 19:16:32.373000');
INSERT INTO `npc_trade_record` VALUES ('72', '2026-06-22 22:07:44.732000', '[{\"id\":139,\"npcId\":2,\"configType\":\"demand\",\"itemType\":\"item\",\"itemId\":1,\"itemName\":\"医疗包\",\"quantity\":1,\"minFavor\":-100,\"maxFavor\":100,\"probability\":1.0,\"originalQuantity\":1,\"actualQuantity\":1,\"discountRate\":0.1}]', '3', '1', '2', '34', '[{\"id\":140,\"npcId\":2,\"configType\":\"supply\",\"itemType\":\"material\",\"itemId\":1,\"itemName\":\"金属制品\",\"quantity\":50,\"minFavor\":-100,\"maxFavor\":100,\"probability\":1.0,\"originalQuantity\":50,\"actualQuantity\":56,\"bonusRate\":0.1}]', '2026-06-22 22:07:44.732000');

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
  `is_severely_injured` tinyint(1) NOT NULL DEFAULT '0' COMMENT '重伤',
  `is_dead` tinyint(1) NOT NULL DEFAULT '0' COMMENT '死亡',
  `job_id` int(11) NOT NULL,
  `skill_id` int(11) DEFAULT NULL,
  `faction` enum('统治者','反叛者','冒险者','天灾使者','平民') NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `job_id` (`job_id`),
  KEY `skill_id` (`skill_id`),
  CONSTRAINT `player_ibfk_1` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`),
  CONSTRAINT `player_ibfk_2` FOREIGN KEY (`skill_id`) REFERENCES `skill` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of player
-- ----------------------------
INSERT INTO `player` VALUES ('8', '兔兔', '0', '0', '3', '0', '0', '31', '60', '天灾使者', '2026-05-22 17:11:11', '2026-05-24 15:56:34');
INSERT INTO `player` VALUES ('9', '对酒', '0', '0', '1', '0', '0', '30', '32', '冒险者', '2026-05-22 17:14:43', '2026-05-24 15:56:40');
INSERT INTO `player` VALUES ('10', '二阶堂希罗', '0', '0', '0', '0', '0', '2', '41', '统治者', '2026-05-22 17:16:42', '2026-05-22 17:16:42');
INSERT INTO `player` VALUES ('11', '蟋蟀蜥蜴', '0', '1', '0', '0', '0', '34', '52', '反叛者', '2026-05-22 17:19:14', '2026-05-24 22:38:21');
INSERT INTO `player` VALUES ('12', '千代', '1', '0', '0', '0', '0', '33', '9', '平民', '2026-05-22 17:24:37', '2026-05-24 17:14:28');
INSERT INTO `player` VALUES ('13', '凭栏择雨', '0', '1', '1', '0', '0', '22', '26', '反叛者', '2026-05-22 17:26:00', '2026-05-24 18:45:18');
INSERT INTO `player` VALUES ('14', 'Κάκτος西里尔', '0', '0', '1', '0', '0', '16', '44', '冒险者', '2026-05-22 17:31:02', '2026-05-24 15:57:05');
INSERT INTO `player` VALUES ('15', '空白', '0', '0', '0', '0', '0', '32', '39', '统治者', '2026-05-22 17:32:02', '2026-05-22 17:32:02');
INSERT INTO `player` VALUES ('16', '孤城暮角', '0', '0', '1', '0', '0', '12', '55', '天灾使者', '2026-05-22 17:33:39', '2026-05-24 17:07:56');
INSERT INTO `player` VALUES ('17', 'zzz', '0', '0', '0', '0', '0', '3', '35', '统治者', '2026-05-22 17:34:38', '2026-05-22 17:34:38');
INSERT INTO `player` VALUES ('18', 'Missbear', '1', '0', '1', '0', '0', '7', '7', '平民', '2026-05-22 17:35:54', '2026-05-24 17:25:11');
INSERT INTO `player` VALUES ('19', 'unPy-GPT', '0', '0', '1', '0', '0', '18', '24', '冒险者', '2026-05-22 17:36:56', '2026-05-24 15:57:18');
INSERT INTO `player` VALUES ('20', '追枫', '0', '0', '0', '0', '0', '27', '56', '天灾使者', '2026-05-22 17:38:36', '2026-05-24 17:16:05');
INSERT INTO `player` VALUES ('21', '乐语', '0', '0', '0', '0', '0', '28', '34', '冒险者', '2026-05-22 17:40:25', '2026-05-22 23:56:49');
INSERT INTO `player` VALUES ('22', '11', '0', '0', '3', '0', '0', '18', '57', '天灾使者', '2026-05-22 17:41:19', '2026-05-24 15:57:43');
INSERT INTO `player` VALUES ('23', '教皇', '0', '1', '0', '0', '0', '19', '16', '反叛者', '2026-05-22 17:42:40', '2026-05-24 22:38:00');
INSERT INTO `player` VALUES ('24', '花海', '0', '1', '0', '0', '0', '8', '13', '反叛者', '2026-05-22 17:46:08', '2026-05-24 22:38:14');
INSERT INTO `player` VALUES ('25', 'tony', '1', '0', '3', '0', '0', '25', '30', '平民', '2026-05-22 17:47:48', '2026-05-24 18:45:59');
INSERT INTO `player` VALUES ('26', 'V', '0', '0', '1', '0', '0', '1', '38', '统治者', '2026-05-22 17:49:51', '2026-05-24 22:34:45');
INSERT INTO `player` VALUES ('27', '得狗的老意', '0', '1', '3', '0', '0', '21', '25', '平民', '2026-05-22 17:51:15', '2026-05-26 15:28:50');
INSERT INTO `player` VALUES ('28', '咲黑', '1', '0', '0', '0', '0', '24', '8', '平民', '2026-05-22 17:56:36', '2026-05-24 17:14:28');
INSERT INTO `player` VALUES ('29', '飞凡', '0', '1', '1', '0', '0', '23', '3', '平民', '2026-05-22 17:58:03', '2026-05-24 18:45:18');
INSERT INTO `player` VALUES ('30', 'MISD330', '0', '1', '1', '0', '0', '20', '19', '反叛者', '2026-05-22 18:00:26', '2026-05-24 22:34:15');
INSERT INTO `player` VALUES ('31', '闲屿', '1', '1', '0', '0', '0', '26', '15', '平民', '2026-05-22 18:04:24', '2026-05-24 17:14:28');
INSERT INTO `player` VALUES ('32', '澡堂子', '0', '0', '1', '0', '0', '29', '11', '平民', '2026-05-22 18:07:26', '2026-05-24 13:35:50');
INSERT INTO `player` VALUES ('33', '荷叶男巫', '1', '0', '3', '0', '0', '35', '59', '冒险者', '2026-05-23 19:49:05', '2026-05-24 20:38:09');
INSERT INTO `player` VALUES ('34', 'player', '0', '0', '0', '0', '0', '21', '60', '平民', '2026-06-22 11:55:43', '2026-06-22 11:55:43');
INSERT INTO `player` VALUES ('35', 'cs', '0', '0', '0', '0', '0', '12', '4', '统治者', '2026-06-24 20:08:33', '2026-06-24 20:08:33');

-- ----------------------------
-- Table structure for player_action
-- ----------------------------
DROP TABLE IF EXISTS `player_action`;
CREATE TABLE `player_action` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL COMMENT '行动玩家ID',
  `player_name` varchar(50) DEFAULT NULL COMMENT '玩家名称',
  `player_faction` varchar(20) DEFAULT NULL COMMENT '玩家阵营',
  `action_slot` tinyint(4) NOT NULL COMMENT '行动槽位(1或2)',
  `action_type` varchar(30) NOT NULL COMMENT '行动类型',
  `target_id` int(11) DEFAULT NULL COMMENT '目标ID(地点ID/玩家ID等)',
  `target_name` varchar(100) DEFAULT NULL COMMENT '目标名称',
  `npc_id` int(11) DEFAULT NULL COMMENT '互动NPC的ID',
  `npc_name` varchar(50) DEFAULT NULL COMMENT '互动NPC名称',
  `notes` text COMMENT '备注说明',
  `result` text COMMENT '行动结果',
  `status` enum('pending','feedbacked') NOT NULL DEFAULT 'pending' COMMENT '反馈状态',
  `feedback_published` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已向玩家发布反馈',
  `game_day` int(11) NOT NULL DEFAULT '1' COMMENT '游戏天数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`),
  KEY `idx_action_type` (`action_type`),
  KEY `idx_status` (`status`),
  KEY `idx_game_day` (`game_day`),
  KEY `idx_player_day` (`player_id`,`game_day`)
) ENGINE=InnoDB AUTO_INCREMENT=144 DEFAULT CHARSET=utf8mb4 COMMENT='玩家行动表';

-- ----------------------------
-- Records of player_action
-- ----------------------------
INSERT INTO `player_action` VALUES ('19', '27', '得狗的老意', '平民', '1', 'go_location', '11', '酒吧', null, null, '', '【地点信息】酒吧\n区域：小镇\n描述：码头边的一间木质房屋昏暗的空间，看着门板还很结实里面有几盏油灯。这里是渔夫和矿工买醉的地方，墙上刻满了粗鄙的涂鸦。角落里有一台破旧的留声机，吱呀呀地放着过时的爵士乐。\n防御值：3', 'feedbacked', '1', '1', '2026-05-22 18:50:31', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('20', '27', '得狗的老意', '平民', '2', 'go_location', '9', '旅店', null, null, '', '【地点信息】旅店\n区域：小镇\n描述：两层木质建筑，一楼是嘈杂的公共酒廊，二楼是狭小的客房。壁炉里的火永远烧不旺，空气中弥漫着廉价朗姆酒、汗水和发霉地毯混合的气味。公告板上贴满了寻人启事和过期的船期表。\n防御值：2', 'feedbacked', '1', '1', '2026-05-22 18:50:32', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('21', '17', 'zzz', '统治者', '1', 'investigate_player', '8', '兔兔', null, null, '', '【调查结果】兔兔的自由行动：\n行动1：调查玩家 → 凭栏择雨\n行动2：调查玩家 → 千代\n\n未能探知兔兔的阵营行动（概率未中）。\n', 'feedbacked', '1', '1', '2026-05-22 19:01:42', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('22', '17', 'zzz', '统治者', '2', 'go_location', '10', '集市', null, null, '', '【地点信息】集市\n区域：小镇\n描述：镇中心的露天广场，只有零星几个木制摊位。平时冷冷清清，但当渔船归来或有补给船消息时，这里会短暂地热闹起来。\n防御值：0\n\n【设施】\n• 行刑台：统治者或者其他阵营可以在这里进行公开行刑行为\n\n【NPC】\n• 塞缪尔·格雷（农户）\n• 弗雷德里克·波特（农户）\n• 米玛·雷铁斯托（手工艺人）\n• 汉斯·施密特（工匠）', 'feedbacked', '1', '1', '2026-05-22 19:01:44', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('23', '25', 'tony', '平民', '1', 'use_skill', null, null, null, null, '先接受来自统治者的物资交易。然后使用【烘培】技能获得两个面包。消耗资源14单位的食物，30kg的木材（被动特性食物消耗为1.5来计算，荷叶认证过的）。', '等待DM反馈\n\n【DM反馈】\n成功', 'feedbacked', '1', '1', '2026-05-22 19:08:45', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('24', '25', 'tony', '平民', '2', 'use_skill', null, null, null, null, '再次进行一样的行动，消耗14单位食物，30kg木材，获得两个面包。', '等待DM反馈\n\n【DM反馈】\n成功', 'feedbacked', '1', '1', '2026-05-22 19:12:35', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('27', '9', '对酒', '冒险者', '1', 'go_location', '15', '伐木营地', null, '托马斯·伍德', '托马斯兄弟，加入我们探索新的世界，看看你手里的斧刃——它不该用来给统治者的避难所添柴，而该劈开冻结的海面，劈出一条属于我们的生路！外面有漫无边际的原始森林，有热腾腾的炉火和自由的风，我一起造船冲出去，把你的名字刻在新世界的第一根栋梁上！站起来，兄弟，别给这座坟墓陪葬，咱们把命运劈成两半——一半留给这该死的雪，另一半烧成黎明。（拉拢托马斯·伍德，为我们生产木材，如果他愿意的话可以上船）', '【地点信息】伐木营地\n区域：海岛\n描述：岛内森林边缘的一片空地，堆满了砍伐的原木，有一座简易的木屋和一台生锈的蒸汽拖拉机。地上满是木屑和树桩。\n防御值：4\n\n【设施】\n• 木板蒸汽箱：用于木材处理的蒸汽箱设备\n• 拖拉机：在伐木行动时辅助工作的蒸汽拖拉机\n• 发电机：伐木营地配备的发电机组\n\n【NPC】\n• 托马斯·伍德（伐木工）\n\n【NPC互动】托马斯·伍德（伐木工）\n态度：忽视\n介绍：沉默寡言的伐木工，靠砍树和做木工为生，只求安稳度日。', 'feedbacked', '1', '1', '2026-05-22 19:16:02', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('28', '9', '对酒', '冒险者', '2', 'go_location', '18', '矿场', null, '卡尔·铁锤', '卡尔兄弟，别把热血冻死在这座死岛上，灾难会吞噬这里的一切，加入我们驶向新的家园，我们会给你最好的待遇。（拉拢矿工生产，他愿意的话可以上船）', '【地点信息】矿场\n区域：特殊\n描述：深入山腹的矿道与地面设施的综合体，包含管理室、矿场仓库和地下矿场。由统治者共同管理，是岛上最重要的资源产地和战略要地。\n防御值：10\n管理方：统治者共同管理\n\n【设施】\n• 切石机：矿场的石材切割设备\n• 管理室：矿场入口旁的木屋，里面有一张办公桌和一部电话（但线路已断）。墙上挂着矿井地图和工作安排表。\n• 矿场仓库：一个用厚木板搭建的棚屋，里面堆放着开采出来的矿石、工具和一些备用木材。门上挂着一把大锁。\n• 地下矿场（避难所）：深入山腹的矿道，墙壁上钉着木支架，每隔一段有一盏昏暗的油灯。深处被清理出一片空间，堆放着储备物资，这里就是计划中的\"避难所\"。\n\n【NPC】\n• 卡尔·铁锤（矿工）\n• 维克多·斯通（矿工）\n\n【NPC互动】卡尔·铁锤（矿工）\n态度：忽视\n介绍：脾气火爆的矿场工人，谁给好处就帮谁。', 'feedbacked', '1', '1', '2026-05-22 19:16:02', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('29', '14', 'Κάκτος西里尔', '冒险者', '1', 'go_location', '3', '邮局', null, null, '调查地点，看看有没有物资或者发电机', '【地点信息】邮局\n区域：小镇\n描述：一间低矮的木屋，窗前挂着\"皇家邮政\"的铜牌。屋内满是油墨和纸张的气味，木制柜台后是分拣信件的格子和一台莫尔斯电报机。墙上贴着轮船班次表和邮票样张。\n防御值：3\n\n【设施】\n• 电报机：可以向外界发送信息的莫尔斯电报机', 'feedbacked', '1', '1', '2026-05-22 19:38:43', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('30', '14', 'Κάκτος西里尔', '冒险者', '2', 'go_location', '4', '教堂', null, null, '调查地点，看看有没有物资或者发电机', '【地点信息】教堂\n区域：小镇\n描述：一座用珊瑚石和木材建造的小教堂，彩色玻璃窗描绘着基督与太平洋岛屿的景象。尖顶上的十字架在阳光下泛着白漆剥落后的斑驳。教堂内长椅简陋，但祭坛前摆着一架巨大的老旧管风琴，积满灰尘。\n防御值：4', 'feedbacked', '1', '1', '2026-05-22 19:38:44', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('34', '24', '花海', '反叛者', '1', 'produce', null, null, null, null, '', '【生产】使用牲畜设施，获得食物15kg\n等待DM结算后物资将发放到您的背包中\n\n【生产结算】已获得石料 15kg', 'feedbacked', '1', '1', '2026-05-22 20:00:51', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('35', '24', '花海', '反叛者', '2', 'use_trait', null, null, null, null, '百宝袋，对燃料使用百宝袋', '等待DM反馈\n\n【DM反馈】\n完成', 'feedbacked', '1', '1', '2026-05-22 20:00:51', '2026-05-22 22:07:50');
INSERT INTO `player_action` VALUES ('36', '22', '11', '天灾使者', '1', 'investigate_player', '12', '千代', null, null, '调查阵营信息', '【调查结果】千代的自由行动：\n行动1：使用职业技能\n行动2：调查玩家 → MISD330\n\n未能探知千代的阵营行动（概率未中）。\n', 'feedbacked', '1', '1', '2026-05-22 20:04:26', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('37', '22', '11', '天灾使者', '2', 'use_skill', null, null, null, null, '潜水，1D6', '等待DM反馈\n\n【DM反馈】\n完成', 'feedbacked', '1', '1', '2026-05-22 20:04:26', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('38', '19', 'unPy-GPT', '冒险者', '1', 'use_skill', null, null, null, null, '从码头使用渔船出海捕鱼', '等待DM反馈\n\n【DM反馈】\n这是使用生产的，下次直接生产就好。', 'feedbacked', '1', '1', '2026-05-22 20:11:37', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('39', '19', 'unPy-GPT', '冒险者', '2', 'investigate_player', '20', '追枫', null, null, '', '【调查结果】追枫的自由行动：\n行动1：使用职业技能\n行动2：前往地点 → 猎人小屋\n\n未能探知追枫的阵营行动（概率未中）。\n\n【DM反馈】\n【调查结果】追枫的自由行动：\n他会潜行，你无法调查', 'feedbacked', '1', '1', '2026-05-22 20:11:37', '2026-05-22 23:48:10');
INSERT INTO `player_action` VALUES ('43', '13', '凭栏择雨', '反叛者', '1', 'hide', null, null, null, null, '', '【隐藏】您将进入隐藏状态，明天将无法被调查、私聊或成为统治者与密谋的行动目标', 'feedbacked', '1', '1', '2026-05-22 20:25:47', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('44', '13', '凭栏择雨', '反叛者', '2', 'go_location', '10', '集市', null, '汉斯·施密特', '和大家汇合，商讨事项', '【地点信息】集市\n区域：小镇\n描述：镇中心的露天广场，只有零星几个木制摊位。平时冷冷清清，但当渔船归来或有补给船消息时，这里会短暂地热闹起来。\n防御值：0\n\n【设施】\n• 行刑台：统治者或者其他阵营可以在这里进行公开行刑行为\n\n【NPC】\n• 塞缪尔·格雷（农户）\n• 弗雷德里克·波特（农户）\n• 米玛·雷铁斯托（手工艺人）\n• 汉斯·施密特（工匠）\n\n【NPC互动】汉斯·施密特（工匠）\n态度：忽视\n介绍：什么都能修的工匠，从钟表到农具都难不倒他，只认工钱不认人。', 'feedbacked', '1', '1', '2026-05-22 20:25:47', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('45', '20', '追枫', '天灾使者', '1', 'use_skill', null, null, null, null, '狩猎，打五块肉，打完以后把武器献祭了', '等待DM反馈\n\n【DM反馈】\n成功', 'feedbacked', '1', '1', '2026-05-22 20:27:57', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('46', '20', '追枫', '天灾使者', '2', 'go_location', '17', '猎人小屋', null, null, '', '【地点信息】猎人小屋\n区域：海岛\n描述：森林深处的一座原木小屋，墙外挂着各种兽皮，屋内弥漫着熏肉和火药的味道。壁炉上挂着一支双管猎枪。\n防御值：3', 'feedbacked', '1', '1', '2026-05-22 20:27:59', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('47', '12', '千代', '平民', '1', 'use_skill', null, null, null, null, '使用启蒙“捕鱼”，教学我、gpt（采珠人）、择雨（旅店老板）', '等待DM反馈\n\n【DM反馈】\n结算完毕', 'feedbacked', '1', '1', '2026-05-22 20:32:46', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('48', '12', '千代', '平民', '2', 'investigate_player', '30', 'MISD330', null, null, '调查医生', '【调查结果】MISD330的自由行动：\n行动1：调查玩家 → 兔兔\n行动2：调查玩家 → 蟋蟀蜥蜴\n\nMISD330的阵营行动：\n（当日未提交阵营行动）\n', 'feedbacked', '1', '1', '2026-05-22 20:32:46', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('49', '10', '二阶堂希罗', '统治者', '1', 'other', null, null, null, null, '将码头仓库物资中运500Kg转入矿产仓库，并保留1个鱼叉及30Kg的食物到个人', '等待DM反馈\n\n【DM反馈】\n完成', 'feedbacked', '1', '1', '2026-05-22 20:39:28', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('50', '10', '二阶堂希罗', '统治者', '2', 'other', null, null, null, null, '将燃料仓库中500Kg搬运至矿厂仓库并保留20Kg到个人', '等待DM反馈\n\n【DM反馈】\n完成', 'feedbacked', '1', '1', '2026-05-22 20:39:29', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('51', '16', '孤城暮角', '天灾使者', '1', 'use_skill', null, null, null, null, '手工艺\n猎弓×1，仿制枪×1，复合盾×1（在枪把上刻上“二房”的名字）', '等待DM反馈\n\n【DM反馈】\n猎弓和复合盾成功，但是仿制枪你不会做', 'feedbacked', '1', '1', '2026-05-22 20:40:14', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('52', '16', '孤城暮角', '天灾使者', '2', 'go_location', '9', '旅店', null, null, '', '【地点信息】旅店\n区域：小镇\n描述：两层木质建筑，一楼是嘈杂的公共酒廊，二楼是狭小的客房。壁炉里的火永远烧不旺，空气中弥漫着廉价朗姆酒、汗水和发霉地毯混合的气味。公告板上贴满了寻人启事和过期的船期表。\n防御值：2', 'feedbacked', '1', '1', '2026-05-22 20:40:14', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('53', '30', 'MISD330', '平民', '1', 'investigate_player', '8', '兔兔', null, null, '', '【调查结果】兔兔的自由行动：\n行动1：调查玩家 → 凭栏择雨\n行动2：调查玩家 → 千代\n\n未能探知兔兔的阵营行动（概率未中）。\n', 'feedbacked', '1', '1', '2026-05-22 20:50:19', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('54', '30', 'MISD330', '平民', '2', 'investigate_player', '11', '蟋蟀蜥蜴', null, null, '', '【调查结果】蟋蟀蜥蜴的自由行动：\n行动1：前往地点 → 警察局\n行动2：调查玩家 → 二阶堂希罗\n\n蟋蟀蜥蜴的阵营行动：\n阵营行动1：额外行动：extra_action\n', 'feedbacked', '1', '1', '2026-05-22 20:50:20', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('55', '8', '兔兔', '平民', '1', 'investigate_player', '13', '凭栏择雨', null, null, '', '【调查结果】凭栏择雨的自由行动：\n行动1：隐藏\n行动2：前往地点 → 集市\n\n凭栏择雨的阵营行动：\n阵营行动1：额外行动：extra_action\n', 'feedbacked', '1', '1', '2026-05-22 20:52:56', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('56', '8', '兔兔', '平民', '2', 'investigate_player', '12', '千代', null, null, '', '【调查结果】千代的自由行动：\n行动1：使用职业技能\n行动2：调查玩家 → MISD330\n\n千代的阵营行动：\n（当日未提交阵营行动）\n', 'feedbacked', '1', '1', '2026-05-22 20:52:57', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('57', '31', '闲屿', '平民', '1', 'go_location', '7', '码头', null, '克拉拉·南丁格尔', '询问是否有在码头遇到过值得注意的人或事，同时希望以为他提供劳动，让他能给自己一些食物', '【地点信息】码头\n区域：小镇\n描述：有着几间木质简陋房屋和用旧木桩搭建的简陋码头，停着几艘渔船和一艘锈迹斑斑的货船。海浪拍打着木桩，发出单调的响声。远处海平面阴沉沉的。\n防御值：3\n\n【设施】\n• 渔船×3：三艘渔船，渔猎技能需要\n• 码头集购仓：需征求统治者同意，玩家可以询问统治者并购买物品\n• 阿弗雷号：轻型杂货船，总吨位约650吨，载重吨位约300吨，全长约42米，型宽约7米，吃水满载约4.2米，燃料30吨。配备螺旋桨2、发动机2、发电机1\n\n【NPC】\n• 克拉拉·南丁格尔（渔民）\n• 杰克·塔克（水手）\n• 鲍勃·塔克（装卸工）\n\n【NPC互动】克拉拉·南丁格尔（渔民）\n态度：忽视\n介绍：一位家中贫困的普通渔民，只希望镇上保持平静。', 'feedbacked', '1', '1', '2026-05-22 21:29:38', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('58', '31', '闲屿', '平民', '2', 'investigate_player', '8', '兔兔', null, null, '', '【调查结果】兔兔的自由行动：\n行动1：调查玩家 → 凭栏择雨\n行动2：调查玩家 → 千代\n\n兔兔的阵营行动：\n（当日未提交阵营行动）\n', 'feedbacked', '1', '1', '2026-05-22 21:29:38', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('59', '32', '澡堂子', '平民', '1', 'go_location', '11', '酒吧', null, null, '寻找酒精', '【地点信息】酒吧\n区域：小镇\n描述：码头边的一间木质房屋昏暗的空间，看着门板还很结实里面有几盏油灯。这里是渔夫和矿工买醉的地方，墙上刻满了粗鄙的涂鸦。角落里有一台破旧的留声机，吱呀呀地放着过时的爵士乐。\n防御值：3', 'feedbacked', '1', '1', '2026-05-22 21:30:30', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('60', '32', '澡堂子', '平民', '2', 'go_location', '16', '墓地', null, null, '找我需要的物资', '【地点信息】墓地\n区域：海岛\n描述：墓地很静，石碑像断掉的牙齿从荒草里斜伸出来。风扫过时，只有自己的脚步声在回应。\n防御值：5\n\n【设施】\n• 坟堆：为了死者一个体面的后事', 'feedbacked', '1', '1', '2026-05-22 21:30:30', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('61', '11', '蟋蟀蜥蜴', '反叛者', '1', 'go_location', '1', '警察局', null, null, '表面是是去上班 问一下小镇最近有没有不太平的事情 把锅全都推给统治者', '【地点信息】警察局\n区域：小镇\n描述：一座木铁混合结构的平房，瓦楞铁皮屋顶，外墙刷着褪色的白漆。门廊上挂着一盏摇曳的煤油灯，屋内有一张办公桌、一个档案柜和一间狭小的临时牢房。墙上贴着殖民地政府的告示和几张泛黄的通缉令。\n防御值：5\n\n【设施】\n• 燃料仓：一个巨大的铁皮储油罐，旁边是几排油桶。这里是全镇的能源命脉，由警察局派员看守。铁门上挂着大锁，周围拉着铁丝网。\n• 发电机：警察局配备的发电机组', 'feedbacked', '1', '1', '2026-05-22 21:31:52', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('62', '11', '蟋蟀蜥蜴', '反叛者', '2', 'investigate_player', '10', '二阶堂希罗', null, null, '调查一下他去了哪里 都和谁说了话', '【调查结果】二阶堂希罗的自由行动：\n行动1：其他\n行动2：其他\n\n二阶堂希罗的阵营行动：\n阵营行动1：安排看守：派遣 花海 看守 镇长厅\n阵营行动2：安排看守：派遣 千代 看守 矿场\n', 'feedbacked', '1', '1', '2026-05-22 21:31:52', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('63', '26', 'V', '统治者', '1', 'transport', null, null, null, null, '[mode:warehouse_to_warehouse]\n[source:fuel]\n[dest:armory]\n[item:item|2|8|0.3]\n[item:item|13|20|0.1]\n[item:material|2|265|1]\n[item:material|6|30|1]\n[item:material|8|200|1]', '【搬运结算】\n模式：仓库→仓库\n源仓库：燃料仓库\n目标仓库：镇武库\n手电筒：搬运8单位（2千克）\n蜡烛：搬运20单位（2千克）\n木材：搬运265单位（265千克）\n沥青：搬运30单位（30千克）\n燃料：搬运200单位（200千克）\n总计搬运：499千克\n（库存已变更）', 'feedbacked', '1', '1', '2026-05-22 21:34:28', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('64', '26', 'V', '统治者', '2', 'go_location', '10', '集市', null, '汉斯·施密特', '', '【地点信息】集市\n区域：小镇\n描述：镇中心的露天广场，只有零星几个木制摊位。平时冷冷清清，但当渔船归来或有补给船消息时，这里会短暂地热闹起来。\n防御值：0\n\n【设施】\n• 行刑台：统治者或者其他阵营可以在这里进行公开行刑行为\n\n【NPC】\n• 塞缪尔·格雷（农户）\n• 弗雷德里克·波特（农户）\n• 米玛·雷铁斯托（手工艺人）\n• 汉斯·施密特（工匠）\n\n【NPC互动】汉斯·施密特（工匠）\n态度：喜好\n介绍：什么都能修的工匠，从钟表到农具都难不倒他，只认工钱不认人。', 'feedbacked', '1', '1', '2026-05-22 21:34:28', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('65', '21', '乐语', '平民', '1', 'go_location', '7', '码头', null, '克拉拉·南丁格尔', '询问小镇中载具的信息，询问能否获得一些满足温饱的食物，希望交换一些打捞上来的特殊物品', '【地点信息】码头\n区域：小镇\n描述：有着几间木质简陋房屋和用旧木桩搭建的简陋码头，停着几艘渔船和一艘锈迹斑斑的货船。海浪拍打着木桩，发出单调的响声。远处海平面阴沉沉的。\n防御值：3\n\n【设施】\n• 渔船×3：三艘渔船，渔猎技能需要\n• 码头集购仓：需征求统治者同意，玩家可以询问统治者并购买物品\n• 阿弗雷号：轻型杂货船，总吨位约650吨，载重吨位约300吨，全长约42米，型宽约7米，吃水满载约4.2米，燃料30吨。配备螺旋桨2、发动机2、发电机1\n\n【NPC】\n• 克拉拉·南丁格尔（渔民）\n• 杰克·塔克（水手）\n• 鲍勃·塔克（装卸工）\n\n【NPC互动】克拉拉·南丁格尔（渔民）\n态度：忽视\n介绍：一位家中贫困的普通渔民，只希望镇上保持平静。', 'feedbacked', '1', '1', '2026-05-22 21:35:37', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('66', '21', '乐语', '平民', '2', 'go_location', '18', '矿场', null, '维克多·斯通', '搜集一些散落的资源。询问矿工需不需要我帮忙带口信，需要的话收取一点的报酬。搜集一些有益的关于小镇的其他情报', '【地点信息】矿场\n区域：特殊\n描述：深入山腹的矿道与地面设施的综合体，包含管理室、矿场仓库和地下矿场。由统治者共同管理，是岛上最重要的资源产地和战略要地。\n防御值：10\n管理方：统治者共同管理\n\n【设施】\n• 切石机：矿场的石材切割设备\n• 管理室：矿场入口旁的木屋，里面有一张办公桌和一部电话（但线路已断）。墙上挂着矿井地图和工作安排表。\n• 矿场仓库：一个用厚木板搭建的棚屋，里面堆放着开采出来的矿石、工具和一些备用木材。门上挂着一把大锁。\n• 地下矿场（避难所）：深入山腹的矿道，墙壁上钉着木支架，每隔一段有一盏昏暗的油灯。深处被清理出一片空间，堆放着储备物资，这里就是计划中的\"避难所\"。\n\n【NPC】\n• 卡尔·铁锤（矿工）\n• 维克多·斯通（矿工）\n\n【NPC互动】维克多·斯通（矿工）\n态度：忽视\n介绍：体格强壮的矿工，相信权力才是活下去的依靠。', 'feedbacked', '1', '1', '2026-05-22 21:35:37', '2026-05-22 22:07:29');
INSERT INTO `player_action` VALUES ('67', '15', '空白', '统治者', '1', 'transport', null, null, null, null, '[mode:warehouse_to_warehouse]\n[source:fuel]\n[dest:armory]\n[item:item|15|2|0.2]\n[item:material|2|479|1]\n[item:material|6|20|1]', '【搬运结算】\n模式：仓库→仓库\n源仓库：燃料仓库\n目标仓库：镇武库\n火柴：搬运2单位（0千克）\n木材：搬运479单位（479千克）\n沥青：搬运20单位（20千克）\n总计搬运：499千克\n（库存已变更）', 'feedbacked', '1', '1', '2026-05-22 22:14:40', '2026-05-22 22:32:30');
INSERT INTO `player_action` VALUES ('68', '15', '空白', '统治者', '2', 'transport', null, null, null, null, '[mode:warehouse_to_warehouse]\n[source:dock]\n[dest:armory]\n[item:item|1|2|0.5]\n[item:item|7|1|1]\n[item:item|10|20|1]\n[item:item|11|3|0.5]\n[item:item|12|1|0.5]\n[item:item|14|5|1]\n[item:item|17|1|0.3]\n[item:item|18|1|0.5]\n[item:weapon|6|2|2]\n[item:ammo|3|2|0.1]\n[item:material|5|300|1]\n[item:material|11|3|1]', '【搬运结算】\n模式：仓库→仓库\n源仓库：码头集购仓\n目标仓库：镇武库\n医疗包：搬运2单位（1千克）\n信号枪：搬运1单位（1千克）\n朗姆酒：搬运20单位（20千克）\n草药：搬运3单位（1千克）\n渔网：搬运1单位（0千克）\n医用酒精：搬运5单位（5千克）\n破损海图：搬运1单位（0千克）\n便当：搬运1单位（0千克）\n鱼叉/矛：搬运2单位（4千克）\n信号弹：搬运2单位（0千克）\n食物：搬运300单位（300千克）\n螺旋桨：搬运3单位（3千克）\n总计搬运：335千克\n（库存已变更）', 'feedbacked', '1', '1', '2026-05-22 22:14:40', '2026-05-22 22:32:30');
INSERT INTO `player_action` VALUES ('69', '18', 'Missbear', '平民', '1', 'go_location', '17', '猎人小屋', null, null, '', '【地点信息】猎人小屋\n区域：海岛\n描述：森林深处的一座原木小屋，墙外挂着各种兽皮，屋内弥漫着熏肉和火药的味道。壁炉上挂着一支双管猎枪。\n防御值：3', 'feedbacked', '1', '1', '2026-05-22 23:43:44', '2026-05-22 23:48:23');
INSERT INTO `player_action` VALUES ('70', '18', 'Missbear', '平民', '2', 'hide', null, null, null, null, '', '【隐藏】您将进入隐藏状态，明天将无法被调查、私聊或成为统治者与密谋的行动目标', 'feedbacked', '1', '1', '2026-05-22 23:43:44', '2026-05-22 23:48:23');
INSERT INTO `player_action` VALUES ('71', '8', '兔兔', '天灾使者', '1', 'investigate_player', '10', '二阶堂希罗', null, null, '', '【调查结果】二阶堂希罗的自由行动：\n行动1：搬运\n行动2：前往地点 → 矿场\n\n二阶堂希罗的阵营行动：\n（当日未提交阵营行动）\n', 'feedbacked', '1', '2', '2026-05-23 15:42:40', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('72', '8', '兔兔', '天灾使者', '2', 'investigate_player', '26', 'V', null, null, '', '【调查结果】V的自由行动：\n行动1：搬运\n行动2：调查玩家 → 咲黑\n\nV的阵营行动：\n（当日未提交阵营行动）\n', 'feedbacked', '1', '2', '2026-05-23 15:42:40', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('73', '18', 'Missbear', '平民', '1', 'investigate_player', '15', '空白', null, null, '', '【调查结果】空白的自由行动：\n行动1：使用职业技能\n行动2：搬运\n\n空白的阵营行动：\n（当日未提交阵营行动）\n', 'feedbacked', '1', '2', '2026-05-23 16:09:56', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('74', '18', 'Missbear', '平民', '2', 'investigate_player', '27', '得狗的老意', null, null, '', '【调查结果】得狗的老意的自由行动：\n（当日未提交个人行动）\n\n得狗的老意的阵营行动：\n（当日未提交阵营行动）\n', 'feedbacked', '1', '2', '2026-05-23 16:09:56', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('75', '28', '咲黑', '平民', '1', 'investigate_player', '15', '空白', null, null, '', '【调查结果】空白的自由行动：\n行动1：使用职业技能\n行动2：搬运\n\n空白的阵营行动：\n（当日未提交阵营行动）\n\n【DM反馈】\n【调查结果】空白的自由行动：\n行动1：使用职业技能\n行动2：搬运\n\n空白的阵营行动：\n（当日未提交阵营行动）', 'feedbacked', '1', '2', '2026-05-23 19:07:44', '2026-05-23 22:17:48');
INSERT INTO `player_action` VALUES ('76', '28', '咲黑', '平民', '2', 'investigate_player', '29', '飞凡', null, null, '', '【调查结果】飞凡的自由行动：\n（当日未提交个人行动）\n\n未能探知飞凡的阵营行动（概率未中）。\n\n【DM反馈】\n【调查结果】飞凡的自由行动：\n（当日未提交个人行动）\n他被选为为劳工\n未能探知飞凡的阵营行动（概率未中）。', 'feedbacked', '1', '2', '2026-05-23 19:07:44', '2026-05-23 22:17:48');
INSERT INTO `player_action` VALUES ('77', '11', '蟋蟀蜥蜴', '反叛者', '1', 'go_location', '19', '监狱', null, '乔克·汤姆', '给他五瓶酒 让他说出一些情报并且喝醉离开', '【地点信息】监狱\n区域：特殊\n描述：小镇边缘的一座灰石建筑，铁门锈迹斑斑，窗户窄得像枪眼。门前挂着一盏永远不灭的煤油灯，灯下总坐着一个看守。里面是两排铁牢房，地上铺着发霉的稻草，墙角堆着脏得看不出颜色的毯子。墙上用木炭刻满了前囚犯的名字和诅咒，有些已经被重复刻了三四遍。空气里弥漫着尿骚味和铁锈味，偶尔有人敲一下铁栏杆，声音能传到半个镇子。\n防御值：8\n管理方：统治者共同管理，监狱长常驻\n\n【设施】\n• 监牢×8：地面上有着不明污渍的铁制监牢，看起来很牢固几乎不可能在空手情况下跑出去。里面关着几个人，听说是因为违反统治者被关起来了。\n• 看守室：内有桌子一张、煤油灯一盏、警棍一根\n• 审讯椅：铁制，带锁扣的审讯椅\n\n【NPC】\n• 乔克·汤姆（民兵）\n\n【NPC互动】乔克·汤姆（民兵）\n态度：厌恶\n介绍：初始就跟着统治者干的监狱看守，一名很忠诚的下属。只是他有点小小的缺点，但统治者们也只能视而不见。', 'feedbacked', '1', '2', '2026-05-23 19:33:24', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('78', '11', '蟋蟀蜥蜴', '反叛者', '2', 'go_location', '19', '监狱', null, null, '劫狱', '【地点信息】监狱\n区域：特殊\n描述：小镇边缘的一座灰石建筑，铁门锈迹斑斑，窗户窄得像枪眼。门前挂着一盏永远不灭的煤油灯，灯下总坐着一个看守。里面是两排铁牢房，地上铺着发霉的稻草，墙角堆着脏得看不出颜色的毯子。墙上用木炭刻满了前囚犯的名字和诅咒，有些已经被重复刻了三四遍。空气里弥漫着尿骚味和铁锈味，偶尔有人敲一下铁栏杆，声音能传到半个镇子。\n防御值：8\n管理方：统治者共同管理，监狱长常驻\n\n【设施】\n• 监牢×8：地面上有着不明污渍的铁制监牢，看起来很牢固几乎不可能在空手情况下跑出去。里面关着几个人，听说是因为违反统治者被关起来了。\n• 看守室：内有桌子一张、煤油灯一盏、警棍一根\n• 审讯椅：铁制，带锁扣的审讯椅\n\n【NPC】\n• 乔克·汤姆（民兵）', 'feedbacked', '1', '2', '2026-05-23 19:33:24', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('79', '20', '追枫', '天灾使者', '1', 'go_location', '2', '镇长厅', null, null, '', '【地点信息】镇长厅\n区域：小镇\n描述：两层殖民风格木楼，带有宽敞的阳台和百叶窗。楼下是办公室和接待室，楼上是行政长官的私人住所。墙上挂着英王乔治六世的肖像和殖民地地图。吊扇无力地转动着。\n防御值：5\n\n【设施】\n• 镇武库仓库：镇长厅内的武器库，存放着小镇的武装储备\n• 发电机：镇长厅配备的发电机组', 'feedbacked', '1', '2', '2026-05-23 19:39:58', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('80', '20', '追枫', '天灾使者', '2', 'investigate_player', '23', '教皇', null, null, '', '【调查结果】教皇的自由行动：\n行动1：使用职业技能\n行动2：前往地点 → 监狱\n\n未能探知教皇的阵营行动（概率未中）。\n', 'feedbacked', '1', '2', '2026-05-23 19:39:58', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('81', '24', '花海', '反叛者', '1', 'produce', null, null, null, null, '', '【生产】使用牲畜设施，获得食物15kg\n等待DM结算后物资将发放到您的背包中\n\n【生产结算】已获得石料 15kg', 'feedbacked', '1', '2', '2026-05-23 19:49:20', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('82', '24', '花海', '反叛者', '2', 'use_trait', null, null, null, null, '百宝袋/复制列表的5个初始物品，我要负责防弹衣、医疗物品，金属，燃料剩余的随机', '等待DM反馈\n\n【DM反馈】\n你将获得一件防弹衣，一个医疗物品，1kg金属和1kg燃料还有随机的一支铅笔', 'feedbacked', '1', '2', '2026-05-23 19:49:20', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('85', '14', 'Κάκτος西里尔', '冒险者', '1', 'go_location', '15', '伐木营地', null, '托马斯·伍德', '自己带着霰弹枪和弹药1发，与维修工（玩家）和气象观测员（玩家）一起前往伐木营地。\n\n先和伐木工（npc）进行一个恳求，说明现在暴风雪来临，我们很需要他的帮助，需要木板蒸汽箱，拖拉机，电锯，木板（或者原木），发电机组（相当于搜刮地点），并且和他说镇长也同意我们求取这些，给了我们这些就不会强迫他当劳工了。\n\n如果他愿意就邀请他上船，本人会适当展示自己手中的霰弹枪。\n\n若伐木工（npc）始终不愿意，本人将拿枪威胁他，最起码把载具和发电机交出来。\n\n若再不同意，就把他束缚起来，拿走载具和发电机组。\n\n最坏的情况，如果他实在反抗激烈，就枪杀他，以无论如何都要拿到载具和发电机组为目的。', '【地点信息】伐木营地\n区域：海岛\n描述：岛内森林边缘的一片空地，堆满了砍伐的原木，有一座简易的木屋和一台生锈的蒸汽拖拉机。地上满是木屑和树桩。\n防御值：4\n\n【设施】\n• 木板蒸汽箱：用于木材处理的蒸汽箱设备\n• 拖拉机：在伐木行动时辅助工作的蒸汽拖拉机\n• 发电机：伐木营地配备的发电机组\n\n【NPC】\n• 托马斯·伍德（伐木工）\n\n【NPC互动】托马斯·伍德（伐木工）\n态度：忽视\n介绍：沉默寡言的伐木工，靠砍树和做木工为生，只求安稳度日。\n\n【DM反馈】\n【NPC互动】托马斯·伍德（伐木工）\n态度：忽视\n介绍：沉默寡言的伐木工，靠砍树和做木工为生，只求安稳度日。\n伐木工已被对酒威胁', 'feedbacked', '1', '2', '2026-05-23 19:55:02', '2026-05-23 22:17:48');
INSERT INTO `player_action` VALUES ('86', '14', 'Κάκτος西里尔', '冒险者', '2', 'go_location', '10', '集市', null, '米玛·雷铁斯托', '先问手工艺人找我有什么事，听镇长说手工艺人有事找我。\n\n然后问手工艺人手中是否有沥青，如果可以的话向手工艺人要些沥青。如果不行就问对方哪里能搞到沥青。', '【地点信息】集市\n区域：小镇\n描述：镇中心的露天广场，只有零星几个木制摊位。平时冷冷清清，但当渔船归来或有补给船消息时，这里会短暂地热闹起来。\n防御值：0\n\n【设施】\n• 行刑台：统治者或者其他阵营可以在这里进行公开行刑行为\n\n【NPC】\n• 塞缪尔·格雷（农户）\n• 弗雷德里克·波特（农户）\n• 米玛·雷铁斯托（手工艺人）\n• 汉斯·施密特（工匠）\n\n【NPC互动】米玛·雷铁斯托（手工艺人）\n态度：喜好\n介绍：老实本分的手工艺人，喜欢待在自己的小屋偶尔出门。\n\n【DM反馈】\n【NPC互动】米玛·雷铁斯托（手工艺人）\n态度：喜好\n介绍：老实本分的手工艺人，喜欢待在自己的小屋偶尔出门。\n他跟你走了还给了你20kg沥青，也许你也可以让他做一些物品。只要给他吃的和取暖以及基本材料就可以。', 'feedbacked', '1', '2', '2026-05-23 19:55:03', '2026-05-23 22:17:48');
INSERT INTO `player_action` VALUES ('87', '10', '二阶堂希罗', '统治者', '1', 'transport', null, null, null, null, '[mode:warehouse_to_player]\n[source:armory]\n[item:item|1|2|0.5]\n[item:item|2|8|0.3]\n[item:item|3|2|0.5]\n[item:item|5|1|2]\n[item:item|6|4|3]\n[item:item|7|1|1]\n[item:item|10|20|1]\n[item:item|11|3|0.5]\n[item:item|12|1|0.5]\n[item:item|13|20|0.1]\n[item:item|14|5|1]\n[item:item|15|2|0.2]\n[item:item|17|1|0.3]\n[item:item|18|1|0.5]\n[item:weapon|1|2|2]\n[item:weapon|2|1|3]\n[item:weapon|3|3|1]\n[item:weapon|4|2|1]\n[item:weapon|6|2|2]\n[item:weapon|7|1|2]\n[item:ammo|1|4|0.1]\n[item:ammo|2|2|0.1]\n[item:ammo|3|2|0.1]\n[item:ammo|4|4|0.1]\n[item:material|6|50|1]\n[item:material|8|178|1]\n[item:material|11|3|1]', '【搬运结算】\n模式：仓库→个人\n源仓库：镇武库\n医疗包：搬运2单位到个人背包（1千克）\n手电筒：搬运8单位到个人背包（2千克）\n手铐：搬运2单位到个人背包（1千克）\n防弹衣：搬运1单位到个人背包（2千克）\n复合盾：搬运4单位到个人背包（12千克）\n信号枪：搬运1单位到个人背包（1千克）\n朗姆酒：搬运20单位到个人背包（20千克）\n草药：搬运3单位到个人背包（1千克）\n渔网：搬运1单位到个人背包（0千克）\n蜡烛：搬运20单位到个人背包（2千克）\n医用酒精：搬运5单位到个人背包（5千克）\n火柴：搬运2单位到个人背包（0千克）\n破损海图：搬运1单位到个人背包（0千克）\n便当：搬运1单位到个人背包（0千克）\n制式手枪：搬运2单位到个人背包（4千克）\n猎枪：搬运1单位到个人背包（3千克）\n警棍：搬运3单位到个人背包（3千克）\n刺刀：搬运2单位到个人背包（2千克）\n鱼叉/矛：搬运2单位到个人背包（4千克）\n猎弓：搬运1单位到个人背包（2千克）\n手枪弹：搬运4单位到个人背包（0千克）\n猎枪弹：搬运2单位到个人背包（0千克）\n信号弹：搬运2单位到个人背包（0千克）\n箭矢：搬运4单位到个人背包（0千克）\n沥青：搬运50单位到个人背包（50千克）\n燃料：搬运178单位到个人背包（178千克）\n螺旋桨：搬运3单位到个人背包（3千克）\n总计搬运：296千克\n（库存已变更）', 'feedbacked', '1', '2', '2026-05-23 19:57:45', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('88', '10', '二阶堂希罗', '统治者', '2', 'go_location', '18', '矿场', null, null, '', '【地点信息】矿场\n区域：特殊\n描述：深入山腹的矿道与地面设施的综合体，包含管理室、矿场仓库和地下矿场。由统治者共同管理，是岛上最重要的资源产地和战略要地。\n防御值：10\n管理方：统治者共同管理\n\n【设施】\n• 切石机：矿场的石材切割设备\n• 管理室：矿场入口旁的木屋，里面有一张办公桌和一部电话（但线路已断）。墙上挂着矿井地图和工作安排表。\n• 矿场仓库：一个用厚木板搭建的棚屋，里面堆放着开采出来的矿石、工具和一些备用木材。门上挂着一把大锁。\n• 地下矿场（避难所）：深入山腹的矿道，墙壁上钉着木支架，每隔一段有一盏昏暗的油灯。深处被清理出一片空间，堆放着储备物资，这里就是计划中的\"避难所\"。\n\n【NPC】\n• 卡尔·铁锤（矿工）\n• 维克多·斯通（矿工）', 'feedbacked', '1', '2', '2026-05-23 19:57:45', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('89', '22', '11', '冒险者', '1', 'other', null, null, null, null, '在码头拆船，并邀请码头的人跟我一起搬（搬到自己这）', '等待DM反馈\n\n【DM反馈】\n你无法邀请码头的人跟你一起搬，这需要额外一个行动点。你自己拆了船，拆了25%的船，目前进度25%。\n获得金属10000kg，木头10000kg', 'feedbacked', '1', '2', '2026-05-23 19:59:58', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('90', '22', '11', '冒险者', '2', 'use_trait', null, null, null, null, '使用天灾特性去削弱食物', '等待DM反馈\n\n【DM反馈】\n腐蚀目前仓库10%的食物', 'feedbacked', '1', '2', '2026-05-23 19:59:58', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('91', '17', 'zzz', '统治者', '1', 'transport', null, null, null, null, '[mode:warehouse_to_player]\n[source:general]\n[item:item|8|2|1]\n[item:weapon|8|2|3]\n[item:weapon|9|1|2]\n[item:material|5|270|1]\n[item:material|8|20|1]', '【搬运结算】\n模式：仓库→个人\n源仓库：矿场仓库\n维修工具包：搬运2单位到个人背包（2千克）\n十字镐：搬运2单位到个人背包（6千克）\n斧头：搬运1单位到个人背包（2千克）\n食物：搬运270单位到个人背包（270千克）\n燃料：搬运20单位到个人背包（20千克）\n总计搬运：300千克\n（库存已变更）', 'feedbacked', '1', '2', '2026-05-23 20:03:29', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('92', '17', 'zzz', '统治者', '2', 'investigate_player', '16', '孤城暮角', null, null, '', '【调查结果】孤城暮角的自由行动：\n（当日未提交个人行动）\n\n未能探知孤城暮角的阵营行动（概率未中）。\n', 'feedbacked', '1', '2', '2026-05-23 20:03:29', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('93', '30', 'MISD330', '反叛者', '1', 'go_location', '18', '矿场', null, '维克多·斯通', '与其沟通，阐述现在统治的残暴！尝试使用便当和承诺其他物品，拉拢他，换点金属，并请求协助夜晚打劫监狱', '【地点信息】矿场\n区域：特殊\n描述：深入山腹的矿道与地面设施的综合体，包含管理室、矿场仓库和地下矿场。由统治者共同管理，是岛上最重要的资源产地和战略要地。\n防御值：10\n管理方：统治者共同管理\n\n【设施】\n• 切石机：矿场的石材切割设备\n• 管理室：矿场入口旁的木屋，里面有一张办公桌和一部电话（但线路已断）。墙上挂着矿井地图和工作安排表。\n• 矿场仓库：一个用厚木板搭建的棚屋，里面堆放着开采出来的矿石、工具和一些备用木材。门上挂着一把大锁。\n• 地下矿场（避难所）：深入山腹的矿道，墙壁上钉着木支架，每隔一段有一盏昏暗的油灯。深处被清理出一片空间，堆放着储备物资，这里就是计划中的\"避难所\"。\n\n【NPC】\n• 卡尔·铁锤（矿工）\n• 维克多·斯通（矿工）\n\n【NPC互动】维克多·斯通（矿工）\n态度：喜好\n介绍：体格强壮的矿工，相信权力才是活下去的依靠。', 'feedbacked', '1', '2', '2026-05-23 20:05:48', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('94', '30', 'MISD330', '反叛者', '2', 'go_location', '10', '集市', null, '米玛·雷铁斯托', '与其沟通，阐述现在统治的残暴！尝试使用便当和承诺其他物品，拉拢他，制造盾牌，并请求协助夜晚打劫监狱', '【地点信息】集市\n区域：小镇\n描述：镇中心的露天广场，只有零星几个木制摊位。平时冷冷清清，但当渔船归来或有补给船消息时，这里会短暂地热闹起来。\n防御值：0\n\n【设施】\n• 行刑台：统治者或者其他阵营可以在这里进行公开行刑行为\n\n【NPC】\n• 塞缪尔·格雷（农户）\n• 弗雷德里克·波特（农户）\n• 米玛·雷铁斯托（手工艺人）\n• 汉斯·施密特（工匠）\n\n【NPC互动】米玛·雷铁斯托（手工艺人）\n态度：忽视\n介绍：老实本分的手工艺人，喜欢待在自己的小屋偶尔出门。', 'feedbacked', '1', '2', '2026-05-23 20:05:48', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('95', '19', 'unPy-GPT', '冒险者', '1', 'go_location', '7', '码头', null, '鲍勃·塔克', '塔克，你在这码头扛了半辈子麻袋，可攒下过真正属于自己的东西？这场暴风雪过后，港口都将不复存在。方舟是我们唯一的希望——跟我走，不需要你再为别人负重前行。冒险者的船上有你的位置，新世界的大地等着我们去征服。放下这个麻袋，拿起桨，当自己的主人。是冻死在这里，还是去创造未来，你选。', '【地点信息】码头\n区域：小镇\n描述：有着几间木质简陋房屋和用旧木桩搭建的简陋码头，停着几艘渔船和一艘锈迹斑斑的货船。海浪拍打着木桩，发出单调的响声。远处海平面阴沉沉的。\n防御值：3\n\n【设施】\n• 渔船×3：三艘渔船，渔猎技能需要\n• 码头集购仓：需征求统治者同意，玩家可以询问统治者并购买物品\n• 阿弗雷号：轻型杂货船，总吨位约650吨，载重吨位约300吨，全长约42米，型宽约7米，吃水满载约4.2米，燃料30吨。配备螺旋桨2、发动机2、发电机1\n\n【NPC】\n• 克拉拉·南丁格尔（渔民）\n• 杰克·塔克（水手）\n• 鲍勃·塔克（装卸工）\n\n【NPC互动】鲍勃·塔克（装卸工）\n态度：忽视\n介绍：一名一直在港口讨生活的搬运工。\n\n【DM反馈】\n【地点信息】码头\n区域：小镇\n描述：有着几间木质简陋房屋和用旧木桩搭建的简陋码头，停着几艘渔船和一艘锈迹斑斑的货船。海浪拍打着木桩，发出单调的响声。远处海平面阴沉沉的。\n防御值：3\n\n【设施】\n• 渔船×3：三艘渔船，渔猎技能需要\n• 码头集购仓：需征求统治者同意，玩家可以询问统治者并购买物品\n• 阿弗雷号：轻型杂货船，总吨位约650吨，载重吨位约300吨，全长约42米，型宽约7米，吃水满载约4.2米，燃料30吨。配备螺旋桨2、发动机2、发电机1\n\n【NPC】\n• 克拉拉·南丁格尔（渔民）\n• 杰克·塔克（水手）\n• 鲍勃·塔克（装卸工）\n\n【NPC互动】鲍勃·塔克（装卸工）\n态度：忽视\n介绍：一名一直在港口讨生活的搬运工。\n塔克泪流满面，感叹终于遇到了明主。', 'feedbacked', '1', '2', '2026-05-23 20:09:41', '2026-05-23 22:17:48');
INSERT INTO `player_action` VALUES ('96', '19', 'unPy-GPT', '冒险者', '2', 'other', null, null, null, null, '邀请装卸工一起在码头从阿弗雷号船上拆下可以用来建造方舟的相关材料', '等待DM反馈\n\n【DM反馈】\n你无法邀请码头的人跟你一起搬，这需要额外一个行动点。你自己拆了船，拆了25%的船，目前进度100%。\n获得金属10000kg，木头20000kg，发动机1，螺旋桨3', 'feedbacked', '1', '2', '2026-05-23 20:09:41', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('97', '25', 'tony', '平民', '1', 'investigate_player', '33', '荷叶男巫', null, null, '我来杀你了嘿嘿嘿嘿', '【调查结果】荷叶男巫的自由行动：\n（当日未提交个人行动）\n\n未能探知荷叶男巫的阵营行动（概率未中）。\n', 'feedbacked', '1', '2', '2026-05-23 20:10:23', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('98', '25', 'tony', '平民', '2', 'use_skill', null, null, null, null, '注意：先收取来自统治者的物资。然后进行烹饪。使用14食物，30木材获得两个面包。', '等待DM反馈', 'feedbacked', '0', '2', '2026-05-23 20:10:23', '2026-05-24 13:38:26');
INSERT INTO `player_action` VALUES ('99', '13', '凭栏择雨', '反叛者', '1', 'use_skill', null, null, null, null, '教师启蒙技能捕鱼进行生产', '等待DM反馈\n\n【DM反馈】\n你将获得10单位食物', 'feedbacked', '1', '2', '2026-05-23 20:11:35', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('100', '13', '凭栏择雨', '反叛者', '2', 'hide', null, null, null, null, '', '【隐藏】您将进入隐藏状态，明天将无法被调查、私聊或成为统治者与密谋的行动目标', 'feedbacked', '1', '2', '2026-05-23 20:11:36', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('101', '9', '对酒', '冒险者', '1', 'other', null, null, null, null, '自己带着斧头邀请维修工（玩家）和船长（玩家带枪）一起前往伐木营地，先和伐木工（npc）进行一个恳求，说明现在暴风雪来临，我们很需要他的帮助，需要木板蒸汽箱，拖拉机，电锯，木板（或者原木），发电机组（相当于搜刮地点），并且和他说镇长也同意我们求取这些，给了我们这些就不会强迫他当劳工了。如果他愿意就邀请他上船（船长可以适当展示一下他的猎枪）。伐木工（npc）始终不愿意，让船长拿枪威胁他，最起码把载具和发电机交出来，如果他还是不同意，就把他束缚起来，拿走载具和发电机组。如果他实在反抗激烈，就枪杀他（下下下之策），最终一定要拿到载具和发电机组。', '等待DM反馈\n\n【DM反馈】\n那么伐木工在这你一顿的威胁恐吓和威逼利诱之下他同意把他身上的木头都交给你。\n你获得了60000kg木头和发电机一台。\n他请求你饶他一命，并且带他上船。如果可以的话，他想用他的钥匙来交换上船名额。他想跟你签订契约，否则他会毁掉钥匙。', 'feedbacked', '1', '2', '2026-05-23 20:17:14', '2026-05-23 22:29:27');
INSERT INTO `player_action` VALUES ('102', '9', '对酒', '冒险者', '2', 'other', null, null, null, null, '去码头执行拆船任务（消息来源于邮差）', '等待DM反馈\n\n【DM反馈】\n你自己拆了船，拆了25%的船，目前进度50%。\n获得金属10000kg，木头10000kg', 'feedbacked', '1', '2', '2026-05-23 20:17:14', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('103', '23', '教皇', '反叛者', '1', 'use_skill', null, null, null, null, '对农民，医生，神父使用布道', '等待DM反馈\n\n【DM反馈】\n好的，收到', 'feedbacked', '1', '2', '2026-05-23 20:26:31', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('104', '23', '教皇', '反叛者', '2', 'go_location', '19', '监狱', null, '乔克·汤姆', '不动声色地利用自己神父的身份套取一些信息', '【地点信息】监狱\n区域：特殊\n描述：小镇边缘的一座灰石建筑，铁门锈迹斑斑，窗户窄得像枪眼。门前挂着一盏永远不灭的煤油灯，灯下总坐着一个看守。里面是两排铁牢房，地上铺着发霉的稻草，墙角堆着脏得看不出颜色的毯子。墙上用木炭刻满了前囚犯的名字和诅咒，有些已经被重复刻了三四遍。空气里弥漫着尿骚味和铁锈味，偶尔有人敲一下铁栏杆，声音能传到半个镇子。\n防御值：8\n管理方：统治者共同管理，监狱长常驻\n\n【设施】\n• 监牢×8：地面上有着不明污渍的铁制监牢，看起来很牢固几乎不可能在空手情况下跑出去。里面关着几个人，听说是因为违反统治者被关起来了。\n• 看守室：内有桌子一张、煤油灯一盏、警棍一根\n• 审讯椅：铁制，带锁扣的审讯椅\n\n【NPC】\n• 乔克·汤姆（民兵）\n\n【NPC互动】乔克·汤姆（民兵）\n态度：厌恶\n介绍：初始就跟着统治者干的监狱看守，一名很忠诚的下属。只是他有点小小的缺点，但统治者们也只能视而不见。', 'feedbacked', '1', '2', '2026-05-23 20:26:31', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('105', '26', 'V', '统治者', '1', 'transport', null, null, null, null, '[mode:warehouse_to_player]\n[source:armory]\n[item:material|2|18|1]\n[item:material|5|260|1]\n[item:material|8|22|1]', '【搬运结算】\n模式：仓库→个人\n源仓库：镇武库\n木材：搬运18单位到个人背包（18千克）\n食物：搬运260单位到个人背包（260千克）\n燃料：搬运22单位到个人背包（22千克）\n总计搬运：300千克\n（库存已变更）', 'feedbacked', '1', '2', '2026-05-23 20:41:47', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('106', '26', 'V', '统治者', '2', 'investigate_player', '28', '咲黑', null, null, '', '【调查结果】咲黑的自由行动：\n行动1：调查玩家 → 空白\n行动2：调查玩家 → 飞凡\n\n咲黑的阵营行动：\n（当日未提交阵营行动）\n', 'feedbacked', '1', '2', '2026-05-23 20:41:48', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('107', '15', '空白', '统治者', '1', 'use_skill', null, null, null, null, '使用维修工技能 修tony面包师的炉子', '等待DM反馈\n\n【DM反馈】\n把炉子修好，现在它又是一个坚强的炉子了', 'feedbacked', '1', '2', '2026-05-23 20:42:34', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('108', '15', '空白', '统治者', '2', 'transport', null, null, null, null, '[mode:warehouse_to_player]\n[source:armory]\n[item:item|5|1|2]\n[item:item|6|1|3]\n[item:weapon|2|1|3]\n[item:ammo|2|2|0.1]\n[item:ammo|4|1|0.1]\n[item:material|2|141|1]\n[item:material|5|100|1]\n[item:material|12|1|50]', '【搬运结算】\n模式：仓库→个人\n源仓库：镇武库\n防弹衣：搬运1单位到个人背包（2千克）\n复合盾：搬运1单位到个人背包（3千克）\n猎枪：搬运1单位到个人背包（3千克）\n猎枪弹：搬运2单位到个人背包（0千克）\n箭矢：搬运1单位到个人背包（0千克）\n木材：搬运141单位到个人背包（141千克）\n食物：搬运100单位到个人背包（100千克）\n发电机：搬运1单位到个人背包（50千克）\n总计搬运：299千克\n（库存已变更）', 'feedbacked', '1', '2', '2026-05-23 20:42:34', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('109', '21', '乐语', '冒险者', '1', 'other', null, null, null, null, '小心翼翼的去码头仔仔细细的拆那艘名叫阿弗雷号的船，然后都放入个人背包中', '等待DM反馈\n\n【DM反馈】\n你无法邀请码头的人跟你一起搬，这需要额外一个行动点。你自己拆了船，拆了25%的船，目前进度75%。\n获得金属10000kg，木头20000kg', 'feedbacked', '1', '2', '2026-05-23 21:04:56', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('110', '21', '乐语', '冒险者', '2', 'use_trait', null, null, null, null, '使用渴望出海特性，并按照一次建设最大使用量从自身背包和可调用仓库中使用资源建造方舟', '等待DM反馈\n\n【DM反馈】\n收到', 'feedbacked', '1', '2', '2026-05-23 21:04:56', '2026-05-23 22:05:19');
INSERT INTO `player_action` VALUES ('111', '31', '闲屿', '平民', '1', 'go_location', '15', '伐木营地', null, '托马斯·伍德', '询问托马斯对天灾的看法以及所作的准备', '【地点信息】伐木营地\n区域：海岛\n描述：岛内森林边缘的一片空地，堆满了砍伐的原木，有一座简易的木屋和一台生锈的蒸汽拖拉机。地上满是木屑和树桩。\n防御值：4\n\n【设施】\n• 木板蒸汽箱：用于木材处理的蒸汽箱设备\n• 拖拉机：在伐木行动时辅助工作的蒸汽拖拉机\n• 发电机：伐木营地配备的发电机组\n\n【NPC】\n• 托马斯·伍德（伐木工）\n\n【NPC互动】托马斯·伍德（伐木工）\n态度：忽视\n介绍：沉默寡言的伐木工，靠砍树和做木工为生，只求安稳度日。\n\n【DM反馈】\n【地点信息】伐木营地\n区域：海岛\n描述：岛内森林边缘的一片空地，堆满了砍伐的原木，有一座简易的木屋和一台生锈的蒸汽拖拉机。地上满是木屑和树桩。\n防御值：4\n\n介绍：沉默寡言的伐木工，靠砍树和做木工为生，只求安稳度日。\n托马斯正在砍树，他在为冒险者工作。', 'feedbacked', '1', '3', '2026-05-24 17:25:35', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('112', '31', '闲屿', '平民', '2', 'go_location', '3', '邮局', null, null, '尝试看看这里有没有值得知道的信息', '【地点信息】邮局\n区域：小镇\n描述：一间低矮的木屋，窗前挂着\"皇家邮政\"的铜牌。屋内满是油墨和纸张的气味，木制柜台后是分拣信件的格子和一台莫尔斯电报机。墙上贴着轮船班次表和邮票样张。\n防御值：3\n\n【设施】\n• 电报机：可以向外界发送信息的莫尔斯电报机\n\n【DM反馈】\n【地点信息】邮局\n区域：小镇\n描述：一间低矮的木屋，窗前挂着\"皇家邮政\"的铜牌。屋内满是油墨和纸张的气味，木制柜台后是分拣信件的格子和一台莫尔斯电报机。墙上贴着轮船班次表和邮票样张。\n防御值：3\n\n【设施】\n• 电报机：可以向外界发送信息的莫尔斯电报机\n\n你看到了一封回信，你尝试破解得到了以下信息\n【……外面天气良好……抱歉无法……请……补给无法确认位置……】', 'feedbacked', '1', '3', '2026-05-24 17:25:36', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('113', '20', '追枫', '天灾使者', '1', 'go_location', '16', '墓地', null, null, '伏击来到这里的所有人，在行动结束后抛开坟墓拿回前任猎手传承的枪和子弹', '【地点信息】墓地\n区域：海岛\n描述：墓地很静，石碑像断掉的牙齿从荒草里斜伸出来。风扫过时，只有自己的脚步声在回应。\n防御值：5\n\n【设施】\n• 坟堆：为了死者一个体面的后事\n\n【DM反馈】\n【地点信息】墓地\n区域：海岛\n描述：墓地很静，石碑像断掉的牙齿从荒草里斜伸出来。风扫过时，只有自己的脚步声在回应。\n防御值：5\n\n【设施】\n• 坟堆：为了死者一个体面的后事\n\n好的，但非常遗憾你带了全套的装备但只遇到了一个人——澡堂子\n他受伤了', 'feedbacked', '1', '3', '2026-05-24 18:03:09', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('114', '20', '追枫', '天灾使者', '2', 'other', null, null, null, null, '伏击在警察局到矿场的这条小路上，伏击路过的除了典狱长外的所有人', '等待DM反馈\n\n【DM反馈】\nok好的，但非常遗憾你带了全套的装备遇到——船长\n他受伤了', 'feedbacked', '1', '3', '2026-05-24 18:03:09', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('115', '9', '对酒', '冒险者', '1', 'other', null, null, null, null, '用拖拉机运木头到阵营仓库', '等待DM反馈\n\n【DM反馈】\n好的', 'feedbacked', '1', '3', '2026-05-24 18:18:36', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('116', '9', '对酒', '冒险者', '2', 'other', null, null, null, null, '用拖拉机运木头到阵营仓库', '等待DM反馈\n\n【DM反馈】\n好的', 'feedbacked', '1', '3', '2026-05-24 18:18:36', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('119', '10', '二阶堂希罗', '统治者', '1', 'investigate_player', '32', '澡堂子', null, null, '', '【调查结果】澡堂子的自由行动：\n（当日未提交个人行动）\n\n澡堂子的阵营行动：\n（当日未提交阵营行动）\n', 'feedbacked', '1', '3', '2026-05-24 18:59:29', '2026-05-24 21:46:06');
INSERT INTO `player_action` VALUES ('120', '10', '二阶堂希罗', '统治者', '2', 'go_location', '16', '墓地', null, null, '去猎人墓穴寻找物品', '【地点信息】墓地\n区域：海岛\n描述：墓地很静，石碑像断掉的牙齿从荒草里斜伸出来。风扫过时，只有自己的脚步声在回应。\n防御值：5\n\n【设施】\n• 坟堆：为了死者一个体面的后事\n\n【DM反馈】\n【地点信息】墓地\n区域：海岛\n描述：墓地很静，石碑像断掉的牙齿从荒草里斜伸出来。风扫过时，只有自己的脚步声在回应。\n防御值：5\n\n【设施】\n• 坟堆：为了死者一个体面的后事\n\n好的', 'feedbacked', '0', '3', '2026-05-24 18:59:29', '2026-05-24 21:48:57');
INSERT INTO `player_action` VALUES ('121', '17', 'zzz', '统治者', '1', 'investigate_player', '11', '蟋蟀蜥蜴', null, null, '', '等待DM反馈调查结果\n\n【DM反馈】\n他是劳工', 'feedbacked', '1', '3', '2026-05-24 19:03:12', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('122', '17', 'zzz', '统治者', '2', 'investigate_player', '33', '荷叶男巫', null, null, '一路走好', '等待DM反馈调查结果\n\n【DM反馈】\nRIP', 'feedbacked', '1', '3', '2026-05-24 19:03:12', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('123', '18', 'Missbear', '平民', '1', 'hide', null, null, null, null, '', '【隐藏】您将进入隐藏状态，明天将无法被调查、私聊或成为统治者与密谋的行动目标\n\n【DM反馈】\n【隐藏】您将进入隐藏状态，明天将无法被调查、私聊或成为统治者与密谋的行动目标', 'feedbacked', '1', '3', '2026-05-24 19:04:09', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('124', '18', 'Missbear', '平民', '2', 'investigate_player', '15', '空白', null, null, '', '等待DM反馈调查结果\n\n【DM反馈】\n【空白 · 第3天 行动反馈总结】\n\n行动一（其他）\n你未能成功，至少你不能当着所有劳工枪毙一个劳工\n\n行动二（其他）\n好好好，你在大冬天找蛐蛐。没找到但你找到了蟑螂~', 'feedbacked', '1', '3', '2026-05-24 19:04:09', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('125', '21', '乐语', '冒险者', '1', 'other', null, null, null, null, '使用10t木头，10t金属，20kg沥青，一个发动机，还有届时从矿场仓库取回的身上所有的帆布进行方舟建设。', '等待DM反馈\n\n【DM反馈】\n好的', 'feedbacked', '1', '3', '2026-05-24 19:19:54', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('126', '21', '乐语', '冒险者', '2', 'investigate_player', '13', '凭栏择雨', null, null, '使用潜行悄悄调查该玩家今天的所有行动，收集所有可能的情报', '等待DM反馈调查结果\n\n【DM反馈】\n【凭栏择雨 · 第3天 行动反馈总结】\n\n行动一：未提交\n\n行动二：未提交\n\n她是劳工\n没能探查到阵营行动', 'feedbacked', '1', '3', '2026-05-24 19:19:54', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('127', '19', 'unPy-GPT', '冒险者', '1', 'other', null, null, null, null, '通过工作量建设的方式推进方舟建设中木头进度5t', '等待DM反馈\n\n【DM反馈】\nok', 'feedbacked', '1', '3', '2026-05-24 19:26:55', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('128', '19', 'unPy-GPT', '冒险者', '2', 'other', null, null, null, null, '通过工作量建设的方式推进方舟建设中木头进度5t', '等待DM反馈\n\n【DM反馈】\nok', 'feedbacked', '1', '3', '2026-05-24 19:26:55', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('129', '26', 'V', '统治者', '1', 'other', null, null, null, null, '问候球球，没了，为什么不让我提交', '等待DM反馈\n\n【DM反馈】\nok，你将见到球球', 'feedbacked', '1', '3', '2026-05-24 19:33:02', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('130', '26', 'V', '统治者', '2', 'go_location', '7', '码头', null, '鲍勃·塔克', '尝试交涉，拉拢npc', '【地点信息】码头\n区域：小镇\n描述：有着几间木质简陋房屋和用旧木桩搭建的简陋码头，停着几艘渔船和一艘锈迹斑斑的货船。海浪拍打着木桩，发出单调的响声。远处海平面阴沉沉的。\n防御值：3\n\n【设施】\n• 渔船×3：三艘渔船，渔猎技能需要\n• 码头集购仓：需征求统治者同意，玩家可以询问统治者并购买物品\n• 阿弗雷号：轻型杂货船，总吨位约650吨，载重吨位约300吨，全长约42米，型宽约7米，吃水满载约4.2米，燃料30吨。配备螺旋桨2、发动机2、发电机1\n\n【NPC】\n• 克拉拉·南丁格尔（渔民）\n• 杰克·塔克（水手）\n• 鲍勃·塔克（装卸工）\n\n【NPC互动】鲍勃·塔克（装卸工）\n态度：厌恶\n介绍：一名一直在港口讨生活的搬运工。\n\n【DM反馈】\n【地点信息】码头\n区域：小镇\n描述：有着几间木质简陋房屋和用旧木桩搭建的简陋码头，停着几艘渔船和一艘锈迹斑斑的货船。海浪拍打着木桩，发出单调的响声。远处海平面阴沉沉的。\n防御值：3\n\n【设施】\n• 渔船×3：三艘渔船，渔猎技能需要\n• 码头集购仓：需征求统治者同意，玩家可以询问统治者并购买物品\n• 阿弗雷号：轻型杂货船，总吨位约650吨，载重吨位约300吨，全长约42米，型宽约7米，吃水满载约4.2米，燃料30吨。配备螺旋桨2、发动机2、发电机1\n\n【NPC互动】鲍勃·塔克（装卸工）\n态度：厌恶\n介绍：一名一直在港口讨生活的搬运工。\n他拒绝和你们交流', 'feedbacked', '1', '3', '2026-05-24 19:33:03', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('131', '24', '花海', '反叛者', '1', 'use_skill', null, null, null, null, '生产15个食物', '等待DM反馈\n\n【行动失败】\n\n【DM反馈】\n你的职业技能未能生效（条件不满足、被打断或遭否决）。', 'feedbacked', '1', '3', '2026-05-24 19:35:00', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('132', '24', '花海', '反叛者', '2', 'use_trait', null, null, null, null, '百宝袋/挑选列表的5个物品复制', '等待DM反馈\n\n【DM反馈】\n百宝袋是一次性技能', 'feedbacked', '1', '3', '2026-05-24 19:35:01', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('133', '15', '空白', '统治者', '1', 'other', null, null, null, null, '跟踪农民然后用背包的猎枪枪毙她 如果没射击技能则用维修工具肉搏', '等待DM反馈\n\n【DM反馈】\n你未能成功，至少你不能当着所有劳工枪毙一个劳工', 'feedbacked', '1', '3', '2026-05-24 19:43:34', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('134', '15', '空白', '统治者', '2', 'other', null, null, null, null, '去码头草坪抓蛐蛐然后偷摸塞镇长帽子里吓唬他 如果码头有很多蛐蛐就打包回去给监狱长和警长加餐', '等待DM反馈\n\n【DM反馈】\n好好好，你在大冬天找蛐蛐。没找到但你找到了蟑螂~', 'feedbacked', '1', '3', '2026-05-24 19:43:35', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('135', '16', '孤城暮角', '天灾使者', '1', 'use_skill', null, null, null, null, '盾盾盾造三个盾', '等待DM反馈\n\n【DM反馈】\nokokokok', 'feedbacked', '1', '3', '2026-05-24 20:34:01', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('136', '16', '孤城暮角', '天灾使者', '2', 'use_skill', null, null, null, null, '盾盾盾造三个盾', '等待DM反馈\n\n【DM反馈】\nokokokokok', 'feedbacked', '1', '3', '2026-05-24 20:34:01', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('137', '14', 'Κάκτος西里尔', '冒险者', '1', 'go_location', '17', '猎人小屋', null, null, '携带霰弹枪和2发子弹，与镇长前往熊仓库并杀熊', '【地点信息】猎人小屋\n区域：海岛\n描述：森林深处的一座原木小屋，墙外挂着各种兽皮，屋内弥漫着熏肉和火药的味道。壁炉上挂着一支双管猎枪。\n防御值：3\n\n【DM反馈】\n【地点信息】猎人小屋\n区域：海岛\n描述：森林深处的一座原木小屋，墙外挂着各种兽皮，屋内弥漫着熏肉和火药的味道。壁炉上挂着一支双管猎枪。\n防御值：3\n收到', 'feedbacked', '1', '3', '2026-05-24 20:43:15', '2026-05-24 20:46:25');
INSERT INTO `player_action` VALUES ('138', '14', 'Κάκτος西里尔', '冒险者', '2', 'go_location', '18', '矿场', null, null, '', '【地点信息】矿场\n区域：特殊\n描述：深入山腹的矿道与地面设施的综合体，包含管理室、矿场仓库和地下矿场。由统治者共同管理，是岛上最重要的资源产地和战略要地。\n防御值：10\n管理方：统治者共同管理\n\n【设施】\n• 切石机：矿场的石材切割设备\n• 管理室：矿场入口旁的木屋，里面有一张办公桌和一部电话（但线路已断）。墙上挂着矿井地图和工作安排表。\n• 矿场仓库：一个用厚木板搭建的棚屋，里面堆放着开采出来的矿石、工具和一些备用木材。门上挂着一把大锁。\n• 地下矿场（避难所）：深入山腹的矿道，墙壁上钉着木支架，每隔一段有一盏昏暗的油灯。深处被清理出一片空间，堆放着储备物资，这里就是计划中的\"避难所\"。\n\n【NPC】\n• 卡尔·铁锤（矿工）\n• 维克多·斯通（矿工）\n\n【DM反馈】\n【地点信息】矿场\n区域：特殊\n描述：深入山腹的矿道与地面设施的综合体，包含管理室、矿场仓库和地下矿场。由统治者共同管理，是岛上最重要的资源产地和战略要地。\n防御值：10\n管理方：统治者共同管理\n\n【设施】\n• 切石机：矿场的石材切割设备\n• 管理室：矿场入口旁的木屋，里面有一张办公桌和一部电话（但线路已断）。墙上挂着矿井地图和工作安排表。\n• 矿场仓库：一个用厚木板搭建的棚屋，里面堆放着开采出来的矿石、工具和一些备用木材。门上挂着一把大锁。\n• 地下矿场（避难所）：深入山腹的矿道，墙壁上钉着木支架，每隔一段有一盏昏暗的油灯。深处被清理出一片空间，堆放着储备物资，这里就是计划中的\"避难所\"。\n\n【NPC】\n• 卡尔·铁锤（矿工）\n• 维克多·斯通（矿工）\n\n收到\n你被不知道哪里的东西给射伤了。\n你把物品最后一刻传输给了你同伴', 'feedbacked', '1', '3', '2026-05-24 20:43:15', '2026-05-24 21:46:06');
INSERT INTO `player_action` VALUES ('139', '11', '蟋蟀蜥蜴', '反叛者', '1', 'use_trait', null, null, null, null, '作为治安官 非常受大家的尊敬 于是悄悄偷懒 过劳状态解除', '等待DM反馈\n\n【行动失败】\n\n【DM反馈】\n你的特性使用未能生效（条件不满足、被打断或遭否决）。', 'feedbacked', '1', '3', '2026-05-24 20:46:20', '2026-05-24 21:37:01');
INSERT INTO `player_action` VALUES ('140', '34', 'player', '平民', '1', 'go_location', '12', '面包店', null, null, '买面包', '【地点信息】面包店\n区域：小镇\n描述：集市附近的木质结构店铺，后面是一个烘焙的石头屋子。进去这里倒是挺温暖的，里面有面包的香气。\n防御值：2\n\n【设施】\n• 烘焙炉：面包店后方的石头烘焙炉', 'pending', '0', '1', '2026-06-22 12:31:13', '2026-06-22 12:31:13');
INSERT INTO `player_action` VALUES ('141', '34', 'player', '平民', '2', 'investigate_player', '20', '追枫', null, null, '看看他什么阵营', '等待DM反馈调查结果', 'pending', '0', '1', '2026-06-22 12:31:14', '2026-06-22 12:31:14');
INSERT INTO `player_action` VALUES ('142', '34', 'player', '平民', '1', 'go_location', '10', '集市', null, '塞缪尔·格雷', '', '【地点信息】集市\n区域：小镇\n描述：镇中心的露天广场，只有零星几个木制摊位。平时冷冷清清，但当渔船归来或有补给船消息时，这里会短暂地热闹起来。\n防御值：0\n\n【设施】\n• 行刑台：统治者或者其他阵营可以在这里进行公开行刑行为\n\n【NPC】\n• 塞缪尔·格雷（农户）\n• 弗雷德里克·波特（农户）\n• 米玛·雷铁斯托（手工艺人）\n• 汉斯·施密特（工匠）\n\n【新认识的NPC】\n• 塞缪尔·格雷（农户）\n• 弗雷德里克·波特（农户）\n• 米玛·雷铁斯托（手工艺人）\n• 汉斯·施密特（工匠）\n\n【NPC互动】塞缪尔·格雷（农户）\n态度：忽视\n介绍：善良而质朴的普通农户，乐于帮助他人。', 'pending', '0', '2', '2026-06-23 23:42:12', '2026-06-23 23:42:12');
INSERT INTO `player_action` VALUES ('143', '34', 'player', '平民', '2', 'go_location', '15', '伐木营地', null, '托马斯·伍德', '', '【地点信息】伐木营地\n区域：海岛\n描述：岛内森林边缘的一片空地，堆满了砍伐的原木，有一座简易的木屋和一台生锈的蒸汽拖拉机。地上满是木屑和树桩。\n防御值：4\n\n【设施】\n• 木板蒸汽箱：用于木材处理的蒸汽箱设备\n• 拖拉机：在伐木行动时辅助工作的蒸汽拖拉机\n• 发电机：伐木营地配备的发电机组\n\n【NPC】\n• 托马斯·伍德（伐木工）\n\n【新认识的NPC】\n• 托马斯·伍德（伐木工）\n\n【NPC互动】托马斯·伍德（伐木工）\n态度：忽视\n介绍：沉默寡言的伐木工，靠砍树和做木工为生，只求安稳度日。', 'pending', '0', '2', '2026-06-23 23:42:13', '2026-06-23 23:42:13');

-- ----------------------------
-- Table structure for player_daily_consumption
-- ----------------------------
DROP TABLE IF EXISTS `player_daily_consumption`;
CREATE TABLE `player_daily_consumption` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL,
  `game_day` int(11) NOT NULL,
  `required_food_units` int(11) NOT NULL DEFAULT '2',
  `required_fuel_kg` int(11) NOT NULL DEFAULT '15',
  `consumed_food_units` int(11) NOT NULL DEFAULT '0',
  `consumed_fuel_kg` int(11) NOT NULL DEFAULT '0',
  `fuel_from_wood_kg` int(11) NOT NULL DEFAULT '0',
  `fuel_from_fuel_kg` int(11) NOT NULL DEFAULT '0',
  `submitted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_day` (`player_id`,`game_day`),
  KEY `idx_game_day` (`game_day`),
  CONSTRAINT `player_daily_consumption_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COMMENT='玩家每日进食与取暖消耗记录';

-- ----------------------------
-- Records of player_daily_consumption
-- ----------------------------
INSERT INTO `player_daily_consumption` VALUES ('3', '9', '1', '2', '15', '2', '15', '0', '1', '1', '2026-05-22 18:22:29', '2026-05-22 18:22:29');
INSERT INTO `player_daily_consumption` VALUES ('4', '8', '1', '2', '15', '2', '15', '15', '0', '1', '2026-05-22 18:29:07', '2026-05-22 18:29:07');
INSERT INTO `player_daily_consumption` VALUES ('5', '15', '1', '2', '15', '2', '15', '15', '0', '1', '2026-05-22 18:44:51', '2026-05-22 18:44:51');
INSERT INTO `player_daily_consumption` VALUES ('6', '14', '1', '2', '15', '2', '15', '0', '1', '1', '2026-05-22 18:46:05', '2026-05-22 18:46:05');
INSERT INTO `player_daily_consumption` VALUES ('7', '27', '1', '2', '15', '2', '15', '0', '1', '1', '2026-05-22 18:46:35', '2026-05-22 18:46:35');
INSERT INTO `player_daily_consumption` VALUES ('8', '23', '1', '2', '15', '2', '15', '15', '0', '1', '2026-05-22 18:46:55', '2026-05-22 18:46:55');
INSERT INTO `player_daily_consumption` VALUES ('9', '19', '1', '2', '15', '2', '15', '15', '0', '1', '2026-05-22 19:04:03', '2026-05-22 19:26:40');
INSERT INTO `player_daily_consumption` VALUES ('10', '16', '1', '2', '15', '2', '15', '0', '1', '1', '2026-05-22 19:15:53', '2026-05-22 19:15:53');
INSERT INTO `player_daily_consumption` VALUES ('11', '20', '1', '2', '15', '2', '15', '0', '1', '1', '2026-05-22 19:40:14', '2026-05-22 19:40:14');
INSERT INTO `player_daily_consumption` VALUES ('12', '22', '1', '2', '15', '2', '15', '15', '0', '1', '2026-05-22 19:54:31', '2026-05-22 19:54:42');
INSERT INTO `player_daily_consumption` VALUES ('13', '13', '1', '2', '15', '2', '15', '15', '0', '1', '2026-05-22 19:59:13', '2026-05-22 19:59:13');
INSERT INTO `player_daily_consumption` VALUES ('14', '12', '1', '2', '15', '2', '15', '15', '0', '1', '2026-05-22 20:00:12', '2026-05-22 20:00:12');
INSERT INTO `player_daily_consumption` VALUES ('15', '24', '1', '2', '15', '2', '15', '15', '0', '1', '2026-05-22 20:04:21', '2026-05-22 20:04:21');
INSERT INTO `player_daily_consumption` VALUES ('16', '21', '1', '2', '15', '2', '15', '15', '0', '1', '2026-05-22 21:05:07', '2026-05-22 21:05:07');
INSERT INTO `player_daily_consumption` VALUES ('17', '31', '1', '2', '15', '2', '15', '15', '0', '1', '2026-05-22 21:09:20', '2026-05-22 21:09:20');
INSERT INTO `player_daily_consumption` VALUES ('18', '11', '1', '2', '15', '2', '15', '0', '1', '1', '2026-05-22 21:23:12', '2026-05-22 21:23:12');
INSERT INTO `player_daily_consumption` VALUES ('19', '32', '1', '2', '15', '2', '15', '15', '0', '1', '2026-05-22 21:32:25', '2026-05-22 21:32:25');
INSERT INTO `player_daily_consumption` VALUES ('20', '30', '1', '2', '15', '2', '15', '0', '1', '1', '2026-05-22 21:51:07', '2026-05-22 21:51:07');
INSERT INTO `player_daily_consumption` VALUES ('21', '10', '1', '2', '15', '2', '15', '0', '1', '1', '2026-05-22 22:22:28', '2026-05-22 22:22:28');
INSERT INTO `player_daily_consumption` VALUES ('22', '17', '1', '2', '15', '2', '15', '15', '0', '1', '2026-05-22 22:59:05', '2026-05-22 22:59:05');
INSERT INTO `player_daily_consumption` VALUES ('23', '29', '1', '2', '15', '2', '15', '15', '0', '1', '2026-05-22 23:27:29', '2026-05-22 23:27:29');
INSERT INTO `player_daily_consumption` VALUES ('24', '18', '1', '2', '15', '2', '15', '15', '0', '1', '2026-05-22 23:44:12', '2026-05-23 00:13:29');
INSERT INTO `player_daily_consumption` VALUES ('25', '25', '1', '2', '15', '2', '15', '0', '1', '1', '2026-05-23 00:05:45', '2026-05-23 00:05:45');
INSERT INTO `player_daily_consumption` VALUES ('26', '28', '1', '2', '15', '2', '15', '0', '1', '1', '2026-05-23 00:31:49', '2026-05-23 00:31:49');
INSERT INTO `player_daily_consumption` VALUES ('27', '26', '1', '2', '15', '2', '15', '0', '1', '1', '2026-05-23 00:32:15', '2026-05-23 00:32:15');
INSERT INTO `player_daily_consumption` VALUES ('28', '20', '2', '2', '15', '2', '15', '0', '1', '1', '2026-05-23 11:37:15', '2026-05-23 11:37:15');
INSERT INTO `player_daily_consumption` VALUES ('29', '13', '2', '2', '15', '2', '15', '15', '0', '1', '2026-05-23 11:49:47', '2026-05-23 11:49:47');
INSERT INTO `player_daily_consumption` VALUES ('30', '22', '2', '2', '15', '2', '15', '0', '1', '1', '2026-05-23 14:00:59', '2026-05-23 14:00:59');
INSERT INTO `player_daily_consumption` VALUES ('31', '16', '2', '2', '15', '2', '15', '0', '1', '1', '2026-05-23 14:04:27', '2026-05-23 14:04:27');
INSERT INTO `player_daily_consumption` VALUES ('32', '8', '2', '2', '15', '2', '15', '15', '0', '1', '2026-05-23 15:29:53', '2026-05-23 15:29:53');
INSERT INTO `player_daily_consumption` VALUES ('33', '9', '2', '2', '15', '2', '15', '0', '1', '1', '2026-05-23 19:03:04', '2026-05-23 19:03:04');
INSERT INTO `player_daily_consumption` VALUES ('34', '28', '2', '2', '15', '1', '15', '0', '1', '0', '2026-05-23 19:06:51', '2026-05-23 19:06:51');
INSERT INTO `player_daily_consumption` VALUES ('35', '19', '2', '2', '15', '2', '15', '15', '0', '1', '2026-05-23 19:07:22', '2026-05-23 19:07:22');
INSERT INTO `player_daily_consumption` VALUES ('36', '15', '2', '2', '15', '2', '15', '15', '0', '1', '2026-05-23 19:22:07', '2026-05-23 19:45:46');
INSERT INTO `player_daily_consumption` VALUES ('37', '10', '2', '2', '15', '2', '15', '0', '1', '1', '2026-05-23 19:24:09', '2026-05-23 19:24:09');
INSERT INTO `player_daily_consumption` VALUES ('38', '21', '2', '2', '15', '2', '15', '15', '0', '1', '2026-05-23 19:38:25', '2026-05-23 19:38:25');
INSERT INTO `player_daily_consumption` VALUES ('39', '24', '2', '2', '15', '2', '15', '15', '0', '1', '2026-05-23 19:46:41', '2026-05-23 19:46:41');
INSERT INTO `player_daily_consumption` VALUES ('40', '14', '2', '2', '15', '2', '15', '0', '1', '1', '2026-05-23 20:09:22', '2026-05-23 20:09:22');
INSERT INTO `player_daily_consumption` VALUES ('41', '11', '2', '2', '15', '2', '15', '0', '1', '1', '2026-05-23 20:10:17', '2026-05-23 20:10:17');
INSERT INTO `player_daily_consumption` VALUES ('42', '25', '2', '2', '15', '2', '15', '0', '1', '1', '2026-05-23 20:25:15', '2026-05-23 20:25:15');
INSERT INTO `player_daily_consumption` VALUES ('43', '23', '2', '2', '15', '2', '15', '15', '0', '1', '2026-05-23 20:26:49', '2026-05-23 20:26:49');
INSERT INTO `player_daily_consumption` VALUES ('44', '30', '2', '2', '15', '2', '15', '0', '1', '1', '2026-05-23 20:28:56', '2026-05-23 20:28:56');
INSERT INTO `player_daily_consumption` VALUES ('45', '29', '2', '2', '15', '2', '15', '15', '0', '1', '2026-05-23 20:31:54', '2026-05-23 20:31:54');
INSERT INTO `player_daily_consumption` VALUES ('46', '27', '2', '2', '15', '2', '15', '0', '1', '1', '2026-05-23 21:06:41', '2026-05-23 21:06:41');
INSERT INTO `player_daily_consumption` VALUES ('47', '26', '2', '2', '15', '2', '15', '0', '1', '1', '2026-05-23 21:50:06', '2026-05-23 21:50:06');
INSERT INTO `player_daily_consumption` VALUES ('48', '17', '2', '2', '15', '2', '15', '0', '1', '1', '2026-05-23 22:20:56', '2026-05-23 22:20:56');
INSERT INTO `player_daily_consumption` VALUES ('49', '32', '2', '2', '15', '2', '15', '15', '0', '1', '2026-05-24 00:54:26', '2026-05-24 00:54:26');
INSERT INTO `player_daily_consumption` VALUES ('50', '20', '3', '2', '15', '2', '15', '0', '1', '1', '2026-05-24 17:15:09', '2026-05-24 17:15:09');
INSERT INTO `player_daily_consumption` VALUES ('51', '31', '3', '2', '15', '2', '15', '15', '0', '1', '2026-05-24 17:17:19', '2026-05-24 17:17:19');
INSERT INTO `player_daily_consumption` VALUES ('52', '21', '3', '2', '15', '2', '15', '15', '0', '1', '2026-05-24 17:19:45', '2026-05-24 17:19:45');
INSERT INTO `player_daily_consumption` VALUES ('53', '16', '3', '2', '15', '2', '15', '0', '1', '1', '2026-05-24 17:43:18', '2026-05-24 17:43:18');
INSERT INTO `player_daily_consumption` VALUES ('54', '9', '3', '2', '15', '2', '15', '0', '1', '1', '2026-05-24 17:58:09', '2026-05-24 17:58:09');
INSERT INTO `player_daily_consumption` VALUES ('55', '10', '3', '2', '15', '2', '15', '0', '1', '1', '2026-05-24 18:03:14', '2026-05-24 18:03:14');
INSERT INTO `player_daily_consumption` VALUES ('56', '17', '3', '2', '15', '2', '15', '0', '1', '1', '2026-05-24 18:16:31', '2026-05-24 18:16:31');
INSERT INTO `player_daily_consumption` VALUES ('57', '19', '3', '2', '15', '2', '15', '0', '1', '1', '2026-05-24 18:54:56', '2026-05-24 18:54:56');
INSERT INTO `player_daily_consumption` VALUES ('58', '26', '3', '2', '15', '2', '15', '15', '0', '1', '2026-05-24 19:22:31', '2026-05-24 19:22:31');
INSERT INTO `player_daily_consumption` VALUES ('59', '14', '3', '2', '15', '2', '15', '0', '1', '1', '2026-05-24 19:24:15', '2026-05-24 19:24:15');
INSERT INTO `player_daily_consumption` VALUES ('60', '24', '3', '2', '15', '2', '15', '15', '0', '1', '2026-05-24 19:33:51', '2026-05-24 19:33:51');
INSERT INTO `player_daily_consumption` VALUES ('61', '15', '3', '2', '15', '2', '15', '15', '0', '1', '2026-05-24 19:37:24', '2026-05-24 19:37:24');
INSERT INTO `player_daily_consumption` VALUES ('62', '11', '3', '2', '15', '2', '15', '15', '0', '1', '2026-05-24 20:43:25', '2026-05-24 20:43:25');
INSERT INTO `player_daily_consumption` VALUES ('63', '23', '3', '2', '15', '2', '15', '15', '0', '1', '2026-05-24 22:30:40', '2026-05-24 22:30:40');
INSERT INTO `player_daily_consumption` VALUES ('64', '13', '3', '2', '15', '2', '15', '15', '0', '1', '2026-05-24 22:33:37', '2026-05-24 22:33:37');
INSERT INTO `player_daily_consumption` VALUES ('65', '34', '1', '2', '15', '2', '15', '15', '0', '1', '2026-06-22 12:29:23', '2026-06-22 12:30:20');

-- ----------------------------
-- Table structure for player_exploration
-- ----------------------------
DROP TABLE IF EXISTS `player_exploration`;
CREATE TABLE `player_exploration` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `event_id` int(11) DEFAULT NULL,
  `game_day` int(11) NOT NULL,
  `player_id` int(11) NOT NULL,
  `player_name` varchar(50) DEFAULT NULL,
  `result` text,
  `status` varchar(20) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `dice_result` int(11) DEFAULT NULL,
  `invest_points` int(11) DEFAULT NULL,
  `total_exploration_value` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of player_exploration
-- ----------------------------
INSERT INTO `player_exploration` VALUES ('1', '2026-06-24 18:07:59.148000', '17', '2', '34', 'player', '✓ 已提交【探索岛屿】\n\n【探索结果】\n发现：废弃的猎人瞭望台\n地点描述：一座建在巨树高处的废弃木台，通往它的绳梯已经半朽。台上有一个被遗弃的巢穴，似乎被匆忙放弃。\n可获得物资：绳索 (10米)， 火把 (1把)\n历史秘密碎片：你在木台的柱子上发现了一连串古老的刻痕，像是某种计数方式。旁边画着一个歪歪扭扭的太阳，正在被雪花吞没。这似乎记录着某个先民对漫漫长夜的恐惧。\n难度：1\n\n等待主持人发放奖励。', 'explored', '2026-06-24 18:09:57.012000', '1', '0', '1');
INSERT INTO `player_exploration` VALUES ('2', '2026-06-24 20:00:43.572000', '56', '100', '8', '兔兔', '✓ 已提交【探索岛屿】\n\n【探索结果】\n发现：冻土下的陶罐\n地点描述：在岛北侧一片冻土荒原上，地面裂开一道缝，露出一个埋藏已久的陶罐口沿。\n可获得物资：炸药（10kg）\n历史秘密碎片：你挖出陶罐，发现里面装着几块刻满符文的骨片。符文你不认识，但你注意到其中一片的背面，有人用现代钢笔写了一行注：\"翻译员说这是某种祈禳文，大意是\'愿火焰吞噬眼睛\'——但为什么我们的祖先要祈禳一个眼睛？\"\n难度：7\n\n等待主持人发放奖励。', 'explored', '2026-06-24 20:01:15.671000', '3', '4', '7');
INSERT INTO `player_exploration` VALUES ('3', '2026-06-24 20:03:48.522000', '39', '101', '8', '兔兔', '✓ 已提交【探索岛屿】\n\n【探索结果】\n发现：废弃的蜂箱\n地点描述：一片向阳的山坡上，排列着几个破败的木质蜂箱，早已没有蜜蜂的踪迹。周围长满了干枯的野花。\n可获得物资：草药（4单位） ， 蜡烛（5根）\n历史秘密碎片：你打开一个蜂箱的底层，发现一块被蜂蜡密封的油布。打开后，里面是一幅用炭笔画的速写：一个巨大的、被藤蔓和苔藓覆盖的圆形建筑，顶端裂开，一道光柱直射向天空。画的背面写着：\"他们以为是信号塔，其实是接收器。他们一直在等\'上面\'的回应。\"\n难度：5\n\n【探索奖励】\n+4个 未知物品\n+5个 未知物品\n', 'settled', '2026-06-24 20:03:58.180000', '1', '4', '5');
INSERT INTO `player_exploration` VALUES ('4', '2026-06-24 20:10:02.400000', null, '2', '35', 'cs', '✓ 已提交【探索岛屿】\n\n【投入物资】\n- 手电筒 x1\n投入探索值: 5/15\n\n等待主持人在夜晚阶段结算探索结果。', 'pending', '2026-06-24 20:10:02.400000', '1', '5', '6');
INSERT INTO `player_exploration` VALUES ('5', '2026-06-24 20:19:50.408000', '47', '200', '8', '兔兔', '✓ 已提交【探索岛屿】\n\n【探索结果】\n发现：失踪猎人营地\n地点描述：在一片茂密的冷杉林中，有一个被遗弃的营地，帐篷已经坍塌，篝火堆里还有未燃尽的骨头。\n可获得物资：猎枪弹（4发） ， 食物（6单位），猎枪（1把）\n历史秘密碎片：你在帐篷下的泥土里挖出一个密封的锡罐，里面是一张纸条：\"我看到了那些\'守夜者\'的秘密集会。他们在向一个银色的盒子祈祷。盒子上刻着那个眼睛。那不是神，是某种监控装置。我决定把它挖出来，埋到一个没人能找到的地方……\"纸条没有署名，但你注意到纸条背面用铅笔轻轻画了一个箭头，指向岛的东北方向。\n难度：16\n\n【探索奖励】\n+4个 未知物品\n+6份 食物\n+1个 未知物品\n', 'settled', '2026-06-24 20:19:50.438000', '6', '10', '16');
INSERT INTO `player_exploration` VALUES ('6', '2026-06-24 20:20:22.442000', '26', '201', '8', '兔兔', '✓ 已提交【探索岛屿】\n\n【探索结果】\n发现：守夜人的秘密补给点\n地点描述：在一块刻有\"静默\"二字的巨石下，你发现一个被油布盖住的小坑。里面似乎是某人存放的紧急物资，用的都是很老旧的款式。\n可获得物资：医疗包 (1个)， 手电筒 (1个)\n历史秘密碎片：油布的一角绣着一行小字：\"致下一位守夜者\"。你不确定这指的是某个职业，还是某个秘密组织的成员。\n难度：7\n\n【探索奖励】\n+1个 医疗包\n+1个 手电筒\n', 'settled', '2026-06-24 20:20:22.470000', '1', '6', '7');
INSERT INTO `player_exploration` VALUES ('7', '2026-06-24 20:20:36.328000', '41', '202', '8', '兔兔', '✓ 已提交【探索岛屿】\n\n【探索结果】\n发现：地热泉眼\n地点描述：岛内一处隐蔽的峡谷中，有一汪冒着热气的地热泉，周围没有积雪，反而长着一些深绿色的蕨类植物。空气中弥漫着一股硫磺味。\n可获得物资：食物（5单位） ， 医疗包（2单位）\n历史秘密碎片：你发现在泉眼边缘的岩石上，有人用锐器刻下了一句话：\"水温能救命，但救不了轮回。他们在地下深处烧着某种东西，那东西让整个岛都在呼吸。\"你伸手探了探水温，温热的触感让你短暂地忘记了寒冷，但那句\"呼吸\"却让你后背发凉。\n难度：8\n\n【探索奖励】\n+5份 食物\n+2个 医疗包\n', 'settled', '2026-06-24 20:20:36.348000', '1', '7', '8');
INSERT INTO `player_exploration` VALUES ('8', '2026-06-24 20:46:37.179000', '38', '1', '35', 'cs', '✓ 已提交【探索岛屿】\n\n【探索结果】\n发现：冰封的猎人小屋\n地点描述：森林边缘一座被积雪半掩的小木屋，门板已经朽坏，屋内一片狼藉。壁炉里还有未燃尽的柴火，但早已冻成了冰坨。\n可获得物资：木材（2吨）， 煤油（2升）， 火柴（3盒）\n历史秘密碎片：你在床板下发现一块刻满划痕的木板，上面记录着\"第7次\"和一个潦草的倒计时。旁边有一行小字：\"我们以为熬过暴雪就赢了，但暴雪之后，还有东西在等着我们。\"字迹到此为止，笔尖在木板上划出一道长长的、颤抖的痕迹。\n难度：16\n\n【探索奖励】\n+2000kg 木材\n+2kg 燃料\n+3个 未知物品\n', 'settled', '2026-06-24 20:46:37.231000', '1', '15', '16');

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
) ENGINE=InnoDB AUTO_INCREMENT=510 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of player_items
-- ----------------------------
INSERT INTO `player_items` VALUES ('71', '8', 'item', '2', '1', '2026-05-22 17:11:10', '2026-06-24 20:20:22');
INSERT INTO `player_items` VALUES ('72', '8', 'item', '13', '1', '2026-05-22 17:11:10', '2026-06-24 20:20:36');
INSERT INTO `player_items` VALUES ('73', '8', 'item', '15', '1', '2026-05-22 17:11:10', '2026-05-22 17:11:10');
INSERT INTO `player_items` VALUES ('74', '8', 'material', '1', '0', '2026-05-22 17:11:10', '2026-05-23 21:51:00');
INSERT INTO `player_items` VALUES ('75', '8', 'material', '2', '20', '2026-05-22 17:11:10', '2026-05-23 15:29:52');
INSERT INTO `player_items` VALUES ('76', '8', 'material', '3', '2', '2026-05-22 17:11:10', '2026-06-24 20:20:36');
INSERT INTO `player_items` VALUES ('78', '8', 'material', '8', '5', '2026-05-22 17:11:10', '2026-05-22 17:11:10');
INSERT INTO `player_items` VALUES ('79', '9', 'item', '16', '5', '2026-05-22 17:14:42', '2026-05-22 17:14:42');
INSERT INTO `player_items` VALUES ('81', '9', 'material', '2', '20050', '2026-05-22 17:14:42', '2026-05-24 20:19:19');
INSERT INTO `player_items` VALUES ('83', '9', 'material', '8', '25', '2026-05-22 17:14:42', '2026-05-24 21:47:34');
INSERT INTO `player_items` VALUES ('84', '9', 'material', '9', '210', '2026-05-22 17:14:42', '2026-05-24 21:15:49');
INSERT INTO `player_items` VALUES ('85', '10', 'item', '2', '0', '2026-05-22 17:16:41', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('86', '10', 'item', '3', '0', '2026-05-22 17:16:41', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('87', '10', 'item', '4', '1', '2026-05-22 17:16:41', '2026-05-22 17:16:41');
INSERT INTO `player_items` VALUES ('88', '10', 'item', '5', '1', '2026-05-22 17:16:41', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('89', '10', 'weapon', '3', '0', '2026-05-22 17:16:41', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('90', '12', 'item', '2', '1', '2026-05-22 17:24:37', '2026-05-22 17:24:37');
INSERT INTO `player_items` VALUES ('91', '12', 'item', '16', '5', '2026-05-22 17:24:37', '2026-05-22 17:24:37');
INSERT INTO `player_items` VALUES ('92', '12', 'material', '1', '10', '2026-05-22 17:24:37', '2026-05-22 17:24:37');
INSERT INTO `player_items` VALUES ('93', '12', 'material', '2', '35', '2026-05-22 17:24:37', '2026-05-22 20:00:12');
INSERT INTO `player_items` VALUES ('95', '12', 'material', '8', '5', '2026-05-22 17:24:37', '2026-05-22 17:24:37');
INSERT INTO `player_items` VALUES ('96', '13', 'material', '2', '754', '2026-05-22 17:26:00', '2026-05-24 22:33:36');
INSERT INTO `player_items` VALUES ('97', '13', 'material', '5', '10', '2026-05-22 17:26:00', '2026-05-24 22:33:36');
INSERT INTO `player_items` VALUES ('98', '13', 'material', '8', '100', '2026-05-22 17:26:00', '2026-05-23 01:30:51');
INSERT INTO `player_items` VALUES ('101', '14', 'weapon', '4', '0', '2026-05-22 17:31:01', '2026-05-23 23:23:44');
INSERT INTO `player_items` VALUES ('103', '14', 'material', '2', '0', '2026-05-22 17:31:01', '2026-05-23 21:12:20');
INSERT INTO `player_items` VALUES ('104', '14', 'material', '3', '0', '2026-05-22 17:31:01', '2026-05-24 19:09:45');
INSERT INTO `player_items` VALUES ('107', '15', 'item', '2', '1', '2026-05-22 17:32:02', '2026-05-22 17:32:02');
INSERT INTO `player_items` VALUES ('108', '15', 'item', '8', '2', '2026-05-22 17:32:02', '2026-05-22 17:32:02');
INSERT INTO `player_items` VALUES ('109', '15', 'material', '1', '10', '2026-05-22 17:32:02', '2026-05-22 17:32:02');
INSERT INTO `player_items` VALUES ('110', '15', 'material', '2', '146', '2026-05-22 17:32:02', '2026-05-24 19:37:24');
INSERT INTO `player_items` VALUES ('111', '15', 'material', '3', '0', '2026-05-22 17:32:02', '2026-05-24 21:13:52');
INSERT INTO `player_items` VALUES ('113', '15', 'material', '8', '0', '2026-05-22 17:32:02', '2026-05-23 00:24:34');
INSERT INTO `player_items` VALUES ('114', '16', 'item', '2', '0', '2026-05-22 17:33:38', '2026-05-24 20:35:15');
INSERT INTO `player_items` VALUES ('115', '16', 'item', '15', '0', '2026-05-22 17:33:38', '2026-05-24 20:35:15');
INSERT INTO `player_items` VALUES ('116', '16', 'material', '1', '8', '2026-05-22 17:33:38', '2026-05-24 20:52:39');
INSERT INTO `player_items` VALUES ('117', '16', 'material', '2', '361', '2026-05-22 17:33:38', '2026-05-24 20:52:51');
INSERT INTO `player_items` VALUES ('118', '16', 'material', '3', '10', '2026-05-22 17:33:38', '2026-05-24 20:35:15');
INSERT INTO `player_items` VALUES ('119', '16', 'material', '5', '0', '2026-05-22 17:33:38', '2026-05-24 20:35:15');
INSERT INTO `player_items` VALUES ('120', '16', 'material', '8', '0', '2026-05-22 17:33:38', '2026-05-24 20:35:15');
INSERT INTO `player_items` VALUES ('121', '16', 'material', '9', '0', '2026-05-22 17:33:38', '2026-05-22 22:46:04');
INSERT INTO `player_items` VALUES ('122', '17', 'item', '2', '1', '2026-05-22 17:34:38', '2026-05-22 17:34:38');
INSERT INTO `player_items` VALUES ('123', '17', 'item', '4', '1', '2026-05-22 17:34:38', '2026-05-22 17:34:38');
INSERT INTO `player_items` VALUES ('124', '17', 'item', '5', '1', '2026-05-22 17:34:38', '2026-05-22 17:34:38');
INSERT INTO `player_items` VALUES ('125', '17', 'weapon', '1', '1', '2026-05-22 17:34:38', '2026-05-22 17:34:38');
INSERT INTO `player_items` VALUES ('126', '17', 'weapon', '3', '1', '2026-05-22 17:34:38', '2026-05-22 17:34:38');
INSERT INTO `player_items` VALUES ('127', '17', 'ammo', '1', '6', '2026-05-22 17:34:38', '2026-05-22 17:34:38');
INSERT INTO `player_items` VALUES ('128', '18', 'item', '1', '1', '2026-05-22 17:35:54', '2026-05-22 17:35:54');
INSERT INTO `player_items` VALUES ('129', '18', 'item', '2', '2', '2026-05-22 17:35:54', '2026-05-22 17:35:54');
INSERT INTO `player_items` VALUES ('130', '18', 'item', '4', '1', '2026-05-22 17:35:54', '2026-05-22 17:35:54');
INSERT INTO `player_items` VALUES ('131', '18', 'item', '6', '1', '2026-05-22 17:35:54', '2026-05-22 17:35:54');
INSERT INTO `player_items` VALUES ('132', '18', 'item', '10', '5', '2026-05-22 17:35:54', '2026-05-22 17:35:54');
INSERT INTO `player_items` VALUES ('133', '18', 'item', '13', '10', '2026-05-22 17:35:54', '2026-05-22 17:35:54');
INSERT INTO `player_items` VALUES ('134', '18', 'weapon', '1', '1', '2026-05-22 17:35:54', '2026-05-22 17:35:54');
INSERT INTO `player_items` VALUES ('135', '18', 'ammo', '1', '2', '2026-05-22 17:35:54', '2026-05-22 17:35:54');
INSERT INTO `player_items` VALUES ('136', '18', 'material', '2', '30', '2026-05-22 17:35:54', '2026-05-23 00:13:29');
INSERT INTO `player_items` VALUES ('137', '18', 'material', '3', '10', '2026-05-22 17:35:54', '2026-05-22 17:35:54');
INSERT INTO `player_items` VALUES ('139', '18', 'material', '8', '5', '2026-05-22 17:35:54', '2026-05-22 17:35:54');
INSERT INTO `player_items` VALUES ('140', '19', 'item', '12', '1', '2026-05-22 17:36:56', '2026-05-22 17:36:56');
INSERT INTO `player_items` VALUES ('141', '19', 'weapon', '4', '0', '2026-05-22 17:36:56', '2026-05-23 19:18:01');
INSERT INTO `player_items` VALUES ('143', '19', 'material', '3', '0', '2026-05-22 17:36:56', '2026-05-24 19:21:42');
INSERT INTO `player_items` VALUES ('145', '19', 'material', '8', '9', '2026-05-22 17:36:56', '2026-05-24 18:54:55');
INSERT INTO `player_items` VALUES ('147', '20', 'weapon', '4', '2', '2026-05-22 17:38:35', '2026-05-24 15:31:17');
INSERT INTO `player_items` VALUES ('153', '20', 'material', '8', '13', '2026-05-22 17:38:36', '2026-05-24 21:22:44');
INSERT INTO `player_items` VALUES ('154', '21', 'item', '2', '1', '2026-05-22 17:40:25', '2026-05-22 17:40:25');
INSERT INTO `player_items` VALUES ('155', '21', 'item', '4', '1', '2026-05-22 17:40:25', '2026-05-22 17:40:25');
INSERT INTO `player_items` VALUES ('157', '21', 'material', '2', '11085', '2026-05-22 17:40:25', '2026-05-24 22:33:01');
INSERT INTO `player_items` VALUES ('158', '21', 'material', '3', '20', '2026-05-22 17:40:25', '2026-05-22 17:40:25');
INSERT INTO `player_items` VALUES ('160', '21', 'material', '8', '5', '2026-05-22 17:40:25', '2026-05-22 17:40:25');
INSERT INTO `player_items` VALUES ('161', '22', 'item', '12', '0', '2026-05-22 17:41:18', '2026-05-23 16:57:01');
INSERT INTO `player_items` VALUES ('162', '22', 'weapon', '4', '1', '2026-05-22 17:41:18', '2026-05-23 23:25:16');
INSERT INTO `player_items` VALUES ('163', '22', 'material', '2', '10000', '2026-05-22 17:41:18', '2026-05-23 21:33:22');
INSERT INTO `player_items` VALUES ('164', '22', 'material', '3', '0', '2026-05-22 17:41:18', '2026-05-23 16:57:01');
INSERT INTO `player_items` VALUES ('165', '22', 'material', '5', '0', '2026-05-22 17:41:18', '2026-05-23 16:57:01');
INSERT INTO `player_items` VALUES ('166', '22', 'material', '8', '0', '2026-05-22 17:41:18', '2026-05-23 16:57:01');
INSERT INTO `player_items` VALUES ('167', '23', 'item', '13', '50', '2026-05-22 17:42:39', '2026-05-22 17:42:39');
INSERT INTO `player_items` VALUES ('168', '23', 'item', '15', '5', '2026-05-22 17:42:39', '2026-05-22 17:42:39');
INSERT INTO `player_items` VALUES ('169', '23', 'material', '1', '10', '2026-05-22 17:42:39', '2026-05-22 17:42:39');
INSERT INTO `player_items` VALUES ('170', '23', 'material', '2', '5', '2026-05-22 17:42:39', '2026-05-24 22:30:39');
INSERT INTO `player_items` VALUES ('171', '23', 'material', '3', '10', '2026-05-22 17:42:39', '2026-05-22 17:42:39');
INSERT INTO `player_items` VALUES ('173', '23', 'material', '8', '3', '2026-05-22 17:42:39', '2026-05-22 21:41:14');
INSERT INTO `player_items` VALUES ('174', '24', 'item', '15', '1', '2026-05-22 17:46:08', '2026-05-22 17:46:08');
INSERT INTO `player_items` VALUES ('175', '24', 'weapon', '9', '1', '2026-05-22 17:46:08', '2026-05-22 17:46:08');
INSERT INTO `player_items` VALUES ('176', '24', 'material', '2', '110', '2026-05-22 17:46:08', '2026-05-24 19:33:50');
INSERT INTO `player_items` VALUES ('177', '24', 'material', '3', '10', '2026-05-22 17:46:08', '2026-05-22 17:46:08');
INSERT INTO `player_items` VALUES ('178', '24', 'material', '5', '-6', '2026-05-22 17:46:08', '2026-05-24 22:19:55');
INSERT INTO `player_items` VALUES ('179', '24', 'material', '8', '13', '2026-05-22 17:46:08', '2026-05-23 21:37:15');
INSERT INTO `player_items` VALUES ('180', '25', 'material', '1', '10', '2026-05-22 17:47:48', '2026-05-22 17:47:48');
INSERT INTO `player_items` VALUES ('181', '25', 'material', '2', '10', '2026-05-22 17:47:48', '2026-05-24 15:40:24');
INSERT INTO `player_items` VALUES ('182', '25', 'material', '5', '12', '2026-05-22 17:47:48', '2026-05-24 15:40:17');
INSERT INTO `player_items` VALUES ('183', '25', 'material', '8', '4', '2026-05-22 17:47:48', '2026-05-23 20:25:14');
INSERT INTO `player_items` VALUES ('184', '26', 'item', '5', '2', '2026-05-22 17:49:50', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('185', '26', 'weapon', '1', '3', '2026-05-22 17:49:50', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('186', '26', 'ammo', '1', '8', '2026-05-22 17:49:50', '2026-05-24 21:20:09');
INSERT INTO `player_items` VALUES ('187', '27', 'item', '10', '31', '2026-05-22 17:51:15', '2026-05-23 21:46:13');
INSERT INTO `player_items` VALUES ('188', '27', 'item', '16', '5', '2026-05-22 17:51:15', '2026-05-22 17:51:15');
INSERT INTO `player_items` VALUES ('189', '27', 'weapon', '2', '0', '2026-05-22 17:51:15', '2026-05-23 01:40:41');
INSERT INTO `player_items` VALUES ('190', '27', 'weapon', '8', '0', '2026-05-22 17:51:15', '2026-05-23 01:40:43');
INSERT INTO `player_items` VALUES ('191', '27', 'weapon', '9', '0', '2026-05-22 17:51:15', '2026-05-23 01:40:34');
INSERT INTO `player_items` VALUES ('192', '27', 'ammo', '2', '0', '2026-05-22 17:51:15', '2026-05-23 01:40:34');
INSERT INTO `player_items` VALUES ('193', '27', 'material', '1', '0', '2026-05-22 17:51:15', '2026-05-23 01:40:34');
INSERT INTO `player_items` VALUES ('194', '27', 'material', '2', '1', '2026-05-22 17:51:15', '2026-05-23 01:40:43');
INSERT INTO `player_items` VALUES ('195', '27', 'material', '3', '0', '2026-05-22 17:51:15', '2026-05-23 01:40:34');
INSERT INTO `player_items` VALUES ('196', '27', 'material', '5', '1', '2026-05-22 17:51:15', '2026-05-23 21:06:40');
INSERT INTO `player_items` VALUES ('197', '27', 'material', '6', '0', '2026-05-22 17:51:15', '2026-05-23 00:45:31');
INSERT INTO `player_items` VALUES ('198', '27', 'material', '8', '33', '2026-05-22 17:51:15', '2026-05-23 21:06:40');
INSERT INTO `player_items` VALUES ('199', '27', 'material', '9', '0', '2026-05-22 17:51:15', '2026-05-23 01:40:34');
INSERT INTO `player_items` VALUES ('200', '28', 'item', '2', '1', '2026-05-22 17:56:35', '2026-05-22 17:56:35');
INSERT INTO `player_items` VALUES ('201', '28', 'item', '7', '1', '2026-05-22 17:56:35', '2026-05-22 17:56:35');
INSERT INTO `player_items` VALUES ('202', '28', 'item', '17', '1', '2026-05-22 17:56:35', '2026-05-22 17:56:35');
INSERT INTO `player_items` VALUES ('203', '28', 'ammo', '3', '2', '2026-05-22 17:56:35', '2026-05-22 17:56:35');
INSERT INTO `player_items` VALUES ('204', '28', 'material', '1', '10', '2026-05-22 17:56:35', '2026-05-22 17:56:35');
INSERT INTO `player_items` VALUES ('205', '28', 'material', '2', '50', '2026-05-22 17:56:35', '2026-05-22 17:56:35');
INSERT INTO `player_items` VALUES ('207', '28', 'material', '8', '13', '2026-05-22 17:56:35', '2026-05-23 19:06:51');
INSERT INTO `player_items` VALUES ('208', '29', 'item', '14', '0', '2026-05-22 17:58:02', '2026-05-23 19:41:08');
INSERT INTO `player_items` VALUES ('209', '29', 'item', '15', '1', '2026-05-22 17:58:02', '2026-05-22 17:58:02');
INSERT INTO `player_items` VALUES ('210', '29', 'material', '1', '5', '2026-05-22 17:58:02', '2026-05-22 19:24:04');
INSERT INTO `player_items` VALUES ('212', '29', 'material', '5', '1', '2026-05-22 17:58:02', '2026-05-23 20:31:54');
INSERT INTO `player_items` VALUES ('213', '29', 'material', '8', '5', '2026-05-22 17:58:02', '2026-05-22 17:58:02');
INSERT INTO `player_items` VALUES ('214', '30', 'item', '1', '1', '2026-05-22 18:00:25', '2026-05-24 20:00:52');
INSERT INTO `player_items` VALUES ('215', '30', 'weapon', '11', '1', '2026-05-22 18:00:26', '2026-05-22 18:00:26');
INSERT INTO `player_items` VALUES ('216', '30', 'material', '1', '10', '2026-05-22 18:00:26', '2026-05-22 18:00:26');
INSERT INTO `player_items` VALUES ('217', '30', 'material', '2', '20', '2026-05-22 18:00:26', '2026-05-23 20:01:52');
INSERT INTO `player_items` VALUES ('218', '30', 'material', '3', '10', '2026-05-22 18:00:26', '2026-05-22 18:00:26');
INSERT INTO `player_items` VALUES ('219', '30', 'material', '5', '2', '2026-05-22 18:00:26', '2026-05-24 20:03:53');
INSERT INTO `player_items` VALUES ('220', '30', 'material', '8', '3', '2026-05-22 18:00:26', '2026-05-23 20:28:55');
INSERT INTO `player_items` VALUES ('221', '31', 'item', '2', '1', '2026-05-22 18:04:23', '2026-05-22 18:04:23');
INSERT INTO `player_items` VALUES ('222', '31', 'weapon', '4', '1', '2026-05-22 18:04:23', '2026-05-22 18:04:23');
INSERT INTO `player_items` VALUES ('223', '31', 'material', '1', '10', '2026-05-22 18:04:23', '2026-05-22 18:04:23');
INSERT INTO `player_items` VALUES ('224', '31', 'material', '2', '20', '2026-05-22 18:04:23', '2026-05-24 17:17:18');
INSERT INTO `player_items` VALUES ('225', '31', 'material', '3', '20', '2026-05-22 18:04:23', '2026-05-22 18:04:23');
INSERT INTO `player_items` VALUES ('227', '31', 'material', '8', '5', '2026-05-22 18:04:23', '2026-05-22 18:04:23');
INSERT INTO `player_items` VALUES ('228', '32', 'item', '2', '1', '2026-05-22 18:07:26', '2026-05-22 18:07:26');
INSERT INTO `player_items` VALUES ('229', '32', 'item', '13', '10', '2026-05-22 18:07:26', '2026-05-22 18:07:26');
INSERT INTO `player_items` VALUES ('230', '32', 'weapon', '8', '1', '2026-05-22 18:07:26', '2026-05-22 18:07:26');
INSERT INTO `player_items` VALUES ('231', '32', 'material', '1', '5', '2026-05-22 18:07:26', '2026-05-23 01:31:35');
INSERT INTO `player_items` VALUES ('232', '32', 'material', '2', '20', '2026-05-22 18:07:26', '2026-05-24 00:54:26');
INSERT INTO `player_items` VALUES ('233', '32', 'material', '3', '10', '2026-05-22 18:07:26', '2026-05-22 18:07:26');
INSERT INTO `player_items` VALUES ('234', '32', 'material', '5', '2', '2026-05-22 18:07:26', '2026-05-24 00:54:26');
INSERT INTO `player_items` VALUES ('235', '32', 'material', '8', '10', '2026-05-22 18:07:26', '2026-05-23 01:31:35');
INSERT INTO `player_items` VALUES ('236', '10', 'item', '22', '1', '2026-05-22 18:24:15', '2026-05-22 18:24:15');
INSERT INTO `player_items` VALUES ('237', '10', 'item', '21', '1', '2026-05-22 18:24:20', '2026-05-22 18:24:20');
INSERT INTO `player_items` VALUES ('238', '10', 'item', '20', '1', '2026-05-22 18:24:24', '2026-05-22 18:24:24');
INSERT INTO `player_items` VALUES ('239', '10', 'item', '19', '1', '2026-05-22 18:24:27', '2026-05-22 18:24:27');
INSERT INTO `player_items` VALUES ('240', '17', 'item', '19', '1', '2026-05-22 18:24:39', '2026-05-22 18:24:39');
INSERT INTO `player_items` VALUES ('241', '17', 'item', '20', '1', '2026-05-22 18:24:42', '2026-05-22 18:24:42');
INSERT INTO `player_items` VALUES ('242', '17', 'item', '21', '1', '2026-05-22 18:24:45', '2026-05-22 18:24:45');
INSERT INTO `player_items` VALUES ('243', '17', 'item', '22', '1', '2026-05-22 18:24:49', '2026-05-22 18:24:49');
INSERT INTO `player_items` VALUES ('244', '26', 'item', '19', '5', '2026-05-22 18:25:21', '2026-05-24 18:40:39');
INSERT INTO `player_items` VALUES ('245', '26', 'item', '20', '5', '2026-05-22 18:25:24', '2026-05-24 18:41:06');
INSERT INTO `player_items` VALUES ('246', '26', 'item', '21', '5', '2026-05-22 18:25:27', '2026-05-24 18:41:04');
INSERT INTO `player_items` VALUES ('247', '26', 'item', '22', '5', '2026-05-22 18:25:31', '2026-05-24 18:41:02');
INSERT INTO `player_items` VALUES ('248', '26', 'item', '7', '1', '2026-05-22 18:26:15', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('249', '26', 'ammo', '3', '7', '2026-05-22 18:26:21', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('250', '24', 'item', '23', '1', '2026-05-22 18:34:38', '2026-05-22 18:34:38');
INSERT INTO `player_items` VALUES ('251', '23', 'item', '23', '1', '2026-05-22 18:34:48', '2026-05-22 18:34:48');
INSERT INTO `player_items` VALUES ('252', '11', 'item', '23', '1', '2026-05-22 18:34:57', '2026-05-22 18:34:57');
INSERT INTO `player_items` VALUES ('253', '13', 'item', '23', '1', '2026-05-22 18:35:17', '2026-05-22 18:35:17');
INSERT INTO `player_items` VALUES ('254', '9', 'item', '24', '1', '2026-05-22 18:35:50', '2026-05-22 18:35:50');
INSERT INTO `player_items` VALUES ('256', '19', 'item', '24', '1', '2026-05-22 18:36:14', '2026-05-22 18:36:14');
INSERT INTO `player_items` VALUES ('257', '11', 'item', '3', '1', '2026-05-22 18:38:11', '2026-05-22 18:38:11');
INSERT INTO `player_items` VALUES ('258', '11', 'weapon', '3', '0', '2026-05-22 18:38:18', '2026-05-24 20:59:52');
INSERT INTO `player_items` VALUES ('259', '11', 'item', '5', '1', '2026-05-22 18:38:23', '2026-05-22 18:38:23');
INSERT INTO `player_items` VALUES ('260', '11', 'item', '2', '1', '2026-05-22 18:38:26', '2026-05-22 18:38:26');
INSERT INTO `player_items` VALUES ('261', '27', 'item', '2', '3', '2026-05-22 18:39:22', '2026-05-22 18:39:22');
INSERT INTO `player_items` VALUES ('262', '13', 'item', '2', '1', '2026-05-22 18:39:56', '2026-05-22 18:39:56');
INSERT INTO `player_items` VALUES ('263', '15', 'item', '19', '1', '2026-05-22 18:51:56', '2026-05-22 18:51:56');
INSERT INTO `player_items` VALUES ('264', '15', 'item', '20', '1', '2026-05-22 18:52:00', '2026-05-22 18:52:00');
INSERT INTO `player_items` VALUES ('265', '15', 'item', '21', '1', '2026-05-22 18:52:03', '2026-05-22 18:52:03');
INSERT INTO `player_items` VALUES ('266', '15', 'item', '22', '1', '2026-05-22 18:52:07', '2026-05-22 18:52:07');
INSERT INTO `player_items` VALUES ('267', '29', 'item', '10', '50', '2026-05-22 19:27:18', '2026-05-23 21:48:40');
INSERT INTO `player_items` VALUES ('268', '20', 'material', '5', '36', '2026-05-22 19:57:48', '2026-05-24 21:22:44');
INSERT INTO `player_items` VALUES ('269', '13', 'weapon', '2', '1', '2026-05-22 20:09:41', '2026-05-22 20:09:41');
INSERT INTO `player_items` VALUES ('270', '13', 'ammo', '2', '2', '2026-05-22 20:09:41', '2026-05-22 20:09:41');
INSERT INTO `player_items` VALUES ('271', '20', 'ammo', '4', '6', '2026-05-22 20:50:36', '2026-05-24 15:31:26');
INSERT INTO `player_items` VALUES ('275', '11', 'item', '10', '5', '2026-05-22 21:11:48', '2026-05-22 21:11:48');
INSERT INTO `player_items` VALUES ('276', '11', 'item', '1', '1', '2026-05-22 21:11:52', '2026-05-22 21:11:52');
INSERT INTO `player_items` VALUES ('277', '11', 'material', '3', '10', '2026-05-22 21:12:41', '2026-05-22 21:12:41');
INSERT INTO `player_items` VALUES ('278', '11', 'material', '2', '30', '2026-05-22 21:12:56', '2026-05-24 20:43:25');
INSERT INTO `player_items` VALUES ('279', '25', 'item', '18', '0', '2026-05-22 21:44:08', '2026-05-23 20:01:33');
INSERT INTO `player_items` VALUES ('280', '12', 'material', '5', '10', '2026-05-22 21:45:47', '2026-05-22 21:45:47');
INSERT INTO `player_items` VALUES ('282', '16', 'item', '6', '7', '2026-05-22 21:54:22', '2026-05-24 20:53:00');
INSERT INTO `player_items` VALUES ('283', '24', 'item', '1', '2', '2026-05-22 22:06:08', '2026-05-23 21:36:58');
INSERT INTO `player_items` VALUES ('284', '24', 'item', '5', '2', '2026-05-22 22:06:20', '2026-05-23 21:36:38');
INSERT INTO `player_items` VALUES ('286', '10', 'material', '8', '0', '2026-05-22 22:08:20', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('287', '14', 'material', '9', '0', '2026-05-22 22:48:18', '2026-05-24 19:09:45');
INSERT INTO `player_items` VALUES ('288', '17', 'material', '2', '25', '2026-05-22 22:55:00', '2026-05-22 22:59:04');
INSERT INTO `player_items` VALUES ('291', '13', 'material', '1', '13', '2026-05-23 00:23:42', '2026-05-23 19:40:10');
INSERT INTO `player_items` VALUES ('292', '10', 'ammo', '3', '1', '2026-05-23 00:24:17', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('295', '9', 'material', '5', '81', '2026-05-23 00:43:35', '2026-05-24 22:27:18');
INSERT INTO `player_items` VALUES ('296', '9', 'material', '6', '0', '2026-05-23 00:45:31', '2026-05-23 21:04:16');
INSERT INTO `player_items` VALUES ('297', '9', 'material', '3', '160', '2026-05-23 01:40:34', '2026-05-24 19:23:58');
INSERT INTO `player_items` VALUES ('298', '9', 'ammo', '2', '0', '2026-05-23 01:40:34', '2026-05-23 19:08:18');
INSERT INTO `player_items` VALUES ('299', '9', 'weapon', '9', '1', '2026-05-23 01:40:34', '2026-05-23 19:16:27');
INSERT INTO `player_items` VALUES ('300', '9', 'weapon', '2', '1', '2026-05-23 01:40:41', '2026-05-24 21:46:51');
INSERT INTO `player_items` VALUES ('301', '9', 'weapon', '8', '0', '2026-05-23 01:40:43', '2026-05-23 19:15:59');
INSERT INTO `player_items` VALUES ('303', '20', 'item', '6', '1', '2026-05-23 14:41:26', '2026-05-24 17:15:54');
INSERT INTO `player_items` VALUES ('304', '20', 'weapon', '7', '1', '2026-05-23 14:41:31', '2026-05-23 14:41:31');
INSERT INTO `player_items` VALUES ('305', '10', 'item', '18', '0', '2026-05-23 15:55:23', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('306', '16', 'item', '12', '0', '2026-05-23 16:57:33', '2026-05-24 20:35:15');
INSERT INTO `player_items` VALUES ('307', '16', 'weapon', '4', '2', '2026-05-23 16:57:33', '2026-05-24 14:44:10');
INSERT INTO `player_items` VALUES ('308', '9', 'item', '18', '1', '2026-05-23 19:05:50', '2026-05-24 18:19:47');
INSERT INTO `player_items` VALUES ('309', '19', 'weapon', '9', '1', '2026-05-23 19:17:06', '2026-05-23 19:17:06');
INSERT INTO `player_items` VALUES ('310', '22', 'weapon', '8', '1', '2026-05-23 19:17:13', '2026-05-23 19:17:13');
INSERT INTO `player_items` VALUES ('311', '9', 'weapon', '4', '0', '2026-05-23 19:18:09', '2026-05-23 23:24:58');
INSERT INTO `player_items` VALUES ('312', '26', 'material', '8', '220', '2026-05-23 19:27:02', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('314', '21', 'weapon', '8', '1', '2026-05-23 19:36:23', '2026-05-23 19:36:23');
INSERT INTO `player_items` VALUES ('318', '30', 'item', '14', '10', '2026-05-23 19:43:39', '2026-05-23 19:43:39');
INSERT INTO `player_items` VALUES ('319', '15', 'material', '5', '90', '2026-05-23 19:45:34', '2026-05-24 19:37:24');
INSERT INTO `player_items` VALUES ('320', '24', 'material', '1', '1', '2026-05-23 19:46:48', '2026-05-23 21:49:06');
INSERT INTO `player_items` VALUES ('321', '30', 'item', '18', '3', '2026-05-23 20:01:52', '2026-05-23 20:01:52');
INSERT INTO `player_items` VALUES ('323', '9', 'item', '20', '1', '2026-05-23 20:12:57', '2026-05-23 20:12:57');
INSERT INTO `player_items` VALUES ('324', '26', 'item', '18', '2', '2026-05-23 20:13:04', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('325', '10', 'item', '7', '1', '2026-05-23 20:13:04', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('326', '8', 'material', '5', '19', '2026-05-23 20:36:28', '2026-06-24 20:20:36');
INSERT INTO `player_items` VALUES ('328', '12', 'item', '20', '1', '2026-05-23 20:55:59', '2026-05-23 20:55:59');
INSERT INTO `player_items` VALUES ('330', '22', 'item', '20', '1', '2026-05-23 20:56:25', '2026-05-23 20:56:25');
INSERT INTO `player_items` VALUES ('331', '19', 'item', '20', '1', '2026-05-23 20:56:43', '2026-05-23 20:56:43');
INSERT INTO `player_items` VALUES ('332', '16', 'item', '20', '1', '2026-05-23 21:01:30', '2026-05-23 21:01:30');
INSERT INTO `player_items` VALUES ('333', '20', 'item', '20', '1', '2026-05-23 21:01:55', '2026-05-23 21:01:55');
INSERT INTO `player_items` VALUES ('335', '22', 'item', '24', '1', '2026-05-23 21:17:33', '2026-05-23 21:17:33');
INSERT INTO `player_items` VALUES ('336', '21', 'item', '24', '1', '2026-05-23 21:17:51', '2026-05-23 21:17:51');
INSERT INTO `player_items` VALUES ('337', '22', 'material', '1', '10000', '2026-05-23 21:33:55', '2026-05-23 21:34:02');
INSERT INTO `player_items` VALUES ('338', '24', 'item', '16', '1', '2026-05-23 21:37:26', '2026-05-23 21:37:26');
INSERT INTO `player_items` VALUES ('341', '19', 'material', '10', '0', '2026-05-23 21:49:24', '2026-05-23 23:42:36');
INSERT INTO `player_items` VALUES ('342', '19', 'material', '11', '3', '2026-05-23 21:49:31', '2026-05-24 19:10:00');
INSERT INTO `player_items` VALUES ('344', '10', 'item', '1', '0', '2026-05-23 22:05:18', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('345', '10', 'item', '6', '1', '2026-05-23 22:05:18', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('346', '10', 'item', '10', '0', '2026-05-23 22:05:18', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('347', '10', 'item', '11', '0', '2026-05-23 22:05:18', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('348', '10', 'item', '12', '0', '2026-05-23 22:05:18', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('349', '10', 'item', '13', '0', '2026-05-23 22:05:18', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('350', '10', 'item', '14', '0', '2026-05-23 22:05:18', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('351', '10', 'item', '15', '1', '2026-05-23 22:05:18', '2026-05-24 21:22:37');
INSERT INTO `player_items` VALUES ('352', '10', 'item', '17', '0', '2026-05-23 22:05:18', '2026-05-24 14:05:33');
INSERT INTO `player_items` VALUES ('353', '10', 'weapon', '1', '0', '2026-05-23 22:05:18', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('354', '10', 'weapon', '2', '0', '2026-05-23 22:05:18', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('355', '10', 'weapon', '4', '0', '2026-05-23 22:05:18', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('356', '10', 'weapon', '6', '1', '2026-05-23 22:05:18', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('357', '10', 'weapon', '7', '0', '2026-05-23 22:05:18', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('358', '10', 'ammo', '1', '0', '2026-05-23 22:05:18', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('359', '10', 'ammo', '2', '0', '2026-05-23 22:05:18', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('360', '10', 'ammo', '4', '0', '2026-05-23 22:05:18', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('361', '10', 'material', '6', '0', '2026-05-23 22:05:18', '2026-05-24 14:06:04');
INSERT INTO `player_items` VALUES ('362', '10', 'material', '11', '0', '2026-05-23 22:05:18', '2026-05-24 14:05:33');
INSERT INTO `player_items` VALUES ('363', '17', 'item', '8', '2', '2026-05-23 22:05:18', '2026-05-23 22:05:18');
INSERT INTO `player_items` VALUES ('364', '17', 'weapon', '8', '2', '2026-05-23 22:05:18', '2026-05-23 22:05:18');
INSERT INTO `player_items` VALUES ('365', '17', 'weapon', '9', '1', '2026-05-23 22:05:18', '2026-05-23 22:05:18');
INSERT INTO `player_items` VALUES ('366', '17', 'material', '5', '239', '2026-05-23 22:05:18', '2026-05-24 20:16:38');
INSERT INTO `player_items` VALUES ('367', '17', 'material', '8', '18', '2026-05-23 22:05:18', '2026-05-24 18:16:30');
INSERT INTO `player_items` VALUES ('368', '26', 'material', '2', '3', '2026-05-23 22:05:19', '2026-05-24 19:22:30');
INSERT INTO `player_items` VALUES ('369', '26', 'material', '5', '215', '2026-05-23 22:05:19', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('370', '15', 'item', '5', '1', '2026-05-23 22:05:19', '2026-05-23 22:05:19');
INSERT INTO `player_items` VALUES ('371', '15', 'item', '6', '1', '2026-05-23 22:05:19', '2026-05-23 22:05:19');
INSERT INTO `player_items` VALUES ('372', '15', 'weapon', '2', '0', '2026-05-23 22:05:19', '2026-05-24 21:09:41');
INSERT INTO `player_items` VALUES ('373', '15', 'ammo', '2', '0', '2026-05-23 22:05:19', '2026-05-24 21:09:41');
INSERT INTO `player_items` VALUES ('374', '15', 'ammo', '4', '1', '2026-05-23 22:05:19', '2026-05-23 22:05:19');
INSERT INTO `player_items` VALUES ('375', '15', 'material', '12', '1', '2026-05-23 22:05:19', '2026-05-23 22:05:19');
INSERT INTO `player_items` VALUES ('376', '14', 'material', '6', '0', '2026-05-23 22:17:14', '2026-05-24 18:28:16');
INSERT INTO `player_items` VALUES ('377', '21', 'weapon', '4', '1', '2026-05-23 23:24:27', '2026-05-23 23:24:27');
INSERT INTO `player_items` VALUES ('379', '10', 'weapon', '12', '25', '2026-05-24 00:13:25', '2026-05-24 00:13:25');
INSERT INTO `player_items` VALUES ('380', '21', 'material', '10', '1', '2026-05-24 01:03:21', '2026-05-24 01:03:21');
INSERT INTO `player_items` VALUES ('381', '21', 'material', '11', '1', '2026-05-24 01:03:21', '2026-05-24 01:03:21');
INSERT INTO `player_items` VALUES ('382', '33', 'item', '1', '1', '2026-05-24 11:38:18', '2026-05-24 11:38:18');
INSERT INTO `player_items` VALUES ('383', '9', 'item', '9', '1', '2026-05-24 13:46:01', '2026-05-24 13:46:01');
INSERT INTO `player_items` VALUES ('384', '13', 'item', '9', '1', '2026-05-24 13:46:16', '2026-05-24 13:46:16');
INSERT INTO `player_items` VALUES ('385', '13', 'item', '18', '3', '2026-05-24 14:09:37', '2026-05-24 14:09:37');
INSERT INTO `player_items` VALUES ('386', '30', 'weapon', '4', '1', '2026-05-24 14:48:57', '2026-05-24 14:48:57');
INSERT INTO `player_items` VALUES ('387', '10', 'material', '5', '0', '2026-05-24 14:55:55', '2026-05-24 20:59:17');
INSERT INTO `player_items` VALUES ('388', '14', 'material', '11', '0', '2026-05-24 14:56:00', '2026-05-24 19:09:45');
INSERT INTO `player_items` VALUES ('389', '26', 'ammo', '4', '8', '2026-05-24 16:18:27', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('390', '26', 'weapon', '7', '2', '2026-05-24 16:18:34', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('391', '26', 'item', '6', '2', '2026-05-24 16:18:44', '2026-05-24 22:39:55');
INSERT INTO `player_items` VALUES ('392', '21', 'item', '18', '1', '2026-05-24 17:08:53', '2026-05-24 17:08:53');
INSERT INTO `player_items` VALUES ('394', '31', 'item', '18', '1', '2026-05-24 17:13:08', '2026-05-24 17:13:08');
INSERT INTO `player_items` VALUES ('395', '16', 'item', '18', '0', '2026-05-24 18:08:32', '2026-05-24 19:15:55');
INSERT INTO `player_items` VALUES ('396', '19', 'item', '18', '1', '2026-05-24 18:23:07', '2026-05-24 18:23:07');
INSERT INTO `player_items` VALUES ('397', '21', 'material', '1', '100', '2026-05-24 18:24:30', '2026-05-24 21:10:44');
INSERT INTO `player_items` VALUES ('398', '19', 'material', '6', '30', '2026-05-24 18:30:38', '2026-05-24 19:07:04');
INSERT INTO `player_items` VALUES ('399', '19', 'material', '9', '0', '2026-05-24 19:10:00', '2026-05-24 19:21:42');
INSERT INTO `player_items` VALUES ('400', '21', 'material', '6', '20', '2026-05-24 19:12:06', '2026-05-24 19:12:06');
INSERT INTO `player_items` VALUES ('401', '21', 'material', '9', '0', '2026-05-24 19:28:08', '2026-05-24 21:09:11');
INSERT INTO `player_items` VALUES ('402', '30', 'ammo', '4', '1', '2026-05-24 20:03:39', '2026-05-24 20:03:39');
INSERT INTO `player_items` VALUES ('403', '30', 'weapon', '7', '1', '2026-05-24 20:03:48', '2026-05-24 20:03:48');
INSERT INTO `player_items` VALUES ('405', '9', 'ammo', '4', '1', '2026-05-24 20:07:26', '2026-05-24 22:15:34');
INSERT INTO `player_items` VALUES ('406', '9', 'weapon', '7', '0', '2026-05-24 20:07:42', '2026-05-24 22:15:34');
INSERT INTO `player_items` VALUES ('407', '17', 'item', '18', '1', '2026-05-24 20:16:38', '2026-05-24 20:16:38');
INSERT INTO `player_items` VALUES ('410', '26', 'item', '1', '2', '2026-05-24 21:00:14', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('411', '26', 'item', '2', '10', '2026-05-24 21:00:14', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('412', '26', 'item', '3', '4', '2026-05-24 21:00:14', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('413', '26', 'item', '10', '20', '2026-05-24 21:00:14', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('414', '26', 'item', '11', '3', '2026-05-24 21:00:14', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('415', '26', 'item', '12', '1', '2026-05-24 21:00:14', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('416', '26', 'item', '13', '20', '2026-05-24 21:00:14', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('417', '26', 'item', '14', '5', '2026-05-24 21:00:14', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('418', '26', 'item', '15', '1', '2026-05-24 21:00:14', '2026-05-24 21:22:30');
INSERT INTO `player_items` VALUES ('419', '26', 'weapon', '2', '1', '2026-05-24 21:00:14', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('420', '26', 'weapon', '3', '4', '2026-05-24 21:00:14', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('421', '26', 'weapon', '4', '2', '2026-05-24 21:00:14', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('422', '26', 'weapon', '6', '1', '2026-05-24 21:00:14', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('423', '26', 'ammo', '2', '2', '2026-05-24 21:00:14', '2026-05-24 21:00:14');
INSERT INTO `player_items` VALUES ('424', '24', 'weapon', '3', '1', '2026-05-24 21:00:20', '2026-05-24 21:00:20');
INSERT INTO `player_items` VALUES ('425', '10', 'material', '3', '10', '2026-05-24 21:15:26', '2026-05-24 21:15:26');
INSERT INTO `player_items` VALUES ('426', '20', 'item', '2', '1', '2026-05-24 21:22:44', '2026-05-24 21:22:44');
INSERT INTO `player_items` VALUES ('427', '20', 'item', '12', '1', '2026-05-24 21:22:44', '2026-05-24 21:22:44');
INSERT INTO `player_items` VALUES ('428', '20', 'item', '15', '1', '2026-05-24 21:22:44', '2026-05-24 21:22:44');
INSERT INTO `player_items` VALUES ('429', '20', 'material', '3', '10', '2026-05-24 21:22:44', '2026-05-24 21:22:44');
INSERT INTO `player_items` VALUES ('430', '20', 'material', '2', '12', '2026-05-24 21:22:44', '2026-05-24 21:22:44');
INSERT INTO `player_items` VALUES ('431', '21', 'material', '5', '2', '2026-05-24 21:38:44', '2026-05-24 21:38:44');
INSERT INTO `player_items` VALUES ('432', '9', 'item', '17', '2', '2026-05-24 21:47:59', '2026-05-24 21:47:59');
INSERT INTO `player_items` VALUES ('433', '17', 'weapon', '2', '1', '2026-05-24 21:49:19', '2026-05-24 21:49:19');
INSERT INTO `player_items` VALUES ('434', '17', 'ammo', '2', '2', '2026-05-24 21:49:19', '2026-05-24 21:49:19');
INSERT INTO `player_items` VALUES ('435', '20', 'ammo', '2', '1', '2026-05-24 21:49:33', '2026-05-24 21:49:33');
INSERT INTO `player_items` VALUES ('436', '20', 'weapon', '2', '1', '2026-05-24 21:49:38', '2026-05-24 21:49:38');
INSERT INTO `player_items` VALUES ('437', '9', 'ammo', '3', '1', '2026-05-24 21:50:53', '2026-05-24 21:50:53');
INSERT INTO `player_items` VALUES ('438', '9', 'item', '7', '1', '2026-05-24 21:50:59', '2026-05-24 21:50:59');
INSERT INTO `player_items` VALUES ('439', '9', 'material', '1', '10000', '2026-05-24 22:09:42', '2026-05-24 22:09:42');
INSERT INTO `player_items` VALUES ('440', '9', 'item', '1', '3', '2026-05-24 22:09:50', '2026-05-24 22:09:50');
INSERT INTO `player_items` VALUES ('441', '24', 'weapon', '7', '1', '2026-05-24 22:19:55', '2026-05-24 22:19:55');
INSERT INTO `player_items` VALUES ('442', '24', 'ammo', '4', '1', '2026-05-24 22:19:55', '2026-05-24 22:19:55');
INSERT INTO `player_items` VALUES ('443', '17', 'item', '6', '1', '2026-05-24 22:27:11', '2026-05-24 22:27:11');
INSERT INTO `player_items` VALUES ('444', '34', 'item', '10', '10', '2026-06-22 11:55:43', '2026-06-22 11:55:43');
INSERT INTO `player_items` VALUES ('445', '34', 'item', '16', '5', '2026-06-22 11:55:43', '2026-06-22 11:55:43');
INSERT INTO `player_items` VALUES ('446', '34', 'weapon', '2', '2', '2026-06-22 11:55:43', '2026-06-22 11:55:43');
INSERT INTO `player_items` VALUES ('447', '34', 'weapon', '8', '2', '2026-06-22 11:55:43', '2026-06-22 11:55:43');
INSERT INTO `player_items` VALUES ('448', '34', 'weapon', '9', '2', '2026-06-22 11:55:43', '2026-06-22 11:55:43');
INSERT INTO `player_items` VALUES ('449', '34', 'ammo', '2', '4', '2026-06-22 11:55:43', '2026-06-22 11:55:43');
INSERT INTO `player_items` VALUES ('450', '34', 'material', '1', '96', '2026-06-22 11:55:43', '2026-06-22 22:07:44');
INSERT INTO `player_items` VALUES ('451', '34', 'material', '2', '335', '2026-06-22 11:55:43', '2026-06-22 12:30:19');
INSERT INTO `player_items` VALUES ('452', '34', 'material', '3', '100', '2026-06-22 11:55:43', '2026-06-22 11:55:43');
INSERT INTO `player_items` VALUES ('453', '34', 'material', '5', '21', '2026-06-22 11:55:43', '2026-06-22 12:29:27');
INSERT INTO `player_items` VALUES ('454', '34', 'material', '6', '30', '2026-06-22 11:55:43', '2026-06-22 11:55:43');
INSERT INTO `player_items` VALUES ('455', '34', 'material', '8', '35', '2026-06-22 11:55:43', '2026-06-22 11:55:43');
INSERT INTO `player_items` VALUES ('456', '34', 'material', '9', '30', '2026-06-22 11:55:43', '2026-06-22 11:55:43');
INSERT INTO `player_items` VALUES ('457', '34', 'weapon', '11', '1', '2026-06-22 11:55:43', '2026-06-22 11:55:43');
INSERT INTO `player_items` VALUES ('458', '34', 'ammo', '3', '1', '2026-06-22 11:55:43', '2026-06-22 11:55:43');
INSERT INTO `player_items` VALUES ('459', '34', 'item', '20', '1', '2026-06-22 12:00:34', '2026-06-22 12:00:34');
INSERT INTO `player_items` VALUES ('494', '34', 'item', '1', '3', '2026-06-22 20:12:17', '2026-06-22 22:07:44');
INSERT INTO `player_items` VALUES ('495', '8', 'item', '16', '4', '2026-06-24 20:03:58', '2026-06-24 20:03:58');
INSERT INTO `player_items` VALUES ('496', '8', 'item', '9', '5', '2026-06-24 20:03:58', '2026-06-24 20:03:58');
INSERT INTO `player_items` VALUES ('497', '35', 'item', '2', '0', '2026-06-24 20:08:32', '2026-06-24 20:10:02');
INSERT INTO `player_items` VALUES ('498', '35', 'item', '15', '1', '2026-06-24 20:08:32', '2026-06-24 20:08:32');
INSERT INTO `player_items` VALUES ('499', '35', 'material', '1', '5', '2026-06-24 20:08:32', '2026-06-24 20:08:32');
INSERT INTO `player_items` VALUES ('500', '35', 'material', '2', '2150', '2026-06-24 20:08:32', '2026-06-24 20:46:37');
INSERT INTO `player_items` VALUES ('501', '35', 'material', '3', '25', '2026-06-24 20:08:32', '2026-06-24 20:46:37');
INSERT INTO `player_items` VALUES ('502', '35', 'material', '5', '8', '2026-06-24 20:08:32', '2026-06-24 20:08:32');
INSERT INTO `player_items` VALUES ('503', '35', 'material', '8', '7', '2026-06-24 20:08:32', '2026-06-24 20:46:37');
INSERT INTO `player_items` VALUES ('504', '35', 'material', '9', '10', '2026-06-24 20:08:32', '2026-06-24 20:08:32');
INSERT INTO `player_items` VALUES ('505', '35', 'item', '25', '1', '2026-06-24 20:08:32', '2026-06-24 20:08:32');
INSERT INTO `player_items` VALUES ('506', '8', 'ammo', '2', '4', '2026-06-24 20:19:50', '2026-06-24 20:19:50');
INSERT INTO `player_items` VALUES ('507', '8', 'weapon', '2', '1', '2026-06-24 20:19:50', '2026-06-24 20:19:50');
INSERT INTO `player_items` VALUES ('508', '8', 'item', '1', '3', '2026-06-24 20:20:22', '2026-06-24 20:20:36');
INSERT INTO `player_items` VALUES ('509', '35', 'item', '13', '3', '2026-06-24 20:46:37', '2026-06-24 20:46:37');

-- ----------------------------
-- Table structure for player_npc_recognition
-- ----------------------------
DROP TABLE IF EXISTS `player_npc_recognition`;
CREATE TABLE `player_npc_recognition` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `location_id` int(11) DEFAULT NULL,
  `npc_id` int(11) NOT NULL,
  `player_id` int(11) NOT NULL,
  `recognized_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of player_npc_recognition
-- ----------------------------
INSERT INTO `player_npc_recognition` VALUES ('1', '7', '1', '1', '2026-06-23 23:33:21.841000');
INSERT INTO `player_npc_recognition` VALUES ('2', '10', '7', '34', '2026-06-23 23:42:12.315000');
INSERT INTO `player_npc_recognition` VALUES ('3', '10', '8', '34', '2026-06-23 23:42:12.326000');
INSERT INTO `player_npc_recognition` VALUES ('4', '10', '9', '34', '2026-06-23 23:42:12.330000');
INSERT INTO `player_npc_recognition` VALUES ('5', '10', '10', '34', '2026-06-23 23:42:12.332000');
INSERT INTO `player_npc_recognition` VALUES ('6', '15', '4', '34', '2026-06-23 23:42:12.660000');

-- ----------------------------
-- Table structure for player_stealth
-- ----------------------------
DROP TABLE IF EXISTS `player_stealth`;
CREATE TABLE `player_stealth` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL COMMENT '玩家ID',
  `player_name` varchar(50) DEFAULT NULL COMMENT '玩家名称',
  `game_day` int(11) NOT NULL COMMENT '潜行生效的天数',
  `source_action_id` int(11) DEFAULT NULL COMMENT '来源行动ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_day` (`player_id`,`game_day`),
  KEY `idx_game_day` (`game_day`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COMMENT='玩家潜行状态表';

-- ----------------------------
-- Records of player_stealth
-- ----------------------------
INSERT INTO `player_stealth` VALUES ('1', '13', '凭栏择雨', '2', '43', '2026-05-22 21:41:45');
INSERT INTO `player_stealth` VALUES ('2', '18', 'Missbear', '2', '70', '2026-05-22 23:48:20');
INSERT INTO `player_stealth` VALUES ('3', '13', '凭栏择雨', '3', '100', '2026-05-23 20:45:23');
INSERT INTO `player_stealth` VALUES ('4', '18', 'Missbear', '4', '123', '2026-05-24 21:37:01');

-- ----------------------------
-- Table structure for quick_interaction
-- ----------------------------
DROP TABLE IF EXISTS `quick_interaction`;
CREATE TABLE `quick_interaction` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `dm_reply` text,
  `faction` varchar(20) NOT NULL,
  `game_day` int(11) NOT NULL,
  `interaction_type` varchar(30) NOT NULL,
  `player_id` int(11) NOT NULL,
  `player_name` varchar(50) DEFAULT NULL,
  `replied_at` datetime(6) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=75 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of quick_interaction
-- ----------------------------
INSERT INTO `quick_interaction` VALUES ('2', '个人行动 3: 使用一个面包/便当 获得额外的行动点。再次进行烹饪，耗费14食物30kg木材获得两个面包/便当。', '2026-05-22 19:15:23.301000', '完成', '平民', '1', 'supplementary_action', '25', 'tony', '2026-05-23 20:35:29.453000', 'replied', '2026-05-23 20:35:29.453000');
INSERT INTO `quick_interaction` VALUES ('3', '行动 4: （快速行动）三个【个人行动】都成功完成后，使用交易，给予V镇长一个面包，和二阶典狱长一个面包。今天一共消耗42食物，90木材，部分消耗的物资来自统治者的交易。共获得6个面包，其中把两个给出去。', '2026-05-22 19:17:53.749000', '完成', '平民', '1', 'quick_action', '25', 'tony', '2026-05-23 20:35:25.298000', 'replied', '2026-05-23 20:35:25.299000');
INSERT INTO `quick_interaction` VALUES ('4', '对西里尔散播瘟疫', '2026-05-22 20:16:33.794000', '收到', '天灾使者', '1', 'quick_action', '16', '孤城暮角', '2026-05-23 20:35:20.692000', 'replied', '2026-05-23 20:35:20.692000');
INSERT INTO `quick_interaction` VALUES ('5', '我是个酒蒙子，我要喝两瓶我的酒', '2026-05-22 20:44:54.151000', '已回复', '平民', '1', 'quick_action', '27', '得狗的老意', '2026-05-23 20:35:15.609000', 'replied', '2026-05-23 20:35:15.609000');
INSERT INTO `quick_interaction` VALUES ('6', '我想探查一下出生的邮局附近有什么物资和物品', '2026-05-22 21:07:22.600000', '一间低矮的木屋，窗前挂着\"皇家邮政\"的铜牌。屋内满是油墨和纸张的气味，木制柜台后是分拣信件的格子和一台莫尔斯电报机。墙上贴着轮船班次表和邮票样张。\n这是你家，防御值为3', '平民', '1', 'quick_action', '21', '乐语', '2026-05-22 22:37:55.456000', 'replied', '2026-05-22 22:37:55.456000');
INSERT INTO `quick_interaction` VALUES ('7', '我从燃料仓库搬运了物资到镇武库，我希望之后从镇武库拿走50kg的物资可以吗', '2026-05-22 21:47:51.566000', '不可以，快速搬运不可在搬运回合使用', '统治者', '1', 'supplementary_action', '26', 'V', '2026-05-22 22:38:32.108000', 'replied', '2026-05-22 22:38:32.108000');
INSERT INTO `quick_interaction` VALUES ('8', '转移40kg燃料仓库的木材和30kg港口的食物', '2026-05-22 21:51:40.878000', '不可以，快速搬运不可在搬运回合使用', '统治者', '1', 'quick_action', '17', 'zzz', '2026-05-22 22:40:40.332000', 'replied', '2026-05-22 22:40:40.333000');
INSERT INTO `quick_interaction` VALUES ('9', '快速搬运一把鱼叉与15Kg燃料到背包', '2026-05-22 22:17:55.368000', '不可以，快速搬运不可在搬运回合使用', '统治者', '1', 'quick_action', '10', '二阶堂希罗', '2026-05-22 22:40:46.787000', 'replied', '2026-05-22 22:40:46.787000');
INSERT INTO `quick_interaction` VALUES ('10', '在白天顺便练练射击技术 用来保护人民群众', '2026-05-22 22:42:22.920000', '好，很有精神', '反叛者', '1', 'quick_action', '11', '蟋蟀蜥蜴', '2026-05-22 23:21:31.906000', 'replied', '2026-05-22 23:21:31.906000');
INSERT INTO `quick_interaction` VALUES ('11', '查看轮船班次表看看有什么发现', '2026-05-22 22:48:39.772000', '你发现了剧情彩蛋，请查看规则书中的线索文献', '平民', '1', 'quick_action', '21', '乐语', '2026-05-22 23:23:19.173000', 'replied', '2026-05-22 23:23:19.173000');
INSERT INTO `quick_interaction` VALUES ('12', '尝试与相遇的人产生肢体接触', '2026-05-22 23:14:39.859000', '你随意碰了碰npc中的一人，对方看了你一眼没说什么', '平民', '1', 'quick_action', '31', '闲屿', '2026-05-22 23:52:15.059000', 'replied', '2026-05-22 23:52:15.061000');
INSERT INTO `quick_interaction` VALUES ('13', '我、对酒、GPT和镇长Vigil一起守镇武库。\n​其中统治层将会把一把威胁5的手枪和对应的弹药1交付我保管', '2026-05-22 23:42:21.390000', '好的', '冒险者', '1', 'supplementary_action', '14', 'Κάκτος西里尔', '2026-05-22 23:51:10.661000', 'replied', '2026-05-22 23:51:10.661000');
INSERT INTO `quick_interaction` VALUES ('14', '观察邮局墙上的邮票', '2026-05-23 01:03:49.181000', '你观察了邮票，边缘已经暗黄有年头了。', '冒险者', '1', 'quick_action', '21', '乐语', '2026-05-23 14:32:30.429000', 'replied', '2026-05-23 14:32:30.442000');
INSERT INTO `quick_interaction` VALUES ('15', '晚上去旅馆睡觉', '2026-05-23 01:04:11.146000', 'ok，已前往此处睡觉。', '冒险者', '1', 'supplementary_action', '21', '乐语', '2026-05-23 14:33:27.374000', 'replied', '2026-05-23 14:33:27.374000');
INSERT INTO `quick_interaction` VALUES ('16', '白天去矿场时候看没人看守顺手取走一些堆放的物资', '2026-05-23 01:05:04.465000', '白天已经结束了，此次行动失败', '冒险者', '1', 'supplementary_action', '21', '乐语', '2026-05-23 14:36:06.317000', 'replied', '2026-05-23 14:36:06.317000');
INSERT INTO `quick_interaction` VALUES ('17', '去码头拆船需要用白天行动点吗，还是用快速行动', '2026-05-23 19:37:41.842000', '白天行动，快速行动没那么厉害', '冒险者', '2', 'rule_consult', '21', '乐语', '2026-05-23 20:32:56.343000', 'replied', '2026-05-23 20:32:56.343000');
INSERT INTO `quick_interaction` VALUES ('18', '对 对酒（气象观测）散播瘟疫', '2026-05-23 19:43:08.923000', '收到', '天灾使者', '2', 'quick_action', '16', '孤城暮角', '2026-05-23 20:33:07.787000', 'replied', '2026-05-23 20:33:07.787000');
INSERT INTO `quick_interaction` VALUES ('19', '使用一个便当加额外的行动', '2026-05-23 19:51:22.909000', '你要干什么你说啊', '统治者', '2', 'quick_action', '10', '二阶堂希罗', '2026-05-23 20:33:30.774000', 'replied', '2026-05-23 20:33:30.774000');
INSERT INTO `quick_interaction` VALUES ('20', '使用一个面包加行动值', '2026-05-23 20:08:46.310000', '还发两条#指指点点', '统治者', '2', 'quick_action', '10', '二阶堂希罗', '2026-05-23 20:33:49.943000', 'replied', '2026-05-23 20:33:49.943000');
INSERT INTO `quick_interaction` VALUES ('21', '天灾使者的破坏行动：破坏面包店炉子', '2026-05-23 20:09:36.707000', '收到', '冒险者', '2', 'quick_action', '22', '11', '2026-05-23 20:34:05.496000', 'replied', '2026-05-23 20:34:05.496000');
INSERT INTO `quick_interaction` VALUES ('22', '白天行动1，去伐木工处，气象观测员不再陪同。', '2026-05-23 20:10:12.476000', '收到', '冒险者', '2', 'supplementary_action', '14', 'Κάκτος西里尔', '2026-05-23 20:34:33.631000', 'replied', '2026-05-23 20:34:33.632000');
INSERT INTO `quick_interaction` VALUES ('23', '白天的行动1，去伐木工处，气象观测员不再陪同', '2026-05-23 20:10:57.125000', '收到', '冒险者', '2', 'supplementary_action', '14', 'Κάκτος西里尔', '2026-05-23 20:35:43.192000', 'replied', '2026-05-23 20:35:43.192000');
INSERT INTO `quick_interaction` VALUES ('24', '快速行动1: 先收取来自统治者的物资。（食物，木材，和武器）', '2026-05-23 20:11:44.948000', '你这不是快速行动，你这是交易', '平民', '2', 'quick_action', '25', 'tony', '2026-05-23 20:36:03.493000', 'replied', '2026-05-23 20:36:03.493000');
INSERT INTO `quick_interaction` VALUES ('25', '使用一个面包。进行第三个人行动。再次使用职业技能。获得两块面包。', '2026-05-23 20:12:33.821000', '收到', '平民', '2', 'quick_action', '25', 'tony', '2026-05-23 20:36:20.090000', 'replied', '2026-05-23 20:36:20.091000');
INSERT INTO `quick_interaction` VALUES ('26', '个人行动结算后。一共给予两个面包给统治者。一个面包给医生（反抗者）。注意，每搓成一个面包就给出去，优先给反抗者。', '2026-05-23 20:14:41.764000', '你把行动都编程了不用我们的交易系统岂不是对不起我们俩的开发？？嗯？？', '平民', '2', 'quick_action', '25', 'tony', '2026-05-23 20:37:02.692000', 'replied', '2026-05-23 20:37:02.692000');
INSERT INTO `quick_interaction` VALUES ('27', '对 对酒（天气预测）散播瘟疫', '2026-05-23 20:19:08.331000', '收到', '天灾使者', '2', 'quick_action', '16', '孤城暮角', '2026-05-23 20:37:19.140000', 'replied', '2026-05-23 20:37:19.141000');
INSERT INTO `quick_interaction` VALUES ('28', '喝酒消除疲劳', '2026-05-23 20:33:03.793000', '好的，将调整你的状态', '平民', '2', 'quick_action', '29', '飞凡', '2026-05-23 20:49:27.989000', 'replied', '2026-05-23 20:49:27.989000');
INSERT INTO `quick_interaction` VALUES ('29', '白天行动一：在矿场与名为铁锤的矿工交互，并执行行动一备注里的行动。白天行动二：在集市与那位手工艺人交互，并执行备注中的行动！', '2026-05-23 20:35:01.074000', '了解', '反叛者', '2', 'supplementary_action', '30', 'MISD330', '2026-05-23 20:38:55.624000', 'replied', '2026-05-23 20:38:55.624000');
INSERT INTO `quick_interaction` VALUES ('30', '再喝两个酒', '2026-05-23 21:06:58.764000', '好的', '平民', '2', 'quick_action', '27', '得狗的老意', '2026-05-23 21:09:46.586000', 'replied', '2026-05-23 21:09:46.586000');
INSERT INTO `quick_interaction` VALUES ('31', '观察自己队伍中有没有内鬼', '2026-05-23 21:09:33.995000', '你观察了一波没发现什么。', '反叛者', '2', 'quick_action', '11', '蟋蟀蜥蜴', '2026-05-23 22:44:31.496000', 'replied', '2026-05-23 22:44:31.496000');
INSERT INTO `quick_interaction` VALUES ('32', '使用便当，额外使用一个行动点，搬运物资，从矿场仓库到避难所，优先生活必须品', '2026-05-23 21:28:48.125000', '收到', '统治者', '2', 'supplementary_action', '26', 'V', '2026-05-24 16:22:09.982000', 'replied', '2026-05-24 16:22:09.990000');
INSERT INTO `quick_interaction` VALUES ('33', '使用面包，前往监狱', '2026-05-23 21:29:28.244000', '好的', '统治者', '2', 'quick_action', '10', '二阶堂希罗', '2026-05-23 22:19:27.250000', 'replied', '2026-05-23 22:19:27.250000');
INSERT INTO `quick_interaction` VALUES ('34', '行动一：自己带着霰弹枪和弹药1发，与维修工（玩家）一起前往伐木营地。\n\n先和伐木工（npc）进行一个恳求，说明现在暴风雪来临，我们很需要他的帮助，需要木板蒸汽箱，拖拉机，电锯，木板（或者原木），发电机组（相当于搜刮地点），并且和他说镇长也同意我们求取这些，给了我们这些就不会强迫他当劳工了。\n\n如果他愿意就邀请他上船，本人会适当展示自己手中的霰弹枪。\n\n若伐木工（npc）始终不愿意，本人将拿枪威胁他，最起码把载具和发电机交出来。\n\n若再不同意，就把他束缚起来，拿走载具和发电机组。\n\n最坏的情况，如果他实在反抗激烈，就枪杀他，以无论如何都要拿到载具和发电机组为目的。', '2026-05-23 22:15:47.552000', '收到', '冒险者', '2', 'supplementary_action', '14', 'Κάκτος西里尔', '2026-05-24 16:22:22.593000', 'replied', '2026-05-24 16:22:22.593000');
INSERT INTO `quick_interaction` VALUES ('35', '行动二：先问手工艺人找我有什么事，听镇长说手工艺人有事找我。\n\n然后问手工艺人手中是否有沥青，如果可以的话向手工艺人要些沥青。如果不行就问对方哪里能搞到沥青。', '2026-05-23 22:16:09.522000', '手艺人给了你沥青，希望你带他走。他可以给你做东西，只要给素材就行。', '冒险者', '2', 'supplementary_action', '14', 'Κάκτος西里尔', '2026-05-23 22:43:51.406000', 'replied', '2026-05-23 22:43:51.406000');
INSERT INTO `quick_interaction` VALUES ('36', '对于第一个伐木营地的行动，我们还拆除了电机，要求拿电锯，然后我这里签订契约同意木工上船，拿到钥匙', '2026-05-23 22:16:24.622000', null, '冒险者', '2', 'ask_dm', '9', '对酒', null, 'processed', '2026-05-24 16:22:47.964000');
INSERT INTO `quick_interaction` VALUES ('37', '在探索地点部分我想邀请码头装卸工跟随我们冒险者一起，加入我们冒险者阵营，一起登上方舟', '2026-05-23 22:24:46.303000', '忽视：天灾使者，冒险者\n他本来是不喜欢你的，但是他看着你身后的船已经有了很大的变化，他点了点头。', '冒险者', '2', 'supplementary_action', '19', 'unPy-GPT', '2026-05-23 22:42:52.455000', 'replied', '2026-05-23 22:42:52.455000');
INSERT INTO `quick_interaction` VALUES ('38', '在守矿口的时候顺便带些煤矿走', '2026-05-24 00:40:57.539000', '好家伙', '平民', '2', 'quick_action', '31', '闲屿', '2026-05-24 16:22:57.584000', 'replied', '2026-05-24 16:22:57.584000');
INSERT INTO `quick_interaction` VALUES ('39', '喝酒消除疲劳', '2026-05-24 00:56:28.978000', '好的', '平民', '2', 'quick_action', '32', '澡堂子', '2026-05-24 13:34:10.210000', 'replied', '2026-05-24 13:34:10.210000');
INSERT INTO `quick_interaction` VALUES ('40', '使用酒消除过劳', '2026-05-24 13:09:49.980000', '好的', '天灾使者', '2', 'quick_action', '16', '孤城暮角', '2026-05-24 13:34:17.922000', 'replied', '2026-05-24 13:34:17.922000');
INSERT INTO `quick_interaction` VALUES ('41', '医疗手工人和自己', '2026-05-24 16:33:44.501000', '收到，但是医疗是自己主要行动做的，你做的是急救手工艺人', '反叛者', '2', 'quick_action', '30', 'MISD330', '2026-05-24 17:05:01.060000', 'replied', '2026-05-24 17:05:01.060000');
INSERT INTO `quick_interaction` VALUES ('42', '吃下便当后，去教堂逛逛，询问是否有人知道昨晚来往的人员', '2026-05-24 17:26:38.940000', 'npc回复你，现在教堂发生了大爆炸，昨天港口的那批人来到了这里，你在破窗处看到了兔兔的尸体，她刺穿了自己的脖颈，狂热且疯狂', '平民', '3', 'supplementary_action', '31', '闲屿', '2026-05-24 18:17:01.796000', 'replied', '2026-05-24 18:17:01.796000');
INSERT INTO `quick_interaction` VALUES ('43', '船长和我一起用海图进行了发报求援搬运，想确认是否有回报', '2026-05-24 17:29:50.863000', '“通讯……失败，……………………船长请………………，我方保持…………，…………安全抵达”\n你们的通讯出现了问题，但是你们说不定会遇到他们。1张天灾牌牌被替换为了奖励牌。', '冒险者', '3', 'supplementary_action', '21', '乐语', '2026-05-24 20:16:59.191000', 'replied', '2026-05-24 20:16:59.191000');
INSERT INTO `quick_interaction` VALUES ('44', '使用便当做盾×1箭×6', '2026-05-24 18:09:20.972000', null, '天灾使者', '3', 'quick_action', '16', '孤城暮角', null, 'processed', '2026-05-24 20:04:49.469000');
INSERT INTO `quick_interaction` VALUES ('45', '1.使用一个面包 2.将冒险者仓库的剩余物品（武器）全部提取到玩家仓库', '2026-05-24 18:10:05.870000', '结算完毕', '冒险者', '3', 'quick_action', '9', '对酒', '2026-05-24 20:17:56.178000', 'replied', '2026-05-24 20:17:56.178000');
INSERT INTO `quick_interaction` VALUES ('46', '对V（镇长）散播瘟疫', '2026-05-24 18:11:27.401000', null, '天灾使者', '3', 'quick_action', '16', '孤城暮角', null, 'processed', '2026-05-24 20:09:31.518000');
INSERT INTO `quick_interaction` VALUES ('47', '用面包运送木头到阵营仓库', '2026-05-24 18:20:47.307000', '结算完毕', '冒险者', '3', 'quick_action', '9', '对酒', '2026-05-24 20:19:52.332000', 'replied', '2026-05-24 20:19:52.332000');
INSERT INTO `quick_interaction` VALUES ('48', '让伐木工去做两次手搓船', '2026-05-24 18:21:04.444000', '结算完毕', '冒险者', '3', 'quick_action', '9', '对酒', '2026-05-24 20:19:56.533000', 'replied', '2026-05-24 20:19:56.533000');
INSERT INTO `quick_interaction` VALUES ('49', '重新补充一下使用面包，然后用载具运送木头到阵营仓库', '2026-05-24 18:21:29.746000', '结算完毕', '冒险者', '3', 'supplementary_action', '9', '对酒', '2026-05-24 20:20:00.972000', 'replied', '2026-05-24 20:20:00.973000');
INSERT INTO `quick_interaction` VALUES ('50', '致不明信号源： 我是本岛方舟船长。暴雪将至，我方即将启航。若您处确有补给，请提供物资清单、坐标与交接方式。请用数字确认您的经纬度。完毕。', '2026-05-24 18:25:40.881000', '“通讯……失败，……………………船长请………………，我方保持…………，…………安全抵达”\n你们的通讯出现了问题，但是你们说不定会遇到他们。1张天灾牌牌被替换为了奖励牌。', '冒险者', '3', 'quick_action', '14', 'Κάκτος西里尔', '2026-05-24 20:17:06.946000', 'replied', '2026-05-24 20:17:06.946000');
INSERT INTO `quick_interaction` VALUES ('51', '前往邮局对先前的不明信号源用电报机进行答复：致不明信号源： 我是本岛方舟船长西里尔。暴雪将至，我方即将启航。若您处确有补给，请提供物资清单、坐标与交接方式。请用数字确认您的经纬度。完毕。', '2026-05-24 18:34:08.328000', '“通讯……失败，……………………船长请………………，我方保持…………，…………安全抵达”\n你们的通讯出现了问题，但是你们说不定会遇到他们。1张天灾牌牌被替换为了奖励牌。', '冒险者', '3', 'quick_action', '14', 'Κάκτος西里尔', '2026-05-24 20:17:18.085000', 'replied', '2026-05-24 20:17:18.085000');
INSERT INTO `quick_interaction` VALUES ('52', '重新总结一下：1使用一个面包，用载具运送木头到阵营仓库 2.让伐木工去搓两次船，如果可以三项材料同时推进就同时推进，如果只能推一项就推10kg沥青', '2026-05-24 18:39:24.365000', '结算完毕', '冒险者', '3', 'supplementary_action', '9', '对酒', '2026-05-24 20:20:11.144000', 'replied', '2026-05-24 20:20:11.144000');
INSERT INTO `quick_interaction` VALUES ('53', '我去矿场仓库搬运里面的帆布，放到身上', '2026-05-24 18:40:44.130000', '收到', '冒险者', '3', 'quick_action', '21', '乐语', '2026-05-24 19:27:41.547000', 'replied', '2026-05-24 19:27:41.547000');
INSERT INTO `quick_interaction` VALUES ('54', '修改一下行动，让伐木工去搓金属，搓10kg金属，不搓沥青了', '2026-05-24 19:02:52.308000', '结算完毕', '冒险者', '3', 'supplementary_action', '9', '对酒', '2026-05-24 20:20:15.037000', 'replied', '2026-05-24 20:20:15.037000');
INSERT INTO `quick_interaction` VALUES ('55', '邀请装卸工与我们一同造船，白天两次行动都通过工作量推进金属物资进度', '2026-05-24 19:03:27.872000', '结算完毕', '冒险者', '3', 'quick_action', '19', 'unPy-GPT', '2026-05-24 20:37:32.498000', 'replied', '2026-05-24 20:37:32.498000');
INSERT INTO `quick_interaction` VALUES ('56', '让手工艺人搓木头', '2026-05-24 19:12:01.527000', '结算完毕', '冒险者', '3', 'quick_action', '14', 'Κάκτος西里尔', '2026-05-24 20:40:42.820000', 'replied', '2026-05-24 20:40:42.820000');
INSERT INTO `quick_interaction` VALUES ('57', '修改行动，伐木工去搓木头造船 搓两次木头', '2026-05-24 19:13:00.761000', '结算完毕', '冒险者', '3', 'supplementary_action', '9', '对酒', '2026-05-24 20:17:48.084000', 'replied', '2026-05-24 20:17:48.085000');
INSERT INTO `quick_interaction` VALUES ('58', '使用便当蹲守熊屋边上林子中，如果有人前来发起暗杀', '2026-05-24 19:17:21.235000', '好的，这是第三个行动点的行动', '天灾使者', '3', 'quick_action', '20', '追枫', '2026-05-24 20:15:17.219000', 'replied', '2026-05-24 20:15:17.219000');
INSERT INTO `quick_interaction` VALUES ('59', '吃掉便当，用工作量推进方舟建设木头5t', '2026-05-24 19:21:26.915000', '收到', '冒险者', '3', 'quick_action', '21', '乐语', '2026-05-24 20:41:03.168000', 'replied', '2026-05-24 20:41:03.168000');
INSERT INTO `quick_interaction` VALUES ('60', '吃了面包后感觉我浑身充满力量，将对酒拉到码头的40t木头，和个人仓库中的30kg沥青，以及所需螺旋桨全部投入到方舟建设中', '2026-05-24 19:33:14.410000', '完成', '冒险者', '3', 'quick_action', '19', 'unPy-GPT', '2026-05-24 20:41:28.406000', 'replied', '2026-05-24 20:41:28.406000');
INSERT INTO `quick_interaction` VALUES ('61', '对不起，最后一次修改行动了，让伐木工用电锯伐两次木头，然后把木头转给我和乐语各20t', '2026-05-24 19:38:27.069000', '结算完毕', '冒险者', '3', 'supplementary_action', '9', '对酒', '2026-05-24 20:18:03.756000', 'replied', '2026-05-24 20:18:03.756000');
INSERT INTO `quick_interaction` VALUES ('62', '取消使用便当', '2026-05-24 19:41:15.980000', '好的，会将给予物资收回。', '天灾使者', '3', 'supplementary_action', '16', '孤城暮角', '2026-05-24 20:16:43.721000', 'replied', '2026-05-24 20:16:43.721000');
INSERT INTO `quick_interaction` VALUES ('63', '白天的个人行动建造中补充提交20t木头，从阵营仓库中提取', '2026-05-24 19:47:32.664000', '收到', '冒险者', '3', 'supplementary_action', '21', '乐语', '2026-05-24 20:41:57.438000', 'replied', '2026-05-24 20:41:57.438000');
INSERT INTO `quick_interaction` VALUES ('64', '使用便当，调查巡夜人missbear', '2026-05-24 20:00:46.369000', '该玩家今日无法被探查', '统治者', '3', 'quick_action', '10', '二阶堂希罗', '2026-05-24 20:19:11.463000', 'replied', '2026-05-24 20:19:11.463000');
INSERT INTO `quick_interaction` VALUES ('65', '吃便当 第三个行动从矿坑搬300木头到避难所', '2026-05-24 20:17:33.652000', '收到', '统治者', '3', 'supplementary_action', '17', 'zzz', '2026-05-24 20:43:45.761000', 'replied', '2026-05-24 20:43:45.761000');
INSERT INTO `quick_interaction` VALUES ('66', '预埋炸药，引线从入口处沿着边缘往里布置，拿着引线的蹲守在入口直接可以看到来路的位置，拿着引线', '2026-05-24 21:19:27.719000', null, '统治者', '1', 'quick_action', '10', '二阶堂希罗', null, 'pending', '2026-05-24 21:19:27.719000');
INSERT INTO `quick_interaction` VALUES ('67', '拿一个警棍', '2026-05-24 22:21:32.496000', null, '反叛者', '3', 'quick_action', '24', '花海', null, 'pending', '2026-05-24 22:21:32.496000');
INSERT INTO `quick_interaction` VALUES ('68', '从仓库拿出枪和子弹 上膛', '2026-05-24 22:30:51.646000', null, '反叛者', '3', 'quick_action', '11', '蟋蟀蜥蜴', null, 'pending', '2026-05-24 22:30:51.646000');
INSERT INTO `quick_interaction` VALUES ('69', '111111', '2026-06-22 12:32:29.993000', null, '平民', '1', 'supplementary_action', '34', 'player', null, 'pending', '2026-06-22 12:32:29.993000');
INSERT INTO `quick_interaction` VALUES ('70', '前往墓地瞻仰西里尔船长。', '2026-06-22 12:36:54.068000', null, '平民', '1', 'quick_action', '34', 'player', null, 'pending', '2026-06-22 12:36:54.068000');
INSERT INTO `quick_interaction` VALUES ('71', '前往营地，拜访托马斯·伍德，并对其致以由衷的感谢。', '2026-06-22 12:37:30.201000', null, '平民', '1', 'quick_action', '34', 'player', null, 'pending', '2026-06-22 12:37:30.201000');
INSERT INTO `quick_interaction` VALUES ('72', '去面包店偷面包', '2026-06-22 12:46:36.046000', null, '平民', '1', 'quick_action', '34', 'player', null, 'pending', '2026-06-22 12:46:36.046000');
INSERT INTO `quick_interaction` VALUES ('73', '现在能做什么', '2026-06-22 12:47:41.983000', null, '平民', '1', 'ask_dm', '34', 'player', null, 'pending', '2026-06-22 12:47:41.983000');
INSERT INTO `quick_interaction` VALUES ('74', '自杀，干死自己', '2026-06-22 19:07:55.789000', null, '平民', '1', 'quick_action', '34', 'player', null, 'pending', '2026-06-22 19:07:55.789000');

-- ----------------------------
-- Table structure for rule_book
-- ----------------------------
DROP TABLE IF EXISTS `rule_book`;
CREATE TABLE `rule_book` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` text,
  `created_at` datetime(6) DEFAULT NULL,
  `order_num` int(11) NOT NULL,
  `section` varchar(50) NOT NULL,
  `title` varchar(100) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=156 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of rule_book
-- ----------------------------
INSERT INTO `rule_book` VALUES ('130', '一、白天自由行动（6:00-7:00）\r\n所有玩家选择两项自由行动。\r\n\r\n1.前往地点：选择一个地点进行探索，可以获得对应地点的信息（可能的资源信息取决于设施），防御值，NPC名单。同时可以选择使用非上锁的设施和地点NPC简单交互。\r\n在第一天开始时会在自己的家内醒来，会知道自己所在地点的信息。\r\n\r\n2.调查玩家：选择一玩家，知晓对方的自由行动，1/2概率获知阵营行动，无法调查潜行技能玩家。无法调查昨天隐藏行动玩家。\r\n\r\n3.生产：根据职业技能生产对应资源。\r\n\r\n4.使用特性：使用需要行动点的特性。\r\n\r\n5.使用职业技能：使用可能的职业技能。如果没有标有行动的技能不需要玩家行动就可以执行。\r\n\r\n6.隐藏：隐藏起自己，第二天不会被调查，也无法被私聊，无法成为统治者与密谋的行动目标。\r\n\r\n7.搬运：将物品从仓库移动到仓库或从仓库移动到自身，自身物品存放仓库不消耗行动点。\r\n\r\n二、主持人整理与反馈行动\r\n1.在8:00前结算\"调查\"、\"特性\"、\"职业技能\"、\"调查玩家\"。\r\n2.结算\"生产\"', null, '1', '每日游戏流程', '每日游戏流程', null);
INSERT INTO `rule_book` VALUES ('131', '白天自由行动（6:00-7:00）\r\n所有玩家选择两项自由行动。\r\n\r\n1.前往地点：选择一个地点进行探索，可以获得对应地点的信息（可能的资源信息取决于设施），防御值，NPC名单。同时可以选择使用非上锁的设施和地点NPC简单交互。\r\n在第一天开始时会在自己的家内醒来，会知道自己所在地点的信息。\r\n\r\n2.调查玩家：选择一玩家，知晓对方的自由行动，1/2概率获知阵营行动，无法调查潜行技能玩家。无法调查昨天隐藏行动玩家。\r\n\r\n3.生产：根据职业技能生产对应资源。\r\n\r\n4.使用特性：使用需要行动点的特性。\r\n\r\n5.使用职业技能：使用可能的职业技能。如果没有标有行动的技能不需要玩家行动就可以执行。\r\n\r\n6.隐藏：隐藏起自己，第二天不会被调查，也无法被私聊，无法成为统治者与密谋的行动目标。\r\n\r\n7.搬运：将物品从仓库移动到仓库或从仓库移动到自身，自身物品存放仓库不消耗行动点。\r\n\r\n主持人整理与反馈行动\r\n1.在8:00前结算\"调查\"、\"特性\"、\"职业技能\"、\"调查玩家\"。\r\n2.结算\"生产\"', null, '2', '自由行动', '白天自由行动', null);
INSERT INTO `rule_book` VALUES ('132', '自由讨论（5:00开始）\r\n在这个时间段开放地点语音频道，任何玩家可以自由选择频道进行语音讨论。\r\n\r\n自由讨论规则\r\n1.尽可能使用语音或者小群。\r\n2.如达成\"协议\"，需告知主持人，使得\"协议\"技能或使用协议书生效。\r\n3.禁止在此阶段进行任何的场外录音行为。', null, '3', '自由讨论', '自由讨论', null);
INSERT INTO `rule_book` VALUES ('133', '协议——某些讨论的结果需要告知主持人\r\n\r\n1.加入阵营：一位玩家接受另一位所属阵营的玩家的邀请，并加入阵营群。这种情况非初始阵营玩家可能背叛。\r\n\r\n2.契约协议：两位或多位玩家在群聊或者kook频道都在场的的情况下，达成合作的意向与合作目标，并由其中一位玩家与女巫沟通（需要女巫在场并且同意），完成契约协议，或者使用协议书。如果违反协议内容，则会受到严苛的惩罚（由女巫或协议书使用者决定）。契约协议必须是一个完整的句子，不能是多条不相干的内容。每人每天最多签订一个契约。\r\n只有女巫（男巫）可以签订契约，若不使用协议书签订内容会被公开。但只公开内容不公开谁签订的契约。\r\n\r\n3.物品交换：两位或多位玩家交换物品。\r\n\r\n4.冒险者、反抗者、统治者阵营初始拥有1个协议书。\r\n\r\n什么不是协议？\r\n1.信息讨论：职业、拥有物品、行为、线索等等信息都不是协议。\r\n2.口头协作：两位或多位玩家达成合作的意向，临时加入阵营。', null, '4', '协议', '协议', null);
INSERT INTO `rule_book` VALUES ('134', '夜间阶段（8:00到所有结算完毕）\r\n\r\n在该阶段，阵营玩家的行动与可能的暴力冲突根据其顺序结算，而无关的玩家可以继续进行自由讨论。\r\n\r\n1.玩家默认所在地点在自己家中，去往别的地点过夜需要告知主持人，不消耗行动点。\r\n2.主持人计算可能的冲突，并确认是否发生阵营行动与暴力冲突。\r\n3.结算阵营行动与暴力冲突。\r\n4.统治者公布第二天劳工名单。\r\n5.阵营告示可能的公开信息。\r\n6.主持人公开可以公开的夜间阶段总结信息。\r\n7.消耗与燃料食物。\r\n8.结算所有资源与设施情况。\r\n\r\n详细和补充规则见\"夜间结算阶段规则\"。', null, '5', '夜间阶段', '夜间阶段', null);
INSERT INTO `rule_book` VALUES ('135', '一、群组\r\n1.四个阵营组建微信群组，可以在任意时间段自由讨论。如果在自由讨论阶段平民明确加入阵营并签订协议，通知主持人并拉入对应阵营群聊。\r\n2.所有的暴力冲突由主持人拉冲突参与者建小群，冲突结束后解散。\r\n3.所有人都在的微信群在游戏开始后禁止讨论。\r\n\r\n二、kook语音频道\r\n自由讨论阶段的语音频道，由玩家自主组织。\r\n\r\n三、kook公屏（集市）\r\n自由讨论阶段的文字频道，记住文字频道会留下记录，谨慎讨论。\r\n\r\n四、kook留言板\r\n在游戏中，统治者与冒险者可以在该频道发布信息，例如招募、资源征集等等。\r\n\r\n禁止一切形式的私自拉群行为。', null, '6', '群组与交流', '群组与交流', null);
INSERT INTO `rule_book` VALUES ('136', '资源交换：核心原则\r\n\r\n1.自由自主的交换：玩家间交易遵旨自愿、自由、自主的原则。玩家可以在任何可能的交流场景达成资源交换的意图。所有交换双方是自愿、自由的。\r\n\r\n2.见证人：所有资源交换需要告知主持人，由主持人见证确保双方资源交换属实，只需要告知一下就可以。\r\n\r\n3.信息不对称：交换双方对资源真实价值、稀缺度和自身需求的信息不同。\r\n\r\n4.物资交换登记：交换任意一方需在网站页面上填写对应的物资数量，并发给主持人对应交易的kook截屏即为物资交换完成。\r\n\r\n参考\r\n- 摆摊：玩家可在KOOK公屏（集市）发布文字信息。发布[求购]急需医疗包，可用木材或情报交换。\r\n- 公开拍卖：统治者或拥有大量资源的玩家可发起资源出售，自由设定商品单与交易形式。', null, '7', '资源交换', '资源交换', null);
INSERT INTO `rule_book` VALUES ('137', '一、玩家与玩家仓库资源转移\r\n在夜晚阶段自由交换，但是需告知主持人。\r\n\r\n二、玩家与仓库资源转移\r\n- 小镇内地点转移：一人一天不影响玩家行动的情况下50kg，跳过玩家行动300kg。\r\n- 岛屿地点转移：一人一天不影响玩家行动的情况下30kg，跳过玩家行动300kg。\r\n\r\n三、仓库与仓库资源转移\r\n- 小镇内地点：一人一天不影响玩家行动的情况下100kg，跳过玩家行动可以搬运500kg。不同的载具可以额外增加搬运量。\r\n- 岛屿地点：一人一天不影响玩家行动的情况下50kg。跳过玩家行动可以搬运300kg。不同的载具可以额外增加搬运量。\r\n\r\n四、生产资源原则\r\n- 自主原则：在网站行动选择哪个行动进行生产。\r\n- 强迫原则：如果因其他玩家强迫行动生产，被强迫方可以告知主持人，将生产资源添加到自己的仓库。', null, '8', '资源移动', '资源移动', null);
INSERT INTO `rule_book` VALUES ('138', '一、计算总战力\r\n总战力 = 参战人数 + 武器威胁值之和 + 地点防御值（仅有守方）+ 技能/特性加成\r\n- 技能加成：格斗使用近战武器/射击使用远程武器 +1\r\n- 地点防御值：守方直接加在总战力里。攻方不加地点防御值。\r\n- 防弹衣/盾：不加战力，用于抵消受伤。物品效果查看物品栏。\r\n每人只能携带一件武器和两件防具参加战斗。\r\n\r\n二、最终攻击力判定\r\n- 攻方最终战力 = 攻方总战力 + 1d4\r\n- 守方最终战力 = 守方总战力 + 1d4\r\n- 差值 = 攻方最终 - 守方最终\r\n\r\n三、根据差值决定结果\r\n差值 ≥ 5：大胜——攻方无伤，守方1人死亡其余受伤\r\n差值 3-4：胜利——攻方无伤，守方1-2人重伤其余受伤\r\n差值 1-2：小胜——攻方无伤，守方全部受伤\r\n差值 0：僵持——双方各1人受伤\r\n差值 -1～-2：小败——攻方全部受伤，守方无伤\r\n差值 -3～-4：失败——攻方1-2人重伤其余受伤，守方无伤\r\n差值 ≤ -5：大败——攻方1人死亡其余受伤，守方无伤\r\n\r\n四、特殊规则\r\n- 在无远程武器的情况下，重伤和受伤标记会在手持近战武器的人员中随机给予。\r\n- 在有远程武器的情况下，重伤和受伤标记在全体人员中随机给予。\r\n- 在失败/胜利情况下1-2人重伤是看1d6骰子的出目，1-3为一人重伤，4-6为两人重伤。\r\n- 战败方掉落全体（每个人）的最高威胁值物品，受伤以及重伤玩家会被己方阵营带走。', null, '9', '战斗规则', '战斗规则', null);
INSERT INTO `rule_book` VALUES ('139', '一、状态说明\r\n- 受伤：获得\"受伤\"标记（无法生产，格斗无效），可被医疗消除。\r\n- 重伤：获得\"重伤\"标记（无法行动，每夜阶段结束时若不急救则死亡）。急救消耗5医疗资源，将重伤转为受伤。\r\n- 死亡：直接移除角色。\r\n\r\n二、战斗事例\r\n示例1（1v1）\r\n攻方1人（步枪4），守方1人（手枪2，地点防御2）\r\n攻方总战力=1+4=5，守方=1+2+2=5\r\n掷骰：攻方3→8，守方1→6，D=2 → 小胜\r\n结果：守方受伤（倒地），攻方无伤。\r\n\r\n示例2（多人，大胜）\r\n攻方5人，武器威胁和=15，守方2人，武器威胁和=4，地点防御3\r\n攻方战力=5+15=20，守方=2+4+3=9\r\n攻方掷4→24，守方掷1→10，D=14 ≥5 → 大胜\r\n结果：守方1人死亡，另1人受伤。攻方无伤。', null, '10', '伤亡规则', '伤亡规则', null);
INSERT INTO `rule_book` VALUES ('140', '夜间结算阶段规则\r\n\r\n1.玩家默认所在地点在自己家中，去往别的地点过夜需要告知主持人，不消耗行动点。地点确定需要在决定晚上行动时告知主持人，不会额外提醒玩家是否更改过夜地点。\r\n\r\n2.主持人计算可能的冲突，并确认是否发生阵营行动与暴力冲突。\r\n\r\n3.结算阵营行动与暴力冲突\r\n- 夜间行动时生产建造搬运类优先结算，战斗与冲突最后结算。\r\n- 如果想要指定杀某个人就要在白天行动时使用调查明确对方的去向。如果是为了杀人去的某地点但是对方不在可以选择是否发起攻击，如果不攻击本行动点不会返还。\r\n- 如果发生A想杀→B想杀→C的情况，会判定为先结算B→C，再结算A→B。在此种情况下不会发生混战情况。\r\n- 如果A想杀→C，B想杀→C，会判定为A和B攻击C，AB是否进行战斗是在AB攻击C之后询问两方意见的。此时因为不在对应地点内，防守方战斗应不计地点防御值。\r\n\r\n4.统治者需要商议第二天劳工名单，并在夜晚结算之前给与主持人。\r\n\r\n5.阵营告示主持人可能要公开的信息。\r\n\r\n6.主持人公开可以公开的夜间阶段总结信息。例如死亡人数，但不会告知死亡地点和何人死亡、哪里被袭击等等。\r\n\r\n7.消耗与燃料食物\r\n- 每人每天的基础燃料消耗是15kg木材，如因其他技能或者天灾牌出现极寒情况，基础消耗调整为30kg木材。\r\n- 每人每天的基础食物消耗是2单位。\r\n\r\n8.结算所有资源与设施情况。', null, '11', '夜间结算', '夜间结算阶段规则', null);
INSERT INTO `rule_book` VALUES ('141', '一、天灾倒计时\r\n通过一个0到100的数字，作为\"天灾\"的进度，当任何游戏阶段到达100时，当天夜晚阶段的最后，进入游戏结算。同时这个进度会影响游戏中六面骰的概率。同时，通过一套天灾牌额外增加天灾点数，与各结局最终的存活难度。\r\n- 每天默认推进33点，第三天推进34点。\r\n- 主持人会抽取3张秘密天灾牌，由杀戮者选择1张触发。特色技能可以查看天灾牌。\r\n\r\n二、六面骰\r\n在游戏中有很多行为或者判定需要骰6面骰来决定是否成功或决定判断结果，例如射击、天灾使者特殊规则、特性中的随机奖励。', null, '12', '天灾系统', '天灾系统', null);
INSERT INTO `rule_book` VALUES ('142', '1.低温侵袭\r\n某处墙体结冰，寒气渗入。本天所有燃料消耗增加20%，木材消耗量15kg→18kg。\r\n\r\n2.灾难蔓延\r\n增加5天暴雪持续时间。\r\n\r\n3.粮仓鼠患\r\n仓库中储存的粮食被老鼠啃食，损失10%的食物储备（向下取整）。\r\n\r\n4.燃料泄漏\r\n储油桶老化破裂，损失一处仓库的10%的燃料储备（优先扣除煤油/燃油）。\r\n\r\n5.工具锈蚀\r\n生产工具普遍老化。当天所有生产行动（渔猎、伐木、挖矿等）产量-20%。\r\n\r\n6.海水倒灌\r\n风暴潮淹没码头设施，沿海仓库的部分物资被冲走（损失10%），方舟受损10%。\r\n\r\n7.水源污染\r\n岛上淡水水源被动物尸体污染，所有玩家当天需额外消耗1升煤油（烧开水）或面临患病风险。\r\n\r\n8.信仰崩塌\r\n神父以及占卜师等精神领袖陷入自我怀疑，当天无法使用\"布道\"或\"占星\"技能。若第三天抽中不影响终局结算加成。\r\n\r\n9.燃料受潮\r\n露天堆放的木柴被雨淋湿。随机一个仓库或玩家损失30kg木材。\r\n\r\n10.逃役\r\n一名劳工趁夜色逃走了。统治者当天指定的劳工名单中，随机一人自动失效（不会劳作，也不会计入劳工）。主持人随机选择，不公开是谁。该玩家知道自己被逃役释放，当天正常进行行动。\r\n\r\n11.祭品\r\n有人在教堂门口发现一只被割喉的黑羊。第二天必定触发一张额外天灾牌（命运在积蓄）。', null, '13', '天灾牌', '天灾牌', null);
INSERT INTO `rule_book` VALUES ('143', '一、NPC基础规则\r\n- 会添加生产类NPC，NPC初始拥有不同阵营倾向。\r\n- NPC在不同的地点，玩家使用调查地点行动后知晓其地点存在的NPC列表。统治者开局知道所有NPC列表与其职业。\r\n- 对话行动可以与不同的NPC互动，获得不同的信息、效果，或者请求帮助。由主持人决定。\r\n\r\n二、态度说明\r\n- 喜好：该NPC会加入、帮助此阵营玩家。\r\n- 厌恶：该NPC会忽视、敌对此阵营玩家，不会提供帮助，反对被当做劳工等。\r\n- 忽视：该NPC对此阵营无感，不帮助也不会加入，如果被强迫，可能对寻找喜好阵营帮助。', null, '15', 'NPC规则', 'NPC规则', null);
INSERT INTO `rule_book` VALUES ('144', '你拥有这个岛屿上的巨大话语权与暴力力量，但是你无法肆意妄为，因为在暗地里，反抗者与天灾使者正在积蓄力量，你必须给予足够的空间与利益来收买更多的人来对抗反抗者与天灾使者。天灾使者自翊正义但掩盖不了他们是混沌的人，只想着杀人。也许你也可以称呼他们另一个名字——杀戮者。\r\n\r\n一、阵营行动（统治者行动只要可以执行就不限次数）\r\n1.安排人员——统治者可以安排一位玩家或者喜好统治者的NPC执行自由行动，并强制共享自由行动的结果信息。玩家可以拒绝，这样做，统治者将有合理的理由进行审判。\r\n\r\n2.安排看守——统治者在夜晚阶段之前安排人员选择一个地点，通常是仓库或统治者所在地。玩家可以拒绝，这样做，统治者将有合理的理由进行审判。每位看守为一个地点防御值增加3，以及携带唯一武器威胁值的防御值。\r\n\r\n二、劳工机制\r\n夜间阶段开始之前（每天18:00前）统治者阵营讨论出一份由其他玩家组成的劳工名单，人数为0～10人（24人局0～6人，48人局0～10人）。\r\n\r\n成为劳工的玩家第二天白天行动将在矿场强制劳动，并添加\"过劳\"标记。该玩家的夜间行动可以进行，此时\"过劳\"标记已经生效。\r\n\r\n统治者可以安排劳工进行建设避难所行动。\r\n\r\n劳工类型基础值：\r\n- 普通劳工：基础值4，压榨后7\r\n- 职业劳工：基础值5，压榨后8\r\n\r\n统治者可以自由选择劳工的换班形式。一天一轮或不进行轮换。（但是要记住，食物与资源的生产仍然需要人手，而过度的压制会加强反抗者的势力，或是让所有阵营联合起来）\r\n\r\n三、过劳标记\r\n拥有过劳标记无法执行生产行动。拥有过劳标记在进行劳工的行动，投1d6投，判定为1则死亡。过劳标记在第三天消除。也就是说，必须隔一天进行劳作。\r\n\r\n四、压榨机制（统治者特权）\r\n- 次数限制：无所需特性情况下统治者每天最多可以对3名劳工使用压榨。\r\n- 效果：被压榨劳工的建造值按上表翻倍（4→7或5→8），但立即获得「受伤」标记。\r\n- 受伤后果：无法进行生产行动，格斗技能无效。\r\n- 策略提示：压榨可以快速推进建造值，但会增加医疗负担和激起反抗情绪。\r\n\r\n五、暴力机关\r\n统治者与其下属控制着仓库与几乎所有的火器，在冲突中占据绝对的优势。\r\n- 在游戏开始，每位统治者拥有一把韦伯利.38手枪与7发子弹。\r\n- 在游戏开始，仓库拥有大量的热武器。统治者可以自由分配武器，自由保存武器存放场景。\r\n\r\n六、公开宣传\r\n任何时候，统治者可以在频道公屏发布消息帖子。\r\n\r\n七、夜晚机制\r\n1.公开审判：统治者如果拥有对某个玩家的\"合理\"罪证信息（例如反抗统治者或天灾使者阵营，逃劳役等等）——可以选择在夜间阶段在集市召开公开审判。需要占用你们一人的夜间行动点召开，占用行动点的人为审判发起人。\r\n在审判期间，玩家可以在kook语音频道讨论。统治者可以选择性地收取民众的想法。或者选择公开投票的方式进行审判。\r\n审判结果最终由统治者决定，但如果不顺应民意可能会进一步失去民众的信任。\r\n\r\n审判结果：\r\n1.劳工——此玩家一直成为劳工。\r\n2.监禁——由统治者决定该玩家被囚禁在监狱，监禁时间无法进行任何自由行动。但需要保证他的基本存活。他的生存物资会从统治者仓库每日结算时自动扣除。\r\n3.死刑——该玩家死亡。（不顺应民意使用此种结果请务必三思）\r\n4.剥夺资产——该玩家的所有物品转移到统治者控制的仓库。\r\n5.无罪——也许是真的无辜又或者你们为了其他人而妥协了。\r\n\r\n2.额外行动：作为统治者夜晚出行虽然不完全安全但如果非要这么做的话也不是不行。使用手电筒在夜晚时间你可以执行白天回合的一个任意行动，或者尝试杀掉某个绊脚石。此环节无法寻找NPC进行对话，NPC在没有前置沟通的情况下会默认回家睡觉。\r\n\r\n八、下属\r\n统治者可以收买其他玩家，为自己做事。可以许诺资源、武器、阵营身份，或是权力。\r\n\r\n九、胜利条件\r\n目标1：扑灭反抗者与天灾使者，限制冒险者的发展，尽可能收买平民。\r\n目标2：建设避难所，在暴雪之前储备尽可能多的资源。\r\n目标3：让所有\"自己人\"和自己进入避难所。并且在暴雪将至阶段活下来。\r\n', null, '16', '阵营机制', '阵营机制-统治者', null);
INSERT INTO `rule_book` VALUES ('145', '你不是暴徒，你是被迫举枪的平民。\r\n当统治者的铁腕压得整座岛屿喘不过气——劳工被压榨、资源被垄断、异见者被关进牢房，你选择了另一条路：反抗。\r\n反抗者不是天生的叛逆者，而是那些受够了沉默代价的人。你们在暗处联络同胞，用破坏和密谋削弱统治者的控制，用舆论和施压点燃平民的怒火。你们不追求权力，只追求平等与自由。\r\n你们可以与除了统治者之外的所有阵营交流沟通，收集更多的平民力量。如果有必要潜入统治者进行情报刺探也是不错的选择。\r\n\r\n一、阵营行动（3选1）\r\n1.额外劳动：如果当天白天有行动的执行的生产，可以额外增加一半产出。\r\n\r\n2.暗中联络：选择1-3位玩家，由主持人统一代发一条秘密信息。什么都可以，不超过50字符即可（以WPS字符计算为准）。\r\n\r\n3.额外行动——作为反抗者你已经习惯在黑暗中行动，你不需要手电筒即可进行夜晚行动。在夜晚时间你可以执行白天回合的一个任意行动，或者尝试杀掉某个反抗革命的人。此环节无法寻找NPC进行对话，NPC在没有前置沟通的情况下会默认回家睡觉。\r\n\r\n初始资源：反叛者拥有猎枪×1，猎枪弹×2，猎弓×2，箭矢×4。\r\n\r\n二、向统治者施压\r\n在夜间阶段，任何玩家都可以团结其他玩家发起对统治者的施压，通过在公屏中发声的方式。而如果反抗者拥有合理的理由（通常是统治者的暴政或贪腐），则可以通过宣传来扩大施压的声音，以罢工或其他方式以此威胁。理由通常需要在自由行动阶段进行调查获得。\r\n通常，施压的目标有，\"一个合理的劳工换班机制\"、\"扩大民兵\"、\"让资源由全体镇民管理\"等等。\r\n在夜晚阶段开始前计算统一参加施压的人群，如果同意施压的人群超过玩家半数，统治者就必须妥协。\r\n记住，反抗者不应该直接参与施压，一旦身份暴露，就有被坑杀的可能。应当在自由讨论阶段合理的利用舆论。\r\n\r\n三、夜间回合（可以做也可以空过）\r\n1.进行密谋：袭击地点\r\n需要组织者确定袭击地点。可探索地点和统治者仓库（不显示在地图上）。\r\n- 每一位参与者增加一点成功率，参与者每一个拥有\"潜行\"技能增加1点成功率。每一位参与者佩戴唯一一把武器，增加武器对应\"威慑点\"的成功率。\r\n- 每个地点拥有基础防御值。统治者在目标地点的每一位玩家增加3点防御值，每一位玩家佩戴的唯一一把武器，增加武器对应\"威慑点\"的防御值。\r\n- 在夜间阶段结算密谋，比较成功率与防御值。\r\n  - 成功率大于防御值超过3点：密谋成功。\r\n  - 超过9点：行动成功，但行动暴露，所有参与成员名称公开。\r\n  - 不超过3点：失败。如果有，则需要放弃一把武器。\r\n  - 成功率小于防御值：行动失败。参与行动者可以选择受降或者暴力反抗。\r\n- 如果成功，玩家可以选择破坏该地点，或选择搜刮。破坏会直接烧毁该地点，在之后的游戏中，无法前往，无法使用该地点的设施，导致NPC死亡。搜刮则可以带走地点的所有资源。\r\n- 如果有民兵或治安官或看守里应外合，则减少3点防御值。\r\n\r\n2.暗杀统治者\r\n需要组织者确定统治者所在的地点。判定规则同密谋。\r\n- 如果成功率大于防御值超过3点：密谋成功，目标统治者死亡。\r\n- 如果超过9点：目标统治者死亡，但行动暴露，所有参与成员名称公开。\r\n- 如果统治者死亡，统治者阵营可以选择是否公开死亡消息。\r\n\r\n3.解救人员\r\n需要组织者确定被关押人员所在的地点。判定规则同密谋。\r\n- 潜行技能增加3点成功率（而非1点）。\r\n\r\n四、暴力革命\r\n在下列里程碑事件之中，完成任意其中三个，即可在当天或之后的阵营回合之中选择策划暴力革命。\r\n\r\n五、胜利条件\r\n目标1：推翻统治者，建立新的秩序。\r\n目标2：获得更多的平民。\r\n目标3：尽可能让所有人进入避难所。在暴雪将至阶段活下来。', null, '17', '阵营机制', '阵营机制-反抗者', null);
INSERT INTO `rule_book` VALUES ('146', '在集体大会中，支持建设避难所的平民远高于建设大船，但是你依旧坚持这场灾难不会结束。而随着统治者暴力的加强，更多人会明白，建设避难所是一个糟糕的决定。\r\n建设方舟：一艘20世纪的帆船已经破旧不堪，但是仍然比归属于渔民与船队的渔船大得多，或许可以召集人手修复好帆船，在暴雨来临前让更多人逃离。\r\n\r\n一、阵营行动（4选1）\r\n1.额外调查：如果当天执行的调查地点，调查玩家。调查地点改为两个，调查玩家改为两名。\r\n\r\n2.额外劳动：如果当天有至少任意一个行动点执行的生产，可以额外增加一半产出。\r\n\r\n3.看守方舟——冒险者在白天行动阶段可以使用行动点作为方舟看守，在夜晚阶段为方舟建造地点增加对于武器与技能的防御值。\r\n\r\n4.方舟建设：在阵营回合可以进行方舟建造，在第三天开始时加入冒险者的人员都可以用自己所拥有的任意数量行动点进行方舟建造。\r\n\r\n初始资源：拥有猎弓2，12只箭，1只矛，一把手枪，4发手枪子弹。\r\n\r\n二、收集物资\r\n在游戏中，冒险者需要整合成员已有的资源，与其他阵营与玩家交换资源，生产资源，公开宣传以募集资源，直接冲突争夺资源，来获得建设方舟的材料与出航的必要物资。\r\n\r\n建设方舟的总共资源（50点载重的情况）：\r\n- 木材250吨\r\n- 金属制品100吨\r\n- 密封材料：沥青100kg\r\n- 发动机至少1个，最多3个\r\n- 螺旋桨2个\r\n- 发电机1个\r\n\r\n技能资源（出海需要的技能，为终局结算提供更多的优势）：\r\n航海、海洋导航、天气预报、渔猎\r\n\r\n三、建设与载重方案\r\n船身大小：冒险者可以选择不同的建设方案来匹配人数。\r\n初始已完成10吨木材，20吨金属制品资源的建设。（根据参与人数改变）\r\n\r\n以250吨木材的大小为基础设计，为50点载重。更小的船型会更加劣势。\r\n- 在完成50点基础载重之后，每增加10吨木材，5吨金属制品与5公斤密封材料，可以增加2点载重。\r\n- 每缺少10吨木材或5吨金属制品或5公斤密封材料，减少3点载重，百分之2.5方舟完成度。资源短缺是短板效应，按最低缺少计算。\r\n\r\n建设规则：\r\n加入冒险者任意一人可以在阵营行动时花费一行动点投入不超过单日最高数量的任意数量资源进行建造：\r\n每天投入最高为：木材30吨、金属制品20吨、密封材料20kg\r\n\r\n若材料不足需白天花费一人一行动点工作量推进：5吨木材、5吨铁制品建造进度、5kg密封材料\r\n\r\n四、出航准备\r\n- a.每1点载重一人，可以乘载一人\r\n- b.每1点载重，100单位食物\r\n- c.每1点载重，2吨燃料\r\n- d.每1点载重，500kg密封材料\r\n- e.每1点载重，2吨木材\r\n- f.医疗资源、武器等物品不计算重量。\r\n\r\n五、海上时间\r\n注意，某些危机事件可能增加航行天数。每多一天就需要额外抽取一张危机事件牌。\r\n- 1个发动机：8天航行时间\r\n- 2个发动机：6天航行时间\r\n- 3个发动机：4天航行时间\r\n- 在没有发动机出航或者只有一个发动机的时候，可以制造帆来辅助航行。在具有两个发动机时帆的作用消失。\r\n- 单帆：消耗100m绳索和80米帆布。收集好材料后需要一个人夜晚阵营回合建造帆，之后就可以使用。效果：提供移速加成。\r\n- 在没有发动机的时候，航行时间为10天。\r\n- 在只有一个发动机的时候，航行时间为7天。\r\n\r\n六、夜晚行动\r\n可以做可以不做。\r\n\r\n七、胜利条件\r\n目标1：选择与反抗者或统治者结盟，拉拢更多的平民。\r\n目标2：获得更多的资源建设方舟。\r\n目标3：在暴雪将至前出海，活下去。\r\n', null, '18', '阵营机制', '阵营机制-冒险者', null);
INSERT INTO `rule_book` VALUES ('147', '你们是天灾的使者，是净化这里的存在。这里已经是腐烂得无可救药了。统治者做着梦幻想统治永远存在却又看不起所有的居民；反抗者自以为正义实际上上位之后会不会变成统治者那些人也尤未可知；冒险者空有一腔热血但是方舟会去往哪里谁能知道。而且你们中有人知道的是大家……在这里的所有人——都是罪人。\r\n\r\n罪在哪里？\r\n眼睁睁看着邻居被送去审判，你闭了嘴，你是罪人。\r\n为了多领一份口粮，偷偷举报了别人，你是罪人。\r\n明知这座岛的规则是吃人的，你还是选择顺从，而不是站出来，你是罪人。\r\n统治者犯的罪是傲慢，反抗者犯的罪是自私，冒险者犯的罪是盲目，而普通人犯的罪是——沉默。\r\n所以，我们是执行者。所以是暴雪吗？是天灾吗？不！这是净化。\r\n\r\n一、阵营行动\r\n1.破坏：选择一个地点的一个设施，使其无法使用。如果目标地点被安排监管，无法破坏。\r\n\r\n2.额外调查：如果当天执行的调查地点，调查玩家。调查地点改为两个，调查玩家改为两名。\r\n\r\n3.诅咒：花费一件威胁值4以上的武器，选择2位玩家，知晓该玩家的阵营。该玩家被标记「诅咒」。\r\n\r\n二、阵营机制\r\n1.阻止胜利：天灾使者既可以是统治者的走狗，施加暴行，激化统治者与平民的矛盾，延缓建设的进度，也可以假扮反抗者或冒险者，分化各个阵营的力量或凝聚力。最终目的，都是增加玩家死亡，推进命运，减缓地下避难所与方舟的建设进度。尽可能让灾难杀死所有人。\r\n\r\n2.正义的潜伏：天灾使者可以以天灾使者的身份加入其他阵营，并使用两套阵营规则。仍然使用天灾使者的胜利条件。无法无视契约协议的惩罚。天灾使者知晓当天天灾牌的内容。\r\n\r\n3.进行密谋：袭击地点\r\n判定规则同反抗者密谋。\r\n- 如果成功，玩家可以选择破坏该地点，或选择搜刮。破坏会直接烧毁该地点，在之后的游戏中，无法前往，无法使用该地点的设施，导致地点所有NPC死亡。搜刮则可以带走地点的所有资源。\r\n\r\n4.制造恐怖：在夜晚阶段，需要组织者确定袭击地点或玩家。袭击玩家需要对方拥有诅咒，至少一位参与者参与。袭击地点，则至少需要两位参与者。\r\n- 制造恐怖的基础成功率取决于场上诅咒的数量和参与者的数量。\r\n- 每有一个诅咒（包括普通诅咒和瘟疫诅咒）成功概率+1\r\n- 每额外多一名参与者，成功概率+1\r\n- 使用1d6骰子投出数值+成功概率。结果大等于6既为成功。无视所有的防具，战力差距。\r\n- 在第一天时有平民玩家因为使者制造恐怖而死亡时，对方将受到天灾的感召。阵营身份变更为天灾使者继续进行游戏。（仅限一人）保留对方原有特性和技能。\r\n- 如果失败，参与者将暴露身份。身份被目标地点所属玩家与目标玩家知晓。不会告知你的具体身份是天灾使者。只会告知玩家袭击了对方，没有成功。同时所有被诅咒标记玩家在公屏公开。\r\n- 如果成功，如果目标拥有诅咒，目标玩家死亡。如果是目标地点，由参与者选择破坏1d6数量的资源品名或设施。\r\n- 如果目标死亡，统治者阵营可以选择是否公开死亡消息。\r\n\r\n5.神契：当命运之轮到达100，触发暴雪将至。杀戮者共有三个神启身份对应其他玩家的特性，名为：瘟疫、战争和饥荒。如果剩下的杀戮玩家只剩下一位他变为死亡骑士，特性更改为死亡骑士。\r\n\r\n三、胜利条件\r\n目标1：隐藏在人群中，蛊惑平民，制造信息迷雾，破坏其他阵营的策略。\r\n目标2：阻止方舟与避难所的建设，破坏资源与仓库。\r\n目标3：让暴雪更快降临。', null, '19', '阵营机制', '阵营机制-天灾使者', null);
INSERT INTO `rule_book` VALUES ('148', '一、阵营机制\r\n1.加入阵营：平民玩家可以自由选择加入反抗者或是冒险者的阵营，或是成为统治者的下属。\r\n\r\n2.口头契约：玩家与玩家之间可以口头协议加入阵营，但无法使用阵营行动与机制。但在这种情况下，平民玩家可以通过口头契约加入多个阵营。\r\n\r\n3.自组织：平民玩家可以尝试形成团体，为一个目标行动。\r\n\r\n4.加入阵营与背叛：平民玩家可以通过各种方式，加入某个阵营。加入对应阵营的玩家可以使用阵营机制与阵营行动。可以在行动阶段告知主持人决定背叛，在夜晚阶段由主持人结束背叛阵营事件，并且踢出阵营群聊。无法再使用阵营机制与阵营行动。\r\n\r\n二、胜利条件\r\n目标1：你也要死吗？不，请活下去，直到最后。\r\n', null, '20', '阵营机制', '阵营机制-平民', null);
INSERT INTO `rule_book` VALUES ('149', '一、方舟建设\r\n在阵营回合可以进行方舟建造，在第三天开始时加入冒险者的人员都可以用自己所拥有的任意数量行动点进行方舟建造。\r\n\r\n二、收集物资\r\n在游戏中，冒险者需要整合成员已有的资源，与其他阵营与玩家交换资源，生产资源，公开宣传以募集资源，直接冲突争夺资源，来获得建设方舟的材料与出航的必要物资。\r\n\r\n建设方舟的总共资源（50点载重的情况）：\r\n- 木材250吨\r\n- 金属制品100吨\r\n- 密封材料：沥青100kg\r\n- 发动机至少1个，最多3个\r\n- 螺旋桨2个\r\n- 发电机1个\r\n\r\n技能资源（出海需要的技能，为终局结算提供更多的优势）：\r\n航海、海洋导航、天气预报、渔猎\r\n\r\n三、建设与载重方案\r\n船身大小：冒险者可以选择不同的建设方案来匹配人数。\r\n初始已完成10吨木材，20吨金属制品资源的建设。（根据参与人数改变）\r\n\r\n以250吨木材的大小为基础设计，为50点载重。更小的船型会更加劣势。\r\n- 在完成50点基础载重之后，每增加10吨木材，5吨金属制品与5公斤密封材料，可以增加2点载重。\r\n- 每缺少10吨木材或5吨金属制品或5公斤密封材料，减少3点载重，百分之2.5方舟完成度。资源短缺是短板效应，按最低缺少计算。\r\n\r\n建设规则：\r\n加入冒险者任意一人可以在阵营行动时花费一行动点投入不超过单日最高数量的任意数量资源进行建造：\r\n每天投入最高为：木材30吨、金属制品20吨、密封材料20kg\r\n\r\n若材料不足需白天花费一人一行动点工作量推进：5吨木材、5吨铁制品建造进度、5kg密封材料\r\n\r\n四、出航准备\r\n- a.每1点载重一人，可以乘载一人\r\n- b.每1点载重，100单位食物\r\n- c.每1点载重，2吨燃料\r\n- d.每1点载重，500kg密封材料\r\n- e.每1点载重，2吨木材\r\n- f.医疗资源、武器等物品不计算重量。\r\n\r\n五、海上时间\r\n注意，某些危机事件可能增加航行天数。每多一天就需要额外抽取一张危机事件牌。\r\n- 1个发动机：8天航行时间\r\n- 2个发动机：6天航行时间\r\n- 3个发动机：4天航行时间\r\n- 在没有发动机出航或者只有一个发动机的时候，可以制造帆来辅助航行。在具有两个发动机时帆的作用消失。\r\n- 单帆：消耗100m绳索和80米帆布。收集好材料后需要一个人夜晚阵营回合建造帆，之后就可以使用。效果：提供移速加成。\r\n- 在没有发动机的时候，航行时间为10天。\r\n- 在只有一个发动机的时候，航行时间为7天。', null, '21', '方舟建造', '方舟建造', null);
INSERT INTO `rule_book` VALUES ('150', '一、战斗类\r\n格斗——在密谋、暴力冲突中，如果装备近距离武器增加威胁值。如果调查玩家，可以选择在夜晚阶段骰d6，寻找攻击机会（初始12 30/3, 50/4, 80/5），成功可以发起小规模冲突。多人调查可以袭击。\r\n\r\n射击——在密谋、暴力冲突中，如果装备远距离武器增加威胁值。如果调查玩家，可以选择在夜晚阶段骰d6，寻找攻击机会（初始12 30/3, 50/4, 80/5），成功可以发起小规模冲突。多人调查可以袭击。\r\n\r\n二、生产类（在白天行动阶段使用生产行动，如需设施需要对应设施完好）\r\n- 捕鱼：在码头使用渔船设施，获得食物10单位。\r\n- 食物生产：使用牲畜设施，获得食物15单位。\r\n- 伐木：使用电锯20吨木材，否则5吨。\r\n- 挖掘：使用电钻20吨石料，否则5吨。\r\n- 手工艺：工具制备单制作一件或至多3件材料、物品或武器。\r\n  制作列表：复合盾（3kg金属制品+3kg木材）、副鱼叉/矛（5kg金属制品+5kg木材）、规制箭矢（3kg木材+3kg金属制品3支）\r\n- 木石工艺：使用木板蒸汽箱，将30吨原木转化为20吨木板，消耗10吨燃料/使用切石机，将20吨石料转化为15米石墙。\r\n- 射击狩猎：需要远程武器（无需子弹或弓箭），5kg肉。\r\n\r\n三、功能类（在白天阶段联系主持人使用，或者其他特殊方式使用）\r\n- 潜行（被动）：为谋略增加成功率，无法被调查。\r\n- 启蒙：每天白天行动时一次，消耗一盒粉笔和2支铅笔，选择除自己之外最多2名玩家，使其临时学会一项基础技能（从以下列表中选：急救、潜行、格斗、捕鱼、伐木、挖矿），从第一天夜晚回合生效持续到第二天白天回合结束。若该玩家已经拥有该技能则增产50%。\r\n- 布道：每天白天行动时一次，选择最多为3人，被布道的玩家在下一个行动中生产增加一半（渔猎、伐木、挖矿）/或者最多为3位玩家消除诅咒（被诅咒的玩家没有任何征兆，这是一个纯预判的技能效果）。\r\n- 急救：花费5医疗资源，将\"重伤\"改为\"受伤\"标记。\r\n- 医疗：每天白天行动时一次，选择一位或至多5位\"受伤\"标记的玩家，目标消除\"受伤\"标记。每个花费3医疗资源，选择一位或多位过劳标记的玩家，消除过劳标记。每个花费2医疗资源。（可以混合）\r\n- 通灵：需消耗10根蜡烛和2升酒精举行仪式。每天白天行动时一次，与一位已死亡玩家建立临时私信交流，死亡玩家需要尽可能告知死前所有信息（由主持人保底提供信息）同时你有1/2概率会获得对方的技能。\r\n- 协议契约：为一位或多位玩家建立协议契约。可以指定违反协议的惩罚内容。\r\n- 潜水：每天白天行动时可以进行一次，在码头使用，由主持人投d6决定结果：1-5食物8单位，6沉船遗物（随机一件较高价值物品，由主持人决定，如医疗包、维修工具包、旧手枪+弹药、信号枪等）。\r\n- 维修：每天可使用一次，修复一个被破坏的设施，花费5维修资源。\r\n- 搬运（被动）：搬运量永远是其他职业的两倍。\r\n\r\n四、特殊类（特殊技能需要自由行动中的技能行动执行）\r\n- 海洋导航：为方舟终局结算提供好的结局倾向，可以查看今日所有的天灾牌。\r\n- 偷盗：选择一位玩家，获得对方的秘密资源信息，选择一件物品获得。需要投掷1d6+1检定在5以上即可获得对方一件物品。\r\n- 天气预测：你通过科学的手段发现异常的暴雪天气的来临，可以查看今日所有的天灾牌。为所有终局结算提供好的结局倾向。\r\n- 占星：为避难所终局结算提供好的结局倾向。在全局游戏中你可以将一张天灾牌撕毁（不生效），或让一张危机事件牌撕毁（不生效）。\r\n- 烘焙：需要10单位的食物和15kg木材可以制作一份便当。便当：当天额外获得1白天行动点，每人每天最多使用1次。\r\n- 巡逻：夜晚行动，若你目前是非阵营玩家想使用该技能而不想加入阵营可以用第二天的一个行动补觉（体现为第二天的一个自由行动将变为其他且只能睡觉）使用该技能。选择一个地点，当晚该地点内非统治者阵营玩家的夜晚行动成功率-30%，巡夜人知晓其行动类型。需要巡夜人当天未处于\"劳工\"或\"过劳\"状态。', null, '22', '技能表', '技能表', null);
INSERT INTO `rule_book` VALUES ('151', '一、状态标记\r\n- 过劳：拥有过劳标记无法执行生产行动、调查玩家和隐匿。拥有过劳标记在当天夜晚行动和第二天进行需要行动点的生产行动时，投1d6骰子，判定为1则死亡。过劳标记在第三天消除。也就是说，必须隔一天进行劳作。5瓶朗姆酒可以消除过劳状态。\r\n- 受伤：无法进行生产行动，格斗技能无效。\r\n- 重伤（致命伤害）：在夜晚阶段如果没有被急救，将死亡。\r\n- 束缚：无法执行自由行动和夜间行动，除非束缚被解除。\r\n- 虚弱：无法生产，格斗、射击技能无效，第三天消除，或者喝酒。\r\n- 诅咒标记：只有天灾使者知晓效果。\r\n\r\n二、其他\r\n- 赐福1：每次战斗，免疫第一次被命中的攻击骰。默认为1威胁，可以与近战武器堆叠。', null, '23', '标记系统', '标记系统', null);
INSERT INTO `rule_book` VALUES ('152', '一、进入避难所\r\n在第三天夜晚环节前往避难所。并被避难所的所属玩家或阵营同意进入。\r\n在进入避难所时除统治者之外的玩家需从个人仓库选择5-10种，不超过5000kg重量的物品直接转移到避难所仓库作为进入的投诚。若投诚之后还有剩余物资，选择1-2件可以偷偷带入避难所。\r\n在反抗者夺取避难所控制权时，可以自由设定要不要把玩家所有资产充公。\r\n\r\n二、登上方舟\r\n在任意一天夜晚阶段用行动点前往方舟。并被方舟的所属冒险者阵营同意登上方舟。所有除武器装备之外所有资源将自动转移到冒险者阵营仓库（不能超出方舟载重）。\r\n\r\n三、特别注意\r\n- 避难所不能在天灾值到达100之前启动关门机制。\r\n- 方舟可以在天灾值到达100之前开船离开，如果天灾值到达100强制登船离开。开船行为不可逆，不可以开船之后掉头回到小岛。\r\n- 避难所和方舟的关门与开船机制都在每天的阵营行动和夜晚回合之后。（半夜关门和半夜走）每天最后一个行动结算。', null, '24', '终局结算', '进入避难所与方舟', null);
INSERT INTO `rule_book` VALUES ('153', '避难所结局结算\r\n\r\n第一阶段：暴雪持续时间判定\r\n暴雪不会立刻结束，其持续时间决定了避难所需要承受的总考验时长。\r\n- 基础持续时间：90天。\r\n- 杀戮者干预：每位存活的杀戮者（天灾使者）玩家，将额外触发一张危机事件牌。\r\n- 天灾诅咒：每一个进入避难所玩家所拥有的诅咒标记，会额外抽取一张危机事件牌。\r\n- 暴雪持续时间（D）= 90 +（已触发天灾牌数量）+（天灾使者进入避难所人数×5）。\r\n\r\n第二阶段：避难所基础资源需要\r\n固定每人10日消耗：\r\n- 食物：每人需要25000大卡。\r\n- 燃料：每人需要100kg木材或50公斤煤炭或等价热量。\r\n- 照明与设备：固定消耗5升煤油。\r\n- 人力需求：进入避难所的人数×（暴雪持续天数/10）\r\n\r\n危机处理：\r\n抽取最终天数/10天的危机事件牌。（如是小数按向下取整计算）\r\n例如天灾最终持续天数为95天。95÷10=9.5，向下取整为9。\r\n混入了一个天灾使者额外加1危机事件牌。最终抽取9+1=10张危机事件牌。\r\n牌堆构成：危机事件牌 + 来自技能的希望牌（拥有占星和天气预测的玩家各提供1张正面事件牌加入牌堆）。\r\n\r\n第三阶段：终局资源结算与生存检定\r\n结算完所有危机事件牌后。通过三种资源确定最终存活人数。\r\n最终幸存人数 = 初始进入人数 × 生存系数（S）\r\n\r\n生存系数（S）由以下几个关键资源的充足率决定，每项系数初始为1.0，根据短缺程度扣减：\r\n\r\n1.食物充足率（F）\r\nF = 实际食物储备总量（大卡）/（总人口 × 2单位食物/天 × D）\r\n影响：若F < 1.0，则S = S × F。例如：食物只够70%的需求，则幸存人数直接打7折。\r\n\r\n2.燃料充足率（H）\r\nH = 实际燃料储备总热量/（总人口 × 每日基础燃料热量 × D）\r\n若H < 1.0，则S = S ×（H^0.5）。（平方根意味着燃料短缺的影响稍缓，但会导致冻伤和疾病）。燃料严重短缺时（例如H<0.5），主持人可额外判定直接冻死一定比例玩家（如5%-10%）。\r\n\r\n3.医疗与秩序系数（M）\r\n- 医疗资源储备：每10份医疗资源，使M增加0.05（上限+0.2）。\r\n- \"患病\"标记：结算时，每存在一个此标记，使M减少0.02。\"患病\"标记来源于危机事件牌。\r\n- 特殊职业或特性：拥有布道、医疗、占星的玩家并存活，可以为群体提供0.1的士气加成。\r\n- S = S ×（1.0 + M）。M可以是正数或负数。\r\n\r\n最终结局与阵营胜利关联：\r\n【新纪元的基石】（对应S ≥ 0.8）\r\n反抗者：由其主导，则达成人民之声与地下黎明结局。\r\n统治者：由其主导，则达成地下黎明结局。无阵营：地下黎明。\r\n\r\n【惨白的黎明】（对应0.5 ≤ S < 0.8）\r\n反抗者：由其主导，则达成人民之声结局。\r\n统治者：由其主导，则达成王的废墟结局。无阵营：挽歌结局。\r\n\r\n【地狱归来的幸存者】（对应0.2 ≤ S < 0.5）\r\n统治者：由其主导，则达成王的废墟结局。\r\n杀戮者：达成天灾的馈赠结局。\r\n\r\n【寂静的坟墓】（对应S < 0.2）\r\n杀戮者：达成天灾的馈赠结局。', null, '25', '终局结算', '避难所结局结算', null);
INSERT INTO `rule_book` VALUES ('154', '方舟结局结算\r\n当冒险者阵营成功建造可以出航的方舟，并在暴雪来临前启航，游戏进入此结算流程。这是一个基于资源、技能、事件检定的叙事性终局。\r\n\r\n第一阶段：启航准备\r\n结算基础：\r\n- 最终登船玩家名单、各玩家携带的私人资源。\r\n- 方舟公共储备：食物（大卡）、燃料、医疗资源、工具、备用船材（木材、金属制品）等。\r\n\r\n第二阶段：航行阶段事件检定\r\n- 固定消耗：每人每天消耗2单位食物。\r\n- 燃料：根据发动机数量、航行天数和航速计算。如果公共储备无法满足消耗可以拆除一部分船只（减少船体完整度5%来补充一吨燃料）补足。\r\n- 抽取并结算航行事件：\r\n  牌堆构成：危机事件牌 + 来自技能的希望牌（拥有天气预测、海洋导航技能的玩家各提供1张正面事件牌加入牌堆）。\r\n  抽取数量：杀戮者干预——每位成功登船的杀戮者玩家，额外触发一张危机事件牌。危机事件抽取：航行天数×1+船上每个\"诅咒\"标记一张。\r\n- 阶段结算：如果船体完整度降至0%，或食物完全耗尽，则航行失败，或燃料无法完成动态增加的航行天数，直接进入【出海死亡】结局。\r\n\r\n第三阶段：登陆检定与最终结局\r\n判定完所有事件牌。最终生存系数（S）计算：\r\n- 基础生存率：S = 1.0\r\n- 资源修正：食物F = 实际食物储备总量（大卡）/（总人口 × 2500大卡/天 × D）。若F < 1，则S = S × F。\r\n- 船体完整度修正：H = 船体完整度（%） / 100。若H < 1，则S = S × H。\r\n- 技能与秩序修正（M）：正面——每拥有一项关键技能（渔猎、海洋导航、天气预测）+0.05；负面——每一个\"患病\"标记使M减少0.02。\"患病\"标记会在危机事件牌中产生。\r\n- 最终S = S_initial ×（资源修正）×（1 + M），S值范围通常在0到1.5之间。\r\n\r\n最终结局判定：\r\n\r\n【诺亚方舟】\r\n条件：S ≥ 1.1。船体完整度>70%，资源充足，士气高昂。\r\n\"桅杆指向崭新的海岸线。方舟几乎完好，储备尚丰。人们相互扶持着踏上了坚实的土地。\"\r\n结局：远航者\r\n\r\n【出海逃生——幸存者】\r\n条件：0.5 ≤ S < 1.1。船体有一定损坏，资源紧张，减不减员按各种物资储备结算。\r\n\"船勉强冲上沙滩，龙骨发出最后的呻吟。每个人都精疲力尽。回首望去，故乡已沉入海平线之下。但前方，是陌生的、充满未知的土地。你们活下来了。\"\r\n结局：黎明之前\r\n\r\n【出海死亡——深海长眠】（失败结局）\r\n条件：S < 0.5或船沉、资源耗尽。\r\n\"……指南针失灵，风暴永无止息。最后一片帆被撕碎，船舱开始进水。在绝望的呼喊与冰冷的海水之间，方舟的梦想连同它的乘客，一同沉入永恒的寂静。\"\r\n结局：深海长眠', null, '26', '终局结算', '方舟结局结算', null);
INSERT INTO `rule_book` VALUES ('155', '一、核心原则\n超游行为是指玩家利用游戏角色不可能获得的信息或渠道（即“超出角色认知”），来为自己或所在阵营谋取优势，从而破坏游戏公平性与沉浸感的行为。\n所有行为判定均以 “你的角色在游戏内能否合理知晓或做到” 为标准。\n惩罚的目的并非针对玩家本人，而是为了维护游戏环境的健康，确保所有参与者都能享受公平、沉浸的生存博弈体验。\n玩家死亡后，如无特别情况，将被移除游戏阵营群与kook频道，仅保留v游戏协调群，作为幽灵存在，并且无法发表与游戏内发言。\n二、超游行为列表（严禁事项）\n第一类：信息泄露与私下沟通\n1，跨阵营秘密信息交换：未经主持人建立临时频道或拉群。不同阵营玩家通过kook频道外渠道（如微信、QQ）交换游戏关键信息（如阵营身份、职业特性、持有道具、行动计划、调查结果等）。\n第二类：利用外部信息\n1，共享主持人私信内容：将主持人或者网页反馈的行动结果、资源变更、战斗详情、特殊线索内容等，通过“截图”、“复制文字”这两种方式分享到公告频道或者分享给其他不应知晓的玩家。\n例外：可以通过转述游戏信息，与其他玩家交流自己的行动，资源信息和行动反馈。\n例外：游戏规则明确允许的公开信息（如“公开宣传”内容）或技能效果（如“讲故事的人”告知的内容）除外。如果你不确定，请不要分享。\n根据玩家现实情况推断游戏行为：例如，因为现实中知道某人擅长欺骗，或因其在群聊中的发言风格，就在游戏内断定其为“天灾使者”或“背叛者”，并采取行动，而无任何游戏内通过“调查玩家”或“前往地点”获得的证据。\n例外：基于该玩家在游戏内已公开或在可获知范围内的发言（如公开宣传、审判发言）进行推断。\n利用规则漏洞或未公开设定：通过反复阅读或分析规则文档，发现并利用设计上的非预期优势，且该行为不符合规则叙述的常理或主持人意图。\n例外：利用规则中明确写明但被其他玩家忽略的机制（如“规则没写，就是能做”），不算超游，但建议先向主持人确认。\n第三类：行动与时间线作弊\n1，非规定时间提交或修改行动：在主持人规定的行动提交截止时间后，私自联系主持人要求修改或补交自由行动、技能使用或密谋参与，除非有主持人事先同意的特殊情况（如网络故障已报备）。\n根据已结算结果“事后诸葛亮”：例如，在主持人私信告知你某地点的NPC已被调动后，你才在公共讨论区宣称“我早就计划去那里调查了”。\n第四类：角色扮演与沉浸破坏\n1，公开游戏内私密信息：故意在公开场合（公屏、大型语音频道）大声朗读或截图展示你的角色资源仓库清单、秘密阵营身份、特性技能效果等，除非游戏机制明确要求或允许公开（如“公开宣传”）。\n2，以现实身份代替角色发言：在游戏讨论中使用“我（现实名字）认为”、“我（现实身份）觉得”等方式进行交流，而非以角色身份思考和发言。例如：“我觉得GM这个规则有问题”应私信主持人，而非在公屏讨论。\n3，恶意质疑或纠缠主持人裁决：在主持人已根据规则做出明确裁决后，仍反复在公开频道争辩、试图以现实辩论影响游戏内判定结果。\n正确做法：对裁决有疑问，应私信主持人礼貌询问。\n第五类：恶意破坏游戏进程\n1，冒充主持人或发布虚假官方指令：在任何频道（公屏、阵营群聊）发布看似来自主持人的虚假公告、指令或要求。\n2，行为或言论恶意针对玩家本人：在游戏内或游戏外，因为游戏内冲突（对方阵营行动导致你损失、对方欺骗了你）而对玩家本人进行现实人身攻击、辱骂或恶意诋毁。\n例外：在游戏内以角色身份进行扮演式威胁或谴责（如“我绝不会放过那个背叛者”）不算超游。\n三、给所有玩家的建议\n1沉浸第一：时刻问自己：“我的角色现在知道什么？会怎么做？”\n2沟通透明：如需进行游戏允许外的特殊互动，先私信主持人申请，由主持人裁定是否以及如何实现。\n3尊重规则与主持人：主持人是游戏的仲裁者。对裁决有疑问可私信礼貌询问，但最终应尊重主持人的决定，主持人有对规则最终解释权。\n4共同维护环境：如果你怀疑其他玩家有超游行为，请私信主持人提供具体证据或描述，而非在公开频道指责。', null, '27', '超游细则', '超游细则', null);

-- ----------------------------
-- Table structure for selected_catastrophe
-- ----------------------------
DROP TABLE IF EXISTS `selected_catastrophe`;
CREATE TABLE `selected_catastrophe` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deck_id` int(11) NOT NULL COMMENT '牌组ID',
  `player_id` int(11) DEFAULT NULL COMMENT '选择的玩家ID（天灾使者）',
  `selected_at` datetime DEFAULT NULL COMMENT '选择时间',
  `is_active` tinyint(1) DEFAULT '0' COMMENT '是否正在生效',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `deck_id` (`deck_id`),
  KEY `player_id` (`player_id`),
  CONSTRAINT `selected_catastrophe_ibfk_1` FOREIGN KEY (`deck_id`) REFERENCES `catastrophe_deck` (`id`) ON DELETE CASCADE,
  CONSTRAINT `selected_catastrophe_ibfk_2` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COMMENT='已选择的天灾牌表';

-- ----------------------------
-- Records of selected_catastrophe
-- ----------------------------
INSERT INTO `selected_catastrophe` VALUES ('1', '12', null, null, '0', '2026-05-22 18:42:58', '2026-05-23 14:33:24');
INSERT INTO `selected_catastrophe` VALUES ('2', '6', null, null, '0', '2026-05-22 18:42:58', '2026-05-23 14:33:24');
INSERT INTO `selected_catastrophe` VALUES ('3', '33', '20', '2026-05-22 20:58:46', '0', '2026-05-22 18:42:58', '2026-05-23 14:33:24');
INSERT INTO `selected_catastrophe` VALUES ('4', '15', null, null, '0', '2026-05-23 14:33:24', '2026-05-23 14:34:40');
INSERT INTO `selected_catastrophe` VALUES ('5', '5', null, null, '0', '2026-05-23 14:33:24', '2026-05-23 14:34:40');
INSERT INTO `selected_catastrophe` VALUES ('6', '14', null, null, '0', '2026-05-23 14:33:24', '2026-05-23 14:34:40');
INSERT INTO `selected_catastrophe` VALUES ('7', '30', null, null, '0', '2026-05-23 14:34:39', '2026-05-23 14:51:42');
INSERT INTO `selected_catastrophe` VALUES ('8', '24', null, null, '0', '2026-05-23 14:34:39', '2026-05-23 14:51:42');
INSERT INTO `selected_catastrophe` VALUES ('9', '7', '16', '2026-05-23 14:47:32', '0', '2026-05-23 14:34:40', '2026-05-23 14:51:42');
INSERT INTO `selected_catastrophe` VALUES ('10', '27', null, null, '0', '2026-05-23 14:51:42', '2026-05-24 17:15:09');
INSERT INTO `selected_catastrophe` VALUES ('11', '29', '16', '2026-05-23 15:00:20', '0', '2026-05-23 14:51:42', '2026-05-24 17:15:09');
INSERT INTO `selected_catastrophe` VALUES ('12', '1', null, null, '0', '2026-05-23 14:51:42', '2026-05-24 17:15:09');
INSERT INTO `selected_catastrophe` VALUES ('13', '23', null, null, '1', '2026-05-24 17:15:09', '2026-05-24 17:15:09');
INSERT INTO `selected_catastrophe` VALUES ('14', '4', null, null, '1', '2026-05-24 17:15:09', '2026-05-24 17:15:09');
INSERT INTO `selected_catastrophe` VALUES ('15', '18', '16', '2026-05-24 17:29:10', '1', '2026-05-24 17:15:09', '2026-05-24 17:29:10');

-- ----------------------------
-- Table structure for shelter_daily_labor
-- ----------------------------
DROP TABLE IF EXISTS `shelter_daily_labor`;
CREATE TABLE `shelter_daily_labor` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `game_day` int(11) NOT NULL COMMENT '游戏天数',
  `worker_kind` varchar(10) NOT NULL DEFAULT 'player' COMMENT 'player|npc',
  `worker_id` int(11) NOT NULL COMMENT '玩家ID或NPC ID',
  `build_value` int(11) NOT NULL DEFAULT '0' COMMENT '当日贡献建造值',
  `is_exploited` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否压榨（建造值翻倍等由主持人裁定）',
  `is_escaped` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否逃役（不计入劳工）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shelter_labor_day_worker` (`game_day`,`worker_kind`,`worker_id`),
  KEY `idx_game_day` (`game_day`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COMMENT='避难所每日劳工名单（总建造值=SUM(build_value)，含玩家与NPC）';

-- ----------------------------
-- Records of shelter_daily_labor
-- ----------------------------
INSERT INTO `shelter_daily_labor` VALUES ('1', '1', 'player', '23', '4', '0', '0', '2026-05-22 19:18:00');
INSERT INTO `shelter_daily_labor` VALUES ('2', '1', 'npc', '5', '8', '1', '0', '2026-05-22 19:18:00');
INSERT INTO `shelter_daily_labor` VALUES ('3', '1', 'npc', '6', '8', '1', '0', '2026-05-22 19:18:00');
INSERT INTO `shelter_daily_labor` VALUES ('4', '1', 'npc', '3', '7', '1', '0', '2026-05-22 19:18:00');
INSERT INTO `shelter_daily_labor` VALUES ('5', '1', 'player', '29', '4', '0', '0', '2026-05-22 19:18:00');
INSERT INTO `shelter_daily_labor` VALUES ('6', '2', 'player', '29', '7', '1', '0', '2026-05-23 19:19:46');
INSERT INTO `shelter_daily_labor` VALUES ('7', '2', 'player', '31', '4', '0', '0', '2026-05-23 19:19:46');
INSERT INTO `shelter_daily_labor` VALUES ('8', '2', 'player', '32', '7', '1', '0', '2026-05-23 19:19:46');
INSERT INTO `shelter_daily_labor` VALUES ('9', '2', 'player', '28', '0', '0', '1', '2026-05-23 19:19:46');
INSERT INTO `shelter_daily_labor` VALUES ('10', '2', 'player', '27', '7', '1', '0', '2026-05-23 19:19:46');
INSERT INTO `shelter_daily_labor` VALUES ('11', '2', 'player', '16', '5', '0', '0', '2026-05-23 19:19:46');
INSERT INTO `shelter_daily_labor` VALUES ('12', '3', 'player', '11', '7', '1', '0', '2026-05-24 18:07:37');
INSERT INTO `shelter_daily_labor` VALUES ('13', '3', 'player', '30', '7', '1', '0', '2026-05-24 18:07:37');
INSERT INTO `shelter_daily_labor` VALUES ('14', '3', 'player', '23', '7', '1', '0', '2026-05-24 18:07:37');
INSERT INTO `shelter_daily_labor` VALUES ('15', '3', 'player', '13', '4', '0', '0', '2026-05-24 18:07:37');
INSERT INTO `shelter_daily_labor` VALUES ('16', '3', 'player', '29', '4', '0', '0', '2026-05-24 18:07:37');
INSERT INTO `shelter_daily_labor` VALUES ('17', '3', 'player', '24', '5', '0', '0', '2026-05-24 18:07:37');

-- ----------------------------
-- Table structure for shelter_labor_day
-- ----------------------------
DROP TABLE IF EXISTS `shelter_labor_day`;
CREATE TABLE `shelter_labor_day` (
  `game_day` int(11) NOT NULL COMMENT '游戏天数',
  `verified` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'DM是否已结算确认',
  `verified_at` datetime DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`game_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='避难所每日劳工结算状态';

-- ----------------------------
-- Records of shelter_labor_day
-- ----------------------------
INSERT INTO `shelter_labor_day` VALUES ('1', '1', '2026-05-23 01:29:01', '2026-05-23 01:29:01');
INSERT INTO `shelter_labor_day` VALUES ('2', '1', '2026-05-23 19:32:38', '2026-05-23 19:32:38');
INSERT INTO `shelter_labor_day` VALUES ('3', '1', '2026-05-24 18:45:18', '2026-05-24 18:45:18');

-- ----------------------------
-- Table structure for shelter_progress
-- ----------------------------
DROP TABLE IF EXISTS `shelter_progress`;
CREATE TABLE `shelter_progress` (
  `id` int(11) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `current_build_value` int(11) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of shelter_progress
-- ----------------------------
INSERT INTO `shelter_progress` VALUES ('1', '2026-05-12 09:58:06.701000', '50', '2026-05-12 09:58:06.701000');

-- ----------------------------
-- Table structure for shelter_stock
-- ----------------------------
DROP TABLE IF EXISTS `shelter_stock`;
CREATE TABLE `shelter_stock` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_type` enum('item','weapon','ammo','material') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品类型：item道具/weapon武器/ammo弹药/material材料',
  `item_id` int(11) NOT NULL COMMENT '物品ID，关联对应类型表的主键',
  `quantity` int(11) NOT NULL DEFAULT '0' COMMENT '物品数量',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_shelter_stock_type_item` (`item_type`,`item_id`),
  KEY `idx_item_type` (`item_type`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=162 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统治者避难所物资库存表';

-- ----------------------------
-- Records of shelter_stock
-- ----------------------------
INSERT INTO `shelter_stock` VALUES ('135', 'item', '9', '1', '2026-05-22 18:46:38.634000', '2026-05-22 19:23:50.519000');
INSERT INTO `shelter_stock` VALUES ('158', 'material', '8', '320', '2026-05-22 19:25:03.406000', '2026-05-23 22:39:19.034000');
INSERT INTO `shelter_stock` VALUES ('159', 'material', '5', '127', '2026-05-22 19:25:07.551000', '2026-05-22 19:25:07.551000');
INSERT INTO `shelter_stock` VALUES ('160', 'material', '2', '2320', '2026-05-23 22:39:36.507000', '2026-05-24 21:30:14.657000');
INSERT INTO `shelter_stock` VALUES ('161', 'material', '10', '1', '2026-05-24 15:10:30.978000', '2026-05-24 15:10:30.978000');

-- ----------------------------
-- Table structure for skill
-- ----------------------------
DROP TABLE IF EXISTS `skill`;
CREATE TABLE `skill` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '技能名称',
  `function` text NOT NULL COMMENT '技能效果描述',
  `faction` enum('平民','统治者','冒险者','反叛者','天灾使者') NOT NULL DEFAULT '平民' COMMENT '阵营限制',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `idx_faction` (`faction`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COMMENT='个人技能表';

-- ----------------------------
-- Records of skill
-- ----------------------------
INSERT INTO `skill` VALUES ('1', '囤积症', '你屋里堆满了舍不得扔的旧报纸、空罐头和麻绳。获得额外的资源【金属制品1000kg，煤油100升，帆布50米，麻绳50米】', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('2', '情报贩子', '你和村里的几个孩子是铁哥们，总能先知道些风声。可以每天指定npc「阿丹」执行调查玩家行动，不占用你的主要行动。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('3', '扮演：圣徒', '你是小镇最虔诚的人。你相信神会拯救所有人，请聆听神父的声音。你会知道他是否能够信任。这是一个公开的扮演特性，请遵守圣徒的规律，他可能代表着一种话语权。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('4', '秘密钥匙', '你拥有小镇一个废弃房屋的地下室钥匙。所有人都不知道还有这样一个地方。若你终局来临时没有进入庇护所或者避难所，你可以在这里储存够你存活90天及以上的物资进入个人结算结局。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('5', '第六感', '你在岛上活了几十年，谁对你有恶意你闻都闻出来。效果：你能够知道对你使用调查的玩家名称', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('6', '偷窃癖', '你喜欢偷东西的感觉，你获得技能偷盗。效果：偷盗：选择一位玩家，获得对方的秘密资源物品信息。需要投掷1d6+1检定在5以上即可获得对方一件物品。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('7', '潮汐症', '每年冬天，当第一场暴风雪封岛，你就开始犯病。睡不着，吃不下，整夜整夜坐在窗前听风声，总觉得海浪声里有人的喊叫。你知道那是幻觉——那年冬天你爹的船没回来，你站在码头听了三天三夜。镇上人说你是“潮汐症”，不吉利，躲着你走。你不怪他们。有些东西，离近了会传染。效果：任何玩家死亡，你会第一时间得到死亡玩家与死亡地点。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('8', '曾经是走私者的线人', '你知道岛上谁在夜里往岸上搬东西，知道哪条船不靠码头靠礁石，知道仓库哪面墙的锁是坏的。你替他们望风、递消息、在酒馆里吆喝“有巡警”的时候，他们就往你口袋里塞几个硬币。效果：你知道通往码头仓库与燃料仓库的暗道，可以避开巡逻人员和看守。也许这些东西对杀戮者或者反抗者很有用。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('9', '银币', '你有一枚银币。不是你赚的，不是你捡的，是——不知道从哪来的。你醒来的时候，它就攥在你手里，攥得掌心发红。你试过花掉，第二天它又回到你口袋；你试过扔掉，第二天它又出现在枕头底下；你试过送人，那人第二天发烧，银币又回来了。你不敢再试了。你怕的不是这枚银币，是它要你做什么。效果：银币不可丢弃、不可交易、不可偷窃。它“选择”留在你身边。全局，你可使用银币进行一次“许愿”。向主持人提出一个要求，如“明天生产+20%”“消除xxx的诅咒”“今日天灾牌无效”，主持人根据需要尽可能地满足要求，越是破坏平衡的愿望越可能带来强大的个人负面因素。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('10', '回声', '你能听见别人听不见的东西。不是耳朵好，是那种——不该听见的东西。酒馆里有人说悄悄话，你能听见；码头有人夜里密谋，你能听见；教堂地窖里神父自言自语，你也能听见。你不敢说。说了，你就是怪物。你把这些声音存在脑子里，像存钱。有一天，你要用它们换点什么。效果:在游戏开始时选择3名玩家你会得到他们全部的初始阵营信息。但不一一对应。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('11', '爱狗人', '你妈生你的时候难产死了，你爹喝了二十年酒，去年也死了。你吃百家饭长大。你认为狗比人好——狗不骗你，狗不嫌你，狗给你暖脚。你养了一只狗，它是你的重要伙伴。效果：初始拥有一只狗，可以给它取名字。它计算为2威胁值。狗不会被偷走，会一直跟着你。在有人试图偷盗你时会打断对方行为。', '平民', '2026-05-02 23:09:27', '2026-05-18 23:59:29');
INSERT INTO `skill` VALUES ('12', '海藻医生', '你奶奶教你的：海藻能止血，昆布能退烧，石莼能敷伤口。镇上人笑你是“海带大夫”，但冬天码头有人被缆绳打断腿的时候，第一个喊的是你。你采了一辈子海藻，尝了一辈子海藻，知道哪片礁石上的能治病，哪片的有毒。你不知道自己算不算医生，但你知道，没有你，这个冬天要多死几个人。效果：获得“医疗”技能', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('13', '百宝袋', '你兜里永远揣着各种各样的物品。效果：一次机会，你可以选择5个物品列表中的非武器的非重复物品，获得其标准单位数量的物品。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('14', '望远镜', '你有个军用望远镜，能看清远处。效果：每天1次，可私聊主持人“行动点x时使用特性望（xz地点）”，获知当时有谁在那里。无法获知矿场，避难所等地下结构。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('15', '海的呼唤', '你不对劲。不是生病，是那种——不该有的不对劲。上个月码头有条狗对着你狂吠，你瞪了它一眼，它夹着尾巴跑了，跑之前尿了一地。上周酒馆老张摸你肩膀，你碰了他一下，他回家后发烧三天。你自己也怕。你开始半夜往海边跑，对着月亮发呆，觉得海在叫你。你知道这不正常，但不知道这算什么。效果：你获得一个“诅咒标记”可以被布道去除。诅咒无法再影响你，你永远无法成为天灾使者的诅咒目标。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('16', '鱼骨占卜', '你奶奶传下来的手艺。不是那些神神叨叨的巫术——是把吃剩的鱼骨头洗干净，扔在桌上，看它们落地的样子。你说这能看出鱼汛、天气、谁家要死人。信的人当真，不信的人当笑话。但去年你说老李家的船别出海，他偏去了，没回来。那之后，找你的人多了，怕你的人也多了。效果：初始获得3个特殊鱼骨。可消耗“鱼骨”为他人占卜，需要对方同意。占卜的结果由主持人暗中判定。（为3选一随机不重复触发。对方的全部协议内容，对方的阵营信息，对方的特性信息）', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('17', '幸运贝壳', '你有一串穿孔的贝壳，说是祖上传下来保平安的。效果：拥有两颗幸运贝壳，一颗可以用来躲避一次伤害，一颗可以用来在没有食物时捡到2单位食物。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('18', '遗书', '你觉得世道不太平，早把后事安排好了。效果：每天可修改一次遗书私聊主持人，死后自动发给你指定的人或者公开。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('19', '单恋者', '你深爱着另一个人，你可以为他（她）付出一切。在游戏开始前选择一名玩家作为你的暗恋对象。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('20', '仇人', '你恨着一位统治者，你知晓隐藏统治者的身份。你憎恨着他也同时为自己准备了一条出路，你白天回合可以选择花费一个行动点进行“消失”。夜晚回合无法被调查，无法被攻击。但第二天你仍然会回到游戏之中。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('21', '扮演·大善', '你坚定地相信神的意志，认为神不会抛弃人们，你不愿意任何人受难和死去，并且诚心地愿望帮助他人。这是一个公开的扮演特性，请遵守善意的规律，它代表着一种话语权。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('22', '忠诚', '你的理念告诉你，当你选择了哪一条路，将不会存在背叛。一旦你加入了某个阵营就无法退出，同时你会给阵营带来意想不到的一定加成。统治者：你担任劳工或者守卫的时候，目标地点额外+2防御值。反抗者：你参与的“密谋”增加1点成功率。冒险者：你参与“方舟建设”时，当日总工作量提高10%。冒险者阵营“看守方舟”时防御值+1。天灾使者（若允许平民加入）：你的“诅咒”目标在被诅咒期间，无法对你发起任何攻击或密谋。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('23', '账房先生', '你算账清楚，记忆力好。效果：游戏开始时，你获得一份模糊的物资统计：知晓所有仓库的总资源排名（如“燃料最多的是燃料仓库，食物最多的是码头”），但不知具体数字和所属玩家。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('24', '老好人', '大家都不太防备你。效果：当你使用“调查玩家”时，如果目标玩家未进行“隐藏”，你不用投1/2概率，直接获知其阵营行动（但不包括具体内容，仅知“进行了生产”“进行了隐藏”等）。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('25', '酒蒙子', '你离不开酒。效果：游戏开始时额外获得20瓶朗姆酒。每天必须消耗2瓶朗姆酒，否则第二天白天行动点-1。（朗姆酒可交易）', '平民', '2026-05-02 23:09:27', '2026-05-22 16:41:45');
INSERT INTO `skill` VALUES ('26', '即兴演说', '你说话能打动人。效果：在“公开审判”中，如果你的发言被主持人认为有说服力，你可以在投票阶段额外增加/减少1票有效影响力（由主持人裁定）。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('27', '老水手', '你曾经出过海。效果：你获得“航海”技能。如果你已拥有，则获得“海洋导航”。在“出海”结算时，你为船只提供一次抗风暴加成（减少一次危机事件）。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('28', '梦魇', '你经常做噩梦，梦见暴雪。效果：每天晚上，你随机获知一张未被触发的天灾牌的名称（不触发效果）。同一天灾牌不会重复获知。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('29', '穷光蛋', '你什么资产都没有。效果：你初始资源减半（食物、燃料等基础配置减半）。但你不会被任何人调查出有价值的资产信息（调查你只能得到“一无所有”），也不会被“偷窃癖”偷走任何东西（因为你没有）。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('30', '守财奴', '你舍不得花。效果：你每天可以多保存25%的食物（每日消耗时按1.5食物计算。）代价：你无法主动赠送资源给其他玩家（必须交换）。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('31', '讲故事的人', '你脑子里有很多故事。效果：每天一次，你可以给去往的某地点某NPC讲一个故事。NPC心情变好后，可能会告诉你一条他“本来不想说”的无关紧要信息（如“昨天码头来了几个人”）。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('32', '好奇心重', '你什么都想知道。效果：你每天可以多问主持人一个“是/否”问题（关于游戏设定、地点、NPC，不涉及玩家阵营和行动）。主持人必须如实回答。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('33', '不满已久', '这些年忍够了。矿场、码头、粮仓……凭什么他们说了算？该换个活法了。效果：游戏开始时，你知晓反抗者阵营其中一个成员的名字。如果你成功加入反抗者阵营，你获得一个一次性效果：在反抗者发起的第一次密谋中，你增加2点成功率（而不是常规的1点）。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('34', '渴望出海', '这岛要完了，但我不想死在这儿。跟着大船走，哪怕当个水手也行。游戏开始时，你知晓冒险者阵营两个成员的名字。如果你成功加入冒险者阵营，你获得一个一次性效果：在“方舟建设”行动中，你可以选择一次贡献量翻倍（一天算两天）。', '平民', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('35', '虚伪的仁慈', '如果主持人告知你其他玩家粮食不足，你必须提供维持对方一天的食物需求。如果对方接受了你的救助，你可以控制对方第二天的一个行动点去向。对方只会知道“受特性影响该行动点无效”。', '统治者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('36', '暴君', '你可以每天额外压榨一名劳工。', '统治者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('37', '铁腕', '效果：你主持的“公开审判”中，投票环节你的个人票数算作3票（而不是1票）。', '统治者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('38', '恩主', '效果：每天一次，你可以免除一名劳工的“过劳”状态（他今天劳作后不会积累过劳标记）。', '统治者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('39', '窃听者', '效果：每天夜晚阶段，你可以选择一条非统治者阵营的私人群组（如反抗者群、冒险者群），主持人随机抽取该群当天的一段对话（不超过5条）匿名发给你。你不会知道说话者是谁。', '统治者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('40', '暗桩', '效果：游戏开始时，你秘密选择一名NPC（非玩家）作为你的“暗桩”。你可以每天向主持人询问一次“暗桩报告”。包括有哪些人来过他所在的地方，和什么NPC进行了交互。', '统治者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('41', '镇守', '效果：当你亲自在某地点过夜（夜晚阶段结束时你位于该地点），该地点的防御值+3。', '统治者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('42', '点名', '每天开始前，你可以宣布一个玩家的名字。该玩家当天无法进行“隐藏”行动（如果已经隐藏，则自动暴露）。', '统治者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('43', '藏料', '效果：游戏开始时，你在码头秘密藏了额外资源：木材20吨、金属制品5吨、密封材料10kg。这些资源只有你知道位置，不会被其他阵营搜刮。', '冒险者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('44', '号召', '效果：每天一次，你可以向一名平民玩家发出“上船邀请”。如果对方同意临时加入冒险者阵营，对方获得一次免费“建设方舟”行动。', '冒险者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('45', '探风', '效果：每天一次，你可以询问主持人“今天是否有玩家在码头或海边活动”。主持人回答“有人”或“没人”，不透露具体是谁。', '冒险者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('46', '听潮', '效果：每天夜晚阶段结束时，你可以询问主持人“今晚是否有玩家在码头进行了非公开行动（如密谋、隐藏、偷盗）”。主持人回答“是”或“否”。', '冒险者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('47', '备材', '效果：全局一次，当你的阵营在进行“方舟建设”时缺少某一种资源（如缺木材、缺金属），你可以使用“备材”。主持人会告知你岛上哪个地点（不精确到NPC）可以找到该资源的下落（如“矿场仓库有金属”或“伐木营地有木材”）。', '冒险者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('48', '同路人', '效果：每天一次，你可以询问主持人“今天是否有平民玩家加入了某个阵营（你可以指定一个阵营，如反抗者）”。主持人回答“有”或“没有”。', '冒险者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('49', '摸底', '效果：每天一次，选择一名玩家。主持人私聊告知你：该玩家今天是否进行了“生产”行动，以及产出是否上交给了统治者（“上交了”“没上交”）。', '反叛者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('50', '硬骨头', '效果：全局一次，如果你被统治者选为劳工，你可以选择“反抗”。当天你不会被计入劳工名单，统治者会得知你未能成功被征为劳工。', '反叛者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('51', '正义宣传', '效果：每天一次，当统治者宣布劳工名单后，你可以选择其中一名劳工。该劳工当天建造值-50%（向下取整）。统治者不会自动知道是谁干的，但可以调查或者排除出来。', '反叛者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('52', '老面孔', '效果：游戏开始时，你随机知晓一名监狱/关押地点看守NPC的名字和弱点（如“他晚上会喝酒”“他贪财”）。你可以利用这个信息，在劫狱时让该看守“暂时离开岗位”（需消耗一次行动点），使目标地点的防御值-2。', '反叛者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('53', '舆论', '效果：每天一次，你可以通过主持人在公屏发布一条“民间声音”（如“矿工们今天不想干活”“码头有人在议论镇长”不超过20个字。）。这条信息会被视为“NPC的态度”，统治者如果无视，可能影响部分NPC对他们的态度。', '反叛者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('54', '集结号', '效果：当劫狱和公开审判两个里程碑都完成后，你可以在劳工傍晚回合宣布“起义”。起义当天，所有反抗者阵营成员的“威胁值”+1（不限武器）。', '反叛者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('55', '瘟疫', '效果：你将永远拥有“瘟疫诅咒”。每天一次，你可以对一名玩家施加“瘟疫诅咒”。该玩家不会立即死亡，但会在终局结算时产生以下影响：如果该玩家在避难所中，避难所的食物消耗增加5%（因为他污染了存粮）；如果该玩家在方舟上，方舟的可能的医疗资源需求+50%（因为他传染他人）；如果该玩家死亡，“瘟疫诅咒”会随机传染给另一名存活玩家（终局结算时生效）。多个“瘟疫诅咒”效果叠加。被诅咒的玩家不会知道，但会感到“身体不适”。', '天灾使者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('56', '战争', '效果：全局一次，当你参与一场“冲突，起义，劫狱”非自身阵营引发对抗和暴力的行动时，你可以宣布“战争降临”。该场冲突中，所有参与者的威胁值翻倍（包括敌我），且冲突结束后，无论胜负，所有参与者都会获得一个“受伤”标记。之后战争的阴影也笼罩在人们心上，触发全局效果“战争”。如果你本人在冲突中死亡，“战争”效果依然持续到冲突结束。', '天灾使者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('57', '饥荒', '效果：全局一次，你可以花费一个行动点“饥荒降临”。选择一种资源类型（食物、燃料、木材、金属制品、医疗资源），主持人从所有仓库（4个统治者仓库、反抗者、冒险者）中随机两个仓库销毁该资源的10%（最低保留1单位）。此特性使用后，所有玩家会收到公屏提示：“一场诡异的霉病席卷了粮仓/燃料/仓库……”，但不知道是谁干的。', '天灾使者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('58', '死亡骑士', '触发条件：当天灾使者阵营只剩最后一名存活成员时，若有天灾使者挂机第一天第二天都挂机也认为他满足死亡条件。玩家可以在当天任意阶段（白天/夜晚/结算前）私信主持人，选择是否“成为死亡骑士”。若没有回答默认拒绝。拒绝：无事发生，游戏继续。同意：全屏公告——“天灾的低语响彻岛屿：有人接过了死亡骑士的缰绳。”效果：你可以选择以下两个效果之一：如果游戏进入终局结算（暴雪将至），你可以暗中联系主持人“终焉宣告”。选择以下效果之一：对避难所：避难所的所有防御值和资源储备在结算时视为80%（食物、燃料只计算80%）；对方舟：方舟在出海结算时额外遭遇2次危机事件。此效果在终局结算时自动生效，无需额外行动。如果你在终局前死亡，此效果依然生效（你已成为死亡骑士，诅咒已落下）。', '天灾使者', '2026-05-02 23:09:27', '2026-05-02 23:09:27');
INSERT INTO `skill` VALUES ('59', '聆讯者', '效果：受到了天灾的启示因此知道了很多外人不知道的信息，你不能在第1，2天加入任何阵营。不得加入杀戮者阵营。', '平民', '2026-05-22 16:49:49', '2026-05-22 16:49:49');
INSERT INTO `skill` VALUES ('60', '神明的眼睛（公开）', '神想知道这场游戏祂能得到什么乐子，当你知道12人（不包括自己）的阵营时。神明会给予你特殊的奖赏。\r\n每天可以提交最多两次名单，会得到错误几个的提示。\r\n利用神明的恩赐活下去。', '平民', '2026-05-22 16:50:43', '2026-05-22 16:50:43');

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
) ENGINE=InnoDB AUTO_INCREMENT=105 DEFAULT CHARSET=utf8mb4 COMMENT='交易主表';

-- ----------------------------
-- Records of trade
-- ----------------------------
INSERT INTO `trade` VALUES ('14', '29', '16', 'completed', '如果可以的话，把我的名字刻在枪的护木上吧', '2026-05-22 19:24:05', '2026-05-22 19:33:43');
INSERT INTO `trade` VALUES ('15', '27', '24', 'completed', '', '2026-05-22 19:24:07', '2026-05-22 19:52:15');
INSERT INTO `trade` VALUES ('16', '27', '24', 'completed', '', '2026-05-22 19:24:36', '2026-05-22 19:52:12');
INSERT INTO `trade` VALUES ('17', '24', '20', 'completed', '定金', '2026-05-22 19:56:33', '2026-05-22 19:57:48');
INSERT INTO `trade` VALUES ('18', '20', '16', 'completed', '', '2026-05-22 19:59:59', '2026-05-22 20:01:34');
INSERT INTO `trade` VALUES ('19', '20', '16', 'completed', '', '2026-05-22 20:01:00', '2026-05-22 20:01:32');
INSERT INTO `trade` VALUES ('20', '13', '27', 'completed', '', '2026-05-22 20:03:49', '2026-05-22 20:09:41');
INSERT INTO `trade` VALUES ('21', '24', '8', 'completed', '愿善良传递下去', '2026-05-22 20:22:27', '2026-05-22 20:53:06');
INSERT INTO `trade` VALUES ('22', '20', '16', 'completed', '', '2026-05-22 20:42:46', '2026-05-22 20:45:41');
INSERT INTO `trade` VALUES ('23', '16', '30', 'pending', '', '2026-05-22 20:53:46', '2026-05-22 20:53:46');
INSERT INTO `trade` VALUES ('24', '16', '30', 'pending', '', '2026-05-22 20:53:49', '2026-05-22 20:53:49');
INSERT INTO `trade` VALUES ('25', '24', '30', 'completed', '', '2026-05-22 21:23:53', '2026-05-22 21:50:23');
INSERT INTO `trade` VALUES ('26', '32', '24', 'completed', '', '2026-05-22 21:28:38', '2026-05-22 21:32:25');
INSERT INTO `trade` VALUES ('27', '11', '24', 'completed', '', '2026-05-22 21:38:40', '2026-05-22 21:41:30');
INSERT INTO `trade` VALUES ('28', '23', '24', 'completed', '', '2026-05-22 21:41:14', '2026-05-22 21:41:29');
INSERT INTO `trade` VALUES ('29', '26', '27', 'rejected', '', '2026-05-22 22:16:17', '2026-05-22 22:21:24');
INSERT INTO `trade` VALUES ('30', '16', '14', 'completed', '', '2026-05-22 22:46:05', '2026-05-22 22:48:19');
INSERT INTO `trade` VALUES ('31', '24', '20', 'completed', '弓好了', '2026-05-22 22:56:40', '2026-05-22 23:06:53');
INSERT INTO `trade` VALUES ('32', '10', '17', 'completed', '', '2026-05-22 22:58:28', '2026-05-22 22:58:50');
INSERT INTO `trade` VALUES ('33', '20', '16', 'completed', '', '2026-05-22 23:09:29', '2026-05-22 23:09:52');
INSERT INTO `trade` VALUES ('34', '24', '9', 'completed', '', '2026-05-22 23:26:54', '2026-05-23 00:43:36');
INSERT INTO `trade` VALUES ('35', '24', '31', 'completed', '', '2026-05-22 23:34:13', '2026-05-22 23:35:47');
INSERT INTO `trade` VALUES ('36', '26', '10', 'completed', '', '2026-05-23 00:23:40', '2026-05-23 00:24:18');
INSERT INTO `trade` VALUES ('37', '10', '26', 'completed', '', '2026-05-23 00:24:12', '2026-05-23 19:27:08');
INSERT INTO `trade` VALUES ('38', '15', '26', 'completed', '', '2026-05-23 00:24:34', '2026-05-23 19:27:02');
INSERT INTO `trade` VALUES ('39', '9', '27', 'completed', '', '2026-05-23 00:44:59', '2026-05-23 00:45:32');
INSERT INTO `trade` VALUES ('40', '13', '32', 'completed', '', '2026-05-23 01:30:51', '2026-05-23 01:31:36');
INSERT INTO `trade` VALUES ('41', '9', '27', 'completed', '', '2026-05-23 01:37:37', '2026-05-23 01:40:44');
INSERT INTO `trade` VALUES ('42', '9', '27', 'completed', '', '2026-05-23 01:37:51', '2026-05-23 01:40:42');
INSERT INTO `trade` VALUES ('43', '9', '27', 'completed', '', '2026-05-23 01:40:05', '2026-05-23 01:40:34');
INSERT INTO `trade` VALUES ('44', '25', '10', 'completed', '', '2026-05-23 11:26:51', '2026-05-23 15:55:24');
INSERT INTO `trade` VALUES ('45', '16', '20', 'completed', '', '2026-05-23 14:37:20', '2026-05-23 14:41:31');
INSERT INTO `trade` VALUES ('46', '16', '20', 'completed', '', '2026-05-23 14:39:26', '2026-05-23 14:41:26');
INSERT INTO `trade` VALUES ('47', '8', '16', 'completed', '给你咧', '2026-05-23 15:34:01', '2026-05-23 15:34:06');
INSERT INTO `trade` VALUES ('48', '22', '16', 'completed', '', '2026-05-23 16:57:02', '2026-05-23 16:57:33');
INSERT INTO `trade` VALUES ('49', '9', '14', 'completed', '', '2026-05-23 19:08:18', '2026-05-23 19:41:44');
INSERT INTO `trade` VALUES ('50', '9', '22', 'completed', '', '2026-05-23 19:15:13', '2026-05-23 19:17:13');
INSERT INTO `trade` VALUES ('51', '9', '21', 'completed', '', '2026-05-23 19:16:00', '2026-05-23 19:36:24');
INSERT INTO `trade` VALUES ('52', '9', '19', 'completed', '', '2026-05-23 19:16:28', '2026-05-23 19:17:06');
INSERT INTO `trade` VALUES ('53', '19', '9', 'completed', '', '2026-05-23 19:18:01', '2026-05-23 19:18:09');
INSERT INTO `trade` VALUES ('54', '25', '15', 'completed', '', '2026-05-23 19:34:34', '2026-05-23 19:45:34');
INSERT INTO `trade` VALUES ('55', '19', '21', 'completed', '', '2026-05-23 19:36:30', '2026-05-23 19:38:10');
INSERT INTO `trade` VALUES ('56', '13', '24', 'completed', '', '2026-05-23 19:40:10', '2026-05-23 19:46:48');
INSERT INTO `trade` VALUES ('57', '29', '30', 'completed', '拿着这些去救下更多的人，不要被你所在的阵营框定，去救更多的人', '2026-05-23 19:41:09', '2026-05-23 19:43:40');
INSERT INTO `trade` VALUES ('58', '24', '8', 'completed', '', '2026-05-23 19:47:32', '2026-05-23 20:36:29');
INSERT INTO `trade` VALUES ('59', '24', '11', 'completed', '', '2026-05-23 19:55:09', '2026-05-23 20:10:04');
INSERT INTO `trade` VALUES ('60', '24', '25', 'completed', '', '2026-05-23 19:55:44', '2026-05-23 19:55:55');
INSERT INTO `trade` VALUES ('61', '30', '25', 'rejected', '康康有没有什么问题', '2026-05-23 19:59:00', '2026-05-23 20:01:13');
INSERT INTO `trade` VALUES ('62', '25', '30', 'completed', '', '2026-05-23 20:01:33', '2026-05-23 20:01:53');
INSERT INTO `trade` VALUES ('63', '25', '10', 'completed', '', '2026-05-23 20:02:20', '2026-05-23 20:03:40');
INSERT INTO `trade` VALUES ('64', '25', '10', 'completed', '', '2026-05-23 20:02:43', '2026-05-23 20:03:42');
INSERT INTO `trade` VALUES ('65', '10', '26', 'completed', '', '2026-05-23 20:09:46', '2026-05-23 20:13:05');
INSERT INTO `trade` VALUES ('66', '26', '9', 'completed', '', '2026-05-23 20:12:47', '2026-05-23 20:12:58');
INSERT INTO `trade` VALUES ('67', '25', '23', 'completed', '', '2026-05-23 20:25:05', '2026-05-23 20:25:34');
INSERT INTO `trade` VALUES ('68', '9', '21', 'completed', '', '2026-05-23 21:04:17', '2026-05-23 21:04:27');
INSERT INTO `trade` VALUES ('69', '14', '21', 'completed', '', '2026-05-23 21:12:20', '2026-05-23 21:30:45');
INSERT INTO `trade` VALUES ('70', '24', '8', 'completed', '', '2026-05-23 21:49:06', '2026-05-23 21:49:47');
INSERT INTO `trade` VALUES ('71', '8', '16', 'completed', '', '2026-05-23 21:51:00', '2026-05-23 21:51:54');
INSERT INTO `trade` VALUES ('72', '14', '21', 'completed', '', '2026-05-23 23:23:45', '2026-05-23 23:24:27');
INSERT INTO `trade` VALUES ('73', '9', '22', 'completed', '', '2026-05-23 23:24:59', '2026-05-23 23:25:17');
INSERT INTO `trade` VALUES ('74', '14', '21', 'completed', '', '2026-05-23 23:40:08', '2026-05-23 23:41:00');
INSERT INTO `trade` VALUES ('75', '19', '21', 'completed', '', '2026-05-23 23:42:37', '2026-05-24 01:03:22');
INSERT INTO `trade` VALUES ('76', '10', '20', 'completed', '', '2026-05-24 12:33:52', '2026-05-24 15:31:27');
INSERT INTO `trade` VALUES ('77', '10', '20', 'completed', '', '2026-05-24 13:39:28', '2026-05-24 15:31:25');
INSERT INTO `trade` VALUES ('78', '10', '14', 'completed', '', '2026-05-24 14:05:33', '2026-05-24 14:56:00');
INSERT INTO `trade` VALUES ('79', '10', '14', 'completed', '', '2026-05-24 14:06:05', '2026-05-24 14:55:55');
INSERT INTO `trade` VALUES ('80', '16', '20', 'completed', '', '2026-05-24 14:43:54', '2026-05-24 15:31:18');
INSERT INTO `trade` VALUES ('81', '16', '30', 'completed', '', '2026-05-24 14:44:11', '2026-05-24 14:48:58');
INSERT INTO `trade` VALUES ('82', '20', '16', 'completed', '', '2026-05-24 18:08:15', '2026-05-24 18:08:33');
INSERT INTO `trade` VALUES ('83', '9', '19', 'completed', '', '2026-05-24 18:19:47', '2026-05-24 18:23:07');
INSERT INTO `trade` VALUES ('84', '14', '19', 'completed', '', '2026-05-24 18:28:16', '2026-05-24 18:30:39');
INSERT INTO `trade` VALUES ('85', '19', '21', 'completed', '', '2026-05-24 19:07:05', '2026-05-24 19:12:07');
INSERT INTO `trade` VALUES ('86', '14', '19', 'completed', '', '2026-05-24 19:09:46', '2026-05-24 19:10:00');
INSERT INTO `trade` VALUES ('87', '9', '14', 'completed', '', '2026-05-24 19:14:26', '2026-05-24 19:14:55');
INSERT INTO `trade` VALUES ('88', '16', '20', 'completed', '', '2026-05-24 19:15:56', '2026-05-24 19:16:45');
INSERT INTO `trade` VALUES ('89', '19', '9', 'completed', '', '2026-05-24 19:21:42', '2026-05-24 19:23:59');
INSERT INTO `trade` VALUES ('90', '10', '26', 'rejected', '', '2026-05-24 20:01:52', '2026-05-24 20:07:17');
INSERT INTO `trade` VALUES ('91', '10', '17', 'completed', '', '2026-05-24 20:15:12', '2026-05-24 20:16:39');
INSERT INTO `trade` VALUES ('92', '24', '23', 'completed', '', '2026-05-24 20:33:57', '2026-05-24 20:43:17');
INSERT INTO `trade` VALUES ('93', '16', '20', 'completed', '', '2026-05-24 20:35:16', '2026-05-24 21:22:44');
INSERT INTO `trade` VALUES ('94', '24', '11', 'completed', '', '2026-05-24 20:37:46', '2026-05-24 20:42:55');
INSERT INTO `trade` VALUES ('95', '10', '26', 'completed', '', '2026-05-24 20:59:18', '2026-05-24 21:00:15');
INSERT INTO `trade` VALUES ('96', '11', '24', 'completed', '', '2026-05-24 20:59:52', '2026-05-24 21:00:21');
INSERT INTO `trade` VALUES ('97', '21', '9', 'completed', '', '2026-05-24 21:09:11', '2026-05-24 21:15:49');
INSERT INTO `trade` VALUES ('98', '15', '17', 'completed', '', '2026-05-24 21:09:41', '2026-05-24 21:49:19');
INSERT INTO `trade` VALUES ('99', '15', '10', 'completed', '', '2026-05-24 21:13:52', '2026-05-24 21:15:27');
INSERT INTO `trade` VALUES ('100', '26', '10', 'completed', '', '2026-05-24 21:22:30', '2026-05-24 21:22:38');
INSERT INTO `trade` VALUES ('101', '24', '9', 'completed', '', '2026-05-24 22:14:48', '2026-05-24 22:16:01');
INSERT INTO `trade` VALUES ('102', '9', '24', 'completed', '', '2026-05-24 22:15:35', '2026-05-24 22:19:55');
INSERT INTO `trade` VALUES ('103', '26', '17', 'completed', '', '2026-05-24 22:26:59', '2026-05-24 22:27:11');
INSERT INTO `trade` VALUES ('104', '17', '26', 'rejected', '', '2026-05-24 22:27:05', '2026-05-24 22:27:35');

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
  `item_key` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_trade_id` (`trade_id`),
  KEY `idx_item` (`item_type`,`item_id`),
  CONSTRAINT `trade_items_ibfk_1` FOREIGN KEY (`trade_id`) REFERENCES `trade` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=197 DEFAULT CHARSET=utf8mb4 COMMENT='交易物品明细表';

-- ----------------------------
-- Records of trade_items
-- ----------------------------
INSERT INTO `trade_items` VALUES ('21', '14', 'material', '1', '5', 'give', '2026-05-22 19:24:05', '金属制品', '个', null);
INSERT INTO `trade_items` VALUES ('22', '14', 'material', '2', '20', 'give', '2026-05-22 19:24:05', '木材', '个', null);
INSERT INTO `trade_items` VALUES ('23', '14', 'weapon', '2', '1', 'take', '2026-05-22 19:24:05', '猎枪', '个', null);
INSERT INTO `trade_items` VALUES ('24', '15', 'material', '5', '5', 'give', '2026-05-22 19:24:07', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('25', '15', 'material', '5', '1', 'take', '2026-05-22 19:24:07', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('26', '16', 'material', '5', '5', 'give', '2026-05-22 19:24:36', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('27', '16', 'material', '5', '1', 'take', '2026-05-22 19:24:36', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('28', '17', 'material', '5', '2', 'give', '2026-05-22 19:56:33', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('29', '18', 'material', '1', '10', 'give', '2026-05-22 19:59:59', '金属制品', '个', null);
INSERT INTO `trade_items` VALUES ('30', '19', 'material', '2', '30', 'give', '2026-05-22 20:01:00', '木材', '个', null);
INSERT INTO `trade_items` VALUES ('31', '20', 'material', '2', '1', 'give', '2026-05-22 20:03:49', '木材', '个', null);
INSERT INTO `trade_items` VALUES ('32', '20', 'weapon', '2', '1', 'take', '2026-05-22 20:03:49', '猎枪', '个', null);
INSERT INTO `trade_items` VALUES ('33', '20', 'ammo', '2', '2', 'take', '2026-05-22 20:03:49', '猎枪弹', '个', null);
INSERT INTO `trade_items` VALUES ('34', '21', 'material', '5', '2', 'give', '2026-05-22 20:22:27', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('35', '22', 'material', '2', '20', 'give', '2026-05-22 20:42:46', '木材', '个', null);
INSERT INTO `trade_items` VALUES ('36', '22', 'material', '3', '10', 'give', '2026-05-22 20:42:46', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('37', '23', 'material', '2', '3', 'take', '2026-05-22 20:53:46', null, null, null);
INSERT INTO `trade_items` VALUES ('38', '23', 'material', '1', '7', 'take', '2026-05-22 20:53:46', null, null, null);
INSERT INTO `trade_items` VALUES ('39', '24', 'material', '2', '3', 'take', '2026-05-22 20:53:49', null, null, null);
INSERT INTO `trade_items` VALUES ('40', '24', 'material', '1', '7', 'take', '2026-05-22 20:53:49', null, null, null);
INSERT INTO `trade_items` VALUES ('41', '25', 'material', '5', '2', 'give', '2026-05-22 21:23:53', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('42', '26', 'material', '5', '2', 'take', '2026-05-22 21:28:38', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('43', '27', 'material', '8', '3', 'give', '2026-05-22 21:38:40', '燃料', 'kg', null);
INSERT INTO `trade_items` VALUES ('44', '28', 'material', '8', '2', 'give', '2026-05-22 21:41:14', '燃料', 'kg', null);
INSERT INTO `trade_items` VALUES ('45', '29', 'ammo', '3', '1', 'give', '2026-05-22 22:16:17', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('46', '29', 'material', '5', '2', 'take', '2026-05-22 22:16:17', null, null, null);
INSERT INTO `trade_items` VALUES ('47', '29', 'material', '8', '15', 'take', '2026-05-22 22:16:17', null, null, null);
INSERT INTO `trade_items` VALUES ('48', '30', 'material', '3', '30', 'give', '2026-05-22 22:46:05', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('49', '30', 'material', '9', '10', 'give', '2026-05-22 22:46:05', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('50', '30', 'material', '1', '7', 'take', '2026-05-22 22:46:05', '金属制品', '个', null);
INSERT INTO `trade_items` VALUES ('51', '31', 'material', '2', '10', 'give', '2026-05-22 22:56:40', '木材', '个', null);
INSERT INTO `trade_items` VALUES ('52', '32', 'material', '5', '2', 'give', '2026-05-22 22:58:28', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('53', '32', 'item', '8', '1', 'take', '2026-05-22 22:58:28', '维修工具包', '个', null);
INSERT INTO `trade_items` VALUES ('54', '33', 'material', '2', '10', 'give', '2026-05-22 23:09:29', '木材', '个', null);
INSERT INTO `trade_items` VALUES ('55', '34', 'material', '5', '2', 'give', '2026-05-22 23:26:54', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('56', '35', 'material', '5', '2', 'give', '2026-05-22 23:34:13', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('57', '36', 'ammo', '3', '1', 'give', '2026-05-23 00:23:40', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('58', '36', 'material', '5', '2', 'take', '2026-05-23 00:23:40', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('59', '36', 'material', '8', '1', 'take', '2026-05-23 00:23:40', '燃料', 'kg', null);
INSERT INTO `trade_items` VALUES ('60', '37', 'material', '5', '2', 'give', '2026-05-23 00:24:12', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('61', '37', 'material', '8', '1', 'give', '2026-05-23 00:24:12', '燃料', 'kg', null);
INSERT INTO `trade_items` VALUES ('62', '37', 'item', '4', '1', 'take', '2026-05-23 00:24:12', '哨子', '个', null);
INSERT INTO `trade_items` VALUES ('63', '38', 'material', '8', '5', 'give', '2026-05-23 00:24:34', '燃料', 'kg', null);
INSERT INTO `trade_items` VALUES ('64', '39', 'material', '6', '30', 'take', '2026-05-23 00:44:59', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('65', '40', 'material', '8', '5', 'give', '2026-05-23 01:30:51', '燃料', 'kg', null);
INSERT INTO `trade_items` VALUES ('66', '40', 'material', '5', '2', 'give', '2026-05-23 01:30:51', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('67', '40', 'material', '1', '5', 'take', '2026-05-23 01:30:51', '金属制品', '个', null);
INSERT INTO `trade_items` VALUES ('68', '41', 'weapon', '8', '2', 'take', '2026-05-23 01:37:37', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('69', '41', 'material', '2', '500', 'take', '2026-05-23 01:37:37', '木材', '个', null);
INSERT INTO `trade_items` VALUES ('70', '41', 'material', '5', '10', 'take', '2026-05-23 01:37:37', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('71', '42', 'weapon', '2', '1', 'take', '2026-05-23 01:37:51', '猎枪', '个', null);
INSERT INTO `trade_items` VALUES ('72', '43', 'material', '9', '30', 'take', '2026-05-23 01:40:05', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('73', '43', 'material', '3', '100', 'take', '2026-05-23 01:40:05', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('74', '43', 'material', '1', '30', 'take', '2026-05-23 01:40:05', '金属制品', '个', null);
INSERT INTO `trade_items` VALUES ('75', '43', 'ammo', '2', '2', 'take', '2026-05-23 01:40:05', '猎枪弹', '个', null);
INSERT INTO `trade_items` VALUES ('76', '43', 'weapon', '9', '2', 'take', '2026-05-23 01:40:05', '斧头', '个', null);
INSERT INTO `trade_items` VALUES ('77', '44', 'item', '18', '2', 'give', '2026-05-23 11:26:51', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('78', '44', 'material', '5', '1', 'take', '2026-05-23 11:26:51', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('79', '45', 'weapon', '7', '1', 'give', '2026-05-23 14:37:20', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('80', '46', 'item', '6', '1', 'give', '2026-05-23 14:39:26', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('81', '47', 'material', '1', '10', 'give', '2026-05-23 15:34:01', '金属制品', '个', null);
INSERT INTO `trade_items` VALUES ('82', '48', 'item', '12', '1', 'give', '2026-05-23 16:57:02', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('83', '48', 'weapon', '4', '1', 'give', '2026-05-23 16:57:02', '刺刀', '个', null);
INSERT INTO `trade_items` VALUES ('84', '48', 'material', '2', '65', 'give', '2026-05-23 16:57:02', '木材', '个', null);
INSERT INTO `trade_items` VALUES ('85', '48', 'material', '3', '20', 'give', '2026-05-23 16:57:02', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('86', '48', 'material', '5', '9', 'give', '2026-05-23 16:57:02', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('87', '48', 'material', '8', '9', 'give', '2026-05-23 16:57:02', '燃料', 'kg', null);
INSERT INTO `trade_items` VALUES ('88', '49', 'ammo', '2', '2', 'give', '2026-05-23 19:08:18', '猎枪弹', '个', null);
INSERT INTO `trade_items` VALUES ('89', '49', 'weapon', '2', '1', 'give', '2026-05-23 19:08:18', '猎枪', '个', null);
INSERT INTO `trade_items` VALUES ('90', '50', 'weapon', '8', '1', 'give', '2026-05-23 19:15:13', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('91', '51', 'weapon', '8', '1', 'give', '2026-05-23 19:16:00', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('92', '52', 'weapon', '9', '1', 'give', '2026-05-23 19:16:28', '斧头', '个', null);
INSERT INTO `trade_items` VALUES ('93', '53', 'weapon', '4', '1', 'give', '2026-05-23 19:18:01', '刺刀', '个', null);
INSERT INTO `trade_items` VALUES ('94', '54', 'material', '5', '4', 'give', '2026-05-23 19:34:34', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('95', '55', 'material', '5', '4', 'give', '2026-05-23 19:36:30', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('96', '56', 'material', '1', '2', 'give', '2026-05-23 19:40:10', '金属制品', '个', null);
INSERT INTO `trade_items` VALUES ('97', '57', 'item', '14', '10', 'give', '2026-05-23 19:41:09', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('98', '58', 'material', '5', '2', 'give', '2026-05-23 19:47:32', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('99', '58', 'material', '1', '2', 'take', '2026-05-23 19:47:32', '金属制品', '个', null);
INSERT INTO `trade_items` VALUES ('100', '59', 'material', '5', '2', 'give', '2026-05-23 19:55:09', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('101', '60', 'material', '5', '20', 'give', '2026-05-23 19:55:44', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('102', '61', 'material', '2', '30', 'give', '2026-05-23 19:59:00', '木材', '个', null);
INSERT INTO `trade_items` VALUES ('103', '61', 'item', '18', '4', 'take', '2026-05-23 19:59:00', null, null, null);
INSERT INTO `trade_items` VALUES ('104', '62', 'item', '18', '3', 'give', '2026-05-23 20:01:33', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('105', '62', 'material', '2', '30', 'take', '2026-05-23 20:01:33', '木材', '个', null);
INSERT INTO `trade_items` VALUES ('106', '63', 'material', '5', '10', 'take', '2026-05-23 20:02:20', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('107', '63', 'material', '2', '15', 'take', '2026-05-23 20:02:20', '木材', '个', null);
INSERT INTO `trade_items` VALUES ('108', '64', 'material', '2', '15', 'take', '2026-05-23 20:02:43', '木材', '个', null);
INSERT INTO `trade_items` VALUES ('109', '64', 'material', '5', '10', 'take', '2026-05-23 20:02:43', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('110', '65', 'item', '18', '1', 'give', '2026-05-23 20:09:46', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('111', '65', 'item', '7', '1', 'take', '2026-05-23 20:09:46', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('112', '66', 'item', '20', '1', 'give', '2026-05-23 20:12:47', '燃料仓库钥匙', '个', null);
INSERT INTO `trade_items` VALUES ('113', '66', 'item', '10', '1', 'take', '2026-05-23 20:12:47', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('114', '67', 'material', '5', '1', 'give', '2026-05-23 20:25:05', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('115', '68', 'material', '6', '30', 'give', '2026-05-23 21:04:17', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('116', '68', 'material', '1', '40', 'give', '2026-05-23 21:04:17', '金属制品', '个', null);
INSERT INTO `trade_items` VALUES ('117', '69', 'material', '2', '80', 'give', '2026-05-23 21:12:20', '木材', '个', null);
INSERT INTO `trade_items` VALUES ('118', '70', 'material', '1', '4', 'give', '2026-05-23 21:49:07', '金属制品', '个', null);
INSERT INTO `trade_items` VALUES ('119', '71', 'material', '1', '2', 'give', '2026-05-23 21:51:00', '金属制品', '个', null);
INSERT INTO `trade_items` VALUES ('120', '72', 'weapon', '4', '1', 'give', '2026-05-23 23:23:45', '刺刀', '个', null);
INSERT INTO `trade_items` VALUES ('121', '73', 'weapon', '4', '1', 'give', '2026-05-23 23:24:59', '刺刀', '个', null);
INSERT INTO `trade_items` VALUES ('122', '74', 'material', '6', '20', 'give', '2026-05-23 23:40:08', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('123', '75', 'material', '10', '1', 'give', '2026-05-23 23:42:37', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('124', '75', 'material', '11', '1', 'give', '2026-05-23 23:42:37', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('125', '76', 'ammo', '4', '2', 'give', '2026-05-24 12:33:52', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('126', '76', 'material', '5', '1', 'take', '2026-05-24 12:33:52', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('127', '77', 'item', '6', '1', 'give', '2026-05-24 13:39:28', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('128', '77', 'weapon', '8', '1', 'take', '2026-05-24 13:39:28', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('129', '78', 'item', '17', '1', 'give', '2026-05-24 14:05:33', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('130', '78', 'material', '11', '3', 'give', '2026-05-24 14:05:33', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('131', '78', 'material', '5', '2', 'take', '2026-05-24 14:05:33', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('132', '79', 'material', '6', '50', 'give', '2026-05-24 14:06:05', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('133', '79', 'material', '5', '1', 'take', '2026-05-24 14:06:05', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('134', '80', 'weapon', '4', '1', 'give', '2026-05-24 14:43:54', '刺刀', '个', null);
INSERT INTO `trade_items` VALUES ('135', '81', 'weapon', '4', '1', 'give', '2026-05-24 14:44:11', '刺刀', '个', null);
INSERT INTO `trade_items` VALUES ('136', '82', 'item', '18', '1', 'give', '2026-05-24 18:08:15', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('137', '83', 'item', '18', '1', 'give', '2026-05-24 18:19:47', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('138', '84', 'material', '6', '50', 'give', '2026-05-24 18:28:16', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('139', '85', 'material', '6', '20', 'give', '2026-05-24 19:07:05', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('140', '86', 'material', '11', '3', 'give', '2026-05-24 19:09:46', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('141', '86', 'material', '9', '10', 'give', '2026-05-24 19:09:46', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('142', '86', 'material', '3', '40', 'give', '2026-05-24 19:09:46', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('143', '87', 'material', '5', '2', 'give', '2026-05-24 19:14:26', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('144', '88', 'item', '18', '1', 'give', '2026-05-24 19:15:56', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('145', '89', 'material', '9', '10', 'give', '2026-05-24 19:21:42', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('146', '89', 'material', '3', '60', 'give', '2026-05-24 19:21:42', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('147', '90', 'item', '18', '1', 'give', '2026-05-24 20:01:52', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('148', '90', 'material', '5', '1', 'take', '2026-05-24 20:01:52', null, null, null);
INSERT INTO `trade_items` VALUES ('149', '91', 'item', '18', '1', 'give', '2026-05-24 20:15:12', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('150', '91', 'material', '5', '1', 'take', '2026-05-24 20:15:12', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('151', '92', 'material', '5', '2', 'give', '2026-05-24 20:33:57', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('152', '93', 'item', '2', '1', 'give', '2026-05-24 20:35:16', '手电筒', '个', null);
INSERT INTO `trade_items` VALUES ('153', '93', 'item', '12', '1', 'give', '2026-05-24 20:35:16', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('154', '93', 'item', '15', '1', 'give', '2026-05-24 20:35:16', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('155', '93', 'material', '8', '11', 'give', '2026-05-24 20:35:16', '燃料', 'kg', null);
INSERT INTO `trade_items` VALUES ('156', '93', 'material', '5', '31', 'give', '2026-05-24 20:35:16', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('157', '93', 'material', '3', '10', 'give', '2026-05-24 20:35:16', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('158', '93', 'material', '2', '12', 'give', '2026-05-24 20:35:16', '木材', '个', null);
INSERT INTO `trade_items` VALUES ('159', '94', 'material', '5', '2', 'give', '2026-05-24 20:37:46', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('160', '95', 'item', '1', '2', 'give', '2026-05-24 20:59:18', '医疗包', '个', null);
INSERT INTO `trade_items` VALUES ('161', '95', 'item', '2', '10', 'give', '2026-05-24 20:59:18', '手电筒', '个', null);
INSERT INTO `trade_items` VALUES ('162', '95', 'item', '3', '4', 'give', '2026-05-24 20:59:18', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('163', '95', 'item', '5', '1', 'give', '2026-05-24 20:59:18', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('164', '95', 'item', '6', '2', 'give', '2026-05-24 20:59:18', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('165', '95', 'item', '10', '20', 'give', '2026-05-24 20:59:18', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('166', '95', 'item', '11', '3', 'give', '2026-05-24 20:59:18', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('167', '95', 'item', '12', '1', 'give', '2026-05-24 20:59:18', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('168', '95', 'item', '13', '20', 'give', '2026-05-24 20:59:18', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('169', '95', 'item', '14', '5', 'give', '2026-05-24 20:59:18', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('170', '95', 'item', '15', '2', 'give', '2026-05-24 20:59:18', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('171', '95', 'item', '18', '1', 'give', '2026-05-24 20:59:18', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('172', '95', 'weapon', '1', '2', 'give', '2026-05-24 20:59:18', '制式手枪', '个', null);
INSERT INTO `trade_items` VALUES ('173', '95', 'weapon', '2', '1', 'give', '2026-05-24 20:59:18', '猎枪', '个', null);
INSERT INTO `trade_items` VALUES ('174', '95', 'weapon', '3', '4', 'give', '2026-05-24 20:59:18', '警棍', '个', null);
INSERT INTO `trade_items` VALUES ('175', '95', 'weapon', '4', '2', 'give', '2026-05-24 20:59:18', '刺刀', '个', null);
INSERT INTO `trade_items` VALUES ('176', '95', 'weapon', '6', '1', 'give', '2026-05-24 20:59:18', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('177', '95', 'weapon', '7', '1', 'give', '2026-05-24 20:59:18', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('178', '95', 'ammo', '1', '4', 'give', '2026-05-24 20:59:18', '手枪弹', '个', null);
INSERT INTO `trade_items` VALUES ('179', '95', 'ammo', '2', '2', 'give', '2026-05-24 20:59:18', '猎枪弹', '个', null);
INSERT INTO `trade_items` VALUES ('180', '95', 'ammo', '3', '2', 'give', '2026-05-24 20:59:18', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('181', '95', 'ammo', '4', '2', 'give', '2026-05-24 20:59:18', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('182', '95', 'material', '8', '193', 'give', '2026-05-24 20:59:18', '燃料', 'kg', null);
INSERT INTO `trade_items` VALUES ('183', '95', 'material', '5', '3', 'give', '2026-05-24 20:59:18', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('184', '95', 'item', '7', '1', 'give', '2026-05-24 20:59:18', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('185', '96', 'weapon', '3', '1', 'give', '2026-05-24 20:59:52', '警棍', '个', null);
INSERT INTO `trade_items` VALUES ('186', '97', 'material', '9', '50', 'give', '2026-05-24 21:09:11', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('187', '98', 'weapon', '2', '1', 'give', '2026-05-24 21:09:41', '猎枪', '个', null);
INSERT INTO `trade_items` VALUES ('188', '98', 'ammo', '2', '2', 'give', '2026-05-24 21:09:41', '猎枪弹', '个', null);
INSERT INTO `trade_items` VALUES ('189', '99', 'material', '3', '10', 'give', '2026-05-24 21:13:52', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('190', '100', 'item', '15', '1', 'give', '2026-05-24 21:22:30', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('191', '101', 'material', '5', '10', 'give', '2026-05-24 22:14:48', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('192', '102', 'weapon', '7', '1', 'give', '2026-05-24 22:15:35', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('193', '102', 'ammo', '4', '1', 'give', '2026-05-24 22:15:35', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('194', '102', 'material', '5', '10', 'take', '2026-05-24 22:15:35', '食物', 'kg', null);
INSERT INTO `trade_items` VALUES ('195', '103', 'item', '6', '1', 'give', '2026-05-24 22:26:59', '未知物品', '个', null);
INSERT INTO `trade_items` VALUES ('196', '104', 'item', '6', '1', 'take', '2026-05-24 22:27:05', null, null, null);

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
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', 'hey', '695390489', 'dm', null, '2026-04-26 22:13:35', '2026-05-22 18:18:31', '1');
INSERT INTO `user` VALUES ('2', '笙笙', '123ghjkl', 'dm', null, '2026-04-26 22:13:35', '2026-05-22 20:29:26', '1');
INSERT INTO `user` VALUES ('10', 'psq810820', 'psq810820', 'player', '8', '2026-05-22 17:11:11', '2026-05-22 17:11:11', '1');
INSERT INTO `user` VALUES ('11', 'duijiu', '496815641', 'player', '9', '2026-05-22 17:14:43', '2026-05-22 17:14:43', '1');
INSERT INTO `user` VALUES ('12', '5201314xy', '5201314xy', 'player', '10', '2026-05-22 17:16:42', '2026-05-22 17:16:42', '1');
INSERT INTO `user` VALUES ('13', 'xishuai', '1986416781', 'player', '11', '2026-05-22 17:19:14', '2026-05-22 17:19:14', '1');
INSERT INTO `user` VALUES ('14', 'maple8276', 'weiyang20021020', 'player', '12', '2026-05-22 17:24:37', '2026-05-22 18:14:34', '1');
INSERT INTO `user` VALUES ('15', 'pinglan', '1548164', 'player', '13', '2026-05-22 17:26:00', '2026-05-22 17:26:00', '1');
INSERT INTO `user` VALUES ('16', 'cyril', 'celtic', 'player', '14', '2026-05-22 17:31:02', '2026-05-22 17:31:02', '1');
INSERT INTO `user` VALUES ('17', '8738', '123456', 'player', '15', '2026-05-22 17:32:02', '2026-05-22 17:32:02', '1');
INSERT INTO `user` VALUES ('18', 'gcmj0526', 'gcmj0526', 'player', '16', '2026-05-22 17:33:39', '2026-05-22 17:33:39', '1');
INSERT INTO `user` VALUES ('19', 'mikotouma', '128307', 'player', '17', '2026-05-22 17:34:38', '2026-05-22 17:34:38', '1');
INSERT INTO `user` VALUES ('20', 'cl5220682', 'asd147258369', 'player', '18', '2026-05-22 17:35:54', '2026-05-22 17:35:54', '1');
INSERT INTO `user` VALUES ('21', 'fxgpt', 'fuxue0522', 'player', '19', '2026-05-22 17:36:56', '2026-05-22 17:36:56', '1');
INSERT INTO `user` VALUES ('22', 'zhuifeng233', '741963', 'player', '20', '2026-05-22 17:38:36', '2026-05-22 19:20:10', '1');
INSERT INTO `user` VALUES ('23', 'leyu', '514615', 'player', '21', '2026-05-22 17:40:25', '2026-05-22 17:40:25', '1');
INSERT INTO `user` VALUES ('24', '1261952870', '15922665792', 'player', '22', '2026-05-22 17:41:19', '2026-05-22 17:41:19', '1');
INSERT INTO `user` VALUES ('25', 'jiaohuang', '1515614', 'player', '23', '2026-05-22 17:42:40', '2026-05-22 17:42:40', '1');
INSERT INTO `user` VALUES ('26', 'huahai', '12345678', 'player', '24', '2026-05-22 17:46:08', '2026-05-22 19:34:17', '1');
INSERT INTO `user` VALUES ('27', 'tony123', 'owokzh&&', 'player', '25', '2026-05-22 17:47:48', '2026-05-22 17:47:48', '1');
INSERT INTO `user` VALUES ('28', 'V', 'Welcome2026!', 'player', '26', '2026-05-22 17:49:51', '2026-05-22 17:49:51', '1');
INSERT INTO `user` VALUES ('29', 'degou', '148561', 'player', '27', '2026-05-22 17:51:15', '2026-05-22 17:51:15', '1');
INSERT INTO `user` VALUES ('30', 'xiaohei', '1221445', 'player', '28', '2026-05-22 17:56:36', '2026-05-22 17:56:36', '1');
INSERT INTO `user` VALUES ('31', 'feifan', '164516', 'player', '29', '2026-05-22 17:58:03', '2026-05-22 17:58:03', '1');
INSERT INTO `user` VALUES ('32', '15102374415', 'sansan117', 'player', '30', '2026-05-22 18:00:26', '2026-05-22 18:00:26', '1');
INSERT INTO `user` VALUES ('33', 'xianyu', '357537', 'player', '31', '2026-05-22 18:04:24', '2026-05-22 18:04:24', '1');
INSERT INTO `user` VALUES ('34', 'hjx18825', '200825', 'player', '32', '2026-05-22 18:07:26', '2026-05-22 18:07:26', '1');
INSERT INTO `user` VALUES ('35', 'mirageshyv', '123456', 'player', '33', '2026-05-23 19:49:05', '2026-05-23 19:49:05', '1');
INSERT INTO `user` VALUES ('36', 'player', '123456', 'player', '34', '2026-06-22 11:55:43', '2026-06-22 11:55:43', '1');
INSERT INTO `user` VALUES ('37', 'cs', 'cs', 'player', '35', '2026-06-24 20:08:33', '2026-06-24 20:08:33', '1');

-- ----------------------------
-- Table structure for warehouse_ark
-- ----------------------------
DROP TABLE IF EXISTS `warehouse_ark`;
CREATE TABLE `warehouse_ark` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `item_type` enum('item','weapon','ammo','material') NOT NULL,
  `item_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_ark_type_item` (`item_type`,`item_id`),
  KEY `idx_item_type` (`item_type`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COMMENT='仓库-冒险者阵营仓库';

-- ----------------------------
-- Records of warehouse_ark
-- ----------------------------
INSERT INTO `warehouse_ark` VALUES ('6', 'material', '2', '40000', '2026-05-24 20:50:30', '2026-05-24 20:50:30');

-- ----------------------------
-- Table structure for warehouse_armory
-- ----------------------------
DROP TABLE IF EXISTS `warehouse_armory`;
CREATE TABLE `warehouse_armory` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `item_type` enum('item','weapon','ammo','material') NOT NULL,
  `item_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_armory_type_item` (`item_type`,`item_id`),
  KEY `idx_item_type` (`item_type`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COMMENT='仓库-镇武库（镇长厅）';

-- ----------------------------
-- Records of warehouse_armory
-- ----------------------------
INSERT INTO `warehouse_armory` VALUES ('15', 'material', '2', '495', '2026-05-22 22:07:28', '2026-05-23 22:05:19');

-- ----------------------------
-- Table structure for warehouse_config
-- ----------------------------
DROP TABLE IF EXISTS `warehouse_config`;
CREATE TABLE `warehouse_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `warehouse_key` varchar(50) NOT NULL COMMENT '仓库标识键',
  `warehouse_name` varchar(50) NOT NULL COMMENT '仓库名称',
  `table_name` varchar(50) NOT NULL COMMENT '对应数据库表名',
  `key_item_id` int(11) NOT NULL COMMENT '对应钥匙的item表ID',
  `icon` varchar(20) DEFAULT '' COMMENT '图标',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`),
  UNIQUE KEY `warehouse_key` (`warehouse_key`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COMMENT='仓库配置表';

-- ----------------------------
-- Records of warehouse_config
-- ----------------------------
INSERT INTO `warehouse_config` VALUES ('1', 'general', '矿场仓库', 'warehouse_general', '19', 'box', '1');
INSERT INTO `warehouse_config` VALUES ('2', 'fuel', '燃料仓库', 'warehouse_fuel', '20', 'fuel', '2');
INSERT INTO `warehouse_config` VALUES ('3', 'armory', '镇武库', 'warehouse_armory', '21', 'sword', '3');
INSERT INTO `warehouse_config` VALUES ('4', 'dock', '码头集购仓', 'warehouse_dock', '22', 'anchor', '4');
INSERT INTO `warehouse_config` VALUES ('5', 'rebel', '反抗者阵营仓库', 'warehouse_rebel', '23', 'flag', '5');
INSERT INTO `warehouse_config` VALUES ('6', 'ark', '冒险者阵营仓库', 'warehouse_ark', '24', 'ship', '6');

-- ----------------------------
-- Table structure for warehouse_dock
-- ----------------------------
DROP TABLE IF EXISTS `warehouse_dock`;
CREATE TABLE `warehouse_dock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `item_type` enum('item','weapon','ammo','material') NOT NULL,
  `item_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_dock_type_item` (`item_type`,`item_id`),
  KEY `idx_item_type` (`item_type`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库-码头集购仓';

-- ----------------------------
-- Records of warehouse_dock
-- ----------------------------

-- ----------------------------
-- Table structure for warehouse_fuel
-- ----------------------------
DROP TABLE IF EXISTS `warehouse_fuel`;
CREATE TABLE `warehouse_fuel` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `item_type` enum('item','weapon','ammo','material') NOT NULL,
  `item_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_fuel_type_item` (`item_type`,`item_id`),
  KEY `idx_item_type` (`item_type`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COMMENT='仓库-燃料仓库（警察局）';

-- ----------------------------
-- Records of warehouse_fuel
-- ----------------------------
INSERT INTO `warehouse_fuel` VALUES ('2', 'material', '2', '99046', '2026-05-17 00:00:00', '2026-05-24 12:46:52');
INSERT INTO `warehouse_fuel` VALUES ('3', 'material', '6', '100', '2026-06-22 12:00:53', '2026-06-22 12:00:53');
INSERT INTO `warehouse_fuel` VALUES ('4', 'material', '8', '20', '2026-06-22 12:01:07', '2026-06-22 12:01:07');

-- ----------------------------
-- Table structure for warehouse_general
-- ----------------------------
DROP TABLE IF EXISTS `warehouse_general`;
CREATE TABLE `warehouse_general` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `item_type` enum('item','weapon','ammo','material') NOT NULL,
  `item_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_general_type_item` (`item_type`,`item_id`),
  KEY `idx_item_type` (`item_type`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COMMENT='仓库-矿场仓库';

-- ----------------------------
-- Records of warehouse_general
-- ----------------------------
INSERT INTO `warehouse_general` VALUES ('1', 'material', '1', '49991', '2026-05-17 00:00:00', '2026-05-24 16:21:04');
INSERT INTO `warehouse_general` VALUES ('2', 'material', '7', '5000', '2026-05-17 00:00:00', '2026-05-17 00:00:00');
INSERT INTO `warehouse_general` VALUES ('4', 'material', '3', '100', '2026-05-17 00:00:00', '2026-05-22 05:21:43');
INSERT INTO `warehouse_general` VALUES ('9', 'material', '12', '1', '2026-05-17 00:00:00', '2026-05-17 00:00:00');
INSERT INTO `warehouse_general` VALUES ('10', 'material', '2', '3060', '2026-05-22 05:21:17', '2026-05-24 21:30:06');
INSERT INTO `warehouse_general` VALUES ('11', 'material', '4', '100', '2026-05-22 05:21:35', '2026-05-22 05:21:35');

-- ----------------------------
-- Table structure for warehouse_rebel
-- ----------------------------
DROP TABLE IF EXISTS `warehouse_rebel`;
CREATE TABLE `warehouse_rebel` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `item_type` enum('item','weapon','ammo','material') NOT NULL,
  `item_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_rebel_type_item` (`item_type`,`item_id`),
  KEY `idx_item_type` (`item_type`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COMMENT='仓库-反抗者阵营仓库';

-- ----------------------------
-- Records of warehouse_rebel
-- ----------------------------
INSERT INTO `warehouse_rebel` VALUES ('1', 'weapon', '2', '1', '2026-05-17 00:00:00', '2026-05-17 00:00:00');
INSERT INTO `warehouse_rebel` VALUES ('2', 'ammo', '2', '2', '2026-05-17 00:00:00', '2026-05-17 00:00:00');
INSERT INTO `warehouse_rebel` VALUES ('3', 'weapon', '7', '2', '2026-05-17 00:00:00', '2026-05-17 00:00:00');
INSERT INTO `warehouse_rebel` VALUES ('4', 'ammo', '4', '4', '2026-05-17 00:00:00', '2026-05-17 00:00:00');
INSERT INTO `warehouse_rebel` VALUES ('5', 'material', '6', '20', '2026-05-22 05:23:38', '2026-05-22 05:23:38');

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
INSERT INTO `weapon` VALUES ('1', '制式手枪', '把', '韦伯利.38口径转轮手枪，英军标准配发。威胁值2，近距离防身武器，装弹6发。', '5', '2026-04-27 11:36:23', '2026-05-19 22:49:10');
INSERT INTO `weapon` VALUES ('2', '猎枪', '把', '12号口径单管或双管猎枪，用于狩猎鸟类和小型动物。威胁值3，中距离武器，装弹2发。', '6', '2026-04-27 11:36:23', '2026-05-02 19:21:45');
INSERT INTO `weapon` VALUES ('3', '警棍', '个', '硬木制成的短棍，长50厘米。威胁值0.5，非致命武器，可用于制服而非杀死目标。', '1', '2026-04-27 11:36:23', '2026-05-02 19:20:27');
INSERT INTO `weapon` VALUES ('4', '刺刀', '把', '军用制式刺刀，长约20厘米。威胁值1。', '2', '2026-04-27 11:36:23', '2026-05-02 19:21:50');
INSERT INTO `weapon` VALUES ('5', '水手刀', '把', '多功能刀具，威胁值2。', '2', '2026-04-27 11:36:23', '2026-05-02 19:21:53');
INSERT INTO `weapon` VALUES ('6', '鱼叉/矛', '个', '铁头木柄的捕鱼工具，长110厘米。威胁值2，既可捕鱼也可作为近战武器，渔民的标配。', '3', '2026-04-27 11:36:23', '2026-06-23 18:50:13');
INSERT INTO `weapon` VALUES ('7', '猎弓', '张', '简单木质主体金属包角的反曲猎弓，威胁值2，无声远程武器。', '4', '2026-04-27 11:36:23', '2026-05-02 19:22:03');
INSERT INTO `weapon` VALUES ('8', '十字镐', '把', '采矿用的双头镐具，长65厘米，重5kg。威胁值0.5，主要用来挖掘石料，紧急时也可作为武器。', '1', '2026-04-27 11:36:23', '2026-05-02 19:21:04');
INSERT INTO `weapon` VALUES ('9', '斧头', '把', '伐木用双面斧，长65厘米。威胁值1，砍树是本职工作，砍人也不是不行。', '2', '2026-04-27 11:36:23', '2026-05-02 19:22:11');
INSERT INTO `weapon` VALUES ('10', '电锯', '把', '二冲程汽油动力链锯，噪音巨大。威胁值2，伐木效率极高（30吨原木/天），但需要燃料且会暴露位置。', '4', '2026-04-27 11:36:23', '2026-05-02 19:22:15');
INSERT INTO `weapon` VALUES ('11', '手术刀', '把', '医用不锈钢手术刀，套装含多型号刀片。威胁值0.5，精准切割工具，在医疗行动中不可或缺。', '1', '2026-04-27 11:36:23', '2026-05-02 19:21:20');
INSERT INTO `weapon` VALUES ('12', '炸药', 'kg', '工业硝铵炸药，每公斤可开凿大量石料。威胁值极高，可用于挖矿加速、拆除建筑或制造大规模破坏', '10', '2026-04-27 11:36:23', '2026-04-27 11:36:23');

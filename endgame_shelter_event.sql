/*
Navicat MySQL Data Transfer

Source Server         : cc
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : snowisland

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2026-06-27 12:04:00
*/

SET FOREIGN_KEY_CHECKS=0;

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

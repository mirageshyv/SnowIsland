-- 食物/能量系统初始化：全局 catalog、玩家个人库存、统治者避难所公共库存（shelter_*_stock）
-- 适用：旧库首次接入，或从早期结构升级。全新全量导入见 sql/snowisland.sql。
-- 全新环境若已用 sql/snowisland.sql 全量导入，则已包含同结构，无需再执行本脚本。
-- 执行前请备份；若表已存在且需保留数据，勿重复执行 DROP 段。

USE snowisland;

SET FOREIGN_KEY_CHECKS=0;

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
  `unit` varchar(16) NOT NULL,
  `kcal_per_unit` int(11) NOT NULL,
  `sort_order` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`item_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `player_energy_stock` (`player_id`, `item_key`, `quantity`) VALUES
(1, 'firewood', 3), (1, 'coal', 1);

CREATE TABLE `shelter_food_stock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `item_key` varchar(64) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sfs_item` (`item_key`),
  CONSTRAINT `fk_sfs_food_catalog` FOREIGN KEY (`item_key`) REFERENCES `food_catalog` (`item_key`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `shelter_energy_stock` (`item_key`, `quantity`) VALUES
('firewood', 25), ('coal', 10), ('fuel_oil', 5);

SET FOREIGN_KEY_CHECKS=1;

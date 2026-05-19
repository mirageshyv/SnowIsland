-- 增量迁移：已合并进 snowisland.sql（全新安装请直接导入主库脚本）
-- 仅用于在旧库上升级；若某条已执行过，请跳过对应语句

ALTER TABLE `player`
  ADD COLUMN `is_severely_injured` tinyint(1) NOT NULL DEFAULT 0 COMMENT '重伤' AFTER `is_injured`,
  ADD COLUMN `is_dead` tinyint(1) NOT NULL DEFAULT 0 COMMENT '死亡' AFTER `is_severely_injured`;

CREATE TABLE IF NOT EXISTS `game_day_settings` (
  `game_day` int(11) NOT NULL COMMENT '游戏天数',
  `required_food_units` int(11) NOT NULL DEFAULT 2 COMMENT '每人每日所需食物（单位）',
  `required_fuel_kg` int(11) NOT NULL DEFAULT 15 COMMENT '每人每日取暖燃料（千克，木材或燃料）',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`game_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日全局消耗需求（DM在游戏设置中配置）';

CREATE TABLE IF NOT EXISTS `player_daily_consumption` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL,
  `game_day` int(11) NOT NULL,
  `required_food_units` int(11) NOT NULL DEFAULT 2,
  `required_fuel_kg` int(11) NOT NULL DEFAULT 15,
  `consumed_food_units` int(11) NOT NULL DEFAULT 0,
  `consumed_fuel_kg` int(11) NOT NULL DEFAULT 0,
  `fuel_from_wood_kg` int(11) NOT NULL DEFAULT 0,
  `fuel_from_fuel_kg` int(11) NOT NULL DEFAULT 0,
  `submitted` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_day` (`player_id`, `game_day`),
  KEY `idx_game_day` (`game_day`),
  CONSTRAINT `player_daily_consumption_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家每日进食与取暖消耗记录';

-- Shelter labor: support NPC workers (drop FK to player)
ALTER TABLE `shelter_daily_labor`
  DROP FOREIGN KEY `shelter_daily_labor_ibfk_1`;

ALTER TABLE `shelter_daily_labor`
  CHANGE COLUMN `player_id` `worker_id` int(11) NOT NULL COMMENT '玩家ID或NPC ID',
  ADD COLUMN `worker_kind` varchar(10) NOT NULL DEFAULT 'player' COMMENT 'player|npc' AFTER `game_day`,
  DROP INDEX `UK_shelter_labor_day_player`,
  ADD UNIQUE KEY `uk_shelter_labor_day_worker` (`game_day`, `worker_kind`, `worker_id`);

-- Migrate existing DB: drop shelter_progress, extend shelter_daily_labor, add shelter_labor_day.
-- Run after backup. Skip statements that fail if already applied.

DROP TABLE IF EXISTS `shelter_progress`;

ALTER TABLE `shelter_daily_labor`
  ADD COLUMN `build_value` int(11) NOT NULL DEFAULT 0 COMMENT '当日贡献建造值' AFTER `player_id`;

ALTER TABLE `shelter_daily_labor`
  ADD COLUMN `is_exploited` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否压榨' AFTER `build_value`;

ALTER TABLE `shelter_daily_labor`
  ADD COLUMN `is_escaped` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否逃役' AFTER `is_exploited`;

-- If you previously added reserved-name columns exploited/escaped, rename them:
-- ALTER TABLE `shelter_daily_labor` CHANGE COLUMN `exploited` `is_exploited` tinyint(1) NOT NULL DEFAULT 0;
-- ALTER TABLE `shelter_daily_labor` CHANGE COLUMN `escaped` `is_escaped` tinyint(1) NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS `shelter_labor_day` (
  `game_day` int(11) NOT NULL COMMENT '游戏天数',
  `verified` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'DM是否已结算确认',
  `verified_at` datetime DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`game_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='避难所每日劳工结算状态';

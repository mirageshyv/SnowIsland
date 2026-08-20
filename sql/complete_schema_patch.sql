-- Apply on a database already imported from an older snowisland.sql dump.
-- Safe to re-run: skips columns/tables that already exist.
-- Usage:
--   mysql -u root -p snowisland < sql/complete_schema_patch.sql

DELIMITER $$

DROP PROCEDURE IF EXISTS si_add_column_if_missing $$
CREATE PROCEDURE si_add_column_if_missing(
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_definition VARCHAR(512)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table
          AND COLUMN_NAME = p_column
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DELIMITER ;

-- job.hidden is required by Job entity; missing this caused GET /api/dm/players 500
CALL si_add_column_if_missing('job', 'hidden', 'TINYINT(1) NOT NULL DEFAULT 0');

CALL si_add_column_if_missing('player', 'overnight_location_id', 'INT NULL');
CALL si_add_column_if_missing('player', 'dm_notes', 'TEXT NULL');
CALL si_add_column_if_missing('player', 'hidden_job_id', 'INT NULL');
CALL si_add_column_if_missing('player', 'trade_banned', 'TINYINT(1) NOT NULL DEFAULT 0');
CALL si_add_column_if_missing('player', 'is_bound', 'TINYINT(1) NOT NULL DEFAULT 0');

CALL si_add_column_if_missing('island_event', 'pack_id', 'INT NULL');
CALL si_add_column_if_missing('island_event', 'pack_name', 'VARCHAR(100) NULL');
CALL si_add_column_if_missing('island_event', 'source_number', 'INT NULL');

CREATE TABLE IF NOT EXISTS `event_pack` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 0,
  `sort_order` int(11) NOT NULL DEFAULT 0,
  `parent_id` int(11) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_pack_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `npc_items` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `npc_id` int(11) NOT NULL,
  `item_type` varchar(20) NOT NULL,
  `item_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_npc_item` (`npc_id`,`item_type`,`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `npc_daily_consumption` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `npc_id` int(11) NOT NULL,
  `game_day` int(11) NOT NULL,
  `required_food_units` int(11) NOT NULL DEFAULT 2,
  `required_fuel_kg` int(11) NOT NULL DEFAULT 25,
  `consumed_food_units` int(11) NOT NULL DEFAULT 0,
  `consumed_fuel_kg` int(11) NOT NULL DEFAULT 0,
  `fuel_from_wood_kg` int(11) NOT NULL DEFAULT 0,
  `fuel_from_fuel_kg` int(11) NOT NULL DEFAULT 0,
  `requirements_met` tinyint(1) NOT NULL DEFAULT 0,
  `result_status` varchar(50) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_npc_day` (`npc_id`,`game_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `player_notebook` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL,
  `title` varchar(80) NOT NULL DEFAULT '未命名',
  `body` text,
  `sort_order` int(11) NOT NULL DEFAULT 0,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_player_notebook_player` (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `player_marker` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `visible_to_player` tinyint(1) NOT NULL DEFAULT 0,
  `note` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_player_marker_player` (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

UPDATE `game_day_settings` SET `required_fuel_kg` = 25 WHERE `required_fuel_kg` = 15;

INSERT INTO `location_npc` (
  `name`, `job`, `gender`, `introduction`, `location_id`,
  `attitude_ruler`, `attitude_rebel`, `attitude_adventurer`, `attitude_scourge`,
  `created_at`, `updated_at`, `status`, `daily_trade_limit`
)
SELECT
  '斯特·贝斯', '民兵', '女',
  '初始就跟着统治者干活的一名很忠心的下属。她会一直遵从统治者的决定，除非她看不到希望。',
  19, '喜好', '厌恶', '忽视', '厌恶', NOW(), NOW(), '正常', 1
FROM DUAL
WHERE EXISTS (SELECT 1 FROM `location` WHERE `id` = 19)
  AND NOT EXISTS (SELECT 1 FROM `location_npc` WHERE `name` = '斯特·贝斯');

DROP PROCEDURE IF EXISTS si_add_column_if_missing;

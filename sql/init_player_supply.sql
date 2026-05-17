-- Player food / fuel stock tables (per player_id + item_key).
-- Safe to run on DBs that already have these tables — skip if objects exist.
-- Full destructive rebuild: see comments in agent history / use only on empty dev DB.

USE snowisland;

CREATE TABLE IF NOT EXISTS `food_catalog` (
  `item_key` varchar(64) NOT NULL,
  `name` varchar(64) NOT NULL,
  `unit` varchar(16) NOT NULL,
  `kcal_per_unit` int(11) NOT NULL DEFAULT '0',
  `sort_order` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`item_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `energy_catalog` (
  `item_key` varchar(64) NOT NULL,
  `name` varchar(64) NOT NULL,
  `unit` varchar(16) NOT NULL,
  `kcal_per_unit` int(11) NOT NULL DEFAULT '0',
  `sort_order` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`item_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `player_food_stock` (
  `player_id` int(11) NOT NULL,
  `item_key` varchar(64) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`player_id`,`item_key`),
  KEY `idx_player_food_item` (`item_key`),
  CONSTRAINT `fk_pfs_player` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `player_energy_stock` (
  `player_id` int(11) NOT NULL,
  `item_key` varchar(64) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`player_id`,`item_key`),
  KEY `idx_player_energy_item` (`item_key`),
  CONSTRAINT `fk_pes_player` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

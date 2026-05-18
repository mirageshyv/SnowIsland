-- Incremental migration: faction action tables (also in snowisland.sql / snowisland_5_15.sql)
USE snowisland;

DROP TABLE IF EXISTS `location_governance`;
DROP TABLE IF EXISTS `faction_action`;

CREATE TABLE `faction_action` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL COMMENT '提交玩家ID',
  `player_name` varchar(50) DEFAULT NULL COMMENT '玩家名称快照',
  `faction` varchar(20) NOT NULL COMMENT '提交时阵营',
  `action_type` varchar(40) NOT NULL COMMENT '阵营行动类型',
  `payload` text COMMENT 'JSON输入数据',
  `result` text COMMENT '行动结果/DM反馈',
  `status` enum('pending','feedbacked') NOT NULL DEFAULT 'pending' COMMENT '反馈状态',
  `game_day` int(11) NOT NULL DEFAULT '1' COMMENT '游戏天数',
  `phase` varchar(20) DEFAULT 'day' COMMENT '阶段: day/night',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_player_day` (`player_id`,`game_day`),
  KEY `idx_faction_day` (`faction`,`game_day`),
  KEY `idx_action_type` (`action_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阵营行动表';

CREATE TABLE `location_governance` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `location_id` int(11) NOT NULL COMMENT '监管地点ID',
  `location_name` varchar(100) DEFAULT NULL COMMENT '地点名称',
  `actor_id` int(11) DEFAULT NULL COMMENT '监管人员ID(玩家或NPC)',
  `actor_name` varchar(50) DEFAULT NULL COMMENT '监管人员名称',
  `actor_kind` varchar(10) DEFAULT 'player' COMMENT 'player或npc',
  `game_day` int(11) NOT NULL COMMENT '生效游戏天数',
  `source_faction_action_id` int(11) DEFAULT NULL COMMENT '来源阵营行动ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_location_day` (`location_id`,`game_day`),
  KEY `idx_game_day` (`game_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地点监管状态表';

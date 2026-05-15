USE snowisland;

DROP TABLE IF EXISTS player_stealth;
DROP TABLE IF EXISTS player_action;

CREATE TABLE player_action (
  id INT AUTO_INCREMENT PRIMARY KEY,
  player_id INT NOT NULL COMMENT '行动玩家ID',
  player_name VARCHAR(50) COMMENT '玩家名称',
  player_faction VARCHAR(20) COMMENT '玩家阵营',
  action_slot TINYINT NOT NULL COMMENT '行动槽位(1或2)',
  action_type VARCHAR(30) NOT NULL COMMENT '行动类型',
  target_id INT COMMENT '目标ID(地点ID/玩家ID等)',
  target_name VARCHAR(100) COMMENT '目标名称',
  npc_id INT COMMENT '互动NPC的ID',
  npc_name VARCHAR(50) COMMENT '互动NPC名称',
  notes TEXT COMMENT '备注说明',
  result TEXT COMMENT '行动结果',
  status ENUM('pending','feedbacked') NOT NULL DEFAULT 'pending' COMMENT '反馈状态',
  game_day INT NOT NULL DEFAULT 1 COMMENT '游戏天数',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_player_id (player_id),
  INDEX idx_action_type (action_type),
  INDEX idx_status (status),
  INDEX idx_game_day (game_day),
  INDEX idx_player_day (player_id, game_day)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家行动表';

CREATE TABLE player_stealth (
  id INT AUTO_INCREMENT PRIMARY KEY,
  player_id INT NOT NULL COMMENT '玩家ID',
  player_name VARCHAR(50) COMMENT '玩家名称',
  game_day INT NOT NULL COMMENT '潜行生效的天数',
  source_action_id INT COMMENT '来源行动ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_player_day (player_id, game_day),
  INDEX idx_game_day (game_day)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家潜行状态表';

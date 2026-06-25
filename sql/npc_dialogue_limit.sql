-- NPC对话交流次数限制系统
-- 版本: 1.0
-- 日期: 2026-06-22

-- 创建玩家NPC每日对话计数器表
CREATE TABLE IF NOT EXISTS npc_daily_dialogue_count (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    player_id INTEGER NOT NULL COMMENT '玩家编号',
    npc_id INTEGER NOT NULL COMMENT 'NPC编号',
    dialogue_count INTEGER NOT NULL DEFAULT 0 COMMENT '当日对话次数',
    last_game_day INTEGER NOT NULL COMMENT '最后对话的游戏天数',
    last_dialogue_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后对话时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_player_npc (player_id, npc_id),
    CONSTRAINT fk_daily_dialogue_player FOREIGN KEY (player_id) REFERENCES player(id),
    CONSTRAINT fk_daily_dialogue_npc FOREIGN KEY (npc_id) REFERENCES location_npc(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家NPC每日对话计数器表';

-- 添加索引提高查询效率
CREATE INDEX idx_daily_dialogue_player ON npc_daily_dialogue_count(player_id);
CREATE INDEX idx_daily_dialogue_npc ON npc_daily_dialogue_count(npc_id);
CREATE INDEX idx_daily_dialogue_day ON npc_daily_dialogue_count(last_game_day);

-- 快速交互表
CREATE TABLE IF NOT EXISTS quick_interaction (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    player_name VARCHAR(50),
    faction VARCHAR(20) NOT NULL,
    interaction_type VARCHAR(30) NOT NULL COMMENT '交互类型: quick_action/supplementary_action/rule_consult/ask_dm',
    content TEXT NOT NULL,
    game_day INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '反馈状态: pending/processed/replied',
    dm_reply TEXT COMMENT 'DM回复内容',
    replied_at DATETIME COMMENT '回复时间戳',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_player_game_day (player_id, game_day),
    INDEX idx_game_day (game_day),
    INDEX idx_faction_game_day (faction, game_day),
    INDEX idx_status (status),
    FOREIGN KEY (player_id) REFERENCES player(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

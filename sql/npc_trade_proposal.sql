-- NPC AI trade proposals (chat-created, accepted on the NPC 交易 tab).
-- Idempotent. Does not alter player-to-player `trade` tables.
-- Rejected rows do not block a later offer the same day (no unique on npc+player+day).

CREATE TABLE IF NOT EXISTS npc_trade_proposal (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    npc_id INTEGER NOT NULL COMMENT 'NPC编号',
    player_id INTEGER NOT NULL COMMENT '被提议的玩家',
    game_day INTEGER NOT NULL COMMENT '游戏天数',
    status VARCHAR(20) NOT NULL DEFAULT 'open' COMMENT 'open/completed/rejected/expired',
    give_items TEXT COMMENT 'NPC给出的物资JSON [{t,id,q,name}]',
    take_items TEXT COMMENT 'NPC索取的物资JSON [{t,id,q,name}]',
    remark VARCHAR(255) DEFAULT NULL COMMENT '短备注，可展示在交易页',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_npc_player_day_proposal (npc_id, player_id, game_day, status),
    KEY idx_npc_proposal_status (npc_id, status),
    CONSTRAINT fk_npc_trade_proposal_npc FOREIGN KEY (npc_id) REFERENCES location_npc(id),
    CONSTRAINT fk_npc_trade_proposal_player FOREIGN KEY (player_id) REFERENCES player(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NPC向玩家提出的交易';

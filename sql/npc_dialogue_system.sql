-- NPC对话系统数据库迁移脚本
-- 功能：实现AI驱动的NPC对话交互系统

-- 1. 创建NPC好感度表
CREATE TABLE IF NOT EXISTS npc_favor (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    npc_id INTEGER NOT NULL,
    player_id INTEGER NOT NULL,
    favor_value INTEGER NOT NULL DEFAULT 0 COMMENT '好感度值(-100~100)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_npc_player (npc_id, player_id),
    CONSTRAINT fk_npc_favor_npc FOREIGN KEY (npc_id) REFERENCES location_npc(id),
    CONSTRAINT fk_npc_favor_player FOREIGN KEY (player_id) REFERENCES player(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NPC好感度表';

-- 2. 创建NPC对话记录表
CREATE TABLE IF NOT EXISTS npc_dialogue (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    npc_id INTEGER NOT NULL,
    player_id INTEGER NOT NULL,
    player_message TEXT NOT NULL COMMENT '玩家发送的消息',
    npc_reply TEXT NOT NULL COMMENT 'NPC回复内容',
    favor_change INTEGER DEFAULT 0 COMMENT '本次对话好感度变化',
    dialogue_round INTEGER NOT NULL COMMENT '对话轮次(游戏天数)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_npc_dialogue_npc FOREIGN KEY (npc_id) REFERENCES location_npc(id),
    CONSTRAINT fk_npc_dialogue_player FOREIGN KEY (player_id) REFERENCES player(id),
    INDEX idx_npc_dialogue_time (created_at),
    INDEX idx_npc_dialogue_player (player_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NPC对话记录表';

-- 3. 扩展LocationNpc表，添加对话相关字段
ALTER TABLE location_npc
    ADD COLUMN personality TEXT COMMENT '性格描述' AFTER introduction,
    ADD COLUMN status TEXT COMMENT '当前状态' AFTER personality,
    ADD COLUMN dialogue_style TEXT COMMENT '对话风格' AFTER status,
    ADD COLUMN avatar_url VARCHAR(255) COMMENT '头像URL' AFTER dialogue_style;

-- 4. 初始化NPC默认好感度（为所有现有NPC和玩家创建初始记录）
INSERT INTO npc_favor (npc_id, player_id, favor_value)
SELECT n.id, p.id, 0
FROM location_npc n
CROSS JOIN player p
ON DUPLICATE KEY UPDATE favor_value = VALUES(favor_value);

-- 5. 插入示例NPC数据（如果表为空）
INSERT IGNORE INTO location_npc (name, job, gender, introduction, location_id, personality, status, dialogue_style) VALUES
('老船长', '港口管理员', '男', '在港口工作了三十年的老水手，见过无数风浪。', 1, '沉稳、固执、重情义', '正常', '粗犷、直接'),
('莉娜', '酒馆老板娘', '女', '小镇上最受欢迎的酒馆老板娘，消息灵通。', 2, '热情、精明、八卦', '正常', '热情、略带调侃'),
('约翰神父', '教堂牧师', '男', '虔诚的信徒，在末世中仍坚守信仰。', 3, '温和、虔诚、善良', '正常', '温和、充满希望'),
('铁匠汤姆', '铁匠', '男', '小镇唯一的铁匠，手艺精湛但脾气古怪。', 4, '固执、认真、不善言辞', '正常', '简短、务实'),
('军医艾琳', '医疗官', '女', '曾经是军队军医，现在负责小镇的医疗事务。', 5, '冷静、专业、冷漠', '正常', '冷静、专业'),
('商人格雷', '商人', '男', '四处奔波的商人，消息灵通。', 6, '精明、圆滑、唯利是图', '正常', '精明、讨价还价'),
('猎人杰克', '猎人', '男', '擅长在荒野中生存的猎人。', 7, '沉默、警觉、孤独', '正常', '简短、警惕'),
('镇长威廉', '镇长', '男', '小镇的管理者，努力维持秩序。', 8, '威严、公正、疲惫', '正常', '正式、威严');

-- 6. 重新初始化好感度
INSERT INTO npc_favor (npc_id, player_id, favor_value)
SELECT n.id, p.id, 0
FROM location_npc n
CROSS JOIN player p
ON DUPLICATE KEY UPDATE favor_value = VALUES(favor_value);

-- 查询验证
SELECT 'NPC列表:' as info;
SELECT id, name, job, personality, status FROM location_npc LIMIT 10;

SELECT '好感度记录数:' as info;
SELECT COUNT(*) as count FROM npc_favor;
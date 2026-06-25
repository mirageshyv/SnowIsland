-- NPC求助系统迁移脚本
-- 版本: 1.0
-- 日期: 2026-06-23

-- 1. 创建NPC求助配置表
CREATE TABLE IF NOT EXISTS npc_help_config (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    npc_id INTEGER NOT NULL COMMENT 'NPC编号',
    help_type VARCHAR(50) NOT NULL COMMENT '求助类型: gather(采集)/hunt(狩猎)/craft(制作)/medical(医疗)/repair(维修)/guide(向导)',
    help_name VARCHAR(100) NOT NULL COMMENT '求助名称',
    help_description TEXT COMMENT '求助描述',
    base_cost_type VARCHAR(20) NOT NULL COMMENT '报酬类型: material/item/weapon/ammo',
    base_cost_item_id INTEGER NOT NULL COMMENT '报酬物资ID',
    base_cost_quantity INTEGER NOT NULL DEFAULT 0 COMMENT '基础报酬数量',
    cost_min_modifier DECIMAL(5,2) DEFAULT 0.80 COMMENT '报酬最小浮动系数',
    cost_max_modifier DECIMAL(5,2) DEFAULT 1.20 COMMENT '报酬最大浮动系数',
    min_favor_level VARCHAR(20) NOT NULL DEFAULT 'neutral' COMMENT '最低好感度要求: hostile/unfriendly/neutral/friendly/close',
    duration_minutes INTEGER DEFAULT 60 COMMENT '帮助持续时间(分钟)',
    success_rate DECIMAL(5,2) DEFAULT 0.90 COMMENT '成功率',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_npc_help_type (npc_id, help_type),
    CONSTRAINT fk_npc_help_config_npc FOREIGN KEY (npc_id) REFERENCES location_npc(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NPC求助配置表';

-- 2. 创建NPC求助记录表
CREATE TABLE IF NOT EXISTS npc_help_record (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    npc_id INTEGER NOT NULL COMMENT 'NPC编号',
    player_id INTEGER NOT NULL COMMENT '玩家编号',
    help_type VARCHAR(50) NOT NULL COMMENT '求助类型',
    help_name VARCHAR(100) NOT NULL COMMENT '求助名称',
    cost_type VARCHAR(20) NOT NULL COMMENT '实际支付类型',
    cost_item_id INTEGER NOT NULL COMMENT '实际支付物资ID',
    cost_quantity INTEGER NOT NULL COMMENT '实际支付数量',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending(处理中)/completed(完成)/failed(失败)/cancelled(取消)',
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    result_description TEXT COMMENT '结果描述',
    favor_change INTEGER DEFAULT 0 COMMENT '好感度变化',
    game_day INTEGER NOT NULL COMMENT '游戏天数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_npc_help_record_npc FOREIGN KEY (npc_id) REFERENCES location_npc(id),
    CONSTRAINT fk_npc_help_record_player FOREIGN KEY (player_id) REFERENCES player(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NPC求助记录表';

-- 3. 初始化示例数据
INSERT INTO npc_help_config (npc_id, help_type, help_name, help_description, base_cost_type, base_cost_item_id, base_cost_quantity, cost_min_modifier, cost_max_modifier, min_favor_level, duration_minutes, success_rate) VALUES
-- NPC 1: 商人 - 可以提供交易相关帮助
(1, 'guide', '寻找物资', '帮助你在附近寻找有用的物资', 'material', 5, 2, 0.80, 1.20, 'neutral', 30, 0.85),
(1, 'craft', '制作工具', '利用现有材料制作简易工具', 'material', 1, 3, 0.80, 1.20, 'friendly', 45, 0.90),

-- NPC 2: 猎人 - 可以提供狩猎帮助
(2, 'hunt', '协助狩猎', '帮助你追踪和捕获猎物', 'material', 5, 3, 0.80, 1.20, 'neutral', 45, 0.80),
(2, 'gather', '采集资源', '帮助你采集木材、草药等资源', 'material', 5, 2, 0.70, 1.10, 'unfriendly', 30, 0.95),

-- NPC 3: 医生 - 可以提供医疗帮助
(3, 'medical', '治疗伤病', '为你提供医疗救治', 'material', 5, 5, 0.80, 1.30, 'neutral', 20, 0.95),
(3, 'guide', '安全指引', '告诉你附近哪些区域比较安全', 'material', 5, 1, 0.50, 1.00, 'unfriendly', 15, 0.99),

-- NPC 4: 杰克·塔克 - 根据其身份添加合适的帮助
(4, 'craft', '武器维修', '帮助你修复损坏的武器', 'material', 1, 2, 0.80, 1.20, 'neutral', 30, 0.90),
(4, 'guide', '区域探索', '告诉你附近区域的情况', 'material', 5, 2, 0.70, 1.00, 'unfriendly', 20, 0.95)

ON DUPLICATE KEY UPDATE
    help_name = VALUES(help_name),
    help_description = VALUES(help_description),
    base_cost_type = VALUES(base_cost_type),
    base_cost_item_id = VALUES(base_cost_item_id),
    base_cost_quantity = VALUES(base_cost_quantity),
    cost_min_modifier = VALUES(cost_min_modifier),
    cost_max_modifier = VALUES(cost_max_modifier),
    min_favor_level = VALUES(min_favor_level),
    duration_minutes = VALUES(duration_minutes),
    success_rate = VALUES(success_rate);
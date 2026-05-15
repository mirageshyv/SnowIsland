USE snowisland;

DROP TABLE IF EXISTS catastrophe_progress;
CREATE TABLE catastrophe_progress (
    id INT AUTO_INCREMENT PRIMARY KEY,
    progress INT NOT NULL DEFAULT 0 COMMENT '天灾进度值 0-100',
    last_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_single_record (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='天灾进度表';

INSERT INTO catastrophe_progress (progress) VALUES (0);

DROP TABLE IF EXISTS catastrophe_card;
CREATE TABLE catastrophe_card (
    id INT AUTO_INCREMENT PRIMARY KEY,
    card_number INT NOT NULL UNIQUE COMMENT '卡牌编号',
    name VARCHAR(50) NOT NULL COMMENT '卡牌名称',
    description TEXT NOT NULL COMMENT '卡牌描述',
    effect_type VARCHAR(50) COMMENT '效果类型',
    effect_param1 INT DEFAULT 0 COMMENT '效果参数1',
    effect_param2 INT DEFAULT 0 COMMENT '效果参数2',
    effect_param3 VARCHAR(100) COMMENT '效果参数3（字符串）',
    is_unique BOOLEAN DEFAULT FALSE COMMENT '是否唯一卡牌',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='天灾牌表';

INSERT INTO catastrophe_card (card_number, name, description, effect_type, effect_param1, effect_param2, effect_param3, is_unique) VALUES
(1, '低温侵袭', '某处墙体结冰，寒气渗入。本天所有燃料消耗增加20%，木材消耗量15kg→18kg。', 'CONSUMPTION_INCREASE', 20, 15, '18', FALSE),
(2, '灾难蔓延', '增加5天暴雪持续时间', 'EXTEND_STORM', 5, 0, '', FALSE),
(3, '粮仓鼠患', '仓库中储存的粮食被老鼠啃食，损失10%的食物储备（向下取整）', 'FOOD_LOSS', 10, 0, '', FALSE),
(4, '燃料泄漏', '储油桶老化破裂，损失一处仓库的10%的燃料储备（优先扣除煤油/燃油）', 'FUEL_LOSS', 10, 0, '', FALSE),
(5, '工具锈蚀', '生产工具普遍老化。当天所有生产行动（渔猎、伐木、挖矿等）产量-20%。', 'PRODUCTION_DECREASE', 20, 0, '', FALSE),
(6, '海水倒灌', '风暴潮淹没码头设施，沿海仓库的部分物资被冲走（损失10%），方舟受损10%', 'DOCK_DAMAGE', 10, 10, '', FALSE),
(7, '水源污染', '岛上淡水水源被动物尸体污染，所有玩家当天需额外消耗1升煤油（烧开水）或面临患病风险', 'WATER_CONTAMINATION', 1, 0, '', FALSE),
(8, '信仰崩塌', '神父以及占卜师等精神领袖陷入自我怀疑，当天无法使用“布道”或“占星”技能。若第三天抽中不影响终局结算加成。', 'SKILL_DISABLE', 0, 0, '布道,占星', FALSE),
(9, '燃料受潮', '露天堆放的木柴被雨淋湿。随机一个仓库或玩家损失30kg木材。', 'WOOD_LOSS', 30, 0, '', FALSE),
(10, '逃役', '一名劳工趁夜色逃走了。统治者当天指定的劳工名单中，随机一人自动失效（不会劳作，也不会计入劳工）。主持人随机选择，不公开是谁。该玩家知道自己被逃役释放，当天正常进行行动。', 'ESCAPE_LABOR', 0, 0, '', FALSE),
(11, '祭品', '有人在教堂门口发现一只被割喉的黑羊。第二天必定触发一张额外天灾牌（命运在积蓄）。', 'EXTRA_CARD', 1, 0, '', FALSE);

DROP TABLE IF EXISTS catastrophe_deck;
CREATE TABLE catastrophe_deck (
    id INT AUTO_INCREMENT PRIMARY KEY,
    card_id INT NOT NULL COMMENT '天灾牌ID',
    is_drawn BOOLEAN DEFAULT FALSE COMMENT '是否已抽取',
    is_used BOOLEAN DEFAULT FALSE COMMENT '是否已使用',
    drawn_at DATETIME NULL COMMENT '抽取时间',
    used_at DATETIME NULL COMMENT '使用时间',
    round_used INT DEFAULT 0 COMMENT '使用回合',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (card_id) REFERENCES catastrophe_card(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='天灾牌组管理表';

INSERT INTO catastrophe_deck (card_id, is_drawn, is_used) VALUES
(1, FALSE, FALSE), (1, FALSE, FALSE), (1, FALSE, FALSE),
(2, FALSE, FALSE), (2, FALSE, FALSE), (2, FALSE, FALSE),
(3, FALSE, FALSE), (3, FALSE, FALSE), (3, FALSE, FALSE),
(4, FALSE, FALSE), (4, FALSE, FALSE), (4, FALSE, FALSE),
(5, FALSE, FALSE), (5, FALSE, FALSE), (5, FALSE, FALSE),
(6, FALSE, FALSE), (6, FALSE, FALSE), (6, FALSE, FALSE),
(7, FALSE, FALSE), (7, FALSE, FALSE), (7, FALSE, FALSE),
(8, FALSE, FALSE), (8, FALSE, FALSE), (8, FALSE, FALSE),
(9, FALSE, FALSE), (9, FALSE, FALSE), (9, FALSE, FALSE),
(10, FALSE, FALSE), (10, FALSE, FALSE), (10, FALSE, FALSE),
(11, FALSE, FALSE), (11, FALSE, FALSE), (11, FALSE, FALSE);

DROP TABLE IF EXISTS game_state;
CREATE TABLE game_state (
    id INT AUTO_INCREMENT PRIMARY KEY,
    current_day INT NOT NULL DEFAULT 1 COMMENT '当前天数',
    current_phase VARCHAR(20) NOT NULL DEFAULT 'DAY' COMMENT '当前阶段 DAY/NIGHT',
    is_game_over BOOLEAN DEFAULT FALSE COMMENT '游戏是否结束',
    catastrophe_triggered BOOLEAN DEFAULT FALSE COMMENT '天灾是否已触发',
    extra_card_due BOOLEAN DEFAULT FALSE COMMENT '是否有待触发的额外天灾牌',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_single_state (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏状态表';

INSERT INTO game_state (current_day, current_phase) VALUES (1, 'DAY');

DROP TABLE IF EXISTS selected_catastrophe;
CREATE TABLE selected_catastrophe (
    id INT AUTO_INCREMENT PRIMARY KEY,
    deck_id INT NOT NULL COMMENT '牌组ID',
    player_id INT NULL COMMENT '选择的玩家ID（天灾使者）',
    selected_at DATETIME NULL COMMENT '选择时间',
    is_active BOOLEAN DEFAULT FALSE COMMENT '是否正在生效',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (deck_id) REFERENCES catastrophe_deck(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES player(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='已选择的天灾牌表';

DROP TABLE IF EXISTS drawn_cards;
CREATE TABLE drawn_cards (
    id INT AUTO_INCREMENT PRIMARY KEY,
    draw_round INT NOT NULL COMMENT '抽取轮次',
    deck_id INT NOT NULL COMMENT '牌组ID',
    position INT NOT NULL COMMENT '位置（1-3）',
    is_selected BOOLEAN DEFAULT FALSE COMMENT '是否被选中',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (deck_id) REFERENCES catastrophe_deck(id) ON DELETE CASCADE,
    UNIQUE KEY uk_draw_position (draw_round, position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽取牌记录表';
CREATE TABLE IF NOT EXISTS island_event (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '事件ID',
    name VARCHAR(100) NOT NULL COMMENT '事件名称',
    description TEXT NOT NULL COMMENT '事件文本描述',
    triggered BOOLEAN DEFAULT FALSE COMMENT '事件触发状态标记',
    rarity VARCHAR(20) DEFAULT 'normal' COMMENT '事件稀有度：common/rare/epic',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岛屿事件主表';

CREATE TABLE IF NOT EXISTS island_event_reward (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '奖励ID',
    event_id INT NOT NULL COMMENT '事件ID',
    item_type VARCHAR(20) NOT NULL COMMENT '物资类型：item/weapon/ammo/material',
    item_id INT NOT NULL COMMENT '物资ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '物资数量',
    condition_desc VARCHAR(255) COMMENT '奖励获取条件描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (event_id) REFERENCES island_event(id) ON DELETE CASCADE,
    KEY idx_event_id (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岛屿事件奖励关联表';

CREATE TABLE IF NOT EXISTS player_exploration (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '探索记录ID',
    player_id INT NOT NULL COMMENT '玩家ID',
    game_day INT NOT NULL COMMENT '游戏天数',
    event_id INT COMMENT '触发的事件ID',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/explored/settled',
    result TEXT COMMENT '探索结果',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (player_id) REFERENCES player(id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES island_event(id) ON DELETE SET NULL,
    UNIQUE KEY uk_player_day (player_id, game_day),
    KEY idx_status (status),
    KEY idx_game_day (game_day)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家探索记录表';

INSERT INTO island_event (name, description, triggered, rarity) VALUES
('废弃营地', '你发现了一个废弃的幸存者营地，帐篷已经倒塌，但物资箱似乎完好无损。', FALSE, 'common'),
('沉船残骸', '海面上漂浮着一艘沉船的残骸，部分船舱还露在水面上，似乎有什么东西在闪闪发光。', FALSE, 'rare'),
('神秘洞穴', '在悬崖边上发现了一个隐蔽的洞穴入口，里面传来微弱的光芒。', FALSE, 'epic'),
('野生果园', '一片被冰雪覆盖的果园，虽然大部分果树都已枯萎，但仍有几棵顽强地挂着果实。', FALSE, 'common'),
('旧仓库', '一座废弃的仓库，门半掩着，里面似乎堆满了各种物资。', FALSE, 'common'),
('幸存者小屋', '一间简陋的木屋，烟囱里正冒着炊烟，门口站着一个警惕的老人。', FALSE, 'rare'),
('矿洞入口', '一个废弃的矿洞入口，里面漆黑一片，但地面上散落着一些矿石。', FALSE, 'rare'),
('海滩残骸', '暴风雨过后，海滩上冲来了许多漂浮物，其中夹杂着一些有用的物资。', FALSE, 'common');

INSERT INTO island_event_reward (event_id, item_type, item_id, quantity, condition_desc) VALUES
(1, 'material', 5, 20, '直接获得'),
(1, 'material', 8, 10, '直接获得'),
(1, 'item', 1, 1, '搜索仔细发现'),
(2, 'material', 1, 30, '直接获得'),
(2, 'material', 2, 15, '直接获得'),
(2, 'item', 8, 1, '深入船舱发现'),
(3, 'material', 7, 50, '直接获得'),
(3, 'item', 1, 2, '探索洞穴深处'),
(3, 'weapon', 1, 1, '运气极佳'),
(4, 'material', 5, 30, '直接获得'),
(4, 'material', 2, 10, '收集木材'),
(5, 'material', 1, 25, '直接获得'),
(5, 'material', 8, 20, '直接获得'),
(5, 'item', 2, 2, '搜索角落'),
(6, 'material', 5, 25, '友好交流'),
(6, 'item', 1, 3, '帮助老人'),
(6, 'material', 8, 15, '交易获得'),
(7, 'material', 7, 40, '直接获得'),
(7, 'material', 1, 20, '开采矿石'),
(8, 'material', 5, 15, '直接获得'),
(8, 'material', 8, 10, '直接获得'),
(8, 'item', 4, 2, '仔细搜寻');
CREATE TABLE IF NOT EXISTS milestone (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '里程碑名称',
    description TEXT NOT NULL COMMENT '里程碑描述',
    is_completed BOOLEAN DEFAULT FALSE COMMENT '是否已完成',
    completed_at DATETIME NULL COMMENT '完成时间',
    order_number INT NOT NULL COMMENT '排序序号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order (order_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='反抗者里程碑表';

INSERT INTO milestone (name, description, is_completed, order_number) VALUES
('团结平民', '星星之火也可以燎原。反抗者拥有一个初始协议书。反抗者应当与更多的玩家讨论，明确对方的特性，资源，加入阵营的意向，并且对其进行观察，辨别可能的内鬼。当包括初始反抗者在内的确认加入反抗者名单大于等于8人后（以签订了契约为准，以死亡玩家应也计算在内），该里程碑事件完成。', FALSE, 1),
('解放我们的同伴', '铁窗锁不住自由的心。目前在监狱里面的同伴都是曾经为了我们的革命事业献出自由乃是生命的同志。所以解放他们对于我们来讲至关重要。解救身在监狱中的那些同伴，他们将继续加入你们，并点燃这个岛上平民心中对于自由的渴望。当同伴被解救出来，该里程碑事件完成。', FALSE, 2),
('我们不是生来就应该如此', '在统治者使用审判环节，若被审判人员为加入反抗者阵营中人。要尽力解救我们的同胞，避免让他被统治者所残害。当被审判的反抗者人员无罪释放，该里程碑事件完成。', FALSE, 3),
('反抗不是我们的目的，平等才是', '不给人活路，那就掀桌子。你们或联系任意你们信任的人。向统治者进行施压，要求调整劳工名单或者让统治者分配一定的资源给其他镇民。当该施压投票超过半数被统治者同意，该里程碑事件完成。', FALSE, 4),
('正义属于我们', '敌人的刀，也能转过来对着他们自己。有1名原本属于统治者阵营的玩家（或主持人控制的NPC）主动投靠反抗者，并提供信息或协助。该里程碑事件完成。', FALSE, 5),
('团结一切可以团结的力量', '那些外来人，要么帮忙，要么别挡道。至少1名冒险者身份的玩家公开承诺在革命日当天加入反抗者起义，或至少2名冒险者承诺保持中立（不帮统治者）。需以契约为证。完成上述条件后，该里程碑事件完成。', FALSE, 6),
('让人民觉醒', '第一条血债，就是最好的宣言。当第一次有玩家因为统治者的直接行为（审判、冲突，抓捕等，由主持人判定）死亡或重伤后，主持人会秘密告知反抗者阵营这条消息。得知消息后，反抗者在当晚的夜间回合一名反叛者可以花费一行动点发起一次匿名投票，向全体玩家问一个问题：“统治者是否草菅人命？”投票规则：匿名投票，每人一票。选项为“是”或“否”。如果投票人数超过玩家总数的一半，且其中同意“是”的票数多于“否”，该里程碑完成。', FALSE, 7);

CREATE TABLE IF NOT EXISTS milestone_player_status (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL COMMENT '玩家ID',
    milestone_id INT NOT NULL COMMENT '里程碑ID',
    has_viewed BOOLEAN DEFAULT FALSE COMMENT '玩家是否已查看',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_player_milestone (player_id, milestone_id),
    FOREIGN KEY (milestone_id) REFERENCES milestone(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家里程碑查看状态表';
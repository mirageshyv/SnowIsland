-- 创建数据库
CREATE DATABASE IF NOT EXISTS snowisland CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE snowisland;

-- 创建职业表 (job)
CREATE TABLE IF NOT EXISTS job (
    id INT(11) PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    skills TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建个人技能表 (skill)
CREATE TABLE IF NOT EXISTS skill (
    id INT(11) PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    function TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建玩家表 (player)
CREATE TABLE IF NOT EXISTS player (
    id INT(11) PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    is_weak TINYINT(1) NOT NULL DEFAULT 0,
    is_overworked TINYINT(1) NOT NULL DEFAULT 0,
    is_injured TINYINT(1) NOT NULL DEFAULT 0,
    job_id INT(11) NOT NULL,
    skill_id INT(11) NULL,
    faction ENUM('统治者', '反叛者', '冒险者', '天灾使者', '平民') NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES job(id),
    FOREIGN KEY (skill_id) REFERENCES skill(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建用户表 (user)
CREATE TABLE IF NOT EXISTS user (
    id INT(11) PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role ENUM('DM', 'PLAYER') NOT NULL,
    player_id INT(11) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status TINYINT(1) NOT NULL DEFAULT 1,
    FOREIGN KEY (player_id) REFERENCES player(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入职业数据
INSERT INTO job (name, skills) VALUES
('战士', '近战攻击、防御、武器精通'),
('法师', '元素魔法、法术护盾、远程攻击'),
('盗贼', '潜行、开锁、背刺'),
('牧师', '治疗、祝福、神圣魔法'),
('猎人', '远程攻击、追踪、宠物驯养');

-- 插入个人技能数据
INSERT INTO skill (name, function) VALUES
('力量增强', '临时提高力量属性'),
('魔法抗性', '减少魔法伤害'),
('敏捷提升', '提高移动速度和闪避'),
('生命恢复', '缓慢恢复生命值'),
('幸运加成', '提高暴击率和掉落率'),
('洞察术', '发现隐藏的宝藏和陷阱'),
('冥想', '恢复魔法值'),
('鼓舞士气', '提高团队战斗力'),
('伪装', '融入环境避免被发现'),
('急救', '快速处理伤口');

-- 插入玩家数据
INSERT INTO player (name, is_weak, is_overworked, is_injured, job_id, skill_id, faction) VALUES
('阿尔伯特', 0, 0, 0, 1, 1, '统治者'),
('莉莉丝', 0, 1, 0, 2, 2, '反叛者'),
('罗宾', 1, 0, 0, 3, 3, '冒险者'),
('亚瑟', 0, 0, 1, 4, 4, '天灾使者'),
('艾米丽', 0, 0, 0, 5, 5, '平民');

-- 插入用户数据 (DM账号)
INSERT INTO user (username, password, role, player_id, status) VALUES
('dm1', 'test123', 'DM', NULL, 1),
('dm2', 'test123', 'DM', NULL, 1);

-- 插入用户数据 (玩家账号)
INSERT INTO user (username, password, role, player_id, status) VALUES
('player1', 'test123', 'PLAYER', 1, 1),
('player2', 'test123', 'PLAYER', 2, 1),
('player3', 'test123', 'PLAYER', 3, 1),
('player4', 'test123', 'PLAYER', 4, 1),
('player5', 'test123', 'PLAYER', 5, 1);

-- 初始化用户账号
-- DM账号
INSERT INTO `user` (username, password, role, player_id, status) VALUES
('dm', 'dm123', 'DM', NULL, 1);

-- 玩家测试账号（如果player表有数据）
INSERT INTO `user` (username, password, role, player_id, status)
SELECT 'player', 'test123', 'PLAYER', id, 1 FROM `player` LIMIT 1;

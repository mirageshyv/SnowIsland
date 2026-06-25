-- 岛屿事件难度字段迁移脚本
-- 为 island_event 表添加 event_difficulty 字段，int类型，范围0-20
-- MySQL 5.7+ / 8.0 兼容

-- 1. 添加 event_difficulty 字段
ALTER TABLE island_event
    ADD COLUMN event_difficulty INT NOT NULL DEFAULT 5
    COMMENT '事件难度(0-20, 0=最简单的捡垃圾，20=极限危险)';

-- 2. 添加CHECK约束（MySQL 8.0+支持；5.7需要在应用层校验）
-- MySQL 8.0+
ALTER TABLE island_event
    ADD CONSTRAINT chk_event_difficulty CHECK (event_difficulty BETWEEN 0 AND 20);

-- 3. 为现有事件设置合理的难度值
-- 难度分配规则：
--   common(普通)  -> 难度 1-5
--   rare(稀有)    -> 难度 6-12
--   epic(史诗)    -> 难度 13-20
UPDATE island_event SET event_difficulty = 2  WHERE id = 1 AND name = '废弃营地';
UPDATE island_event SET event_difficulty = 4  WHERE id = 2 AND name = '沉船残骸';
UPDATE island_event SET event_difficulty = 15 WHERE id = 3 AND name = '神秘洞穴';
UPDATE island_event SET event_difficulty = 1  WHERE id = 4 AND name = '野生果园';
UPDATE island_event SET event_difficulty = 3  WHERE id = 5 AND name = '旧仓库';
UPDATE island_event SET event_difficulty = 8  WHERE id = 6 AND name = '幸存者小屋';
UPDATE island_event SET event_difficulty = 10 WHERE id = 7 AND name = '矿洞入口';
UPDATE island_event SET event_difficulty = 2  WHERE id = 8 AND name = '海滩残骸';

-- 4. 数据兜底：所有未匹配上的事件统一设置为默认难度5
UPDATE island_event SET event_difficulty = 5 WHERE event_difficulty IS NULL OR event_difficulty < 0 OR event_difficulty > 20;

-- 5. 验证迁移结果
SELECT id, name, rarity, event_difficulty FROM island_event ORDER BY event_difficulty;
-- =============================================
-- SnowIsland NPC数据表（重新设计）
-- =============================================

USE snowisland;

DROP TABLE IF EXISTS location_npc;

CREATE TABLE location_npc (
  id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'NPC唯一标识符',
  name VARCHAR(50) NOT NULL COMMENT 'NPC名字',
  job VARCHAR(50) NOT NULL COMMENT 'NPC职业',
  gender ENUM('男','女') NOT NULL COMMENT '性别',
  introduction TEXT COMMENT 'NPC介绍',
  location_id INT NOT NULL COMMENT '所在地点ID',
  attitude_ruler ENUM('喜好','厌恶','忽视') NOT NULL DEFAULT '忽视' COMMENT '对统治者的态度',
  attitude_rebel ENUM('喜好','厌恶','忽视') NOT NULL DEFAULT '忽视' COMMENT '对反叛者的态度',
  attitude_adventurer ENUM('喜好','厌恶','忽视') NOT NULL DEFAULT '忽视' COMMENT '对冒险者的态度',
  attitude_scourge ENUM('喜好','厌恶','忽视') NOT NULL DEFAULT '忽视' COMMENT '对天灾使者的态度',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_location_id (location_id),
  INDEX idx_job (job),
  CONSTRAINT fk_npc_location FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地点NPC表';

-- =============================================
-- 插入NPC数据
-- =============================================

-- 码头 (location_id = 7)
INSERT INTO location_npc (name, job, gender, introduction, location_id, attitude_ruler, attitude_rebel, attitude_adventurer, attitude_scourge) VALUES
('克拉拉·南丁格尔', '渔民', '女', '一位家中贫困的普通渔民，只希望镇上保持平静。', 7, '忽视', '忽视', '喜好', '厌恶'),
('杰克·塔克', '水手', '男', '曾在商船当水手，船沉后困在岛上，做梦都想再上一次船。', 7, '忽视', '厌恶', '喜好', '忽视'),
('鲍勃·塔克', '装卸工', '男', '一名一直在港口讨生活的搬运工。', 7, '喜好', '厌恶', '忽视', '忽视');

-- 伐木营地 (location_id = 15)
INSERT INTO location_npc (name, job, gender, introduction, location_id, attitude_ruler, attitude_rebel, attitude_adventurer, attitude_scourge) VALUES
('托马斯·伍德', '伐木工', '男', '沉默寡言的伐木工，靠砍树和做木工为生，只求安稳度日。', 15, '喜好', '厌恶', '忽视', '忽视');

-- 矿场 (location_id = 18)
INSERT INTO location_npc (name, job, gender, introduction, location_id, attitude_ruler, attitude_rebel, attitude_adventurer, attitude_scourge) VALUES
('卡尔·铁锤', '矿工', '男', '脾气火爆的矿场工人，谁给好处就帮谁。', 18, '喜好', '厌恶', '忽视', '厌恶'),
('维克多·斯通', '矿工', '男', '体格强壮的矿工，相信权力才是活下去的依靠。', 18, '喜好', '厌恶', '忽视', '厌恶');

-- 集市 (location_id = 10)
INSERT INTO location_npc (name, job, gender, introduction, location_id, attitude_ruler, attitude_rebel, attitude_adventurer, attitude_scourge) VALUES
('塞缪尔·格雷', '农户', '男', '善良而质朴的普通农户，乐于帮助他人。', 10, '厌恶', '忽视', '喜好', '忽视'),
('弗雷德里克·波特', '农户', '男', '性格孤僻的，住在镇外，对别人的生死毫不在意。', 10, '厌恶', '喜好', '忽视', '忽视'),
('米玛·雷铁斯托', '手工艺人', '女', '老实本分的手工艺人，喜欢待在自己的小屋偶尔出门。', 10, '厌恶', '忽视', '喜好', '忽视'),
('汉斯·施密特', '工匠', '男', '什么都能修的工匠，从钟表到农具都难不倒他，只认工钱不认人。', 10, '喜好', '忽视', '忽视', '厌恶');

-- 监狱 (location_id = 19)
INSERT INTO location_npc (name, job, gender, introduction, location_id, attitude_ruler, attitude_rebel, attitude_adventurer, attitude_scourge) VALUES
('乔克·汤姆', '民兵', '男', '初始就跟着统治者干的监狱看守，一名很忠诚的下属。只是他有点小小的缺点，但统治者们也只能视而不见。', 19, '喜好', '厌恶', '忽视', '厌恶');

-- =============================================
-- SnowIsland NPC数据表（开放 12 人，含人设与对话风格）
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
  avatar_url VARCHAR(255) DEFAULT NULL,
  dialogue_style TEXT COMMENT 'AI 对话风格提示词',
  personality TEXT COMMENT '性格',
  status VARCHAR(50) DEFAULT '正常',
  daily_trade_limit INT DEFAULT 1,
  clue_keywords TEXT,
  special_clue_content TEXT,
  INDEX idx_location_id (location_id),
  INDEX idx_job (job),
  CONSTRAINT fk_npc_location FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地点NPC表';

-- 码头 (location_id = 7)
INSERT INTO location_npc (name, job, gender, introduction, personality, dialogue_style, location_id, attitude_ruler, attitude_rebel, attitude_adventurer, attitude_scourge, status, daily_trade_limit) VALUES
('克拉拉·南丁格尔', '渔民', '女', '家中贫困的普通渔民，只希望镇上保持平静。她每天修补渔网，数着日子过，祈祷暴雪别落到她头上。', '胆怯温和，只想安稳过日子。', '声音小，经常低头说话，提到“暴雪”时会下意识攥紧衣角。', 7, '忽视', '忽视', '喜好', '厌恶', '正常', 1),
('杰克·塔克', '水手', '男', '曾在商船当水手，船沉后困在岛上，做梦都想再上一次船。他每天在码头踱步，盯着海平线发呆。', '焦躁不安，像被困在笼子里的海鸟。', '说话带着海风味的粗犷，每三句话里有一句是“等船来了我就走”。', 7, '忽视', '厌恶', '喜好', '忽视', '正常', 1),
('鲍勃·塔克', '装卸工', '男', '一直在港口讨生活的搬运工，膀大腰圆，嘴里永远叼着半根烟。他谁都不信，只信工钱和拳头。', '粗鲁务实，谁给钱多就给谁干活。', '话不多，用“嗯”“行”“钱呢”三个词就能完成一次交易。', 7, '喜好', '厌恶', '忽视', '忽视', '正常', 1);

-- 伐木营地 (location_id = 15)
INSERT INTO location_npc (name, job, gender, introduction, personality, dialogue_style, location_id, attitude_ruler, attitude_rebel, attitude_adventurer, attitude_scourge, status, daily_trade_limit) VALUES
('托马斯·伍德', '伐木工', '男', '沉默寡言的伐木工，靠砍树和做木工为生，只求安稳度日。他住在林中小屋，镇上的人很少见到他。', '孤僻寡言，对政治毫无兴趣。', '答话永远慢半拍，好像你说话他要先翻译成木头语言。使用单音节词为主。', 15, '喜好', '厌恶', '忽视', '忽视', '正常', 1);

-- 矿场 (location_id = 18)
INSERT INTO location_npc (name, job, gender, introduction, personality, dialogue_style, location_id, attitude_ruler, attitude_rebel, attitude_adventurer, attitude_scourge, status, daily_trade_limit) VALUES
('卡尔·铁锤', '矿工', '男', '脾气火爆的矿场工人，谁给好处就帮谁。他嗓门大、拳头硬，在矿场里混得开，没人敢惹他。', '暴躁直率，利益至上，不在乎对错。', '说话像在吼，每句话都带脏字，谈到“好处”时眼睛会亮起来。', 18, '喜好', '厌恶', '忽视', '厌恶', '正常', 1),
('维克多·斯通', '矿工', '男', '体格强壮的矿工，相信权力才是活下去的依靠。他崇拜强者，认为统治者镇得住场子，镇上才不至于乱套。', '务实忠诚，迷信权力，看不起弱者。', '说话简短有力，像在砸石头。提到“统治者”时语气会放尊重点。', 18, '喜好', '厌恶', '忽视', '厌恶', '正常', 1);

-- 集市 (location_id = 10)
INSERT INTO location_npc (name, job, gender, introduction, personality, dialogue_style, location_id, attitude_ruler, attitude_rebel, attitude_adventurer, attitude_scourge, status, daily_trade_limit) VALUES
('塞缪尔·格雷', '农户', '男', '善良而质朴的普通农户，乐于帮助他人。他种的菜总有多余的分给邻居，从不计较回报。', '温和宽厚，发自内心地相信善良。', '语速舒缓，像在慢悠悠地翻土。喜欢用“我总觉得啊”开头，但从不强加观点。', 10, '厌恶', '忽视', '喜好', '忽视', '正常', 1),
('弗雷德里克·波特', '农户', '男', '性格孤僻的住在镇外的农户，对别人的生死毫不在意。他种自己的地，吃自己的粮，从不参与镇上任何事。', '冷漠自私，独来独往，不关心任何人。', '能用点头摇头解决的绝不开腔，开了腔也是“关我什么事”。', 10, '厌恶', '喜好', '忽视', '忽视', '正常', 1),
('米玛·雷铁斯托', '手工艺人', '女', '老实本分的手工艺人，喜欢待在自己的小屋偶尔出门。她编篮子、织布，手艺好但不爱张扬。', '安静本分，不惹事也不怕事。', '说话轻声细语，手上永远在忙活——编东西、缝东西、磨东西。句子短，不议论人。', 10, '厌恶', '忽视', '喜好', '忽视', '正常', 1),
('汉斯·施密特', '工匠', '男', '什么都能修的工匠，从钟表到农具都难不倒他，只认工钱不认人。他修东西时从不说话，修完报价，拿钱走人。', '理性冷漠，技术至上，人情淡薄。', '除非在谈工钱，否则不开口。谈价格时句句精准，一句废话没有。', 10, '喜好', '忽视', '忽视', '厌恶', '正常', 1);

-- 监狱 (location_id = 19)
INSERT INTO location_npc (name, job, gender, introduction, personality, dialogue_style, location_id, attitude_ruler, attitude_rebel, attitude_adventurer, attitude_scourge, status, daily_trade_limit) VALUES
('乔克·汤姆', '民兵', '男', '初始就跟着统治者干的监狱看守，一名很忠诚的下属。只是他有点小小的缺点，但统治者们也只能视而不见。', '忠诚但管不住嘴，有小毛病但不致命。', '话多，喜欢吹嘘自己和统治者的关系，说到一半会突然压低声音说“其实我告诉你个小秘密”。', 19, '喜好', '厌恶', '忽视', '厌恶', '正常', 1),
('斯特·贝斯', '民兵', '女', '初始就跟着统治者干活的一名很忠心的下属。她会一直遵从统治者的决定，除非她看不到希望。', '忠诚可靠，但有自己的底线。', '话少，回答简洁。提到“统治者”时语气恭敬，提到“暴雪”时语气会变犹豫。', 19, '喜好', '厌恶', '忽视', '厌恶', '正常', 1);

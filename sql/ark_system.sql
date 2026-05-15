-- =====================================================
-- 方舟系统数据库表结构
-- SnowIsland 方舟建造与航行模块
-- =====================================================

SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS `ark_construction_log`;
DROP TABLE IF EXISTS `ark_voyage`;
DROP TABLE IF EXISTS `ark_required_skill`;
DROP TABLE IF EXISTS `ark_sail`;
DROP TABLE IF EXISTS `ark_construction`;
DROP TABLE IF EXISTS `ark_config`;
SET FOREIGN_KEY_CHECKS=1;

-- =====================================================
-- Table 1: ark_config（方舟配置表）
-- 存储方舟全局配置信息
-- =====================================================
CREATE TABLE `ark_config` (
  `id` INT NOT NULL DEFAULT 1 COMMENT '单例配置，固定为1',

  -- 基础建设目标
  `target_wood` DECIMAL(10,2) NOT NULL DEFAULT 250.00 COMMENT '目标木材(吨)',
  `target_metal` DECIMAL(10,2) NOT NULL DEFAULT 100.00 COMMENT '目标金属制品(吨)',
  `target_asphalt` DECIMAL(10,2) NOT NULL DEFAULT 100.00 COMMENT '目标沥青(kg)',

  -- 基础载重配置
  `base_capacity` INT NOT NULL DEFAULT 50 COMMENT '基础载重点数',

  -- 每日投入上限
  `daily_wood_limit` DECIMAL(10,2) NOT NULL DEFAULT 30.00 COMMENT '每日木材投入上限(吨)',
  `daily_metal_limit` DECIMAL(10,2) NOT NULL DEFAULT 20.00 COMMENT '每日金属投入上限(吨)',
  `daily_asphalt_limit` DECIMAL(10,2) NOT NULL DEFAULT 20.00 COMMENT '每日沥青投入上限(kg)',

  -- 工作推进配置（材料不足时）
  `work_wood_per_unit` DECIMAL(5,2) NOT NULL DEFAULT 5.00 COMMENT '每工作单位消耗木材(吨)',
  `work_metal_per_unit` DECIMAL(5,2) NOT NULL DEFAULT 5.00 COMMENT '每工作单位消耗金属(吨)',
  `work_asphalt_per_unit` DECIMAL(5,2) NOT NULL DEFAULT 5.00 COMMENT '每工作单位消耗沥青(kg)',

  -- 出航物资配置（每载重点）
  `food_per_capacity` INT NOT NULL DEFAULT 100 COMMENT '每载重点所需食物(单位)',
  `fuel_per_capacity` DECIMAL(5,2) NOT NULL DEFAULT 2.00 COMMENT '每载重点所需燃料(吨)',
  `sealant_per_capacity` DECIMAL(5,2) NOT NULL DEFAULT 500.00 COMMENT '每载重点所需密封材料(kg)',
  `sail_wood_per_capacity` DECIMAL(5,2) NOT NULL DEFAULT 2.00 COMMENT '每载重点所需木材(吨)',

  -- 航行时间配置（按发动机数量）
  `sail_days_0_engine` INT NOT NULL DEFAULT 10 COMMENT '0发动机航行天数',
  `sail_days_1_engine` INT NOT NULL DEFAULT 8 COMMENT '1发动机航行天数',
  `sail_days_2_engine` INT NOT NULL DEFAULT 6 COMMENT '2发动机航行天数',
  `sail_days_3_engine` INT NOT NULL DEFAULT 4 COMMENT '3发动机航行天数',

  -- 帆的航行时间加成
  `sail_days_with_sail_0_engine` INT NOT NULL DEFAULT 10 COMMENT '有帆0发动机航行天数',
  `sail_days_with_sail_1_engine` INT NOT NULL DEFAULT 7 COMMENT '有帆1发动机航行天数',

  -- 帆建造材料需求
  `sail_rope_required` DECIMAL(10,2) NOT NULL DEFAULT 100.00 COMMENT '帆所需绳索(米)',
  `sail_canvas_required` DECIMAL(10,2) NOT NULL DEFAULT 80.00 COMMENT '帆所需帆布(米)',

  -- 游戏状态
  `current_game_day` INT NOT NULL DEFAULT 1 COMMENT '当前游戏天数',
  `ark_status` VARCHAR(20) NOT NULL DEFAULT 'building' COMMENT '方舟状态:building/preparing/sailing/completed',

  -- 惩罚规则
  `shortage_capacity_penalty` INT NOT NULL DEFAULT 3 COMMENT '短缺资源减少载重点数',
  `shortage_completion_penalty` DECIMAL(4,2) NOT NULL DEFAULT 2.50 COMMENT '短缺资源减少完成度百分比',

  -- 初始建设（根据参与人数）
  `initial_wood` DECIMAL(10,2) NOT NULL DEFAULT 10.00 COMMENT '初始已建木材(吨)',
  `initial_metal` DECIMAL(10,2) NOT NULL DEFAULT 20.00 COMMENT '初始已建金属(吨)',

  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='方舟全局配置表';

-- 插入默认配置数据
INSERT INTO `ark_config` (`id`, `target_wood`, `target_metal`, `target_asphalt`, `base_capacity`,
`daily_wood_limit`, `daily_metal_limit`, `daily_asphalt_limit`,
`work_wood_per_unit`, `work_metal_per_unit`, `work_asphalt_per_unit`,
`food_per_capacity`, `fuel_per_capacity`, `sealant_per_capacity`, `sail_wood_per_capacity`,
`sail_days_0_engine`, `sail_days_1_engine`, `sail_days_2_engine`, `sail_days_3_engine`,
`sail_days_with_sail_0_engine`, `sail_days_with_sail_1_engine`,
`sail_rope_required`, `sail_canvas_required`, `current_game_day`, `ark_status`,
`shortage_capacity_penalty`, `shortage_completion_penalty`, `initial_wood`, `initial_metal`)
VALUES (1, 250.00, 100.00, 100.00, 50, 30.00, 20.00, 20.00, 5.00, 5.00, 5.00, 100, 2.00, 500.00, 2.00, 10, 8, 6, 4, 10, 7, 100.00, 80.00, 1, 'building', 3, 2.50, 10.00, 20.00);

-- =====================================================
-- Table 2: ark_construction（方舟建设进度表）
-- 记录方舟各项建设内容的完成进度
-- =====================================================
CREATE TABLE `ark_construction` (
  `id` INT NOT NULL AUTO_INCREMENT,

  -- 建设阶段
  `stage_code` VARCHAR(30) NOT NULL COMMENT '阶段代码:foundation/frame/hull/deck/final',
  `stage_name` VARCHAR(50) NOT NULL COMMENT '阶段名称',
  `stage_description` VARCHAR(255) COMMENT '阶段描述',

  -- 进度触发点（完成度百分比）
  `trigger_progress` INT NOT NULL COMMENT '触发此阶段的完成度门槛',

  -- 该阶段资源需求
  `required_wood` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '本阶段所需木材(吨)',
  `required_metal` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '本阶段所需金属(吨)',
  `required_asphalt` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '本阶段所需沥青(kg)',

  -- 该阶段累计投入
  `invested_wood` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已投入木材(吨)',
  `invested_metal` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已投入金属(吨)',
  `invested_asphalt` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已投入沥青(kg)',

  -- 该阶段工作推进
  `work_units_completed` INT NOT NULL DEFAULT 0 COMMENT '已完成工作单位',
  `work_units_required` INT NOT NULL DEFAULT 0 COMMENT '所需工作单位',

  -- 核心部件需求
  `required_engines` INT NOT NULL DEFAULT 0 COMMENT '所需发动机数量',
  `required_propellers` INT NOT NULL DEFAULT 0 COMMENT '所需螺旋桨数量',
  `required_generators` INT NOT NULL DEFAULT 0 COMMENT '所需发电机数量',

  -- 状态
  `is_completed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否完成此阶段',
  `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',

  -- 阶段顺序
  `display_order` INT NOT NULL DEFAULT 1 COMMENT '显示顺序',

  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stage_code` (`stage_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='方舟建设进度表';

-- 插入5个建设阶段
INSERT INTO `ark_construction` (`stage_code`, `stage_name`, `stage_description`, `trigger_progress`,
`required_wood`, `required_metal`, `required_asphalt`, `invested_wood`, `invested_metal`, `invested_asphalt`,
`work_units_completed`, `work_units_required`, `required_engines`, `required_propellers`, `required_generators`,
`is_completed`, `display_order`) VALUES
('foundation', '底部龙骨', '底部龙骨与基础结构已完成，船体轮廓初步形成', 30, 50, 20, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
('frame', '基本框架', '船舷与主舱完成，具备勉强出航能力', 50, 80, 40, 40, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2),
('hull', '船壳完成', '加装护舷和附属空间，稳定性显著提升', 70, 60, 25, 25, 0, 0, 0, 0, 0, 0, 2, 0, 0, 3),
('deck', '甲板铺设', '甲板与舱室进一步完善，物资承载能力增强', 90, 40, 10, 10, 0, 0, 0, 0, 0, 0, 0, 1, 0, 4),
('final', '方舟完工', '方舟全面完工，可承载人员和长期补给', 100, 20, 5, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5);

-- =====================================================
-- Table 3: ark_required_skill（方舟必需技能表）
-- 存储建造方舟所需的技能信息
-- =====================================================
CREATE TABLE `ark_required_skill` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `skill_code` VARCHAR(50) NOT NULL COMMENT '技能代码',
  `skill_name` VARCHAR(100) NOT NULL COMMENT '技能名称',
  `skill_description` VARCHAR(255) COMMENT '技能描述',
  `skill_type` VARCHAR(20) NOT NULL COMMENT '技能类型:construction/voyage/endgame',
  `effect_bonus` INT NOT NULL DEFAULT 0 COMMENT '效果加成分数',
  `is_required` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为必需技能',
  `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_skill_code` (`skill_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='方舟必需技能表';

-- 插入技能数据
INSERT INTO `ark_required_skill` (`skill_code`, `skill_name`, `skill_description`, `skill_type`, `effect_bonus`, `is_required`, `priority`) VALUES
('sailing', '航海', '基础航海技能，用于出航', 'voyage', 5, 0, 1),
('navigation', '海洋导航', '精确定位航线，提供更好的结局倾向', 'endgame', 10, 0, 2),
('weather_forecast', '天气预报', '可查看所有天灾牌', 'endgame', 8, 0, 3),
('fishing', '渔猎', '航行中可补充食物', 'voyage', 5, 0, 4),
('construction', '建造', '加速方舟建造进度', 'construction', 10, 0, 5);

-- =====================================================
-- Table 4: ark_sail（帆配置表）
-- 保存帆的相关配置数据
-- =====================================================
CREATE TABLE `ark_sail` (
  `id` INT NOT NULL DEFAULT 1 COMMENT '单例帆配置，固定为1',

  -- 帆建造状态
  `is_built` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '帆是否已建造',
  `built_by_player_id` INT DEFAULT NULL COMMENT '建造者玩家ID',
  `built_at_game_day` INT DEFAULT NULL COMMENT '建造日(游戏天数)',

  -- 帆的效果
  `effect_0_engine_days` INT NOT NULL DEFAULT 10 COMMENT '无发动机时航行天数',
  `effect_1_engine_days` INT NOT NULL DEFAULT 7 COMMENT '1发动机时航行天数',

  -- 建造材料需求（固定值）
  `required_rope` DECIMAL(10,2) NOT NULL DEFAULT 100.00 COMMENT '所需绳索(米)',
  `required_canvas` DECIMAL(10,2) NOT NULL DEFAULT 80.00 COMMENT '所需帆布(米)',

  -- 已收集材料
  `collected_rope` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已收集绳索(米)',
  `collected_canvas` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已收集帆布(米)',

  -- 建造进度
  `construction_progress` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '建造进度(0-100%)',

  -- 帆的状态
  `condition_status` VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '状态:normal/damaged/destroyed',
  `last_repaired_day` INT DEFAULT NULL COMMENT '最后修复日',

  -- 建造所需工作
  `requires_work_action` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '建造是否需要工作行动',
  `work_action_player_id` INT DEFAULT NULL COMMENT '执行建造工作的玩家ID',
  `is_work_completed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '工作是否完成',

  -- 时间戳
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帆配置表';

-- 插入默认帆配置
INSERT INTO `ark_sail` (`id`, `is_built`, `effect_0_engine_days`, `effect_1_engine_days`,
`required_rope`, `required_canvas`, `collected_rope`, `collected_canvas`,
`construction_progress`, `condition_status`, `requires_work_action`, `is_work_completed`)
VALUES (1, 0, 10, 7, 100.00, 80.00, 0, 0, 0, 'normal', 1, 0);

-- =====================================================
-- Table 5: ark_voyage（航行记录表）
-- 保存航行相关数据
-- =====================================================
CREATE TABLE `ark_voyage` (
  `id` INT NOT NULL AUTO_INCREMENT,

  -- 航行基本信息
  `voyage_number` INT NOT NULL COMMENT '航行次数(第几次航行)',
  `departure_day` INT NOT NULL COMMENT '出发日(游戏天数)',
  `planned_return_day` INT COMMENT '计划返回日',
  `actual_return_day` INT COMMENT '实际返回日',

  -- 航行配置
  `engine_count` INT NOT NULL DEFAULT 0 COMMENT '发动机数量(0-3)',
  `has_sail` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否有帆',
  `has_generator` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否有发电机',
  `propeller_count` INT NOT NULL DEFAULT 0 COMMENT '螺旋桨数量',

  -- 航行时间计算
  `base_sail_days` INT NOT NULL COMMENT '基础航行天数',
  `final_sail_days` INT NOT NULL COMMENT '最终航行天数(含危机事件)',

  -- 载重与人员
  `total_capacity` INT NOT NULL DEFAULT 0 COMMENT '总载重',
  `passenger_count` INT NOT NULL DEFAULT 0 COMMENT '登船人数',
  `passenger_list` TEXT COMMENT '登船玩家ID列表(JSON)',

  -- 出航物资
  `food_loaded` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '装载食物(单位)',
  `fuel_loaded` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '装载燃料(吨)',
  `sealant_loaded` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '装载密封材料(kg)',
  `wood_loaded` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '装载木材(吨)',

  -- 航行状态
  `status` VARCHAR(20) NOT NULL DEFAULT 'preparing' COMMENT '状态:preparing/departed/encountering_crisis/returned/lost',
  `current_day_offset` INT NOT NULL DEFAULT 0 COMMENT '当前航行天数偏移',

  -- 危机事件记录
  `crisis_events` TEXT COMMENT '遭遇的危机事件(JSON数组)',
  `crisis_count` INT NOT NULL DEFAULT 0 COMMENT '危机事件数量',

  -- 技能加成
  `has_navigator` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否有导航技能',
  `has_weather_watcher` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否有天气预报技能',
  `has_fisher` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否有渔猎技能',

  -- 终局结算相关
  `voyage_result` VARCHAR(50) COMMENT '航行结果:success/partial_failure/failure',
  `final_bonus` INT DEFAULT 0 COMMENT '终局加成分数',
  `notes` TEXT COMMENT '主持人备注',

  -- 时间戳
  `departed_at` DATETIME DEFAULT NULL COMMENT '实际出发时间',
  `returned_at` DATETIME DEFAULT NULL COMMENT '实际返回时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='航行记录表';

-- =====================================================
-- Table 6: ark_construction_log（建设日志表）
-- 记录建设过程中的关键操作日志
-- =====================================================
CREATE TABLE `ark_construction_log` (
  `id` INT NOT NULL AUTO_INCREMENT,

  -- 玩家信息
  `player_id` INT NOT NULL COMMENT '操作玩家ID',
  `player_name` VARCHAR(50) DEFAULT NULL COMMENT '玩家名称(冗余存储)',

  -- 游戏时间
  `game_day` INT NOT NULL COMMENT '操作时的游戏天数',

  -- 操作类型
  `action_type` VARCHAR(30) NOT NULL COMMENT '操作类型:invest/work/install/build_sail/prepare_supplies',

  -- 操作详情
  `action_description` VARCHAR(255) NOT NULL COMMENT '操作描述',

  -- 资源变更
  `wood_change` DECIMAL(10,2) DEFAULT 0 COMMENT '木材变化(吨)',
  `metal_change` DECIMAL(10,2) DEFAULT 0 COMMENT '金属变化(吨)',
  `asphalt_change` DECIMAL(10,2) DEFAULT 0 COMMENT '沥青变化(kg)',

  -- 工作推进
  `work_units` INT DEFAULT 0 COMMENT '投入工作单位',

  -- 部件变更
  `engine_installed` INT DEFAULT 0 COMMENT '安装发动机数',
  `propeller_installed` INT DEFAULT 0 COMMENT '安装螺旋桨数',
  `generator_installed` INT DEFAULT 0 COMMENT '安装发电机数',

  -- 建设阶段变更
  `previous_stage` VARCHAR(30) DEFAULT NULL COMMENT '之前阶段',
  `current_stage` VARCHAR(30) DEFAULT NULL COMMENT '当前阶段',

  -- 载重与完成度变更
  `previous_capacity` INT DEFAULT NULL COMMENT '之前载重',
  `current_capacity` INT DEFAULT NULL COMMENT '当前载重',
  `previous_completion` DECIMAL(5,2) DEFAULT NULL COMMENT '之前完成度%',
  `current_completion` DECIMAL(5,2) DEFAULT NULL COMMENT '当前完成度%',

  -- 附加影响
  `capacity_change` INT DEFAULT 0 COMMENT '载重变化',
  `completion_change` DECIMAL(5,2) DEFAULT 0 COMMENT '完成度变化%',

  -- 帆相关
  `sail_built` TINYINT(1) DEFAULT 0 COMMENT '是否建造了帆',
  `sail_rope_used` DECIMAL(10,2) DEFAULT 0 COMMENT '使用绳索(米)',
  `sail_canvas_used` DECIMAL(10,2) DEFAULT 0 COMMENT '使用帆布(米)',

  -- 时间戳
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),
  KEY `idx_player_day` (`player_id`, `game_day`),
  KEY `idx_action_type` (`action_type`),
  KEY `idx_game_day` (`game_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='方舟建设日志表';

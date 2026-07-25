-- ============================================
-- SnowIsland 数据库初始化脚本
-- ============================================
-- 此脚本在 MySQL 容器首次启动时自动执行
-- 注意：表创建顺序很重要，请勿随意调整

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -------------------------------------------
-- 基础表结构
-- -------------------------------------------

-- 用户相关表
source /docker-entrypoint-initdb.d/sql/rule_book.sql;
source /docker-entrypoint-initdb.d/sql/location_database.sql;
source /docker-entrypoint-initdb.d/sql/location_npc_data.sql;
source /docker-entrypoint-initdb.d/sql/warehouse_database.sql;
source /docker-entrypoint-initdb.d/sql/init_material_system.sql;
source /docker-entrypoint-initdb.d/sql/init_job_data.sql;
source /docker-entrypoint-initdb.d/sql/init_job_initial_items.sql;
source /docker-entrypoint-initdb.d/sql/init_skill_data.sql;
source /docker-entrypoint-initdb.d/sql/init_trade_system.sql;

-- 玩家相关表
source /docker-entrypoint-initdb.d/sql/ark_system.sql;
source /docker-entrypoint-initdb.d/sql/ark_construction_schema.sql;
source /docker-entrypoint-initdb.d/sql/action_database.sql;
source /docker-entrypoint-initdb.d/sql/milestone_database.sql;
source /docker-entrypoint-initdb.d/sql/faction_action_database.sql;
source /docker-entrypoint-initdb.d/sql/catastrophe_database.sql;
source /docker-entrypoint-initdb.d/sql/shelter_stock_data.sql;
source /docker-entrypoint-initdb.d/sql/player_status_consumption_labor.sql;
source /docker-entrypoint-initdb.d/sql/shelter_daily_labor_migration.sql;
source /docker-entrypoint-initdb.d/sql/fix_shelter_labor_reserved_columns.sql;

-- 快速交互系统
source /docker-entrypoint-initdb.d/sql/quick_interaction.sql;

-- NPC 对话系统
source /docker-entrypoint-initdb.d/sql/npc_dialogue_system.sql;
source /docker-entrypoint-initdb.d/sql/npc_dialogue_limit.sql;

-- NPC 帮助系统
source /docker-entrypoint-initdb.d/sql/npc_help_system.sql;

-- NPC 交易系统
source /docker-entrypoint-initdb.d/sql/npc_trade_system.sql;

-- 岛屿探索系统
source /docker-entrypoint-initdb.d/sql/create_island_event_tables.sql;
source /docker-entrypoint-initdb.d/sql/insert_endgame_events.sql;

-- 终局结算系统
source /docker-entrypoint-initdb.d/sql/rule_book_optimized.sql;
source /docker-entrypoint-initdb.d/sql/rule_book_data.sql;

-- -------------------------------------------
-- 数据初始化
-- -------------------------------------------
source /docker-entrypoint-initdb.d/sql/init_database.sql;
source /docker-entrypoint-initdb.d/sql/snowisland.sql;
source /docker-entrypoint-initdb.d/sql/snowisland_5_14.sql;
source /docker-entrypoint-initdb.d/sql/snowisland_5_15.sql;
source /docker-entrypoint-initdb.d/sql/snowisland1.sql;

-- -------------------------------------------
-- 后续更新
-- -------------------------------------------
source /docker-entrypoint-initdb.d/sql/update_job_descriptions.sql;
source /docker-entrypoint-initdb.d/sql/update_item_catalog_remarks.sql;
source /docker-entrypoint-initdb.d/sql/update_player_faction.sql;
source /docker-entrypoint-initdb.d/sql/update_ark_limits.sql;
source /docker-entrypoint-initdb.d/sql/update_bayonet_threat.sql;
source /docker-entrypoint-initdb.d/sql/alter_island_event_difficulty.sql;

SET FOREIGN_KEY_CHECKS = 1;

-- 数据库初始化完成
-- 使用 root 用户连接数据库，密码: 695390489

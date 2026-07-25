-- ============================================
-- SnowIsland 数据库表结构参考
-- ============================================
-- 此文件仅供参考，实际表结构由 JPA/Hibernate 自动创建
-- 如需手动创建数据库，请参考 sql/ 目录下的各个 SQL 文件

-- 数据库创建
CREATE DATABASE IF NOT EXISTS snowisland
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE snowisland;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(100),
    `role` VARCHAR(20) DEFAULT 'PLAYER',
    `status` VARCHAR(20) DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 玩家状态表
CREATE TABLE IF NOT EXISTS `player_status` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `health` INT DEFAULT 100,
    `satiety` INT DEFAULT 100,
    `spirit` INT DEFAULT 100,
    `endurance` INT DEFAULT 100,
    `location_id` BIGINT,
    `job_id` BIGINT,
    `faction_id` BIGINT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- NPC 好感度表
CREATE TABLE IF NOT EXISTS `npc_favor` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `npc_id` BIGINT NOT NULL,
    `player_id` BIGINT NOT NULL,
    `favor_value` INT DEFAULT 0,
    `recognized` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_npc_player` (`npc_id`, `player_id`),
    INDEX `idx_player_favor` (`player_id`, `favor_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- NPC 交易配置表
CREATE TABLE IF NOT EXISTS `npc_trade_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `npc_id` BIGINT NOT NULL,
    `item_type` VARCHAR(50) NOT NULL,
    `item_id` BIGINT NOT NULL,
    `demand_quantity` INT DEFAULT 0,
    `supply_quantity` INT DEFAULT 0,
    `daily_limit` INT DEFAULT 10,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_npc_item` (`npc_id`, `item_type`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 交易记录表
CREATE TABLE IF NOT EXISTS `npc_trade_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `player_id` BIGINT NOT NULL,
    `npc_id` BIGINT NOT NULL,
    `item_type` VARCHAR(50) NOT NULL,
    `item_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    `trade_type` VARCHAR(20) NOT NULL,
    `favor_value` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_player_trade` (`player_id`, `created_at`),
    INDEX `idx_npc_trade` (`npc_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- NPC 对话表
CREATE TABLE IF NOT EXISTS `npc_dialogue` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `npc_id` BIGINT NOT NULL,
    `dialogue_key` VARCHAR(100) NOT NULL,
    `content` TEXT NOT NULL,
    `response_type` VARCHAR(20) DEFAULT 'NORMAL',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 玩家仓库表
CREATE TABLE IF NOT EXISTS `warehouse` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `player_id` BIGINT NOT NULL,
    `item_type` VARCHAR(50) NOT NULL,
    `item_id` BIGINT NOT NULL,
    `quantity` INT DEFAULT 0,
    `slot_index` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_player_slot` (`player_id`, `slot_index`),
    INDEX `idx_player_items` (`player_id`, `item_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Align ark/transport/tools/overnight with reference rules (existing DBs)
SET NAMES utf8mb4;

INSERT INTO `weapon` (`id`, `name`, `unit`, `remark`, `threat_level`, `created_at`, `updated_at`)
SELECT 13, '电钻', '把', '电动钻机，用于开采石料。生产工具（挖掘时5吨，否则1吨）。威胁值1。', 1, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `weapon` WHERE `id` = 13);

UPDATE `weapon`
SET `remark` = '二冲程汽油动力链锯，噪音巨大。威胁值4，伐木效率高（持有时5吨木材/天，否则1吨），但需要燃油且会暴露位置。'
WHERE `id` = 10;

-- overnight_location_id added via JPA ddl-auto=update on Player entity

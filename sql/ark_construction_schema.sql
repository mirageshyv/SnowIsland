-- =====================================================
-- Ark Construction Schema
-- SnowIsland - Ark Construction Module
-- =====================================================

SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS ark_construction;
SET FOREIGN_KEY_CHECKS=1;

CREATE TABLE ark_construction (
    id INT NOT NULL DEFAULT 1 COMMENT '单方舟模式，固定值为1',
    current_wood INT NOT NULL DEFAULT 0 COMMENT '当前木材数量（吨）',
    current_metal INT NOT NULL DEFAULT 0 COMMENT '当前金属制品数量（吨）',
    current_sealant INT NOT NULL DEFAULT 0 COMMENT '当前密封材料数量（kg）',
    engine_count INT NOT NULL DEFAULT 0 COMMENT '发动机数量（0-3）',
    propeller_count INT NOT NULL DEFAULT 0 COMMENT '螺旋桨数量',
    generator_count INT NOT NULL DEFAULT 0 COMMENT '发电机数量',
    current_cargo_capacity INT NOT NULL DEFAULT 0 COMMENT '当前载重能力',
    completion_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '完成度百分比',
    has_sail TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已建造帆',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='方舟建造进度表';

-- Insert initial data
INSERT INTO ark_construction (
    id, current_wood, current_metal, current_sealant, 
    engine_count, propeller_count, generator_count, 
    current_cargo_capacity, completion_percentage, has_sail
) VALUES (
    1, 0, 0, 0, 
    0, 0, 0, 
    0, 0.00, 0
);

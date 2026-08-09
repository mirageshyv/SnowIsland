-- 对齐《覆雪之下》参考资料：武器威胁值文案、道具/物资说明、便当→面包
-- 对已有数据库执行；全新安装请同步以 snowisland.sql 为准

-- ========== P0: 武器威胁值文案（与 threat_level / 参考资料一致）==========
UPDATE `weapon` SET `remark` = '韦伯利.38口径转轮手枪，英军标准配发。威胁值5，远程武器。' WHERE `id` = 1;
UPDATE `weapon` SET `remark` = '12号口径单管或双管猎枪，用于狩猎鸟类和小型动物。威胁值6，远程武器。' WHERE `id` = 2;
UPDATE `weapon` SET `remark` = '硬木制成的短棍，长50厘米。威胁值1，非致命武器，可用于制服而非杀死目标。' WHERE `id` = 3;
UPDATE `weapon` SET `remark` = '军用制式刺刀，长约20厘米。威胁值2。' WHERE `id` = 4;
UPDATE `weapon` SET `remark` = '铁头木柄的捕鱼工具，长110厘米。威胁值3，既可捕鱼也可作为近战武器，渔民的标配。' WHERE `id` = 6;
UPDATE `weapon` SET `remark` = '简单木质主体金属包角的反曲猎弓。威胁值4，无声远程武器。' WHERE `id` = 7;
UPDATE `weapon` SET `remark` = '采矿用的双头镐具，长65厘米，重5kg。威胁值1，主要用来挖掘石料，紧急时也可作为武器。' WHERE `id` = 8;
UPDATE `weapon` SET `remark` = '伐木用双面斧，长65厘米。威胁值2，砍树是本职工作，砍人也不是不行。' WHERE `id` = 9;
UPDATE `weapon` SET `remark` = '二冲程汽油动力链锯，噪音巨大。威胁值4，伐木效率极高（30吨原木/天），但需要燃油且会暴露位置。' WHERE `id` = 10;
UPDATE `weapon` SET `remark` = '医用不锈钢手术刀，套装含多型号刀片。威胁值1，精准切割工具，在医疗行动中不可或缺。' WHERE `id` = 11;

-- ========== P1: 防弹衣 / 绳索 / 火把 / 草木灰 ==========
UPDATE `item` SET `remark` = '复合金属材质制成的防护背心。在暴力冲突中可将一次「重伤」降级为「受伤」，或将一次「受伤」无效化，每场冲突限用一次。' WHERE `id` = 5;
UPDATE `material` SET `remark` = '麻绳或钢丝绳，直径1-2厘米。用于捆绑、拖拽、登山或船只系泊。可以提升探索成功程度。' WHERE `id` = 3;
UPDATE `item` SET `remark` = '手工艺人的产出物品，配合每个人都有的火柴可以用来夜间照明并取暖。亦可作为探索道具（+7探索值）。' WHERE `id` = 25;
UPDATE `material` SET `remark` = '烧炭工烧制燃料的副产物，1单位的草木灰等于1单位医疗资源。' WHERE `id` = 13;

-- ========== 便当 → 面包 ==========
UPDATE `item` SET
  `name` = '面包',
  `remark` = '面包师精心制作的便携餐食，松松软软量大管饱，营养均衡且便于携带。食用后可获得额外1个白天行动点，每人每天限吃一份。可以储存和交易，是后期高密度行动的重要战略资源。面包本身不提供热量，其核心价值在于让人挤出更多时间做事，而非填饱肚子。'
WHERE `id` = 18;

UPDATE `job` SET `description` = '烘焙：需要 5 单位食物与 15kg 木材制作 1 份面包；面包当天额外获得 1 个白天行动点，每人每天限 1 次。' WHERE `id` = 22;
UPDATE `job` SET `description` = '烘焙：需要 5 单位食物与 15kg 木材制作 1 份面包；面包当天额外获得 1 个白天行动点，每人每天限 1 次。' WHERE `id` = 25;

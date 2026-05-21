-- 更新物品图鉴 remark（已有数据库执行；全新安装以 snowisland.sql 为准）

UPDATE `item` SET `remark` = '装有绷带、消毒剂和止血带的急救包裹。每包提供10点医疗资源，可治疗伤口或稳定重伤员。' WHERE `id` = 1;
UPDATE `item` SET `remark` = '金属外壳的便携照明工具，使用煤油或电池。夜间行动的基础工具。' WHERE `id` = 2;
UPDATE `item` SET `remark` = '铁制约束器具，可将一名无反抗能力的玩家束缚。被束缚者无法进行大部分行动，直到被释放。' WHERE `id` = 3;
UPDATE `item` SET `remark` = '铜制哨子，声音尖锐可传遍全岛。吹响后会在公屏显示哨音，可用于报警或召集同伴。' WHERE `id` = 4;
UPDATE `item` SET `remark` = '复合金属材质制成的防护背心。在暴力冲突中可将一次「枪击」降级为「受伤」，或将一次「受伤」无效化，每场冲突限用一次。' WHERE `id` = 5;
UPDATE `item` SET `remark` = '轻质金属与帆布复合的防暴盾牌。一次「受伤」无效化，每场冲突限用一次。' WHERE `id` = 6;
UPDATE `item` SET `remark` = '维里式单发信号枪，可发射彩色信号弹。发射后由主持人在公屏展示信号内容（不超过5个字），可用于求援或传递简单信息。' WHERE `id` = 7;
UPDATE `item` SET `remark` = '内含基础维修工具。每包提供10点维修资源，可修复损坏的机械或设施。' WHERE `id` = 8;
UPDATE `item` SET `remark` = '带法律效力的空白契约纸。玩家之间可自行签订契约协议，用协议书签订的协议不公开契约信息。违约将受到约定惩罚。每名玩家每天最多成为契约者一次。' WHERE `id` = 9;
UPDATE `item` SET `remark` = '甘蔗酿造的烈酒，酒精度约40%。5瓶朗姆酒可以消除疲劳。' WHERE `id` = 10;
UPDATE `item` SET `remark` = '岛上采集的药用植物，经干燥处理后保存。可用于简易治疗，等于3医疗资源。' WHERE `id` = 11;
UPDATE `item` SET `remark` = '麻绳编织的捕鱼网具。渔民可用其与渔船一起进行渔猎行动。' WHERE `id` = 12;
UPDATE `item` SET `remark` = '动物油脂或蜂蜡制成的照明物。提供基础夜间照明，比煤油更安静但亮度较低。' WHERE `id` = 13;
UPDATE `item` SET `remark` = '75%浓度的消毒酒精，每升可用于提供5点医疗资源。' WHERE `id` = 14;
UPDATE `item` SET `remark` = '防水密封包装的火柴，每盒约50根。生火做饭、点灯取暖的基础消耗品。' WHERE `id` = 15;
UPDATE `item` SET `remark` = '木质铅笔。可用于书写信件、记录信息或绘制简易地图。' WHERE `id` = 16;
UPDATE `item` SET `remark` = '年代久远的航海图，部分区域已损毁或褪色。仍可辨认大致航线与岛屿位置，是远洋航行的重要参考。' WHERE `id` = 17;
UPDATE `item` SET `remark` = '面包师精心制作的便携餐食，内含面包、肉干和腌制蔬菜，营养均衡且便于携带。食用后可获得额外1个白天行动点，每人每天限吃一份。可以储存和交易，是后期高密度行动的重要战略资源。便当本身不提供热量，其核心价值在于让人挤出更多时间做事，而非填饱肚子。' WHERE `id` = 18;

UPDATE `weapon` SET `remark` = '韦伯利.38口径转轮手枪，英军标准配发。威胁值2，近距离防身武器，装弹6发。' WHERE `id` = 1;
UPDATE `weapon` SET `remark` = '12号口径单管或双管猎枪，用于狩猎鸟类和小型动物。威胁值3，中距离武器，装弹2发。' WHERE `id` = 2;
UPDATE `weapon` SET `remark` = '硬木制成的短棍，长50厘米。威胁值0.5，非致命武器，可用于制服而非杀死目标。' WHERE `id` = 3;
UPDATE `weapon` SET `remark` = '军用制式刺刀，长约20厘米。威胁值1。' WHERE `id` = 4;
UPDATE `weapon` SET `remark` = '多功能刀具，威胁值2。' WHERE `id` = 5;
UPDATE `weapon` SET `remark` = '铁头木柄的捕鱼工具，长110厘米。威胁值2，既可捕鱼也可作为近战武器，渔民的标配。' WHERE `id` = 6;
UPDATE `weapon` SET `remark` = '简单木质主体金属包角的反曲猎弓，威胁值2，无声远程武器。' WHERE `id` = 7;
UPDATE `weapon` SET `remark` = '采矿用的双头镐具，长65厘米，重5kg。威胁值0.5，主要用来挖掘石料，紧急时也可作为武器。' WHERE `id` = 8;
UPDATE `weapon` SET `remark` = '伐木用双面斧，长65厘米。威胁值1，砍树是本职工作，砍人也不是不行。' WHERE `id` = 9;
UPDATE `weapon` SET `remark` = '二冲程汽油动力链锯，噪音巨大。威胁值2，伐木效率极高（30吨原木/天），但需要燃料且会暴露位置。' WHERE `id` = 10;
UPDATE `weapon` SET `remark` = '医用不锈钢手术刀，套装含多型号刀片。威胁值0.5，精准切割工具，在医疗行动中不可或缺。' WHERE `id` = 11;
UPDATE `weapon` SET `remark` = '工业硝铵炸药，每公斤可开凿大量石料。威胁值极高，可用于挖矿加速、拆除建筑或制造大规模破坏' WHERE `id` = 12;

UPDATE `ammo` SET `remark` = '.38/200口径手枪弹药，每发为韦伯利手枪专用。装填后可使手枪的威胁值生效。' WHERE `id` = 1;
UPDATE `ammo` SET `remark` = '12号口径霰弹，每发内含多颗铅弹。装填后可使猎枪的威胁值生效。' WHERE `id` = 2;
UPDATE `ammo` SET `remark` = '红/绿两色信号弹，每发可发射一次。用信号枪发射后，由主持人在公屏展示信号内容。' WHERE `id` = 3;
UPDATE `ammo` SET `remark` = '木质箭杆配金属箭头，每支为猎弓专用。装填后可使猎弓的威胁值生效。' WHERE `id` = 4;

UPDATE `material` SET `name` = '燃料', `remark` = '统称可用于燃烧供能的物资，包括木柴、煤炭、燃料等。不同设备的燃料消耗标准不同，详见燃料消耗表。' WHERE `id` = 8;
UPDATE `material` SET `remark` = '加工后的铁件、钉子、铁丝等金属材料。可用于锻造武器或工具，或修建设施的基础材料。' WHERE `id` = 1;
UPDATE `material` SET `remark` = '从岛上砍伐的原木，未经过加工。可直接作为燃料（15kg/天取暖），或加工成木板用于建筑。' WHERE `id` = 2;
UPDATE `material` SET `remark` = '麻绳或钢丝绳，直径1-2厘米。用于捆绑、拖拽、登山或船只系泊。' WHERE `id` = 3;
UPDATE `material` SET `remark` = '原木经蒸汽箱加工后的标准化板材。用于建造避难所、修理船只或制作家具，比原木更易使用。' WHERE `id` = 4;
UPDATE `material` SET `remark` = '泛指各种可食用物资，包括面粉、鱼干、咸肉、土豆等。' WHERE `id` = 5;
UPDATE `material` SET `remark` = '黑色粘稠的石油残渣，桶装保存。可用于修补船体裂缝、防水处理或作为燃料。' WHERE `id` = 6;
UPDATE `material` SET `remark` = '从采石场开采的岩石，大小不一。' WHERE `id` = 7;
UPDATE `material` SET `remark` = '厚实的亚麻或棉帆布，每卷宽1.5米。用于制作船帆、帐篷、防水布，或修补帆布类装备。' WHERE `id` = 9;
UPDATE `material` SET `remark` = '船用柴油发动机，方舟的核心动力设备，每台可使航行速度提升一档。' WHERE `id` = 10;
UPDATE `material` SET `remark` = '三叶螺旋桨，直径1.5米。方舟推进装置，需与发动机配套使用，破损后可拆卸更换。' WHERE `id` = 11;
UPDATE `material` SET `remark` = '老旧柴油发电机，已闲置多年。可尝试修理后发电，为岛上提供有限电力，需持续消耗燃料。' WHERE `id` = 12;

UPDATE `catastrophe_card` SET `description` = REPLACE(`description`, '煤油/燃油', '煤油/燃料') WHERE `description` LIKE '%煤油/燃油%';

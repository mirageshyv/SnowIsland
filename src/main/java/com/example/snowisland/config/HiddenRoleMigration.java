package com.example.snowisland.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;

/**
 * 隐藏身份：job.hidden、player.hidden_job_id，以及 6 个隐藏职业及其初始物资（幂等）。
 */
@Component
@Order(5)
public class HiddenRoleMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(HiddenRoleMigration.class);

    @PersistenceContext
    private EntityManager entityManager;

    private static final class JobSeed {
        final String name;
        final String skills;
        final String description;

        JobSeed(String name, String skills, String description) {
            this.name = name;
            this.skills = skills;
            this.description = description;
        }
    }

    private static final class ItemSeed {
        final String jobName;
        final String itemType;
        final int itemId;
        final int quantity;
        final String unit;

        ItemSeed(String jobName, String itemType, int itemId, int quantity, String unit) {
            this.jobName = jobName;
            this.itemType = itemType;
            this.itemId = itemId;
            this.quantity = quantity;
            this.unit = unit;
        }
    }

    private static final String NATIVE_SHARED =
            "身份永远隐藏，原住民之间默认互不知晓。若同时探索同一地点，或同处一地且现场无其他玩家，则自动互认。"
            + "开局抽表面阵营与公共职业，并获得该职业的正常资源与技能。"
            + "26人局固定2名（从鱼人/荒原狼/石之子中随机抽2个各1人，配置在平民席位）；48人局亦为2名。"
            + "开局由主持人告知一处隐秘居所（与信天翁所知不同）。"
            + "\n共同目标：首要不暴露原住民身份、进入避难所并活下去。"
            + "成就「未启之门」：暴雪结束时无其他玩家执行二级仪式，且至少一名原住民成功举行过一次二级仪式。"
            + "成就「地脉守护者」：暴雪结束时原住民阵营持有至少5枚祭坛石，且全游戏未举行任何仪式。"
            + "收集全场祭坛石、抢先完成仪式，阻止铁门被打开。";

    private static final List<JobSeed> JOB_SEEDS = Arrays.asList(
        new JobSeed(
            "鱼人",
            "鱼鳞外套（被动）",
            "潮汐之子。原住民。你幼时被矿场的老人从海边捡回；入水时皮肤会泛起暗蓝色纹路。"
            + "\n初始：鱼鳞外套（每天一次防弹衣效果，不可交易、不可被偷盗或被得知拥有）；祭坛石×1（绑定个人仓库，不可被偷盗或被得知拥有，可交易或赠送）；随机仪式记物由主持人发放。"
            + "\n" + NATIVE_SHARED
        ),
        new JobSeed(
            "荒原狼",
            "野性追踪（被动）；刺刀威胁值2不可被偷盗",
            "孤野的猎手。原住民。你出生在矿场后山的乱石堆，比镇上任何人更熟悉这座岛的每一道裂缝。"
            + "\n初始：刺刀（威胁值2，不可被偷盗）；祭坛石×1（绑定个人仓库，不可被偷盗或被得知拥有，可交易或赠送）；随机仪式记物由主持人发放。"
            + "\n" + NATIVE_SHARED
        ),
        new JobSeed(
            "石之子",
            "石之皮肤（被动）；铁镐威胁值2",
            "沉默的脊梁。原住民。你小时候掉进废弃巷道，第四天自己走出来，手里攥着发微光的矿石；从此不怕黑、不怕冷。"
            + "\n初始：铁镐×1（威胁值2）；祭坛石×1（绑定个人仓库，不可被偷盗或被得知拥有，可交易或赠送）；随机仪式记物由主持人发放。"
            + "\n" + NATIVE_SHARED
        ),
        new JobSeed(
            "飞行员",
            "维修,驾驶员",
            "钢铁与天空的重逢。外来者。维修：可修复设施或飞机部件，无需额外资源，可指导安装。驾驶员：可驾驶已修复的飞机。飞机修复需4个部件（发电机、螺旋桨、油箱、起落架），燃料150升煤油；坠机点仅飞行员初始知晓。快速行动：机库维修（夜晚，金属5kg+5点维修资源，每次最多装1个部件）、试飞（白天1其他行动或夜晚快速行动，最终一次性消耗煤油150升）。弱点：坠机点被发现后可能被破坏。胜利条件：集齐4部件并加注≥150升燃料，暴雪降临前或开始时起飞；成功则飞行员与登记平民单独胜利、不计入避难所/方舟。失败：暴雪降临时未修复则个人目标失败。4个飞机部件与维修资源20由DM手动记录发放。"
        ),
        new JobSeed(
            "信天翁",
            "根源感知（被动）,替身（被动）",
            "感知者、岛屿倾听者。身份隐藏。根源感知（被动）：开局知晓一块祭坛石的具体地点，并且知道一处隐秘居所（与原住民开局所知不同）。替身（被动）：开局可选一个公共职业并拥有其资源与技能。初始通灵笔记×1、蜡烛×10、酒精10升。胜利条件：至少成功进行过一次通灵笔记二级仪式，且游戏结束时没有直接暴露自己的身份。"
        ),
        new JobSeed(
            "调查记者",
            "调查员,书写",
            "真相的重量。调查员：白天调查玩家不消耗行动点（每天限1次）。书写：可制作并保存档案，档案不可被销毁。真相档案初始5页，每5kg木材扩1页，最多20页；可通过调查玩家、监听、挖掘、协议见证获取信息。胜利条件：档案≥15条有效信息（含≥2名不同阵营玩家真实阵营身份、1条阵营行动隐藏证据、1条天灾或地脉真相）；记者存活至暴雪结束，或档案暴雪后被找到。"
        )
    );

    private static final List<ItemSeed> ITEM_SEEDS = Arrays.asList(
        new ItemSeed("鱼人", "item", 30, 1, "件"),
        new ItemSeed("鱼人", "item", 27, 1, "枚"),
        new ItemSeed("荒原狼", "weapon", 4, 1, "把"),
        new ItemSeed("荒原狼", "item", 27, 1, "枚"),
        new ItemSeed("石之子", "weapon", 15, 1, "把"),
        new ItemSeed("石之子", "item", 27, 1, "枚"),
        new ItemSeed("信天翁", "item", 28, 1, "本"),
        new ItemSeed("信天翁", "item", 13, 10, "根"),
        new ItemSeed("信天翁", "item", 14, 10, "升"),
        new ItemSeed("飞行员", "item", 39, 1, "张"),
        new ItemSeed("飞行员", "material", 2, 75, "kg"),
        new ItemSeed("飞行员", "material", 3, 10, "米"),
        new ItemSeed("飞行员", "material", 5, 3, "kg"),
        new ItemSeed("调查记者", "item", 42, 1, "本"),
        new ItemSeed("调查记者", "item", 43, 1, "支"),
        new ItemSeed("调查记者", "material", 5, 3, "kg")
    );

    @Override
    @Transactional
    public void run(String... args) {
        try {
            ensureColumn("job", "hidden", "TINYINT(1) NOT NULL DEFAULT 0");
            ensureColumn("player", "hidden_job_id", "INT NULL");
            seedHiddenJobs();
            seedInitialItems();
        } catch (Exception e) {
            logger.error("隐藏身份迁移失败: {}", e.getMessage(), e);
        }
    }

    private void ensureColumn(String table, String column, String definition) {
        Number count = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?1 AND COLUMN_NAME = ?2"
        ).setParameter(1, table).setParameter(2, column).getSingleResult();

        if (count.intValue() == 0) {
            entityManager.createNativeQuery(
                    "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition
            ).executeUpdate();
            logger.info("{} 表已添加 {} 列", table, column);
        }
    }

    private void seedHiddenJobs() {
        for (JobSeed seed : JOB_SEEDS) {
            int hiddenFlag = "飞行员".equals(seed.name) ? 0 : 1;
            Number exists = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM job WHERE name = :name"
            ).setParameter("name", seed.name).getSingleResult();
            if (exists.intValue() > 0) {
                entityManager.createNativeQuery(
                    "UPDATE job SET skills = :skills, description = :description, hidden = :hiddenFlag, updated_at = NOW() " +
                    "WHERE name = :name"
                )
                    .setParameter("skills", seed.skills)
                    .setParameter("description", seed.description)
                    .setParameter("hiddenFlag", hiddenFlag)
                    .setParameter("name", seed.name)
                    .executeUpdate();
                logger.info("已更新{}职业「{}」", hiddenFlag == 0 ? "公开" : "隐藏", seed.name);
                continue;
            }
            entityManager.createNativeQuery(
                "INSERT INTO job (name, skills, description, hidden, created_at, updated_at) " +
                "VALUES (:name, :skills, :description, :hiddenFlag, NOW(), NOW())"
            )
                .setParameter("name", seed.name)
                .setParameter("skills", seed.skills)
                .setParameter("description", seed.description)
                .setParameter("hiddenFlag", hiddenFlag)
                .executeUpdate();
            logger.info("已添加{}职业「{}」", hiddenFlag == 0 ? "公开" : "隐藏", seed.name);
        }
        entityManager.createNativeQuery(
            "UPDATE job SET hidden = 0 WHERE name = '飞行员'"
        ).executeUpdate();
        logger.info("已将职业「飞行员」设为公开 (hidden=0)");
    }

    private void seedInitialItems() {
        for (JobSeed job : JOB_SEEDS) {
            Number jobIdNum = lookupJobId(job.name);
            if (jobIdNum == null) {
                logger.warn("未找到职业「{}」，跳过初始物资", job.name);
                continue;
            }
            int jobId = jobIdNum.intValue();
            Number itemCount = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM job_initial_items WHERE job_id = :jobId"
            ).setParameter("jobId", jobId).getSingleResult();
            if (itemCount.intValue() > 0) {
                continue;
            }
            for (ItemSeed item : ITEM_SEEDS) {
                if (!item.jobName.equals(job.name)) {
                    continue;
                }
                entityManager.createNativeQuery(
                    "INSERT INTO job_initial_items (job_id, item_type, item_id, quantity, unit, created_at, updated_at) " +
                    "VALUES (:jobId, :itemType, :itemId, :quantity, :unit, NOW(), NOW())"
                )
                    .setParameter("jobId", jobId)
                    .setParameter("itemType", item.itemType)
                    .setParameter("itemId", item.itemId)
                    .setParameter("quantity", item.quantity)
                    .setParameter("unit", item.unit)
                    .executeUpdate();
            }
            logger.info("已为隐藏职业「{}」写入初始物资", job.name);
        }
        correctWastelandWolfWeapon();
    }

    private void correctWastelandWolfWeapon() {
        Number jobIdNum = lookupJobId("荒原狼");
        if (jobIdNum == null) {
            return;
        }
        int updated = entityManager.createNativeQuery(
            "UPDATE job_initial_items SET item_id = 4, unit = '把', updated_at = NOW() " +
            "WHERE job_id = :jobId AND item_type = 'weapon' AND item_id = 14"
        )
            .setParameter("jobId", jobIdNum.intValue())
            .executeUpdate();
        if (updated > 0) {
            logger.info("已将荒原狼初始武器从匕首(14)更正为刺刀(4)");
        }
    }

    private Number lookupJobId(String name) {
        @SuppressWarnings("unchecked")
        List<Object> ids = entityManager.createNativeQuery(
            "SELECT id FROM job WHERE name = :name"
        ).setParameter("name", name).getResultList();
        if (ids.isEmpty()) {
            return null;
        }
        return (Number) ids.get(0);
    }
}

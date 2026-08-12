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

    private static final List<JobSeed> JOB_SEEDS = Arrays.asList(
        new JobSeed(
            "鱼人",
            "鱼鳞外套（被动）",
            "潮汐之子。原住民阵营，身份永远隐藏。鱼鳞外套每天一次防弹衣效果，不可交易、不可被偷盗或被得知拥有。初始1枚祭坛石绑定个人仓库（不可被偷盗，可交易或赠送）；随机仪式记物由DM发放。原住民共同目标见阵营规则。"
        ),
        new JobSeed(
            "荒原狼",
            "野性追踪（被动）；匕首威胁值3不可被偷盗",
            "孤野的猎手。原住民阵营，身份永远隐藏。野性追踪（被动）。初始匕首威胁值3且不可被偷盗；祭坛石×1绑定个人仓库（不可被偷盗，可交易或赠送）；随机仪式记物由DM发放。原住民共同目标见阵营规则。"
        ),
        new JobSeed(
            "石之子",
            "石之皮肤（被动）；铁镐威胁值2",
            "沉默的脊梁。原住民阵营，身份永远隐藏。石之皮肤（被动）。初始铁镐威胁值2；祭坛石×1绑定个人仓库（不可被偷盗，可交易或赠送）；随机仪式记物由DM发放。原住民共同目标见阵营规则。"
        ),
        new JobSeed(
            "飞行员",
            "维修,飞行员",
            "钢铁与天空的重逢。外来者。维修：可修复设施或飞机部件，无需额外资源，可指导安装。飞行员：可驾驶已修复的飞机。飞机修复需4个部件（发电机、螺旋桨、油箱、起落架），燃料150升；坠机点仅飞行员初始知晓。快速行动：机库维修、试飞。弱点：坠机点被发现后可能被破坏。胜利条件：集齐4部件并加注≥150升燃料，暴雪降临前或开始时起飞。4个飞机部件与维修资源20由DM手动记录发放。"
        ),
        new JobSeed(
            "信天翁",
            "根源感知（被动）,替身（被动）",
            "感知者、岛屿倾听者。身份隐藏。根源感知（被动）：开局知晓一块祭坛石的具体地点。替身（被动）：开局可选一个公共职业并拥有其资源与技能。初始通灵笔记×1、蜡烛×10、酒精10升。胜利条件：至少完成一次任意二级仪式，且游戏结束时身份未被确认。"
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
        new ItemSeed("荒原狼", "weapon", 14, 1, "把"),
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
            Number exists = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM job WHERE name = :name"
            ).setParameter("name", seed.name).getSingleResult();
            if (exists.intValue() > 0) {
                continue;
            }
            entityManager.createNativeQuery(
                "INSERT INTO job (name, skills, description, hidden, created_at, updated_at) " +
                "VALUES (:name, :skills, :description, 1, NOW(), NOW())"
            )
                .setParameter("name", seed.name)
                .setParameter("skills", seed.skills)
                .setParameter("description", seed.description)
                .executeUpdate();
            logger.info("已添加隐藏职业「{}」", seed.name);
        }
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

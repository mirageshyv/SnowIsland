package com.example.snowisland.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * 新规则内容种子：祭坛石、仪式道具、外来者道具等。
 * 幂等：按名称判断是否已存在；item 表补充 tradable 列（不可交易道具拦截用）。
 */
@Component
@Order(3)
public class NewRulesetDataMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(NewRulesetDataMigration.class);

    @PersistenceContext
    private EntityManager entityManager;

    private static final class ItemSeed {
        final int preferredId;
        final String name;
        final String unit;
        final String remark;
        final boolean tradable;

        ItemSeed(int preferredId, String name, String unit, String remark, boolean tradable) {
            this.preferredId = preferredId;
            this.name = name;
            this.unit = unit;
            this.remark = remark;
            this.tradable = tradable;
        }
    }

    private static final class WeaponSeed {
        final int preferredId;
        final String name;
        final String unit;
        final String remark;
        final int threatLevel;

        WeaponSeed(int preferredId, String name, String unit, String remark, int threatLevel) {
            this.preferredId = preferredId;
            this.name = name;
            this.unit = unit;
            this.remark = remark;
            this.threatLevel = threatLevel;
        }
    }

    private static final List<ItemSeed> ITEM_SEEDS = Arrays.asList(
        new ItemSeed(27, "祭坛石", "枚", "一枚泛着微光的奇异石头，握在手中能感到隐约的温热，似乎蕴含着某种能量。", true),
        new ItemSeed(28, "通灵笔记", "本", "一本古旧的笔记，最后一页空白。据说拥有者可以借助它举行某种仪式。", true),
        new ItemSeed(29, "发光矿石碎片", "枚", "一块发出微弱脉动光的矿石碎片，可作为信物使用。", true),
        new ItemSeed(30, "鱼鳞外套", "件", "一件覆盖细密鳞片的外套，贴身穿着时有奇异的防护力。不可交易。", false),
        new ItemSeed(31, "契约铁卷", "卷", "一卷用铁片串成的册子，记载着某种古老契约的完整条款。", true),
        new ItemSeed(32, "革命宣言", "份", "一份用铅笔写在旧报纸边缘的宣言，字迹潦草但充满力量。", true),
        new ItemSeed(33, "古老龙骨图", "卷", "一卷发黄的羊皮纸，画着龙骨结构图，关键节点标注了奇怪的符文。", true),
        new ItemSeed(34, "灰烬预言书", "本", "一本被烧过但依然可读的皮面书，书页边缘焦黑，核心内容完整。", true),
        new ItemSeed(35, "人皮册子残页", "页", "装订怪异的册子残页，大部分已腐烂，记载着某种禁忌的内容。", true),
        new ItemSeed(36, "星象观测手稿", "份", "一叠详细记录星轨与星象的手稿，出自五十年前的一位灯塔看守之手。", true),
        new ItemSeed(37, "尸油蜡烛", "支", "特制的蜡烛，燃烧时散发奇异的气味。据说是某些仪式的必需品。", true),
        new ItemSeed(38, "深海鱼油蜡烛", "支", "以深海鱼油制成的蜡烛，点燃后火焰呈幽蓝色。", true),
        new ItemSeed(39, "旧地图", "张", "一张残旧的地图，标注着某个地点的大致位置。", true),
        new ItemSeed(40, "飞机部件·油箱", "个", "飞机用金属油箱，保存完好，可以安装使用。", true),
        new ItemSeed(41, "飞机部件·起落架", "个", "飞机起落架组件，结构完整，可以安装使用。", true),
        new ItemSeed(42, "笔记本", "本", "一本可以书写记录的笔记本，初始5页。不可交易。", false),
        new ItemSeed(43, "钢笔", "支", "一支可以正常书写的钢笔。不可交易。", false),
        new ItemSeed(44, "归墟罗盘", "个", "一枚刻满螺旋纹路的青铜盘，中心嵌着一颗永不下沉的黑色石子。", true),
        new ItemSeed(45, "龙骨刻刀", "把", "一把由鲸骨打磨成的刻刀，刀柄镶嵌着一颗已石化的鱼眼珠。", true),
        new ItemSeed(46, "灾厄之眼", "枚", "一枚黑色玻璃球，内部有不断旋转的暗红色雾气。", true),
        new ItemSeed(47, "引魂烛台", "座", "一座三足青铜烛台，底座刻着扭曲的符文。", true),
        new ItemSeed(48, "星轨轮盘", "个", "一个用黑曜石磨成的圆盘，表面刻着黄道十二宫符号，中心有一处凹槽。", true),
        new ItemSeed(49, "烬火旗", "面", "一面被烧得只剩一半的旗帜，无论如何烧都不会彻底化为灰烬。", true)
    );

    private static final List<WeaponSeed> WEAPON_SEEDS = Arrays.asList(
        new WeaponSeed(14, "匕首", "把", "一把锋利的匕首，便于隐蔽携带。威胁值2。", 2),
        new WeaponSeed(15, "铁镐", "把", "结实的铁镐，可用于挖掘，紧急时也可作为武器。威胁值2。", 2)
    );

    @Override
    @Transactional
    public void run(String... args) {
        try {
            ensureTradableColumn();
            seedItems();
            seedWeapons();
            applyTradableFlags();
        } catch (Exception e) {
            logger.error("新规则数据迁移失败: {}", e.getMessage(), e);
        }
    }

    private void ensureTradableColumn() {
        Number count = (Number) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM information_schema.COLUMNS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'item' AND COLUMN_NAME = 'tradable'"
        ).getSingleResult();

        if (count.intValue() == 0) {
            entityManager.createNativeQuery(
                "ALTER TABLE item ADD COLUMN tradable TINYINT(1) NOT NULL DEFAULT 1"
            ).executeUpdate();
            logger.info("item 表已添加 tradable 列");
        }
    }

    private void seedItems() {
        for (ItemSeed seed : ITEM_SEEDS) {
            Number exists = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM item WHERE name = :name"
            ).setParameter("name", seed.name).getSingleResult();
            if (exists.intValue() > 0) {
                continue;
            }

            int id = resolveId("item", seed.preferredId, seed.name);
            entityManager.createNativeQuery(
                "INSERT INTO item (id, name, unit, remark, tradable, created_at, updated_at) " +
                "VALUES (:id, :name, :unit, :remark, :tradable, NOW(), NOW())"
            )
                .setParameter("id", id)
                .setParameter("name", seed.name)
                .setParameter("unit", seed.unit)
                .setParameter("remark", seed.remark)
                .setParameter("tradable", seed.tradable ? 1 : 0)
                .executeUpdate();
            logger.info("已添加道具「{}」到 item 表，ID: {}", seed.name, id);
        }
    }

    private void seedWeapons() {
        for (WeaponSeed seed : WEAPON_SEEDS) {
            Number exists = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM weapon WHERE name = :name"
            ).setParameter("name", seed.name).getSingleResult();
            if (exists.intValue() > 0) {
                continue;
            }

            int id = resolveId("weapon", seed.preferredId, seed.name);
            entityManager.createNativeQuery(
                "INSERT INTO weapon (id, name, unit, remark, threat_level, created_at, updated_at) " +
                "VALUES (:id, :name, :unit, :remark, :threat, NOW(), NOW())"
            )
                .setParameter("id", id)
                .setParameter("name", seed.name)
                .setParameter("unit", seed.unit)
                .setParameter("remark", seed.remark)
                .setParameter("threat", seed.threatLevel)
                .executeUpdate();
            logger.info("已添加武器「{}」到 weapon 表，ID: {}", seed.name, id);
        }
    }

    /** 确保不可交易标记正确（幂等，即使道具行早已存在也会修正）。 */
    private void applyTradableFlags() {
        for (ItemSeed seed : ITEM_SEEDS) {
            if (!seed.tradable) {
                entityManager.createNativeQuery(
                    "UPDATE item SET tradable = 0 WHERE name = :name"
                ).setParameter("name", seed.name).executeUpdate();
            }
        }
    }

    /** 优先使用固定 ID（与前端映射保持一致）；若被占用则退回 MAX(id)+1 并告警。 */
    private int resolveId(String table, int preferredId, String name) {
        Number occupied = (Number) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM " + table + " WHERE id = :id"
        ).setParameter("id", preferredId).getSingleResult();

        if (occupied.intValue() == 0) {
            return preferredId;
        }

        Object maxIdResult = entityManager.createNativeQuery(
            "SELECT COALESCE(MAX(id), 0) FROM " + table
        ).getSingleResult();
        int fallback = ((Number) maxIdResult).intValue() + 1;
        logger.warn("{} 表 ID {} 已被占用，道具「{}」使用回退 ID {}（注意：前端映射可能需要同步调整）",
            table, preferredId, name, fallback);
        return fallback;
    }
}

package com.example.snowisland.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * 幂等：把 player.faction 扩到含 外来者 / 原住民。
 * 已有 VARCHAR 列则跳过（已能存任意阵营名）。
 */
@Component
@Order(7)
public class FactionEnumMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(FactionEnumMigration.class);

    private static final String TARGET_ENUM =
            "ENUM('统治者','反叛者','冒险者','天灾使者','平民','外来者','原住民') NOT NULL";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            widenPlayerFaction();
        } catch (Exception e) {
            logger.error("player.faction 阵营枚举迁移失败: {}", e.getMessage(), e);
        }
    }

    private void widenPlayerFaction() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(
                "SELECT DATA_TYPE, COLUMN_TYPE FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'player' AND COLUMN_NAME = 'faction'"
        ).getResultList();
        if (rows.isEmpty()) {
            logger.warn("player.faction 列不存在，跳过枚举迁移");
            return;
        }
        Object[] row = rows.get(0);
        String dataType = row[0] != null ? String.valueOf(row[0]).toLowerCase() : "";
        String columnType = row[1] != null ? String.valueOf(row[1]) : "";
        if (!"enum".equals(dataType)) {
            logger.info("player.faction 类型为 {}，无需扩 ENUM", dataType);
            return;
        }
        if (columnType.contains("外来者") && columnType.contains("原住民")) {
            logger.info("player.faction 已包含外来者/原住民，跳过");
            return;
        }
        entityManager.createNativeQuery(
                "ALTER TABLE player MODIFY COLUMN faction " + TARGET_ENUM
        ).executeUpdate();
        logger.info("已将 player.faction 扩展为含 外来者/原住民 的 ENUM");
    }
}

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
 * 图鉴管理：为 item/weapon/ammo/material 表补充 tag、image_url 列（幂等）。
 */
@Component
@Order(4)
public class CatalogCodexMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CatalogCodexMigration.class);

    private static final List<String> TABLES = Arrays.asList("item", "weapon", "ammo", "material");

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            for (String table : TABLES) {
                ensureColumn(table, "tag", "VARCHAR(100) NULL");
                ensureColumn(table, "image_url", "VARCHAR(255) NULL");
            }
        } catch (Exception e) {
            logger.error("图鉴列迁移失败: {}", e.getMessage(), e);
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
}

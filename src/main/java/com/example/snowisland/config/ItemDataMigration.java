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
import java.util.List;

@Component
@Order(2)
public class ItemDataMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ItemDataMigration.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        migrateTorchItem();
    }

    @SuppressWarnings("unchecked")
    private void migrateTorchItem() {
        try {
            List<Object[]> existing = entityManager.createNativeQuery(
                "SELECT id, name FROM item WHERE name = '火把'"
            ).getResultList();

            if (!existing.isEmpty()) {
                logger.info("数据库已存在火把道具，跳过迁移");
                return;
            }

            BigInteger maxIdResult = (BigInteger) entityManager.createNativeQuery(
                "SELECT COALESCE(MAX(id), 0) FROM item"
            ).getSingleResult();
            int nextId = maxIdResult.intValue() + 1;

            if (nextId < 26) nextId = 26;

            entityManager.createNativeQuery(
                "INSERT INTO item (id, name, unit, remark, created_at, updated_at) " +
                "VALUES (:id, '火把', '把', '木质火把，蘸有沥青和煤油。夜间照明与探索的重要工具，可有效提升探索发现率。持续时间约2小时，+7点探索值/个。', NOW(), NOW())"
            ).setParameter("id", nextId).executeUpdate();

            logger.info("成功添加火把道具到item表，ID: {}", nextId);
        } catch (Exception e) {
            logger.error("迁移火把道具失败: {}", e.getMessage(), e);
        }
    }
}

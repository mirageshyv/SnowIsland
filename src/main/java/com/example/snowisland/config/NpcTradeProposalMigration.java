package com.example.snowisland.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Ensure npc_trade_proposal exists, and that reject does not block a later offer the same day.
 */
@Component
@Order(14)
public class NpcTradeProposalMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(NpcTradeProposalMigration.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            Number count = (Number) entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM information_schema.TABLES "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'npc_trade_proposal'"
            ).getSingleResult();
            if (count.intValue() == 0) {
                entityManager.createNativeQuery(
                        "CREATE TABLE npc_trade_proposal ("
                                + "id INTEGER PRIMARY KEY AUTO_INCREMENT, "
                                + "npc_id INTEGER NOT NULL, "
                                + "player_id INTEGER NOT NULL, "
                                + "game_day INTEGER NOT NULL, "
                                + "status VARCHAR(20) NOT NULL DEFAULT 'open', "
                                + "give_items TEXT, "
                                + "take_items TEXT, "
                                + "remark VARCHAR(255) DEFAULT NULL, "
                                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, "
                                + "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
                                + "KEY idx_npc_player_day_proposal (npc_id, player_id, game_day, status), "
                                + "KEY idx_npc_proposal_status (npc_id, status)"
                                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
                ).executeUpdate();
                logger.info("已创建 npc_trade_proposal 表");
                return;
            }
            dropIndexIfExists("uk_npc_player_day_proposal");
            ensureIndex("idx_npc_player_day_proposal",
                    "CREATE INDEX idx_npc_player_day_proposal ON npc_trade_proposal (npc_id, player_id, game_day, status)");
        } catch (Exception e) {
            logger.error("npc_trade_proposal 表迁移失败: {}", e.getMessage(), e);
        }
    }

    private void dropIndexIfExists(String indexName) {
        Number count = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM information_schema.STATISTICS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'npc_trade_proposal' AND INDEX_NAME = ?1"
        ).setParameter(1, indexName).getSingleResult();
        if (count.intValue() > 0) {
            entityManager.createNativeQuery("ALTER TABLE npc_trade_proposal DROP INDEX " + indexName).executeUpdate();
            logger.info("已删除 npc_trade_proposal.{}", indexName);
        }
    }

    private void ensureIndex(String indexName, String ddl) {
        Number count = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM information_schema.STATISTICS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'npc_trade_proposal' AND INDEX_NAME = ?1"
        ).setParameter(1, indexName).getSingleResult();
        if (count.intValue() == 0) {
            entityManager.createNativeQuery(ddl).executeUpdate();
            logger.info("已创建 npc_trade_proposal.{}", indexName);
        }
    }
}

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
 * Raise the default daily heating requirement from 15 to 25 heat units.
 * Idempotent: only runs while game_day_settings.required_fuel_kg still defaults to 15.
 */
@Component
@Order(13)
public class HeatingRequirementMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(HeatingRequirementMigration.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            if (!columnDefaultIs(15, "game_day_settings", "required_fuel_kg")) {
                return;
            }
            alterDefault("game_day_settings", "required_fuel_kg",
                    "int(11) NOT NULL DEFAULT 25 COMMENT '每人每日取暖燃料（千克，木材或燃料）'");
            alterDefaultIfPresent("player_daily_consumption", "required_fuel_kg",
                    "int(11) NOT NULL DEFAULT 25");
            alterDefaultIfPresent("npc_daily_consumption", "required_fuel_kg",
                    "int(11) NOT NULL DEFAULT 25");
            int days = entityManager.createNativeQuery(
                    "UPDATE game_day_settings SET required_fuel_kg = 25 WHERE required_fuel_kg = 15"
            ).executeUpdate();
            int players = tableExists("player_daily_consumption")
                    ? entityManager.createNativeQuery(
                    "UPDATE player_daily_consumption SET required_fuel_kg = 25 "
                            + "WHERE required_fuel_kg = 15 AND submitted = 0"
            ).executeUpdate()
                    : 0;
            int npcs = tableExists("npc_daily_consumption")
                    ? entityManager.createNativeQuery(
                    "UPDATE npc_daily_consumption SET required_fuel_kg = 25 "
                            + "WHERE required_fuel_kg = 15 AND IFNULL(requirements_met, 0) = 0"
            ).executeUpdate()
                    : 0;
            logger.info("取暖需求已从 15 更新为 25（天数 {}，未提交玩家 {}，未满足 NPC {}）",
                    days, players, npcs);
        } catch (Exception e) {
            logger.error("取暖需求迁移失败: {}", e.getMessage(), e);
        }
    }

    private boolean columnDefaultIs(int expected, String table, String column) {
        Object raw = entityManager.createNativeQuery(
                "SELECT COLUMN_DEFAULT FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?1 AND COLUMN_NAME = ?2"
        ).setParameter(1, table).setParameter(2, column).getSingleResult();
        if (raw == null) {
            return false;
        }
        try {
            return Integer.parseInt(String.valueOf(raw).replace("'", "").trim()) == expected;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean tableExists(String table) {
        Number count = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM information_schema.TABLES "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?1"
        ).setParameter(1, table).getSingleResult();
        return count.intValue() > 0;
    }

    private boolean columnExists(String table, String column) {
        Number count = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?1 AND COLUMN_NAME = ?2"
        ).setParameter(1, table).setParameter(2, column).getSingleResult();
        return count.intValue() > 0;
    }

    private void alterDefaultIfPresent(String table, String column, String definition) {
        if (tableExists(table) && columnExists(table, column)) {
            alterDefault(table, column, definition);
        }
    }

    private void alterDefault(String table, String column, String definition) {
        entityManager.createNativeQuery(
                "ALTER TABLE " + table + " MODIFY COLUMN " + column + " " + definition
        ).executeUpdate();
    }
}

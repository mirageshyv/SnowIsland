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
 * 幂等：为灯塔、集市、教堂补入地点设施。按 (location_id, name) 判断是否已存在，不覆盖已有行刑台。
 */
@Component
@Order(8)
public class LocationFacilityMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(LocationFacilityMigration.class);

    @PersistenceContext
    private EntityManager entityManager;

    private static final class FacilitySeed {
        final int locationId;
        final String name;
        final String description;

        FacilitySeed(int locationId, String name, String description) {
            this.locationId = locationId;
            this.name = name;
            this.description = description;
        }
    }

    private static final List<FacilitySeed> SEEDS = Arrays.asList(
        new FacilitySeed(5, "望远镜", "灯塔顶层的望远镜，可远眺海面与岛岸。"),
        new FacilitySeed(10, "牲畜栏", "集市一侧圈养牲畜的栏舍。"),
        new FacilitySeed(10, "烧炭炉", "集市边用于烧制木炭的炉灶。"),
        new FacilitySeed(10, "棚屋", "集市旁供人歇脚存放杂物的简易棚屋。"),
        new FacilitySeed(4, "礼拜堂", "教堂内供祷告与仪式的侧厅。")
    );

    @Override
    @Transactional
    public void run(String... args) {
        try {
            seedFacilities();
        } catch (Exception e) {
            logger.error("地点设施种子迁移失败: {}", e.getMessage(), e);
        }
    }

    private void seedFacilities() {
        for (FacilitySeed seed : SEEDS) {
            Number locationExists = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM location WHERE id = :id"
            ).setParameter("id", seed.locationId).getSingleResult();
            if (locationExists.intValue() == 0) {
                logger.warn("地点 ID {} 不存在，跳过设施「{}」", seed.locationId, seed.name);
                continue;
            }

            Number exists = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM location_facility WHERE location_id = :locationId AND name = :name"
            )
                .setParameter("locationId", seed.locationId)
                .setParameter("name", seed.name)
                .getSingleResult();
            if (exists.intValue() > 0) {
                continue;
            }

            entityManager.createNativeQuery(
                "INSERT INTO location_facility (location_id, name, description, created_at, updated_at) " +
                "VALUES (:locationId, :name, :description, NOW(), NOW())"
            )
                .setParameter("locationId", seed.locationId)
                .setParameter("name", seed.name)
                .setParameter("description", seed.description)
                .executeUpdate();
            logger.info("已为地点 {} 添加设施「{}」", seed.locationId, seed.name);
        }
    }
}

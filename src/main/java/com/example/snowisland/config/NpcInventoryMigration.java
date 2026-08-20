package com.example.snowisland.config;

import com.example.snowisland.service.NpcInventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hibernate 会建表；此处只给尚无背包的既有 NPC 按职业补发玩家职业初始物资。
 */
@Component
@Order(12)
public class NpcInventoryMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(NpcInventoryMigration.class);

    @Autowired
    private NpcInventoryService npcInventoryService;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            int granted = npcInventoryService.seedAllEmptyInventories();
            if (granted > 0) {
                logger.info("已为既有 NPC 补发 {} 份职业初始物资", granted);
            }
        } catch (Exception e) {
            logger.error("NPC 背包种子失败: {}", e.getMessage(), e);
        }
    }
}

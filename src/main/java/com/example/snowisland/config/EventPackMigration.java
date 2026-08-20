package com.example.snowisland.config;

import com.example.snowisland.entity.EventPack;
import com.example.snowisland.entity.IslandEvent;
import com.example.snowisland.repository.EventPackRepository;
import com.example.snowisland.repository.IslandEventRepository;
import com.example.snowisland.service.ExplorationDataInitService;
import com.example.snowisland.service.ExplorationDataInitService.ExplorationEventData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 幂等：种子四个顶层事件包，并按事件名把已有 island_event划入对应包。
 */
@Component
@Order(6)
public class EventPackMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(EventPackMigration.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ExplorationDataInitService explorationDataInitService;

    @Autowired
    private EventPackRepository eventPackRepository;

    @Autowired
    private IslandEventRepository islandEventRepository;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            ensureColumn("island_event", "pack_id", "INT NULL");
            ensureColumn("island_event", "pack_name", "VARCHAR(100) NULL");
            ensureColumn("island_event", "source_number", "INT NULL");
            explorationDataInitService.ensureSeedPacks();
            assignExistingEvents();
            int inserted = explorationDataInitService.insertMissingEventsFromFile();
            if (inserted > 0) {
                logger.info("已从文件补入 {} 条缺失探索事件", inserted);
            }
            int refreshed = explorationDataInitService.refreshSeedEventsFromFile();
            if (refreshed > 0) {
                logger.info("已按文件刷新 {} 条种子探索事件", refreshed);
            }
        } catch (Exception e) {
            logger.error("事件包迁移失败: {}", e.getMessage(), e);
        }
    }

    private void assignExistingEvents() {
        List<ExplorationEventData> parsed = explorationDataInitService.parseEventsFromFile();
        Map<String, ExplorationEventData> byName = new HashMap<>();
        for (ExplorationEventData data : parsed) {
            if (data.name != null && !data.name.isEmpty()) {
                byName.put(data.name, data);
            }
        }

        int assigned = 0;
        for (IslandEvent event : islandEventRepository.findAll()) {
            boolean needsPack = event.getPackId() == null;
            boolean needsNumber = event.getSourceNumber() == null;
            if (!needsPack && !needsNumber) {
                continue;
            }
            ExplorationEventData data = byName.get(event.getName());
            if (data == null) {
                continue;
            }
            boolean changed = false;
            if (needsPack) {
                String packName = EventPack.nameForEventNumber(data.eventNumber);
                EventPack pack = eventPackRepository.findByName(packName).orElse(null);
                if (pack != null) {
                    event.setPackId(pack.getId());
                    event.setPackName(pack.getName());
                    changed = true;
                }
            }
            if (needsNumber && data.eventNumber != null) {
                event.setSourceNumber(data.eventNumber);
                changed = true;
            }
            if (changed) {
                islandEventRepository.save(event);
                assigned++;
            }
        }
        logger.info("事件包迁移完成，按名称补全 {} 条事件的 pack/编号", assigned);
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

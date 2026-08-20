package com.example.snowisland.config;

import com.example.snowisland.service.NpcRosterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 幂等：开放 12 名 NPC 的人设/对话风格，补齐缺失角色，移除文档标注「没开」的 NPC。
 */
@Component
@Order(11)
public class NpcRosterMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(NpcRosterMigration.class);

    @Autowired
    private NpcRosterService npcRosterService;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            npcRosterService.ensureOnBoot();
        } catch (Exception e) {
            logger.error("NPC 名册迁移失败: {}", e.getMessage(), e);
        }
    }
}

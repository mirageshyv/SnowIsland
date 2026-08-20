package com.example.snowisland.service;

import com.example.snowisland.entity.LocationNpc;
import com.example.snowisland.repository.LocationNpcRepository;
import com.example.snowisland.util.NpcRoster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class NpcRosterService {

    private static final Logger logger = LoggerFactory.getLogger(NpcRosterService.class);

    @Autowired
    private LocationNpcRepository locationNpcRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 启动时：加宽对话风格列、补齐缺失 NPC、写入人设文案、移除未开放 NPC。
     * 不改阵营态度与状态，以免覆盖对局中的劳工结算或伤亡。
     */
    @Transactional
    public int ensureOnBoot() {
        widenDialogueStyleColumn();
        int upserted = 0;
        for (NpcRoster.Spec spec : NpcRoster.canonical()) {
            LocationNpc npc = findByName(spec.name);
            if (npc == null) {
                npc = new LocationNpc();
                applyIdentity(npc, spec);
                applyPrompt(npc, spec);
                applyAttitudes(npc, spec);
                npc.setStatus("正常");
                npc.setDailyTradeLimit(1);
                locationNpcRepository.save(npc);
                upserted++;
                logger.info("已补入开放 NPC「{}」", spec.name);
            } else {
                boolean firstPrompt = npc.getPersonality() == null || npc.getPersonality().trim().isEmpty();
                applyPrompt(npc, spec);
                if (firstPrompt) {
                    applyAttitudes(npc, spec);
                }
                if (npc.getJob() == null || npc.getJob().isEmpty()) {
                    npc.setJob(spec.job);
                }
                if (npc.getGender() == null) {
                    npc.setGender(spec.gender);
                }
                locationNpcRepository.save(npc);
                upserted++;
            }
        }
        int removed = removeUnusedNpcs();
        logger.info("NPC 名册已同步：写入/更新 {} 人，移除未开放 {} 人", upserted, removed);
        return upserted;
    }

    /**
     * 游戏重置：12 名开放 NPC 回到出生点、初始阵营态度与正常状态，并删除未开放 NPC。
     */
    @Transactional
    public int resetToCanonical() {
        widenDialogueStyleColumn();
        int synced = 0;
        for (NpcRoster.Spec spec : NpcRoster.canonical()) {
            LocationNpc npc = findByName(spec.name);
            if (npc == null) {
                npc = new LocationNpc();
                npc.setDailyTradeLimit(1);
            }
            applyIdentity(npc, spec);
            applyPrompt(npc, spec);
            applyAttitudes(npc, spec);
            npc.setStatus("正常");
            if (npc.getDailyTradeLimit() == null) {
                npc.setDailyTradeLimit(1);
            }
            locationNpcRepository.save(npc);
            synced++;
        }
        int removed = removeUnusedNpcs();
        logger.info("已将 NPC 重置为开放名册 {} 人，移除未开放 {} 人", synced, removed);
        return synced;
    }

    private LocationNpc findByName(String name) {
        List<LocationNpc> matches = locationNpcRepository.findByName(name);
        return matches.isEmpty() ? null : matches.get(0);
    }

    private static void applyIdentity(LocationNpc npc, NpcRoster.Spec spec) {
        npc.setName(spec.name);
        npc.setJob(spec.job);
        npc.setGender(spec.gender);
        npc.setLocationId(spec.locationId);
    }

    private static void applyPrompt(LocationNpc npc, NpcRoster.Spec spec) {
        npc.setIntroduction(spec.introduction);
        npc.setPersonality(spec.personality);
        npc.setDialogueStyle(spec.dialogueStyle);
    }

    private static void applyAttitudes(LocationNpc npc, NpcRoster.Spec spec) {
        npc.setAttitudeRuler(spec.attitudeRuler);
        npc.setAttitudeRebel(spec.attitudeRebel);
        npc.setAttitudeAdventurer(spec.attitudeAdventurer);
        npc.setAttitudeScourge(spec.attitudeScourge);
    }

    private void widenDialogueStyleColumn() {
        try {
            Number count = (Number) entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'location_npc' "
                            + "AND COLUMN_NAME = 'dialogue_style' AND DATA_TYPE = 'varchar'"
            ).getSingleResult();
            if (count.intValue() > 0) {
                entityManager.createNativeQuery(
                        "ALTER TABLE location_npc MODIFY COLUMN dialogue_style TEXT"
                ).executeUpdate();
                logger.info("location_npc.dialogue_style 已加宽为 TEXT");
            }
        } catch (Exception e) {
            logger.warn("加宽 dialogue_style 列失败: {}", e.getMessage());
        }
    }

    private int removeUnusedNpcs() {
        int removed = 0;
        for (String name : NpcRoster.unusedNames()) {
            List<LocationNpc> extra = locationNpcRepository.findByName(name);
            for (LocationNpc npc : extra) {
                deleteNpcDependents(npc.getId());
                locationNpcRepository.delete(npc);
                removed++;
                logger.info("已移除未开放 NPC「{}」", name);
            }
        }
        return removed;
    }

    private void deleteNpcDependents(Integer npcId) {
        String[] tables = {
                "npc_help_config", "npc_trade_config", "npc_items", "npc_item",
                "npc_favor", "npc_favor_adjustment", "npc_dialogue",
                "npc_help_record", "npc_trade_record",
                "npc_daily_dialogue_count", "npc_daily_trade_count",
                "npc_daily_consumption", "player_npc_recognition", "clue_trigger_log"
        };
        for (String table : tables) {
            try {
                entityManager.createNativeQuery("DELETE FROM " + table + " WHERE npc_id = ?1")
                        .setParameter(1, npcId)
                        .executeUpdate();
            } catch (Exception ignored) {
                // 表不存在或无 npc_id 列时跳过
            }
        }
    }
}

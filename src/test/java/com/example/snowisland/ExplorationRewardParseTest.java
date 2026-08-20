package com.example.snowisland;

import com.example.snowisland.service.ExplorationDataInitService;
import com.example.snowisland.service.ExplorationDataInitService.ExplorationEventData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Brace-format loot parser: recognized names, unmatched names, intentional-none.
 * Instantiates the service directly; EntityManager is null so live 图鉴 is skipped
 * and hardcoded fallback maps are used.
 */
class ExplorationRewardParseTest {

    private final ExplorationDataInitService service = new ExplorationDataInitService();

    @Test
    @DisplayName("EM 为 null 时硬编码回退：绳索 (10米) → unmatched 为空")
    void recognizedReward_unmatchedEmpty() {
        List<ExplorationEventData> events = service.parseBraceFormat(
                "{5}{废弃哨站\n地点描述：废墟。\n可获得物资：绳索 (10米)\n历史碎片：刻痕。\n}");
        assertEquals(1, events.size());
        ExplorationEventData data = events.get(0);
        assertTrue(data.unmatchedRewards == null || data.unmatchedRewards.isEmpty());
        assertEquals(1, data.rewards.size());
    }

    @Test
    @DisplayName("EM 为 null 时硬编码回退：未知寒铁镐 (1把) 仍 unmatched")
    void unknownReward_unmatchedContainsName() {
        List<ExplorationEventData> events = service.parseBraceFormat(
                "{5}{废弃哨站\n地点描述：废墟。\n可获得物资：寒铁镐 (1把)\n历史碎片：刻痕。\n}");
        assertEquals(1, events.size());
        ExplorationEventData data = events.get(0);
        assertTrue(data.unmatchedRewards.contains("寒铁镐"));
        assertTrue(data.rewards == null || data.rewards.isEmpty());
    }

    @Test
    @DisplayName("无物资 → unmatched 为空且 rewards 为空")
    void noneLoot_emptyRewardsAndUnmatched() {
        List<ExplorationEventData> events = service.parseBraceFormat(
                "{5}{废弃哨站\n地点描述：废墟。\n可获得物资：无物资\n历史碎片：刻痕。\n}");
        assertEquals(1, events.size());
        ExplorationEventData data = events.get(0);
        assertTrue(data.unmatchedRewards == null || data.unmatchedRewards.isEmpty());
        assertTrue(data.rewards == null || data.rewards.isEmpty());
    }
}

package com.example.snowisland;

import com.example.snowisland.service.IslandExplorationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 岛屿探索事件难度单元测试
 *
 * 覆盖：
 *  1. 难度值边界验证（0、20 及越界）
 *  2. 探索值加成计算（火把、手电筒、蜡烛、绳索）
 *  3. 难度范围常量正确性
 *
 * 由于 IslandExplorationService#isValidDifficulty 为静态工具方法，无需 Spring 上下文。
 */
class IslandEventDifficultyTest {

    // ---------- 1. 边界值验证 ----------

    @Test
    @DisplayName("难度值合法边界 - 最小值 0 通过校验")
    void validDifficulty_minBoundary() {
        assertTrue(IslandExplorationService.isValidDifficulty(0),
                "难度 0 必须在合法范围内");
    }

    @Test
    @DisplayName("难度值合法边界 - 最大值 20 通过校验")
    void validDifficulty_maxBoundary() {
        assertTrue(IslandExplorationService.isValidDifficulty(20),
                "难度 20 必须在合法范围内");
    }

    @Test
    @DisplayName("难度值合法边界 - 中间值通过校验")
    void validDifficulty_middleValues() {
        for (int d = 0; d <= 20; d++) {
            assertTrue(IslandExplorationService.isValidDifficulty(d),
                    "难度 " + d + " 必须在合法范围内");
        }
    }

    @Test
    @DisplayName("难度值非法 - 小于 0 拒绝")
    void invalidDifficulty_belowMin() {
        assertFalse(IslandExplorationService.isValidDifficulty(-1));
        assertFalse(IslandExplorationService.isValidDifficulty(-100));
        assertFalse(IslandExplorationService.isValidDifficulty(Integer.MIN_VALUE));
    }

    @Test
    @DisplayName("难度值非法 - 大于 20 拒绝")
    void invalidDifficulty_aboveMax() {
        assertFalse(IslandExplorationService.isValidDifficulty(21));
        assertFalse(IslandExplorationService.isValidDifficulty(100));
        assertFalse(IslandExplorationService.isValidDifficulty(Integer.MAX_VALUE));
    }

    @Test
    @DisplayName("难度值非法 - null 拒绝")
    void invalidDifficulty_null() {
        assertFalse(IslandExplorationService.isValidDifficulty(null),
                "null 难度应被拒绝");
    }

    // ---------- 2. 探索值加成计算 ----------

    @Test
    @DisplayName("探索值加成 - 火把 +7")
    void explorationBonus_torch() {
        assertEquals(7, IslandExplorationService.EXPLORATION_ITEM_BONUS.get(26),
                "火把探索值加成为 7");
    }

    @Test
    @DisplayName("探索值加成 - 手电筒 +5")
    void explorationBonus_flashlight() {
        assertEquals(5, IslandExplorationService.EXPLORATION_ITEM_BONUS.get(2),
                "手电筒探索值加成为 5");
    }

    @Test
    @DisplayName("探索值加成 - 蜡烛 +2")
    void explorationBonus_candle() {
        assertEquals(2, IslandExplorationService.EXPLORATION_ITEM_BONUS.get(13),
                "蜡烛探索值加成为 2");
    }

    @Test
    @DisplayName("探索值加成 - 绳索 +1")
    void explorationBonus_rope() {
        assertEquals(1, IslandExplorationService.EXPLORATION_ITEM_BONUS.get(3),
                "绳索探索值加成为 1");
    }

    @Test
    @DisplayName("探索值上限 - 最大投入 15 点")
    void explorationMaxInvestPoints() {
        assertEquals(15, IslandExplorationService.MAX_INVEST_POINTS,
                "最大投入探索值为 15");
    }

    // ---------- 3. 难度常量 ----------

    @Test
    @DisplayName("难度常量符合规范定义")
    void difficultyConstants() {
        assertAll(
                () -> assertEquals(0, IslandExplorationService.MIN_DIFFICULTY,
                        "难度最小值必须为 0"),
                () -> assertEquals(20, IslandExplorationService.MAX_DIFFICULTY,
                        "难度最大值必须为 20"),
                () -> assertTrue(IslandExplorationService.MAX_DIFFICULTY > IslandExplorationService.MIN_DIFFICULTY,
                        "难度上限必须大于下限")
        );
    }
}

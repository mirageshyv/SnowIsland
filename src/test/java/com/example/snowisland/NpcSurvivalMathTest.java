package com.example.snowisland;

import com.example.snowisland.util.NpcSurvivalMath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NpcSurvivalMathTest {

    @Test
    public void reserveBlocksLastDayOfFoodAndHeat() {
        assertEquals(4, NpcSurvivalMath.sellableFood(6, 2));
        assertEquals(0, NpcSurvivalMath.sellableFood(2, 2));
        assertEquals(15, NpcSurvivalMath.sellableWood(30, 0, 15));
        assertEquals(0, NpcSurvivalMath.sellableWood(15, 0, 15));
        assertEquals(30, NpcSurvivalMath.sellableWood(30, 1, 15));
        assertEquals(0, NpcSurvivalMath.sellableFuel(0, 1, 15));
        assertEquals(1, NpcSurvivalMath.sellableFuel(15, 1, 15));
        assertTrue(NpcSurvivalMath.reserveHolds(2, 15, 0, 2, 15));
        assertFalse(NpcSurvivalMath.reserveHolds(1, 15, 0, 2, 15));
    }

    @Test
    public void heatSpendsWoodThenFuel() {
        NpcSurvivalMath.HeatSpend onlyWood = NpcSurvivalMath.spendHeat(20, 2, 15);
        assertEquals(15, onlyWood.woodUsed);
        assertEquals(0, onlyWood.fuelUsed);
        assertEquals(15, onlyWood.heatGained);

        NpcSurvivalMath.HeatSpend mixed = NpcSurvivalMath.spendHeat(5, 2, 15);
        assertEquals(5, mixed.woodUsed);
        assertEquals(1, mixed.fuelUsed);
        assertEquals(20, mixed.heatGained);
        assertEquals(0, mixed.shortfall);
    }

    @Test
    public void weakThenDeathAndRecovery() {
        assertEquals("虚弱", NpcSurvivalMath.nextStatusAfterDay("正常", false));
        assertEquals("死亡", NpcSurvivalMath.nextStatusAfterDay("虚弱", false));
        assertEquals("正常", NpcSurvivalMath.nextStatusAfterDay("虚弱", true));
        assertEquals("受伤", NpcSurvivalMath.nextStatusAfterDay("受伤", true));
        assertEquals("死亡", NpcSurvivalMath.nextStatusAfterDay("死亡", false));
        assertEquals("失踪", NpcSurvivalMath.nextStatusAfterDay("失踪", false));
        assertFalse(NpcSurvivalMath.participatesInDailySurvival("死亡"));
        assertTrue(NpcSurvivalMath.participatesInDailySurvival("虚弱"));
    }
}

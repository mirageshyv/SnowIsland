package com.example.snowisland.util;

import com.example.snowisland.entity.TradeItem.ItemType;
import com.example.snowisland.util.NpcTradeProposalMath.Line;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NpcTradeProposalMathTest {

    @Test
    public void foodShortfallTakesFoodOnly() {
        List<Line> take = NpcTradeProposalMath.survivalTake(0, 30, 2, 2, 25);
        assertEquals(1, take.size());
        assertEquals(ItemType.material, take.get(0).itemType);
        assertEquals(ItemCatalog.FOOD_MATERIAL_ID, take.get(0).itemId);
        assertEquals(2, take.get(0).quantity);
        assertFalse(NpcTradeProposalMath.canSurviveToday(0, 30, 2, 2, 25));
    }

    @Test
    public void heatShortfallUsesFuelWhenAtLeast15() {
        List<Line> take = NpcTradeProposalMath.survivalTake(5, 0, 0, 2, 25);
        assertEquals(1, take.size());
        assertEquals(ItemCatalog.FUEL_MATERIAL_ID, take.get(0).itemId);
        assertEquals(2, take.get(0).quantity);
    }

    @Test
    public void smallHeatShortfallUsesWood() {
        List<Line> take = NpcTradeProposalMath.survivalTake(5, 20, 0, 2, 25);
        assertEquals(1, take.size());
        assertEquals(ItemCatalog.WOOD_MATERIAL_ID, take.get(0).itemId);
        assertEquals(5, take.get(0).quantity);
    }

    @Test
    public void survivingNeedsNoTake() {
        assertTrue(NpcTradeProposalMath.canSurviveToday(2, 25, 0, 2, 25));
        assertTrue(NpcTradeProposalMath.survivalTake(2, 25, 0, 2, 25).isEmpty());
    }

    @Test
    public void deterministicGivePrefersNonSurvivalAndCapsLines() {
        List<Line> surplus = new ArrayList<>();
        surplus.add(new Line(ItemType.material, ItemCatalog.FOOD_MATERIAL_ID, 4, ItemCatalog.FOOD_NAME));
        surplus.add(new Line(ItemType.material, 3, 8, "绳索"));
        surplus.add(new Line(ItemType.item, 10, 2, "朗姆酒"));
        surplus.add(new Line(ItemType.weapon, 9, 1, "斧头"));
        surplus.add(new Line(ItemType.material, 4, 3, "木板"));
        List<Line> give = NpcTradeProposalMath.deterministicGive(surplus, 80);
        assertEquals(3, give.size());
        assertEquals(3, give.get(0).itemId);
        assertEquals(5, give.get(0).quantity);
        assertEquals(10, give.get(1).itemId);
        assertEquals(1, give.get(1).quantity);
        assertEquals(4, give.get(2).itemId);
    }

    @Test
    public void noSurplusMeansEmptyGive() {
        assertTrue(NpcTradeProposalMath.deterministicGive(new ArrayList<Line>()).isEmpty());
    }

    @Test
    public void opportunisticTakeClampsAndRejectsNonTradable() {
        Line ok = NpcTradeProposalMath.clampOpportunisticTake(ItemType.material, 3, 9, "绳索", true);
        assertNotNull(ok);
        assertEquals(5, ok.quantity);
        assertNull(NpcTradeProposalMath.clampOpportunisticTake(ItemType.item, 30, 1, "钥匙", false));
        Line item = NpcTradeProposalMath.clampOpportunisticTake(ItemType.item, 10, 4, "朗姆酒", true);
        assertEquals(1, item.quantity);
    }

    @Test
    public void clampGiveAgainstSellableDropsWhenGone() {
        Line line = new Line(ItemType.material, 3, 4, "绳索");
        assertNull(NpcTradeProposalMath.clampGiveAgainstSellable(line, 0));
        assertEquals(2, NpcTradeProposalMath.clampGiveAgainstSellable(line, 2).quantity);
    }

    @Test
    public void lowFavorGivesFewerLinesAndSmallerMaterialCaps() {
        assertEquals(1, NpcTradeProposalMath.maxGiveLines(0));
        assertEquals(2, NpcTradeProposalMath.maxGiveLines(40));
        assertEquals(3, NpcTradeProposalMath.maxGiveLines(80));
        assertEquals(1, NpcTradeProposalMath.maxOfferedExtras(0));
        assertEquals(1, NpcTradeProposalMath.maxOfferedExtras(40));
        assertEquals(1, NpcTradeProposalMath.maxGreedyExtraTakes(0));
        assertEquals(0, NpcTradeProposalMath.maxGreedyExtraTakes(40));
        List<Line> surplus = new ArrayList<>();
        surplus.add(new Line(ItemType.material, 3, 8, "绳索"));
        surplus.add(new Line(ItemType.item, 10, 1, "朗姆酒"));
        List<Line> stingy = NpcTradeProposalMath.deterministicGive(surplus, 0);
        assertEquals(1, stingy.size());
        assertEquals(2, stingy.get(0).quantity);
        surplus.add(new Line(ItemType.weapon, 9, 1, "斧头"));
        List<Line> noWeapon = NpcTradeProposalMath.deterministicGive(surplus, 0);
        assertEquals(1, noWeapon.size());
        assertEquals(3, noWeapon.get(0).itemId);
        assertFalse(NpcTradeProposalMath.mayGive(ItemType.weapon, 9, 0));
        assertTrue(NpcTradeProposalMath.mayGive(ItemType.weapon, 9, 80));
    }

}
